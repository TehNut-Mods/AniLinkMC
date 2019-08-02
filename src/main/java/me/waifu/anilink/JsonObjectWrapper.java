package me.waifu.anilink;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;

public class JsonObjectWrapper {

    private final JsonObject wrapped;

    public JsonObjectWrapper(JsonObject wrapped) {
        this.wrapped = wrapped;
    }

    public boolean has(String name) {
        return wrapped.has(name);
    }

    public boolean isNull(String name) {
        return wrapped.get(name).isJsonNull();
    }

    public long getLong(String name) {
        return has(name) ? wrapped.getAsJsonPrimitive(name).getAsLong() : 0L;
    }

    public int getInt(String name) {
        return has(name) ? wrapped.getAsJsonPrimitive(name).getAsInt() : 0;
    }

    public short getShort(String name) {
        return has(name) ? wrapped.getAsJsonPrimitive(name).getAsShort() : 0;
    }

    public byte getByte(String name) {
        return has(name) ? wrapped.getAsJsonPrimitive(name).getAsByte() : 0;
    }

    public float getFloat(String name) {
        return has(name) ? wrapped.getAsJsonPrimitive(name).getAsFloat() : 0.0F;
    }

    public double getDouble(String name) {
        return has(name) ? wrapped.getAsJsonPrimitive(name).getAsDouble() : 0.0D;
    }

    public String getString(String name) {
        return has(name) ? wrapped.getAsJsonPrimitive(name).getAsString() : "";
    }

    public char getCharacter(String name) {
        return has(name) ? wrapped.getAsJsonPrimitive(name).getAsCharacter() : Character.MIN_VALUE;
    }

    public boolean getBoolean(String name) {
        return has(name) && wrapped.getAsJsonPrimitive(name).getAsBoolean();
    }

    public JsonObjectWrapper getObject(String name) {
        return new JsonObjectWrapper(has(name) ? wrapped.getAsJsonObject(name) : new JsonObject());
    }

    public JsonArray getArray(String name) {
        return has(name) ? wrapped.getAsJsonArray(name) : new JsonArray();
    }

    public JsonObject unwrap() {
        return wrapped;
    }
}
