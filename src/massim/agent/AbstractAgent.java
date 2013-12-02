package massim.agent;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class provides a very simple foundation to agents.
 * It will only connect once (no automatic reconnection).
 * It will authenticate itself and wait for any messages.
 * You can send ping using "sendPing" whenever
 */
public abstract class AbstractAgent {

	@SuppressWarnings("serial")
	private class SocketClosedException extends Exception {
	}

	private int networkPort;
	private String networkHost;
	private InetSocketAddress socketAddress;
	private Socket socket;

	private InputStream inputStream;
	private OutputStream outputStream;
	protected String username;
	private String password;

	protected DocumentBuilderFactory documentBuilderFactory;
	private TransformerFactory transformerFactory;

	protected static Logger logger = Logger.getLogger("agentLog.log");

	public static String getDate() {
		Date dt = new Date();
		SimpleDateFormat df = new SimpleDateFormat("HH-mm-ss_dd-MM-yyyy");
		return df.format(dt);
	}

	public AbstractAgent() {
		networkHost = "localhost";
		networkPort = 0;

		socket = new Socket();
		documentBuilderFactory = DocumentBuilderFactory.newInstance();
		transformerFactory = TransformerFactory.newInstance();
	}

	public String getHost() {
		return networkHost;
	}

	public void setHost(String host) {
		this.networkHost = host;
	}

	public int getPort() {
		return networkPort;
	}

	public void setPort(int port) {
		this.networkPort = port;
	}

	public String getUsername() {
		return username;
	}

	public void setUsername(String username) {
		this.username = username;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	/**
	 * Starts the agent main thread.
	 *
	 * @see @link agentThread
	 */
	public void start() {
		new Thread() {
			public void run() {
				agentThread();
			}
		}.start();
	}

	/**
	 * Provides a easy way for the authentication against a server.
	 * It must be called before the agent is bind to the server and
	 * the output stream is initialized.
	 *
	 * @param username Username of the actual agent.
	 * @param password Password associated with the username.
	 * @throws IOException When the connection have not been initialized.
	 */
	public void sendAuthentication(String username, String password) throws IOException {

		try {
			Document doc = documentBuilderFactory.newDocumentBuilder().newDocument();
			Element root = doc.createElement("message");
			root.setAttribute("type", "auth-request");
			doc.appendChild(root);

			Element auth = doc.createElement("authentication");
			auth.setAttribute("username", username);
			auth.setAttribute("password", password);
			root.appendChild(auth);

			this.sendDocument(doc);
		} catch (ParserConfigurationException e) {
			System.out.println("unable to create new document for authentication.");
			e.printStackTrace();
		}
	}

	/**
	 * Waits for an authentication response from the server.
	 * It must be called after the <code>sendAuthentication</code>
	 * method call.
	 *
	 * @return true when the authentication hat been successful, false otherwise.
	 * @throws IOException When the connection have not been initialized.
	 */
	public boolean receiveAuthenticationResult() throws IOException {

		try {
			Document doc = receiveDocument();
			Element root = doc.getDocumentElement();
			if (root == null) return false;
			if (!root.getAttribute("type").equalsIgnoreCase("auth-response")) return false;
			NodeList nl = root.getChildNodes();
			Element authresult = null;
			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Element.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase("authentication")) {
					authresult = (Element) n;
					break;
				}
			}
			if (!authresult.getAttribute("result").equalsIgnoreCase("ok")) return false;
		} catch (SAXException e) {
			e.printStackTrace();
			return false;
		} catch (ParserConfigurationException e) {
			e.printStackTrace();
			return false;
		} catch (SocketClosedException e) {
			e.printStackTrace();
			return false;
		}

