package miku.lib.jvm.hotspot.interpreter;

import miku.lib.jvm.hotspot.oops.Method;

public interface BytecodeVisitor {
    void prologue(Method var1);

    void visit(Bytecode var1);

    void epilogue();
}