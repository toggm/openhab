/**
 * Copyright (c) 2010-2013, openHAB.org and others.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 */
package org.openhab.core.scala.internal;

import static org.openhab.core.events.EventConstants.TOPIC_PREFIX;
import static org.openhab.core.events.EventConstants.TOPIC_SEPERATOR;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Dictionary;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.commons.vfs2.FileChangeEvent;
import org.apache.commons.vfs2.FileListener;
import org.apache.commons.vfs2.FileObject;
import org.apache.commons.vfs2.FileSystemException;
import org.apache.commons.vfs2.FileSystemManager;
import org.apache.commons.vfs2.VFS;
import org.apache.commons.vfs2.impl.DefaultFileMonitor;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ItemRegistryChangeListener;
import org.openhab.core.items.StateChangeListener;
import org.openhab.core.scala.RuleEngineExecutor;
import org.openhab.core.service.AbstractActiveService;
import org.openhab.core.types.Command;
import org.openhab.core.types.EventType;
import org.openhab.core.types.State;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RuleService extends AbstractActiveService implements
		ManagedService, EventHandler, ItemRegistryChangeListener,
		StateChangeListener, FileListener {

	private static final String CONFIGURATION_BASE = "/configuration/scala";

	private long refreshInterval = 200;

	private List<Item> eventQueue = Collections
			.synchronizedList(new ArrayList<Item>());

	private DefaultFileMonitor fileMonitor;

	private static final Logger logger = LoggerFactory
			.getLogger(RuleService.class);

	private HammurabiAdapter scalaRuleEngineAdapter;

	private ItemRegistry itemRegistry;

	private ScalaRuleCompiler scalaRuleCompiler;

	public void activate() {

		scalaRuleEngineAdapter = new HammurabiAdapter();
		scalaRuleCompiler = new ScalaRuleCompiler(CONFIGURATION_BASE);
		boolean successFully = scalaRuleEngineAdapter.initialize();

		// now add all registered items to the session
		if (itemRegistry != null) {
			for (Item item : itemRegistry.getItems()) {
				itemAdded(item);
			}
		}

		// register rule scanner
		FileObject file;
		try {
			FileSystemManager fsManager = VFS.getManager();
			file = fsManager.resolveFile(CONFIGURATION_BASE);

			fileMonitor = new DefaultFileMonitor(this);
			fileMonitor.setRecursive(true);
			fileMonitor.setDelay(refreshInterval);
			fileMonitor.addFile(file);

		} catch (FileSystemException e) {
			e.printStackTrace();
			logger.warn("Coudln't start filemonitor", e);
			successFully = false;
		}

		setProperlyConfigured(successFully);
	}

	public void deactivate() {
		shutdown();
	}

	public void setItemRegistry(ItemRegistry itemRegistry) {
		this.itemRegistry = itemRegistry;
		itemRegistry.addItemRegistryChangeListener(this);
	}

	public void unsetItemRegistry(ItemRegistry itemRegistry) {
		itemRegistry.removeItemRegistryChangeListener(this);
		this.itemRegistry = null;
	}

	/**
	 * {@inheritDoc}scalaRuleCompiler
	 */
	@SuppressWarnings("rawtypes")
	public void updated(Dictionary config) throws ConfigurationException {
		if (config != null) {
			String evalIntervalString = (String) config.get("evalInterval");
			if (StringUtils.isNotBlank(evalIntervalString)) {
				refreshInterval = Long.parseLong(evalIntervalString);

				// restart scanner
				fileMonitor.setDelay(refreshInterval);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void allItemsChanged(Collection<String> oldItemNames) {

		Collection<Item> items = itemRegistry.getItems();
		for (Item item : items) {
			if (oldItemNames.contains(item.getName())) {
				// first remove all previous items from the session
				scalaRuleEngineAdapter.itemRemoved(item);

				// then add the current ones again
				scalaRuleEngineAdapter.itemAdded(item);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void itemAdded(Item item) {
		scalaRuleEngineAdapter.itemAdded(item);
	}

	/**
	 * {@inheritDoc}
	 */
	public void itemRemoved(Item item) {
		scalaRuleEngineAdapter.itemRemoved(item);
		if (item instanceof GenericItem) {
			GenericItem genericItem = (GenericItem) item;
			genericItem.removeStateChangeListener(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void stateChanged(Item item, State oldState, State newState) {
		eventQueue.add(item);
	}

	/**
	 * {@inheritDoc}
	 */
	public void stateUpdated(Item item, State state) {
		eventQueue.add(item);
	}

	public void receiveCommand(String itemName, Command command) {
		try {
			Item item = itemRegistry.getItem(itemName);
			eventQueue.add(item);
		} catch (ItemNotFoundException e) {
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected synchronized void execute() {
		// remove all previous events from the session

		ArrayList<Item> clonedQueue = new ArrayList<Item>(eventQueue);
		eventQueue.clear();

		// push modified items into workingmemory
		for (Item item : clonedQueue) {
			scalaRuleEngineAdapter.itemRemoved(item);
			scalaRuleEngineAdapter.itemAdded(item);
		}

		if (clonedQueue.size() > 0) {
			// reevaluate rules
			scalaRuleEngineAdapter.reevaluateRules();
		}
	}

	@Override
	protected long getRefreshInterval() {
		return refreshInterval;
	}

	@Override
	protected String getName() {
		return "Rule Evaluation Service";
	}

	/**
	 * {@inheritDoc}
	 */
	public void handleEvent(Event event) {
		String itemName = (String) event.getProperty("item");

		String topic = event.getTopic();
		String[] topicParts = topic.split(TOPIC_SEPERATOR);

		if (!(topicParts.length > 2) || !topicParts[0].equals(TOPIC_PREFIX)) {
			return; // we have received an event with an invalid topic
		}
		String operation = topicParts[1];

		if (operation.equals(EventType.COMMAND.toString())) {
			Command command = (Command) event.getProperty("command");
			if (command != null)
				receiveCommand(itemName, command);
		}
	}

	public void fileChanged(FileChangeEvent event) throws Exception {
		recompileRules();
	}

	public void fileCreated(FileChangeEvent event) throws Exception {
		recompileRules();
	}

	public void fileDeleted(FileChangeEvent event) throws Exception {
		recompileRules();
	}

	private void recompileRules() {
		List<RuleEngineExecutor> newRuleEngines = scalaRuleCompiler
				.recompileRules();

		// reinject new rules
		scalaRuleEngineAdapter.setRuleEngines(newRuleEngines);
	}
}
