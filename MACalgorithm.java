/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DiplomskiRad;

import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Arrays;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

/**
 *
 * @author PC
 */
public class MACalgorithm {

    public static byte[] calculateMAC(byte[] data, String MACkey, String MACsalt) {
        try {
            String pass = "qwertyujnbhuvloiuyt566543dcvtyuiioplkjhgfdsazxcvbnm,;;98765432wer";
            String salt = "hbgvfcxewz34567890jiungfder6789omjhvgcfxesr67890p[;l,mnbfgfcrxezw";
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(pass.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey originalKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            //Creating a Mac object
            Mac mac = Mac.getInstance("HmacSHA256");
            //Initializing the Mac object
            mac.init(originalKey);
            //Computing the Mac
            byte[] macResult = mac.doFinal(data);
            byte[] mac64 = Base64.getEncoder().encode(macResult);
            System.out.println("MAC value: " + new String(mac64) + ", length: " + macResult.length);
            return macResult;
        } catch (NoSuchAlgorithmException | InvalidKeySpecException | InvalidKeyException ex) {
            Logger.getLogger(AESalgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static boolean compareMAC(byte[] text1, byte[] MACvalue, String password, String salt) {
        byte[] data = MACalgorithm.calculateMAC(text1, password, salt);

        byte[] mac64a = Base64.getEncoder().encode(data);
        System.out.println("MAC value_a: " + new String(mac64a));
        byte[] mac64b = Base64.getEncoder().encode(MACvalue);
        System.out.println("MAC value_b: " + new String(mac64b));

        if (Arrays.equals(data, MACvalue)) {
            System.out.println("MAC OK");
            return true;
        } else {
            System.out.println("MAC NOT OK");
            return false;
        }
    }

}
