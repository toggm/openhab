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
import hammurabi.RuleEngine;
import hammurabi.WorkingMemory;

import java.io.File;
import java.io.FilenameFilter;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Collection;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import org.apache.commons.lang.StringUtils;
import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.GenericItem;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemNotFoundException;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.items.ItemRegistryChangeListener;
import org.openhab.core.items.StateChangeListener;
import org.openhab.core.scala.RuleEngineExecutor;
import org.openhab.core.scala.model.Changeset;
import org.openhab.core.scala.model.RuleEngineListener;
import org.openhab.core.service.AbstractActiveService;
import org.openhab.core.types.Command;
import org.openhab.core.types.EventType;
import org.openhab.core.types.State;
import org.openhab.core.types.TypeParser;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.cm.ManagedService;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.Option;
import scala.Tuple2;

public class RuleService extends AbstractActiveService implements
		ManagedService, EventHandler, ItemRegistryChangeListener,
		StateChangeListener {

	static private final Logger logger = LoggerFactory
			.getLogger(RuleService.class);

	private ItemRegistry itemRegistry = null;

	private long refreshInterval = 200;

	private WorkingMemory workingMemory;
	
	private TimerTask scanConfigurationFilesTask;
	
	private Timer timer;
	
	private Map<String, Long> configTimestamps = new HashMap<String, Long>();
	
	private Map<String, RuleEngineExecutor> ruleEngines = new HashMap<String, RuleEngineExecutor>();
	
	public void activate() {
		
		//initialize hammurabi
		workingMemory = new WorkingMemory();	
		
		// read all rule files
		timer = new Timer();
				
		// now add all registered items to the session
		if (itemRegistry != null) {
			for (Item item : itemRegistry.getItems()) {
				itemAdded(item);
			}
		}

		setProperlyConfigured(true);
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
	 * {@inheritDoc}
	 */
	@SuppressWarnings("rawtypes")
	public void updated(Dictionary config) throws ConfigurationException {
		if (config != null) {
			String evalIntervalString = (String) config.get("evalInterval");
			if (StringUtils.isNotBlank(evalIntervalString)) {
				refreshInterval = Long.parseLong(evalIntervalString);
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
				internalItemRemoved(item);				

				// then add the current ones again
				internalItemAdded(item);
			}
		}
	}

	private void internalItemAdded(Item item) {
		workingMemory.$plus(item);
	}

	private void internalItemRemoved(Item item) {		
		workingMemory.$minus(item);
	}

	/**
	 * {@inheritDoc}
	 */
	public void itemAdded(Item item) {
		internalItemAdded(item);
	}

	/**
	 * {@inheritDoc}
	 */
	public void itemRemoved(Item item) {
		internalItemRemoved(item);
		if (item instanceof GenericItem) {
			GenericItem genericItem = (GenericItem) item;
			genericItem.removeStateChangeListener(this);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public void stateChanged(Item item, State oldState, State newState) {
		//eventQueue.add(new StateEvent(item, oldState, newState));
	}

	/**
	 * {@inheritDoc}
	 */
	public void stateUpdated(Item item, State state) {
		//eventQueue.add(new StateEvent(item, state));
	}

	public void receiveCommand(String itemName, Command command) {
		try {
			Item item = itemRegistry.getItem(itemName);
			//TODO eventQueue.add(new CommandEvent(item, command));
		} catch (ItemNotFoundException e) {
		}
	}

	/**
	 * @{inheritDoc
	 */
	@Override
	protected synchronized void execute() {
		// remove all previous events from the session
		
//		ArrayList<RuleEvent> clonedQueue = new ArrayList<RuleEvent>(eventQueue);
//		eventQueue.clear();
	

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
	

	private void scanConfiguration(File config) {
		String name = config.getName();
		if (configTimestamps.containsKey(name)) {
			//check modification timestamp
			if (configTimestamps.get(name) < config.lastModified()) {
				//changed, rescan and reapply rules
				
				//remove from current cache
				ruleEngines.remove(name);
				
				//readd
				scanConfigurationFile(config);
			}
		}
		else {
			//new config file, scan and apply rules
			scanConfigurationFile(config);
		}
	}
	
	private void scanConfigurationFile(File config) {
		//compile scala file
		RuleEngineExecutor ruleEngine = null;
		
		//add to rule engines cache
		ruleEngines.put(config.getName(), ruleEngine);
		
		//apply workingmemory
		applyOn(ruleEngine);
	}
	
	private void applyOn(RuleEngineExecutor ruleEngine) {
		
		
		//apply workingmemory and handle results
		ruleEngine.execOn(workingMemory, new RuleEngineListener() {
			
			public void updated(Item item, State state) {
				ItemRegistry registry = (ItemRegistry) RulesActivator.itemRegistryTracker.getService();
				EventPublisher publisher = (EventPublisher) RulesActivator.eventPublisherTracker.getService();
				if(publisher!=null && registry!=null) {
					publisher.postUpdate(item.getName(), state);					
				}
			}
			
			public void send(Item item, Command cmd) {
				ItemRegistry registry = (ItemRegistry) RulesActivator.itemRegistryTracker.getService();
				EventPublisher publisher = (EventPublisher) RulesActivator.eventPublisherTracker.getService();
				
				if(publisher!=null && registry!=null) {
					publisher.sendCommand(item.getName(), cmd);				
				}
			}
		});		
	}
	
	private class ScanConfigurationFilesTask extends TimerTask {

		private String resourceBase;

		public ScanConfigurationFilesTask(String resourceBase) {
			this.resourceBase = resourceBase;
		}
		
		@Override
		public void run() {
			URL resource = getClass().getResource(resourceBase);
			if (resource != null) {
				File file;
				try {
					file = new File(resource.toURI());
					File[] files = file.listFiles(new FilenameFilter() {
						
						public boolean accept(File file, String arg1) {
							return file.getName().endsWith(".scala");
						}
					});
					for (File config: files) {
						scanConfiguration(config);
					}
				} catch (URISyntaxException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}				
		}
	}
}
