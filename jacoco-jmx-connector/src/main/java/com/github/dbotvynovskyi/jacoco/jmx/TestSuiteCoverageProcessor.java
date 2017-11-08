package com.github.dbotvynovskyi.jacoco.jmx;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.ho.yaml.Yaml;
import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

import com.github.dbotvynovskyi.jacoco.jmx.entities.LinesRange;
import com.github.dbotvynovskyi.jacoco.jmx.entities.TestSuitePerClassCoverageDetails;
import com.github.dbotvynovskyi.jacoco.jmx.entities.YamlReportEntry;

// TODO is not a threadsafe for now, just a POC
public class TestSuiteCoverageProcessor {

	private final TiaReportBuilderConfiguration configuration;
	private final JaCoCoMBeanClient jaCoCoMBeanClient;
	private Instant startTime;
	private String currentTestSuiteName;
	private final YamlReportEntry yamlReportEntry = new YamlReportEntry();

	public TestSuiteCoverageProcessor(final TiaReportBuilderConfiguration configuration) {
		this.configuration = configuration;
		jaCoCoMBeanClient = new JaCoCoMBeanClient(configuration);
	}

	public void beforeTestSuite(String testSuiteName) {
		this.currentTestSuiteName = testSuiteName;
		startTime = Instant.now();
		jaCoCoMBeanClient.resetCoverageStatistic();
	}

	public void afterTestSuite(String testSuiteName) throws IOException {
		Duration timeElapsed = Duration.between(startTime, Instant.now()); //TODO gather duration statistic

		if (!currentTestSuiteName.equals(testSuiteName)) {
			throw new IllegalStateException("Wrong test suite processing order, coverage data is corrupted");
		}

		byte[] executionData = jaCoCoMBeanClient.getExecutionData();
		Collection<String> affectedClasses = getAffectedClasses(toExecutionDataStore(executionData));
		dumpCoverageReport(testSuiteName, affectedClasses, timeElapsed);
	}

	private void dumpCoverageReport(String testSuiteName, Collection<String> affectedClasses, Duration timeElapsed) throws IOException {
		for (String affectedClass : affectedClasses) {
			yamlReportEntry.getClassCoverageDetailsMap().computeIfAbsent(affectedClass, k -> new LinkedList<>());
			List<TestSuitePerClassCoverageDetails> list = yamlReportEntry.getClassCoverageDetailsMap().get(affectedClass);
			TestSuitePerClassCoverageDetails details = new TestSuitePerClassCoverageDetails();
			details.setTestSuiteName(testSuiteName);
			details.setTestSuiteDuration(timeElapsed.toMillis());
			details.setRanges(Collections.singletonList(new LinesRange(-1, -1))); //TODO gather ranges

			list.add(details);
		}

		File reportFile = ensureReportFileExists(
				System.getProperty("user.dir"),
				"target",
				configuration.getReportsFolderName(),
				configuration.getReportName()
		);

		System.out.println("Jacoco TIA report for test suite[" + testSuiteName + "] stored to: " + reportFile.toString());

		Yaml.dump(yamlReportEntry, reportFile);
	}

	private File ensureReportFileExists(String rootPath, String... subPaths) throws IOException {
		File file = new File(rootPath);

		for (int i = 0; i < subPaths.length; i++) {
			file = new File(file, subPaths[i]);

			if (!file.exists()) {
				if (i + 1 < subPaths.length) {
					file.mkdir();
				} else {
					file.createNewFile();
				}
			}
		}

		return file;
	}

	private ExecutionDataStore toExecutionDataStore(byte[] executionData) throws IOException {
		SessionInfoStore sessionInfoStore = new SessionInfoStore();
		ExecutionDataStore executionDataStore = new ExecutionDataStore();

		final ExecutionDataReader reader = new ExecutionDataReader(new ByteArrayInputStream(executionData));
		reader.setSessionInfoVisitor(sessionInfoStore);
		reader.setExecutionDataVisitor(executionDataStore);
		reader.read();

		return executionDataStore;
	}

	private Collection<String> getAffectedClasses(ExecutionDataStore executionDataStore) throws IOException {
		final CoverageBuilder coverageBuilder = new CoverageBuilder();
		final Analyzer analyzer = new Analyzer(executionDataStore, coverageBuilder);
		analyzer.analyzeAll(new File(configuration.getClassesDirectory()));
		IBundleCoverage bundle = coverageBuilder.getBundle("tia-jacoco-report");

		List<String> affectedSourceFiles = new LinkedList<>();
		for (IPackageCoverage iPackageCoverage : bundle.getPackages()) {
			for (ISourceFileCoverage iSourceFileCoverage : iPackageCoverage.getSourceFiles()) {
				affectedSourceFiles.add(iSourceFileCoverage.getPackageName() + "/" + iSourceFileCoverage.getName());
			}
		}

		return affectedSourceFiles;
	}

	//TODO find a way to properly close connection
	public void destroy() {
		jaCoCoMBeanClient.close();
	}

}
