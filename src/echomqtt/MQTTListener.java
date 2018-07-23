package echomqtt;

/**
 *
 * @author ymakino
 */
public interface MQTTListener {
    boolean arrived(String topic, String payload);
}
