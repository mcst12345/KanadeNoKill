package kanade.kill;

import com.wzz.death.ItemDeath;
import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

public class Test implements Opcodes {
    static Unsafe unsafe;

    public static void main(String[] args) throws Throwable {
        System.out.println(ItemDeath.ALLATORIxDEMO("V\u0003{\u0015^\u0014"));
    }

    private static class A {
        public void test() {
            System.out.println("123");
        }
    }

    private static class B extends A {
        @Override
        public void test() {
            System.out.println("456");
        }
    }
}
