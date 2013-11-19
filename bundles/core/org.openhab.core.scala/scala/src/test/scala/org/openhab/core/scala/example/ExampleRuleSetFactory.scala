package org.openhab.core.scala.example

import org.openhab.core.scala.RuleSetFactory
import hammurabi.Rule
import hammurabi.Rule._
import org.openhab.core.items.Item
import org.openhab.core.scala.model.BusEvent._
import org.openhab.core.types.Command
import org.openhab.core.library.types.OnOffType
import org.openhab.core.library.items.NumberItem
import org.openhab.core.library.types.DecimalType
import org.openhab.core.library.items.SwitchItem

object ExampleRuleSetFactoryImpl extends RuleSetFactory {
  override def generateRuleSet(): Set[Rule] = {
    Set(
      rule("Rule1") let {
        val i = kindOf[NumberItem] having (_.getName() == "item1")
        when {
          i.getState() == new DecimalType(1)
        } then {
          send(OnOffType.ON) to i
        }
      },
      rule("Rule2") let {
        val i = kindOf[NumberItem] having (_.getName() == "item1")
        when {
          i.getState() == new DecimalType(2)
        } then {
          updated(i) to (new DecimalType(4))
        }
      },
      rule("Rule3") let {
        val i = kindOf[NumberItem] having (_.getName() == "item1")
        when {
          i.getState() == new DecimalType(3)
        } then {
          //access items library
          val switch = kindOf[SwitchItem] having (_.getName() == "item2")
          send(OnOffType.ON) to switch
        }
      })
  }
}