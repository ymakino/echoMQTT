package echomqtt;

/**
 *
 * @author ymakino
 */
public interface MQTTListener {
    void arrived(String topic, String payload);
}
