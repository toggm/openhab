package org.openhab.core.scala.internal;

import org.openhab.core.items.Item;
import org.openhab.core.scala.model.RuleEvent;

public interface RuleEngineAdapter {

	public abstract void itemRemoved(Item item);

	public abstract void itemAdded(Item item);

	public abstract void receivedEvent(RuleEvent event);

	public abstract void reevaluateRules();

	public abstract void sourceFileChanged();

}