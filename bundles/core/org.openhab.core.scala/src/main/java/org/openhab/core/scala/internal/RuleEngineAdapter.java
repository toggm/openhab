package org.openhab.core.scala.internal;

import org.openhab.core.items.Item;

public interface RuleEngineAdapter {

	public abstract boolean initialize();

	public abstract void itemRemoved(Item item);

	public abstract void itemAdded(Item item);

	public abstract void reevaluateRules();

	public abstract void sourceFileChanged();

}