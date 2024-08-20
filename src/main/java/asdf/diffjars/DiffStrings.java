package asdf.diffjars;

import org.apache.bcel.*;
import org.apache.bcel.classfile.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;

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
