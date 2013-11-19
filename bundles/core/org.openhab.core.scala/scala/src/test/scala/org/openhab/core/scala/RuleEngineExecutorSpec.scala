package org.openhab.core.scala

import org.specs2.mutable._
import org.specs2.runner._
import org.openhab.core.scala.example.ExampleRuleSetFactoryImpl
import hammurabi.RuleEngine
import hammurabi.WorkingMemory
import org.openhab.core.library.items.NumberItem
import org.openhab.core.library.items.SwitchItem
import org.openhab.core.scala.model.RuleEngineListener
import org.specs2.mock.Mockito
import org.mockito.Matchers._
import org.openhab.core.library.types.OnOffType
import org.openhab.core.library.types.DecimalType

object RuleEngineExecutorSpec extends Specification with Mockito {
  "RuleEngineExecutor" should {
    val rules = ExampleRuleSetFactoryImpl.generateRuleSet
    val ruleEngine = RuleEngine(rules)

    val item1 = new NumberItem("item1")
    val item2 = new SwitchItem("item2")
    val wm = WorkingMemory(item1, item2)

    val listener = mock[RuleEngineListener]
    val executor = new RuleEngineExecutor(ruleEngine)

    "Execute Rule1" in {

      //execute rule 1ls
      item1.setState(new DecimalType(1))
      executor.execOn(wm, listener)

      there was one(listener).send(item1, OnOffType.ON)

    }
    "Execute Rule2" in {

      //execute rule2
      item1.setState(new DecimalType(2))
      executor.execOn(wm, listener)

      there was one(listener).updated(item1, new DecimalType(4))

    }
    "Execute Rule3" in {

      //execute rule3
      item1.setState(new DecimalType(3))
      executor.execOn(wm, listener)

      there was one(listener).send(item2, OnOffType.ON)
    }
  }
}