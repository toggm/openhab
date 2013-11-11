package org.openhab.core.scala

import hammurabi.WorkingMemory
import org.openhab.core.scala.model.Changeset
import hammurabi.RuleEngine
import org.openhab.core.items.Item
import org.openhab.core.types.State
import org.openhab.core.types.Command
import org.openhab.core.scala.model.RuleEngineListener

class RuleEngineExecutor(val ruleEngine: RuleEngine) {
  
	def execOn(memory: WorkingMemory, listener: RuleEngineListener) = {
    
	  //clear static objects map before executing ruleengine
	  Changeset.dock(Some(listener))
	  
	  RuleEngine()
	  
	  ruleEngine.execOn(memory)
	  
	  //return actual result of changeset
	  Changeset.dock(None)
	} 
}
