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
    withListener(listener) {
      ruleEngine.execOn(memory)
    }

<<<<<<< HEAD
	BusEvent.synchronized {
		//clear static objects map before executing ruleengine
		BusEvent.dock(Some(listener))

		//eval rules
		ruleEngine.execOn(memory)

		//return actual result of changeset
		BusEvent.dock(None)
	}
=======
    //clear static objects map before executing ruleengine
    //    BusEvent.dock(Some(listener))

    //eval rules
    //    ruleEngine.execOn(memory)

    //return actual result of changeset
    //    BusEvent.dock(None)
  }

  def withListener(listener: RuleEngineListener)(block: => Unit) {
    try {
      BusEvent.synchronized {
        BusEvent.dock(Some(listener))

        block
      }
    } finally {
      BusEvent.dock(None)
    }
>>>>>>> b4ec47acf7ace5b92bb7f369595b9efe9fbeb6c1
  }
}
