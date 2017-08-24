package protocol;

import app.GameManager;
import app.GameRoom;
import tokenizer.TBGPCommand;
import tokenizer.TBGPMessage;

public class TBGP implements AsyncServerProtocol<TBGPMessage> {
	private GameRoom fGameRoom=null;
	private String fNickname=null;
	private boolean shouldClose = false;
	private boolean connectionTerminated = false;

	@Override
	public void processMessage(TBGPMessage msg, ProtocolCallback<TBGPMessage> callback) {
		if (connectionTerminated) return;
		boolean inARoom = (fGameRoom!=null);
		boolean acquiredAName = (fNickname!=null);
		TBGPCommand command=msg.getCommand();
		if (command!=null) {
			TBGPMessage result=null;
			switch (command) {
			case NICK:
				if (msg.getParameters()!=null && !acquiredAName){
					boolean ans = GameManager.getInstance().acquireNickname(msg.getParameters(), (TBGPProtocolCallback) callback);
					if (ans) fNickname = msg.getParameters();
					result = new TBGPMessage (TBGPCommand.SYSMSG, msg.getCommand()+" " +(ans? "ACCEPTED":"REJECTED"));
				}
				else result = new TBGPMessage (TBGPCommand.SYSMSG, msg.getCommand()+" REJECTED you already have a nickname: " + fNickname + " , the nickname can not be changed. or bad imput");
				break;
			case JOIN:
				if (!acquiredAName) {
					result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand()+" REJECTED please choose a nickname using NICK <your_nickname> command, then try again.");
				}
				// if currently in a room, checks if the room is in the middle of a game session (then we can't quit the room)
				else if (msg.getParameters()!=null && inARoom && !fGameRoom.quit((TBGPProtocolCallback) callback)) { 
					result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand()+" REJECTED please wait for the on-going game in your room to end, then try again or bad room name");
				}
				else {	// in a boring room or not in a room
					boolean ans = GameManager.getInstance().joinToRoom(msg.getParameters(), fNickname);
					if(ans) {
						fGameRoom = GameManager.getInstance().searchRoom(msg.getParameters());
						result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand()+" ACCEPTED welcome to room "+ msg.getParameters() + " !");
					}
					else result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand()+" REJECTED there is currently an on-going game in room "+ msg.getParameters() +", try again later.");
				}
				break;
			case MSG:
				if (msg.getParameters()!=null && !inARoom) {
					result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand()+" REJECTED please join a room using JOIN <room_name> command, then try again.");
				}
				else {
					result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand()+" ACCEPTED");
					fGameRoom.broadcast(fNickname + ": "+ msg.getParameters(), TBGPCommand.USRMSG);
				}
				break;
			case LISTGAMES:
				result=new TBGPMessage(TBGPCommand.SYSMSG, "ACCEPTED "+GameManager.getInstance().listGames());
				break;
			case STARTGAME:
				if (inARoom && !fGameRoom.isActive() && msg.getParameters()!=null) {
					if(fGameRoom.startGame( msg.getParameters()) ) //Check if can open a game
						result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand()+" ACCEPTED");
					else
						result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand()+" REJECTED game "+msg.getParameters()+ " dosent exsist or your rome is a midle of it");
					
				}
				else {
					result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand()+" REJECTED you're either not in room or currently in a game session, please join a room or wait for the current game to be over.");
				}
				break;
			case SELECTRESP : case TXTRESP:    //for active game
				if (msg.getParameters()!=null && inARoom && fGameRoom.isActive()) {
					fGameRoom.getGameProtocol().processMessage(msg, (TBGPProtocolCallback) callback);
				}
				else {
					result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand()+" REJECTED this command is used only while playing in an on-going game session or bad command");
				}
				break;
			case QUIT:
				if (!inARoom || inARoom&&fGameRoom.quit((TBGPProtocolCallback)callback)) {
					GameManager.getInstance().exit(fNickname);
					shouldClose = true;
					result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand()+" ACCEPTED");
				}
				else {	// in a room which has an on-going game
					result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand()+" REJECTED please wait for the current game to be over.");
				}
				break;
			default:
				result=new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand() +" UNIDENTIFIED this is not a user command!");
				break;
			}
			if (result!=null) callback.sendMessage(result);
		}
		else {
			callback.sendMessage(new TBGPMessage(TBGPCommand.SYSMSG, "EMPTY or UNFAMILIAR command! use a command from this list:\n"
					+ "NICK <nickname>, JOIN <game_room_name>, MSG <chat_message>, LISTGAMES,\n"
					+ "STARTGAME <game_name>, TXTRESP <text_response>, SELECTRESP <number_of_selected_response>, QUIT"));
		}

	}

	@Override
	public boolean isEnd(TBGPMessage msg) {
		return shouldClose;
	}

	@Override
	public boolean shouldClose() {
		return shouldClose;
	}

	@Override
	public void connectionTerminated() {
		if(fNickname!=null && fGameRoom!=null ){
		fGameRoom.quit(GameManager.getInstance().getCallback(fNickname));
		GameManager.getInstance().exit(fNickname);
		}
		if(fNickname!=null)GameManager.getInstance().exit(fNickname);
		connectionTerminated=true;
	}



}
