package protocol;

/**
 * A protocol that describes the behavior of the server .
 * @param <T> type of messages the protocol handles .
 */
public interface ServerProtocol <T> {
	/**
	 * Processes a message
	 * @param msg the message to process
	 * @param callback unique {@link ProtocolCallback} to the connection from which the message was originated
	 */
	void processMessage (T msg, ProtocolCallback <T > callback);
	
	/**
	 * Determine whether the given message is the termination message .
	 * @param msg the message to examine
	 * @return true if the message is the termination message , false otherwise
	 */
	boolean isEnd ( T msg ) ;
}
