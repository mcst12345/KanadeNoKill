package kanade.kill;

public class Empty {
    public static void main(String[] arg) {
        String s = "()Ljava/lang/ClassLoader;";
        System.out.println(s.substring(s.length() - 1));
        s = s.replace('/', '.');
        if (s.endsWith(";")) {
            s = s.substring(s.lastIndexOf(")L") + 2, s.length() - 1);
        }
        System.out.println(s);
    }
}
