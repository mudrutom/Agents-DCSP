package massim.agent.student;

import massim.agent.Position;
import massim.agent.student.puzzle.PuzzleConstants;

/**
 * Class encapsulating meta-data about the agent.
 */
public class AgentMetadata implements PuzzleConstants {

	private final String name;

	/** Agents' number. */
	public Integer queen;

	/** Agents' parent and child flags. */
	public Boolean isParent, isChild;

	/** Position of the agent. */
	public Position position;

	/** Constructor of the AgentMetadata class. */
	public AgentMetadata(String name) {
		this.name = name;
	}

	/** @return the name of the agent. */
	public String getName() {
		return name;
	}
}
