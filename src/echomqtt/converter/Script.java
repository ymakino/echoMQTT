package echomqtt.converter;

import echomqtt.json.JArray;
import echomqtt.json.JValue;
import echomqtt.json.JsonDecoderException;
import echomqtt.json.JsonEncoderException;
import echowand.common.Data;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.script.Bindings;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

/**
 *
 * @author ymakino
 */
public class Script extends Converter {
    private static final Logger logger = Logger.getLogger(Script.class.getName());
    private static final String className = Script.class.getName();
    
    private int size = -1;
    
    private String scriptString = null;
    private String scriptFile = null;
    private ScriptEngineManager manager;
    private ScriptEngine engine;
    private Bindings bindings;
    
    public Script(HashMap<String, String> params) throws ConverterException {
        logger.entering(className, "Command", params);
        
        manager = new ScriptEngineManager();
        engine = manager.getEngineByName("javascript");
        bindings = engine.createBindings();
        
        for (String key : params.keySet()) {
            String value = params.get(key);
            
            switch (key) {
                case "size":
                    size = java.lang.Integer.parseInt(value);
                    break;
                case "script":
                    scriptString = value;
                    break;
                case "file":
                    scriptFile = value;
                    break;
                default:
                    ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                    logger.throwing(className, "Command", exception);
                    throw exception;
            }
        }
        
        logger.exiting(className, "Command");
    }
    
    public String getScript() {
        return scriptString;
    }
    
    @Override
    public int getSize() {
        return size;
    }
    
    @Override
    public JValue convert(Data data) throws ConverterException {
        logger.entering(className, "convert", data);
        
        JValue[] jvalues = new JValue[data.size()];
        for (int i=0; i<jvalues.length; i++) {
            jvalues[i] = JValue.newNumber(0x00ff & data.get(i));
        }
        
        try {
            
            String json = JValue.newArray(jvalues).toJSON();
            engine.eval("var value = " + json + ";", bindings);
            bindings.put("inputType", "Data");
            bindings.put("outputType", "JSON");
            
            if (scriptString != null) {
                engine.eval(scriptString, bindings);
            } else {
                FileReader reader = new FileReader(scriptFile);
                engine.eval(reader, bindings);
            }
            
            engine.eval("result = JSON.stringify(result);", bindings);
            
            JValue result = JValue.parseJSON((String)bindings.get("result"));
            
            logger.exiting(className, "convert", result);
            return result;
        } catch (ScriptException ex) {
            ConverterException exception = new ConverterException("cannot convert: " + data, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        } catch (JsonDecoderException ex) {
            ConverterException exception = new ConverterException("invalid JSON format", ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        } catch (FileNotFoundException ex) {
            ConverterException exception = new ConverterException("no such file: " + scriptFile, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        } catch (JsonEncoderException ex) {
            ConverterException exception = new ConverterException("cannot convert: " + data, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
    }

    @Override
    public Data convert(JValue jvalue) throws ConverterException {
        logger.entering(className, "convert", jvalue);
        
        try {
            engine.eval("var value = " + jvalue.toJSON(), bindings);

            if (scriptString != null) {
                engine.eval(scriptString, bindings);
            } else {
                FileReader reader = new FileReader(scriptFile);
                engine.eval(reader, bindings);
            }
            
            engine.eval("result = JSON.stringify(result);", bindings);
            
            JValue resultValue = JValue.parseJSON((String)bindings.get("result"));
            
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
        } catch (ScriptException ex) {
            ConverterException exception = new ConverterException("cannot convert: " + jvalue, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        } catch (FileNotFoundException ex) {
            ConverterException exception = new ConverterException("no such file: " + scriptFile, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        } catch (JsonEncoderException ex) {
            ConverterException exception = new ConverterException("cannot convert: " + jvalue, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        } catch (JsonDecoderException ex) {
            ConverterException exception = new ConverterException("cannot convert: " + jvalue, ex);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Script");
        builder.append("{");
        builder.append("script: ").append(scriptString);
        builder.append("}");
        
        return builder.toString();
    }
    
    public static void main(String[] args) throws ConverterException, JsonDecoderException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("script", "var result=value");
        Converter c1 = new Script(map);
        System.out.println(c1);
        
        Data d = new Data((byte)16);
        System.out.println(d);
        System.out.println(c1.convert(JValue.parseJSON("[1,2,3]")));
    }
}
