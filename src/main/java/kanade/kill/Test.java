package kanade.kill;

import sun.misc.Unsafe;

public class Test {
    public static void main(String[] args) throws Throwable {
        Unsafe u = Unsafe.getUnsafe();
        System.out.println(u);
        System.out.println("114514!\n114514!\n");
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
