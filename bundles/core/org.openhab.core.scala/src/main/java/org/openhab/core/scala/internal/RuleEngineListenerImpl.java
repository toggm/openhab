package org.openhab.core.scala.internal;

import org.openhab.core.events.EventPublisher;
import org.openhab.core.items.Item;
import org.openhab.core.items.ItemRegistry;
import org.openhab.core.scala.model.RuleEngineListener;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;

public class RuleEngineListenerImpl implements RuleEngineListener {

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
}
