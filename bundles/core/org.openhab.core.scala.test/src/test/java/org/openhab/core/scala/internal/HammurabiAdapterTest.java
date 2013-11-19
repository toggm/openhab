package org.openhab.core.scala.internal;

import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import hammurabi.WorkingMemory;

import java.util.LinkedList;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mockito;
import org.openhab.core.library.items.NumberItem;
import org.openhab.core.library.items.StringItem;
import org.openhab.core.scala.RuleEngineExecutor;
import org.openhab.core.scala.model.RuleEngineListener;

@RunWith(JUnit4.class)
public class HammurabiAdapterTest {

	@Test
	public void testWorkingMemory() {
		NumberItem numberItem = new NumberItem("numberItem");
		StringItem stringItem = new StringItem("stringItem");
		ScalaCompiler compiler = mock(ScalaCompiler.class);
		ClassloaderUtil classloaderUtil = mock(ClassloaderUtil.class);
		RuleEngineListener listener = mock(RuleEngineListener.class);

		HammurabiAdapter adapter = new HammurabiAdapter("base", compiler,
				classloaderUtil, listener);
		adapter.itemAdded(numberItem);
		adapter.itemAdded(stringItem);

		WorkingMemory workingMemory = adapter.getWorkingMemory();

		assertTrue(workingMemory.contains(numberItem));
		assertTrue(workingMemory.contains(stringItem));

		// remove again
		adapter.itemRemoved(numberItem);
		adapter.itemRemoved(stringItem);

		assertFalse(workingMemory.contains(numberItem));
		assertFalse(workingMemory.contains(stringItem));

	}

	@Test
	public void shouldReevaluteRule() {
		RuleEngineExecutor engine = mock(RuleEngineExecutor.class);
		ScalaCompiler compiler = mock(ScalaCompiler.class);
		ClassloaderUtil classloaderUtil = mock(ClassloaderUtil.class);
		RuleEngineListener listener = mock(RuleEngineListener.class);

		LinkedList<RuleEngineExecutor> list = new LinkedList<RuleEngineExecutor>();
		list.add(engine);

		HammurabiAdapter adapter = new HammurabiAdapter("base", compiler,
				classloaderUtil, listener);
		adapter.setRuleEngines(list);

		// execute
		adapter.reevaluateRules();

		// check
		verify(engine, Mockito.times(1)).execOn(any(WorkingMemory.class),
				any(RuleEngineListener.class));
	}
}
