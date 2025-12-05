import java.io.*;
import java.util.*;

public class day05AppJava {
    static List<long[]> arrMrg = new ArrayList<>();

    // binary search: check if nr lies in any merged interval
    static boolean searchNrBN(long nr) {
        int lo = 0, hi = arrMrg.size() - 1;
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            long l = arrMrg.get(mid)[0];
            long r = arrMrg.get(mid)[1];
            if (nr < l) {
                hi = mid - 1;
            } else if (nr > r) {
                lo = mid + 1;
            } else {
                return true; // inside interval
            }
        }
        return false;
    }

    // insert [l,r] into arrMrg, merging overlaps
    static void pushMergeBinSrch(long l, long r) {
        int lo = 0, hi = arrMrg.size() - 1;
        int pos = arrMrg.size();
        while (lo <= hi) {
            int mid = (lo + hi) / 2;
            if (arrMrg.get(mid)[1] >= l) {
                pos = mid;
                hi = mid - 1;
            } else {
                lo = mid + 1;
            }
        }

        if (pos == arrMrg.size() || arrMrg.get(pos)[0] > r) {
            arrMrg.add(pos, new long[]{l, r});
            return;
        }

        long newL = Math.min(arrMrg.get(pos)[0], l);
        long newR = Math.max(arrMrg.get(pos)[1], r);
        int j = pos + 1;
        while (j < arrMrg.size() && arrMrg.get(j)[0] <= newR) {
            newR = Math.max(newR, arrMrg.get(j)[1]);
            j++;
        }
        // replace arrMrg[pos..j-1] with merged interval
        for (int k = j - 1; k >= pos; k--) {
            arrMrg.remove(k);
        }
        arrMrg.add(pos, new long[]{newL, newR});
    }

    public static void main(String[] args) throws Exception {
        List<String> arr = new ArrayList<>();
        try (BufferedReader br = new BufferedReader(new FileReader("input.txt"))) {
            String line;
            while ((line = br.readLine()) != null) {
                arr.add(line);
            }
        }

        // find blank line index
        int idx = -1;
        for (int i = 0; i < arr.size(); i++) {
            if (arr.get(i).isEmpty()) {
                idx = i;
                break;
            }
        }

        // build merged intervals from arr[0..idx-1]
        for (int i = 0; i < idx; i++) {
            String[] parts = arr.get(i).split("-");
            long l = Long.parseLong(parts[0]);
            long r = Long.parseLong(parts[1]);
            pushMergeBinSrch(l, r);
        }

        // test1: count numbers inside intervals
        long sum = 0;
        for (int i = idx + 1; i < arr.size(); i++) {
            long nr = Long.parseLong(arr.get(i));
            if (searchNrBN(nr)) sum++;
        }

        // test2: total length of merged intervals
        long sum2 = 0;
        for (long[] p : arrMrg) {
            sum2 += p[1] - p[0] + 1;
        }

        System.out.println("[" + sum + ", " + sum2 + "]");
    }
}
