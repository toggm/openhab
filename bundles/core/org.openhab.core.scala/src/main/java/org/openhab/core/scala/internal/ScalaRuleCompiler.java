package org.openhab.core.scala.internal;

import hammurabi.Rule;
import hammurabi.RuleEngine;

import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.openhab.core.scala.RuleEngineExecutor;
import org.openhab.core.scala.RuleSetFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import scala.collection.immutable.Set;

public class ScalaRuleCompiler {
	private String configurationBase;
	private static final Logger logger = LoggerFactory
			.getLogger(ScalaRuleCompiler.class);

	public ScalaRuleCompiler(String configurationBase) {
		this.configurationBase = configurationBase;
	}

	public List<RuleEngineExecutor> recompileRules() {
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
			boolean success = ScalaUtil.compileScalaFiles(scalaFiles, libs,
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

			Class<? extends RuleSetFactory>[] foundRuleSetFactories = ClassloaderUtil
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
