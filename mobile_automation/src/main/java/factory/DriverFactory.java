package factory;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.options.UiAutomator2Options;
import io.appium.java_client.ios.IOSDriver;
import io.appium.java_client.ios.options.XCUITestOptions;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import utilities.ConfigReader;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.time.Duration;

public class DriverFactory {
    private static final Logger logger = LogManager.getLogger(DriverFactory.class);
    private static AppiumDriver driver;

    public static synchronized AppiumDriver getDriver() {
        try {
            String platform = ConfigReader.getPlatform().toLowerCase();
            logger.info("Initializing new Appium driver for platform: {}", platform);

            AppiumDriver newDriver = null;
            try {
                switch (platform) {
                    case "android":
                        newDriver = createAndroidDriver();
                        break;
                    case "ios":
                        newDriver = createIOSDriver();
                        break;
                    default:
                        throw new IllegalArgumentException("Unsupported platform: " + platform);
                }

                // Validate driver and session before returning
                if (newDriver != null) {
                    // Wait for session to be established
                    int maxWait = 10;
                    boolean sessionValid = false;
                    
                    for (int i = 0; i < maxWait; i++) {
                        try {
                            if (newDriver.getSessionId() != null) {
                                sessionValid = true;
                                break;
                            }
                            Thread.sleep(1000);
                        } catch (Exception e) {
                            if (i < maxWait - 1) {
                                Thread.sleep(1000);
                            }
                        }
                    }

                    if (!sessionValid) {
                        throw new RuntimeException("Failed to establish valid driver session");
                    }

                    driver = newDriver; // Only set the static driver after validation
                    logger.info("Successfully initialized Appium driver for {} with session ID: {}", 
                              platform, driver.getSessionId());
                    return driver;
                } else {
                    throw new RuntimeException("Driver creation returned null");
                }
            } catch (Exception e) {
                logger.error("Failed to initialize {} driver: {}", platform, e.getMessage());
                if (newDriver != null) {
                    try {
                        newDriver.quit();
                    } catch (Exception qe) {
                        logger.warn("Failed to quit failed driver instance: {}", qe.getMessage());
                    }
                }
                throw new RuntimeException("Failed to initialize " + platform + " driver", e);
            }
        } catch (Exception e) {
            logger.error("Critical error in driver initialization: {}", e.getMessage());
            throw new RuntimeException("Critical error in driver initialization", e);
        }
    }

    private static AndroidDriver createAndroidDriver() throws MalformedURLException {
        UiAutomator2Options options = new UiAutomator2Options();

        // Common Android capabilities
        options.setPlatformName("Android")
                .setAutomationName(ConfigReader.getAutomationName())
                .setAppPackage(ConfigReader.getAndroidAppPackage())
                .setAppActivity(ConfigReader.getAndroidAppActivity())
                .setNoReset(true)
                .setFullReset(false)
                .setAutoGrantPermissions(true)
                .setNewCommandTimeout(Duration.ofSeconds(ConfigReader.getCommandTimeout()))
                .setCapability("forceAppLaunch", ConfigReader.getForceAppLaunch());

        // Set device specific capabilities
        if (ConfigReader.getEmulatorEnabled()) {
            setupAndroidEmulator(options);
        } else {
            setupRealAndroidDevice(options);
        }
        
        // Create URL using modern pattern
        URL serverUrl = URI.create(ConfigReader.getAppiumServerUrl()).toURL();
        return new AndroidDriver(serverUrl, options);
    }

    private static IOSDriver createIOSDriver() throws MalformedURLException {
        XCUITestOptions options = new XCUITestOptions();

        // Common iOS capabilities
        options.setPlatformName("iOS")
                .setAutomationName("XCUITest")
                .setBundleId(ConfigReader.getIOSBundleId())
                .setApp(ConfigReader.getIOSAppPath())
                .setNoReset(true)
                .setFullReset(false)
                .setAutoAcceptAlerts(ConfigReader.getIOSAutoAcceptAlerts())
                .setShowIosLog(ConfigReader.getShowIOSLog())
                .setNewCommandTimeout(Duration.ofSeconds(ConfigReader.getCommandTimeout()));

        // Set device specific capabilities
        if (ConfigReader.getEmulatorEnabled()) {
            setupIOSSimulator(options);
        } else {
            setupRealIOSDevice(options);
        }
        
        // Create URL using modern pattern
        URL serverUrl = URI.create(ConfigReader.getAppiumServerUrl()).toURL();
        return new IOSDriver(serverUrl, options);
    }

    private static void setupAndroidEmulator(UiAutomator2Options options) {
        options.setDeviceName(ConfigReader.getDeviceName())
                .setAvd(ConfigReader.getAvdName())
                .setPlatformVersion(ConfigReader.getPlatformVersion())
                .setAvdLaunchTimeout(Duration.ofSeconds(180))
                .setAvdReadyTimeout(Duration.ofSeconds(180))
                .setIsHeadless(ConfigReader.isHeadless());
    }

    private static void setupRealAndroidDevice(UiAutomator2Options options) {
        options.setDeviceName(ConfigReader.getDeviceName())
                .setUdid(ConfigReader.getUdid());
    }

    private static void setupIOSSimulator(XCUITestOptions options) {
        options.setDeviceName(ConfigReader.getIOSSimulatorDevice())
                .setPlatformVersion(ConfigReader.getIOSPlatformVersion())
                .setIsHeadless(ConfigReader.isHeadless());
    }

    private static void setupRealIOSDevice(XCUITestOptions options) {
        options.setDeviceName(ConfigReader.getIOSDeviceName())
                .setUdid(ConfigReader.getIOSUdid());
    }

    public static void quitDriver() {
        if (driver != null) {
            try {
                // Check if session is still active
                if (driver.getSessionId() != null) {
                    logger.info("Quitting Appium driver...");
                    driver.quit();
                    logger.info("Appium driver quit successfully");
                }
            } catch (Exception e) {
                String errorMsg = "Error while quitting driver: " + e.getMessage();
                logger.error(errorMsg, e);
                System.err.println(errorMsg);
            } finally {
                driver = null;
            }
        }
    }
    
    /**
     * Check if the current driver session is active
     * @return true if session is active, false otherwise
     */
    public static boolean isSessionActive() {
        try {
            return driver != null && driver.getSessionId() != null;
        } catch (Exception e) {
            return false;
        }
    }
}