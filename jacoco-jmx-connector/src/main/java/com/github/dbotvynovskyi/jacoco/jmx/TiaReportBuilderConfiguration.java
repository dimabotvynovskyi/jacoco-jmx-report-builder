package com.github.dbotvynovskyi.jacoco.jmx;

public class TiaReportBuilderConfiguration {

	/**
	 * JMX service URL, for example "service:jmx:rmi:///jndi/rmi://some-service.docker:9999/jmxrmi"
	 */
	private final String jacocoAgentJmxUrl;
	private final String classesDirectory;
	
	private final String finalReportName = "final_jacoco_report.exec";
	private final String reportsFolderName = "jacoco-tia-report";

	public TiaReportBuilderConfiguration(String classesPath, String jacocoAgentJmxUrl) {
		this.classesDirectory = classesPath;
		this.jacocoAgentJmxUrl = jacocoAgentJmxUrl;
	}

	public String getClassesDirectory() {
		return classesDirectory;
	}

	public String getJacocoAgentJmxUrl() {
		return jacocoAgentJmxUrl;
	}

	public String getFinalReportName() {
		return finalReportName;
	}

	public String getReportsFolderName() {
		return reportsFolderName;
	}
}
