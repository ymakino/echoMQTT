package echomqtt;

/**
 *
 * @author ymakino
 */
public class PublishTopic {
    private String topic;
    private String node;
    private int instance;
    
    public PublishTopic(String topic, String node, int instance) {
        this.topic = topic;
        this.node = node;
        this.instance = instance;
    }
    
    public PublishTopic(String topic, String node) {
        this.topic = topic;
        this.node = node;
        this.instance = -1;
    }
    
    public PublishTopic(String topic, int instance) {
        this.topic = topic;
        this.node = null;
        this.instance = instance;
    }
    
    public PublishTopic(String topic) {
        this.topic = topic;
        this.node = null;
        this.instance = -1;
    }
    
    public String getTopic() {
        return topic;
    }
    
    public String getNode() {
        return node;
    }
    
    public int getInstance() {
        return instance;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        
        builder.append("PublishTopic {");
        
        if (node != null) {
            builder.append("node: ").append(node).append(", ");
        }
        
        if (instance > 0) {
            builder.append("instance: ").append(instance).append(", ");
        }
        
        builder.append("topic: ").append(topic);
        
        return builder.toString();
    }
}
