package reactor;

import java.nio.charset.Charset;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;
import app.GameManager;
import protocol.AsyncServerProtocol;
import protocol.MyFormatter;
import protocol.ProtocolCallback;
import protocol.ProtocolCallbackFactory;
import protocol.ServerProtocolFactory;
import protocol.TBGP;
import tokenizer.MessageTokenizer;
import tokenizer.TBGPMessage;
import tokenizer.TBGPMessageTokenizer;
import tokenizer.TokenizerFactory;

public class Server {
	
	private final static Logger logger=Logger.getGlobal();
	private final static ConsoleHandler consoleHandler=new ConsoleHandler();
	private final static MyFormatter myFormatter=new MyFormatter();
	
	/**
	 * Main program. Create and run a Reactor-based server for the TBGP protocol. 
	 * args[0] (command line) port\n
	 * args[1] (command line) poolsize\n
	 * args[2] (command line) json path\n
	 */
	public static void main(String args[]) {
		
		LogManager.getLogManager().reset();
		LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).setLevel(Level.CONFIG);
		LogManager.getLogManager().getLogger(Logger.GLOBAL_LOGGER_NAME).addHandler(consoleHandler);
		consoleHandler.setFormatter(myFormatter); 

		if (args.length != 3) {
		System.err.println("Usage: java Reactor <port> <pool_size> <json_paths>");
		System.exit(1);
		}

		String[] jsonPaths = {args[2] + ".json"};
		//String[] jsonPaths = {"jsonExample/2.json"}; 
		GameManager.getInstance().initialize(jsonPaths);

		try {
			int port = Integer.parseInt(args[0]);
			int poolSize = Integer.parseInt(args[1]);
			//int port = 8090;
			//int poolSize = 7;	
			Reactor<TBGPMessage> reactor = startTBGPServer(port, poolSize);

			Thread thread = new Thread(reactor);
			thread.start();
			logger.info("Reactor is ready on port " + reactor.getPort());
			thread.join();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	
	public static Reactor<TBGPMessage> startTBGPServer(int port, int poolSize) {
        ServerProtocolFactory<TBGPMessage> protocolMaker = new ServerProtocolFactory<TBGPMessage>() {
            public AsyncServerProtocol<TBGPMessage> create() {
                return new TBGP();
            }
        };

        final Charset charset = Charset.forName("UTF-8");
        TokenizerFactory<TBGPMessage> tokenizerMaker = new TokenizerFactory<TBGPMessage>() {
            public MessageTokenizer<TBGPMessage> create() {
                return new TBGPMessageTokenizer(charset);
            }
        };
        
        ProtocolCallbackFactory<TBGPMessage> callbackMaker = new ProtocolCallbackFactory<TBGPMessage>() {
        	public ProtocolCallback<TBGPMessage> create(ConnectionHandler<TBGPMessage> handler) {
                return new TBGPCallback(handler);
            }
        };

        Reactor<TBGPMessage> reactor = new Reactor<TBGPMessage>(port, poolSize, protocolMaker, tokenizerMaker, callbackMaker);
        return reactor;
    }
}
