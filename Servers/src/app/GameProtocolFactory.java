package app;
/**
 * The GameProtocolFactory interface is a tool used to create new {@link GameProtocol}s of a given type.
 */
public interface GameProtocolFactory {
	/**
	 * Creates a new {@link GameProtocol} of a given game type.
	 * @param gameroom		the {@link GameRoom} in which the game is initialized
	 * @return		a new {@link GameProtocol}
	 */
	public GameProtocol create(GameRoom gameroom);
}
