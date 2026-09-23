package Tiennt.example;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvFileSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class AccountServiceTest {

    private AccountService service;

    @BeforeEach
    void setUp() {
        service = new AccountService();
    }

    // ---------- isValidEmail ----------

    @ParameterizedTest(name = "Email hop le: {0}")
    @ValueSource(strings = {
            "john@example.com",
            "alice.b@mail.co.uk",
            "carol_99@domain.io"
    })
    @DisplayName("isValidEmail tra ve true voi email dung dinh dang")
    void isValidEmail_ValidEmails_ReturnsTrue(String email) {

        // Act
        boolean result = service.isValidEmail(email);

        // Assert
        assertTrue(result);
    }

    @ParameterizedTest(name = "Email khong hop le: \"{0}\"")
    @CsvSource(value = {
            "bobmail.com",
            "missing@dot",
            "'@nodomain.com'",
            "' '",
            "NULL"
    }, nullValues = "NULL")
    @DisplayName("isValidEmail tra ve false voi email sai dinh dang / null")
    void isValidEmail_InvalidEmails_ReturnsFalse(String email) {

        // Act
        boolean result = service.isValidEmail(email);

        // Assert
        assertFalse(result);
    }

    // ---------- registerAccount ----------

    @ParameterizedTest(name = "Row {index}: ({0},{1},{2}) -> {3}")
    @CsvFileSource(resources = "/test-data.csv", numLinesToSkip = 1)
    @DisplayName("registerAccount voi du lieu tu test-data.csv")
    void registerAccount_FromCsv(
            String username,
            String password,
            String email,
            boolean expected) {

        // Act
        boolean actual =
                service.registerAccount(username, password, email);

        // Assert
        assertEquals(expected, actual,
                () -> String.format(
                        "(%s,%s,%s) phai tra ve %s",
                        username, password, email, expected
                )
        );
    }

    // ---------- Edge cases ----------

    @Test
    @DisplayName("registerAccount: password = 6 ky tu -> false")
    void registerAccount_PasswordExactly6_ReturnsFalse() {

        // Arrange
        String username = "bob";
        String password = "abcdef";
        String email = "bob@mail.com";

        // Act
        boolean actual =
                service.registerAccount(username, password, email);

        // Assert
        assertFalse(actual, "password phai > 6 ky tu");
    }

    @Test
    @DisplayName("registerAccount: password = 7 ky tu -> true")
    void registerAccount_PasswordExactly7_ReturnsTrue() {

        assertTrue(
                service.registerAccount(
                        "bob",
                        "abcdefg",
                        "bob@mail.com"
                )
        );
    }

    @Test
    @DisplayName("registerAccount: tat ca tham so null -> false")
    void registerAccount_AllNull_ReturnsFalse() {

        assertFalse(
                service.registerAccount(null, null, null)
        );
    }
}