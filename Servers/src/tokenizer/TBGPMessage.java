package tokenizer;

public class TBGPMessage implements Message<TBGPMessage> {
	private TBGPCommand fCommand;
	private String fParameters;
	
	public TBGPMessage(TBGPCommand command, String parameters) {
		fCommand=command;
		fParameters=parameters;
	}

	public TBGPCommand getCommand() {
		return fCommand;
	}

	public String getParameters() {
		return fParameters;
	}
	
}
