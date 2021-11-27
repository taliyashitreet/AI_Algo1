import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Scanner; // Import the Scanner class to read text files


// This class for pars the xml file to baysien network- the net will be held by HashMap
public class Ex1 {
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

    }

    /**
     * In the main function we will read the xml and txt(input) files
     * during we will create the network that will be saved in a Hashmap
     * called V -  after creating the network we can
     * create an output file with all the answers to queries read from the input file
     * @param args
     * @throws IOException
     */

    public static void main(String[] args) throws IOException {

        readFromFile("input.txt"); //the path of the input txt
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



        } catch (ParserConfigurationException e) {
            //e.printStackTrace();
        } catch (IOException e) {
            //e.printStackTrace();
        } catch (SAXException e) {
            //e.printStackTrace();
        }


        Searches s= new Searches(V);



        FileWriter writer = new FileWriter("output.txt");
        for (int i = 1; i < array.length; i++) {
            if(!array[i].contains("P(")){
                String ans=s.BaysSearch(array[i]);
                writer.write(ans+"\n");
            }
            else {
                VariableElimination VE= new VariableElimination(V,array[i]);
                String ans= VE.EliminationProcess();
                writer.write(ans+"\n");
            }
        }
        writer.close();



    }
}
