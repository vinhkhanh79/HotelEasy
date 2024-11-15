package com.datn.tourhotel.security;

import java.security.SecureRandom;

public class RandomStringGenerator {
    private static final String CHARACTERS = "abcdefghijklmnopqrstuvwxyz123456789";
    private static final int LENGTH = 16;

    public String generateRandomString() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder(LENGTH);
        for (int i = 0; i < LENGTH; i++) {
            int index = random.nextInt(CHARACTERS.length());
            sb.append(CHARACTERS.charAt(index));
        }
        return sb.toString();
    }
}
