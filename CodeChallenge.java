import java.io.IOException;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.concurrent.ForkJoinPool;
import java.io.*;
import java.nio.channels.*;

/*
 * Task 1: Number of cars seen in total
 * Task 2: A sequence of lines (date format) and the number of cars seen on the day for all days
 * Task 3: The top 3 half hours with most cars
 * Task 4: The 1.5 hour period with least cars
 */
public class CodeChallenge {
    private static List<String> inputArray = new ArrayList<String>();
    private static HashMap<String, Integer> map = new HashMap<>(); //For Task 1,2
    private static HashMap<Integer, String> occuranceMap = new HashMap<>(); //For Task 3
    private static final Integer TOP_THREE_INDEX = 3;

    public static void main(String[] args) throws IOException {
        try {
            long startTime = System.currentTimeMillis();
            // Slow way to read file
            // File file = new File("LargeInput.txt");
            // inputArray = Files.readAllLines(file.toPath());

            // Get file name from terminal and feed into file input stream
            FileInputStream fileInputStream = new FileInputStream(args[0]);
            FileChannel channel = fileInputStream.getChannel();
            // get the total number of bytes in the file
            long total_size = channel.size();

            // Read the whole file with fork-join pool method
            FileReadWhole(fileInputStream, channel, 0, total_size);

            long endTime = System.currentTimeMillis();
            System.out.println("Total execution time to read file: " + (endTime - startTime) + "ms");

            /* For Task 1 and Task 2, do Task 2 first, then re-use the sub total result (output as a map having subtotal records),
            Add them up to get the total sum for Task 1
            And continue to display result for Task 2*/
            startTime = System.currentTimeMillis();
            map = DaySumMap(inputArray);
            endTime = System.currentTimeMillis();
            // System.out.println("Total Day Sum execution time: " + (endTime - startTime) +"ms");

            /*Task 1: Output the numbers of cars seen in total*/
            TotalSum(map);

            /*Task2: Sequence of line contains a date 
            Use tree map since tree map can do the sort while inserting the map  */
            TreeMap<String, Integer> sorted = new TreeMap<>();
            sorted.putAll(map);

            // Display the TreeMap which is naturally sorted
            System.out.println("Task 2:");
            for (Map.Entry<String, Integer> entry : sorted.entrySet()) {
                System.out.println(entry.getKey() + " " + entry.getValue());
            }
            
            /*Task 3: The top 3 half hours with most cars
            Similar to Task 2, but change the hashmap pattern and included comparator 
            So the tree map can be built in reversed order*/
            occuranceMap = OccuranceToDayMap(inputArray);
            TreeMap<Integer, String> sortedOccurance = new TreeMap<>(new Comparator<Integer>() {
                @Override
                public int compare(Integer s1, Integer s2) {
                    return s2.compareTo(s1);
                }
            });;

            // Copy all data from hashMap into TreeMap
            sortedOccurance.putAll(occuranceMap);
            System.out.println("Task 3:");
            // Display the TreeMap which is naturally sorted
            Integer i = 0;
            for (Map.Entry<Integer, String> entry : sortedOccurance.entrySet()) {
                if (i < TOP_THREE_INDEX){
                    System.out.println(entry.getValue() + " " + entry.getKey());
                    i += 1;
                }
                else{ //Only need to display top 3 half hours with most cars, so break after displaying the first 3 records.
                    break;
                }
            }

            // Task 4: The 1.5 hour period with least cars
            List<String> LeastConsecutive = LeastThreeConsecutive(inputArray);
            System.out.println("Task 4:");
            for(String s : LeastConsecutive)
            {
                System.out.println(s);
            }

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Task 1 helper function
    public static int TotalSum(HashMap<String, Integer> map) {
        int TotalSum = 0;
        for (int subtotal : map.values()) {
            TotalSum += subtotal;
        }
        System.out.println("Task 1:");
        System.out.println("The numbers of cars seen in total: " + TotalSum);

        return TotalSum;
    }

    // Task 1,2 fork-join function
    public static HashMap<String, Integer> DaySumMap(List<String> x) {
        DayMap m = new DayMap(x, 0, x.size());
        ForkJoinPool.commonPool().invoke(m);
        HashMap<String, Integer> map = m.map;

        return map;
    }

    // Task 3 fork-join function
    public static HashMap<Integer, String> OccuranceToDayMap(List<String> x) {
        OccuranceMap m = new OccuranceMap(x, 0, x.size());
        ForkJoinPool.commonPool().invoke(m);
        HashMap<Integer, String> map = m.map;

        return map;
    }

    // Task 4 fork-join function
    public static List<String> LeastThreeConsecutive(List<String> x) {
        LeastThreeConsecutive m = new LeastThreeConsecutive(x, 0, x.size());
        ForkJoinPool.commonPool().invoke(m);
        List<String> result = m.result;

        return result;
    }

    // Fork-join function to read the whole file
    public static void FileReadWhole(FileInputStream stream, FileChannel channel, long low, long high) {
        try {
            FileReadChunk c = new FileReadChunk(stream, channel, low, high);
            ForkJoinPool.commonPool().invoke(c);
            inputArray = c.result;
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    

    // Back up code, in the case of the change of requirement, this is the most
    // direct way to feed in list of strings
    // from the given input, and use fork-join method to add up all the results
    // public static int TotalSum(List<String> x) {
    // SumArray t = new SumArray(x, 0, x.size());
    // ForkJoinPool.commonPool().invoke(t);
    // int sum = t.result;
    // System.out.println("Total Sum: " + sum);

    // return sum;
    // }

    //Constructed for task 1, but not in use atm (leave this for future reference in the case of needing to extend the function)
    // private static class SumArray extends RecursiveAction {
    // static int SEQUENTIAL_THRESHOLD = 3;
    // int lo;
    // int hi;
    // List<String> arr;
    // int result = 0;

    // SumArray(List<String> arr, int low, int high) {
    // this.lo = low;
    // this.hi = high;
    // this.arr = arr;
    // }

    // protected void compute() {
    // if (hi - lo <= SEQUENTIAL_THRESHOLD) {
    // try {
    // for (int i = lo; i < hi; i++) {
    // String num = arr.get(i).split(" ")[1];
    // result += Integer.parseInt(num.trim());
    // }
    // } catch (Exception ex) {
    // ex.printStackTrace();
    // }
    // } else {
    // SumArray left = new SumArray(arr, lo, (hi + lo) / 2);
    // SumArray right = new SumArray(arr, (hi + lo) / 2, hi);
    // left.fork();
    // right.compute();
    // left.join();
    // result = left.result + right.result;
    // }
    // }
    // }


}
