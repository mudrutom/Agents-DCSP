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
	 * The no-good, a map of positions assigned to queens. The map keys
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

	/** @return <tt>true</tt> IFF given queen has assigned position */
	public boolean hasPosition(int queen) {
		return noGood.containsKey(queen);
	}

	/** @return a position assigned to given queen if any */
	public int getPosition(int queen) {
		return noGood.containsKey(queen) ? noGood.get(queen) : INVALID_QUEEN_POSITION;
	}

	/** Assigns a position for given queen. */
	public void setPosition(int queen, int position) {
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

	/** @return new no-good using this no-good for given queen */
	public NoGood createNoGoodForQueen(Queen queen) {
		final int queenNumber = queen.getNumber();
		final NoGood newNoGood = new NoGood();
		for (Map.Entry<Integer, Integer> entry : noGood.entrySet()) {
			if (entry.getKey() != queenNumber) {
				newNoGood.setPosition(entry.getKey(), entry.getValue());
			}
		}
		return newNoGood;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append('[');
		for (Map.Entry<Integer, Integer> entry : noGood.entrySet()) {
			sb.append('Q').append(entry.getKey()).append('=').append(entry.getValue()).append(", ");
		}
		if (sb.length() > 1) {
			sb.delete(sb.length() - 2, sb.length());
		}
		sb.append(']');
		return sb.toString();
	}
}