		return true;
	}

	/**
	 * Unifies the authentication process. It sends the authentication
	 * and then waits for the response from the server.
	 *
	 * @param username Username of the actual agent.
	 * @param password Password associated with the username.
	 * @return true when the authentication hat been successful, false otherwise.
	 * @throws IOException When the connection have not been initialized.
	 * @see #sendAuthentication(String, String) sendAuthentication
	 * @see #receiveAuthenticationResult() receiveAuthenticationResult
	 */
	public boolean doAuthentication(String username, String password) throws IOException {
		sendAuthentication(username, password);
		return receiveAuthenticationResult();
	}

	/**
	 * This method manages the reception of a packet from the server.
	 * It takes no parameters and supposes the authentication is done and
	 * hat succeed. It also writes to std-err the contents of the package.
	 *
	 * @return a byte array with the response from the server.
	 * @throws IOException When the connection have not been initialized.
	 * @throws SocketClosedException
	 */
	public byte[] receivePacket() throws IOException, SocketClosedException {

		ByteArrayOutputStream buffer = new ByteArrayOutputStream();
		int read = inputStream.read();
		while (read != 0) {
			if (read == -1) {
				throw new SocketClosedException();
			}
			buffer.write(read);
			read = inputStream.read();
		}
		String s = "Server -> Agent: AgentName " + this.username + "\n" + buffer.toString();

		synchronized (logger) {
			logger.log(Level.ALL, s);
		}

		return buffer.toByteArray();
	}

	/**
	 * Receives a packet from the server using the <code>receivePacket<code>
	 * method and converts the received data to a XML Document object.
	 *
	 * @return A valid XML Document object.
	 * @throws SAXException When the received data is not well-formed.
	 * @throws IOException When the connection have not been initialized.
	 * @throws ParserConfigurationException
	 * @throws SocketClosedException
	 * @see #receivePacket() receivePacket
	 */
	public Document receiveDocument() throws SAXException, IOException, ParserConfigurationException, SocketClosedException {

		byte[] raw = receivePacket();
		Document doc = documentBuilderFactory.newDocumentBuilder().parse(new ByteArrayInputStream(raw));
		return doc;
	}

	/**
	 * Is the main agent's thread. It makes all the agent's work. First
	 * it manages the authentication, if it is not successful it will end.
	 * Then calls the <code>processLogin</code> method that is an user
	 * specified method. And next it remains in an infinite loop receiving
	 * and processing messages from the server. The messages must start with
	 * the <code>message<code> element. If it encounters any problem
	 * with the reception it ends execution.
	 *
	 * @see #doAuthentication(String, String) doAuthentication
	 * @see #processLogIn() processLogIn
	 * @see #receiveDocument() receiveDocument
	 * @see #processMessage(Element) processMessage
	 */
	public void agentThread() {

		System.out.println("Connecting to " + networkHost + " port " + networkPort);

		try {

			socketAddress = new InetSocketAddress(networkHost, networkPort);

			if (socketAddress.isUnresolved()) {
				throw new RuntimeException("The internet address " + networkHost + " at port " + networkPort + " is unresolvable");
			}

			socket.connect(socketAddress, 2000);
			inputStream = socket.getInputStream();
			outputStream = socket.getOutputStream();

			System.out.println("Successfully connected");

			boolean auth = doAuthentication(username, password);
			if (!auth) {
				System.out.println("Authentication failed");
				return;
			}
			processLogIn();
			while (true) {
				Document doc = null;
				try {
					doc = receiveDocument();
				} catch (SAXException e) {
					e.printStackTrace();
				} catch (ParserConfigurationException e) {
					e.printStackTrace();
				}

				Element el_root = doc.getDocumentElement();

				if (el_root == null) {
					System.out.println("No document element found");
					continue;
				}

				if (el_root.getNodeName().equals("message")) {
					if (!processMessage(el_root)) break;
				}
				else {
					System.out.println("Unknown document received");
				}
			}

		} catch (IOException e) {
			System.out.println("IOException");
			e.printStackTrace();
			return;
		} catch (SocketClosedException e) {
			System.out.println("Socket was closed");
		}
	}

	/**
	 * This method parses the message received from the server and selects
	 * the right action to do next. The messages must be of the type:
	 * <ol>
	 *     <li><code>request-action</code></li>
	 *     <li><code>sim-start</code></li>
	 *     <li><code>sim-end</code></li>
	 * </ol><p/>
	 * If the type is one of the first three, it builds a valid response
	 * envelop and calls the method related with the actual request which
	 * will build the correct response content for the server. The responsible
	 * of sending such response is this method also, after it is build.
	 *
	 * @param el_message XML Element object containing the message to process.
	 * @return true always
	 * @see #processRequestAction(Element, Element, long, long) processRequestAction
	 * @see #processSimulationStart(Element, long) processSimulationStart
	 * @see #processSimulationEnd(Element, long) processSimulationEnd
	 * @see #sendDocument(Document) sendDocument
	 */
	public boolean processMessage(Element el_message) {

		String type = el_message.getAttribute("type");
		if (type.equals("request-action") || type.equals("sim-start") || type.equals("sim-end")) {
			//get perception
			Element el_perception = null;
			NodeList nl = el_message.getChildNodes();
			String infoelementname = "perception";

			if (type.equals("request-action")) {
				infoelementname = "perception";
			}
			else if (type.equals("sim-start")) {
				infoelementname = "simulation";
			}
			else if (type.equals("sim-end")) {
				infoelementname = "sim-result";
			}

			for (int i = 0; i < nl.getLength(); i++) {
				Node n = nl.item(i);
				if (n.getNodeType() == Element.ELEMENT_NODE && n.getNodeName().equalsIgnoreCase(infoelementname)) {
					if (el_perception == null) el_perception = (Element) n;
					else {
						System.out.println("perception message doesn't contain right number of perception elements");
						return true;
					}
				}
			}

			Document doc = null;
			try {
				doc = documentBuilderFactory.newDocumentBuilder().newDocument();
			} catch (ParserConfigurationException e) {
				System.out.println("parser config error");
				e.printStackTrace();
				System.exit(1);
			}
			Element el_response = doc.createElement("message");

			doc.appendChild(el_response);
			Element el_action = doc.createElement("action");
			el_response.setAttribute("type", "action");
			el_response.appendChild(el_action);

			long currenttime = 0;
			try {
				currenttime = Long.parseLong(el_message.getAttribute("timestamp"));
			} catch (NumberFormatException e) {
				System.out.println("number format invalid");
				e.printStackTrace();
				return true;
			}

			long deadline = 0;

			if (type.equals("request-action")) {

				try {
					deadline = Long.parseLong(el_perception.getAttribute("deadline"));
				} catch (NumberFormatException e) {
					System.out.println("number format invalid");
					e.printStackTrace();
					return true;
				}
				processRequestAction(el_perception, el_action, currenttime, deadline);
			}
			else if (type.equals("sim-start")) {
				processSimulationStart(el_perception, currenttime);
			}
			else if (type.equals("sim-end")) {
				processSimulationEnd(el_perception, currenttime);
			}

			el_action.setAttribute("id", el_perception.getAttribute("id"));

			try {

				// sending of action only for request-action message!!!
				if (type.equals("request-action")) sendDocument(doc);

			} catch (IOException e) {
				System.out.println("IO Exception while trying to send action");
				e.printStackTrace();
				System.exit(1);
			}

		}
		return true;
	}

	public void processRequestAction(Element perception, Element target, long currenttime, long deadline) {
	}

	public void processSimulationEnd(Element perception, long currenttime) {
	}

	public void processSimulationStart(Element perception, long currenttime) {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
		}
	}

	public void processLogIn() {
	}

	/**
	 * Sends an specified XML Document to the server.
	 *
	 * @param doc An XML Document object containing the message to send.
	 * @throws IOException
	 */
	public void sendDocument(Document doc) throws IOException {
		try {
			transformerFactory.newTransformer().transform(new DOMSource(doc), new StreamResult(outputStream));

			ByteArrayOutputStream temp = new ByteArrayOutputStream();
			transformerFactory.newTransformer().transform(new DOMSource(doc), new StreamResult(temp));
			String s = "Agent -> Server:\n" + temp.toString();
			logger.log(Level.ALL, s);
			outputStream.write(0);
			outputStream.flush();
		} catch (TransformerConfigurationException e) {
			System.out.println("transformer config error");
			e.printStackTrace();
			System.exit(1);
		} catch (TransformerException e) {
			System.out.println("transformer error error");
			e.printStackTrace();
			System.exit(1);
		}
	}
}
