package org.openhab.core.scala

import hammurabi.WorkingMemory
import hammurabi.RuleEngine
import org.openhab.core.items.Item
import org.openhab.core.types.State
import org.openhab.core.types.Command
import org.openhab.core.scala.model.RuleEngineListener
import org.openhab.core.scala.model.BusEvent

class RuleEngineExecutor(val ruleEngine: RuleEngine) {

  def execOn(memory: WorkingMemory, listener: RuleEngineListener) = {

	BusEvent.synchronized {
		//clear static objects map before executing ruleengine
		BusEvent.dock(Some(listener))

		//eval rules
		ruleEngine.execOn(memory)

		//return actual result of changeset
		BusEvent.dock(None)
	}
  }
}
