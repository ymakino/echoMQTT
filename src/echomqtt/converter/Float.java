package echomqtt.converter;

import echomqtt.json.JValue;
import echomqtt.json.JsonDecoderException;
import echowand.common.Data;
import java.math.BigDecimal;
import java.math.BigInteger;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 *
 * @author ymakino
 */
public class Float extends Converter {
    private static final Logger logger = Logger.getLogger(Float.class.getName());
    private static final String className = Float.class.getName();
    
    private boolean isSigned = true;
    private int size = -1;
    private double divide = 10;
    
    public Float(HashMap<String, String> params) throws ConverterException {
        logger.entering(className, "Float", params);
        
        for (String key : params.keySet()) {
            String value = params.get(key);
            
            switch (key) {
                case "size":
                    size = java.lang.Integer.parseInt(value);
                    break;
                case "divide":
                    divide = java.lang.Double.parseDouble(value);
                    break;
                case "signed":
                    if (value.toLowerCase().equals("true")) {
                        isSigned = true;
                    } else if (value.toLowerCase().equals("false")) {
                        isSigned = false;
                    } else {
                        ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                        logger.throwing(className, "Integer", exception);
                        throw exception;
                    }
                    break;
                case "unsigned":
                    if (value.toLowerCase().equals("true")) {
                        isSigned = false;
                    } else if (value.toLowerCase().equals("false")) {
                        isSigned = true;
                    } else {
                        ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                        logger.throwing(className, "Integer", exception);
                        throw exception;
                    }
                    break;
                default:
                    ConverterException exception = new ConverterException("invalid parameter: " + key + " = " + value);
                    logger.throwing(className, "Float", exception);
                    throw exception;
            }
        }
        
        logger.exiting(className, "Float");
    }
    
    protected static BigInteger u(Data data) {
        return new BigInteger(data.toString(), 16);
    }
    
    protected static BigInteger s(Data data) {
        
        boolean isNegative = ((data.get(0) & 0x80) != 0);
        
        String str;
        
        if (!isNegative) {
            str = data.toString();
        } else {
            byte[] bytes = new byte[data.size()];
            for (int i=0; i<data.size(); i++) {
                bytes[i] = (byte)(0x00ff & ((data.get(i) ^ (byte)0xff)));
            }
            str = new Data(bytes).toString();
        }
        
        BigInteger num = new BigInteger(str, 16);
        
        if (isNegative) {
            num = num.add(BigInteger.ONE).negate();
        }

        return num;
    }
    
    public boolean isSigned() {
        return isSigned;
    }
    
    public int getSize() {
        return size;
    }
    
    public double getDivide() {
        return divide;
    }
    
    @Override
    public JValue convert(Data data) {
        logger.entering(className, "convert", data);
        
        BigDecimal num;
        
        if (isSigned) {
            num = new BigDecimal(Integer.s(pack(data))).divide(new BigDecimal(divide));
        } else {
            num = new BigDecimal(Integer.u(pack(data))).divide(new BigDecimal(divide));
        }
        
        JValue result = JValue.newNumber(num);
        logger.exiting(className, "convert", result);
        return result;
    }

    @Override
    public Data convert(JValue jvalue) throws ConverterException {
        logger.entering(className, "convert", jvalue);
        
        BigDecimal value;
        
        if (jvalue.isNumber()) {
            value = jvalue.asNumber().getValue();
        } else if (jvalue.isString()) {
            value = new BigDecimal(jvalue.asString().getValue());
        } else {
            ConverterException exception = new ConverterException("invalid value: " + jvalue);
            logger.throwing(className, "convert", exception);
            throw exception;
        }
        
        BigInteger num = value.multiply(BigDecimal.valueOf(divide)).toBigInteger();
        
        byte[] buf = new byte[size];
        
        for (int i=0; i<size; i++) {
            buf[size - i - 1] = (byte)(num.shiftRight(8 * i).intValue() & 0xff);
        }
        
        if (isSigned && value.signum() == 1) {
            buf [0] &= 0x7f;
        }
        
        Data result = new Data(buf);
        logger.exiting(className, "convert", result);
        return result;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("Float");
        builder.append("{");
        builder.append("size: ").append(size).append(", ");
        builder.append("divide: ").append(divide);
        builder.append("}");
        
        return builder.toString();
    }
    
    public static void main(String[] args) throws ConverterException, JsonDecoderException {
        HashMap<String, String> map = new HashMap<String, String>();
        map.put("size", "2");
        map.put("divide", "10");
        map.put("signed", "true");
        Converter c1 = new Float(map);
        
        Data d = c1.convert(JValue.parseJSON("1024.6"));
        System.out.println(d);
        System.out.println(c1.convert(d));
        System.out.println(c1.convert(JValue.parseJSON("1024.6")));
        
        Data d1 = c1.convert(JValue.parseJSON("6553.5"));
        System.out.println(d1);
        System.out.println(c1.convert(d1));
        System.out.println(c1.convert(JValue.parseJSON("6553.5")));
    }
}
