package com.github.dbotvynovskyi.jacoco.jmx;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

public class JaCoCoMBeanClient {

	private final JMXConnector jmxc;
	private final IAgent agentProxy;

	JaCoCoMBeanClient(final TiaReportBuilderConfiguration configuration) {
		try {
			// Open connection to the coverage agent:
			final JMXServiceURL url = new JMXServiceURL(configuration.getJacocoAgentJmxUrl());
			jmxc = JMXConnectorFactory.connect(url, null);
			final MBeanServerConnection connection = jmxc.getMBeanServerConnection();

			agentProxy = MBeanServerInvocationHandler.newProxyInstance(
					connection,
					new ObjectName("org.jacoco:type=Runtime"),
					IAgent.class,
					false
			);
		}
		catch (Exception e) {
			throw new IllegalArgumentException("Unable to obtain connection to MBeanServer", e);
		}
	}

	byte[] getExecutionData() {
		agentProxy.dump(false);
		return agentProxy.getExecutionData(false);
	}

	ExecutionDataStore getExecutionDataStore() throws IOException {
		byte[] executionData = getExecutionData();
		SessionInfoStore sessionInfoStore = new SessionInfoStore();
		ExecutionDataStore executionDataStore = new ExecutionDataStore();

		final ExecutionDataReader reader = new ExecutionDataReader(new ByteArrayInputStream(executionData));
		reader.setSessionInfoVisitor(sessionInfoStore);
		reader.setExecutionDataVisitor(executionDataStore);
		reader.read();

		return executionDataStore;
	}

	void resetCoverageStatistic() {
		agentProxy.reset();
	}

	void close() {
		try {
			jmxc.close();
		}
		catch (IOException e) {
			// ignored
		}
	}

}
