package kanade.kill.util;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class NetworkTool {
    public static String getUserIP() throws IOException {
        HttpGet get = new HttpGet("https://www.yoisaki-kanade.com/ip");
        get.addHeader("User-Agent", "Kanade");
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(get);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
            String line;
            StringBuilder responseSB = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                responseSB.append(line);
            }
            line = responseSB.toString();
            return line;
        }
    }

    public static boolean CheckUser(String s) {
        HttpGet get = new HttpGet("https://verify.yoisaki-kanade.com/" + s);
        get.addHeader("User-Agent", "Kanade");
        try (CloseableHttpClient client = HttpClients.createDefault()) {
            HttpResponse response = client.execute(get);
            BufferedReader reader = new BufferedReader(new InputStreamReader(response.getEntity().getContent(), StandardCharsets.UTF_8));
            String line;
            StringBuilder responseSB = new StringBuilder();
            while ((line = reader.readLine()) != null) {
                responseSB.append(line);
            }
            line = responseSB.toString();
            return line.equals("true");
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

}
