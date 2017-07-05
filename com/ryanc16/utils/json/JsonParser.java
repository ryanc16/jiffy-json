package com.ryanc16.utils.json;
import java.io.IOException;
import java.io.Reader;
import java.util.Stack;
/**
 * An object used to parse a string or file into JsonObjects or JsonArrays.
 * @author Ryan
 */
public class JsonParser {

	private Reader file_reader;
	private Stack<Object> nesting;
	private StringBuffer file_buff;
	private int offset;
	private final int MAX_BUFFER_SIZE = 1048576;//1MB
	private final int BUFF_SIZE = 1024;//1KB
	private boolean onkey;
	private boolean hasmore;
	private char current;
	private String key;
	private Object value;
	/**
	 * Indicates the total number of bytes parsed during parsing process.
	 * <br>Can indicate file size or string length.
	 */
	public long parsedBytes = 0L;

	/**
	 * Used to construct a new JsonParser object.
	 */
	public JsonParser() {
		nesting = new Stack<Object>();
		hasmore = false;
	}
	/**
	 * Parses the json from a reader into a usable Java object.
	 * @param reader The reader in which to read the json from. Can be a {@link java.io.FileReader FileReader}, {@link java.io.InputStreamReader InputStreamReader}, etc..
	 * @return {@link JsonEntity} object that can be cast to either {@link JsonObject} or {@link JsonArray} depending on the type of item it was parsed as.
	 * @throws JsonParseException When the json does not start with a '{' or a '[' character.
	 * @throws IOException When the buffer cannot read from the supplied reader object. Could indicate closed stream etc..
	 */
	public JsonEntity parse(Reader reader) throws JsonParseException,IOException {
		file_reader = reader;
		copyIntoBuffer();
		return parse(file_buff.toString());
	}
	/**
	 * Parses the json contained in a string into a usable Java object.
	 * @param json The string containing valid json.
	 * @return JsonEntity Object that can be cast to either {@link JsonObject} or {@link JsonArray} depending on the type of item it was parsed as.
	 * @throws JsonParseException When the json does not start with a '{' or a '[' character.
	 */
	public JsonEntity parse(String json) throws JsonParseException {
		offset = -1;
		onkey = false;
		current = getNextCharAndAdvancePointer();
		switch(current) {
			case '{': return readObject();
			case '[': return readArray();
			default: throw new JsonParseException("Character at position 0 did not indicate an object '{' or an array '['.");
		}
	}
	
	private void read() {
		current = getNextCharAndAdvancePointer();
		skipWhitespace();
		switch(current) {
		case ':':
			startValue();
			break;
		case ',':
			if(workingWithObject())
				startKey();
			break;
		case '"':
			readString();
			if(!onkey)endValue();
			break;
		case 't':
		case 'f':
			readBoolean();
			endValue();
			break;
		case 'I':
			readInfinity();
			break;
		case '-':
		case '0':
		case '1':
		case '2':
		case '3':
		case '4':
		case '5':
		case '6':
		case '7':
		case '8':
		case '9':
			readNumber();
			endValue();
			break;
		case 'n':
			readNull();
			endValue();
			break;
		case '{':
			String _key0 = key;
			value = readObject();
			if(workingWithObject()) {
				((JsonObject)nesting.peek()).put(_key0, value);
				onkey=true;
			}
			else ((JsonArray)nesting.peek()).add(value);
			break;
		case '[':
			String _key1 = key;
			value = readArray();
			if(workingWithObject()) {
				((JsonObject)nesting.peek()).put(_key1, value);
				onkey = true;
			}
			else ((JsonArray)nesting.peek()).add(value);
			break;
		default: break;
		}
	}
	
	private JsonObject readObject() {
		onkey = true;
		nesting.push(new JsonObject());
		while(current!='}') read();
		if(peekNextChar()!='}') current = getNextCharAndAdvancePointer();
		return (JsonObject)nesting.pop();
	}
	
	private JsonArray readArray() {
		nesting.push(new JsonArray());
		while(current!=']') read();
		if(peekNextChar()!=']') current = getNextCharAndAdvancePointer();
		return (JsonArray)nesting.pop();
	}
	
