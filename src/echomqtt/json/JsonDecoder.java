package echomqtt.json;

import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * JSON文字列とValueの値の間での相互変換を実現する。
 * @author ymakino
 */
public class JsonDecoder {
    private static final Logger logger = Logger.getLogger(JsonDecoder.class.getName());
    private static final String className = JsonDecoder.class.getName();

    private String string;
    private int length;
    private int index;

    public JsonDecoder(String string) {
        logger.entering(className, "JsonDecoder", string);

        this.string = string;
        this.length = string.length();
        this.index = 0;

        logger.exiting(className, "JsonDecoder");
    }

    private boolean finished() {
        logger.entering(className, "finished");
        
        boolean result = length == index;
        logger.exiting(className, "finished", result);
        return result;
    }

    private char getNextChar() throws JsonDecoderException {
        logger.entering(className, "getNextChar");

        if (index < string.length()) {
            char ch = string.charAt(index);
            logger.exiting(className, "getNextChar", ch);
            return ch;
        } else {
            JsonDecoderException exception = new JsonDecoderException(index);
            logger.throwing(className, "getNextChar", exception);
            throw exception;
        }
    }

    private boolean isNextNumber() throws JsonDecoderException {
        logger.entering(className, "isNextNumber");
        
        char c = getNextChar();
        boolean result = Character.isDigit(c);
        
        logger.exiting(className, "isNextNumber", result);
        return result;
    }

    private boolean isNextBoolean() throws JsonDecoderException {
        logger.entering(className, "isNextBoolean");
        
        char c = getNextChar();
        boolean result = c == 't' || c == 'f';
        
        logger.exiting(className, "isNextBoolean", result);
        return result;
    }

    private boolean isNextNull() throws JsonDecoderException {
        logger.entering(className, "isNextNull");
        
        char c = getNextChar();
        boolean result = c == 'n';
        
        logger.exiting(className, "isNextNull", result);
        return result;
    }

    private boolean isNextChar(char checkChar) throws JsonDecoderException {
        logger.entering(className, "isNextChar");
        
        char c = getNextChar();
        boolean result = c == checkChar;
        
        logger.exiting(className, "isNextChar", result);
        return result;
    }

    private boolean isNextObjectBegin() throws JsonDecoderException {
        logger.entering(className, "isNextObjectBegin");
        
        boolean result = isNextChar('{');
        
        logger.exiting(className, "isNextObjectBegin", result);
        return result;
    }

    private boolean isNextObjectEnd() throws JsonDecoderException {
        logger.entering(className, "isNextObjectEnd");
        
        boolean result = isNextChar('}');
        
        logger.exiting(className, "isNextObjectEnd", result);
        return result;
    }

    private boolean isNextArrayBegin() throws JsonDecoderException {
        logger.entering(className, "isNextArrayBegin");
        
        boolean result = isNextChar('[');
        
        logger.exiting(className, "isNextArrayBegin", result);
        return result;
    }

    private boolean isNextArrayEnd() throws JsonDecoderException {
        logger.entering(className, "isNextArrayEnd");
        
        boolean result = isNextChar(']');
        
        logger.exiting(className, "isNextArrayEnd", result);
        return result;
    }

    private boolean isNextDoubleQuote() throws JsonDecoderException {
        logger.entering(className, "isNextDoubleQuote");
        
        boolean result = isNextChar('"');
        
        logger.exiting(className, "isNextDoubleQuote", result);
        return result;
    }

    private boolean isNextBackSlash() throws JsonDecoderException {
        logger.entering(className, "isNextBackSlash");
        
        boolean result = isNextChar('\\');
        
        logger.exiting(className, "isNextBackSlash", result);
        return result;
    }

    private boolean isNextColon() throws JsonDecoderException {
        logger.entering(className, "isNextColon");
        
        boolean result = isNextChar(':');
        
        logger.exiting(className, "isNextColon", result);
        return result;
    }

    private boolean isNextComma() throws JsonDecoderException {
        logger.entering(className, "isNextComma");
        
        boolean result = isNextChar(',');
        
        logger.exiting(className, "isNextComma", result);
        return result;
    }

    private boolean isNextMinus() throws JsonDecoderException {
        logger.entering(className, "isNextMinus");
        
        boolean result = isNextChar('-');
        
        logger.exiting(className, "isNextMinus", result);
        return result;
    }

    private void consumeChar() throws JsonDecoderException {
        logger.entering(className, "consumeChar");
        
        if (finished()) {
            JsonDecoderException exception = new JsonDecoderException(index);
            logger.throwing(className, "publish", exception);
            throw exception;
        }

        index++;
        
        logger.exiting(className, "consumeChar");
    }

