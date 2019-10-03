package echomqtt.converter;

import echomqtt.json.JArray;
import echomqtt.json.JValue;
import echomqtt.json.JsonDecoderException;
import echomqtt.json.JsonEncoderException;
import echowand.common.Data;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class Command extends Converter {
    private static final Logger logger = Logger.getLogger(Command.class.getName());
    private static final String className = Command.class.getName();
    
    private int size = -1;
    private String file = null;
    private String[] args = new String[0];
    
    private String[] parseStringArray(String str) throws ConverterException {
        
        try {
            String[] strings;
            JValue jvalue = JValue.parseJSON(str);

            if (!jvalue.isArray()) {
                jvalue = JValue.newArray(new JValue[]{jvalue});
            }
            
            JArray jarray = jvalue.asArray();
            strings = new String[jarray.size()];
            for (int i=0; i<jarray.size(); i++) {
                if (jarray.get(i).isNull()) {
                    strings[i] = null;
                } else {
                    strings[i] = jarray.get(i).asString().getValue();
                }
            }

            return strings;
        
        } catch (JsonDecoderException ex) {
            ConverterException exception = new ConverterException("decode error: " + str, ex);
            logger.throwing(className, "parseStringArray", exception);
            throw exception;
        }
    }
    
    public Command(HashMap<String, String> params) throws ConverterException {
        logger.entering(className, "Command", params);
        
        for (String key : params.keySet()) {
            String value = params.get(key);
            
            switch (key) {
                case "size":
                    size = java.lang.Integer.parseInt(value);
                    break;
                case "file":
                    file = value;
                    break;
                case "args":
                    args = parseStringArray(value);
                    break;
                default:
                    ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                    logger.throwing(className, "Command", exception);
                    throw exception;
            }
        }
        
        logger.exiting(className, "Command");
    }
    
    public String getFile() {
        return file;
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public JValue convert(Data data) throws ConverterException {
        logger.entering(className, "convert", data);
        
        try {
            String[] command = new String[1 + args.length];
            command[0] = file;
            System.arraycopy(args, 0, command, 1, args.length);
            
            for (int i=1; i<command.length; i++) {
                if (command[i] == null) {
                    command[i] = data.toString();
                }
            }
            
            Process child = Runtime.getRuntime().exec(command);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(child.getOutputStream()));
            try {
                writer.write(data.toString());
                writer.close();
            } catch (IOException ex) {
                logger.logp(Level.INFO, className, "convert", "cannot write", ex);
            }
            
            child.waitFor();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(child.getInputStream()));
            String jsonStr = reader.lines().reduce("", (n1, n2) -> n1 + n2);
            
            JValue result;
            
            try {
                result = JValue.parseJSON(jsonStr);
            } catch (JsonDecoderException ex) {
                ConverterException exception = new ConverterException("invalid JSON: " + jsonStr, ex);
                logger.throwing(className, "convert", exception);
                throw exception;
            }
            
            logger.exiting(className, "convert", result);
            return result;
        } catch (IOException ex) {
            ConverterException exception = new ConverterException("cannot convert: " + file + " " + data, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        } catch (InterruptedException ex) {
            ConverterException exception = new ConverterException("cannot convert: " + file + " " + data, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
    }

    @Override
    public Data convert(JValue jvalue) throws ConverterException {
        logger.entering(className, "convert", jvalue);
        
        try {
            String[] command = new String[1 + args.length];
            command[0] = file;
            System.arraycopy(args, 0, command, 1, args.length);
            
            for (int i=1; i<command.length; i++) {
                if (command[i] == null) {
                    command[i] = jvalue.toJSON();
                }
            }
            
            Process child = Runtime.getRuntime().exec(command);
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(child.getOutputStream()));
            try {
                writer.write(jvalue.toJSON());
                writer.close();
            } catch (IOException ex) {
                logger.logp(Level.INFO, className, "convert", "cannot write", ex);
            }
            
            child.waitFor();
            
            BufferedReader reader = new BufferedReader(new InputStreamReader(child.getInputStream()));
            String jsonStr = reader.lines().reduce("", (n1, n2) -> n1 + n2);
            JValue resultValue = JValue.parseJSON(jsonStr);
            
            if (!resultValue.isArray()) {
                ConverterException exception = new ConverterException("invalid array: " + resultValue.toJSON());
                logger.throwing(className, "convert", exception);
                throw exception;
            }
            
            JArray jarray = resultValue.asArray();
            byte[] bytes = new byte[jarray.size()];
            
            for (int i=0; i<jarray.size(); i++) {
                JValue jv = jarray.get(i);
                
                if (!jv.isNumber()) {
                    ConverterException exception = new ConverterException("invalid number: " + jv.toJSON());
                    logger.throwing(className, "convert", exception);
                    throw exception;
                }
                
                bytes[i] = jv.asNumber().getValue().byteValue();
            }
            
            Data result = new Data(bytes);
            
            logger.exiting(className, "convert", result);
            return result;
        } catch (IOException ex) {
            ConverterException exception = new ConverterException("cannot convert: " + file + " " + jvalue, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        } catch (InterruptedException ex) {
            ConverterException exception = new ConverterException("cannot convert: " + file + " " + jvalue, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        } catch (JsonEncoderException ex) {
            ConverterException exception = new ConverterException("cannot convert: " + file + " " + jvalue, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        } catch (JsonDecoderException ex) {
            ConverterException exception = new ConverterException("cannot convert: " + file + " " + jvalue, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Command");
        builder.append("{");
        builder.append("file: ").append(file).append(", ");
        builder.append("args: [");
        for (int i=0; i<args.length; i++) {
            if (i != 0) {
                builder.append(", ");
            }
            builder.append(args[i]);
        }
        builder.append("]");
        builder.append("}");
        
        return builder.toString();
    }
    
    public static void main(String[] args) throws ConverterException, JsonDecoderException, JsonEncoderException {
        HashMap<String, String> map1 = new HashMap<String, String>();
        map1.put("file", "python");
        map1.put("args", "[\"-c\", \"import sys; print(\\\"%.01f\\\" % (int(sys.argv[1], 16)/10.0))\", null]");
        Converter c1 = new Command(map1);
        System.out.println(c1);
        
        Data d = new Data((byte)0x12, (byte)0x34);
        System.out.println("Input: " + d + " (" + 0x1234 + ")");
        System.out.println("Output: " + c1.convert(d));
        
        HashMap<String, String> map2 = new HashMap<String, String>();
        map2.put("file", "python");
        map2.put("args", "[\"-c\", \"import sys; print(sys.argv[1])\", null]");
        
        Converter c2 = new Command(map2);
        System.out.println(c2);
        JValue j = JValue.newArray(new JValue[]{JValue.newNumber(0x12), JValue.newNumber(0x34)});
        System.out.println("Input: " + j);
        System.out.println("Output: " + c2.convert(j));
    }
}
