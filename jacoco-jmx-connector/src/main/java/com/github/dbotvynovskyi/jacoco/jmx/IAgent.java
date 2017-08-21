package com.github.dbotvynovskyi.jacoco.jmx;

public interface IAgent {

	String getVersion();

	String getSessionId();

	void setSessionId(String id);

	byte[] getExecutionData(boolean reset);

	void dump(boolean reset);

	void reset();
	
}
