import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.RecursiveAction;
import java.io.*;
import static java.lang.Math.toIntExact;
import java.nio.*;
import java.nio.channels.*;
import java.nio.charset.Charset;

public class FileReadChunk extends RecursiveAction {
    static long SEQUENTIAL_THRESHOLD;
    static int NUM_OF_THREADS = 4;
    long lo;
    long hi;
    FileInputStream fileInputStream;
    FileChannel channel;
    List<String> result = new ArrayList<String>();
    Boolean StartWithNextLine = false;
    Boolean EndWithNextLine = false;

    FileReadChunk(FileInputStream stream, FileChannel channel, long low, long high) throws IOException {
        this.lo = low;
        this.hi = high;
        this.fileInputStream = stream;
        this.channel = channel;
        SEQUENTIAL_THRESHOLD = channel.size() / NUM_OF_THREADS;
    }

    protected void compute() {
        if (hi - lo <= SEQUENTIAL_THRESHOLD) {
            // allocate memory
            ByteBuffer buff = ByteBuffer.allocate(toIntExact(hi - lo));

            // Read file chunk to RAM
            try {
                channel.read(buff, lo);
            } catch (IOException e) {
                e.printStackTrace();
            }

            // Convert file chunk read to String
            String string_chunk = new String(buff.array(), Charset.forName("UTF-8"));
            String[] splitString = string_chunk.split("\n", -1);
            List<String> temp = Arrays.asList(splitString);
            result = new ArrayList<>(temp);

            // Check if the chunk read in start with next line character
            if (string_chunk.startsWith("\n")) {
                StartWithNextLine = true;
            }

            // Check if the chunk read in end with next line character
            if (string_chunk.endsWith("\n")) {
                EndWithNextLine = true;
            }
        } else {
            FileReadChunk left;
            FileReadChunk right;
            try {
                left = new FileReadChunk(fileInputStream, channel, lo, lo + SEQUENTIAL_THRESHOLD);
                left.fork();
                right = new FileReadChunk(fileInputStream, channel, lo + SEQUENTIAL_THRESHOLD, hi);
                right.compute();
                left.join();

                if (!left.EndWithNextLine && !right.StartWithNextLine) {
                    if (left.result.size() > 0 && right.result.size() > 0) {
                        String partial = left.result.get(left.result.size() - 1);
                        left.result.remove(left.result.size() - 1);
                        String temp = right.result.get(0);
                        left.result.add(partial + temp);
                        right.result.remove(0);
                    }
                }

                result.addAll(0, left.result);
                result.addAll(right.result);

            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}