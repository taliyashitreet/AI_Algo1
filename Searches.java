import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Searches {
    HashMap<String, BaysNode> BaysNet;


    public Searches(HashMap<String, BaysNode> netInput){
        this.BaysNet=new HashMap<String, BaysNode>();
        for (String key : netInput.keySet()) {
            this.BaysNet.put(key, new BaysNode(netInput.get(key)));
        }
    }

    public String BaysSearch(String Query) {
        String[] tmp = Query.split("\\|");
        String[] Q = tmp[0].split("-");
        String start = Q[0]; //start the search from this node
        String toSearch = Q[1]; //end the search with this node
        String[] G=(tmp.length>1)?tmp[1].split(","): new String[0];
        String[] given = new String[G.length];
        for (int i = 0; i <G.length; i++) {
            given[i]=G[i].split("=")[0];
        }
        return BaysBallSearch(start, toSearch, given);
    }
   public String BaysBallSearch(String start, String toSearch,String[] given)    {

        for (String variable : this.BaysNet.keySet()) {
            this.BaysNet.get(variable).SetColor("white");
        }
        //if we have "for given"
        for (int i = 0; i < given.length; i++) {
            this.BaysNet.get(given[i]).SetColor("red"); // red=the given node
        }

    Queue<BaysNode> queue = new LinkedList<BaysNode>();
        queue.add(this.BaysNet.get(start));
        this.BaysNet.get(start).SetColor("grey");
        while(!queue.isEmpty())

    {
        BaysNode curr = queue.poll();
        if (!curr.getName().equals(toSearch)) {
            if (curr.getColor().equals("grey") || curr.getColor().equals("blue")) { //gry/blue means we can search of all the neighbors
                if (curr.getChildren().size() != 0) {
                    for (BaysNode c : curr.getChildren()) {
                        BaysNode C=BaysNet.get(c.getName());
                            if (C.getColor().equals("white")) {
                            queue.add(C);
                            C.SetColor("green");
                        } else if (C.getColor().equals("red")) {
                            queue.add(C);
                            C.SetColor("red from perent");
                        }
                    }
                }
                if (curr.getParents().size() != 0) {
                    for (BaysNode p : curr.getParents()) {
                        BaysNode P=BaysNet.get(p.getName());
                        if (P.getColor().equals("white")) {
                            queue.add(P);
                            P.SetColor("blue");
                        } else if (P.getColor().equals("red")) {
                            queue.add(P);
                            P.SetColor("red from child"); //cant add any of this red's Neighbors
                        }
                    }
                }
            } else if (curr.getColor().equals("green")) { // geen means we came from a parent
                if (curr.getChildren().size() != 0) {
                    for (BaysNode c : curr.getChildren()) {
                        BaysNode C=BaysNet.get(c.getName());
                        if (C.getColor().equals("white")) {
                            queue.add(C);
                            C.SetColor("green");
                        } else if (C.getColor().equals("red")) {
                            queue.add(C);
                            C.SetColor("red from perent"); //can enter only this red's perant
                        }
                    }
                }
            } else if (curr.getColor().equals("red from perent")) {
                if (curr.getParents().size() != 0) {
                    for (BaysNode p : curr.getParents()) {
                        BaysNode P=BaysNet.get(p.getName());
                        if (P.getColor().equals("white")) { // doesn't entered yet
                            queue.add(P);
                            P.SetColor("blue");
                        } else if (P.getColor().equals("green")) { //already entered
                            queue.add(P);
                            P.SetColor("red from perent"); //
                        }
                    }
                }
            }
        } else {
            return "no"; //we found the node end - so they are dependent
        }
    }
        return"yes"; // independent
}
public String ancestor(String start, String toFind){
    for (String variable : this.BaysNet.keySet()) {
        BaysNet.get(variable).SetColor("white");
    }
    Queue<BaysNode> queue = new LinkedList<BaysNode>();
    queue.add(BaysNet.get(start));
    BaysNet.get(start).SetColor("grey");
    while(!queue.isEmpty()) {
        BaysNode curr = queue.poll();
        if (!curr.getName().equals(toFind)) {
            for(BaysNode c: curr.getChildren()){
                if(BaysNet.get(c.getName()).getColor().equals("white")){
                    BaysNet.get(c.getName()).SetColor("gray");
                    queue.add(c);
                }
            }
        }
        else{ return "yes"; }

    }
    return "no";

}

}
