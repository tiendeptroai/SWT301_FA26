package Tiennt.example;

import java.time.LocalDate;
import java.time.Period;
import java.util.Locale;
import java.util.regex.Pattern;

public final class AccountValidator {

    // Constructor private theo yêu cầu
    private AccountValidator() {
    }

    // BR-REG-02: Dài 5-20, bắt đầu bằng chữ cái, chỉ gồm [A-Za-z0-9_]
    private static final Pattern USERNAME_PATTERN = Pattern.compile("^[A-Za-z][A-Za-z0-9_]{4,19}$");

    // BR-REG-04: dạng local@domain.tld, TLD >= 2 chữ cái, nhãn domain không rỗng, dài <= 100
    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9._%+-]+@(?:[A-Za-z0-9-]+\\.)+[A-Za-z]{2,}$");

    // BR: 0[3|5|7|8|9] + 8 chữ số
    private static final Pattern PHONE_PATTERN = Pattern.compile("^0[35789]\\d{8}$");

    private static final String SPECIAL_CHARS = "!@#$%^&*()_+-=";

    /**
     * 1. Kiểm tra username
     */
    public static boolean isValidUsername(String username) {
        if (username == null) {
            return false;
        }
        return USERNAME_PATTERN.matcher(username).matches();
    }

    /**
     * 2. Kiểm tra email
     */
    public static boolean isValidEmail(String email) {
        if (email == null || email.length() > 100) {
            return false;
        }
        return EMAIL_PATTERN.matcher(email).matches();
    }

    /**
     * 3. Kiểm tra password:
     * - Dài 8-32 ký tự
     * - Đủ 4 nhóm: chữ hoa, chữ thường, chữ số, ký tự đặc biệt trong SPECIAL_CHARS
     * - Chỉ các ký tự cho phép (không chứa khoảng trắng / ký tự lạ)
     * - Không chứa username (case-insensitive; bỏ qua nếu username null/blank)
     */
    public static boolean isValidPassword(String password, String username) {
        if (password == null || password.length() < 8 || password.length() > 32) {
            return false;
        }

        boolean upper = false;
        boolean lower = false;
        boolean digit = false;
        boolean special = false;

        for (char c : password.toCharArray()) {
            if (c >= 'A' && c <= 'Z') {
                upper = true;
            } else if (c >= 'a' && c <= 'z') {
                lower = true;
            } else if (c >= '0' && c <= '9') {
                digit = true;
            } else if (SPECIAL_CHARS.indexOf(c) >= 0) {
                special = true;
            } else {
                return false; // Chứa khoảng trắng hoặc ký tự không hợp lệ
            }
        }

        // Kiểm tra đủ cả 4 nhóm
        if (!upper || !lower || !digit || !special) {
            return false;
        }

        // Không chứa username (bỏ qua nếu username null hoặc rỗng)
        if (username != null && !username.trim().isEmpty()) {
            String lowerPass = password.toLowerCase(Locale.ROOT);
            String lowerUser = username.toLowerCase(Locale.ROOT);
            if (lowerPass.contains(lowerUser)) {
                return false;
            }
        }

        return true;
    }

    /**
     * 4. Kiểm tra số điện thoại:
     * - Đầu số 03, 05, 07, 08, 09 + 8 chữ số
     * - null trả false
     */
    public static boolean isValidPhone(String phone) {
        if (phone == null) {
            return false;
        }
        return PHONE_PATTERN.matcher(phone).matches();
    }

    /**
     * 5. Tính tuổi (số năm tròn):
     * - Nếu dob hoặc today là null hoặc dob sau today thì trả về -1 (hoặc 0) để tránh lỗi NPE
     */
    public static int calculateAge(LocalDate dob, LocalDate today) {
        if (dob == null || today == null || dob.isAfter(today)) {
            return 0;
        }
        return Period.between(dob, today).getYears();
    }
}