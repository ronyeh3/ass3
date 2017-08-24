package reactor;

import java.nio.charset.CharacterCodingException;


import protocol.TBGPProtocolCallback;
import tokenizer.TBGPMessage;

public class TBGPCallback implements TBGPProtocolCallback {
	
	private ConnectionHandler<TBGPMessage> fConnectionHandler;

	public TBGPCallback(ConnectionHandler<TBGPMessage> connectionHandler) {
		fConnectionHandler=connectionHandler;
	}

	@Override
	public void sendMessage(TBGPMessage msg)  {
		try {
			fConnectionHandler.addOutData(msg);
		} catch (CharacterCodingException e) {
			e.printStackTrace();
		}
	}
	
}
