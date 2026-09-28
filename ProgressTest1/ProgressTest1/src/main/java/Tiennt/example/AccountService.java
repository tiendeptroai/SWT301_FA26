package Tiennt.example;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

public class AccountService {

    public static final int MAX_FAILED_ATTEMPTS = 5;
    public static final int PASSWORD_HISTORY_SIZE = 3;
    public static final int MIN_AGE = 18;

    private final Map<String, Account> accountsByUsername = new HashMap<>(); // key: username lowercase
    private final Map<String, String> usernameByEmail = new HashMap<>();     // email lowercase -> username key

    public AccountService() {
    }

    // ================= Đăng ký (TODO-04) =================
    public ResultCode register(String username, String email, String password,
                               String confirmPassword, LocalDate dateOfBirth, String phone) {
        LocalDate today = LocalDate.now();

        // BR-REG-01: Kiểm tra trống, null hoặc ngày sinh ở tương lai
        if (isBlank(username) || isBlank(email) || isBlank(password) || isBlank(confirmPassword)
                || dateOfBirth == null || dateOfBirth.isAfter(today)) {
            return ResultCode.INVALID_INPUT;
        }

        // BR-REG-02: Kiểm tra định dạng username
        if (!AccountValidator.isValidUsername(username)) {
            return ResultCode.INVALID_USERNAME;
        }

        // BR-REG-04: Kiểm tra định dạng email
        if (!AccountValidator.isValidEmail(email)) {
            return ResultCode.INVALID_EMAIL;
        }

        // BR-REG-06: Kiểm tra mật khẩu (độ dài, ký tự, không chứa username)
        if (!AccountValidator.isValidPassword(password, username)) {
            return ResultCode.WEAK_PASSWORD;
        }

        // BR-REG-07: Xác nhận mật khẩu trùng khớp
        if (!password.equals(confirmPassword)) {
            return ResultCode.PASSWORD_MISMATCH;
        }

        // BR-REG-08: Kiểm tra đủ tuổi (>= 18)
        if (AccountValidator.calculateAge(dateOfBirth, today) < MIN_AGE) {
            return ResultCode.UNDERAGE;
        }

        // BR-REG-09: Số điện thoại (tùy chọn: null hoặc rỗng thì hợp lệ, sai regex thì lỗi)
        if (phone != null && !phone.isEmpty() && !AccountValidator.isValidPhone(phone)) {
            return ResultCode.INVALID_PHONE;
        }

        String userKey = key(username);
        String emailKey = key(email);

        // BR-REG-03: Kiểm tra trùng username
        if (accountsByUsername.containsKey(userKey)) {
            return ResultCode.DUPLICATE_USERNAME;
        }

        // BR-REG-05: Kiểm tra trùng email
        if (usernameByEmail.containsKey(emailKey)) {
            return ResultCode.DUPLICATE_EMAIL;
        }

        // BR-REG-10: Tạo tài khoản với salt và hash SHA-256
        String salt = PasswordHasher.generateSalt();
        Account account = new Account(username, emailKey, dateOfBirth, phone,
                salt, PasswordHasher.hash(salt, password));
        accountsByUsername.put(userKey, account);
        usernameByEmail.put(emailKey, userKey);

        return ResultCode.SUCCESS;
    }

    // Các hàm tra cứu phục vụ test case
    public Optional<Account> findByUsername(String username) {
        if (isBlank(username)) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountsByUsername.get(key(username)));
    }

    public boolean isLocked(String username) {
        return findByUsername(username).map(Account::isLocked).orElse(false);
    }

    // Các hàm cho TODO tiếp theo
    public ResultCode login(String username, String password) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode disableAccount(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    public ResultCode unlockAccount(String username) {
        throw new UnsupportedOperationException("TODO");
    }

    // Helpers
    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String key(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}