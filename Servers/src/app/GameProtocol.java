package app;

import protocol.TBGPProtocolCallback;
import tokenizer.TBGPMessage;
/**
 * The GameProtocol interface represents some game, which can be called upon in a {@link GameRoom}.
 * Lists the methods every game should be able to manage, including processing player commands and initializing the game.
 */
public interface GameProtocol {
	/**
	 * Receive player commands, processes them and responds accordingly.
	 * @param msg	The player's message.
	 * @param callback	The player's {link TBGPProtocolCallback}.
	 */
	public void processMessage(TBGPMessage msg, TBGPProtocolCallback callback);
	/**
	 * Initialize and begin the game.
	 * @param jsonPath	The path of the JSON file containing necessary objects for the game.
	 */
	public void initialize(String jsonPath);
}