    private void consumeChar(char c) throws JsonDecoderException {
        logger.entering(className, "consumeChar", c);
        
        if (finished()) {
            JsonDecoderException exception = new JsonDecoderException(index);
            logger.throwing(className, "publish", exception);
            throw exception;
        }

        if (getNextChar() != c) {
            JsonDecoderException exception = new JsonDecoderException(index);
            logger.throwing(className, "publish", exception);
            throw exception;
        }

        index++;
        
        logger.exiting(className, "consumeChar");
    }

    private void skipSpaces() {
        logger.entering(className, "skipSpaces");
        
        for (int i = index; i < length; i++) {
            char c = string.charAt(i);

            if (!Character.isWhitespace(c)) {
                break;
            }

            index = i + 1;
        }
        
        logger.exiting(className, "skipSpaces");
    }

    private JValue parseNumberWithSign(int sign) throws JsonDecoderException {
        logger.entering(className, "parseNumberWithSign", sign);
        
        int firstIndex = index;
        int lastIndex = index;

        for (int i = firstIndex; i < length; i++) {
            char c = string.charAt(i);
            if (!Character.isDigit(c) && c != '.') {
                break;
            }

            consumeChar();
            lastIndex++;
        }

        if (firstIndex == lastIndex) {
            JsonDecoderException exception = new JsonDecoderException(index);
            logger.throwing(className, "parseNumberWithSign", exception);
            throw exception;
        }

        if (string.charAt(firstIndex) == '.' || string.charAt(lastIndex - 1) == '.') {
            JsonDecoderException exception = new JsonDecoderException(index);
            logger.throwing(className, "parseNumberWithSign", exception);
            throw exception;
        }

        JValue value;
        String substr = string.substring(firstIndex, lastIndex);
        value = JValue.newNumber(new BigDecimal(substr));

        logger.exiting(className, "parseNumberWithSign", value);
        return value;
    }

    private boolean isHexDigit(char c) {
        logger.entering(className, "isHexDigit", c);
        
        boolean result = Character.isDigit(c) || "abcdefABCDEF".contains(Character.toString(c));
        logger.exiting(className, "isHexDigit", result);
        return result;
    }

    private JValue parseHexNumberWithSign(int sign) throws JsonDecoderException {
        logger.entering(className, "parseHexNumberWithSign", sign);

        consumeChar('0');
        consumeChar('x');

        int firstIndex = index;
        int lastIndex = index;

        for (int i = firstIndex; i < length; i++) {
            char c = string.charAt(i);
            if (!isHexDigit(c)) {
                break;
            }

            consumeChar();
            lastIndex++;
        }

        if (firstIndex == lastIndex) {
            JsonDecoderException exception = new JsonDecoderException(index);
            logger.throwing(className, "parseHexNumberWithSign", exception);
            throw exception;
        }

        String substr = string.substring(firstIndex, lastIndex);
        JValue value = JValue.newNumber(new BigInteger(substr, 16));
        logger.exiting(className, "parseHexNumberWithSign", value);
        return value;
    }

    private JValue parseNumber() throws JsonDecoderException {
        logger.entering(className, "parseNumber");

        int firstIndex = index;
        int sign = 1;

        if (isNextMinus()) {
            sign = -1;
            consumeChar();
            firstIndex++;
        }

        JValue value;
        if (string.startsWith("0x", firstIndex)) {
            value = parseHexNumberWithSign(sign);
        } else {
            value = parseNumberWithSign(sign);
        }

        logger.exiting(className, "parseNumber", value);
        return value;
    }

    private JValue parseNull() throws JsonDecoderException {
        logger.entering(className, "parseNull");

        JValue returnValue;
        if (string.startsWith("null", index)) {
            index += "null".length();
            returnValue = JValue.newNull();
        } else {
            JsonDecoderException exception = new JsonDecoderException(index);
            logger.throwing(className, "parseNull", exception);
            throw exception;
        }

        logger.exiting(className, "parseNull", returnValue);
        return returnValue;
    }

    private JValue parseBoolean() throws JsonDecoderException {
        logger.entering(className, "parseBoolean");

        JValue returnValue;
        if (string.startsWith("true", index)) {
            index += "true".length();
            returnValue = JValue.newBoolean(true);
        } else if (string.startsWith("false", index)) {
            index += "false".length();
            returnValue = JValue.newBoolean(false);
        } else {
            JsonDecoderException exception = new JsonDecoderException(index);
            logger.throwing(className, "parseBoolean", exception);
            throw exception;
        }

        logger.exiting(className, "parseBoolean", returnValue);
        return returnValue;
    }

