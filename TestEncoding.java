import java.nio.charset.StandardCharsets;
import java.util.Arrays;
public class TestEncoding {
    public static void main(String[] args) {
        byte[] gbk = {(byte)0xb5, (byte)0xc7, (byte)0xc2, (byte)0xbc, (byte)0xb3, (byte)0xc9, (byte)0xb9, (byte)0xa6};
        String s = new String(gbk, StandardCharsets.UTF_8);
        System.out.println("Chars: " + Arrays.toString(s.toCharArray()));
        System.out.println("String: " + s);
        System.out.println("UTF-8 bytes: " + Arrays.toString(s.getBytes(StandardCharsets.UTF_8)));
    }
}
