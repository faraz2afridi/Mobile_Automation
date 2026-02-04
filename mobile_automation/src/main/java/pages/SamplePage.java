package pages;

import helper.ConfigReader;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.By;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

import static helper.DriverHelper.*;

/**
 * Sample Page Object Model class for mobile automation testing.
 * This is a demonstration file showcasing page object pattern implementation.
 */
public class SamplePage {
    private final AndroidDriver driver;
    private static final Logger logger = LogManager.getLogger(SamplePage.class);

    // Locators - Using dummy resource IDs
    private final By welcomeMessage = By.id("com.sample.app:id/tvWelcome");
    private final By usernameField = By.id("com.sample.app:id/etUsername");
    private final By passwordField = By.id("com.sample.app:id/etPassword");
    private final By loginButton = By.id("com.sample.app:id/btnLogin");
    private final By forgotPasswordLink = By.id("com.sample.app:id/tvForgotPassword");
    private final By signUpButton = By.xpath("//android.widget.Button[@text='Sign Up']");
    private final By errorMessage = By.id("com.sample.app:id/tvError");
    private final By successMessage = By.xpath("//android.widget.TextView[@text='Login Successful']");
    private final By menuIcon = By.xpath("//android.widget.ImageButton[@content-desc='Menu']");
    private final By profileIcon = By.id("com.sample.app:id/ivProfile");
    private final By settingsButton = By.xpath("//android.widget.TextView[@text='Settings']");
    private final By logoutButton = By.id("com.sample.app:id/btnLogout");
    private final By confirmLogoutButton = By.id("com.sample.app:id/btnConfirm");

    // Timeout configurations
    private static final int TIMEOUT_SECONDS = ConfigReader.getIntProperty("timeout.seconds");
    private static final int TIMEOUT_MAX_SECONDS = ConfigReader.getIntProperty("timeout.maximum");

    /**
     * Constructor to initialize the page with driver instance.
     *
     * @param driver AndroidDriver instance
     */
    public SamplePage(AndroidDriver driver) {
        this.driver = driver;
    }

