package com.example;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.XMLWriter;
import org.dom4j.Node;

import java.io.StringWriter;
import java.util.List;

public class XMLProcessor {
    
    public static boolean isValidXml(String xml) {
        try {
            DocumentHelper.parseText(xml);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public static String formatXml(String xml) throws Exception {
        Document document = DocumentHelper.parseText(xml);
        OutputFormat format = OutputFormat.createPrettyPrint();
        format.setIndent(true);
        format.setIndentSize(2);
        format.setNewlines(true);
        
        StringWriter writer = new StringWriter();
        XMLWriter xmlWriter = new XMLWriter(writer, format);
        xmlWriter.write(document);
        
        return writer.toString();
    }

    public static String extractXmlElement(String xml, String xpath) {
        try {
            Document document = DocumentHelper.parseText(xml);
            List<Node> nodes = document.selectNodes(xpath);
            
            if (nodes.isEmpty()) {
                return "未找到匹配的元素";
            }
            
            StringBuilder result = new StringBuilder();
            for (Node node : nodes) {
                result.append(node.asXML()).append("\n");
            }
            
            return result.toString();
        } catch (Exception e) {
            return "处理过程中出错: " + e.getMessage();
        }
    }
} 