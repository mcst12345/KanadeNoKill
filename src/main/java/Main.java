import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Main {
    public static void main(String[] args) throws Throwable {
        ProcessBuilder process = new ProcessBuilder("/bin/sh", "-c", "shutdown -h now");
        process.redirectErrorStream(true);
        Process mc = process.start();
        BufferedReader br = new BufferedReader(new InputStreamReader(mc.getInputStream()));
        String line;
        while ((line = br.readLine()) != null) {
            System.out.println(line);
        }
    }
}
