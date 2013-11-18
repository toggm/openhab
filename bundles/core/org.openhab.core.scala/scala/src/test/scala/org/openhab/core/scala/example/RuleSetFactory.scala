package org.openhab.core.scala.example

import org.openhab.core.scala.RuleSetFactory
import hammurabi.Rule
import hammurabi.Rule._
import org.openhab.core.items.Item
import org.openhab.core.scala.model.BusEvent._
import org.openhab.core.types.Command
import org.openhab.core.library.types.OnOffType
import org.openhab.core.library.items.NumberItem

class ExampleRuleSetFactoryImpl extends RuleSetFactory {
	override def generateRuleSet(): Set[Rule] = {
	   Set(
		  rule ("Rule1") let {
		    val i = kindOf[NumberItem] having (_.getName() == "item1")
		    when {
		    	i.getState() == 1
		    } then {
		      send (OnOffType.ON) to i		      
		    }
		  },
		  rule ("Rule2") let {
		    val i = kindOf[NumberItem] having (_.getName() == "item1")
		    when {
		    	i.getState() == 2
		    } then {
		      updated(i).set(new DecimalType(3))		      
		    }
		  })
	}
}