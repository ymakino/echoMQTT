package echomqtt;

import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

/**
 *
 * @author ymakino
 */
public class Rules {
    private LinkedList<PublishRule> publishRules;
    private LinkedList<SubscribeRule> subscribeRules;
    
    public Rules(Collection<PublishRule> publishRules, Collection<SubscribeRule> subscribeRules) {
        this.publishRules = new LinkedList<PublishRule>(publishRules);
        this.subscribeRules = new LinkedList<SubscribeRule>(subscribeRules);
    }
    
    public int countPublishRules() {
        return publishRules.size();
    }
    
    public PublishRule getPublishRuleAt(int index) {
        return publishRules.get(index);
    }
    
    public List<PublishRule> getPublishRules() {
        return new ArrayList<PublishRule>(publishRules);
    }
    
    public int countSubscribeRules() {
        return subscribeRules.size();
    }
    
    public SubscribeRule getSubscribeRuleAt(int index) {
        return subscribeRules.get(index);
    }
    
    public List<SubscribeRule> getSubscribeRules() {
        return new ArrayList<SubscribeRule>(subscribeRules);
    }
}
