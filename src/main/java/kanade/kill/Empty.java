package kanade.kill;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

public class Empty implements Opcodes {
    public static void main(String[] args) {
        ClassNode cn = new ClassNode(ASM5);
        ClassNode changedClass = new ClassNode(ASM5);
        changedClass.methods.add(new MethodNode(ACC_PUBLIC, "abc", "()V", null, null));
        changedClass.methods.add(new MethodNode(ACC_PUBLIC, "def", "()V", null, null));
        System.out.println(cn.methods);

        final List<MethodNode> methods = new ArrayList<>(changedClass.methods);
        for (MethodNode mn1 : changedClass.methods) {
            for (MethodNode mn2 : cn.methods) {
                if (mn1.name.equals(mn2.name) && mn1.desc.equals(mn2.desc)) {
                    methods.remove(mn1);
                }
            }
        }
        cn.methods.addAll(methods);
        final List<FieldNode> fields = new ArrayList<>(changedClass.fields);
        for (FieldNode fn1 : changedClass.fields) {
            for (FieldNode fn2 : cn.fields) {
                if (fn1.name.equals(fn2.name) && fn1.desc.equals(fn2.desc)) {
                    fields.remove(fn1);
                }
            }
        }
        cn.fields.addAll(fields);

        System.out.println(cn.methods);
    }
}
