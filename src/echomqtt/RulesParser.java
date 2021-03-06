package echomqtt;

import echomqtt.converter.Converter;
import echomqtt.converter.ConverterException;
import echomqtt.converter.ConverterManager;
import echomqtt.converter.Raw;
import echomqtt.converter.Segment;
import echowand.common.EOJ;
import echowand.common.EPC;
import echowand.util.Pair;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

/**
 *
 * @author ymakino
 */
public class RulesParser {
    private static final Logger logger = Logger.getLogger(RulesParser.class.getName());
    private static final String className = RulesParser.class.getName();
    
    private ConverterManager converterManager = new ConverterManager();
    
    public Rules parseXMLFile(String xmlFile) throws IOException, ParserConfigurationException, SAXException {
        logger.entering(className, "parseXMLFile", xmlFile);
        
        List<String> lines = Files.readAllLines(Paths.get(xmlFile));
        
        StringBuilder builder = new StringBuilder();
        for (String line : lines) {
            builder.append(line);
        }
        
        Rules result = parseXMLString(builder.toString());
        logger.exiting(className, "parseXMLFile", result);
        return result;
    }
    
    public Rules parseXMLString(String xmlString) throws ParserConfigurationException, SAXException, IOException {
        logger.entering(className, "parseXMLString", xmlString);
        
        DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        Document document = builder.parse(new ByteArrayInputStream(xmlString.getBytes()));
        
        Rules result = parseDocument(document);
        logger.exiting(className, "parseXMLString", result);
        return result;
    }
    
    public Rules parseDocument(Document document) {
        logger.entering(className, "parseDocument", document);
        
        Node node = document.getChildNodes().item(0);
        
        if (!node.getNodeName().equals("rules")) {
            logger.exiting(className, "parseDocument", null);
            return null;
        }
        
        Rules result = parseRules(node);
        logger.exiting(className, "parseDocument", result);
        return result;
    }
    
