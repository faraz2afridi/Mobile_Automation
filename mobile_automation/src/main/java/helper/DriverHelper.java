package helper;

import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.AppiumBy;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.Arrays;
import java.util.List;

public class DriverHelper {

    private static AndroidDriver driver;
    public static final Logger logger = LogManager.getLogger(DriverHelper.class);

    public DriverHelper(AndroidDriver driver) {
        DriverHelper.driver = driver;
    }

    public static final int TIMEOUT_SECONDS = ConfigReader.getIntProperty("timeout.seconds");
    public static final int TIMEOUT_MAX_SECONDS = ConfigReader.getIntProperty("timeout.maximum");
    public static final int TIMEOUT_MIN_SECONDS = ConfigReader.getIntProperty("timeout.minimum");

    public static void waitAndClickExplicitWait(By element, AndroidDriver driver){
        try {
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
            wait.until(ExpectedConditions.visibilityOfElementLocated(element));
            wait.until(ExpectedConditions.elementToBeClickable(element));
            driver.findElement(element).click();
        }catch(Exception e){
            logger.debug("Element may not be present - {}", element);
        }
    }

    public static void waitAndClick(By element, AndroidDriver driver){
        // Set an implicit wait for the driver
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(TIMEOUT_SECONDS)); // Adjust as needed

        try {
            // Attempt to find the element and click it
            WebElement clickableElement = driver.findElement(element);
            clickableElement.click();
            logger.info("Successfully clicked the element: {}", element);
        } catch (Exception e) {
        }
    }

    public static void waitAndClickWithTimeOut(By element, AndroidDriver driver, int timeOut){
        // Set an implicit wait for the driver
        driver.manage().timeouts().implicitlyWait(Duration.ofSeconds(timeOut)); // Adjust as needed

        try {
            // Attempt to find the element and click it
            WebElement clickableElement = driver.findElement(element);
            clickableElement.click();
            logger.info("Successfully clicked the element: {}", element);
        } catch (Exception e) {
        }
    }

    public static void fluentWaitAndClick(By element, AndroidDriver driver) {
        try {
            List<Class<? extends Throwable>> ignoredExceptions = Arrays.asList(
                    NoSuchElementException.class,
                    StaleElementReferenceException.class,
                    TimeoutException.class
            );
            FluentWait<AndroidDriver> wait = new FluentWait<>(driver)
                    .withTimeout(Duration.ofSeconds(TIMEOUT_MIN_SECONDS))
                    .pollingEvery(Duration.ofMillis(500))
                    .ignoreAll(ignoredExceptions);
            wait.until(ExpectedConditions.elementToBeClickable(element));
            driver.findElement(element).click();
        } catch (Exception e) {
            logger.warn("Element may not be present or clickable - {}", element);
        }
    }

    public static void sendKeys(By element, AndroidDriver driver, String value){
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
        wait.until(ExpectedConditions.visibilityOfElementLocated(element));
        wait.until(ExpectedConditions.elementToBeClickable(element));
        driver.findElement(element).sendKeys(value);
        logger.info("Sending Keys - {}", value);
    }

    // Android-specific scroll to element
    public static void scrollToElementAndroid(By locator, AndroidDriver driver) {
        boolean isElementFound = false;

        while (!isElementFound) {
            try {
                WebElement element = driver.findElement(locator);
                isElementFound = element.isDisplayed();
            } catch (Exception e) {
                scrollDownAndroid(locator, driver);
            }
        }
    }

    public static void scrollDownAndroid(By locator, AndroidDriver driver) {
        // Perform a scroll if element not found
        driver.findElement(AppiumBy.androidUIAutomator(
                        "new UiScrollable(new UiSelector().scrollable(true)).scrollForward()"
        ));
    }

    public static boolean checkVisible(By locator, AndroidDriver driver){
        boolean checkVisible=false;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
        wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
        checkVisible = true;
        return checkVisible;
    }

    public static boolean checkVisibleWithHandle(By locator, AndroidDriver driver){
        boolean checkVisible=false;
        try{
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
            wait.until(ExpectedConditions.visibilityOfElementLocated(locator));
            checkVisible = true;
        }catch(Exception e){
            logger.debug("Element may not be present - {}", locator);
        }
        return checkVisible;
    }

    /**
     * Quick presence check: temporarily reduces implicit wait and uses findElements to
     * determine presence without waiting the full default timeout. Use for optional fields/popups.
     */
    public static boolean isElementPresentQuick(By locator, AndroidDriver driver, Duration timeout) {
        try {
            Duration original = driver.manage().timeouts().getImplicitWaitTimeout();
            try {
                driver.manage().timeouts().implicitlyWait(timeout);
                return !driver.findElements(locator).isEmpty();
            } finally {
                driver.manage().timeouts().implicitlyWait(original);
            }
        } catch (Exception e) {
            logger.debug("Quick presence check failed for {}: {}", locator, e.getMessage());
            return false;
        }
    }

    public static boolean checkInVisible(By locator, AndroidDriver driver){
        boolean checkInVisible=false;
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
        wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
        checkInVisible = true;
        return checkInVisible;
    }

    public static boolean checkInVisibleWithHandle(By locator, AndroidDriver driver){
        boolean checkInVisible=false;
        try{
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(TIMEOUT_SECONDS));
            wait.until(ExpectedConditions.invisibilityOfElementLocated(locator));
            checkInVisible = true;
        }catch(Exception e){
            logger.debug("Element may not be present - {}", locator);
        }
        
        return checkInVisible;
    }
}