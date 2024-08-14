package asdf;

import org.apache.bcel.Const;
import org.apache.bcel.classfile.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Pattern;

public class Main {
    public static final Pattern pattern = Pattern.compile("^[a-z]{1,2}\\.[a-zA-Z]+\\($");
    public static final Pattern patternClient = Pattern.compile("^client\\.[a-z][a-zA-Z]*\\($");

    public static void main(String[] args) throws IOException {
        String path1 = args[0];
        String path2 = args[1];

        File file1 = new File(path1);
        File file2 = new File(path2);

        Set<String> strings1;
        try (JarFile jarFile1 = new JarFile(file1)) {
            strings1 = findAllStrings(jarFile1);
        }

        Set<String> strings2;
        try (JarFile jarFile2 = new JarFile(file2)) {
            strings2 = findAllStrings(jarFile2);
        }

        strings1 = filter(strings1);
        strings2 = filter(strings2);

        int count1 = 0;
        for (String string : strings1) {
            if (!strings2.contains(string)) {
                System.out.println(string);
            }
            count1++;
        }

        System.out.println("Found " + count1 + " strings in jar1 that are not in jar2");

        int count2 = 0;
        for (String string : strings2) {
            if (!strings1.contains(string)) {
                System.out.println(string);
                count2++;
            }
        }
        System.out.println("found " + count2 + " strings in jar2 that are not in jar1");
    }

    static Set<String> filter(Set<String> strings) {
        Set<String> filteredStrings = new HashSet<>();
        for (String string : strings) {
            if (pattern.matcher(string).matches() || patternClient.matcher(string).matches()) {
                continue;
            }

            filteredStrings.add(string);
        }
        return filteredStrings;
    }

    static Set<String> findAllStrings(JarFile jarFile) throws IOException {
        Set<String> strings = new HashSet<>();

        Enumeration<JarEntry> entries = jarFile.entries();

        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                InputStream input = jarFile.getInputStream(entry);
                ClassParser parser = new ClassParser(input, entry.getName());
                JavaClass javaClass = parser.parse();
                ConstantPool constantsPool = javaClass.getConstantPool();
                Constant[] constants = constantsPool.getConstantPool();
                for (Constant constant : constants) {
                    if (constant == null) continue;

                    if (constant.getTag() != Const.CONSTANT_String) {
                        continue;
                    }

                    ConstantString constantString = (ConstantString) constant;
                    int stringIndex = constantString.getStringIndex();

                    String string = constantsPool.constantToString(stringIndex, Const.CONSTANT_Utf8);
                    strings.add(string);
                }
            }
        }

        return strings;
    }
}
