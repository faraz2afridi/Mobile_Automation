package factory;

import io.appium.java_client.AppiumDriver;
import io.appium.java_client.service.local.AppiumDriverLocalService;
import io.appium.java_client.service.local.AppiumServiceBuilder;
import org.apache.logging.log4j.LogManager;
import org.openqa.selenium.remote.SessionId;
import org.testng.annotations.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Comparator;
import java.util.Date;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import org.apache.logging.log4j.Logger;
import utilities.ConfigReader;
import java.util.logging.Level;
import org.testng.ITestResult;
import io.appium.java_client.android.AndroidDriver;

/**
 * Base test class that handles Appium server lifecycle and test setup/teardown.
 * Manages Appium server start/stop operations, Allure reporting, and provides
 * common test functionality.
 */
public class BaseTest {
    private static final int TIMEOUT_MAX_SECONDS = helper.ConfigReader.getIntProperty("timeout.maximum");

    private static final Logger logger = LogManager.getLogger(BaseTest.class);
    private static final int APPIUM_SERVER_START_TIMEOUT_SECONDS = TIMEOUT_MAX_SECONDS;
    private static AppiumDriverLocalService service;
    private static final ThreadLocal<AppiumDriver> driver = new ThreadLocal<>();
    private static final String ALLURE_RESULTS_DIR = "allure-results";
    private static final String ALLURE_REPORT_DIR = "allure-report";

    /**
     * Gets the driver instance for the current thread.
     * 
     * @return The AppiumDriver instance for the current thread
     */
    protected AndroidDriver getDriver() {
        return (AndroidDriver) driver.get();
    }

    /**
     * Sets the driver instance for the current thread.
     * 
     * @param driverInstance The AppiumDriver instance to set
     */
    protected void setDriver(AppiumDriver driverInstance) {
        driver.set(driverInstance);
    }

    /**
     * Removes the driver instance for the current thread.
     * Should be called in teardown to prevent memory leaks.
     */
    protected void removeDriver() {
        driver.remove();
    }

