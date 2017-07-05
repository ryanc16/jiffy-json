package com.ryanc16.utils.json;
/**
 * An abstract class that is returned by the {@link JsonParser} to represent either a {@link JsonObject} or a {@link JsonArray} since the type may previously have been unknown.
 * <br>Can ask this object which type it is to determine the type to cast it to.
 * Can be cast directly to a {@link JsonObject} or {@link JsonArray} if/when type is known.
 * @author Ryan
 * */
public abstract class JsonEntity {
	/**
	 * Used to determine if this object is a {@link JsonObject}.
	 * @return true if this object was parsed as a {@link JsonObject}.
	 */
	public boolean isJsonObject() {return getClass().equals(JsonObject.class);}
	/**
	 * Used to determine if this object is a {@link JsonArray}.
	 * @return true if this object was parsed as a {@link JsonArray}.
	 */
	public boolean isJsonArray() {return getClass().equals(JsonArray.class);}
}
