package kanade.kill;

import java.io.File;

public class Test {
    public static void main(String[] args) {
        String s = "wrwecfmods/sacv acd.jar";
        System.out.println(s.substring(s.lastIndexOf(File.separator) - 4, s.lastIndexOf(File.separator)));
    }
}
