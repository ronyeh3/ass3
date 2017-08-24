package app;


import java.util.Iterator;
import java.util.Map.Entry;
import java.util.LinkedList;

import java.util.HashMap;


import protocol.TBGPProtocolCallback;


public class BlufferQuestion {

	private String question;
	private String realAnswer;
	private HashMap<String,LinkedList<TBGPProtocolCallback>> clientsBybluffs = new HashMap<String,LinkedList<TBGPProtocolCallback>>();
	private String[] ansOptions = null;
	/**
	 * The BlufferQuestion ctor.
	 */
	public BlufferQuestion(String question, String trueAnswer) {
		this.question = question;
		this.realAnswer = trueAnswer;
	}

	/**
	 * adds the player bluff for this question
	 */
	public void addPlayerBluff(TBGPProtocolCallback callback, String bluff) {
		if(clientsBybluffs.containsKey(bluff)) {  //maybe more then one specific bluff 
			clientsBybluffs.get(bluff).add(callback);
		} else {
			LinkedList<TBGPProtocolCallback> clientsBybluff = new LinkedList<TBGPProtocolCallback>();
			clientsBybluff.add(callback);
			clientsBybluffs.put(bluff, clientsBybluff);
		}
	}
	/**
	 * return 	a list with the true answer and the players bluffs randomly order.
	 */
	public String[] printAndMIXAnswers() {
		//ArrayList<String> x = new ArrayList<String>(bluffs.size() + 1);
		this.ansOptions = new String[clientsBybluffs.size() + 1];
		int roll = (int)(ansOptions.length*Math.random());
		this.ansOptions[roll] = realAnswer;
		Iterator<Entry<String, LinkedList<TBGPProtocolCallback>>> it = clientsBybluffs.entrySet().iterator();
		int i = 0;
		while(it.hasNext()) {
			if(ansOptions[i] == null) ansOptions[i] = it.next().getKey();
			else {
				i++;
				ansOptions[i] = it.next().getKey();
			}
			i++;
		}
		return ansOptions;
	}
	/**
	 * gets how many bluff+real ans
	 */
	public int getNumOfOptions() {
		return ansOptions.length;
	}
	/**
		*return a textresp by the selecting options order(selectorder)that made
	 */
	public String getOptions(int choiceNum) {
		if(ansOptions != null && choiceNum < ansOptions.length && choiceNum >=0) 
			 return ansOptions[choiceNum];
		else return null;
	}
	/**
    * return the list(usuale only one inside) of the client who made the bluff
	 */
	public LinkedList<TBGPProtocolCallback> getCallbackByBluff(String choice) {
		return clientsBybluffs.get(choice);
	}
	/**
	 * return this question
	 */
	public String getQuestion() {
		return question;
	}
	/**
	 * return the true answer for this question
	 */
	public String getTrueAnswer() {
		return realAnswer;
	}
}
