package app;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Set;
import java.util.HashMap;
import java.util.LinkedList;
import protocol.TBGPProtocolCallback;
import tokenizer.TBGPCommand;
import tokenizer.TBGPMessage;

public class BlufferProtocol implements GameProtocol {
	
	private String gameState = "prestate";
	private BlufferQuestion[] questions = new BlufferQuestion[3];
	private int numOfCurrentQuestion = 0;
	private GameRoom gameRoom;
	private int playerCounter;
	private StringBuilder totalscors = new StringBuilder();
	private HashMap<TBGPProtocolCallback, Integer> scores;
	private HashMap<TBGPProtocolCallback, Integer> currentRoundScore;
	private HashMap<TBGPProtocolCallback, Boolean> wasCorrect;
	/**
	 *  BlufferProtocol CTOR.
	 */
	public BlufferProtocol(String jsonPath, GameRoom gameroom) {
		this.gameRoom = gameroom;
		scores = new HashMap<TBGPProtocolCallback, Integer>();
		currentRoundScore = new HashMap<TBGPProtocolCallback, Integer>();
		wasCorrect = new HashMap<TBGPProtocolCallback, Boolean>();
		playerCounter = gameRoom.numOfPlayers();
		initialize(jsonPath);         //Initiate game immediately AND START TO ASK
	}
	
	@Override
	/**
   * PROCESS PARTICULAR TBGP-MASSAGE THAT TBGP SENST ACORDING TO THE GAMESTATE
	 */
	public synchronized void processMessage(TBGPMessage msg, TBGPProtocolCallback callback) {
		switch(gameState) {
			case "prestate":
				callback.sendMessage(new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand() + " REJECTED: Game NOT started"));
				break;
				
			case "toBluff":
				if(msg.getCommand() != TBGPCommand.TXTRESP) {
					callback.sendMessage(new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand() + " REJECTED: enter a bluff for the question"));
				} else {
					String bluff = msg.getParameters().toLowerCase(); //change to  lower case letters.
					playerCounter--;   //sync safe
					if(!bluff.equals(questions[numOfCurrentQuestion].getTrueAnswer())) {
						questions[numOfCurrentQuestion].addPlayerBluff(callback, bluff);
					}
					callback.sendMessage(new TBGPMessage(TBGPCommand.SYSMSG, "TXTRESP ACCEPTED"));
					if(playerCounter == 0) {
						String[] choices = questions[numOfCurrentQuestion].printAndMIXAnswers();
						String choiceList = "";
						for(int i = 0; i < choices.length; i++) {
							choiceList = choiceList + (i+1) + ". " + choices[i] + " ";
						}
						gameRoom.broadcast("choose one answr from: " +choiceList, TBGPCommand.ASKCHOICES);
						playerCounter = gameRoom.numOfPlayers();
						gameState = "toChoice";
					}
				}
				break;
			case "toChoice":
				if(msg.getCommand() != TBGPCommand.SELECTRESP) {
					callback.sendMessage(new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand() + " REJECTED: select an answer from list"));
				} else {
					Integer choiceNum = -1;
					try {
						choiceNum = Integer.parseInt(msg.getParameters());
					} catch(NumberFormatException e) {
						callback.sendMessage(new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand() + " REJECTED: Response shold be an integer"));
						break;
					}
					if(choiceNum.intValue() < questions[numOfCurrentQuestion].getNumOfOptions()+1 && choiceNum.intValue() >= 1) {
						playerCounter--;
						callback.sendMessage(new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand() + " ACCEPTED"));
						callback.sendMessage(new TBGPMessage(TBGPCommand.GAMEMSG, "The correct answer is: " + questions[numOfCurrentQuestion].getTrueAnswer()));
						
						  if(questions[numOfCurrentQuestion].getOptions(choiceNum-1) == questions[numOfCurrentQuestion].getTrueAnswer()) {
							currentRoundScore.put(callback, currentRoundScore.get(callback) + 10); //Update player score for correct answer
							wasCorrect.put(callback, true); //Update that player picked the correct answer
					    	} else { //wrong ans , give point to the one who made this flase  answer - the choiceNum
							LinkedList<TBGPProtocolCallback> bluffersCallbacks = questions[numOfCurrentQuestion].getCallbackByBluff( questions[numOfCurrentQuestion].getOptions(choiceNum-1) );   // serch who tricked a false ans
							bluffersCallbacks.forEach((i) -> {
								currentRoundScore.put(i, currentRoundScore.get(i) + 5); //Update bluffer score if a player picked his bluff - usualy only one client in this list
							});
						}
					} else callback.sendMessage(new TBGPMessage(TBGPCommand.SYSMSG, msg.getCommand() + " REJECTED: incorrent input range"));
					if(playerCounter == 0) {
						wasCorrect.forEach((k,v) -> {
							int roundScore = currentRoundScore.get(k);
							k.sendMessage(new TBGPMessage(TBGPCommand.GAMEMSG, (v? "Correct!! ":"Wrong-") +" you got in this round " +roundScore + "pts"));
							scores.put(k, scores.get(k) + roundScore);
							currentRoundScore.put(k, 0);   //frashing currnt scores round for next question
							wasCorrect.put(k, false);
						});
						if(numOfCurrentQuestion < 2) {
							numOfCurrentQuestion++;  //go to next qusetion
							gameRoom.broadcast(questions[numOfCurrentQuestion].getQuestion(), TBGPCommand.ASKTXT);
							playerCounter = gameRoom.numOfPlayers();
							gameState = "toBluff";
						} else {  //finised game 
							scores.forEach((k,v) -> {
								totalscors.append(gameRoom.getPlayerNick(k) + ": " + v + "pts || ") ;  					
							});
							gameRoom.broadcast("Summary: " + totalscors.toString(), TBGPCommand.GAMEMSG);
							gameRoom.endGame();
						}
					}
				}
				break;
		}

	}
	/**
	 * Initiate game with json file,
	 * and the playars info.
	 */
	@Override
	public void initialize(String jsonPath) {
		JsonParser parser = new JsonParser();
		ArrayList<BlufferQuestion> tempArrQuestions = new ArrayList<BlufferQuestion>();
		try {
			FileReader file = new FileReader(jsonPath);
			JsonObject pars = (JsonObject)parser.parse(file);
			JsonArray jquestions = pars.get("questions").getAsJsonArray();
			for(int i=0; i<jquestions.size(); i++){
				JsonObject current = jquestions.get(i).getAsJsonObject();
				String question = current.get("questionText:").getAsString();
				String answer = current.get("realAnswer:").getAsString().toLowerCase();
				tempArrQuestions.add(new BlufferQuestion(question,answer));
			}
			for (int j=0; j<3; j++){
				questions[j] = tempArrQuestions.remove((int)(Math.random()*tempArrQuestions.size()));
			}
			gameRoom.broadcast(questions[0].getQuestion(), TBGPCommand.ASKTXT);
			gameState = "toBluff";
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		Set<TBGPProtocolCallback> playerList = gameRoom.getPlayerList();    //initiate players info
		playerList.forEach((i) -> {
			scores.put(i, 0);
			currentRoundScore.put(i, 0);
			wasCorrect.put(i, false);
		});
	}

}
