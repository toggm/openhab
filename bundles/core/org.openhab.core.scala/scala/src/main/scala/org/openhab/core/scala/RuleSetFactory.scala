package org.openhab.core.scala

import hammurabi.Rule

trait RuleSetFactory {
	def generateRuleSet: Set[Rule] = Set()
}