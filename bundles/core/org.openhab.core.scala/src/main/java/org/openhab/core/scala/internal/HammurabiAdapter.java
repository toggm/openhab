package org.openhab.core.scala.internal;

import hammurabi.Rule;
import hammurabi.RuleEngine;
import hammurabi.WorkingMemory;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import org.openhab.core.items.Item;
import org.openhab.core.scala.RuleEngineExecutor;
import org.openhab.core.scala.RuleSetFactory;
import org.openhab.core.scala.model.RuleEngineListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.collection.immutable.Set;

public class HammurabiAdapter implements RuleEngineAdapter {

	private WorkingMemory workingMemory;

	private List<RuleEngineExecutor> ruleEngines = new LinkedList<RuleEngineExecutor>();

	private static final Logger logger = LoggerFactory
			.getLogger(HammurabiAdapter.class);

	private String configurationBase;

	private ScalaCompiler compiler;

	private ClassloaderUtil classloaderUtil;

	private RuleEngineListener listener;

	public HammurabiAdapter(String configurationBase, ScalaCompiler compiler,
			ClassloaderUtil classloaderUtil, RuleEngineListener listener) {
		this.configurationBase = configurationBase;
		this.compiler = compiler;
		this.classloaderUtil = classloaderUtil;
		this.listener = listener;

		// initialize hammurabi
		workingMemory = new WorkingMemory();

	}

	/**
	 * @return
	 */
	public WorkingMemory getWorkingMemory() {
		return workingMemory;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openhab.core.scala.internal.RuleEngineAdapter#itemRemoved(org.openhab
	 * .core.items.Item)
	 */
	public void itemRemoved(Item item) {
		workingMemory.$minus(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openhab.core.scala.internal.RuleEngineAdapter#itemAdded(org.openhab
	 * .core.items.Item)
	 */
	public void itemAdded(Item item) {
		workingMemory.$plus(item);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.openhab.core.scala.internal.RuleEngineAdapter#reevaluateRules()
	 */
	public void reevaluateRules() {
		if (ruleEngines != null) {
			for (RuleEngineExecutor ruleEngine : ruleEngines) {
				applyOn(ruleEngine);
			}
		}
	}

	private void applyOn(RuleEngineExecutor ruleEngine) {

		// apply workingmemory and handle results
		ruleEngine.execOn(workingMemory, listener);
	}

	protected void setRuleEngines(List<RuleEngineExecutor> newRuleEngines) {
		// remove old ruleengines
		ruleEngines.clear();

		// add all new ruleenignes
		ruleEngines.addAll(newRuleEngines);

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.openhab.core.scala.internal.RuleEngineAdapter#sourceFileChanged()
	 */
	public void sourceFileChanged() {
		List<RuleEngineExecutor> newRuleEngines = recompileRules();

		// reinject new rules
		setRuleEngines(newRuleEngines);

		// reevaluate
		reevaluateRules();
	}

	/**
	 * @return
	 */
	protected List<RuleEngineExecutor> recompileRules() {
		List<RuleEngineExecutor> ruleEngines = new ArrayList<RuleEngineExecutor>();
		try {
			URL libDirUrl = getClass().getResource(configurationBase + "/lib");
			File libDir = new File(libDirUrl.toURI());
			File dir = new File(configurationBase);
			File[] libs = libDir.listFiles();

			// handle only scalaFiles
			File[] scalaFiles = dir.listFiles(new FilenameFilter() {

				public boolean accept(File dir, String name) {
					return name.endsWith(".scala");
				}
			});

			File outputPath = FileUtil.createTempDir("openhab", "scala");

			// recompile all files
			boolean success = compiler.compileScalaFiles(scalaFiles, libs,
					outputPath);
			if (!success) {
				// stop
				logger.warn("Compiling scala files wasn't successfully, compilation failed");
				return ruleEngines;
			}
			File[] compileScalaFiles = outputPath.listFiles();
			String[] filenames = new String[compileScalaFiles.length];
			for (int i = 0; i < compileScalaFiles.length; ++i) {
				filenames[i] = compileScalaFiles[i].getName();
			}

			Class<? extends RuleSetFactory>[] foundRuleSetFactories = classloaderUtil
					.loadImplementationsFromFiles(RuleSetFactory.class,
							outputPath.getAbsolutePath(), filenames, libDirUrl);
			if (foundRuleSetFactories != null) {
				for (Class<? extends RuleSetFactory> factoryClass : foundRuleSetFactories) {
					try {
						RuleSetFactory factory = factoryClass.newInstance();
						if (factory != null) {
							// replace old uleengineexecutor
							Set<Rule> rules = factory.generateRuleSet();
							if (rules != null) {
								RuleEngineExecutor ruleEngine = new RuleEngineExecutor(
										new RuleEngine(rules));

								// reevaluate ruleengine
								ruleEngines.add(ruleEngine);
							}
						}
					} catch (InstantiationException e) {
						logger.debug("Coudln't instanciate RuleSetFactory", e);
					} catch (IllegalAccessException e) {
						logger.debug("Coudln't instanciate RuleSetFactory", e);
					}
				}
			}
		} catch (IOException e) {
			logger.warn("Coudln't recompile scala files", e);
		} catch (URISyntaxException e) {
			logger.warn("Coudln't recompile scala files", e);
		}

		return ruleEngines;
	}
}