    private JValue parseString() throws JsonDecoderException {
        logger.entering(className, "parseString");

        StringBuilder builder = new StringBuilder();

        consumeChar();

        for (int i = index; i < length; i++) {
            if (isNextDoubleQuote()) {
                consumeChar();
                JValue returnValue = JValue.newString(builder.toString());
                logger.exiting(className, "parseString", returnValue);
                return returnValue;
            }

            if (isNextBackSlash()) {
                consumeChar();
                char c = getNextChar();
                switch (c) {
                    case 'b':
                        builder.append("\b");
                        break;
                    case 'f':
                        builder.append("\f");
                        break;
                    case 'n':
                        builder.append("\n");
                        break;
                    case 'r':
                        builder.append("\r");
                        break;
                    case 't':
                        builder.append("\t");
                        break;
                    case '"':
                        builder.append("\"");
                        break;
                    case '\\':
                        builder.append("\\");
                        break;
                    default:
                        builder.append(c);
                }
            } else {
                builder.append(getNextChar());
            }

            consumeChar();
        }

        JsonDecoderException exception = new JsonDecoderException(index);
        logger.throwing(className, "parseString", exception);
        throw exception;
    }

    private JValue parseArray() throws JsonDecoderException {
        logger.entering(className, "parseArray");

        consumeChar();
        skipSpaces();

        if (isNextArrayEnd()) {
            consumeChar();
            skipSpaces();
            JValue returnValue = JValue.newArray();
            logger.exiting(className, "parseArray", returnValue);
            return returnValue;
        }

        LinkedList<JValue> values = new LinkedList<JValue>();

        for (;;) {

            values.add(parsePartial());
            skipSpaces();

            if (isNextArrayEnd()) {
                consumeChar();
                skipSpaces();
                break;
            }

            if (!isNextComma()) {
                JsonDecoderException exception = new JsonDecoderException(index);
                logger.throwing(className, "parseArray", exception);
                throw exception;
            }

            consumeChar();
            skipSpaces();
        }

        JValue returnValue = JValue.newArray(values);
        logger.exiting(className, "parseArray", returnValue);
        return returnValue;
    }

    private JValue parseObject() throws JsonDecoderException {
        logger.entering(className, "parseObject");

        consumeChar();
        skipSpaces();

        HashMap<String, JValue> values = new HashMap<String, JValue>();

        while (!isNextObjectEnd()) {
            JValue keyValue = parseString();

            skipSpaces();

            if (!isNextColon()) {
                throw new JsonDecoderException(index);
            }

            consumeChar();

            skipSpaces();

            if (!keyValue.isString()) {
                JsonDecoderException exception = new JsonDecoderException(index);
                logger.throwing(className, "parseObject", exception);
                throw exception;
            }

            String key = keyValue.asString().getValue();

            values.put(key, parsePartial());

            skipSpaces();

            if (isNextObjectEnd()) {
                break;
            }

            if (!isNextComma()) {
                JsonDecoderException exception = new JsonDecoderException(index);
                logger.throwing(className, "parseObject", exception);
                throw exception;
            }

            consumeChar();
            skipSpaces();
        }

        consumeChar();

        JValue returnValue = JValue.newObject(values);
        logger.exiting(className, "parseObject", returnValue);
        return returnValue;
    }

    public JValue parsePartial() throws JsonDecoderException {
        logger.entering(className, "parsePartial");

        skipSpaces();

        JValue value;
        if (isNextNumber() || isNextMinus()) {
            value = parseNumber();
        } else if (isNextBoolean()) {
            value = parseBoolean();
        } else if (isNextDoubleQuote()) {
            value = parseString();
        } else if (isNextArrayBegin()) {
            value = parseArray();
        } else if (isNextObjectBegin()) {
            value = parseObject();
        } else if (isNextNull()) {
            value = parseNull();
        } else {
            JsonDecoderException exception = new JsonDecoderException(index);;
            logger.throwing(className, "parsePartial", exception);
            throw exception;
        }

        skipSpaces();

        logger.exiting(className, "parsePartial", value);
        return value;
    }

    public JValue parse() throws JsonDecoderException {
        logger.entering(className, "parse");

        skipSpaces();

        JValue value = parsePartial();

        skipSpaces();

        if (!finished()) {
            JsonDecoderException exception = new JsonDecoderException(index);;
            logger.throwing(className, "parse", exception);
            throw exception;
        }

        logger.exiting(className, "parse", value);
        return value;
    }

    public static JValue decode(String string) throws JsonDecoderException {
        logger.entering(className, "decode", string);
        
        JsonDecoder decoder = new JsonDecoder(string);
        JValue returnValue = decoder.parse();
        
        logger.exiting(className, "decode", returnValue);
        return returnValue;
    }
}
