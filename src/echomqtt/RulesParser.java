package echomqtt;

import echomqtt.converter.Converter;
import echomqtt.converter.ConverterManager;
import echowand.common.EOJ;
import echowand.common.EPC;
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
        
        String address = null;
        EOJ eoj = null;
        LinkedList<PropertyRule> propertyRules = new LinkedList<PropertyRule>();
        int interval = -1;
        String topic = null;
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
                    case "address": address = content; break;
                    case "eoj": eoj = new EOJ(content); break;
                    case "property": propertyRules.add(parseProperty(node)); break;
                    case "interval": interval = Integer.parseInt(content); break;
                    case "topic": topic = content; break;
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
        
        if (address == null || eoj == null || propertyRules.size() == 0 || topic == null) {
            logger.logp(Level.INFO, className, "parsePublish", "invalid contents: " + publishNode);
            logger.exiting(className, "parsePublish", null);
            return null;
        }
        
        PublishRule result = new PublishRule(address, eoj, interval, topic, propertyRules, getEnabled, notifyEnabled);
        logger.exiting(className, "parsePublish", result);
        return result;
    }
    
    public SubscribeRule parseSubscribe(Node subscribeNode) {
        logger.entering(className, "parseSubscribe", subscribeNode);
        
        String address = null;
        EOJ eoj = null;
        LinkedList<PropertyRule> propertyRules = new LinkedList<PropertyRule>();
        String topic = null;
        
        NodeList nodeList = subscribeNode.getChildNodes();
        
        for (int i=0; i<nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            if (node.getNodeType() == Node.ELEMENT_NODE) {
                String content = node.getTextContent().trim();
                switch (node.getNodeName()) {
                    case "address": address = content; break;
                    case "eoj": eoj = new EOJ(content); break;
                    case "property": propertyRules.add(parseProperty(node)); break;
                    case "topic": topic = content; break;
                    default: 
                        logger.logp(Level.INFO, className, "parseSubscribe", "invalid node: " + node.getNodeName());
                        logger.exiting(className, "parseSubscribe", null);
                        return null;
                }
            }
        }
        
        if (address == null || eoj == null || propertyRules.size() == 0 || topic == null) {
            logger.logp(Level.INFO, className, "parseSubscribe", "invalid contents: " + subscribeNode);
            logger.exiting(className, "parseSubscribe", null);
            return null;
        }
        
        SubscribeRule result = new SubscribeRule(address, eoj, topic, propertyRules);
        logger.exiting(className, "parseSubscribe", result);
        return result;
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
                    case "converter": converter = parseConverter(node); break;
                    default: 
                        logger.logp(Level.INFO, className, "parseProperty", "invalid node: " + node.getNodeName());
                        logger.exiting(className, "parseProperty", null);
                        return null;
                }
            }
        }
        
        if (epc == null || name == null || converter == null) {
            logger.logp(Level.INFO, className, "parseProperty", "invalid contents: " + propertyNode);
            logger.exiting(className, "parseProperty", null);
            return null;
        }
        
        PropertyRule result = new PropertyRule(epc, name, converter);
        logger.exiting(className, "parseProperty", result);
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
