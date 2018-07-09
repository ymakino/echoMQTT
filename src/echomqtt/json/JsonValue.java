package echomqtt.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class JsonValue {
    private static final Logger logger = Logger.getLogger(JsonValue.class.getName());
    private static final String className = JsonValue.class.getName();
    
    /**
     * ヌル型の値を生成する。
     * @return ヌル型の値
     */
    public static JsonNull newNull() {
        logger.entering(className, "newNull");
        
        JsonNull returnValue = new JsonNull();
        
        logger.exiting(className, "newNull", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された数値を持つ数値型の値を生成する。
     * @param value 数値の指定
     * @return 数値型の値
     */
    public static JsonNumber newNumber(long value) {
        logger.entering(className, "newNumber", value);
        
        JsonNumber returnValue = new JsonNumber(value);
        
        logger.exiting(className, "newNumber", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された数値を持つ数値型の値を生成する。
     * @param value 数値の指定
     * @return 数値型の値
     */
    public static JsonNumber newNumber(double value) {
        logger.entering(className, "newDecimal", value);
        
        JsonNumber returnValue = new JsonNumber(value);
        
        logger.exiting(className, "newDecimal", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された数値を持つ数値型の値を生成する。
     * @param value 数値の指定
     * @return 数値型の値
     */
    public static JsonNumber newNumber(BigDecimal value) {
        logger.entering(className, "newDecimal", value);
        
        JsonNumber returnValue = new JsonNumber(value);
        
        logger.exiting(className, "newDecimal", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された数値を持つ数値型の値を生成する。
     * @param value 数値の指定
     * @return 数値型の値
     */
    public static JsonNumber newNumber(BigInteger value) {
        logger.entering(className, "newDecimal", value);
        
        JsonNumber returnValue = new JsonNumber(new BigDecimal(value));
        
        logger.exiting(className, "newDecimal", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された真偽値を持つブーリアン型の値を生成する。
     * @param value 真偽値の指定
     * @return ブーリアン型の値
     */
    public static JsonBoolean newBoolean(boolean value) {
        logger.entering(className, "newBoolean", value);
        
        JsonBoolean returnValue = new JsonBoolean(value);
        
        logger.exiting(className, "newBoolean", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された文字列を持つ文字列型の値を生成する。
     * @param value 文字列の指定
     * @return 文字列型の値
     */
    public static JsonString newString(String value) {
        logger.entering(className, "newReal", value);
        
        JsonString returnValue = new JsonString(value);
        
        logger.exiting(className, "newReal", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された値を要素に持つ配列型の値を生成する。
     * @param values 要素の値の指定
     * @return 配列型の値
     */
    public static JsonArray newArray(List<? extends JsonValue> values) {
        logger.entering(className, "newArray", values);
        
        JsonArray returnValue = new JsonArray(values);
        
        logger.exiting(className, "newArray", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された値を要素に持つ配列型の値を生成する。
     * @param values 要素の値の指定
     * @return 配列型の値
     */
    public static JsonArray newArray(JsonValue... values) {
        logger.entering(className, "newArray", values);
        
        JsonArray returnValue = new JsonArray(values);
        
        logger.exiting(className, "newArray", returnValue);
        return returnValue;
    }
    
    /**
     * 指定されたキーと値を要素に持つレコード型の値を生成する。
     * @param valueMap キーと値の指定
     * @return レコード型の値
     */
    public static JsonObject newObject(Map<String, ? extends JsonValue> valueMap) {
        logger.entering(className, "newObject", valueMap);
        
        JsonObject returnValue = new JsonObject(valueMap);
        
        logger.exiting(className, "newObject", returnValue);
        return returnValue;
    }
    
    /**
     * JSON形式の文字列を生成する。
     * @return JSON形式の文字列
     */
    public String toJSON() throws JsonEncoderException {
        logger.entering(className, "toJSON");
        
        String json = JsonEncoder.encode(this);
        
        logger.exiting(className, "toJSON", json);
        return json;
    }
    
    /**
     * JSON形式の文字列からValue型の値を生成する。
     * @param json JSON形式の文字列
     * @return 生成されたValue型の値
     */
    public static JsonValue parseJSON(String json) throws JsonDecoderException {
        logger.entering(className, "parseJSON", json);
        
        JsonValue returnValue = JsonDecoder.decode(json);
        
        logger.exiting(className, "parseJSON", returnValue);
        return returnValue;
    }
    
    public <T extends JsonValue> T cast(Class<T> cls) {
        logger.entering(className, "cast", cls);
        
        if (!cls.isInstance(this)) {
            logger.exiting(className, "cast", null);
            return null;
        }
        
        logger.exiting(className, "cast", (T)this);
        return (T)this;
    }
    
    public boolean isArray() {
        logger.entering(className, "isArray");
        
        logger.exiting(className, "isArray", false);
        return false;
    }
    
    public boolean isBoolean() {
        logger.entering(className, "isBoolean");
        
        logger.exiting(className, "isBoolean", false);
        return false;
    }
    
    public boolean isNull() {
        logger.entering(className, "isNull");
        
        logger.exiting(className, "isNull", false);
        return false;
    }
    
    public boolean isNumber() {
        logger.entering(className, "isNumber");
        
        logger.exiting(className, "isNumber", false);
        return false;
    }
    
    public boolean isObject() {
        logger.entering(className, "isObject");
        
        logger.exiting(className, "isObject", false);
        return false;
    }
    
    public boolean isString() {
        logger.entering(className, "isString");
        
        logger.exiting(className, "isString", false);
        return false;
    }
    
    public JsonArray asArray() {
        logger.entering(className, "asArray");
        
        JsonArray result = cast(JsonArray.class);
        
        logger.exiting(className, "asArray", result);
        return result;
    }
    
    public JsonBoolean asBoolean() {
        logger.entering(className, "asBoolean");
        
        JsonBoolean result = cast(JsonBoolean.class);
        
        logger.exiting(className, "asBoolean", result);
        return result;
    }
    
    public JsonNull asNull() {
        logger.entering(className, "asNull");
        
        JsonNull result = cast(JsonNull.class);
        
        logger.exiting(className, "asNull", result);
        return result;
    }
    
    public JsonNumber asNumber() {
        logger.entering(className, "asNumber");
        
        JsonNumber result = cast(JsonNumber.class);
        
        logger.exiting(className, "asNumber", result);
        return result;
    }
    
    public JsonObject asObject() {
        logger.entering(className, "asObject");
        
        JsonObject result = cast(JsonObject.class);
        
        logger.exiting(className, "asObject", result);
        return result;
    }
    
    public JsonString asString() {
        logger.entering(className, "asString");
        
        JsonString result = cast(JsonString.class);
        
        logger.exiting(className, "asString", result);
        return result;
    }
    
    public static void main(String[] args) {
        try {
            if (args.length != 0) {
                for (String arg: args) {
                        System.out.println(JsonValue.parseJSON(arg));
                        System.out.println(JsonValue.parseJSON(arg).toJSON());
                }
            } else {
                System.out.println(JsonValue.parseJSON("{\"ho\\\"ge\": 0x1234, \"fuga\":null}"));
                System.out.println(JsonValue.parseJSON("{\"ho\\\"ge\": 0x1234, \"fuga\":null}").toJSON());
            }
        } catch (JsonDecoderException ex) {
            logger.logp(Level.SEVERE, className, "main", "catched exception", ex);
        } catch (JsonEncoderException ex) {
            logger.logp(Level.SEVERE, className, "main", "catched exception", ex);
        }
    }
}
