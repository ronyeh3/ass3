package reactor;

import java.nio.ByteBuffer;
import protocol.*;
import tokenizer.*;

/**
 * This class supplies some data to the protocol, which then processes the data,
 * possibly returning a reply. This class is implemented as an executor task.
 * 
 */
public class ProtocolTask<T> implements Runnable {

	private final ServerProtocol<T> _protocol;
	private final MessageTokenizer<T> _tokenizer;
	private final ConnectionHandler<T> _handler;
	private ProtocolCallback<T> _callbackProtocol;

	public ProtocolTask(final ServerProtocol<T> protocol, final MessageTokenizer<T> tokenizer, final ConnectionHandler<T> h) {
		_protocol = protocol;
		_tokenizer = tokenizer;
		_handler = h;
	}

	// we synchronize on ourselves, in case we are executed by several threads
	// from the thread pool.
	public synchronized void run() {
		_callbackProtocol = _handler.getCallback();
		// go over all complete messages and process them.
		while (_tokenizer.hasMessage()) {
			T newMsg = _tokenizer.nextMessage();
			_protocol.processMessage(newMsg, _callbackProtocol);
		}
	}

	public void addBytes(ByteBuffer b) {
		_tokenizer.addBytes(b);
	}
}
