package org.openhab.core.scala.model

import org.openhab.core.types.Command
import org.openhab.core.items.Item
import org.openhab.core.types.State

sealed trait RuleEvent {
}

sealed trait ItemEvent extends RuleEvent{
  def item:Item
}

case class CommandEvent(item:Item, cmd: Command) extends ItemEvent {
 
}

case class StateEvent(item:Item, oldState:State, newState:State) extends ItemEvent {
  
}

object SystemEventType extends Enumeration {
  type SystemEventType = Value
  val Startup, Shutdown = Value
}

case class SystemEvent(val eventType:SystemEventType.Value) extends RuleEvent {
}