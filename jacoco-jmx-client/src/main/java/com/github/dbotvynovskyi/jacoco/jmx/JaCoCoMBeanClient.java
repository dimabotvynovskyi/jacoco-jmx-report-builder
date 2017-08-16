package com.github.dbotvynovskyi.jacoco.jmx;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import javax.management.MBeanServerConnection;
import javax.management.MBeanServerInvocationHandler;
import javax.management.ObjectName;
import javax.management.remote.JMXConnector;
import javax.management.remote.JMXConnectorFactory;
import javax.management.remote.JMXServiceURL;

import org.jacoco.core.data.ExecutionData;
import org.jacoco.core.data.ExecutionDataReader;
import org.jacoco.core.data.ExecutionDataStore;
import org.jacoco.core.data.SessionInfoStore;

public class JaCoCoMBeanClient {

	//TODO configurable url
	private static final String SERVICE_URL = "service:jmx:rmi:///jndi/rmi://sqbo-ops-soa.docker:9999/jmxrmi";

	private final JMXConnector jmxc;
	private final IAgent agentProxy;

	JaCoCoMBeanClient() {
		try {
			// Open connection to the coverage agent:
			final JMXServiceURL url = new JMXServiceURL(SERVICE_URL);
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

	public static void main(final String[] args) throws Exception {
		// create client
		JaCoCoMBeanClient jacocoMBeanClient = new JaCoCoMBeanClient();

		// print report to console
		jacocoMBeanClient.printReport();

		// close
		jacocoMBeanClient.close();
	}

	public void printReport() throws IOException {
		ExecutionDataStore executionDataStore = getExecutionDataStore();

		for (ExecutionData executionData : executionDataStore.getContents()) {
			System.out.printf("%016x  %3d of %3d   %s%n",
					executionData.getId(),
					getHitCount(executionData.getProbes()),
					executionData.getProbes().length,
					executionData.getName());
		}
	}

	private int getHitCount(final boolean[] data) {
		int count = 0;
		for (final boolean hit : data) {
			if (hit) {
				count++;
			}
		}
		return count;
	}

}
