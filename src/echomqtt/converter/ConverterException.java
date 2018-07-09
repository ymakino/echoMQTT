package echomqtt.converter;

/**
 *
 * @author ymakino
 */
public class ConverterException extends Exception {
    
    /**
     * 
     * @param message 例外に関する情報
     */
    public ConverterException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message 例外に関する情報
     * @param cause この例外の発生原因となった例外
     */
    public ConverterException(String message, Exception cause) {
        super(message, cause);
    }
}
