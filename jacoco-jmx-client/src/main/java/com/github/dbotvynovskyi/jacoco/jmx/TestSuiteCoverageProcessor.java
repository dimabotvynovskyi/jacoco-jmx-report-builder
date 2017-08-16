package com.github.dbotvynovskyi.jacoco.jmx;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.util.Collection;
import java.util.HashSet;
import java.util.Properties;
import java.util.Set;
import java.util.stream.Collectors;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

public class TestSuiteCoverageProcessor {

	private final JaCoCoMBeanClient jaCoCoMBeanClient;
	private Instant startTime;
	private String currentTestSuiteName;

	public TestSuiteCoverageProcessor() {
		jaCoCoMBeanClient = new JaCoCoMBeanClient();
	}

	public void beforeTestSuite(String testSuiteName) {
		this.currentTestSuiteName = testSuiteName;
		startTime = Instant.now();
		jaCoCoMBeanClient.resetCoverageStatistic();
	}

	public void afterTestSuite(String testSuiteName) throws IOException {
		if (!currentTestSuiteName.equals(testSuiteName)) {
			throw new IllegalStateException("Wrong test suite processing order, coverage data is corrupted");
		}

		Duration timeElapsed = Duration.between(startTime, Instant.now());
		System.out.println("elapsed time ( milliseconds ):..." + timeElapsed.toMillis());

		byte[] executionData = jaCoCoMBeanClient.getExecutionData();
		Collection<String> affectedClasses = getAffectedClasses(toExecutionDataStore(executionData));

		Properties props = new Properties();
		props.setProperty(testSuiteName, affectedClasses.stream().collect(Collectors.joining(",")));
		//TODO smarter file name

		new File("/tmp/jacoco/test/" + testSuiteName).createNewFile();

		props.store(new FileOutputStream("/tmp/jacoco/test/" + testSuiteName), null);

		appendFinalReport(executionData);
	}

	private void appendFinalReport(byte[] executionData) throws IOException {
		// TODO check if file exists or/not
		new File("/tmp/jacoco/test/final_jacoco_report.exec").createNewFile();

		try (FileOutputStream output = new FileOutputStream("/tmp/jacoco/test/final_jacoco_report.exec", true)) {
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

	private Collection<String> getAffectedClasses(ExecutionDataStore executionDataStore) {
		Set<String> names = new HashSet<>(executionDataStore.getContents().size());
		for (ExecutionData executionData : executionDataStore.getContents()) {
			names.add(executionData.getName());
		}
		return names;
	}

	public void destroy() {
		jaCoCoMBeanClient.close();
	}

}
