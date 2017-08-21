package com.github.dbotvynovskyi.jacoco.jmx;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Properties;
import java.util.stream.Collectors;

import org.jacoco.core.analysis.Analyzer;
import org.jacoco.core.analysis.CoverageBuilder;
import org.jacoco.core.analysis.IBundleCoverage;
import org.jacoco.core.analysis.IPackageCoverage;
import org.jacoco.core.analysis.ISourceFileCoverage;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

public class TestSuiteCoverageProcessor {

	private final TiaReportBuilderConfiguration configuration;
	private final JaCoCoMBeanClient jaCoCoMBeanClient;
	private Instant startTime;
	private String currentTestSuiteName;

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
		Duration timeElapsed = Duration.between(startTime, Instant.now());
		
		if (!currentTestSuiteName.equals(testSuiteName)) {
			throw new IllegalStateException("Wrong test suite processing order, coverage data is corrupted");
		}

		byte[] executionData = jaCoCoMBeanClient.getExecutionData();
		Collection<String> affectedClasses = getAffectedClasses(toExecutionDataStore(executionData));

		Properties props = new Properties();
		props.setProperty(testSuiteName, affectedClasses.stream().collect(Collectors.joining(",")));
		props.setProperty("time.elapsed.milliseconds", "" + timeElapsed.toMillis());

		File targetPath = new File(System.getProperty("user.dir"), "target");
		File reportFolder = new File(targetPath, configuration.getReportsFolderName());
		reportFolder.mkdir();

		File reportFile = new File(reportFolder, testSuiteName);
		reportFile.createNewFile();

		props.store(new FileOutputStream(reportFile), null);

		appendFinalReport(executionData);
	}

	private void appendFinalReport(byte[] executionData) throws IOException {
		// TODO check if file exists or/not
		File targetPath = new File(System.getProperty("user.dir"), "target");
		File finalReport = new File(targetPath, configuration.getFinalReportName());
		finalReport.createNewFile();

		try (FileOutputStream output = new FileOutputStream(finalReport, true)) {
			output.write(executionData);
		}
		// TODO process exception correctly
	}

	ExecutionDataStore toExecutionDataStore(byte[] executionData) throws IOException {
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
