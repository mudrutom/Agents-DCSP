package massim.agent;

import cz.agents.alite.communication.DefaultCommunicator;
import cz.agents.alite.communication.channel.CommunicationChannelException;
import cz.agents.alite.communication.channel.DirectCommunicationChannel;
import cz.agents.alite.communication.channel.DirectCommunicationChannel.ReceiverTable;
import massim.agent.student.MyQueenAgent;

import java.util.LinkedList;

public class StartAgents {

	final static int N_AGENTS = 4;

	public static void main(String[] args) {
		startAgents("localhost", 12300, N_AGENTS);
	}

	public static void startAgents(String host, int port, int nAgents) {
		ReceiverTable receiverTable = new DirectCommunicationChannel.DefaultReceiverTable();

		LinkedList<String> agentNames = new LinkedList<String>();

		for (int i = 1; i <= nAgents; i++) {
			agentNames.add(idToAgentName(i));
		}

		for (int i = 1; i <= nAgents; i++) {
			String agentName = idToAgentName(i);
			System.out.println("Adding agent " + agentName);

			final MASQueenAgent agent = new MyQueenAgent(host, port, agentName, "1", nAgents);

			DefaultCommunicator communicator = new DefaultCommunicator(agentName);
			try {
				communicator.addChannel(new DirectCommunicationChannel(communicator, receiverTable));
			} catch (CommunicationChannelException e) {
				e.printStackTrace();
			}
			communicator.addMessageHandler(agent);

			// setup communication infrastructure
			agent.setCommunicator(communicator, agentNames);

			// start in a new thread
			new Thread(new Runnable() {
				@Override
				public void run() {
					agent.start();
				}
			}).run();

			Thread.yield();
		}
	}

	private static String idToAgentName(int i) {
		return (i % 2 == 1) ? "a" + (i / 2 + 1) : "b" + (i / 2);
	}
}

