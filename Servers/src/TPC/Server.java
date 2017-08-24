package TPC;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import app.GameManager;
import protocol.AsyncServerProtocol;
import protocol.ProtocolCallback;
import protocol.ServerProtocolFactory;
import protocol.TBGP;
import protocol.TPCCallbackFactory;
import tokenizer.MessageTokenizer;
import tokenizer.TBGPMessage;
import tokenizer.TBGPMessageTokenizer;
import tokenizer.TokenizerFactory;
import protocol.MyFormatter;

public class Server {
	
	private final static Logger logger=Logger.getGlobal();
	private final static ConsoleHandler consoleHandler=new ConsoleHandler();
	private final static MyFormatter myFormatter=new MyFormatter();
	
	/**
	 * main function- creates and runs a TBGP TPC server 
	 * @param args  first argument- port number. second argument- json paths for the games
	 * @throws IOException
	 */
	public static void main(String[] args) throws IOException {
		
		LogManager.getLogManager().reset();
		LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.CONFIG);
		LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).addHandler(consoleHandler);
		consoleHandler.setFormatter(myFormatter); 
		
		 if (args.length != 2) {
	            System.err.println("Usage: java TPCserver <port> <json_paths>");
	            System.exit(1);
		 }
		 
	    int port = Integer.decode(args[0]).intValue();
		String[] jsonPaths = {args[1] + ".json"};
	//int port=8090;
	//String[] jsonPaths = {"jsonExample/2.json"};     // change to path
        GameManager.getInstance().initialize(jsonPaths);
		TPCserver<TBGPMessage> server = new TPCserver<TBGPMessage>(port, new ServerProtocolFactory<TBGPMessage>() {
            public AsyncServerProtocol<TBGPMessage> create() {
                return new TBGP();
            }
        }, new TokenizerFactory<TBGPMessage>(){
			public MessageTokenizer<TBGPMessage> create() {
		        final Charset charset = Charset.forName("UTF-8");
				return new TBGPMessageTokenizer(charset);
			}
        }, new TPCCallbackFactory<TBGPMessage>(){

			@Override
			public ProtocolCallback<TBGPMessage> create(ConnectionHandler<TBGPMessage> handler) {
				return new TBGPCallback(handler);
			}
        	
        });
		
		Thread serverThread = new Thread(server);
      serverThread.start();
		try {
			serverThread.join();
			server.close();
			logger.info("Server stopped");
		} catch (InterruptedException e) {
			logger.info("Server stopped");
		}
				
	}

}
