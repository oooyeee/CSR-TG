package testes;

import java.security.SecureRandom;
import util.Conversions;
import util.Log;

public class Test2 {
    public static void main(String[] params){

        String temp = "9D5B039267538685681CB35165927F8F9D7E0F2BD8F1EDC5EDB491177C76216C";

        byte[] keyBytes = new byte[32]; // 256 bit
        new SecureRandom().nextBytes(keyBytes);

        String str = Conversions.bytesToHex(keyBytes).replaceAll(" ", "");
        Log.info("bytes:" + str);

        
        byte[] bytes = Conversions.hexStringToBytes(str);
        String check = Conversions.bytesToHex(bytes).replaceAll(" ", "");
        Log.info("check:" + check);
    }
}
