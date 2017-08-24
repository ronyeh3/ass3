package TPC;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.CharacterCodingException;
import java.util.logging.Logger;
import protocol.AsyncServerProtocol;
import protocol.ProtocolCallback;
import protocol.ServerProtocol;
import protocol.TPCCallbackFactory;
import tokenizer.MessageTokenizer;

public class ConnectionHandler<T> implements Runnable {
	private SocketChannel fClientSocket;
	private ServerProtocol<T> fProtocol;
	private MessageTokenizer<T> fTokenizer;
	private ProtocolCallback<T> fCallback;
	private TPCCallbackFactory<T> fCallbackFactory;
	private static final int BUFFER_SIZE = 1024;
	private static final Logger logger = Logger.getGlobal();

	/**
	 * ConnectionHandler constructor
	 * @param socketChannel the client socket
	 * @param protocol the server protocol
	 * @param tokenizer a tokenizer which handles bytes to and from messages conversions
	 * @param callbackFactory the the ConnectionHandler callback factory
	 */
	public ConnectionHandler(SocketChannel socketChannel, ServerProtocol<T> protocol, MessageTokenizer<T> tokenizer, TPCCallbackFactory<T> callbackFactory) {
		fClientSocket = socketChannel;
		fProtocol = protocol;
		fTokenizer = tokenizer;
		fCallbackFactory = callbackFactory;
		logger.info("A new client connected to the server: "+socketChannel.socket().getRemoteSocketAddress());
	}
	
	/**
	 * @param msg the message of generic type T to send
	 */
	public void sendMessage(T msg) {
		ByteBuffer bytesBuff;
		try {
			bytesBuff = fTokenizer.getBytesForMessage(msg);
			while (bytesBuff.remaining() != 0) fClientSocket.write(bytesBuff);
		} catch (CharacterCodingException ccexception) {
			ccexception.printStackTrace();
		} catch (IOException exception) {
			logger.info("Error - IOException");
		}	
	}
	
	/**
	 * handler's lifetime- creating the callback, reading from and writing to the socket using the tokenizer and protocol
	 */
	public void run() {		
		initialize();
		try {
			process();
		} catch (IOException exception) { logger.info("Error - IOException"); } 
		logger.info("Connection terminated");
		close();
	}
	
	// the handler's main agenda, getting bytes from the socket to the tokenizer.
	private void process() throws IOException {	
		ByteBuffer bytesBuff = ByteBuffer.allocate(BUFFER_SIZE);
		int amountRead = 0;
		T newMsg = null;
		while (true) {
			amountRead = 0;
			try {
				amountRead = fClientSocket.read(bytesBuff);
			} catch (IOException exception) { 
				logger.info("Error - IOException");
				amountRead = -1; 
			}
			if (amountRead != -1) {		//received some bytes
				bytesBuff.flip();
				fTokenizer.addBytes(bytesBuff);
				if (fTokenizer.hasMessage()) {
					newMsg = fTokenizer.nextMessage();
					fProtocol.processMessage(newMsg, fCallback);
					if (fProtocol.isEnd(newMsg)) {
						close();
						break;
					}
				}
			}
			else {	// only if the channel is closed
				logger.info("client disconnected : " + fClientSocket.socket().getRemoteSocketAddress());
				close();
				if (fProtocol instanceof AsyncServerProtocol) {	// tell the protocol that the connection terminated.
					((AsyncServerProtocol<T>)fProtocol).connectionTerminated();
				}
				return;
			}
			bytesBuff.clear();
		}
	}
	
	// creating the callback
	private void initialize()  {
		fCallback = fCallbackFactory.create(this);
	}
 
	//closing the connection (End-msg received)
	private void close() {
		try {			
			fClientSocket.close();
		}
		catch (IOException exception) {
			logger.info("Error - IOException");
		}
	}
}
