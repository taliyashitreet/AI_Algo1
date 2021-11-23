import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;

public class SortedFactorsCollection implements Comparator<String> {
    private String[] toSort;
    private LinkedHashMap<String, LinkedHashMap<String, Double>> cptColl;


    SortedFactorsCollection(LinkedHashMap<String, LinkedHashMap<String, Double>> factorsCollection) {
        this.cptColl = factorsCollection;

    }

    @Override
    public int compare(String o1, String o2) {
        return 0;
    }
}


//    @Override
//    public int compare(int i, int j) {
//        String k1 = this.toSort.get(i);
//        String k2 = this.toSort.get(j);
//        String[] A = k1.split("-");
//        String[] B = k2.split("-");
//        if (this.cptColl.get(k1).size() > this.cptColl.get(k2).size()) {
//            swap(i, j);
//        } else if (this.cptColl.get(k1).size() == this.cptColl.get(k2).size()) {
//            int a = 0;
//            for (String k : A) {
//                for (char at : k.toCharArray())
//                    a += at; //cast to ASCII value for i set
//            }
//            int b = 0;
//            for (String k : B) {
//                for (char at : k.toCharArray()) {
//                    b += at; //cast to ASCII value for j set
//                }
//            }
//            if (a > b) {
//                swap(i, j);
//            }
//
//        }
//    }
//
//
//    private void swap(int i, int j) {
//        String tmp =this.toSort.get(i);
//        this.toSort.get(j) = this.toSort.get(i);
//        this.toSort.get(i) = tmp;
//    }
//
//}