    /**
     * Sets up the test class before running any tests.
     * This includes configuring logging, cleaning up previous Allure results,
     * ensuring Allure results directory exists, and ensuring Appium server is
     * running.
     * 
     * @throws IOException if there is an error setting up the test class
     */
    @BeforeClass(alwaysRun = true)
    public void setUp() throws IOException {
        try {
            logger.info("Starting test class setup...");

            // Configure Java Util Logging to use Log4j2
            System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

            // Configure Log4j2 log levels programmatically
            configureLogging();

            // Clean up previous Allure results
            cleanAllureResults();

            // Ensure Allure results directory exists
            ensureAllureDirectoriesExist();

            // Ensure Appium server is running
            ensureAppiumServerRunning();

            logger.info("Test class setup completed successfully");
        } catch (Exception e) {
            logger.error("Test class setup failed: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Clears the app cache for the specified package on the device.
     *
     * @param driver      The Appium driver for the device.
     * @param packageName The package name of the app whose cache should be cleared.
     * @throws IOException If there is an error executing the adb command.
     */
    public void clearAppCache(AndroidDriver driver, String packageName) {
        String deviceUdid = driver.getSessionId().toString(); // Get the device UDID from the session

        // Construct the adb command to clear the app cache
        String command = String.format("adb -s %s shell pm clear %s", deviceUdid, packageName);

        try {
            Process process = new ProcessBuilder("sh", "-c", command).start();
            process.waitFor(); // Wait for the command to complete
            System.out.println("App cache cleared for package: " + packageName);
        } catch (InterruptedException | IOException e) {
            System.err.println("Failed to clear app cache: " + e.getMessage());
        }
    }

    /**
     * Sets up the test method before running the test.
     * This includes initializing the driver, clearing the app cache, and logging
     * the setup status.
     * 
     * @throws Exception If there is an error setting up the test method.
     */
    @BeforeMethod(alwaysRun = true)
    public void setUpMethod() {
        logger.info("Starting test method setup...");
        try {
            initializeDriver();
            clearAppCache((AndroidDriver) driver.get(), ConfigReader.getAndroidAppPackage());
            logger.info("Test method setup completed successfully");
        } catch (Exception e) {
            logger.error("Test method setup failed: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Cleans up after the test method has run.
     * This method clears the app cache, removes the driver instance, and logs the
     * teardown status.
     *
     * @param result The ITestResult object representing the test being torn down.
     * @throws Exception If there is an error tearing down the test method.
     */
    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        logger.info("Starting test method teardown for: {}", result.getName());

        AppiumDriver currentDriver = getDriver();
        if (currentDriver != null) {
            try {
                // Handle test failures
                if (result.getStatus() == ITestResult.FAILURE) {
                    logger.error("Test failed: {}", result.getName());
                    try {
                        // Capture screenshot before quitting
                        if (currentDriver.getSessionId() != null) {
                            // Add your screenshot capture code here
                            logger.info("Captured failure evidence for test: {}", result.getName());
                        }
                    } catch (Exception e) {
                        logger.error("Failed to capture failure evidence: {}", e.getMessage());
                    }
                }

                // Check session validity before quitting
                if (currentDriver.getSessionId() != null) {
                    try {
                        currentDriver.quit();
                        logger.info("Driver quit successfully for test: {}", result.getName());
                    } catch (Exception e) {
                        logger.warn("Error quitting driver: {}. Session may be terminated.", e.getMessage());
                    }
                } else {
                    logger.info("Driver session already terminated for test: {}", result.getName());
                }

            } catch (Exception e) {
                logger.error("Unexpected error in teardown: {}", e.getMessage());
            } finally {
                removeDriver();
                logger.info("Driver cleanup completed for test: {}", result.getName());
            }
        } else {
            logger.info("No driver instance found to clean up for test: {}", result.getName());
        }
    }

    /**
     * Configures the Java Util Logging system to use Log4j2.
     * Sets the log level for each logger to the appropriate level.
     */
    private void configureLogging() {
        // Configure specific loggers
        org.apache.logging.log4j.core.config.Configurator.setRootLevel(org.apache.logging.log4j.Level.INFO);

        // Set Selenium and Appium related loggers to WARN to reduce noise
        org.apache.logging.log4j.core.config.Configurator.setLevel("io.appium", org.apache.logging.log4j.Level.WARN);
        org.apache.logging.log4j.core.config.Configurator.setLevel("org.openqa.selenium",
                org.apache.logging.log4j.Level.WARN);

        // Disable noisy third-party logging
        org.apache.logging.log4j.core.config.Configurator.setLevel("org.apache.http",
                org.apache.logging.log4j.Level.ERROR);
        org.apache.logging.log4j.core.config.Configurator.setLevel("io.netty", org.apache.logging.log4j.Level.ERROR);

        // Configure JUL to use Log4j2
        System.setProperty("java.util.logging.manager", "org.apache.logging.log4j.jul.LogManager");

        // Suppress specific JUL loggers
        java.util.logging.Logger.getLogger("io.appium").setLevel(Level.WARNING);
        java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.WARNING);
    }

    /**
     * Cleans up previous Allure results.
     * Deletes all files in the Allure results directory.
     */
    private void cleanAllureResults() {
        Path allureResultsDir = Paths.get(ALLURE_RESULTS_DIR);
        if (Files.exists(allureResultsDir)) {
            try {
                logger.info("Cleaning up previous Allure results...");
                try (var pathStream = Files.walk(allureResultsDir)) {
                    pathStream
                            .sorted(Comparator.reverseOrder())
                            .map(Path::toFile)
                            .filter(File::exists)
                            .forEach(file -> {
                                try {
                                    if (file.isDirectory()) {
                                        file.delete();
                                    } else if (!file.delete() && file.exists()) {
                                        logger.warn("Could not delete file: " + file.getAbsolutePath());
                                    }
                                } catch (SecurityException e) {
                                    logger.warn("Permission denied when trying to delete: " + file.getAbsolutePath(),
                                            e);
                                }
                            });

                    if (Files.exists(allureResultsDir)) {
                        Files.deleteIfExists(allureResultsDir);
                    }
                }
            } catch (IOException e) {
                logger.info("Error cleaning up Allure results directory: " + e.getMessage(), e);
            }
        }
    }

    /**
     * Ensures that the Allure results and report directories exist.
     * Creates the directories if they do not already exist.
     */
    private void ensureAllureDirectoriesExist() throws IOException {
        try {
            logger.info("Creating Allure results directory...");
            Files.createDirectories(Paths.get(ALLURE_RESULTS_DIR));
            Files.createDirectories(Paths.get(ALLURE_REPORT_DIR));
        } catch (IOException e) {
            logger.info("Error creating Allure directories: " + e.getMessage(), e);
            throw e;
        }
    }

    /**
     * Ensures that the Appium server is running.
     * If the server is not running, it starts the server and waits for it to be
     * ready.
     * 
     * @throws IOException if there is an error starting the server
     */
    private void ensureAppiumServerRunning() {
        if (service == null || !service.isRunning()) {
            logger.info("Starting Appium server...");
            startAppiumServer();
        } else {
            logger.info("Appium server is already running");
        }
    }

    /**
     * Initializes the driver for the test.
     * This method ensures that the Appium server is running and creates a new
     * driver instance.
     * It also cleans up any existing driver instance before creating a new one.
     * 
     * @throws RuntimeException If there is an error initializing the driver.
     */
    protected synchronized void initializeDriver() {
        int maxRetries = 3;
        int currentRetry = 0;
        Exception lastException = null;

        while (currentRetry < maxRetries) {
            try {
                // Always cleanup existing driver first
                AppiumDriver existingDriver = getDriver();
                if (existingDriver != null) {
                    try {
                        existingDriver.quit();
                    } catch (Exception e) {
                        logger.warn("Error quitting existing driver: {}", e.getMessage());
                    } finally {
                        removeDriver();
                    }
                }

                // Ensure Appium server is running
                ensureAppiumServerRunning();

                // Create new driver instance with retry
                logger.info("Attempting to initialize new driver (attempt {}/{})", currentRetry + 1, maxRetries);
                AppiumDriver driverInstance = DriverFactory.getDriver();

                if (driverInstance == null) {
                    throw new RuntimeException("Driver initialization failed - DriverFactory returned null");
                }

                // Give the driver a moment to establish the session
                int maxSessionWait = 10;
                SessionId sessionId = null;

                for (int i = 0; i < maxSessionWait && sessionId == null; i++) {
                    try {
                        sessionId = driverInstance.getSessionId();
                        if (sessionId == null && i < maxSessionWait - 1) {
                            Thread.sleep(1000);
                        }
                    } catch (Exception e) {
                        if (i < maxSessionWait - 1) {
                            Thread.sleep(1000);
                        }
                    }
                }

                if (sessionId == null) {
                    throw new RuntimeException("Failed to obtain valid session ID");
                }

                logger.info("New session created with ID: {}", sessionId.toString());

                try {
                    // Set implicit wait
                    driverInstance.manage().timeouts().implicitlyWait(Duration.ofSeconds(TIMEOUT_MAX_SECONDS));

                    // Additional session validation
                    driverInstance.getPageSource();

                    // Set the driver only after successful validation
                    setDriver(driverInstance);
                    logger.info("Driver initialized successfully with session ID: {} (attempt {}/{})",
                            sessionId.toString(), currentRetry + 1, maxRetries);
                } catch (Exception e) {
                    logger.error("Failed to configure driver after session creation: {}", e.getMessage());
                    try {
                        driverInstance.quit();
                    } catch (Exception qe) {
                        logger.warn("Failed to quit driver after configuration error: {}", qe.getMessage());
                    }
                    throw e;
                }
                return; // Success - exit method

            } catch (Exception e) {
                lastException = e;
                logger.warn("Driver initialization attempt {}/{} failed: {}",
                        currentRetry + 1, maxRetries, e.getMessage());

                // Clean up failed attempt
                try {
                    AppiumDriver failedDriver = getDriver();
                    if (failedDriver != null) {
                        try {
                            failedDriver.quit();
                        } catch (Exception qe) {
                            logger.warn("Failed to quit failed driver: {}", qe.getMessage());
                        }
                        removeDriver();
                    }
                } catch (Exception ce) {
                    logger.warn("Error during cleanup of failed attempt: {}", ce.getMessage());
                }

                // Wait before retry
                if (currentRetry < maxRetries - 1) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException ie) {
                        Thread.currentThread().interrupt();
                        break;
                    }
                }

                currentRetry++;
            }
        }

        // If we get here, all retries failed
        String errorMsg = String.format("Driver initialization failed after %d attempts", maxRetries);
        logger.error(errorMsg, lastException);
        throw new RuntimeException(errorMsg, lastException);
    }

    /**
     * Generates an Allure report for the current test run.
     * This method creates the report directory, copies the generated Allure results
     * to the report directory,
     * and opens the report in the default browser.
     *
     * @throws IOException If there is an error generating the report.
     */
    private void generateAllureReport() {
        try {
            logger.info("Generating Allure report...");
            ProcessBuilder processBuilder = new ProcessBuilder(
                    "allure", "generate", "--single-file", "--clean",
                    ALLURE_RESULTS_DIR, "-o", ALLURE_REPORT_DIR);

            Process process = processBuilder.start();
            int exitCode = process.waitFor();

            if (exitCode == 0) {
                logger.info("Allure report generated successfully");
            } else {
                logger.info("Failed to generate Allure report. Exit code: " + exitCode);
            }
        } catch (Exception e) {
            logger.info("Error generating Allure report: " + e.getMessage(), e);
        }
    }

    /**
     * Opens the generated Allure report in the default browser.
     * This method checks if the report file exists, then uses the appropriate
     * command to open the file in the default browser.
     * The command used depends on the operating system of the machine running the
     * test.
     *
     * @throws Exception If there is an error opening the report.
     */
    private void openAllureReport() {
        try {
            File reportFile = new File(ALLURE_REPORT_DIR, "index.html");
            if (reportFile.exists()) {
                String reportPath = reportFile.getAbsolutePath();
                logger.info("Opening Allure report: " + reportPath);

                // Use ProcessBuilder for all OS commands
                ProcessBuilder openBuilder;
                String os = System.getProperty("os.name").toLowerCase();

                if (os.contains("mac")) {
                    openBuilder = new ProcessBuilder("open", reportPath);
                } else if (os.contains("win")) {
                    openBuilder = new ProcessBuilder("cmd", "/c", "start", "", reportPath);
                } else {
                    openBuilder = new ProcessBuilder("xdg-open", reportPath);
                }

                openBuilder.start();
                logger.info("Allure report opened in browser");
            } else {
                logger.error("Allure report file not found at: {}", reportFile.getAbsolutePath());
            }
        } catch (Exception e) {
            logger.error("Error opening Allure report: {}", e.getMessage(), e);
        }
    }

    /**
     * Cleans up after the test class has run.
     * This method stops the Appium server, removes the driver instance, and deletes
     * the Allure results directory.
     *
     * @throws IOException If there is an error stopping the Appium server or
     *                     deleting the Allure results directory.
     */
    @AfterClass(alwaysRun = true)
    public void afterClass() {
        logger.info("Starting test class cleanup...");
        try {
            stopAppiumServer();
        } catch (Exception e) {
            logger.warn("Error during test class cleanup: {}", e.getMessage());
        }
    }

    /**
     * Cleans up after the test suite has run.
     * This method stops the Appium server, removes the driver instance, and deletes
     * the Allure results directory.
     *
     * @throws IOException If there is an error stopping the Appium server or
     *                     deleting the Allure results directory.
     */
    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        generateAllureReport();
        openAllureReport();
    }

    /**
     * Starts the Appium server programmatically.
     * Handles server initialization and waits for it to be fully started.
     */
    private synchronized void startAppiumServer() {
        if (service != null && service.isRunning()) {
            logger.info("Appium server is already running");
            return;
        }

        // Kill any existing Appium processes first
        killAppiumProcesses();

        try {
            logger.info("Starting Appium server...");

            // Configure Java Util Logging to suppress Appium logs
            java.util.logging.Logger.getLogger("io.appium").setLevel(Level.OFF);
            java.util.logging.Logger.getLogger("org.openqa.selenium").setLevel(Level.OFF);

            // Create log file with timestamps
            String timestamp = new SimpleDateFormat("yyyyMMdd-HHmmss").format(new Date());
            File logFile = new File("logs/appium_server_" + timestamp + ".log");
            logFile.getParentFile().mkdirs();

            // Try to find an available port starting from the configured port
            int basePort = ConfigReader.getIPPort();
            int maxPortAttempts = 10;
            Exception lastException = null;

            for (int portOffset = 0; portOffset < maxPortAttempts; portOffset++) {
                int currentPort = basePort + portOffset;
                try {
                    AppiumServiceBuilder builder = new AppiumServiceBuilder()
                            .withIPAddress(ConfigReader.getIPAddress())
                            .usingPort(currentPort)
                            .withLogFile(logFile)
                            .withArgument(() -> "--log-level", "error")
                            .withArgument(() -> "--log-no-colors")
                            .withArgument(() -> "--relaxed-security"); // Add relaxed security for better compatibility

                    service = AppiumDriverLocalService.buildService(builder);
                    service.start();

                    // Wait for the server to be fully started
                    long startTime = System.currentTimeMillis();
                    while (!service.isRunning() &&
                            (System.currentTimeMillis() - startTime) < APPIUM_SERVER_START_TIMEOUT_SECONDS * 1000) {
                        Thread.sleep(1000);
                    }

                    if (service.isRunning()) {
                        logger.info("Appium server started successfully on port: " + currentPort);
                        return;
                    }

                    logger.warn("Failed to start Appium server on port: " + currentPort + ", trying next port");
                    lastException = null;
                } catch (Exception e) {
                    lastException = e;
                    logger.warn("Failed to start Appium server on port " + currentPort + ": " + e.getMessage());
                    try {
                        if (service != null) {
                            service.stop();
                        }
                    } catch (Exception ignored) {
                    }
                }
            }

            if (lastException != null) {
                throw new RuntimeException("Failed to start Appium server after " + maxPortAttempts + " attempts",
                        lastException);
            } else {
                throw new RuntimeException("Failed to start Appium server after " + maxPortAttempts + " attempts");
            }
        } catch (Exception e) {
            throw new RuntimeException("Failed to start Appium server: " + e.getMessage(), e);
        }
    }

    /**
     * Stops the Appium server if it's running.
     */
    private void stopAppiumServer() {
        try {
            if (service != null && service.isRunning()) {
                logger.info("Stopping Appium server...");
                service.stop();

                // Kill any remaining Appium processes
                killAppiumProcesses();
                logger.info("Appium server stopped successfully");
            }
        } catch (Exception e) {
            logger.info("Error while stopping Appium server: {}", e.getMessage(), e);
        }
    }

    /**
     * Kills any remaining Appium processes.
     */
    private void killAppiumProcesses() {
        logger.info("Cleaning up any existing Appium processes...");
        try {
            String os = System.getProperty("os.name").toLowerCase();
            String[] command;

            if (os.contains("win")) {
                command = new String[] { "cmd", "/c", "taskkill /F /IM node.exe" };
            } else {
                // For Unix/Linux/Mac, try multiple commands
                if (new ProcessBuilder("lsof", "-t", "-i:4723").start().waitFor() == 0) {
                    // If port 4723 is in use, kill that process specifically
                    command = new String[] { "bash", "-c", "lsof -t -i:4723 | xargs kill -9" };
                } else {
                    // Otherwise try to kill any Appium processes
                    command = new String[] { "bash", "-c", "pkill -f 'appium|node.*appium'" };
                }
            }

            Process process = new ProcessBuilder(command).start();
            boolean finished = process.waitFor(10, TimeUnit.SECONDS);

            if (!finished) {
                process.destroyForcibly();
                logger.warn("Timeout while killing Appium processes");
            } else {
                logger.info("Successfully cleaned up existing Appium processes");
            }

            // Give some time for processes to fully terminate
            Thread.sleep(2000);

        } catch (Exception e) {
            logger.warn("Error during Appium process cleanup: {}", e.getMessage());
            // Continue despite cleanup errors
        }
    }
}