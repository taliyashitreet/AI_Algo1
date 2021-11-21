import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.io.File;  // Import the File class
import java.io.FileNotFoundException;  // Import this class to handle errors
import java.util.Scanner; // Import the Scanner class to read text files


// This class for pars the xml file to baysien network- the net will be held by HashMap
public class MainBeysNet {
    static HashMap<String, BaysNode> V = new HashMap<>(); //collection of nodes - The Net
    static String[] array = new String[0]; // will contain the input data

    public static void readFromFile(String fileName) { //enter data to the "data array"
        String data = "";
        try {
            File myObj = new File(fileName);
            Scanner myReader = new Scanner(myObj);
            int index = 0;
            while (myReader.hasNextLine()) {
                array = Arrays.copyOf(array, array.length + 1);
                data = myReader.nextLine();
                array[index++] = data;
            }
            myReader.close();
        } catch (FileNotFoundException e) {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        System.out.println(Arrays.toString(array));
    }

    public static void main(String[] args) {
        readFromFile("input.txt");
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newDefaultInstance();
        try {
            DocumentBuilder db = dbf.newDocumentBuilder();
            Document doc = db.parse(array[0]); // the name of the xml file
            NodeList listVAR = doc.getElementsByTagName("VARIABLE");
            NodeList listDEF = doc.getElementsByTagName("DEFINITION");
            for (int i = 0; i < listVAR.getLength(); i++) {
                Node node = listVAR.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String variableName = element.getElementsByTagName("NAME").item(0).getTextContent(); //the variable name
                    int length = element.getElementsByTagName("OUTCOME").getLength(); //the values of variable
                    BaysNode b = new BaysNode(variableName, length);
                    V.put(variableName, b); // enter the variable to our BeysNet
                    for (int j = 0; j < length; j++) {
                        String value = element.getElementsByTagName("OUTCOME").item(j).getTextContent();
                        b.addToValues(value);
                    }
                }
            }
            for (int i = 0; i < listDEF.getLength(); i++) {
                Node node = listDEF.item(i);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element elementD = (Element) node;
                    String variable = elementD.getElementsByTagName("FOR").item(0).getTextContent();
                    int length = elementD.getElementsByTagName("GIVEN").getLength();
                    for (int j = 0; j < length; j++) {
                        String perants = elementD.getElementsByTagName("GIVEN").item(j).getTextContent();
                        V.get(variable).addToParents(V.get(perants));
                        V.get(perants).addToChildren(V.get(variable));
                    }
                    String CPT = elementD.getElementsByTagName("TABLE").item(0).getTextContent();
                    V.get(variable).createCPT(CPT);

                }

            }

//            for (String variable : V.keySet()) {
//                System.out.println(V.get(variable).toString());
            //}


        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SAXException e) {
            e.printStackTrace();
        }
        //String ans = BaysBall.BaysSearch("L-B|T'=T", V);


        String a="P(B=T|J=T,M=T)A-E";
        VariableElimination VE= new VariableElimination(V,a);
        //System.out.println( Arrays.toString(VE.getEvidence()));
        //System.out.println( Arrays.toString(VE.getHidden()));
        //System.out.println(VE.getQuery());
        Searches s= new Searches(V);
        String  evidence="M";
        String hid= "ALK";
        System.out.println(hid.substring(1,2));
        String[] evi={"M","J"};

        //VE.FactorsToString();
        VE.reduction();
        VE.FactorsToString();


//        for (String s : a) {
//            for (String t : b) {
//                for (String c : e) {
//                    System.out.println(s + t + c);
//                }
//            }
//        }
    }
}
