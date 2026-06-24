package io.github.mrergos.gymcrm.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("UserUtils tests")
public class UserUtilsTest {

    private static final String VALID_CHARS =
            "ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789";

    @Test
    @DisplayName("generateUniqueUsername: returns base when not taken")
    void generateUniqueUsername_whenBaseNotTaken_shouldReturnBase() {
        //given
        //when
        String result = UserUtils.generateUniqueUsername("John", "Smith", username -> false);
        //then
        assertEquals("John.Smith", result);
    }

    @Test
    @DisplayName("generateUniqueUsername: adds suffix '1' when base is taken")
    void generateUniqueUsername_whenBaseTaken_shouldAddSuffix1() {
        //given
        Set<String> taken = Set.of("John.Smith");
        //when
        String result = UserUtils.generateUniqueUsername("John", "Smith", taken::contains);
        //then
        assertEquals("John.Smith1", result);
    }

    @Test
    @DisplayName("generateUniqueUsername: increments suffix when multiple taken")
    void generateUniqueUsername_whenMultipleTaken_shouldIncrementSuffix() {
        //given
        Set<String> taken = Set.of("John.Smith", "John.Smith1", "John.Smith2");
        //when
        String result = UserUtils.generateUniqueUsername("John", "Smith", taken::contains);
        //then
        assertEquals("John.Smith3", result);
    }

    @Test
    @DisplayName("generateUniqueUsername: result is always unique")
    void generateUniqueUsername_resultIsAlwaysUnique() {
        //given
        Set<String> taken = new HashSet<>();
        for (int i = 0; i < 5; i++) {
            //when
            String username = UserUtils.generateUniqueUsername("John", "Smith", taken::contains);
            //then
            assertFalse(taken.contains(username), "Duplicate username generated: " + username);
            taken.add(username);
        }
    }

    @Test
    @DisplayName("generatePassword: length is exactly 10")
    void generatePassword_shouldHaveLengthTen() {
        //given
        //when
        //then
        assertEquals(10, UserUtils.generatePassword().length());
    }

    @Test
    @DisplayName("generatePassword: contains only valid characters")
    void generatePassword_shouldContainOnlyValidChars() {
        //given
        //when
        String password = UserUtils.generatePassword();
        //then
        for (char c : password.toCharArray()) {
            assertTrue(VALID_CHARS.indexOf(c) >= 0,
                    "Unexpected char '" + c + "' in password");
        }
    }

    @RepeatedTest(5)
    @DisplayName("generatePassword: produces different values on each call (randomness check)")
    void generatePassword_shouldBeRandom() {
        //given
        Set<String> passwords = new HashSet<>();
        //when
        for (int i = 0; i < 10; i++) {
            passwords.add(UserUtils.generatePassword());
        }
        //then
        assertTrue(passwords.size() > 1, "All 10 passwords were identical - randomness broken");
    }
}
