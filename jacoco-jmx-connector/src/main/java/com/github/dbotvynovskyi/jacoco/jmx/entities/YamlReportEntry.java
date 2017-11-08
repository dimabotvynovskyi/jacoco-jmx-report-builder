package com.github.dbotvynovskyi.jacoco.jmx.entities;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class YamlReportEntry {

	private Map<String, List<TestSuitePerClassCoverageDetails>> classCoverageDetailsMap = new HashMap<>();

	public Map<String, List<TestSuitePerClassCoverageDetails>> getClassCoverageDetailsMap() {
		return classCoverageDetailsMap;
	}

	public void setClassCoverageDetailsMap(
			Map<String, List<TestSuitePerClassCoverageDetails>> classCoverageDetailsMap) {
		this.classCoverageDetailsMap = classCoverageDetailsMap;
	}

}
