package massim.agent.student;

import massim.agent.MASQueenAgent;
import massim.agent.student.puzzle.PuzzleConstants;

import java.io.Serializable;

/**
 * A token used in termination detection of ABT.
 */
public class TerminationToken implements PuzzleConstants, Serializable {

	private static final long serialVersionUID = -6283447408771143904L;

	/** Initiator agent of this token. */
	private final String initiator;
	/** Agent sequence of this token. */
	private final String[] agentSequence;

	/** The message counter of this token. */
	private int messageCounter;

	/** Constructor of the TerminationToken class. */
	public TerminationToken(MASQueenAgent initiator, String[] agentSequence) {
		this.initiator = initiator.getUsername();
		this.agentSequence = agentSequence;
		messageCounter = 0;
	}

	/** Increments the message counter by given value. */
	public void incrementCounter(int value) {
		messageCounter += value;
	}

	/** @return value of the message counter */
	public int getMessageCounter() {
		return messageCounter;
	}

	/** @return <tt>true</tt> IFF given agent is initiator of this token */
	public boolean isInitiator(MASQueenAgent agent) {
		return initiator.equals(agent.getUsername());
	}

	/** @return the next agent to send this token to (from agent sequence) */
	public String getNextAgent(MASQueenAgent agent) {
		final String name = agent.getUsername();
		int index = -1;
		for (int i = agentSequence.length - 1; i >= 0; i--) {
			if (name.equals(agentSequence[i])) {
				index = i - 1;
				break;
			}
		}
		return (index == -1) ? initiator : agentSequence[index];
	}

	@Override
	public String toString() {
		return String.format("Token(%d)", messageCounter);
	}
}
