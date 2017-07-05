package com.ryanc16.utils.json;
/**
 * An exception to be thrown where there is an error parsing a json string.
 * @author Ryan
 */
public class JsonParseException extends Exception{
	private static final long serialVersionUID = 1580586606242289688L;
	public JsonParseException() {
		super("Something went wrong parsing the json string.");
	}
	public JsonParseException(String message) {
		super(message);
	}

}