	private void startKey() {
		onkey = true;
	}
	
	private void startValue() {
		onkey = false;
	}
	
	private void endValue() {
		if(onkey)return;
		if(workingWithObject()) {
			((JsonObject)nesting.peek()).put(key, value);
			startKey();
		}
		else ((JsonArray)nesting.peek()).add(value);
	}
	
	private boolean isWhitespace(char current) {
		switch(current) {
			case '\t':
			case '\n':
			case '\r':
			case '\f':
			case ' ':
				return true;
		}
		return false;
	}
	
	private void skipWhitespace() {
		while(isWhitespace(current))
			current = getNextCharAndAdvancePointer();
	}
	
	private boolean isEscapeCharacter(char current) {
		return current=='\\';
	}
	
	private void readBoolean() {
		if(current=='t') {
			value = true;
			offset+=3;
		}
		else {
			value = false;
			offset+=4;
		}
	}
	
	private boolean isQuote(char current) {
		if(current=='"') return true;
		return false;
	}
	
	private void readString() {
		StringBuilder str = new StringBuilder();
		current = getNextCharAndAdvancePointer();
		while(!isQuote(current)) {
			if(isEscapeCharacter(current))
				current = getNextCharAndAdvancePointer();
			str.append(current);
			current = getNextCharAndAdvancePointer();
		}
		if(onkey) key = str.toString();
		else value = str.toString();
	}
	
	private void readInfinity() {
		value = Double.POSITIVE_INFINITY;
		offset+=7;
	}
	
	private boolean isNumber(char current) {
		switch(current) {
			case '-':
			case '.':
			case '0':
			case '1':
			case '2':
			case '3':
			case '4':
			case '5':
			case '6':
			case '7':
			case '8':
			case '9':
				return true;
		}
		return false;
	}
	
	private void readNumber() {
		StringBuilder number = new StringBuilder();
		boolean hasDecimal = false;
		while(isNumber(current)) {
			if(!hasDecimal && current=='.') hasDecimal = true;
			number.append(current);
			current = getNextCharAndAdvancePointer();
		}
		offset--;
		if(hasDecimal)
			value = Double.parseDouble(number.toString());
		else value = Long.parseLong(number.toString());
	}
	
	private void readNull() {
		value = null;
		offset+=3;
	}
	/**
	 * Looks to see what the next character will be without advancing the offset.
	 * @return The next character in the buffer.
	 */
	private char peekNextChar() {
		return (offset+1)>file_buff.length()-1?file_buff.charAt(offset):file_buff.charAt(offset+1);
	}
	/**
	 * Gets the next character in the buffer and advances the offset.
	 * <br>If the end of the buffer has been reached, the next sequence is loaded in to the buffer and then the character is returned.
	 * @return The next character in the buffer.
	 */
	private char getNextCharAndAdvancePointer() {
		offset++;
		if(offset >= file_buff.length() && hasmore) {
			try{
				copyIntoBuffer();
			}catch(IOException ioe) {ioe.printStackTrace();}
			offset=0;
		}
		return file_buff.charAt(offset);
	}
	
	private boolean workingWithObject() {
		return nesting.isEmpty()?false:nesting.peek().getClass().equals(JsonObject.class);
	}
	/**
	* Used to copy sequential pieces of a string from a reader object into the buffer.
	* Needs to use this to avoid {@link java.lang.OutOfMemoryError OutOfMemoryError}
	*/
	private void copyIntoBuffer() throws IOException {
		file_buff = new StringBuffer(BUFF_SIZE);
		char[] cbuf = new char[BUFF_SIZE];
		int read = 0;
		int total_read = 0;
		hasmore = false;
		while((read = file_reader.read(cbuf))!=-1) {
			total_read+=read;
			file_buff.append(cbuf,0,read);
			if(total_read >= MAX_BUFFER_SIZE) {
				hasmore = true;
				break;
			}
		}
		parsedBytes+=total_read;
	}
	
}