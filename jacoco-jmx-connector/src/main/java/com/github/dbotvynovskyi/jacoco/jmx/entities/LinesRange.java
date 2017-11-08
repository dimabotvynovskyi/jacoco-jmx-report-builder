package com.github.dbotvynovskyi.jacoco.jmx.entities;

public class LinesRange {

	private int min;
	private int max;

	public LinesRange() {
	}

	public LinesRange(int min, int max) {
		this.min = min;
		this.max = max;
	}

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min;
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max;
	}
}
