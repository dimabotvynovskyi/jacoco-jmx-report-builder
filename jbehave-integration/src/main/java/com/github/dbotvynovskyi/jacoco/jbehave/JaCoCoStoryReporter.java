package com.github.dbotvynovskyi.jacoco.jbehave;

import java.util.List;
import java.util.Map;

import org.jbehave.core.model.ExamplesTable;
import org.jbehave.core.model.GivenStories;
import org.jbehave.core.model.Meta;
import org.jbehave.core.model.Narrative;
import org.jbehave.core.model.OutcomesTable;
import org.jbehave.core.model.Scenario;
import org.jbehave.core.model.Story;
import org.jbehave.core.model.StoryDuration;
import org.jbehave.core.reporters.StoryReporter;

import com.github.dbotvynovskyi.jacoco.jmx.TestSuiteCoverageProcessor;

public class JaCoCoStoryReporter implements StoryReporter {

	private final TestSuiteCoverageProcessor testSuiteCoverageProcessor = new TestSuiteCoverageProcessor();
	private final ThreadLocal<String> scenarioTitle = new ThreadLocal<String>();

	public void beforeScenario(String scenarioTitle) {
		this.scenarioTitle.set(scenarioTitle);
		testSuiteCoverageProcessor.beforeTestSuite(scenarioTitle);
	}

	public void afterScenario() {
		testSuiteCoverageProcessor.beforeTestSuite(this.scenarioTitle.get());
	}

	public void storyNotAllowed(Story story, String s) {

	}

	public void storyCancelled(Story story, StoryDuration storyDuration) {

	}

	public void beforeStory(Story story, boolean b) {
	}

	public void afterStory(boolean b) {

	}

	public void narrative(Narrative narrative) {

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
