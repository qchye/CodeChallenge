import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.RecursiveAction;

public class LeastThreeConsecutive extends RecursiveAction {
    static int SEQUENTIAL_THRESHOLD = 110000;
    static int PEEK_AHEAD_ONE_INDEX = 1;
    static int PEEK_AHEAD_TWO_INDEX = 2;
    static int COUNT_BEGIN_INDEX = 20;
    int lo;
    int hi;
    List<String> arr;
    List<String> result = new ArrayList<>();
    Integer tempSum = Integer.MAX_VALUE;

    LeastThreeConsecutive(List<String> arr, int low, int high) {
        this.lo = low;
        this.hi = high;
        this.arr = arr;
    }

    //Look ahead 1 and 2 places to check the subtotal of 3 consecutive half hours
    protected void compute() {
        if (hi - lo <= SEQUENTIAL_THRESHOLD) {
            for (int i = lo; i < hi; i++) {
                if (i + PEEK_AHEAD_ONE_INDEX < arr.size() && i + PEEK_AHEAD_TWO_INDEX < arr.size())
                {
                    String s1 = arr.get(i);
                    String s2 = arr.get(i+PEEK_AHEAD_ONE_INDEX);
                    String s3 = arr.get(i+PEEK_AHEAD_TWO_INDEX);

                    Integer PartialSum = PartialSum(s1, s2, s3);
                    if (PartialSum < tempSum)
                    {
                        tempSum = PartialSum;
                        result = new ArrayList<>();
                        result.add(s1);
                        result.add(s2);
                        result.add(s3);
                    }
                }
            }
        } else {
            LeastThreeConsecutive left = new LeastThreeConsecutive(arr, lo, (hi + lo) / 2);
            LeastThreeConsecutive right = new LeastThreeConsecutive(arr, (hi + lo) / 2, hi);
            left.fork();
            right.compute();
            left.join();

            if (left.tempSum < right.tempSum)
            {
                result = left.result;
            }
            else{
                result = right.result;
            }
        }
    }

    private Integer PartialSum(String s1, String s2, String s3){
        Integer carCount1 = Integer.parseInt(s1.substring(COUNT_BEGIN_INDEX).trim());
        Integer carCount2 = Integer.parseInt(s2.substring(COUNT_BEGIN_INDEX).trim());
        Integer carCount3 = Integer.parseInt(s3.substring(COUNT_BEGIN_INDEX).trim());

        Integer Total = carCount1 + carCount2 + carCount3;

        return Total;
    }
}