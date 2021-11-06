import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;

public class BeysNet {
    public static void main(String[] args) {
        DocumentBuilderFactory dbf= DocumentBuilderFactory.newDefaultInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc= db.parse("alarm_net.xml");
            NodeList listVAR=doc.getElementsByTagName("VARIABLE");
            NodeList listDEF=doc.getElementsByTagName("DEFINITION");
            for (int i = 0; i < listVAR.getLength(); i++) {
                BaysNode node= (BaysNode) listVAR.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String variableName = element.getElementsByTagName("NAME").item(0).getTextContent();
                    NodeList values = element.getElementsByTagName("OUTCOME");

                }
            }
            for (int i = 0; i < listDEF.getLength(); i++) {
                Node n= listDEF.item(i);
                if (n.getNodeType() == Node.ELEMENT_NODE) {
                    Element elementD = (Element) n;
                    String variable = elementD.getElementsByTagName("FOR").item(0).getTextContent();
                    String perants = elementD.getElementsByTagName("GIVEN").item(0).getTextContent();
                    String CPT = elementD.getElementsByTagName("TABLE").item(0).getTextContent();
                }
            }
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
    }
}
