package protocol;

import reactor.ConnectionHandler;

public interface ProtocolCallbackFactory<T> {
	ProtocolCallback<T> create(ConnectionHandler<T> handler);
}
