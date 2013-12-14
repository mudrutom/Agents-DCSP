package massim.agent.student;

import cz.agents.alite.communication.Message;
import massim.agent.Action;
import massim.agent.MASPerception;
import massim.agent.MASQueenAgent;
import massim.agent.Position;
import massim.agent.student.puzzle.ChessBoard;
import massim.agent.student.puzzle.PuzzleConstants;
import massim.agent.student.utils.MessageData;
import massim.agent.student.utils.MessageUtils;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * MAS queen agent implementation using cooperative Asynchronous Backtracking (ABT).
 */
public class MyQueenAgent extends MASQueenAgent implements PuzzleConstants {

	private static final boolean INFO = true, DEBUG = true, VERBOSE = true;

	/** The total number of agents in the system. */
	private final int size;
	/** The agents' chessboard. */
	private final ChessBoard chessBoard;

	/** Meta-data about agents' friends. */
	private final Map<String, AgentMetadata> friendMetadata;

	/** Current state of the agent. */
	private AgentState state;
	/** Current position of the agent. */
	private Position myPosition;
	/** A queen assigned to the agent. */
	private int myQueen;

	/** Constructor of the MyQueenAgent class. */
	public MyQueenAgent(String host, int port, String username, String password, int nAgents) {
		super(host, port, username, password);
		size = nAgents;
		chessBoard = new ChessBoard(size);
		friendMetadata = new LinkedHashMap<String, AgentMetadata>(size);
		state = AgentState.init;
		myPosition = null;
		myQueen = INVALID_QUEEN_POSITION;
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
			case init:
				action = doInit();
				break;
			case working:
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
			String type = data.getType();
			printDebug("MSG from " + message.getSender() + " [" + type + ": " + data.getData() + "]");

			// retrieve meta-data about the sender (friend agent)
			AgentMetadata metadata = friendMetadata.get(message.getSender());
			if (metadata == null) {
				metadata = new AgentMetadata(message.getSender());
				friendMetadata.put(message.getSender(), metadata);
			}

			// processing of general messages
			if ("myState".equals(type)) {
				metadata.state = MessageUtils.getData(data);
			} else if ("myPosition".equals(type)) {
				metadata.position = MessageUtils.getData(data);
			} else if ("myQueen".equals(type)) {
				metadata.queen = MessageUtils.<Integer>getData(data);
			}
		}
	}

	/** Agent initialization, establish agent hierarchy. */
	private Action doInit() {
		if (myQueen == INVALID_QUEEN_POSITION) {
			// send agents' queen
			myQueen = myPosition.getY() - 1;
			broadcast(MessageUtils.create("myQueen", myQueen));
			broadcast(MessageUtils.create("myPosition", myPosition));
			printInfo("my queen is " + myQueen);
		} else {
			// establish agent hierarchy
			int count = 0;
			for (AgentMetadata metadata : friendMetadata.values()) {
				if (metadata.queen != null) {
					count++;
					metadata.isParent = myQueen - 1 == metadata.queen;
					metadata.isChild = myQueen > metadata.queen;
				}
			}
			if (count == size) {
				setState(AgentState.working);
			}
		}

		return Action.SKIP;
	}

	/** @param state new state of the agent */
	protected void setState(AgentState state) {
		this.state = state;
		broadcast(MessageUtils.create("myState", state));
	}

	/** Prints given message to STD-OUT if in INFO mode. */
	protected void printInfo(String message) {
		if (INFO) System.out.println(username + ": " + message);
	}

	/** Prints given message to STD-OUT if in DEBUG mode. */
	protected void printDebug(String message) {
		if (DEBUG) System.out.println(username + "(" + state + "): " + message);
	}

	/** Prints given message to STD-OUT if in VERBOSE mode. */
	protected void printVerbose(String message) {
		if (VERBOSE) System.out.println(username + ": " + message);
	}
}
