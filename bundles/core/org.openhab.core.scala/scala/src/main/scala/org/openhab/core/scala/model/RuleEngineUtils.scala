package org.openhab.core.scala.model

import org.openhab.core.items.Item
import org.openhab.core.types.State
import org.openhab.core.types.Command
import scala.collection.mutable.MutableList
import scala.collection.mutable.ConcurrentMap
import org.joda.time.DateTime
import org.openhab.model.script.actions.Timer
import org.openhab.model.script.actions.ScriptExecution._
import org.eclipse.xtext.xbase.lib.Procedures.Procedure0;
import org.openhab.model.script.actions.ScriptExecution

object BusEvent {

  var ruleEngineListener: Option[RuleEngineListener] = None

  def updated(item: Item) = new {
    def to(state: State) = {
      ruleEngineListener.map(_.updated(item, state))
    }
  }

  def send(cmd: Command) = new {
    def to(item: Item) = new {
      ruleEngineListener.map(_.send(item, cmd))
    }
  }

  def dock(listener: Option[RuleEngineListener]) = {
    ruleEngineListener = listener
  }
  
  def createTimer(time:DateTime)(block:  => Any):Timer = {
	  ScriptExecution.createTimer(time, new Procedure0() {
	    override def apply() = {
	      block
	    }
	  })
	}
}

trait RuleEngineListener {
  def send(item: Item, cmd: Command)

  def updated(item: Item, state: State)
}
