package protocol;

import TPC.ConnectionHandler;

public interface TPCCallbackFactory<T> {
	ProtocolCallback<T> create(ConnectionHandler<T> handler);

}
