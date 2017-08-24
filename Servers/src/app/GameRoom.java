package app;

import java.util.logging.Logger;
import java.util.concurrent.ConcurrentHashMap;
import java.util.Set;
import tokenizer.TBGPCommand;
import tokenizer.TBGPMessage;
import protocol.TBGPProtocolCallback;

public class GameRoom {
	
	private ConcurrentHashMap<TBGPProtocolCallback, String> players;
	private String thisRoomName;
	private GameProtocol currentGame = null;
	private final static Logger logger=Logger.getGlobal();
	
	/**
     *ctor
	 */
	public GameRoom(String name) {
		this.thisRoomName = name;
		players = new ConcurrentHashMap<TBGPProtocolCallback, String>();
	}
	/**
	 * Starts a game with current players
	 */
	public synchronized boolean startGame(String gameName) {
		if(!isActive()) {
			GameProtocolFactory game1 = GameManager.getInstance().searchGame(gameName);
			if (game1!=null){
				this.currentGame = game1.create(this);    //when creates called, game starts
				logger.info(gameName + " game starting");
				return true;
			}
			else{
				logger.info(gameName + " DOSENT EXIST");
				return false;
			}
		} else {
			logger.info("A different game is already in session");
			return false;
		}
	}
	/**
	 *check if active
	 */
	public boolean isActive() {
		return currentGame != null;
	}
	/**
	 * Add a player to game room.
	 */
	public void addPlayer(String nickname, TBGPProtocolCallback callback) {
		broadcast(nickname + " joined the room", TBGPCommand.SYSMSG);
		players.put(callback, nickname);
	}
	/**
	 * Send a message to all players in the game room.
	 */
	public void broadcast(String msg, TBGPCommand command) {
		players.forEach((k,v) -> k.sendMessage(new TBGPMessage(command,msg)));
	}
	/**
	 * remove a player fron the game room if possible
	 */
	public boolean quit(TBGPProtocolCallback callback) {
		if(!isActive()) {
			String removedPlayer = players.remove(callback);
			logger.info(removedPlayer + " left the room");
			broadcast(removedPlayer + " left room "+this.thisRoomName, TBGPCommand.SYSMSG);
			return true;
		} else {
			logger.info("Unable to leave room in a middle of a game");
			return false;
		}
	}

	public String toString() {
		return thisRoomName;
	}

	public int numOfPlayers() {
		return players.size();
	}
	
	public GameProtocol getGameProtocol() {
		return currentGame;
	}


	public Set<TBGPProtocolCallback> getPlayerList() {
			return players.keySet();
	}

	public String getPlayerNick(TBGPProtocolCallback callback) {
		return players.get(callback);
	}

	public void endGame() {
		currentGame = null;
	}

}
