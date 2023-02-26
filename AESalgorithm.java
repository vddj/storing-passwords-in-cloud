/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DiplomskiRad;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Paths;
import java.security.NoSuchAlgorithmException;
import java.security.spec.AlgorithmParameterSpec;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.KeySpec;
import java.util.Base64;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.crypto.Cipher;
import javax.crypto.CipherInputStream;
import javax.crypto.CipherOutputStream;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.PBEKeySpec;
import javax.crypto.spec.SecretKeySpec;

public class AESalgorithm {

    private Cipher ecipher;
    private Cipher dcipher;
    // Buffer used to transport the bytes from one stream to another
    private int sizeOfBuff = 1024;
    private byte[] buf = new byte[sizeOfBuff];

    public AESalgorithm(SecretKey key) {
        // Create an 8-byte initialization vector
        byte[] iv = new byte[]{
            0x00, 0x01, 0x02, 0x03, 0x04, 0x05, 0x06, 0x07, 0x08, 0x09, 0x0a, 0x0b, 0x0c, 0x0d, 0x0e, 0x0f
        };

        AlgorithmParameterSpec paramSpec = new IvParameterSpec(iv);
        try {
            ecipher = Cipher.getInstance("AES/CBC/PKCS5Padding");
            dcipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            // CBC requires an initialization vector
            ecipher.init(Cipher.ENCRYPT_MODE, key, paramSpec);
            dcipher.init(Cipher.DECRYPT_MODE, key, paramSpec);
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //json -> bin
    public static void encryptFile(String password, String salt) {
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey originalKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            AESalgorithm encrypter = new AESalgorithm(originalKey);

            encrypter.encrypt(new FileInputStream("../app/src/main/resources/example.json"),
                    new FileOutputStream("../app/src/main/resources/example.bin"), password, salt);

            System.out.println("AESalgorithm -> encryptFile -> fajl example.json enkriptovan u example.bin");
            System.out.println("\t password: " + password + ", salt: " + salt);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | FileNotFoundException ex) {
            Logger.getLogger(AESalgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    // bin -> json
    public static boolean decryptFile(String password, String salt) {
        boolean ret = false;
        try {
            SecretKeyFactory factory = SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");
            KeySpec spec = new PBEKeySpec(password.toCharArray(), salt.getBytes(), 65536, 256);
            SecretKey originalKey = new SecretKeySpec(factory.generateSecret(spec).getEncoded(), "AES");

            AESalgorithm encrypter = new AESalgorithm(originalKey);

            ret = encrypter.decrypt(new java.io.File("../app/src/main/resources/example.bin"),
                    new FileOutputStream("../app/src/main/resources/example.json"), password, salt);

            System.out.println("AESalgorithm -> decryptFile -> fajl example.bin dekriptovan u example.json");
            System.out.println("\t password: " + password + ", salt: " + salt);

        } catch (NoSuchAlgorithmException | InvalidKeySpecException | FileNotFoundException ex) {
            Logger.getLogger(AESalgorithm.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret;
    }

    private void encrypt(InputStream in, OutputStream out, String password, String salt) {
        try {
            InputStream mediumInputStream = new FileInputStream("../app/src/main/resources/medium.bin");
            OutputStream mediumOutputStream = new FileOutputStream("../app/src/main/resources/medium.bin");
            java.io.File nekifajl = new java.io.File("../app/src/main/resources/medium.bin");
            mediumOutputStream = new CipherOutputStream(mediumOutputStream, ecipher);

            byte[] data = java.nio.file.Files.readAllBytes(Paths.get("../app/src/main/resources/example.json"));
            int numRead;
            while ((numRead = in.read(buf)) >= 0) {
                mediumOutputStream.write(buf, 0, numRead);
            }
            byte[] MACvalue = MACalgorithm.calculateMAC(data, password, salt);
            mediumOutputStream.write(MACvalue);
            mediumOutputStream.close();

            byte[] arr = new byte[(int) nekifajl.length()];
            mediumInputStream.read(arr);
            mediumInputStream.close();
            byte[] actualByte = Base64.getEncoder().encode(arr);
            out.write(actualByte);
            out.close();

        } catch (java.io.IOException e) {
            System.err.println("AESalgorithm -> encrypt -> GRESKA");
            e.printStackTrace();
        }
    }

    private boolean decrypt(java.io.File inFile, OutputStream out, String password, String salt) {
        try {
            InputStream mediumInputStream = new FileInputStream("../app/src/main/resources/medium.bin");
            OutputStream mediumOutputStream = new FileOutputStream("../app/src/main/resources/medium.bin");

            FileInputStream mediumInputStream_1 = new FileInputStream("../app/src/main/resources/medium_1.bin");
            OutputStream mediumOutputStream_1 = new FileOutputStream("../app/src/main/resources/medium_1.bin");

            byte[] arr;
            try ( FileInputStream fl = new FileInputStream(inFile)) {
                arr = new byte[(int) inFile.length()];
                fl.read(arr);
            }

            byte[] actualByte = Base64.getDecoder().decode(arr);
            mediumOutputStream.write(actualByte);

            mediumOutputStream.close();
            mediumInputStream = new CipherInputStream(mediumInputStream, dcipher);

            int numRead = 0;
            while ((numRead = mediumInputStream.read(buf)) >= 0) {
                mediumOutputStream_1.write(buf, 0, numRead);
            }
            mediumOutputStream_1.close();
            java.io.File inFile2 = new java.io.File("../app/src/main/resources/medium_1.bin");
            arr = new byte[(int) inFile2.length()];

            mediumInputStream_1.read(arr);

            byte[] arr2 = new byte[(int) inFile2.length() - 32];
            System.arraycopy(arr, 0, arr2, 0, arr2.length);

            int pos = arr.length - 32;
            byte[] MACvalue = new byte[32];
            System.arraycopy(arr, pos, MACvalue, 0, 32);

            out.write(arr, 0, arr.length - 32);
            out.close();

            boolean MACcheck = MACalgorithm.compareMAC(arr2, MACvalue, password, salt);

            return true;
        } catch (java.io.IOException e) {
            System.err.println("AESalgorithm -> decrypt -> GRESKA");
            //e.printStackTrace();
            return false;
        }
    }

    public static void main(String args[]) {

    }

}
