package massim.agent.student;

import cz.agents.alite.communication.Message;
import massim.agent.Action;
import massim.agent.MASPerception;
import massim.agent.MASQueenAgent;
import massim.agent.Position;
import massim.agent.student.puzzle.ChessBoard;
import massim.agent.student.puzzle.PuzzleConstants;
import massim.agent.student.puzzle.Queen;
import massim.agent.student.utils.MessageData;
import massim.agent.student.utils.MessageUtils;

import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * MAS queen agent implementation using cooperative Asynchronous Backtracking (ABT).
 */
public class MyQueenAgent extends MASQueenAgent implements PuzzleConstants {

	private static final boolean INFO = true, DEBUG = true, VERBOSE = false;

	/** The total number of agents in the system. */
	private final int size;
	/** The agents' chessboard. */
	private final ChessBoard chessBoard;

	/** Meta-data about agents' friends. */
	private final Map<String, AgentMetadata> friendMetadata;
	/** The no-good store of the agent. */
	private final Map<Integer, NoGood> noGoodStore;

	/** Current state of the agent. */
	private AgentState state;
	/** Current position of the agent. */
	private Position myPosition;
	/** A queen assigned to the agent. */
	private Queen myQueen;

	/** Constructor of the MyQueenAgent class. */
	public MyQueenAgent(String host, int port, String username, String password, int nAgents) {
		super(host, port, username, password);
		size = nAgents;
		chessBoard = new ChessBoard(size);
		friendMetadata = new LinkedHashMap<String, AgentMetadata>(size);
		noGoodStore = new LinkedHashMap<Integer, NoGood>(size);
		state = AgentState.initI;
		myPosition = null;
		myQueen = null;
	}

	@Override
	protected Action deliberate(MASPerception percept) {
		final long t = System.currentTimeMillis();

		// refresh agents' position
		myPosition = new Position(percept.getPosX(), percept.getPosY());

		processMessages();

		// decide the next action
		final Action action;
		switch (state) {
			case initI:
			case initII:
				action = doInit();
				break;
			case working:
				action = doAbtWork();
				break;
			case idle:
				action = getNextAction();
				break;
			case finished:
			default:
				action = Action.SKIP;
		}

		printVerbose("step=" + percept.getStep() +  " action=" + action + " t=" + (System.currentTimeMillis() - t));
		return action;
	}

	/** Processing of the messages in agents' inbox. */
	protected void processMessages() {
		final List<Message> messages = getNewMessages();
		for (Message message : messages) {
			// parse received data
			MessageData data = MessageUtils.parse(message);

			// retrieve meta-data about the sender (friend agent)
			AgentMetadata metadata = friendMetadata.get(message.getSender());
			if (metadata == null) {
				metadata = new AgentMetadata(message.getSender());
				friendMetadata.put(message.getSender(), metadata);
			}

			String type = data.getType();
			printDebug("MSG from Q" + ((metadata.queen == null) ? "?" : metadata.queen) + " [" + type + ": " + data.getData() + "]");

			// processing of general messages
			if ("myState".equals(type)) {
				metadata.state = MessageUtils.getData(data);
			} else if ("myPosition".equals(type)) {
				metadata.position = MessageUtils.getData(data);
			} else if ("myQueen".equals(type)) {
				metadata.queen = MessageUtils.<Integer>getData(data);
			}

			processAbtMessage(data, metadata);
		}
	}

	/** Sends an <tt>Ok?</tt> messages. */
	protected void sendOk() {
		for (AgentMetadata metadata : friendMetadata.values()) {
			if (metadata.isChild) {
				sendMessage(metadata.getName(), MessageUtils.create("Ok?", myQueen.getPosition()));
			}
		}
	}

	/** Sends a <tt>NoGood</tt> messages. */
	protected void sendNoGood(NoGood noGood) {
		for (AgentMetadata metadata : friendMetadata.values()) {
			if (metadata.isParent) {
				sendMessage(metadata.getName(), MessageUtils.create("NoGood", noGood));
			}
		}
	}

