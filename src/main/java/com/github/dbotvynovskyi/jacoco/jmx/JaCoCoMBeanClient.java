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

	private static final String SERVICE_URL = "service:jmx:rmi:///jndi/rmi://some-service.docker:9999/jmxrmi";

	private final JMXConnector jmxc;
	private final IAgent agentProxy;

	public JaCoCoMBeanClient() throws Exception {
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

	byte[] getExecutionData() {
		agentProxy.dump(false);
		return agentProxy.getExecutionData(false);
	}

	void reset() {
		agentProxy.reset();
	}

	public void close() throws IOException {
		// Close connection:
		jmxc.close();
	}

	public static void main(final String[] args) throws Exception {
		// create client
		JaCoCoMBeanClient jacocoMBeanClient = new JaCoCoMBeanClient();

		byte[] executionData = jacocoMBeanClient.getExecutionData();
		// print report to console
		jacocoMBeanClient.printReport(executionData);

		// close
		jacocoMBeanClient.close();
	}

	public void printReport(byte[] data) throws IOException {
		SessionInfoStore sessionInfoStore = new SessionInfoStore();
		ExecutionDataStore executionDataStore = new ExecutionDataStore();

		final ExecutionDataReader reader = new ExecutionDataReader(new ByteArrayInputStream(data));
		reader.setSessionInfoVisitor(sessionInfoStore);
		reader.setExecutionDataVisitor(executionDataStore);
		reader.read();

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
