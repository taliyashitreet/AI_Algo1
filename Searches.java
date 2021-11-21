import org.w3c.dom.Node;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Queue;

public class Searches {
    HashMap<String, BaysNode> BaysNet;


    public Searches(HashMap<String, BaysNode> BaysNet){
        this.BaysNet=BaysNet;
    }

    public String BaysSearch(String Query) {
        String[] tmp = Query.split("\\|");
        String[] Q = tmp[0].split("-");
        String[] G = tmp[1].split(",");
        String start = Q[0]; //start the search from this node
        String toSearch = Q[1]; //end the search with this node
        String[] given = new String[G.length];
        for (int i = 0; i <G.length; i++) {
            given[i]=G[i].split("=")[0];
        }
        return BaysBallSearch(start, toSearch, G);
    }

   public String BaysBallSearch(String start, String toSearch,String[] given)    {

        for (String variable : this.BaysNet.keySet()) {
            BaysNet.get(variable).SetColor("white");
        }
        //if we have "for given"
        for (int i = 0; i < given.length; i++) {
            BaysNet.get(given[i]).SetColor("red"); // red=the given node
        }

    Queue<BaysNode> queue = new LinkedList<BaysNode>();
        queue.add(BaysNet.get(start));
        BaysNet.get(start).

    SetColor("grey");
        while(!queue.isEmpty())

    {
        BaysNode curr = queue.poll();
        if (!curr.getName().equals(toSearch)) {
            if (curr.getColor() == "grey" || curr.getColor() == "blue") { //gry/blue means we can search of all the neighbors
                if (curr.getChildren().size() != 0) {
                    for (BaysNode p : curr.getChildren()) {
                        if (p.getColor() == "white") {
                            queue.add(p);
                            p.SetColor("green");
                        } else if (p.getColor() == "red") {
                            queue.add(p);
                            p.SetColor("red from perent");
                        }
                    }
                }
                if (curr.getParents().size() != 0) {
                    for (BaysNode p : curr.getParents()) {
                        if (p.getColor() == "white") {
                            queue.add(p);
                            p.SetColor("blue");
                        } else if (p.getColor() == "red") {
                            queue.add(p);
                            p.SetColor("red from child"); //cant add any of this red's Neighbors
                        }
                    }
                }
            } else if (curr.getColor() == "green") { // geen means we came from a parent
                if (curr.getChildren().size() != 0) {
                    for (BaysNode p : curr.getChildren()) {
                        if (p.getColor() == "white") {
                            queue.add(p);
                            p.SetColor("green");
                        } else if (p.getColor() == "red") {
                            queue.add(p);
                            p.SetColor("red from perent"); //can enter only this red's perant
                        }
                    }
                }
            } else if (curr.getColor() == "red from perent") {
                if (curr.getParents().size() != 0) {
                    for (BaysNode p : curr.getParents()) {
                        if (p.getColor() == "white") { // doesn't entered yet
                            queue.add(p);
                            p.SetColor("blue");
                        } else if (p.getColor() == "green") { //already entered
                            queue.add(p);
                            p.SetColor("red from perent"); //
                        }
                    }
                }
            }
        } else {
            return "No"; //we found the node end - so they are dependent
        }
    }
        return"Yes"; // independent
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
                if(c.getColor()=="white"){
                    c.SetColor("gray");
                    queue.add(c);
                }
            }
        }
        else{ return "No"; }

    }
    return "Yes";

}

}