	/** Processing of ABT messages. */
	private void processAbtMessage(MessageData data, AgentMetadata metadata) {
		final String type = data.getType();
		if ("Ok?".equals(type)) {
			// update agents' context
			chessBoard.setPosition(metadata.queen, MessageUtils.<Integer>getData(data));
			state = AgentState.working;
		} else if ("NoGood".equals(type)) {
			// verify received no-good
			final NoGood noGood = MessageUtils.getData(data);
			if (noGood.verifyContext(chessBoard)) {
				// apply the no-good
				final int position = noGood.getPosition(myQueen.getNumber());
				myQueen.markUnavailable(position);
				noGoodStore.put(position, noGood.createNoGoodForQueen(myQueen));
				state = AgentState.working;
			}
		}
	}

	/** Agent initialization, establish agent hierarchy. */
	private Action doInit() {
		if (myQueen == null) {
			// send agents' queen
			myQueen = new Queen(myPosition.getY() - 1, size);
			printInfo("my queen is Q" + myQueen.getNumber());
			broadcast(MessageUtils.create("myQueen", myQueen.getNumber()));
			broadcast(MessageUtils.create("myPosition", myPosition));
		} else if (friendMetadata.size() == size - 1) {
			if (state == AgentState.initI) {
				// establish agent hierarchy
				int count = 0;
				for (AgentMetadata metadata : friendMetadata.values()) {
					if (metadata.queen != null) {
						metadata.isParent = myQueen.isParentQueen(metadata.queen);
						metadata.isChild = myQueen.isChildQueen(metadata.queen);
						count++;
					}
				}
				if (count == size - 1) {
					// print agent hierarchy
					final StringBuilder parents = new StringBuilder("parents:");
					final StringBuilder children = new StringBuilder("children:");
					for (AgentMetadata metadata : friendMetadata.values()) {
						if (metadata.isParent) parents.append(" Q").append(metadata.queen);
						if (metadata.isChild) children.append(" Q").append(metadata.queen);
					}
					printDebug(parents.toString());
					printDebug(children.toString());

					state = AgentState.initII;
					broadcast(MessageUtils.create("myState", AgentState.working));
				}
			} else {
				// start working when all other agents are also ready
				int count = 0;
				for (AgentMetadata metadata : friendMetadata.values()) {
					if (metadata.state == AgentState.working) count++;
				}
				if (count == size - 1) {
					state = AgentState.working;
				}
			}
		}

		return Action.SKIP;
	}

	/** Performs actual coordinated ABT (check of the agent view). */
	private Action doAbtWork() {
		// find & remove invalid no-goods
		final List<Integer> nowAvailable = new LinkedList<Integer>();
		for (Map.Entry<Integer, NoGood> entry : noGoodStore.entrySet()) {
			if (!entry.getValue().verifyContext(chessBoard)) {
				// context is no longer valid
				nowAvailable.add(entry.getKey());
			}
		}
		for (Integer value : nowAvailable) {
			noGoodStore.remove(value);
			myQueen.markAvailable(value);
		}

		// determine the queen position
		boolean valid = myQueen.hasPosition() && chessBoard.checkConstraints();
		if (!valid && myQueen.hasNextPosition()) {
			for (int i = 0; i <= size; i++) {
				myQueen.nextPosition();
				chessBoard.setPosition(myQueen);

				// validate constraints
				valid = myQueen.hasPosition() && chessBoard.checkConstraints();
				if (valid) {
					sendOk();
					break;
				}
			}
		}

		if (!valid) {
			myQueen.invalidate();

			// send no-good if unfeasible
			final NoGood noGood = chessBoard.getNoGoodForQueen(myQueen);
			printDebug("sending no-good " + noGood + "\n" + chessBoard);
			sendNoGood(noGood);
		}

		state = AgentState.idle;
		return getNextAction();
	}

	/** @return next action for the agent */
	private Action getNextAction() {
		return ChessBoard.getAction(myPosition.getX(), myQueen.getPosition() + 1);
	}

	/** Prints given message to STD-OUT if in INFO mode. */
	protected void printInfo(String message) {
		if (INFO) System.out.println(username + ": " + message);
	}

	/** Prints given message to STD-OUT if in DEBUG mode. */
	protected void printDebug(String message) {
		if (DEBUG) System.out.println(username + "(" + myQueen + "): " + message);
	}

	/** Prints given message to STD-OUT if in VERBOSE mode. */
	protected void printVerbose(String message) {
		if (VERBOSE) System.out.println(username + ": " + message);
	}
}
