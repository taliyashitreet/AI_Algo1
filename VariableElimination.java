
import org.w3c.dom.Node;

import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Set;

public class VariableElimination {
    private HashMap<String, BaysNode> net;
    private LinkedHashMap<String, LinkedHashMap<String, Double>> FactorsCollection; //collection of cpt
    private String query;
    private String[] evidence;
    private String[] hidden;


    //constructor
    VariableElimination(HashMap<String, BaysNode> net, String Query) { //get the whole network and building the VariableElimination
        this.net = net;
        this.FactorsCollection = new LinkedHashMap<>();
        for (String name : net.keySet()) {
            this.FactorsCollection.put(name, net.get(name).getCpt());
        }
        String[] tmp = Query.split("\\|");
        this.query = (tmp[0].split("\\("))[1];
        this.evidence = tmp[1].split("\\)")[0].split(",");
        this.hidden = tmp[1].split("\\)")[1].split("-");
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
     */
    public void reduction() {
        for (int i = 0; i < evidence.length; i++) {
            String[] evi = evidence[i].split("="); //we got a string like: "B=T" so we need to split
            BaysNode curr = net.get(evi[0]);
            reduction(curr, evi[1]);
        }
        String[] quer = this.query.split("=");
        BaysNode curr = net.get(quer[0]);
        reduction(curr, quer[1]);
    }

    private void reduction(BaysNode curr, String val) {
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
                                    if (keyArray[i].substring(index,index+1).equals(value)) { //the val will be at the index in this case
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

}

