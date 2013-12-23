package massim.agent.student.puzzle;

import massim.agent.Action;
import massim.agent.student.NoGood;

import java.util.Arrays;

/**
 * A class representing a chessboard for the N-queen puzzle.
 */
public class ChessBoard implements PuzzleConstants {

	/** The size of the chess board. */
	private final int size;

	/** The positions of the queens. */
	private final int[] queenPositions;

	/** Constructor of the ChessBoard class. */
	public ChessBoard(int size) {
		this.size = size;
		queenPositions = new int[size];
		invalidatePositions();
	}

	/** Invalidates positions of all the queens. */
	public void invalidatePositions() {
		Arrays.fill(queenPositions, INVALID_QUEEN_POSITION);
	}

	/** Sets position for the given queen. */
	public void setPosition(Queen queen) {
		setPosition(queen.getNumber(), queen.getPosition());
	}

	/** Sets position for the given queen. */
	public void setPosition(int queen, int position) {
		validate(queen, position);
		queenPositions[queen] = position;
	}

	/** @return position of the given queen */
	public int getPosition(int queen) {
		validate(queen);
		return queenPositions[queen];
	}

	/**
	 * Checks whether all the constrains for the N-queen puzzle are
	 * satisfied, i.e. uniqueness of row, column and diagonal positions.
	 */
	public boolean checkConstraints() {
		for (int i = 0; i < size; i++) {
			int queen = queenPositions[i];
			if (queen != INVALID_QUEEN_POSITION) {
				for (int j = 0; j < size; j++) {
					int other = queenPositions[j];
					if (i != j && other != INVALID_QUEEN_POSITION) {
						// check for column constraints
						if (queen == other) {
							return false;
						}
						// check for diagonal constraints
						if (queen - i == other - j || queen + i == other + j) {
							return false;
						}
					}
				}
			}
		}
		return true;
	}

	/** @return a no-good (constraint violations) for given queen */
	public NoGood getNoGoodForQueen(Queen queen) {
		return getNoGoodForQueen(queen.getNumber());
	}

	/** @return a no-good (constraint violations) for given queen number */
	public NoGood getNoGoodForQueen(int n) {
		validate(n);
		final NoGood noGood = new NoGood();
		final int queen = queenPositions[n];
		for (int i = 0; i < size; i++) {
			int other = queenPositions[i];
			if (i != n && other != INVALID_QUEEN_POSITION) {
				// column constraints
				if (queen == other) {
					noGood.setViolation(i, other);
				}
				// diagonal constraints
				if (queen - n == other - i || queen + n == other + i) {
					noGood.setViolation(i, other);
				}
			}
		}
		return noGood;
	}

	/** Throws an exception if given positions are not valid. */
	private void validate(int... positions) {
		for (int position : positions) {
			if (position != INVALID_QUEEN_POSITION && (position < 0 || position >= size)) {
				throw new IllegalArgumentException("invalid position " + position);
			}
		}
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder(size + size * size);
		for (int i = 0; i < size; i++) {
			char[] row = new char[size];
			if (queenPositions[i] != INVALID_QUEEN_POSITION) {
				Arrays.fill(row, '-');
				row[queenPositions[i]] = 'Q';
			} else {
				Arrays.fill(row, '?');
			}
			sb.append(row).append('\n');
		}
		return sb.toString();
	}

	/** @return an Action to get form one position to the other */
	public static Action getAction(int fromX, int toX) {
		return (fromX == toX) ? Action.SKIP : (fromX < toX) ? Action.WEST : Action.EAST;
	}
}
