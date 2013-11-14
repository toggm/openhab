package org.openhab.core.scala.internal.model;

import hammurabi.WorkingMemory;

public class ClassWithDependencies implements Model {

	private WorkingMemory workingMemory;

	public ClassWithDependencies() {
		// intanciate workingmemory in constructor to proof resolving
		// runtime dependencies during instanciation
		workingMemory = new WorkingMemory();
	}

	public WorkingMemory getWorkingMemory() {
		return workingMemory;
	}
}