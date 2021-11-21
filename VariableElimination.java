
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

public class VariableElimination {
    private HashMap<String, BaysNode> net;
    private LinkedHashMap<String, LinkedHashMap<String, Double>> FactorsCollection; //collection of cpt
    private String[] SortedFactorsCollection;
    private String query;
    private String[] evidence;
    private String[] hidden;


    //constructor
    VariableElimination(HashMap<String, BaysNode> net, String Query) { //get the whole network and building the VariableElimination
        this.net = net;
        this.SortedFactorsCollection=new String[net.size()];
        this.FactorsCollection = new LinkedHashMap<>();
        int i=0;
        for (String name : net.keySet()) {
            this.FactorsCollection.put(name, net.get(name).getCpt());
            this.SortedFactorsCollection[i]=name;
            i++;
        }
        String[] tmp = Query.split("\\|");
        this.query = (tmp[0].split("\\("))[1];
        this.evidence = tmp[1].split("\\)")[0].split(",");
        this.hidden = tmp[1].split("\\)")[1].split("-");
        sort();
    }

    /**
     * After we saved the FactorsCollection strings in an String[]array -
     * we want to sort the strings from the smallest to the biggest
     * and if 2 strings length are same size we will sort according to the ASCII values of the cpt table
     */

    private void sort() {
        for(int i=0; i<this.SortedFactorsCollection.length; i++){
            for(int j=i+1; j<this.SortedFactorsCollection.length; j++){
                String tmp = "";
                LinkedHashMap<String, Double> i_cpt=this.net.get(SortedFactorsCollection[i]).getCpt();
                LinkedHashMap<String, Double> j_cpt=this.net.get(SortedFactorsCollection[j]).getCpt();
                if(i_cpt.size()>j_cpt.size()){
                  swap(i,j);
                }
                else if(i_cpt.size()==j_cpt.size()){
                    int a=0;
                    for(String k: i_cpt.keySet()){
                        a+= k.charAt(0); //cast to ASCII value for i set
                    }
                    int b=0;
                    for(String k: j_cpt.keySet()){
                        b+= k.charAt(0); //cast to ASCII value for j set
                    }
                    if(a>b){
                       swap(i,j);
                    }

                }
            }
        }
    }
    private void swap(int i, int j){
        String tmp="";
        tmp=this.SortedFactorsCollection[i];
        this.SortedFactorsCollection[i]=this.SortedFactorsCollection[j];
        this.SortedFactorsCollection[j]=tmp;
    }

    //getters
    public LinkedHashMap<String, LinkedHashMap<String, Double>> getFactorsCollection() {
        return FactorsCollection;
    }

    public String getQuery() {
        return query;
    }

    public String[] getEvidence() {
        return evidence;
    }

    public String[] getHidden() {
        return hidden;
    }

    /**
     * We must first check whether the hidden variable is independent of the query and evidence variables
     * if it is independent we will to delete any appearance of it
     */
    public void dependency() { //first,
        String query = this.query.split("=")[0];
        String[] evi = new String[this.evidence.length];
        for (int i = 0; i < this.evidence.length; i++) {
            evi[i] = this.evidence[i].split("=")[0];
        }
        Searches searches = new Searches(net);
        for (String hid : this.hidden) {
            if (searches.ancestor(hid, query).equals("Yes") || searches.BaysBallSearch(query, hid, evi).equals("Yes")) {
                removeFactor(hid);
            }
        }
        for (String evidence : evi) {
            for (String hid : this.hidden) {
                if (searches.ancestor(hid, evidence).equals("Yes") || searches.BaysBallSearch(evidence, hid, evi).equals("Yes")) {
                    removeFactor(hid);
                }
            }
        }
    }

    public void removeFactor(String name) { //remove all the factors that contains "name"

        for (BaysNode n : net.values()) { //the Factor of name itself
            if (n.getName().equals(name)) {
                this.FactorsCollection.get(name).remove(name);
            }
            for (BaysNode p : n.getParents()) { //the Factors that name is given there
                if (p.getName().equals(name)) {
                    this.FactorsCollection.get(p.getName()).remove(p.getName());
                }
            }

        }
    }

    /**
     * In this function we want to reduce the tables by deleting all the instances that are not relevant to the query
     * And all the values that are a complementary probability
     */
    public void reduction() {
        for (int i = 0; i < evidence.length; i++) {
            String[] evi = evidence[i].split("="); //we got a string like: "B=T" so we need to split
            BaysNode curr = net.get(evi[0]);
            reduction(curr, evi[1]);
        }
        String[] quer = this.query.split("=");
        BaysNode queryNode = net.get(quer[0]);

        for(BaysNode n:this.net.values()) { //now deleting complementary probability
            if (n != queryNode) {
                String value = n.getValues()[n.getValues().length - 1];
                Set<String> K = FactorsCollection.get(n.getName()).keySet(); //all the cpt's keys
                String[] keyArray = K.toArray(new String[K.size()]); //create a string array to be a iterable object for deleting
                for (int i = 0; i < keyArray.length; i++) {
                    if (keyArray[i].length() == 1 && keyArray[i].equals(value)) { //a small table
                        FactorsCollection.get(n.getName()).remove(keyArray[i]);
                    } else if (keyArray[i].length() > 1) { //bigger one
                        if (keyArray[i].substring(keyArray[i].length() - 1).equals(value)) { //the val will be at the last char in this case
                            FactorsCollection.get(n.getName()).remove(keyArray[i]);
                        }
                    }
                }

            }
        }
    }

    private void reduction(BaysNode curr, String val) { //Auxiliary function for deletion by a given value in a specific node
        for (String value : curr.getValues()) {
            if (!value.equals(val)) {
                for (BaysNode node : net.values()) {
                    if (node.getName().equals(curr.getName())) { //the curr is the node
                        Set<String> K=FactorsCollection.get(curr.getName()).keySet(); //all the cpt's keys
                        String[] keyArray = K.toArray(new String[K.size()]); //create a string array to be a iterable object for deleting
                        for (int i=0; i< keyArray.length; i++) {
                            if (keyArray[i].length() == 1 && keyArray[i].equals(value)) { //a small table
                                FactorsCollection.get(curr.getName()).remove(keyArray[i]);
                            } else if (keyArray[i].length() > 1) { //bigger one
                                if (keyArray[i].substring(keyArray[i].length() - 1).equals(value)) { //the val will be at the last char in this case
                                    FactorsCollection.get(curr.getName()).remove(keyArray[i]);
                                }
                            }
                        }
                    } else {
                        int index = 0;
                        for (BaysNode p : node.getParents()) {
                            if (p.getName().equals(curr.getName())) {
                                index++; //the index of curr at node's parents array
                                Set<String> K=FactorsCollection.get(node.getName()).keySet(); //all the cpt's keys
                                String[] keyArray = K.toArray(new String[K.size()]);
                                for (int i=0; i< keyArray.length; i++) {
                                    if (keyArray[i].substring(index,index+1).equals(value)) { //the val will be at the "index" in this case
                                        FactorsCollection.get(node.getName()).remove(keyArray[i]);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    public void FactorsToString() {
        String str = "";
        if (this.FactorsCollection!= null) {
            for (String c : this.FactorsCollection.keySet()) {
                str += c + ":";
                for(String val: this.FactorsCollection.get(c).keySet())
                 str+= val +":"+ this.FactorsCollection.get(c).get(val)+ "\n";
            }
        }
        System.out.println(str);
    }

    public String[] getSortedFactorsCollection() {
        return SortedFactorsCollection;
    }
}

