package com.github.dbotvynovskyi.jacoco.jmx.entities;

import java.util.List;

public class TestSuitePerClassCoverageDetails {

	private String testSuiteName;
	private long testSuiteDuration; //millis
	private List<LinesRange> ranges;

	public String getTestSuiteName() {
		return testSuiteName;
	}

	public void setTestSuiteName(String testSuiteName) {
		this.testSuiteName = testSuiteName;
	}

	public long getTestSuiteDuration() {
		return testSuiteDuration;
	}

	public void setTestSuiteDuration(long testSuiteDuration) {
		this.testSuiteDuration = testSuiteDuration;
	}

	public List<LinesRange> getRanges() {
		return ranges;
	}

	public void setRanges(List<LinesRange> ranges) {
		this.ranges = ranges;
	}
}
