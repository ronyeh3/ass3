package app;

import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import protocol.TBGPProtocolCallback;
import java.util.logging.Logger;

public class GameManager {
	
	private HashMap<String,GameProtocolFactory> games;   //list of games
	private ConcurrentHashMap<String,TBGPProtocolCallback> players;
	private ConcurrentHashMap<String,GameRoom> gamerooms;
	private String gameList = "";
	private final static Logger logger=Logger.getGlobal();
	
	private static class GameManagerInstance {
		private static GameManager instance = new GameManager();    //singelton
	}
	/**
	 * private ctor
	 */
	private GameManager() {
		players = new ConcurrentHashMap<String,TBGPProtocolCallback>();
		gamerooms = new ConcurrentHashMap<String,GameRoom>();
		games = new HashMap<String,GameProtocolFactory>();
	}
	/**
	 * all methods access from here
	 */
	public static GameManager getInstance() {
		return GameManagerInstance.instance;
	}
	/**
	 *  subscribe a nickname
	 */
	public synchronized boolean acquireNickname(String nickname, TBGPProtocolCallback callback) {
		if(players.containsKey(nickname)) {
			logger.info("Nickname taken");
			return false;
		} else {
			logger.info("the nick : "+nickname +  " accepted");
			players.put(nickname, callback);
			return true;
		}
	}
	/**
	 * Check  if game exists
	 */
	public GameProtocolFactory searchGame(String gameName) {
		if(games.containsKey(gameName)) return games.get(gameName);
		else {
			logger.info("Game " + gameName + " is not available");
			return null;
		}
	}
	/**
	 * Deletes the player from the singelton.
	 */
	public void exit(String nickname) {
		if(nickname != null && players.containsKey(nickname)) {
			players.remove(nickname);
			logger.info(nickname + " exit");
		}
		else 
			logger.info(nickname + " hasent subscribed");
	}
	/**
	 * Join to a room if possible.
	 */
	public synchronized boolean joinToRoom(String roomName, String nickname) {   //to change!!!!!!!!!!!!!!!!!!!
		if(gamerooms.containsKey(roomName)) {
			GameRoom gameRoom = gamerooms.get(roomName);
				if(gameRoom.isActive()) {
					logger.info(nickname + " unable to join to game room " + " in a midlle of a game");
					return false;
				} else {
					gameRoom.addPlayer(nickname,players.get(nickname));
					logger.info(nickname + " join " + roomName + " game room");
					return true;
				}
			
		} else {
			GameRoom gameRoom = new GameRoom(roomName);
				gameRoom.addPlayer(nickname,players.get(nickname));
				gamerooms.put(roomName, gameRoom);
				logger.info(nickname + " joined " + roomName + " game room");
				return true;
		}
	}

	public GameRoom searchRoom(String roomName) {
		if(gamerooms.containsKey(roomName)) 
			return gamerooms.get(roomName);
		else 
			return null;
	}

	public String listGames() {
		return gameList;
	}

	public TBGPProtocolCallback getCallback(String nick){
		return players.get(nick);
	}
	/**
     *adds a game
	 */
	public void initialize(String[] jsonPaths) {
		games.put("BLUFFER", (GameRoom gameroom)-> {   //creates "lamda class" of bluffer	, implament creatmethod							
				return new BlufferProtocol(jsonPaths[0], gameroom); //FIRST PATH OF THE BLUFERGAME
		});
		gameList +="BLUFFER"+ gameList + " ";
	}
}
