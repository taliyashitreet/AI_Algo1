import java.util.*;

public class VariableElimination {
    private HashMap<String, BaysNode> net;
    private LinkedHashMap<String, LinkedHashMap<String, Double>> FactorsCollection; //collection of cpt
    private String[] SortedFactorsCollection;
    private String query;
    private String[] evidence;
    private String[] hidden;


    //constructor
    VariableElimination(HashMap<String, BaysNode> netInput, String Query) { //get the whole network and building the VariableElimination
        this.net = new HashMap<String, BaysNode>();
        for (String key : netInput.keySet()) {
            this.net.put(key, new BaysNode(netInput.get(key)));
        }
        this.SortedFactorsCollection = new String[net.size()];
        this.FactorsCollection = new LinkedHashMap<>();
        for (String name : net.keySet()) {
            this.FactorsCollection.put(name, net.get(name).getCpt());
        }
        String[] tmp = Query.split("\\|");
        this.query = (tmp[0].split("\\("))[1];
        this.evidence = tmp[1].split("\\)")[0].split(",");
        this.hidden = tmp[1].split("\\)")[1].split("-");
    }

    public int[] EliminationProcess() {
        this.dependency();
        this.reduction();
        this.changingKeys();
        this.CreateStringArray();
        return this.BeforeJoin();
    }

    /**
     * After we saved the FactorsCollection strings in an String[]array -
     * we want to sort the strings from the smallest to the biggest
     * and if 2 strings length are same size we will sort according to the ASCII values of the cpt table
     */

    private void swap(int i, int j) {
        String tmp = "";
        tmp = this.SortedFactorsCollection[i];
        this.SortedFactorsCollection[i] = this.SortedFactorsCollection[j];
        this.SortedFactorsCollection[j] = tmp;
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
        for (int i = 0; i < this.hidden.length; i++) {
            String hid = this.hidden[i];
            if (searches.ancestor(hid, query).equals("Yes") || searches.BaysBallSearch(query, hid, evi).equals("Yes")) {
                removeFactor(hid);
                removeHid(i);
            }
        }
        for (String evidence : evi) {
            for (int i = 0; i < this.hidden.length; i++) {
                String hid = this.hidden[i];
                if (searches.ancestor(hid, evidence).equals("Yes") || searches.BaysBallSearch(evidence, hid, evi).equals("Yes")) {
                    removeFactor(hid);
                    removeHid(i);
                }
            }
        }
    }

    //remove this Hidden variable from the hidden array
    private void removeHid(int hid) {
        String[] newHidden = new String[this.hidden.length - 1];
        for (int i = 0; i < this.hidden.length; i++) {
            if (i != hid) {
                newHidden[i] = this.hidden[i];
            }
        }
        this.hidden = newHidden;
    }

