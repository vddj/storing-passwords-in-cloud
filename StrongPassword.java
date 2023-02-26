/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package DiplomskiRad;

import java.security.SecureRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.lang3.RandomStringUtils;

/**
 *
 * @author PC
 */
public class StrongPassword {

    public static String generatePassword() {
        int randomStrLength = 20;
        char[] possibleCharacters = ("ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789~`!@#$%^&*()-_=+[{]}\\|;:\'\",<.>/?").toCharArray();
        String randomStr = RandomStringUtils.random(randomStrLength, 0, possibleCharacters.length - 1, false, false, possibleCharacters, new SecureRandom());
        return randomStr;
    }

    public static void main(String args[]) {
        String[] s = {
            "s5v5a5m5a5l5a", "malaVeliko", "dve1brojke2", "specijalni*&znaci", "maloVeliko8vvvv7broj", "maloV56*(_"
        };
        for (int i = 0; i < 6; i++) {
            passwordCheck(s[i]);
        }
    }

    public static String passwordCheck(String pass) {
        Pattern malo = Pattern.compile("[a-z]");
        Pattern veliko = Pattern.compile("[A-Z]");
        Pattern cifra = Pattern.compile("[0-9]");
        Pattern special = Pattern.compile("[\\$\\%\\^\\+\\=\\[\\]\\{\\!\\@\\#\\}\\;\\:\\|\\,\\.\\<\\>\\?\\&\\*\\(\\)\\_]");

        Matcher matcher1 = malo.matcher(pass);
        Matcher matcher2 = veliko.matcher(pass);
        Matcher matcher3 = cifra.matcher(pass);
        Matcher matcher4 = special.matcher(pass);

        int points = 0;
        while (matcher1.find()) {
            points++;
        }
        while (matcher2.find()) {
            points += 2;
        }
        while (matcher3.find()) {
            points += 3;
        }
        while (matcher4.find()) {
            points += 4;
        }

        if (pass.length() > 8) {
            points += 3;
        }
        if (pass.length() > 14) {
            points += 4;
        }
        if (pass.length() > 20) {
            points += 5;
        }
        System.out.println(pass + " : " + points);

        if (points < 20) {
            return "weak";
        }
        if (points < 40) {
            return "medium";
        } else {
            return "strong";
        }
    }
}
