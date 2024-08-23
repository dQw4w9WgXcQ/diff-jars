package asdf.diffjars;

import lombok.extern.slf4j.*;
import org.objectweb.asm.*;
import org.objectweb.asm.tree.*;

import java.io.*;
import java.util.*;
import java.util.jar.*;
import java.util.regex.*;

@Slf4j
public class DiffCalls {
    public static Set<String> diffCalls(File file1, File file2) {
        Set<String> calls1;
        try (JarFile jarFile1 = new JarFile(file1)) {
            calls1 = findAllCalls(jarFile1);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Set<String> calls2;
        try (JarFile jarFile2 = new JarFile(file2)) {
            calls2 = findAllCalls(jarFile2);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Set<String> newCalls = new HashSet<>();
        for (String call : calls2) {
            if (!calls1.contains(call)) {
                newCalls.add(call);
            }
        }

        log.info("Found {} new calls", newCalls.size());

        return newCalls;
    }

    public static Set<String> findAllCalls(JarFile jarFile) {
        List<ClassNode> classes = new ArrayList<>();
        Enumeration<JarEntry> entries = jarFile.entries();
        while (entries.hasMoreElements()) {
            JarEntry entry = entries.nextElement();
            if (entry.getName().endsWith(".class")) {
                ClassReader cr;
                try {
                    cr = new ClassReader(jarFile.getInputStream(entry));
                } catch (IOException e) {
                    throw new RuntimeException(e);
                }
                ClassNode cn = new ClassNode();
                cr.accept(cn, 0);
                classes.add(cn);
            }
        }

        Set<String> allCalls = new HashSet<>();//signature descriptions
        for (ClassNode cn : classes) {
            allCalls.addAll(findCalls(cn));
        }
        return allCalls;
    }

    public static final Pattern RL_OWNER_PATTERN = Pattern.compile("^rl\\d+$");

    public static Set<String> findCalls(ClassNode cn) {
        Set<String> calls = new HashSet<>();
        for (MethodNode mn : cn.methods) {
            for (AbstractInsnNode insn : mn.instructions) {
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode methodInsn = (MethodInsnNode) insn;

                    String methodOwner = methodInsn.owner;
                    String methodName = methodInsn.name;
                    String methodDesc = methodInsn.desc;

                    if (RL_OWNER_PATTERN.matcher(methodOwner).matches()) {
                        methodOwner = "RUNELITE";
                    }

                    if (methodOwner.length() == 2 || methodOwner.equals("client")) {
                        methodOwner = "OBF";
                    }

                    if (methodName.length() == 2 ||
                            (methodName.length() == 3 &&
                                    methodName.charAt(0) == 'a' &&
                                    Character.isLowerCase(methodName.charAt(1)) &&
                                    Character.isLowerCase(methodName.charAt(2)))) {
                        methodName = "OBF";
                    }

                    calls.add(methodOwner + "." + methodName);// + methodDesc); todo rename OBF in desc
                }
            }
        }
        return calls;
    }
}
