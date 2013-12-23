package massim.agent.student;

import massim.agent.student.puzzle.ChessBoard;
import massim.agent.student.puzzle.PuzzleConstants;
import massim.agent.student.puzzle.Queen;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A class encapsulating a no-good.
 */
public class NoGood implements PuzzleConstants, Serializable {

	private static final long serialVersionUID = 1219419392599087008L;

	/**
	 * The no-good, a map of constraint violations. The map keys
	 * correspond to a queen number and values to its position.
	 */
	private final Map<Integer, Integer> noGood;

	/** Constructor of the NoGood class. */
	public NoGood() {
		noGood = new LinkedHashMap<Integer, Integer>();
	}

	/** @return <tt>true</tt> IFF this no-good is empty */
	public boolean isEmpty() {
		return noGood.isEmpty();
	}

	/** @return <tt>true</tt> IFF given queen is violating any constraint */
	public boolean hasViolation(int queen) {
		return noGood.containsKey(queen);
	}

	/** @return a value causing constraint violation for given queen */
	public int getViolation(int queen) {
		return noGood.containsKey(queen) ? noGood.get(queen) : INVALID_QUEEN_POSITION;
	}

	/** Sets a constraint violation for given queen. */
	public void setViolation(int queen, int position) {
		noGood.put(queen, position);
	}

	/** @return <tt>true</tt> IFF verifies given context of this no-good */
	public boolean verifyContext(ChessBoard chessBoard) {
		for (Map.Entry<Integer, Integer> entry : noGood.entrySet()) {
			if (chessBoard.getPosition(entry.getKey()) != entry.getValue()) {
				return false;
			}
		}
		return true;
	}

	/** Creates and returns new no-good from received no-good. */
	public static NoGood createNoGood(Queen queen, NoGood receivedNoGood) {
		final int queenNumber = queen.getNumber();
		final NoGood noGood = new NoGood();
		for (Map.Entry<Integer, Integer> entry : receivedNoGood.noGood.entrySet()) {
			if (entry.getKey() != queenNumber) {
				noGood.setViolation(entry.getKey(), entry.getValue());
			}
		}
		return noGood;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (Map.Entry<Integer, Integer> entry : noGood.entrySet()) {
			sb.append('Q').append(entry.getKey()).append('=').append(entry.getValue()).append(", ");
		}
		sb.delete(sb.length() - 2, sb.length());
		sb.append(']');
		return sb.toString();
	}
}
