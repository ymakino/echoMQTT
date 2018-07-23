package echomqtt.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class JValue {
    private static final Logger logger = Logger.getLogger(JValue.class.getName());
    private static final String className = JValue.class.getName();
    
    /**
     * ヌル型の値を生成する。
     * @return ヌル型の値
     */
    public static JNull newNull() {
        logger.entering(className, "newNull");
        
        JNull returnValue = new JNull();
        
        logger.exiting(className, "newNull", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された数値を持つ数値型の値を生成する。
     * @param value 数値の指定
     * @return 数値型の値
     */
    public static JNumber newNumber(long value) {
        logger.entering(className, "newNumber", value);
        
        JNumber returnValue = new JNumber(value);
        
        logger.exiting(className, "newNumber", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された数値を持つ数値型の値を生成する。
     * @param value 数値の指定
     * @return 数値型の値
     */
    public static JNumber newNumber(double value) {
        logger.entering(className, "newDecimal", value);
        
        JNumber returnValue = new JNumber(value);
        
        logger.exiting(className, "newDecimal", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された数値を持つ数値型の値を生成する。
     * @param value 数値の指定
     * @return 数値型の値
     */
    public static JNumber newNumber(BigDecimal value) {
        logger.entering(className, "newDecimal", value);
        
        JNumber returnValue = new JNumber(value);
        
        logger.exiting(className, "newDecimal", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された数値を持つ数値型の値を生成する。
     * @param value 数値の指定
     * @return 数値型の値
     */
    public static JNumber newNumber(BigInteger value) {
        logger.entering(className, "newDecimal", value);
        
        JNumber returnValue = new JNumber(new BigDecimal(value));
        
        logger.exiting(className, "newDecimal", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された真偽値を持つブーリアン型の値を生成する。
     * @param value 真偽値の指定
     * @return ブーリアン型の値
     */
    public static JBoolean newBoolean(boolean value) {
        logger.entering(className, "newBoolean", value);
        
        JBoolean returnValue = new JBoolean(value);
        
        logger.exiting(className, "newBoolean", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された文字列を持つ文字列型の値を生成する。
     * @param value 文字列の指定
     * @return 文字列型の値
     */
    public static JString newString(String value) {
        logger.entering(className, "newReal", value);
        
        JString returnValue = new JString(value);
        
        logger.exiting(className, "newReal", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された値を要素に持つ配列型の値を生成する。
     * @param values 要素の値の指定
     * @return 配列型の値
     */
    public static JArray newArray(List<? extends JValue> values) {
        logger.entering(className, "newArray", values);
        
        JArray returnValue = new JArray(values);
        
        logger.exiting(className, "newArray", returnValue);
        return returnValue;
    }
    
    /**
     * 指定された値を要素に持つ配列型の値を生成する。
     * @param values 要素の値の指定
     * @return 配列型の値
     */
    public static JArray newArray(JValue... values) {
        logger.entering(className, "newArray", values);
        
        JArray returnValue = new JArray(values);
        
        logger.exiting(className, "newArray", returnValue);
        return returnValue;
    }
    
    /**
     * 指定されたキーと値を要素に持つオブジェクト型の値を生成する。
     * @param valueMap キーと値の指定
     * @return レコード型の値
     */
    public static JObject newObject(Map<String, ? extends JValue> valueMap) {
        logger.entering(className, "newObject", valueMap);
        
        JObject returnValue = new JObject(valueMap);
        
        logger.exiting(className, "newObject", returnValue);
        return returnValue;
    }
    
    /**
     * 要素の存在しないオブジェクト型の値を生成する。
     * @return レコード型の値
     */
    public static JObject newObject() {
        logger.entering(className, "newObject");
        
        JObject returnValue = new JObject(new HashMap<String, JValue>());
        
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
    public static JValue parseJSON(String json) throws JsonDecoderException {
        logger.entering(className, "parseJSON", json);
        
        JValue returnValue = JsonDecoder.decode(json);
        
        logger.exiting(className, "parseJSON", returnValue);
        return returnValue;
    }
    
    public <T extends JValue> T cast(Class<T> cls) {
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
    
    public JArray asArray() {
        logger.entering(className, "asArray");
        
        JArray result = cast(JArray.class);
        
        logger.exiting(className, "asArray", result);
        return result;
    }
    
    public JBoolean asBoolean() {
        logger.entering(className, "asBoolean");
        
        JBoolean result = cast(JBoolean.class);
        
        logger.exiting(className, "asBoolean", result);
        return result;
    }
    
    public JNull asNull() {
        logger.entering(className, "asNull");
        
        JNull result = cast(JNull.class);
        
        logger.exiting(className, "asNull", result);
        return result;
    }
    
    public JNumber asNumber() {
        logger.entering(className, "asNumber");
        
        JNumber result = cast(JNumber.class);
        
        logger.exiting(className, "asNumber", result);
        return result;
    }
    
    public JObject asObject() {
        logger.entering(className, "asObject");
        
        JObject result = cast(JObject.class);
        
        logger.exiting(className, "asObject", result);
        return result;
    }
    
    public JString asString() {
        logger.entering(className, "asString");
        
        JString result = cast(JString.class);
        
        logger.exiting(className, "asString", result);
        return result;
    }
    
    public static void main(String[] args) {
        try {
            if (args.length != 0) {
                for (String arg: args) {
                        System.out.println(JValue.parseJSON(arg));
                        System.out.println(JValue.parseJSON(arg).toJSON());
                }
            } else {
                System.out.println(JValue.parseJSON("{\"ho\\\"ge\": 0x1234, \"fuga\":null}"));
                System.out.println(JValue.parseJSON("{\"ho\\\"ge\": 0x1234, \"fuga\":null}").toJSON());
            }
        } catch (JsonDecoderException ex) {
            logger.logp(Level.SEVERE, className, "main", "catched exception", ex);
        } catch (JsonEncoderException ex) {
            logger.logp(Level.SEVERE, className, "main", "catched exception", ex);
        }
    }
}
