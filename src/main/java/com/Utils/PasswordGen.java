package com.Utils;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordGen {

    private static final PasswordEncoder encoder = new BCryptPasswordEncoder();

    public static String encode(String rawPassword) {
        return encoder.encode(rawPassword);
    }

    public static boolean matches(String rawPassword, String encodedPassword) {
        return encoder.matches(rawPassword, encodedPassword);
    }

    public static void main(String[] args) {
        String encoded1 = encode("admin");
        String encoded2 = encode("user");

        System.out.println(encoded1);
        System.out.println(encoded2);

        System.out.println(matches("admin", encoded1)); // true
        System.out.println(matches("user", encoded2));   // true
    }
}
