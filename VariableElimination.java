import java.text.DecimalFormat;
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
        this.hidden = tmp[1].split("\\)")[1].substring(1).split("-");
    }

    /**
     * this is the maim function- You can clearly see in this function the process of answering the query-
     * first you have to check dependency in order to delete irrelevant factors.
     * Then reduce - delete all the values that do not interest us in the query.
     * then I will create an array of strings that saves all the Hashmap key names (for convenience)
     * and then calculate the number of multiplication
     * and addition operations during join and eliminate and of course normalize
     *
     * @return
     */
    public String EliminationProcess() {
        if (IsInTheTable()) {
            return IsIn() + ",0,0";
        } else {
            this.dependency();
            this.reduction();
            this.changingKeys();
            this.CreateStringArray();
            int[] operations = new int[2];
            operations = this.BeforeJoin();
            String ans = this.normalization(operations[1]);
            return ans + "," + operations[0];
        }
    }

    /**
     * After we saved the FactorsCollection strings in an String[]array -
     * we want to sort the strings from the smallest to the biggest
     * and if 2 strings length are same size we will sort according to the ASCII values of the cpt table
     */
    public boolean IsInTheTable() { //Checks whether the answer is in the cell in the table
        if (this.evidence.length == 1 && evidence[0].equals("")) {
            return true;
        }
        return false;
    }

    public String IsIn() { //Cell in the table
        String[] q = this.query.split("=");
        String ans = "";
        for (String key : this.net.get(q[0]).getCpt().keySet()) {
            String[] k = key.split("-");
            if (k[k.length - 1].equals(q[1])) {
                ans += Double.toString(this.net.get(q[0]).getCpt().get(key));
            }
        }
        return ans;
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
    public void dependency() {
        String query = this.query.split("=")[0];
        String[] evi = new String[this.evidence.length];
        for (int i = 0; i < this.evidence.length; i++) {
            evi[i] = this.evidence[i].split("=")[0];
        }
        Searches searches = new Searches(this.net);
        String[] copyOf = new String[this.hidden.length];
        copyOf = Arrays.copyOf(this.hidden, this.hidden.length);
        for (int i = 0; i < copyOf.length; i++) {
            String hid = copyOf[i];
            boolean flag = true;
            for (String e : evi) {
                if (searches.ancestor(hid, e).equals("yes")) //If it is an ancestor of one of them then it is impossible to delete it
                    flag = false;
                //If the hidden variable is neither an ancestor of evidence nor of the query
                // then it can be deleted from the table
            }
            if (searches.ancestor(hid, query).equals("yes"))
                flag = false;
            //If the hidden variable is neither an ancestor of evidence nor of the query
            // then it can be deleted from the table
            if (flag) {
                removeFactor(hid);
                removeHid(copyOf[i]);

            }
            if (!flag) {
                //If the variable is independent
                if (searches.BaysBallSearch(query, hid, evi).equals("yes")) {
                    removeFactor(hid);
                    removeHid(copyOf[i]);
                }
            }

        }
    }

    //remove this Hidden variable from the hidden array
    private void removeHid(String hid) {
        String[] newHidden = new String[this.hidden.length - 1];
        boolean flag = true;
        for (int i = 0; i < this.hidden.length; i++) {
            if (!hidden[i].equals(hid)) {
                if (flag)
                    newHidden[i] = this.hidden[i];
                else newHidden[i - 1] = this.hidden[i];
            } else flag = false;
        }
        this.hidden = newHidden;
    }

    //remove the factor from the factors collection
    public void removeFactor(String name) { //remove all the factors that contains "name"
        String[] evi = new String[evidence.length];
        for (int i = 0; i < evidence.length; i++) {
            String[] tmp = evidence[i].split("=");
            evi[i] = tmp[0];
        }
        //For sure the children of the node we want to delete hold values within their table that need to be deleted
        for (BaysNode n : this.net.get(name).getChildren()) {
            if (n.getParents().size() == 1) {
                this.FactorsCollection.remove(n.getName());
                this.net.remove(n.getName());
            } else {
                String eviStr = Arrays.toString(evi);
                boolean flag = true;
                for (int i = 0; i < n.getParents().size(); i++) {
                    if (!n.getParents().get(i).getName().equals(name)) {
                        if (!eviStr.contains(n.getParents().get(i).getName()))
                            flag = false;
                    }
                }
                if (flag) {
                    this.FactorsCollection.remove(n.getName());
                    this.net.remove(n.getName());
                    this.SortedFactorsCollection = new String[this.FactorsCollection.size()];
                    if (eviStr.contains(n.getName())) {
                        removeEvi(n.getName());
                    }
                }
            }
        }

        this.FactorsCollection.remove(name);
        this.net.remove(name);
        this.SortedFactorsCollection = new String[this.FactorsCollection.size()];
    }

    private void removeEvi(String evi) {
        for (int i = 0; i < evidence.length; i++) {
            if (evidence[i].contains(evi)) {
                removeEvi(i);
            }
        }
    }

    private void removeEvi(int evi) { //remove the evidence from the evidence's array
        String[] newEvi = new String[this.evidence.length - 1];
        for (int i = 0; i < this.evidence.length; i++) {
            if (i != evi) {
                if (i < newEvi.length)
                    newEvi[i] = this.evidence[i];
                else newEvi[i - 1] = this.evidence[i];
            }
        }
        this.evidence = newEvi;
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
        for (BaysNode n : net.values()) {
            for (BaysNode p : n.getParents()) {
                for (int i = 0; i < evidence.length; i++) {
                    String[] evi = evidence[i].split("=");//we got a string like: "B=T" so we need to split
                    if (p.getName().equals(evi[0])) {
                        reduction(n, p, evi[1]);
                    }
                }
            }
        }
    }

    private void reduction(BaysNode curr, BaysNode evi, String val) {
        int index = curr.getParents().indexOf(evi);
        Set<String> K = net.get(curr.getName()).getCpt().keySet(); //all the cpt's keys
        String[] keyArray = K.toArray(new String[K.size()]);
        for (int i = 0; i < keyArray.length; i++) {
            String[] tmp = keyArray[i].split("-");
            if (!tmp[index].equals(val)) {
                this.FactorsCollection.get(curr.getName()).remove(keyArray[i]);
                this.net.get(curr.getName()).getCpt().remove(keyArray[i]);
            }
        }
    }

    private void reduction(BaysNode curr, String val) { //Auxiliary function for deletion by a given value in a specific node
        Set<String> K = net.get(curr.getName()).getCpt().keySet(); //all the cpt's keys
        String[] keyArray = K.toArray(new String[K.size()]);
        for (int i = 0; i < keyArray.length; i++) {
            String[] tmp = keyArray[i].split("-");
            if (!tmp[tmp.length - 1].equals(val)) {
                this.FactorsCollection.get(curr.getName()).remove(keyArray[i]);
                this.net.get(curr.getName()).getCpt().remove(keyArray[i]);
            }
        }

        if (this.getFactorsCollection().get(curr.getName()).size() == 1) {
            this.FactorsCollection.remove(curr.getName());
            this.net.remove(curr.getName());
            this.SortedFactorsCollection = new String[this.FactorsCollection.size()];
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

    /**
     * This function is responsible for the whole process of variable elimination -
     * checks if there are any hidden ones that need to be treated -
     * If there are two tables with the same variable hidden then join them
     * if there is one table left with this hidden than we de eliminate
     * @return
     */
    public int[] BeforeJoin() {
        int mult = 0;
        int sum = 0;
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
                        mult += join(toSend, 0);
                    } else { //it means that there is only one table left that contains the hidden
                        sum += eliminate(toSend[0], hid, 0);
                    }
                }
            }
        }
        if (this.FactorsCollection.size() == 2)
            mult += join(this.SortedFactorsCollection, 0);
        return new int[]{mult, sum};
    }

    private void delLastEvi() {
        for (String evi : evidence) {
            if (Arrays.toString(this.SortedFactorsCollection).contains(evi))
                this.FactorsCollection.remove(evi);
        }
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
        if (toPut.substring(toPut.length() - 1).equals("-"))
            toPut = toPut.substring(0, toPut.length() - 1);
        Set<String> K = this.FactorsCollection.get(el).keySet();
        String[] keyArray = K.toArray(new String[K.size()]);
        for (int i = 0; i < keyArray.length; i++) {
            String[] val1 = keyArray[i].split("-");
            boolean flag = false;
            double prob = 0;
            for (int j = i + 1; j < keyArray.length; j++) {
                String[] val2 = keyArray[j].split("-");
                boolean flag2 = true;
                for (int k = 0; k < val2.length; k++) {
                    if (k != index && !val1[k].equals(val2[k])) {
                        flag2 = false;
                        break;
                    }
                }
                if (flag2) {
                    flag = true;
                    prob += this.FactorsCollection.get(el).get(keyArray[i]) +
                            this.FactorsCollection.get(el).get(keyArray[j]);
                    sum++;

                }
            }
            if (flag) {
                String putVal = "";
                for (int k = 0; k < val1.length; k++) {
                    if (k != index) {
                        if (k != val1.length - 1)
                            putVal += val1[k] + "-";
                        else putVal += val1[k];
                    }
                }
                newCpt.put(putVal, prob);
            }
        }
        this.FactorsCollection.put(toPut, newCpt);
        this.FactorsCollection.remove(el);
        String[] newSorted = new String[this.SortedFactorsCollection.length];
        for (int i = 0; i < newSorted.length; i++) {
            if (this.SortedFactorsCollection[i].equals(el)) {
                newSorted[i] = toPut;
            } else {
                newSorted[i] = this.SortedFactorsCollection[i];
            }
        }
        this.SortedFactorsCollection = newSorted;
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
                if (tmp1.equals(tmp2)) { //When the indices of the overlapping variables are the same the values should be multiplied
                    double prob = this.FactorsCollection.get(joins[1]).get(key1) *
                            this.FactorsCollection.get(joins[0]).get(key2);
                    mult++;
                    String putVal = "";//the new name of the new Factor we're making
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
        if (!putName.equals(joins[1])) {
            this.FactorsCollection.remove(joins[1]);
        } else this.DelFromSorted(joins[0]);
        if (!putName.equals(joins[0])) {
            this.FactorsCollection.remove(joins[0]);
        } else this.DelFromSorted(joins[1]);
        if (!putName.equals(joins[1]) && !putName.equals(joins[0])) this.DelFromSorted(joins, putName);
        return mult;
    }


    public void DelFromSorted(String[] joins, String putName) {
        String[] newSorted = new String[this.SortedFactorsCollection.length - 1];
        boolean flag = true;
        boolean flag2 = true;
        for (int i = 0; i < this.SortedFactorsCollection.length; i++) {
            if (!this.SortedFactorsCollection[i].equals(joins[0]) && !this.SortedFactorsCollection[i].equals(joins[1])) {
                if (flag2)
                    newSorted[i] = this.SortedFactorsCollection[i];
                else newSorted[i - 1] = this.SortedFactorsCollection[i];
            } else if (flag) { //the first time we get a that needed to be remove
                newSorted[i] = putName;
                flag = false;
            } else flag2 = false;
        }
        this.SortedFactorsCollection = newSorted;

        sort();

    }

    public void DelFromSorted(String joins) {
        String[] newSorted = new String[this.SortedFactorsCollection.length - 1];
        boolean flag = true;
        for (int i = 0; i < this.SortedFactorsCollection.length; i++) {
            if (!this.SortedFactorsCollection[i].equals(joins)) {
                if (flag)
                    newSorted[i] = this.SortedFactorsCollection[i];
                else newSorted[i - 1] = this.SortedFactorsCollection[i];
            } else flag = false;
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

    private void swap(int i, int j) {
        String tmp = "";
        tmp = this.SortedFactorsCollection[i];
        this.SortedFactorsCollection[i] = this.SortedFactorsCollection[j];
        this.SortedFactorsCollection[j] = tmp;
    }

    public String normalization(int sum) {
        String[] q = this.query.split("=");
        double normFactor = 0;
        double tmp = 0;
        double toNormal = 0;
        for (double val : this.FactorsCollection.get(this.SortedFactorsCollection[0]).values()) {
            tmp += val;
            sum++;
        }
        sum=sum-1;//For each pair one connection operation
        normFactor = 1.0 / tmp;
        String[] findQ = this.SortedFactorsCollection[0].split("-");
        int index = 0;
        for (int i = 0; i < findQ.length; i++) {
            if (findQ[i].equals(q[0])) {
                index = i;
            }
        }
        for (String key : this.FactorsCollection.keySet()) {
            for (String k : this.FactorsCollection.get(key).keySet()) {
                String[] val = k.split("-");
                if (val[index].equals(q[1])) {
                    toNormal += this.FactorsCollection.get(key).get(k);
                    break;
                }
            }
        }
        double ans = toNormal * normFactor;
        DecimalFormat df = new DecimalFormat("#.#####");

        return df.format(ans) + "," + Integer.toString(sum);
    }
}




