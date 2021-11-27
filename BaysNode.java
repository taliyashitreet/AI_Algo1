import org.w3c.dom.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;

public class BaysNode implements Node {
    private String Name;
    private String[] values; // True , False
    private int index = 0;
    private ArrayList<BaysNode> parents;
    private ArrayList<BaysNode> children;
    private String color;
    private LinkedHashMap<String, Double> cpt;

    //Getters
    public String getName() {
        return Name;
    }

    public String[] getValues() {
        return values;
    }

    public int getIndex() {
        return index;
    }

    public ArrayList<BaysNode> getParents() {
        return this.parents;
    }

    public ArrayList<BaysNode> getChildren() {
        return children;
    }

    public String getColor() {
        return this.color;
    }

    public void addToChildren(Node child) {
        children.add((BaysNode) child);
    }

    public LinkedHashMap<String, Double> getCpt() {
       return this.cpt;
    }

    public BaysNode(String name, int length) {
        this.values = new String[length];
        this.Name = name;
        this.parents = new ArrayList<>();
        this.children = new ArrayList<>();
        this.color = "white";
        this.cpt = new LinkedHashMap<String, Double>();
    }
    public BaysNode(BaysNode other){
        this.values = new String[other.values.length];
        this.Name = other.getName();
        this.parents = new ArrayList<BaysNode>();
        for(BaysNode p: other.getParents()){
            this.addToParents(p);
        }
        this.children = new ArrayList<BaysNode>();
        for(BaysNode c: other.getChildren()){
            this.addToChildren(c);
        }
        this.color = "white";
        this.cpt = new LinkedHashMap<String, Double>();
        for(String key :other.getCpt().keySet()){
             this.cpt.put(key,other.getCpt().get(key));
        }
        this.values=new String[other.getValues().length];
        for (int i = 0; i < values.length; i++) {
           this.values[i]=other.getValues()[i];
        }

    }


    public void createCPT(String table) {
        String[] toArr = table.split(" ");
        int i=0;
        while (i<=toArr.length) {
            if(parents.size()>0) {
                Enter(i, 0, this.parents, "", toArr);
                break;
            }
            for (String val : this.values) {
                cpt.put(val, Double.parseDouble(toArr[i++]));
            }
            break;
        }

    }


    private int Enter(int i, int n, ArrayList<BaysNode> parents, String ans,String[] toArr) {
        if (n == parents.size()-1) {
            for (String val1 : parents.get(n).getValues()) {
                for (String val : this.values) {
                    cpt.put(ans + val1+ "-" + val, Double.parseDouble(toArr[i++]));
                }
            }
            return i;
        }
        for (String val1 : parents.get(n).getValues()) {
            ans =(n>0)?ans+ val1+"-":val1+"-";
            i=Enter(i, n + 1, parents, ans, toArr);
        }
    return i;
    }

    public void SetColor(String color) {
        this.color = color;
    }

    public void addToValues(String val) {
        this.values[index++] = val;
    }

    public void addToParents(Node n) {
        parents.add((BaysNode) n);
    }

    //Auxiliary functions for ToString
    public String getP() {
        String str = "";
        for (Node n : parents) {
            str += n.getNodeName() + ",";
        }
        return (str.length() > 0) ? str.substring(0, (str.length() - 1)) : str;
    }

    public String getC() {
        String str = "";
        if (this.children != null) {
            for (Node n : children) {
                str += n.getNodeName() + ",";
            }
        }
        return (str.length() > 0) ? str.substring(0, (str.length() - 1)) : str;
    }

    //all the Node's methods because of the implementation:
    @Override
    public String toString() {
        return "BaysNode=" + getNodeName() +
                ", values=[" + Arrays.toString(values) +
                "] perant={" + getP() +
                "} children={" + getC() +
                "} cpt="+ checkCPT();


    }

    private String  checkCPT() {
        String str="";
        if (cpt == null) {
            return str;
        }
        for (String k : cpt.keySet()) {
           str+=k+":"+ cpt.get(k)+" ";
        }
        return str;
    }






    @Override
    public String getNodeName() {
        return this.Name;
    }

    @Override
    public String getNodeValue() throws DOMException {
        return null;
    }

    @Override
    public void setNodeValue(String nodeValue) throws DOMException {

    }

    @Override
    public short getNodeType() {
        return 0;
    }

    @Override
    public Node getParentNode() {
        return null;
    }

    @Override
    public NodeList getChildNodes() {
        return null;
    }

    @Override
    public Node getFirstChild() {
        return null;
    }

    @Override
    public Node getLastChild() {
        return null;
    }

    @Override
    public Node getPreviousSibling() {
        return null;
    }

    @Override
    public Node getNextSibling() {
        return null;
    }

    @Override
    public NamedNodeMap getAttributes() {
        return null;
    }

    @Override
    public Document getOwnerDocument() {
        return null;
    }

    @Override
    public Node insertBefore(Node newChild, Node refChild) throws DOMException {
        return null;
    }

    @Override
    public Node replaceChild(Node newChild, Node oldChild) throws DOMException {
        return null;
    }

    @Override
    public Node removeChild(Node oldChild) throws DOMException {
        return null;
    }

    @Override
    public Node appendChild(Node newChild) throws DOMException {
        return null;
    }

    @Override
    public boolean hasChildNodes() {
        return false;
    }

    @Override
    public Node cloneNode(boolean deep) {
        return null;
    }

    @Override
    public void normalize() {

    }

    @Override
    public boolean isSupported(String feature, String version) {
        return false;
    }

    @Override
    public String getNamespaceURI() {
        return null;
    }

    @Override
    public String getPrefix() {
        return null;
    }

    @Override
    public void setPrefix(String prefix) throws DOMException {

    }

    @Override
    public String getLocalName() {
        return null;
    }

    @Override
    public boolean hasAttributes() {
        return false;
    }

    @Override
    public String getBaseURI() {
        return null;
    }

    @Override
    public short compareDocumentPosition(Node other) throws DOMException {
        return 0;
    }

    @Override
    public String getTextContent() throws DOMException {
        return null;
    }

    @Override
    public void setTextContent(String textContent) throws DOMException {

    }

    @Override
    public boolean isSameNode(Node other) {
        return false;
    }

    @Override
    public String lookupPrefix(String namespaceURI) {
        return null;
    }

    @Override
    public boolean isDefaultNamespace(String namespaceURI) {
        return false;
    }

    @Override
    public String lookupNamespaceURI(String prefix) {
        return null;
    }

    @Override
    public boolean isEqualNode(Node arg) {
        return false;
    }

    @Override
    public Object getFeature(String feature, String version) {
        return null;
    }

    @Override
    public Object setUserData(String key, Object data, UserDataHandler handler) {
        return null;
    }

    @Override
    public Object getUserData(String key) {
        return null;
    }



}