    public Rules parseRules(Node rulesNode) {
        logger.entering(className, "parseRules", rulesNode);
        
        LinkedList<PublishRule> publishRules = new LinkedList<PublishRule>();
        LinkedList<SubscribeRule> subscribeRules = new LinkedList<SubscribeRule>();
        
        if (!rulesNode.getNodeName().equals("rules")) {
            logger.logp(Level.INFO, className, "parseRules", "invalid node: " + rulesNode.getNodeName());
            logger.exiting(className, "parseRules", null);
            return null;
        }
        
         NodeList nodeList = rulesNode.getChildNodes();
         for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                switch (node.getNodeName()) {
                    case "publish":
                        PublishRule publishRule = parsePublish(node);
                        if (publishRule == null) {
                            logger.logp(Level.INFO, className, "parseRules", "invalid node: " + node.getNodeName());
                            logger.exiting(className, "parseRules", null);
                            return null;
                        }
                        publishRules.add(publishRule);
                        break;
                    case "subscribe":
                        SubscribeRule subscribeRule = parseSubscribe(node);
                        if (subscribeRule == null) {
                            logger.logp(Level.INFO, className, "parseRules", "invalid node: " + node.getNodeName());
                            logger.exiting(className, "parseRules", null);
                            return null;
                        }
                        subscribeRules.add(subscribeRule); break;
                    default:
                        logger.logp(Level.INFO, className, "parseRules", "invalid node: " + node.getNodeName());
                        logger.exiting(className, "parseRules", null);
                        return null;
                }
            }
         }
         
         Rules result = new Rules(publishRules, subscribeRules);
        logger.exiting(className, "parseRules", result);
        return result;
    }
    
    public PublishRule parsePublish(Node publishNode) {
        logger.entering(className, "parsePublish", publishNode);
        
        String nodeName = null;
        EOJ eoj = null;
        HashMap<String, String> addition = new HashMap<String, String>();
        LinkedList<PropertyRule> propertyRules = new LinkedList<PropertyRule>();
        int interval = -1;
        int delay = 0;
        LinkedList<PublishTopic> publishTopics = new LinkedList<PublishTopic>();
        boolean getEnabled = true;
        boolean notifyEnabled = false;
        
        Node nodeGet = publishNode.getAttributes().getNamedItem("get");
        Node nodeNotify = publishNode.getAttributes().getNamedItem("notify");
        
        if (nodeGet != null) {
            switch (nodeGet.getTextContent().trim()) {
                case "enabled": getEnabled = true; break;
                case "disabled": getEnabled = false; break;
                default:
                    logger.logp(Level.INFO, className, "parsePublish", "invalid attribute: " + nodeGet);
                    logger.exiting(className, "parsePublish", null);
                    return null;
            }
        }
        
        if (nodeNotify != null) {
            switch (nodeNotify.getTextContent().trim()) {
                case "enabled": notifyEnabled = true; break;
                case "disabled": notifyEnabled = false; break;
                default:
                    logger.logp(Level.INFO, className, "parsePublish", "invalid attribute: " + nodeNotify);
                    logger.exiting(className, "parsePublish", null);
                    return null;
            }
        }
        
        NodeList nodeList = publishNode.getChildNodes();
        
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String content = node.getTextContent().trim();
                switch (node.getNodeName()) {
                    case "node": nodeName = content; break;
                    case "eoj": eoj = parseEOJ(content); break;
                    case "property": propertyRules.add(parseProperty(node)); break;
                    case "addition": addition = parseAddition(node); break;
                    case "interval": interval = Integer.parseInt(content); break;
                    case "delay": delay = Integer.parseInt(content); break;
                    case "topic": publishTopics.add(parseTopic(node)); break;
                    default:
                        logger.logp(Level.INFO, className, "parsePublish", "invalid node: " + node.getNodeName());
                        logger.exiting(className, "parsePublish", null);
                        return null;
                }
            }
        }
        
        if (interval == -1 && getEnabled) {
            logger.logp(Level.INFO, className, "parsePublish", "interval node is required for Get: " + publishNode);
            logger.exiting(className, "parsePublish", null);
            return null;
        }
        
        if (eoj == null || propertyRules.size() == 0 || publishTopics.size() == 0) {
            System.out.println("hoge");
            logger.logp(Level.INFO, className, "parsePublish", "invalid contents: " + publishNode);
            logger.exiting(className, "parsePublish", null);
            return null;
        }
        
        PublishRule result = new PublishRule(nodeName, eoj, interval, delay, publishTopics, propertyRules, addition, getEnabled, notifyEnabled);
        logger.exiting(className, "parsePublish", result);
        return result;
    }
    
    public EOJ parseEOJ(String eojStr) {
        if (eojStr.length() == 4) {
            eojStr = eojStr + "00";
        }
        
        return new EOJ(eojStr);
    }
    
    public SubscribeRule parseSubscribe(Node subscribeNode) {
        logger.entering(className, "parseSubscribe", subscribeNode);
        
        String nodeName = null;
        EOJ eoj = null;
        LinkedList<PropertyRule> propertyRules = new LinkedList<PropertyRule>();
        String topic = null;
        
        NodeList nodeList = subscribeNode.getChildNodes();
        
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String content = node.getTextContent().trim();
                switch (node.getNodeName()) {
                    case "node": nodeName = content; break;
                    case "eoj": eoj = parseEOJ(content); break;
                    case "property": propertyRules.add(parseProperty(node)); break;
                    case "topic": topic = content; break;
                    default: 
                        logger.logp(Level.INFO, className, "parseSubscribe", "invalid node: " + node.getNodeName());
                        logger.exiting(className, "parseSubscribe", null);
                        return null;
                }
            }
        }
        
        if (eoj == null || propertyRules.size() == 0 || topic == null) {
            logger.logp(Level.INFO, className, "parseSubscribe", "invalid contents: " + subscribeNode);
            logger.exiting(className, "parseSubscribe", null);
            return null;
        }
        
        SubscribeRule result = new SubscribeRule(nodeName, eoj, topic, propertyRules);
        logger.exiting(className, "parseSubscribe", result);
        return result;
    }
    
    public PublishTopic parseTopic(Node topicNode) {
        logger.entering(className, "parseTopic", topicNode);
        
        String topic;
        String node = null;
        int instance = -1;
        
        Node nodeNode = topicNode.getAttributes().getNamedItem("node");
        if (nodeNode != null) {
            node = nodeNode.getTextContent().trim();
        }
        
        Node instanceNode = topicNode.getAttributes().getNamedItem("instance");
        if (instanceNode != null) {
            instance = Integer.parseInt(instanceNode.getTextContent().trim());
        }
        
        topic = topicNode.getTextContent().trim();
        
        return new PublishTopic(topic, node, instance);
    }
    
    public HashMap<String, String> parseAddition(Node additionNode) {
        logger.entering(className, "parseAddition", additionNode);
        
        HashMap<String, String> params = new HashMap<String, String>();
        
        NodeList nodeList = additionNode.getChildNodes();
        
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                switch (node.getNodeName()) {
                    case "param": params.putAll(parseParam(node));break;
                    default: 
                        logger.logp(Level.INFO, className, "parseAddition", "invalid node: " + node.getNodeName());
                        logger.exiting(className, "parseAddition", null);
                        return null;
                }
            }
        }
        
        logger.exiting(className, "parseAddition", params);
        return params;
    }
    
    public PropertyRule parseProperty(Node propertyNode) {
        logger.entering(className, "parseProperty", propertyNode);
        
        EPC epc = null;
        String name = null;
        Converter converter = null;
        
        NodeList nodeList = propertyNode.getChildNodes();
        
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String content = node.getTextContent().trim();
                switch (node.getNodeName()) {
                    case "epc": epc = EPC.fromByte((byte)Integer.parseInt(content, 16)); break;
                    case "name": name = content; break;
                    case "array": converter = parseArray(node); break;
                    case "object": converter = parseObject(node); break;
                    case "segment": converter = parseSegment(node); break;
                    case "converter": converter = parseConverter(node); break;
                    default: 
                        logger.logp(Level.INFO, className, "parseProperty", "invalid node: " + node.getNodeName());
                        logger.exiting(className, "parseProperty", null);
                        return null;
                }
            }
        }
        
        if (converter == null) {
            converter = new Raw();
        }
        
        if (epc == null || name == null) {
            logger.logp(Level.INFO, className, "parseProperty", "invalid contents: " + propertyNode);
            logger.exiting(className, "parseProperty", null);
            return null;
        }
        
        PropertyRule result = new PropertyRule(epc, name, converter);
        logger.exiting(className, "parseProperty", result);
        return result;
    }
    
    public Converter parseSegment(Node segmentNode) {
        logger.entering(className, "parseSegment", segmentNode);
        
        Node offsetNode = segmentNode.getAttributes().getNamedItem("offset");
        
        if (offsetNode == null) {
            logger.logp(Level.INFO, className, "parseSegment", "no offset attribute: " + segmentNode);
            logger.exiting(className, "parseSegment", null);
            return null;
        }
        
        int offset;
        
        try {
            offset = Integer.parseInt(offsetNode.getTextContent());
        } catch (NumberFormatException ex) {
            logger.logp(Level.INFO, className, "parseSegment", "invalid offset attribute: " + offsetNode.getTextContent());
            logger.exiting(className, "parseSegment", null);
            return null;
        }
        
        Node sizeNode = segmentNode.getAttributes().getNamedItem("size");
        int size = -1;
        
        if (sizeNode != null) {
            try {
                size = Integer.parseInt(sizeNode.getTextContent());
            } catch (NumberFormatException ex) {
                logger.logp(Level.INFO, className, "parseSegment", "invalid size attribute: " + offsetNode.getTextContent());
                logger.exiting(className, "parseSegment", null);
                return null;
            }
        }
        
        NodeList nodeList = segmentNode.getChildNodes();
        Converter converter = null;
        
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                switch (node.getNodeName()) {
                    case "array": converter = parseArray(node); break;
                    case "object": converter = parseObject(node); break;
                    case "segment": converter = parseSegment(node); break;
                    case "converter": converter = parseConverter(node); break;
                    default: 
                        logger.logp(Level.INFO, className, "parseSegment", "invalid node: " + node.getNodeName());
                        logger.exiting(className, "parseSegment", null);
                        return null;
                }
            }
        }
        
        if (converter == null) {
            converter = new Raw();
        }
        
        Converter result = new Segment(offset, size, converter);
        
        logger.exiting(className, "parseSegment", result);
        return result;
    }
    
    public Converter parseArray(Node arrayNode) {
        logger.entering(className, "parseArray", arrayNode);
        
        Node minNode = arrayNode.getAttributes().getNamedItem("min");
        Node maxNode = arrayNode.getAttributes().getNamedItem("max");
        Node modeNode = arrayNode.getAttributes().getNamedItem("mode");
        
        if (minNode == null) {
            logger.logp(Level.INFO, className, "parseArray", "no min attribute: " + arrayNode);
            logger.exiting(className, "parseArray", null);
            return null;
        }
        
        if (maxNode == null) {
            logger.logp(Level.INFO, className, "parseArray", "no max attribute: " + arrayNode);
            logger.exiting(className, "parseArray", null);
            return null;
        }
        
        int min;
        int max;
        boolean objectMode = false;
        
        try {
            min = Integer.parseInt(minNode.getTextContent());
        } catch (NumberFormatException ex) {
            logger.logp(Level.INFO, className, "parseArray", "invalid min attribute: " + minNode.getTextContent(), ex);
            logger.exiting(className, "parseArray", null);
            return null;
        }
        
        try {
            max = Integer.parseInt(maxNode.getTextContent());
        } catch (NumberFormatException ex) {
            logger.logp(Level.INFO, className, "parseArray", "invalid max attribute: " + maxNode.getTextContent(), ex);
            logger.exiting(className, "parseArray", null);
            return null;
        }
        
        if (modeNode != null) {
            switch (modeNode.getTextContent().trim().toLowerCase()) {
                case "object": objectMode = true; break;
                case "array" : objectMode = false; break;
                default:
                    logger.logp(Level.INFO, className, "parseArray", "invalid mode: " + modeNode.getTextContent());
                    logger.exiting(className, "parseArray", null);
                    return null;
            }
        }
        
        Converter converter = null;
        
        NodeList nodeList = arrayNode.getChildNodes();
        
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                switch (node.getNodeName()) {
                    case "array": converter = parseArray(node); break;
                    case "object": converter = parseObject(node); break;
                    case "segment": converter = parseSegment(node); break;
                    case "converter": converter = parseConverter(node); break;
                    default: 
                        logger.logp(Level.INFO, className, "parseArray", "invalid node: " + node.getNodeName());
                        logger.exiting(className, "parseArray", null);
                        return null;
                }
            }
        }
        
        if (converter == null) {
            logger.logp(Level.INFO, className, "parseArray", "invalid contents: " + arrayNode);
            logger.exiting(className, "parseArray", null);
            return null;
        }
        
        Converter result;
        try {
            result = new echomqtt.converter.Array(converter, min, max, objectMode);
        } catch (ConverterException ex) {
            logger.logp(Level.INFO, className, "parseArray", "invalid converters: " + arrayNode, ex);
            logger.exiting(className, "parseArray", null);
            return null;
        }
        
        logger.exiting(className, "parseArray", result);
        return result;
    }
    
    public Converter parseObject(Node objectNode) {
        logger.entering(className, "parseObject", objectNode);
        
        LinkedList<Pair<String, Converter>> pairs = new LinkedList<Pair<String, Converter>>();
        
        NodeList nodeList = objectNode.getChildNodes();
        
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                switch (node.getNodeName()) {
                    case "value":
                        Pair<String, Converter> pair = parseValue(node);
                        if (pair == null) {
                            logger.logp(Level.INFO, className, "parseObject", "invalid value: " + node);
                            logger.exiting(className, "parseObject", null);
                            return null;
                        }
                        pairs.add(pair);
                        break;
                    default: 
                        logger.logp(Level.INFO, className, "parseObject", "invalid node: " + node.getNodeName());
                        logger.exiting(className, "parseObject", null);
                        return null;
                }
            }
        }
        
        if (pairs.isEmpty()) {
            logger.logp(Level.INFO, className, "parseObject", "invalid contents: " + objectNode);
            logger.exiting(className, "parseObject", null);
            return null;
        }
        
        Converter result;
        try {
            result = new echomqtt.converter.Object(pairs);
        } catch (ConverterException ex) {
            logger.logp(Level.INFO, className, "parseObject", "invalid converters: " + objectNode, ex);
            logger.exiting(className, "parseObject", null);
            return null;
        }
        
        logger.exiting(className, "parseArray", result);
        return result;
    }
    
    public Converter parseConverter(Node converterNode) {
        logger.entering(className, "parseConverter", converterNode);
        
        String className = null;
        HashMap<String, String> params = new HashMap<String, String>();
        
        NodeList nodeList = converterNode.getChildNodes();
        
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String content = node.getTextContent().trim();
                switch (node.getNodeName()) {
                    case "class": className = content; break;
                    case "param": params.putAll(parseParam(node));break;
                    default: 
                        logger.logp(Level.INFO, className, "parseConverter", "invalid node: " + node.getNodeName());
                        logger.exiting(className, "parseConverter", null);
                        return null;
                }
            }
        }
        
        if (className == null) {
            logger.logp(Level.INFO, className, "parseConverter", "invalid contents: " + converterNode);
            logger.exiting(className, "parseConverter", null);
            return null;
        }
        
        Converter result = converterManager.getConverter(className, params);
        logger.exiting(className, "parseConverter", result);
        return result;
    }
    
    public Pair<String, Converter> parseValue(Node valueNode) {
        logger.entering(className, "parseValue", valueNode);
        
        String key = null;
        
        Node nameNode = valueNode.getAttributes().getNamedItem("name");
        
        if (nameNode != null) {
            key = nameNode.getTextContent().trim();
        }
        
        Converter converter = null;
        NodeList nodeList = valueNode.getChildNodes();
        
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                switch (node.getNodeName()) {
                    case "array": converter = parseArray(node); break;
                    case "object": converter = parseObject(node); break;
                    case "segment": converter = parseSegment(node); break;
                    case "converter": converter = parseConverter(node); break;
                    default: 
                        logger.logp(Level.INFO, className, "parseValue", "invalid node: " + node.getNodeName());
                        logger.exiting(className, "parseValue", null);
                        return null;
                }
            }
        }
        
        if (key == null) {
            logger.logp(Level.INFO, className, "parseValue", "invalid contents: " + valueNode);
            logger.exiting(className, "parseValue", null);
            return null;
        }
        
        if (converter == null) {
            converter = new Raw();
        }
        
        Pair<String, Converter> pair = new Pair<String, Converter>(key, converter);
        logger.exiting(className, "parseValue", pair);
        return pair;
    }
    
    public HashMap<String, String> parseParam(Node paramNode) {
        logger.entering(className, "parseParam", paramNode);
        
        String key = null;
        String value = null;
        
        Node nameNode = paramNode.getAttributes().getNamedItem("name");
        
        if (nameNode != null) {
            key = nameNode.getTextContent().trim();
        }
        
        value = paramNode.getTextContent();
        
        if (key == null || value == null) {
            logger.logp(Level.INFO, className, "parseParam", "invalid contents: " + paramNode);
            logger.exiting(className, "parseParam", null);
            return null;
        }
        
        HashMap<String, String> param = new HashMap<String, String>();
        param.put(key, value);
        logger.exiting(className, "parseParam", param);
        return param;
    }
}
