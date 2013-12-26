package massim.agent.student.puzzle;

import java.io.Serializable;
import java.util.Arrays;

/**
 * A class representing a queen at the chessboard.
 */
public class Queen implements PuzzleConstants, Serializable {

	private static final long serialVersionUID = -5231646753847234315L;

	/** The number or row of the queen. */
	private final int number;

	/** The domain, possible queen positions. */
	private final boolean[] domain;

	/** Current position of the queen. */
	private int position;

	/** Constructor of the Queen class. */
	public Queen(int number, int chessboardSize) {
		if (chessboardSize < 1) throw new IllegalArgumentException("chessboardSize=" + chessboardSize);
		if (number >= chessboardSize) throw new IllegalArgumentException("number=" + number);
		this.number = number;
		domain = new boolean[chessboardSize];
		position = INVALID_QUEEN_POSITION;
		resetDomain();
	}

	/** @return the number of the queen */
	public int getNumber() {
		return number;
	}

	/** Resets the domain (possible positions) of the queen. */
	public void resetDomain() {
		Arrays.fill(domain, true);
	}

	/** Marks given positions as unavailable. */
	public void markUnavailable(int... positions) {
		for (int p : positions) {
			if (p >= 0 && p < domain.length) {
				domain[p] = false;
				position = (position == p) ? INVALID_QUEEN_POSITION : position;
			}
		}
	}

	/** Marks given positions as available. */
	public void markAvailable(int... positions) {
		for (int p : positions) {
			if (p >= 0 && p < domain.length) {
				domain[p] = true;
			}
		}
	}

	/** @return <tt>true</tt> IFF the queen has assigned some position */
	public boolean hasPosition() {
		return position != INVALID_QUEEN_POSITION;
	}

	/** @return current position of the queen */
	public int getPosition() {
		return position;
	}

	/** @return <tt>true</tt> IFF the queen has more available positions */
	public boolean hasNextPosition() {
		for (boolean p : domain) {
			if (p) return true;
		}
		return false;
	}

	/** Moves the queen to next available position if any. */
	public int nextPosition() {
		final int size = domain.length;
		final int pos = (position == INVALID_QUEEN_POSITION || position == size - 1) ? -1 : position;
		for (int p = pos + 1; p < size; p++) {
			if (domain[p]) return position = p;
		}
		return position;
	}

	/** @return <tt>true</tt> IFF given queen is a parent of this queen */
	public boolean isParentQueen(int queenNumber) {
		return number == queenNumber + 1;
	}

	/** @return <tt>true</tt> IFF given queen is a child of this queen */
	public boolean isChildQueen(int queenNumber) {
		return number < queenNumber;
	}

	@Override
	public String toString() {
		return String.format("Q%d:%d", number, position);
	}
}
