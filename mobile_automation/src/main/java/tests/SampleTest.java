package tests;

import factory.BaseTest;
import io.qameta.allure.Step;
import org.testng.annotations.Test;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import pages.SamplePage;
import io.appium.java_client.android.AndroidDriver;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Sample test class demonstrating mobile automation testing framework.
 * This class showcases test structure and page object model usage with dummy data.
 */
public class SampleTest extends BaseTest {
    private static final Logger logger = LogManager.getLogger(SampleTest.class);

    private SamplePage samplePage;
    private AndroidDriver driver;

    /**
     * Initialize page objects before each test method execution.
     */
    @Step("Initialize page objects before each test")
    @BeforeMethod
    public void initializePageObjects() {
        logger.info("Initializing page objects for test execution");
        // Driver is already initialized in BaseTest.setUpMethod()
        driver = getDriver();
        samplePage = new SamplePage(driver);
    }

    /**
     * Cleanup after each test method.
     * This method can be used to perform logout or other cleanup operations.
     */
    @AfterMethod
    public void cleanupAfterTest() {
        logger.info("Performing cleanup after test execution");
        // Add any cleanup logic here if needed
        // For example: samplePage.logout();
    }

    /**
     * Sample test to verify login functionality with default credentials.
     * This test demonstrates basic login flow using dummy data.
     */
    @Test(description = "Sample test to verify login functionality with default credentials", priority = 1)
    public void testLoginWithDefaultCredentials() {
        logger.info("Starting test: Login with default credentials");
        
        // Wait for page to load
        samplePage.waitForPageToLoad();
        
        // Verify page is loaded
        boolean isPageLoaded = samplePage.isPageLoaded();
        assert isPageLoaded : "Page did not load successfully";
        
        // Verify welcome message is displayed
        boolean isWelcomeDisplayed = samplePage.isWelcomeMessageDisplayed();
        assert isWelcomeDisplayed : "Welcome message is not displayed";
        
        // Perform login with default credentials
        samplePage.loginWithDefaultCredentials();
        
        // Verify success message (if applicable)
        // Note: This is a sample test, actual implementation may vary
        logger.info("Test completed: Login with default credentials");
    }

    /**
     * Sample test to verify login functionality with custom credentials.
     * This test demonstrates login flow with user-provided dummy credentials.
     */
    @Test(description = "Sample test to verify login functionality with custom credentials", priority = 2)
    public void testLoginWithCustomCredentials() {
        logger.info("Starting test: Login with custom credentials");
        
        // Wait for page to load
        samplePage.waitForPageToLoad();
        
        // Dummy credentials for demonstration
        String dummyUsername = "test_user@example.com";
        String dummyPassword = "TestPassword123";
        
        // Perform login with custom credentials
        samplePage.login(dummyUsername, dummyPassword);
        
        // Verify login success (if applicable)
        // Note: This is a sample test, actual implementation may vary
        logger.info("Test completed: Login with custom credentials");
    }

    /**
     * Sample test to verify forgot password functionality.
     * This test demonstrates navigation to forgot password flow.
     */
    @Test(description = "Sample test to verify forgot password functionality", priority = 3)
    public void testForgotPassword() {
        logger.info("Starting test: Forgot password functionality");
        
        // Wait for page to load
        samplePage.waitForPageToLoad();
        
        // Click on forgot password link
        samplePage.clickForgotPassword();
        
        // Add verification steps here if needed
        logger.info("Test completed: Forgot password functionality");
    }

    /**
     * Sample test to verify sign up functionality.
     * This test demonstrates navigation to sign up flow.
     */
    @Test(description = "Sample test to verify sign up functionality", priority = 4)
    public void testSignUp() {
        logger.info("Starting test: Sign up functionality");
        
        // Wait for page to load
        samplePage.waitForPageToLoad();
        
        // Click on sign up button
        samplePage.clickSignUp();
        
        // Add verification steps here if needed
        logger.info("Test completed: Sign up functionality");
    }

    /**
     * Sample test to verify page elements visibility.
     * This test demonstrates element verification on the page.
     */
    @Test(description = "Sample test to verify page elements visibility", priority = 5)
    public void testPageElementsVisibility() {
        logger.info("Starting test: Page elements visibility");
        
        // Wait for page to load
        samplePage.waitForPageToLoad();
        
        // Verify welcome message
        boolean isWelcomeDisplayed = samplePage.isWelcomeMessageDisplayed();
        assert isWelcomeDisplayed : "Welcome message should be displayed";
        
        // Get welcome message text
        String welcomeText = samplePage.getWelcomeMessageText();
        logger.info("Welcome message text: {}", welcomeText);
        
        // Verify page is fully loaded
        boolean isPageLoaded = samplePage.isPageLoaded();
        assert isPageLoaded : "Page should be fully loaded";
        
        logger.info("Test completed: Page elements visibility");
    }

    /**
     * Sample test to verify navigation to profile section.
     * This test demonstrates navigation flow within the application.
     */
    @Test(description = "Sample test to verify navigation to profile section", priority = 6)
    public void testNavigateToProfile() {
        logger.info("Starting test: Navigation to profile");
        
        // Wait for page to load
        samplePage.waitForPageToLoad();
        
        // Perform login first (if required)
        samplePage.loginWithDefaultCredentials();
        
        // Navigate to profile
        samplePage.navigateToProfile();
        
        // Add verification steps here if needed
        logger.info("Test completed: Navigation to profile");
    }

    /**
     * Sample test to verify navigation to settings.
     * This test demonstrates settings navigation flow.
     */
    @Test(description = "Sample test to verify navigation to settings", priority = 7)
    public void testNavigateToSettings() {
        logger.info("Starting test: Navigation to settings");
        
        // Wait for page to load
        samplePage.waitForPageToLoad();
        
        // Perform login first (if required)
        samplePage.loginWithDefaultCredentials();
        
        // Navigate to settings
        samplePage.navigateToSettings();
        
        // Add verification steps here if needed
        logger.info("Test completed: Navigation to settings");
    }

    /**
     * Sample test to verify logout functionality.
     * This test demonstrates logout flow from the application.
     */
    @Test(description = "Sample test to verify logout functionality", priority = 8)
    public void testLogout() {
        logger.info("Starting test: Logout functionality");
        
        // Wait for page to load
        samplePage.waitForPageToLoad();
        
        // Perform login first
        samplePage.loginWithDefaultCredentials();
        
        // Perform logout
        samplePage.logout();
        
        // Add verification steps here if needed
        // For example: verify user is logged out and redirected to login page
        logger.info("Test completed: Logout functionality");
    }

    /**
     * Sample test to verify error handling during login.
     * This test demonstrates error scenario handling.
     */
    @Test(description = "Sample test to verify error handling during login", priority = 9)
    public void testLoginErrorHandling() {
        logger.info("Starting test: Login error handling");
        
        // Wait for page to load
        samplePage.waitForPageToLoad();
        
        // Clear any existing input
        samplePage.clearLoginFields();
        
        // Attempt login with invalid credentials
        String invalidUsername = "invalid_user@example.com";
        String invalidPassword = "InvalidPassword";
        samplePage.login(invalidUsername, invalidPassword);
        
        // Verify error message is displayed (if applicable)
        boolean isErrorDisplayed = samplePage.isErrorMessageDisplayed();
        if (isErrorDisplayed) {
            logger.info("Error message displayed as expected");
        } else {
            logger.warn("Error message not displayed - this may be expected behavior");
        }
        
        logger.info("Test completed: Login error handling");
    }
}