    /**
     * Verifies if the welcome message is displayed on the page.
     *
     * @return true if welcome message is visible, false otherwise
     */
    public boolean isWelcomeMessageDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
            wait.until(ExpectedConditions.visibilityOfElementLocated(welcomeMessage));
            logger.info("Welcome message is displayed");
            return driver.findElement(welcomeMessage).isDisplayed();
        } catch (Exception e) {
            logger.warn("Welcome message is not displayed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Performs login with dummy credentials.
     * Uses sample data for demonstration purposes.
     *
     * @param username Dummy username for login
     * @param password Dummy password for login
     */
    public void login(String username, String password) {
        logger.info("Attempting to login with username: {}", username);
        
        // Enter username
        waitAndClick(usernameField, driver);
        sendKeys(usernameField, driver, username);
        
        // Enter password
        waitAndClick(passwordField, driver);
        sendKeys(passwordField, driver, password);
        
        // Hide keyboard if visible
        driver.hideKeyboard();
        
        // Click login button
        waitAndClick(loginButton, driver);
        
        logger.info("Login attempt completed");
    }

    /**
     * Performs login with default dummy credentials.
     * This method uses hardcoded dummy values for demonstration.
     */
    public void loginWithDefaultCredentials() {
        String dummyUsername = "sample_user@example.com";
        String dummyPassword = "SamplePassword123";
        login(dummyUsername, dummyPassword);
    }

    /**
     * Clicks on the forgot password link.
     */
    public void clickForgotPassword() {
        logger.info("Clicking forgot password link");
        waitAndClick(forgotPasswordLink, driver);
    }

    /**
     * Clicks on the sign up button.
     */
    public void clickSignUp() {
        logger.info("Clicking sign up button");
        waitAndClick(signUpButton, driver);
    }

    /**
     * Verifies if error message is displayed.
     *
     * @return true if error message is visible, false otherwise
     */
    public boolean isErrorMessageDisplayed() {
        try {
            return checkVisibleWithHandle(errorMessage, driver);
        } catch (Exception e) {
            logger.debug("Error message not displayed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Verifies if success message is displayed after login.
     *
     * @return true if success message is visible, false otherwise
     */
    public boolean isSuccessMessageDisplayed() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
            wait.until(ExpectedConditions.visibilityOfElementLocated(successMessage));
            logger.info("Success message is displayed");
            return true;
        } catch (Exception e) {
            logger.warn("Success message is not displayed: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Navigates to profile section from menu.
     */
    public void navigateToProfile() {
        logger.info("Navigating to profile section");
        
        // Click menu icon
        waitAndClick(menuIcon, driver);
        
        // Click profile icon
        waitAndClick(profileIcon, driver);
    }

    /**
     * Navigates to settings page.
     */
    public void navigateToSettings() {
        logger.info("Navigating to settings");
        
        // Open menu
        waitAndClick(menuIcon, driver);
        
        // Click settings
        waitAndClick(settingsButton, driver);
    }

    /**
     * Performs logout from the application.
     */
    public void logout() {
        logger.info("Attempting to logout");
        
        try {
            // Open menu
            waitAndClick(menuIcon, driver);
            
            // Scroll to logout button if needed
            scrollToElementAndroid(logoutButton, driver);
            
            // Click logout
            waitAndClick(logoutButton, driver);
            
            // Confirm logout
            waitAndClick(confirmLogoutButton, driver);
            
            logger.info("Logout completed successfully");
        } catch (Exception e) {
            logger.error("Error during logout: {}", e.getMessage());
            throw new RuntimeException("Logout failed", e);
        }
    }

    /**
     * Gets the text content of the welcome message.
     *
     * @return Welcome message text
     */
    public String getWelcomeMessageText() {
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
            wait.until(ExpectedConditions.visibilityOfElementLocated(welcomeMessage));
            String text = driver.findElement(welcomeMessage).getText();
            logger.info("Welcome message text: {}", text);
            return text;
        } catch (Exception e) {
            logger.warn("Could not retrieve welcome message text: {}", e.getMessage());
            return "";
        }
    }

    /**
     * Verifies if the page is loaded by checking for key elements.
     *
     * @return true if page is loaded, false otherwise
     */
    public boolean isPageLoaded() {
        try {
            boolean welcomeVisible = checkVisibleWithHandle(welcomeMessage, driver);
            boolean usernameVisible = checkVisibleWithHandle(usernameField, driver);
            boolean loginButtonVisible = checkVisibleWithHandle(loginButton, driver);
            
            boolean pageLoaded = welcomeVisible && usernameVisible && loginButtonVisible;
            logger.info("Page loaded status: {}", pageLoaded);
            return pageLoaded;
        } catch (Exception e) {
            logger.warn("Error checking if page is loaded: {}", e.getMessage());
            return false;
        }
    }

    /**
     * Clears the username and password fields.
     */
    public void clearLoginFields() {
        logger.info("Clearing login fields");
        
        try {
            WebElement usernameElement = driver.findElement(usernameField);
            usernameElement.clear();
            
            WebElement passwordElement = driver.findElement(passwordField);
            passwordElement.clear();
            
            logger.info("Login fields cleared successfully");
        } catch (Exception e) {
            logger.warn("Error clearing login fields: {}", e.getMessage());
        }
    }

    /**
     * Waits for the page to be fully loaded with all elements visible.
     */
    public void waitForPageToLoad() {
        logger.info("Waiting for page to fully load");
        
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_MAX_SECONDS));
            wait.until(ExpectedConditions.visibilityOfElementLocated(welcomeMessage));
            wait.until(ExpectedConditions.elementToBeClickable(loginButton));
            logger.info("Page loaded successfully");
        } catch (Exception e) {
            logger.error("Page did not load within timeout: {}", e.getMessage());
            throw new RuntimeException("Page load timeout", e);
        }
    }
}
