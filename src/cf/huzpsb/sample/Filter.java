package cf.huzpsb.sample;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class Filter {
    private static final List<String> ses;

    static {
        ses = new LinkedList<>();
        Scanner scanner;
        try {
            scanner = new Scanner(new File(Main.dir, "filter.txt"), "UTF-8");
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
        while (scanner.hasNextLine()) {
            String str = scanner.nextLine();
            ses.add(str);
        }
    }

    public static boolean isBanned(String msg) {
        for (String se : ses) {
            if (msg.contains(se)) {
                return true;
            }
        }
        return false;
    }
}
