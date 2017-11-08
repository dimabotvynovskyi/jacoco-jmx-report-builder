package com.github.dbotvynovskyi.jacoco.jbehave;

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Lifecycle;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.reporters.StoryReporter;

import com.github.dbotvynovskyi.jacoco.jmx.TestSuiteCoverageProcessor;
import com.github.dbotvynovskyi.jacoco.jmx.TiaReportBuilderConfiguration;

public class JaCoCoStoryReporter implements StoryReporter {

	private final TestSuiteCoverageProcessor testSuiteCoverageProcessor;
	private final ThreadLocal<String> scenarioTitle = new ThreadLocal<String>();

	public JaCoCoStoryReporter(TiaReportBuilderConfiguration configuration) {
		testSuiteCoverageProcessor = new TestSuiteCoverageProcessor(configuration);
	}

	public void beforeScenario(String scenarioTitle) {
		final String clearScenarioTitle = scenarioTitle.replaceAll(" ", "_");

		this.scenarioTitle.set(clearScenarioTitle);
		testSuiteCoverageProcessor.beforeTestSuite(clearScenarioTitle);
	}

	public void afterScenario() {
		try {
			testSuiteCoverageProcessor.afterTestSuite(this.scenarioTitle.get());
		}
		catch (Exception e) {
			System.out.println("Failed to build report for test suite + " + this.scenarioTitle.get() + ", reason: " + e.toString());
		}
	}

	public void storyNotAllowed(Story story, String s) {

	}

	public void storyCancelled(Story story, StoryDuration storyDuration) {

	}

	public void beforeStory(Story story, boolean b) {
		// TODO probably measure story duration
	}

	public void afterStory(boolean b) {
		// TODO
	}

	public void narrative(Narrative narrative) {

	}

	public void lifecyle(Lifecycle lifecycle) {

	}

	public void scenarioNotAllowed(Scenario scenario, String s) {

	}

	public void scenarioMeta(Meta meta) {
	}

	public void givenStories(GivenStories givenStories) {

	}

	public void givenStories(List<String> list) {

	}

	public void beforeExamples(List<String> list, ExamplesTable examplesTable) {

	}

	public void example(Map<String, String> map) {

	}

	public void afterExamples() {

	}

	public void beforeStep(String s) {

	}

	public void successful(String s) {
	}

	public void ignorable(String s) {

	}

	public void pending(String s) {

	}

	public void notPerformed(String s) {

	}

	public void failed(String s, Throwable throwable) {

	}

	public void failedOutcomes(String s, OutcomesTable outcomesTable) {

	}

	public void restarted(String s, Throwable throwable) {

	}

	public void dryRun() {

	}

	public void pendingMethods(List<String> list) {

	}
}
