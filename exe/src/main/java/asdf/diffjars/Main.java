package asdf.diffjars;

import lombok.extern.slf4j.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;

@Slf4j
class Main {
    public static void main(String[] args) throws IOException {
        String path1 = args[0];
        String path2 = args[1];

        File file1 = new File(path1);
        File file2 = new File(path2);

        log.info("jar1: {}", file1.getAbsolutePath());
        log.info("jar2: {}", file2.getAbsolutePath());

        Set<String> strings1;
        try (JarFile jarFile1 = new JarFile(file1)) {
            strings1 = DiffStrings.findAllStrings(jarFile1, DiffStrings.PATTERNS);
        }

        Set<String> strings2;
        try (JarFile jarFile2 = new JarFile(file2)) {
            strings2 = DiffStrings.findAllStrings(jarFile2, DiffStrings.PATTERNS);
        }

        int count = 0;
        for (String string : strings2) {
            if (!strings1.contains(string)) {
                System.out.println(string);
                System.out.println();
                count++;
            }
        }

        log.info("{} new strings", count);
    }
}
