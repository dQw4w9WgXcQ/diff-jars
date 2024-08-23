package asdf.diffjars;

import lombok.extern.slf4j.*;
import org.apache.bcel.*;
import org.apache.bcel.classfile.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;

@Slf4j
public class DiffStrings {
    public static final Pattern PATTERN = Pattern.compile("^[a-z]{1,2}\\.[a-zA-Z]+\\($");
    public static final Pattern CLIENT_PATTERN = Pattern.compile("^client\\.[a-z][a-zA-Z]*\\($");
    public static final Pattern INIT_PATTERN = Pattern.compile("^[a-z]{1,2}\\.<init>\\($");
    public static final Pattern FIELD_PATTERN = Pattern.compile("^field\\d{1,4}$");
    public static final List<Pattern> PATTERNS = Arrays.asList(
            PATTERN,
            CLIENT_PATTERN,
            INIT_PATTERN,
            FIELD_PATTERN
    );

    public static Set<String> diffStrings(File file1, File file2) {
        try {
            Set<String> strings1;
            try (JarFile jarFile1 = new JarFile(file1)) {
                strings1 = findAllStrings(jarFile1, DiffStrings.PATTERNS);
            }

            log.info("Found {} strings in old jar {}", strings1.size(), file1);

            Set<String> strings2;
            try (JarFile jarFile2 = new JarFile(file2)) {
                strings2 = findAllStrings(jarFile2, DiffStrings.PATTERNS);
            }

            log.info("Found {} strings in new jar {}", strings2.size(), file2);

            Set<String> newStrings = new HashSet<>();

            for (String string : strings2) {
                if (!strings1.contains(string)) {
                    newStrings.add(string);
                }
            }

            log.info("{} new strings", newStrings.size());

            return newStrings;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static Set<String> findAllStrings(JarFile jarFile, List<Pattern> filterPatterns) throws IOException {
        return filterStrings(findAllStringsRaw(jarFile), filterPatterns);
    }

    private static Set<String> filterStrings(Set<String> strings, List<Pattern> filterPatterns) {
        Set<String> filteredStrings = new HashSet<>();

        outer:
        for (String string : strings) {
            for (Pattern filterPattern : filterPatterns) {
                if (filterPattern.matcher(string).matches()) {
                    continue outer;
                }
            }

            filteredStrings.add(string);
        }

        return filteredStrings;
    }

    private static Set<String> findAllStringsRaw(JarFile jarFile) throws IOException {
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
