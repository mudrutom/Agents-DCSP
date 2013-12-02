package massim.agent.student;

import cz.agents.alite.communication.Message;
import massim.agent.Action;
import massim.agent.MASPerception;
import massim.agent.MASQueenAgent;

import java.util.List;

/** This example agent illustrates the usage API available in the MASQueenAgent class. */
public class MyQueenAgent extends MASQueenAgent {

	// The total number of agents in the system
	int nAgents;

	public MyQueenAgent(String host, int port, String username, String password, int nAgents) {
		super(host, port, username, password);
		this.nAgents = nAgents;
	}

	@Override
	protected Action deliberate(MASPerception percept) {

		// This is how you get the index of the row at which the agent currently stays
		percept.getPosY();

		// This is how you process the incoming messages
		List<Message> newMessages = getNewMessages();

		for (Message message : newMessages) {
			System.out.println("Received a message from " + message.getSender() + " with the content " + message.getContent().toString());
		}

		// This is how you send a message
		// sendMessage("a1", new StringContent("Hello world"));

		// This is how you broadcast a message
		// broadcast(new StringContent("Hello world"));

		// This is how you notify us that the agents are finished with computing and moved to to their final position
		// (every agent should call this method after it is standing at its final position)
		notifyFinished(true);

		// In the case there is no valid solution, at least one agent should call the following method
		notifyFinished(false);

		// You can slow down the deliberation like this.

		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
		}

		// At the end of each deliberation step, you should return one of the following actions:
		Action[] availableActions = { Action.SKIP, Action.WEST, Action.EAST, Action.NORTH, Action.SOUTH };
		return availableActions[(int) (Math.random() * (availableActions.length))];
	}
}