    //remove the factor from the factors collection
    public void removeFactor(String name) { //remove all the factors that contains "name"

        for (BaysNode n : net.values()) { //the Factor of name itself
            if (n.getName().equals(name)) {
                this.FactorsCollection.remove(n.getName());
            }
            for (BaysNode p : n.getParents()) { //the Factors that name is given there
                if (p.getName().equals(name)) {
                    this.FactorsCollection.remove(p.getName());
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
//        String[] quer = this.query.split("=");
//        BaysNode queryNode = net.get(quer[0]);
//
//        for(BaysNode n:this.net.values()) { //now deleting complementary probability
//            if (n != queryNode) {
//                String value = n.getValues()[n.getValues().length - 1];
//                Set<String> K = FactorsCollection.get(n.getName()).keySet(); //all the cpt's keys
//                String[] keyArray = K.toArray(new String[K.size()]); //create a string array to be a iterable object for deleting
//                for (int i = 0; i < keyArray.length; i++) {
//                    if (keyArray[i].length() == 1 && keyArray[i].equals(value)) { //a small table
//                        FactorsCollection.get(n.getName()).remove(keyArray[i]);
//                    } else if (keyArray[i].length() > 1) { //bigger one
//                        if (keyArray[i].substring(keyArray[i].length() - 1).equals(value)) { //the val will be at the last char in this case
//                            FactorsCollection.get(n.getName()).remove(keyArray[i]);
//                        }
//                    }
//                }
//
//            }
//        }
    }

    private void reduction(BaysNode curr, String val) { //Auxiliary function for deletion by a given value in a specific node
        for (String value : curr.getValues()) {
            if (!value.equals(val)) {
                for (BaysNode node : net.values()) {
                    if (node.getName().equals(curr.getName())) { //the curr is the node
                        Set<String> K = FactorsCollection.get(curr.getName()).keySet(); //all the cpt's keys
                        String[] keyArray = K.toArray(new String[K.size()]); //create a string array to be a iterable object for deleting
                        for (int i = 0; i < keyArray.length; i++) {
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
                                Set<String> K = FactorsCollection.get(node.getName()).keySet(); //all the cpt's keys
                                String[] keyArray = K.toArray(new String[K.size()]);
                                for (int i = 0; i < keyArray.length; i++) {
                                    if (keyArray[i].substring(index, index + 1).equals(value)) { //the val will be at the "index" in this case
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
        if (this.FactorsCollection != null) {
            for (String c : this.FactorsCollection.keySet()) {
                str += c + ":";
                for (String val : this.FactorsCollection.get(c).keySet())
                    str += val + ":" + this.FactorsCollection.get(c).get(val) + "\n";
            }
        }
        System.out.println(str);
    }

    public String[] getSortedFactorsCollection() {
        return SortedFactorsCollection;
    }

    /**
     * changing the key's string of all the factors for convenience in subsequent use
     */
    public void changingKeys() {
        for (BaysNode node : net.values()) {
            String parents = "";
            for (BaysNode p : node.getParents()) {
                parents += p.getName() + "-";
            }
            LinkedHashMap<String, Double> value = this.FactorsCollection.get(node.getName());
            this.FactorsCollection.remove(node.getName());
            this.FactorsCollection.put(parents + node.getName(), value);
        }
    }

    /**
     * this fuction enter the key's strings of our Factors collection - and then sort it
     */
    public void CreateStringArray() {
        int i = 0;
        for (String name : this.FactorsCollection.keySet()) {
            this.SortedFactorsCollection[i] = name;
            i++;
        }
        sort(); //sorting function
    }

    public int[] BeforeJoin() {
        int mult=0;
        int sum=0;
        for (int i = 0; i < this.hidden.length; i++) { //the Elimination start now
            String hid = this.hidden[i];
            while (Arrays.toString(this.SortedFactorsCollection).contains(hid)) { //As long as the hid we want to eliminate exists in the collection
                String[] toSend = new String[2];
                int index = 0;
                for (String s : this.SortedFactorsCollection) { //go through the sorting array to make a join in the correct order
                    if (s.contains(hid)) {
                        toSend[index++] = s;
                        if (index == 2) {
                            break;
                        }
                    }
                }
                if (toSend[0] != null) {
                    if (toSend[1] != null) {
                        mult+=join(toSend,mult);
                    } else { //it means that there is only one table left that contains the hidden
                        sum+=eliminate(toSend[0], hid,sum);
                    }
                }
            }
        }
        mult+=join(this.SortedFactorsCollection,mult);
       return new int[]{mult, sum};
    }

    public int eliminate(String el, String hid, int sum) {
        LinkedHashMap<String, Double> newCpt = new LinkedHashMap<>();
        String[] toEl = el.split("-");
        int index = 0;
        String toPut = "";
        for (int i = 0; i < toEl.length; i++) {
            if (toEl[i].contains(hid)) {
                index = i;
            } else toPut += toEl[i] + "-";
        }
        if (toPut.substring(toPut.length()-1).equals("-"))
            toPut = toPut.substring(0,toPut.length()-1);
        for (String key1 : this.FactorsCollection.get(el).keySet()) {
            for (String key2 : this.FactorsCollection.get(el).keySet()) {
                String putVal = "";
                if (key1.equals(key2)) break;
                else {
                    String[] val1 = key1.split("-");
                    String[] val2 = key2.split("-");
                    String tmp1 = val1[index];
                    String tmp2 = val2[index];
                    if (!tmp1.equals(tmp2)) {
                        for (int i = 0; i < val1.length; i++) {
                            if (i != index) {
                                if (i != val1.length - 1)
                                    putVal += val1[i] + "-";
                                else putVal += val1[i];
                            }
                        }
                        if (newCpt.get(putVal) == null) {
                            double prob = this.FactorsCollection.get(el).get(key1) +
                                    this.FactorsCollection.get(el).get(key2);
                            sum++;
                            newCpt.put(putVal, prob);
                        }
                    }
                }

            }
        }
        this.FactorsCollection.put(toPut, newCpt);
        this.FactorsCollection.remove(el);
        String[] newSorted = new String[this.SortedFactorsCollection.length];
        for (int i = 0; i < newSorted.length; i++) {
            if (this.SortedFactorsCollection[i].equals(el)) {
                newSorted[i]=toPut;
            } else {
                newSorted[i] = this.SortedFactorsCollection[i];
            }
        }
        this.SortedFactorsCollection=newSorted;
        sort();
        return sum;
    }

    public int join(String[] joins, int mult) {
        LinkedHashMap<String, Double> newCpt = new LinkedHashMap<>();
        String[] a = joins[1].split("-");
        String[] b = joins[0].split("-");
        ArrayList<Integer> index_a = new ArrayList<>();
        ArrayList<Integer> index_b = new ArrayList<>();
        String putName = "";
        for (int i = 0; i < a.length; i++) {
            for (int j = 0; j < b.length; j++) {
                if (a[i].equals(b[j])) {
                    index_a.add(i);
                    index_b.add(j);
                }
            }
        }

        for (int i = 0; i < a.length; i++) {
            putName += a[i] + "-";
        }
        for (int i = 0; i < b.length; i++) {
            if (!putName.contains(b[i]))
                putName += b[i] + "-";
        }
        if (putName.substring(putName.length() - 1).equals("-")) {
            putName = putName.substring(0, putName.length() - 1);
        }

        for (String key1 : this.FactorsCollection.get(joins[1]).keySet()) {
            for (String key2 : this.FactorsCollection.get(joins[0]).keySet()) {
                String[] val1 = key1.split("-");
                String[] val2 = key2.split("-");
                String tmp1 = "";
                String tmp2 = "";
                for (int i : index_a) {
                    tmp1 += val1[i] + "-";

                }
                for (int j : index_b) {
                    tmp2 += val2[j] + "-";

                }
                if (tmp1.equals(tmp2)) {
                    double prob = this.FactorsCollection.get(joins[1]).get(key1) *
                            this.FactorsCollection.get(joins[0]).get(key2);
                    mult++;
                    String putVal = "";  // ????????????????
                    for (int i = 0; i < val1.length; i++) {
                        putVal += val1[i] + "-";

                    }
                    for (int i = 0; i < val2.length; i++) {
                        for (int j : index_b) {
                            if (i != j) {
                                putVal += val2[i] + "-";
                            }
                        }
                    }
                    if (putVal.substring(putVal.length() - 1).equals("-")) {
                        putVal = putVal.substring(0, putVal.length() - 1);
                    }
                    newCpt.put(putVal, prob);
                }
            }
        }
        this.FactorsCollection.put(putName, newCpt);
        if(!putName.equals(joins[1])) this.FactorsCollection.remove(joins[1]);
        if(!putName.equals(joins[0]))this.FactorsCollection.remove(joins[0]);
        this.DelFromDorted(joins, putName);
        return mult;
    }


    public void DelFromDorted(String[] joins, String putName) {
        String[] newSorted = new String[this.SortedFactorsCollection.length - 1];
        boolean flag = true;
        for (int i = 0; i < this.SortedFactorsCollection.length; i++) {
            if (!this.SortedFactorsCollection[i].equals(joins[0]) && !this.SortedFactorsCollection[i].equals(joins[1])) {
                if (flag)
                    newSorted[i] = this.SortedFactorsCollection[i];
                else {
                    newSorted[i - 1] = this.SortedFactorsCollection[i];
                }
            }
            else  { //the first time we get a that needed to be remove
                newSorted[i] = putName;
                i++;
                flag = false;
            }
        }
        this.SortedFactorsCollection = newSorted;

        sort();

    }

    private void sort() {
        for (int i = 0; i < this.SortedFactorsCollection.length; i++) {
            for (int j = i + 1; j < this.SortedFactorsCollection.length; j++) {
                String tmp = "";
                int i_cpt = this.FactorsCollection.get(SortedFactorsCollection[i]).size();
                int j_cpt = this.FactorsCollection.get(SortedFactorsCollection[j]).size();
                if (i_cpt > j_cpt) {
                    swap(i, j);
                } else if (i_cpt == j_cpt) {
                    int a = 0;
                    for (String k : this.SortedFactorsCollection[i].split("-")) {
                        a += k.charAt(0); //cast to ASCII value for i set
                    }
                    int b = 0;
                    for (String k : this.SortedFactorsCollection[j].split("-")) {
                        b += k.charAt(0); //cast to ASCII value for j set
                    }
                    if (a > b) {
                        swap(i, j);
                    }

                }
            }
        }
    }
}




