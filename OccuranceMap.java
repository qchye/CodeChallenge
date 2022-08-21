import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class OccuranceMap extends RecursiveAction {
    static int SEQUENTIAL_THRESHOLD = 110000;
    static int COUNT_BEGIN_INDEX = 20;
    static int DAY_END_INDEX = 19;
    int lo;
    int hi;
    List<String> arr;
    HashMap<Integer, String> map = new HashMap<Integer, String>();

    OccuranceMap(List<String> arr, int low, int high) {
        this.lo = low;
        this.hi = high;
        this.arr = arr;
    }

    protected void compute() {
        if (hi - lo <= SEQUENTIAL_THRESHOLD) {
            for (int i = lo; i < hi; i++) {
                String wholeString = arr.get(i);
                String Day = wholeString.substring(0, DAY_END_INDEX);
                Integer carCount = Integer.parseInt(wholeString.substring(COUNT_BEGIN_INDEX).trim());
                map.put(carCount, Day);
            }
        } else {
            OccuranceMap left = new OccuranceMap(arr, lo, (hi + lo) / 2);
            OccuranceMap right = new OccuranceMap(arr, (hi + lo) / 2, hi);
            left.fork();
            right.compute();
            left.join();

            map.putAll(right.map);
            map.putAll(left.map);
        }
    }
}