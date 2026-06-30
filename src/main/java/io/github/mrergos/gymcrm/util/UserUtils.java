package io.github.mrergos.gymcrm.util;


import java.security.SecureRandom;
import java.util.function.Predicate;

public final class UserUtils {
    private static final SecureRandom RANDOM = new SecureRandom();

    private static final String USERNAME_SEPARATOR = ".";
    private static final int PASSWORD_LENGTH = 10;
    private static final String PASSWORD_ALPHABET = "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    private UserUtils() {}

    public static String generatePassword() {
        return RANDOM.ints(PASSWORD_LENGTH, 0, PASSWORD_ALPHABET.length())
                .mapToObj(PASSWORD_ALPHABET::charAt)
                .collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
                .toString();
    }

    public static String generateUniqueUsername(String firstName, String lastName,
                                                Predicate<String> usernameExists) {
        String baseUsername = String.join(USERNAME_SEPARATOR,firstName, lastName);
        if (!usernameExists.test(baseUsername)) {
            return baseUsername;
        }

        String username;
        int serialNumber = 1;
        do {
            username = baseUsername + serialNumber++;
        }
        while (usernameExists.test(username));

        return username;
    }
}
