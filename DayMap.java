import java.util.HashMap;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class DayMap extends RecursiveAction {
    static int SEQUENTIAL_THRESHOLD = 110000;
    static int COUNT_BEGIN_INDEX = 20;
    static int DAY_END_INDEX = 10;
    int lo;
    int hi;
    List<String> arr;
    HashMap<String, Integer> map = new HashMap<String, Integer>();

    DayMap(List<String> arr, int low, int high) {
        this.lo = low;
        this.hi = high;
        this.arr = arr;
    }

    protected void compute() {
        if (hi - lo <= SEQUENTIAL_THRESHOLD) {
            for (int i = lo; i < hi; i++) {
                String wholeString = arr.get(i);
                String Day = wholeString.substring(0,10);
                Integer carCount = Integer.parseInt(wholeString.substring(COUNT_BEGIN_INDEX).trim());
                if (map.keySet().contains(Day)) {
                    map.put(Day, map.get(Day) + carCount);
                } else {
                    map.put(Day, carCount);
                }
            }
        } else {
            DayMap left = new DayMap(arr, lo, (hi + lo) / 2);
            DayMap right = new DayMap(arr, (hi + lo) / 2, hi);
            left.fork();
            right.compute();
            left.join();

            left.map.forEach((k, v) -> map.merge(k, v, Math::addExact));
            right.map.forEach((k, v) -> map.merge(k, v, Math::addExact));
        }
    }
}