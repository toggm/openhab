package org.openhab.core.scala.model

import org.openhab.core.items.Item
import org.openhab.core.types.State
import org.openhab.core.types.Command
import scala.collection.mutable.MutableList
import scala.collection.mutable.ConcurrentMap

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
}

trait RuleEngineListener {
  def send(item: Item, cmd: Command)

  def updated(item: Item, state: State)
}
