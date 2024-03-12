package kanade.kill;

import sun.misc.Unsafe;

public class Test {
    public static void main(String[] args) throws Throwable {
        for(int i = 0 ; i <= 360; i++){
            double tmp = i * 0.017453292519943295;
            double tmp1 = Math.cosh(tmp)*Math.cosh(tmp)*Math.cosh(tmp);
            double tmp2 = Math.sinh(tmp)*Math.sinh(tmp)*Math.sinh(tmp);
            System.out.println(tmp1*2.5+":"+tmp2*2.5);
            //world.spawnParticle(type,X - tmp1*2.5 - 5,y,Z - tmp2*2.5 - 5,tmp1*0.14514,0,tmp2*0.14514);
            //world.spawnParticle(type,X + tmp1*2.5 + 5,y,Z - tmp2*2.5 + 5,tmp1*0.14514,0,tmp2*0.14514);
        }
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
