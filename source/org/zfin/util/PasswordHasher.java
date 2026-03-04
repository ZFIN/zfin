package org.zfin.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class PasswordHasher {
    public static void main(String[] args) {
        if (args.length != 1) {
            System.err.println("Usage: java PasswordHasher <password>");
            System.exit(1);
        }

        String password = args[0];
        String hashedPassword = new BCryptPasswordEncoder().encode(password);
        System.out.println(hashedPassword);
    }
}
