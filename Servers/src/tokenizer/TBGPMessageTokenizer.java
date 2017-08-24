package tokenizer;

import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.Charset;
import java.nio.charset.CharsetDecoder;
import java.nio.charset.CharsetEncoder;
//import java.util.Vector;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TBGPMessageTokenizer implements MessageTokenizer<TBGPMessage> {
	
	private final CharsetDecoder fDecoder;
	private final CharsetEncoder fEncoder;
	private final String fFramingSign = "\n";
	private final StringBuffer stringBuff = new StringBuffer();
	//private final Vector<ByteBuffer> _buffers = new Vector<ByteBuffer>(); //maybe vector?
	//queue of received bytes (in the form of ByteBuffer)
	private final ConcurrentLinkedQueue<ByteBuffer> fRecievedBytes=new ConcurrentLinkedQueue<ByteBuffer>();
	
	public TBGPMessageTokenizer(Charset charset) {
		fDecoder = charset.newDecoder();
		fEncoder = charset.newEncoder();
	}

	/**
	 * Add some bytes to the message stream. 
	 * @param bytes an array of bytes to be appended to the message stream.
	 */
	public synchronized void addBytes(ByteBuffer bytes) {
		fRecievedBytes.add(bytes);
	}

	/**
	 * Is there a complete message ready?.
	 * @return true the next call to nextMessage() will not return null, false otherwise.
	 */
	public synchronized boolean hasMessage() {
		while(fRecievedBytes.size() > 0) {
			ByteBuffer bytes=fRecievedBytes.poll();
			CharBuffer chars=CharBuffer.allocate(bytes.remaining());
			fDecoder.decode(bytes, chars, false); // false: more bytes may follow. Any unused bytes are kept in the decoder.
			chars.flip();
			stringBuff.append(chars);
		}
		return stringBuff.indexOf(fFramingSign) > -1;
	}

	/**
	 * Get the next complete message if it exists, advancing the tokenizer to the next message.
	 * @return the next complete message, null if no complete message exist. if the command is not valid returns TBGPMessage with null command field
	 */
	public synchronized TBGPMessage nextMessage() {
		String commandParameters = null;
		String commandName = null;
		TBGPCommand command = null;
		int messageEnd = stringBuff.indexOf(fFramingSign);
		int commandEnd = stringBuff.indexOf(" ");
		if (messageEnd>-1) {
			if (commandEnd==-1 || commandEnd>messageEnd) commandName = stringBuff.substring(0, messageEnd);
			else { 	//message contains command and parameters
				commandName = stringBuff.substring(0, commandEnd);
				commandParameters = stringBuff.substring(commandEnd+" ".length(), messageEnd);
			}
			stringBuff.delete(0, messageEnd+fFramingSign.length());
			try {
				command = TBGPCommand.valueOf(commandName);	    	 
			} catch(IllegalArgumentException e) {command = null;}
		}
		else return null;
		return new TBGPMessage(command, commandParameters);
	}

	/**
	 * Convert a TBGP message into a bytes representation, taking care of encoding and framing.
	 * @return a ByteBuffer with the message content converted to bytes, after framing information has been added.
	 */
	public ByteBuffer getBytesForMessage(TBGPMessage msg) throws CharacterCodingException {
		StringBuilder sb = new StringBuilder(msg.getCommand()+" "+msg.getParameters());
		sb.append(fFramingSign);
		ByteBuffer bb = this.fEncoder.encode(CharBuffer.wrap(sb));
		return bb;
	}

}
