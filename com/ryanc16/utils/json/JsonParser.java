package com.ryanc16.utils.json;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Stack;
/**
 * An object used to parse a string or file into {@link JsonObject}s or {@link JsonArray}s.
 * @author Ryan
 */
public class JsonParser {

	private Reader src_reader;
	private Stack<Object> nesting;
	private StringBuffer json_buff;
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
		src_reader = reader;
		copyIntoBuffer();
		if(json_buff.length()==0) throw new JsonParseException("Empty string was passed to parser.");
		offset = -1;
		onkey = false;
		current = getNextCharAndAdvancePointer();
		switch(current) {
			case '{': return readObject();
			case '[': return readArray();
			default: throw new JsonParseException("Character at position 0 did not indicate an object '{' or an array '['.");
		}
	}
	/**
	 * Parses the json contained in a string into a usable Java object.
	 * @param json The string containing valid json.
	 * @return JsonEntity Object that can be cast to either {@link JsonObject} or {@link JsonArray} depending on the type of item it was parsed as.
	 * @throws JsonParseException When the json does not start with a '{' or a '[' character.
	 */
	public JsonEntity parse(String json) throws JsonParseException {
		src_reader = new StringReader(json);
		try {
			return parse(src_reader);
		}
		catch(IOException ioe) {
			throw new JsonParseException();
		}
	}
	
	private void read() throws JsonParseException{
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
			endValue();
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
			if(workingWithObject() && key==null)
				throwUnexpectedCharacterException(current);
			String _key0 = key;
			value = readObject();
			if(workingWithObject()) {
				((JsonObject)nesting.peek()).put(_key0, value);
				onkey=true;
			}
			else ((JsonArray)nesting.peek()).add(value);
			break;
		case '[':
			if(workingWithObject() && key==null)
				throwUnexpectedCharacterException(current);
			String _key1 = key;
			value = readArray();
			if(workingWithObject()) {
				((JsonObject)nesting.peek()).put(_key1, value);
				onkey = true;
			}
			else ((JsonArray)nesting.peek()).add(value);
			break;
		case '}':
		case ']':
			break;
		default: throwUnexpectedCharacterException(current);
		}
	}
	
	private JsonObject readObject() throws JsonParseException{
		key = null;
		onkey = true;
		nesting.push(new JsonObject());
		while(current!='}') read();
		if(peekNextChar()!='}') current = getNextCharAndAdvancePointer();
		return (JsonObject)nesting.pop();
	}
	
	private JsonArray readArray() throws JsonParseException{
		key=null;
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
	
	private boolean isEscapeCharacter(char c) {
		return c=='\\';
	}
	
	private boolean isControlCharacter(char c) {
		switch(c) {
			//case '"':
			case '\\':
			case '/':
			case 'b':
			case 'f':
			case 'n':
			case 'r':
			case 't':
			case 'u':
				return true;
		}
		return false;
	}
	
	private void readBoolean() throws JsonParseException{
		if(onkey) throwUnexpectedCharacterException(current);
		if(current=='t') {
			value = true;
			readNextNChars(4);
		}
		else {
			value = false;
			readNextNChars(5);
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
			if(isEscapeCharacter(current)){
				if(isControlCharacter(peekNextChar())) {
					str.append(current);
					current = getNextCharAndAdvancePointer();
				}
				else if(isQuote(peekNextChar())) {
					current = getNextCharAndAdvancePointer();
				}
			}
			str.append(current);
			current = getNextCharAndAdvancePointer();
		}
		
		if(onkey) key = str.toString();
		else value = str.toString();
	}
	
	private void readInfinity() throws JsonParseException {
		if(onkey) throwUnexpectedCharacterException(current);
		value = Double.POSITIVE_INFINITY;
		readNextNChars(8);
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
	
	private void readNumber() throws JsonParseException {
		if(onkey) throwUnexpectedCharacterException(current);
		StringBuilder number = new StringBuilder();
		boolean hasDecimal = false;
		while(isNumber(current)) {
			if(hasDecimal && current=='.') throwUnexpectedCharacterException(current);
			if(!hasDecimal && current=='.') hasDecimal = true;
			
			number.append(current);
			current = getNextCharAndAdvancePointer();
		}
		offset--;
		try{
			if(hasDecimal){
				Double d = Double.parseDouble(number.toString());
				if(d<Float.MAX_VALUE && d>Float.MIN_VALUE)
					value = Float.parseFloat(number.toString());
				else value = d;
			}
			else{
				Long l = Long.parseLong(number.toString());
				if(l<Integer.MAX_VALUE && l>Integer.MIN_VALUE)
					value = Integer.parseInt(number.toString());
				else value = l;
			}
		}catch(NumberFormatException nfe){
			throwUnexpectedCharacterException(number.charAt(0));
		}
	}
	
	private void readNull() throws JsonParseException {
		value = null;
		readNextNChars(4);
		//offset+=4;
	}
	/**
	 * Looks to see what the next character will be without advancing the offset.
	 * @return The next character in the buffer.
	 */
	private char peekNextChar() {
		return (offset+1)>json_buff.length()-1?json_buff.charAt(offset):json_buff.charAt(offset+1);
	}
	/**
	 * Gets the next character in the buffer and advances the offset.
	 * <br>If the end of the buffer has been reached, the next sequence is loaded in to the buffer and then the character is returned.
	 * @return The next character in the buffer.
	 */
	private char getNextCharAndAdvancePointer() {
		offset++;
		if(offset >= json_buff.length() && hasmore) {
			try{
				copyIntoBuffer();
			}catch(IOException ioe) {ioe.printStackTrace();}
			offset=0;
		}
		return json_buff.charAt(offset);
	}
	
	private void readNextNChars(int num_chars_to_skip) {
		for(int i=0;i<num_chars_to_skip;i++)
			current = getNextCharAndAdvancePointer();
	}
	
	private boolean workingWithObject() {
		return nesting.isEmpty()?false:nesting.peek().getClass().equals(JsonObject.class);
	}
	/**
	* Used to copy sequential pieces of a string from a reader object into the buffer.
	* Needs to use this to avoid {@link java.lang.OutOfMemoryError OutOfMemoryError} for large amounts of json.
	*/
	private void copyIntoBuffer() throws IOException {
		json_buff = new StringBuffer(BUFF_SIZE);
		char[] cbuf = new char[BUFF_SIZE];
		int read = 0;
		int total_read = 0;
		hasmore = false;
		while((read = src_reader.read(cbuf))!=-1) {
			total_read+=read;
			json_buff.append(cbuf,0,read);
			if(total_read >= MAX_BUFFER_SIZE) {
				hasmore = true;
				break;
			}
		}
		parsedBytes+=total_read;
	}
	
	private void throwUnexpectedCharacterException(char unexpected) throws JsonParseException {
		
		long location = (parsedBytes-Math.min(parsedBytes,MAX_BUFFER_SIZE))+offset;
		String message = "Unexpected character '"+unexpected+"' at position "+location+ ((nesting.peek()!=null)?" while parsing "+nesting.peek():".");
		throw new JsonParseException(message);
	}
}