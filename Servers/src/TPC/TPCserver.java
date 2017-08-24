package TPC;

import java.io.*;
import java.net.*;
import java.nio.channels.ServerSocketChannel;
import java.util.logging.Logger;
import protocol.ServerProtocolFactory;
import protocol.TPCCallbackFactory;
import tokenizer.TokenizerFactory;

public class TPCserver<T> implements Runnable {

	private ServerSocketChannel fServerSocket;
	private ServerProtocolFactory<T> fServerProtocolFactory;
	private TokenizerFactory<T> fTokenizerFactory;
	private TPCCallbackFactory<T> fCallbackFactory;
	private int fListenPort;
	private static final Logger logger = Logger.getGlobal();

	/**
	 * TPCserver constructor
	 * @param port the port number for the server socket
	 * @param protocolFactory server protocol factory needed to create new protocols for new connections
	 * @param tokenizer tokenizer factory needed to create new tokenizers for new connections
	 * @param callbackFactory callback factory in order to create a new callback fo every new connection
	 */
	public TPCserver(int port, ServerProtocolFactory<T> protocolFactory, TokenizerFactory<T> tokenizer, TPCCallbackFactory<T> callbackFactory) {
		fServerSocket = null;
		fListenPort = port;
		fServerProtocolFactory = protocolFactory;
		fTokenizerFactory =  tokenizer;
		fCallbackFactory = callbackFactory;
	}

	/**
	 * the server main agenda, waiting for new connections, assigning each with a connection handler
	 */
	public void run() {
		initialize();
		while (true) {
			try {
				ConnectionHandler<T> newConnection = new ConnectionHandler<T>(fServerSocket.accept(), fServerProtocolFactory.create(), fTokenizerFactory.create(),fCallbackFactory);
				new Thread(newConnection).start();
			}
			catch (IOException e) {
				logger.info("Failed to accept on port " + fListenPort);
			}
		}
	}

	/**
	 * closes the connection
	 * @throws IOException
	 */
	public void close() throws IOException {
		fServerSocket.close();
	}

	private void initialize() {	//creating the server socket
		try {
			ServerSocketChannel ssChannel = ServerSocketChannel.open();
			ssChannel.socket().bind(new InetSocketAddress(fListenPort));
			fServerSocket = ssChannel;
			logger.info("Listening... (port: " + fListenPort+")");
		} catch (IOException exception) {
			logger.info("Error - IOException - can not create ServerSocket :falied to listen to port "+fListenPort+" ");
		}
	}

}


