package echomqtt.converter;

import echowand.common.Data;

/**
 *
 * @author ymakino
 */
public interface Converter {
    public String convertData(Data data) throws ConverterException;
    public Data convertString(String str) throws ConverterException;
}
