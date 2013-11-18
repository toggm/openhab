package org.openhab.core.scala.internal.model;

public class SimpleClassWithoutDependencies implements Model {

	@Override
	public void test() {
		System.out.println("test");
	}
}