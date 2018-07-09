package echomqtt;

/**
 *
 * @author ymakino
 */
public class PublisherException extends Exception {
    
    /**
     * 
     * @param message 例外に関する情報
     */
    public PublisherException(String message) {
        super(message);
    }
    
    /**
     * 
     * @param message 例外に関する情報
     * @param cause この例外の発生原因となった例外
     */
    public PublisherException(String message, Exception cause) {
        super(message, cause);
    }
}
