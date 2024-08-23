package asdf.diffjars;

import lombok.extern.slf4j.*;

import java.io.*;
import java.util.*;

@Slf4j
class Main {
    public static void main(String[] args) {
        String path1 = args[0];
        String path2 = args[1];

        File file1 = new File(path1);
        File file2 = new File(path2);

        log.info("Comparing {} and {}", file1, file2);

        Set<String> newStrings = DiffStrings.diffStrings(file1, file2);
        for (String s : newStrings) {
            System.out.println(s);
        }

        Set<String> newCalls = DiffCalls.diffCalls(file1, file2);
        for (String s : newCalls) {
            System.out.println(s);
        }
    }
}
