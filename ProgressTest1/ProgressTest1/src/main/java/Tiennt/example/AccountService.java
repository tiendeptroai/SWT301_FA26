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

    // ================= Đăng ký (TODO-4) =================
    public ResultCode register(String username, String email, String password,
                               String confirmPassword, LocalDate dateOfBirth, String phone) {
        LocalDate today = LocalDate.now();

        // BR-REG-01
        if (isBlank(username) || isBlank(email) || isBlank(password) || isBlank(confirmPassword)
                || dateOfBirth == null || dateOfBirth.isAfter(today)) {
            return ResultCode.INVALID_INPUT;
        }

        // BR-REG-02
        if (!AccountValidator.isValidUsername(username)) {
            return ResultCode.INVALID_USERNAME;
        }

        // BR-REG-04
        if (!AccountValidator.isValidEmail(email)) {
            return ResultCode.INVALID_EMAIL;
        }

        // BR-REG-06
        if (!AccountValidator.isValidPassword(password, username)) {
            return ResultCode.WEAK_PASSWORD;
        }

        // BR-REG-07
        if (!password.equals(confirmPassword)) {
            return ResultCode.PASSWORD_MISMATCH;
        }

        // BR-REG-08
        if (AccountValidator.calculateAge(dateOfBirth, today) < MIN_AGE) {
            return ResultCode.UNDERAGE;
        }

        // BR-REG-09
        if (phone != null && !phone.isEmpty() && !AccountValidator.isValidPhone(phone)) {
            return ResultCode.INVALID_PHONE;
        }

        String userKey = key(username);
        String emailKey = key(email);

        // BR-REG-03
        if (accountsByUsername.containsKey(userKey)) {
            return ResultCode.DUPLICATE_USERNAME;
        }

        // BR-REG-05
        if (usernameByEmail.containsKey(emailKey)) {
            return ResultCode.DUPLICATE_EMAIL;
        }

        // BR-REG-10
        String salt = PasswordHasher.generateSalt();
        Account account = new Account(username, emailKey, dateOfBirth, phone,
                salt, PasswordHasher.hash(salt, password));
        accountsByUsername.put(userKey, account);
        usernameByEmail.put(emailKey, userKey);

        return ResultCode.SUCCESS;
    }

    // ================= Đăng nhập (TODO-6) =================
    // Thứ tự: LOG-01 -> tồn tại -> DISABLED -> đang khóa -> kiểm tra mật khẩu -> thành công
    public ResultCode login(String username, String password) {
        // BR-LOG-01
        if (isBlank(username) || isBlank(password)) {
            return ResultCode.INVALID_INPUT;
        }

        // BR-LOG-02, 03: Kiểm tra tài khoản tồn tại (không tiết lộ lý do)
        Account account = accountsByUsername.get(key(username));
        if (account == null) {
            return ResultCode.INVALID_CREDENTIALS;
        }

        // BR-LOG-04: Kiểm tra trạng thái DISABLED
        if (account.getStatus() == AccountStatus.DISABLED) {
            return ResultCode.ACCOUNT_DISABLED;
        }

        // BR-LOG-06: Đang bị khóa -> từ chối và KHÔNG tăng bộ đếm
        if (account.isLocked()) {
            return ResultCode.ACCOUNT_LOCKED;
        }

        // BR-LOG-03, 05: Kiểm tra mật khẩu
        if (!PasswordHasher.matches(account.getSalt(), password, account.getCurrentPasswordHash())) {
            account.incrementFailedAttempts();
            // Lần sai thứ 5 (>= 5) thì khóa tài khoản
            if (account.getFailedAttempts() >= MAX_FAILED_ATTEMPTS) {
                account.lock();
                return ResultCode.ACCOUNT_LOCKED;
            }
            return ResultCode.INVALID_CREDENTIALS;
        }

        // BR-LOG-08: Thành công -> reset số lần thất bại về 0
        account.resetFailedAttempts();
        return ResultCode.SUCCESS;
    }

    // ================= Quản trị & truy vấn (TODO-6) =================
    public ResultCode disableAccount(String username) {
        Optional<Account> account = findByUsername(username);
        if (account.isEmpty()) {
            return ResultCode.USER_NOT_FOUND;
        }
        account.get().setStatus(AccountStatus.DISABLED);
        return ResultCode.SUCCESS;
    }

    /**
     * BR-ADM-03: Mở khóa tài khoản bị khóa do nhập sai mật khẩu (reset về 0 và locked = false)
     */
    public ResultCode unlockAccount(String username) {
        Optional<Account> account = findByUsername(username);
        if (account.isEmpty()) {
            return ResultCode.USER_NOT_FOUND;
        }
        account.get().unlock();
        return ResultCode.SUCCESS;
    }

    public Optional<Account> findByUsername(String username) {
        if (isBlank(username)) {
            return Optional.empty();
        }
        return Optional.ofNullable(accountsByUsername.get(key(username)));
    }

    public boolean isLocked(String username) {
        return findByUsername(username).map(Account::isLocked).orElse(false);
    }

    // Stub cho tính năng đổi/quên mật khẩu (bonus)
    public ResultCode changePassword(String username, String oldPassword, String newPassword, String confirmPassword) {
        throw new UnsupportedOperationException("TODO");
    }

    // Helper methods
    private static boolean isBlank(String s) {
        return s == null || s.isBlank();
    }

    private static String key(String s) {
        return s.toLowerCase(Locale.ROOT);
    }
}