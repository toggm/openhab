package org.openhab.core.scala.internal;

import hammurabi.WorkingMemory;

import java.util.LinkedList;
import java.util.List;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.scala.RuleEngineExecutor;
import org.openhab.core.scala.model.RuleEngineListener;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class HammurabiAdapter {

	private WorkingMemory workingMemory;

	private List<RuleEngineExecutor> ruleEngines = new LinkedList<RuleEngineExecutor>();

	public boolean initialize() {

		// initialize hammurabi
		workingMemory = new WorkingMemory();

		return true;
	}

	public void itemRemoved(Item item) {
		workingMemory.$minus(item);
	}

	public void itemAdded(Item item) {
		workingMemory.$plus(item);
	}

	public void reevaluateRules() {
		if (ruleEngines != null) {
			for (RuleEngineExecutor ruleEngine : ruleEngines) {
				applyOn(ruleEngine);
			}
		}
	}

	private void applyOn(RuleEngineExecutor ruleEngine) {

		// apply workingmemory and handle results
		ruleEngine.execOn(workingMemory, new RuleEngineListener() {

			public void updated(Item item, State state) {
				ItemRegistry registry = (ItemRegistry) RulesActivator.itemRegistryTracker
						.getService();
				EventPublisher publisher = (EventPublisher) RulesActivator.eventPublisherTracker
						.getService();
				if (publisher != null && registry != null) {
					publisher.postUpdate(item.getName(), state);
				}
			}

			public void send(Item item, Command cmd) {
				ItemRegistry registry = (ItemRegistry) RulesActivator.itemRegistryTracker
						.getService();
				EventPublisher publisher = (EventPublisher) RulesActivator.eventPublisherTracker
						.getService();

				if (publisher != null && registry != null) {
					publisher.sendCommand(item.getName(), cmd);
				}
			}
		});
	}

	public void setRuleEngines(List<RuleEngineExecutor> newRuleEngines) {
		// remove old ruleengines
		ruleEngines.clear();

		// add all new ruleenignes
		ruleEngines.addAll(newRuleEngines);

		// reevaluate
		reevaluateRules();
	}
}
