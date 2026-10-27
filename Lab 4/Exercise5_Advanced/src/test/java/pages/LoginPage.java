
package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

public class LoginPage extends BasePage {

    public LoginPage(WebDriver driver) {
        super(driver);
    }

    // ===== LOCATORS =====
    private final By usernameField = By.id("username");
    private final By passwordField = By.id("password");
    private final By loginButton = By.cssSelector("button[type='submit']");
    private final By successMsg = By.cssSelector(".flash.success");
    private final By errorMsg = By.cssSelector(".flash.error");

    // TODO 7 - Locator moi
    private final By flashMessage = By.id("flash");

    // ===== ACTIONS =====

    // Mo trang dang nhap
    public void navigate() {
        navigateTo("https://the-internet.herokuapp.com/login");
    }

    // Dang nhap - giu nguyen tu TODO 6
    public void login(String username, String password) {
        type(usernameField, username);
        type(passwordField, password);
        click(loginButton);
    }

    // ===== METHODS TU TODO 6 =====

    public By getSuccessLocator() {
        return successMsg;
    }

    public By getErrorLocator() {
        return errorMsg;
    }

    public String getMessageText(By locator) {
        return getText(locator);
    }

    // ===== TODO 7 - METHODS MOI =====

    // Dang nhap thanh cong va tra ve SecureAreaPage
    public SecureAreaPage loginAs(String username, String password) {
        login(username, password);
        return new SecureAreaPage(driver);
    }

    // Lay thong bao tren trang Login
    public String getFlashMessage() {
        return getText(flashMessage);
    }

    // Kiem tra co dang o trang Login khong
    public boolean isAt() {
        return driver.getCurrentUrl().endsWith("/login");
    }
}
