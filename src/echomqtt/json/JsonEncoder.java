package echomqtt.json;

import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * JSON文字列とValueの値の間での相互変換を実現する。
 * @author ymakino
 */
public class JsonEncoder {
    private static final Logger logger = Logger.getLogger(JsonEncoder.class.getName());
    private static final String className = JsonEncoder.class.getName();

    private JValue value;
    private HashMap<Character, String> encodeMap = new HashMap<Character, String>();

    public JsonEncoder(JValue value) {
        logger.entering(className, "JsonEncoder", value);

        this.value = value;

        encodeMap.put('\b', "\\b");
        encodeMap.put('\f', "\\f");
        encodeMap.put('\n', "\\n");
        encodeMap.put('\r', "\\r");
        encodeMap.put('\t', "\\t");
        encodeMap.put('\"', "\\\"");
        encodeMap.put('\\', "\\\\");

        logger.exiting(className, "JsonEncoder");
    }

    /**
     * 指定された数値の値を文字列に変換する。
     * @param value 数値の値の指定
     * @return 変換された文字列
     */
    public String encodeNumber(JNumber value) {
        logger.entering(className, "encodeNumber", value);
        String returnString = value.getValue().toPlainString();
        logger.exiting(className, "encodeNumber", returnString);
        return returnString;
    }

    /**
     * ヌル型の値を文字列に変換する。
     * @param value ヌル型の値の指定
     * @return 変換された文字列
     */
    public String encodeNull(JNull value) {
        logger.entering(className, "encodeNull", value);
        String returnString = "null";
        logger.exiting(className, "encodeNull", returnString);
        return returnString;
    }

    /**
     * 指定された真偽値を文字列に変換する。
     * @param value 真偽値の指定
     * @return 変換された文字列
     */
    public String encodeBoolean(JBoolean value) {
        logger.entering(className, "encodeBoolean", value);

        String returnString;
        if (value.getValue()) {
            returnString = "true";
        } else {
            returnString = "false";
        }

        logger.exiting(className, "encodeBoolean", returnString);
        return returnString;
    }

    private void escapeChar(char c, StringBuilder builder) {
        logger.entering(className, "escapeChar", new Object[]{c, builder});

        String s = encodeMap.get(c);
        if (s != null) {
            builder.append(s);
        } else {
            builder.append(c);
        }

        logger.exiting(className, "escapeChar");
    }

    private void escapeString(String str, StringBuilder builder) {
        logger.entering(className, "escapeString", new Object[]{str, builder});

        int len = str.length();

        for (int i = 0; i < len; i++) {
            char c = str.charAt(i);
            escapeChar(c, builder);
        }

        logger.exiting(className, "escapeString");
    }

    /**
     * 指定された文字列の値を文字列に変換する。
     * @param value 文字列の値の指定
     * @return 変換された文字列
     */
    public String encodeString(JString value) {
        logger.entering(className, "encodeString", value);

        StringBuilder builder = new StringBuilder();

        builder.append('"');
        escapeString(value.getValue(), builder);
        builder.append('"');

        String returnString = builder.toString();
        logger.exiting(className, "encodeString", returnString);
        return returnString;
    }

    /**
     * 指定された配列の値を文字列に変換する。
     * @param value 配列の値の指定
     * @return 変換された文字列
     */
    public String encodeArray(JArray value) throws JsonEncoderException {
        logger.entering(className, "encodeArray", value);

        int len = value.size();
        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < len; i++) {
            if (i != 0) {
                builder.append(",");
            }
            builder.append(encode(value.get(i)));
        }

        String returnString = "[" + builder.toString() + "]";
        logger.exiting(className, "encodeArray", returnString);
        return returnString;
    }

    /**
     * 指定されたレコードの値を文字列に変換する。
     * @param value レコードの値の指定
     * @return 変換された文字列
     */
    public String encodeObject(JObject value) throws JsonEncoderException {
        logger.entering(className, "encodeObject", value);

        boolean first = true;
        StringBuilder builder = new StringBuilder();
        for (String member : value.getMembers()) {
            if (!first) {
                builder.append(",");
            }
            builder.append('"');
            escapeString(member, builder);
            builder.append('"');
            builder.append(":");
            builder.append(encode(value.get(member)));
            first = false;
        }

        String returnString = "{" + builder.toString() + "}";
        logger.exiting(className, "encodeObject", returnString);
        return returnString;
    }

    public String encodePartial(JValue valuePartial) throws JsonEncoderException {
        logger.entering(className, "encodePartial", valuePartial);
        
        String returnString;
        
        if (valuePartial.isArray()) {
            returnString = encodeArray(valuePartial.asArray());
        } else if (valuePartial.isBoolean()) {
            returnString = encodeBoolean(valuePartial.asBoolean());
        } else if (valuePartial.isNull()) {
            returnString = encodeNull(valuePartial.asNull());
        } else if (valuePartial.isNumber()) {
            returnString = encodeNumber(valuePartial.asNumber());
        } else if (valuePartial.isObject()) {
            returnString = encodeObject(valuePartial.asObject());
        } else if (valuePartial.isString()) {
            returnString = encodeString(valuePartial.asString());
        } else {
            JsonEncoderException exception = new JsonEncoderException("invalide value " + valuePartial);;
            logger.throwing(className, "encodePartial", exception);
            throw exception;
        }

        logger.exiting(className, "encode", returnString);
        return returnString;
    }
    
    public String encode() throws JsonEncoderException {
        logger.entering(className, "encode");
        
        String returnString = encodePartial(value);

        logger.exiting(className, "encode", returnString);
        return returnString;
    }
    
    public static String encode(JValue value) throws JsonEncoderException {
        logger.entering(className, "encode", value);
        
        JsonEncoder encoder = new JsonEncoder(value);
        String returnValue = encoder.encode();
        
        logger.exiting(className, "encode", returnValue);
        return returnValue;
    }
}
