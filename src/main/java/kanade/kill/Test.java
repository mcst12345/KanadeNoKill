package kanade.kill;

import org.objectweb.asm.Opcodes;
import sun.misc.Unsafe;

import javax.crypto.Cipher;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLStreamHandler;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Arrays;

public class Test implements Opcodes {
    static Unsafe unsafe;

    ///root/.gradle/caches/modules-2/files-2.1/software.amazon.eventstream/eventstream/1.0.1/6ff8649dffc5190366ada897ba8525a836297784/eventstream-1.0.1.jar
    public static byte[] encrypt(byte[] data, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.ENCRYPT_MODE, key);
        return cipher.doFinal(data);
    }

    public static byte[] decrypt(byte[] encryptedData, Key key) throws Exception {
        Cipher cipher = Cipher.getInstance("AES");
        cipher.init(Cipher.DECRYPT_MODE, key);
        return cipher.doFinal(encryptedData);
    }


    public static void main(String[] args) throws IOException {
        File file = new File("/root/.minecraft/mods");
        System.out.println(Arrays.toString(file.listFiles()));
    }

    public static class aaa extends URLStreamHandler {
        @Override
        protected URLConnection openConnection(URL u) throws IOException {
            return null;
        }
    }

    /*请求url获取返回的内容*/
    public static String getReturn(HttpURLConnection connection) throws IOException {
        StringBuilder buffer = new StringBuilder();
        //将返回的输入流转换成字符串
        try (InputStream inputStream = connection.getInputStream();
             InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader)) {
            String str;
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            return buffer.toString();
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

    //处理http请求  requestUrl为请求地址  requestMethod请求方式，值为"GET"或"POST"
    public static String httpRequest(String requestUrl, String requestMethod, String outputStr) {
        StringBuilder buffer = null;
        try {
            URL url = new URL(requestUrl);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setDoOutput(true);
            conn.setDoInput(true);
            conn.setRequestMethod(requestMethod);
            conn.connect();
            //往服务器端写内容 也就是发起http请求需要带的参数
            if (null != outputStr) {
                OutputStream os = conn.getOutputStream();
                os.write(outputStr.getBytes(StandardCharsets.UTF_8));
                os.close();
            }

            //读取服务器端返回的内容
            InputStream is = conn.getInputStream();
            InputStreamReader isr = new InputStreamReader(is, StandardCharsets.UTF_8);
            BufferedReader br = new BufferedReader(isr);
            buffer = new StringBuilder();
            String line = null;
            while ((line = br.readLine()) != null) {
                buffer.append(line);
            }
        } catch (Exception ignored) {
        }
        return buffer.toString();
    }
}
