package massim.agent.student.puzzle;

import massim.agent.Action;

/**
 * Contains the constant of the puzzle (problem).
 */
public interface PuzzleConstants {

	/** Invalid queen position indicator, i.e. unassigned position. */
	public static final int INVALID_QUEEN_POSITION = -1;

	/** Possible actions of the agents. */
	public static final Action[] ACTIONS = {
			Action.EAST, Action.WEST, Action.SKIP
	};

}
