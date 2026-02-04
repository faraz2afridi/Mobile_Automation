package utilities;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Paths;
import java.util.Properties;

public class ConfigReader {
    private static final Properties properties = new Properties();
    private static final String CONFIG_FILE = "/config.properties"; // This is correct for resources directory

    static {
        loadProperties();
    }

    private static void loadProperties() {
        try (InputStream input = ConfigReader.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new RuntimeException("Unable to find " + CONFIG_FILE);
            }
            properties.load(input);
        } catch (IOException e) {
            throw new RuntimeException("Error loading properties file: " + CONFIG_FILE, e);
        }
    }

    public static String getProperty(String key) {
        return properties.getProperty(key);
    }

    public static int getIntProperty(String key, int defaultValue) {
        String value = getProperty(key);
        if (value == null) {
            return defaultValue;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }

    // Android specific methods
    public static String getAndroidAppPackage() {
        return getProperty("android.app.package");
    }

    public static String getAndroidAppActivity() {
        return getProperty("android.app.activity");
    }

    // iOS specific methods
    public static String getIOSBundleId() {
        return getProperty("ios.bundle.id");
    }

    public static String getIOSAppPath() {
        String path = getProperty("ios.app");
        if (path.startsWith("~/")) {
            return Paths.get(System.getProperty("user.home"), path.substring(2)).toString();
        }
        return path;
    }

    public static String getIOSDeviceName() {
        return getProperty("ios.device.name");
    }

    public static String getIOSPlatformVersion() {
        return getProperty("ios.platform.version");
    }

    public static String getIOSUdid() {
        return getProperty("ios.udid");
    }

    public static String getIOSSimulatorDevice() {
        return getProperty("ios.simulator.device");
    }

    public static boolean getIOSAutoAcceptAlerts() {
        return Boolean.parseBoolean(getProperty("ios.auto.accept.alerts"));
    }

    public static boolean getShowIOSLog() {
        return Boolean.parseBoolean(getProperty("ios.show.ios.log"));
    }

    // Common device methods
    public static String getDeviceName() {
        return getProperty("device.name");
    }

    public static String getUdid() {
        return getProperty("device.udid");
    }

    public static String getAutomationName() {
        return getProperty("device.automation.name");
    }

    public static String getPlatform() {
        return getProperty("platform");
    }

    public static boolean getClearAppData() {
        return Boolean.parseBoolean(getProperty("clear.app.data"));
    }

    public static String getAppiumServerUrl() {
        return getProperty("appium.server.url");
    }

    public static boolean getEmulatorEnabled() {
        return Boolean.parseBoolean(getProperty("emulator.enabled"));
    }

    public static String getAvdName() {
        return getProperty("emulator.avd");
    }
    
    public static String getAvd() {
        return getAvdName(); // For backward compatibility
    }

    public static String getPlatformVersion() {
        return getProperty("emulator.platform.version");
    }

    public static boolean isHeadless() {
        return Boolean.parseBoolean(getProperty("emulator.headless"));
    }

    public static long getImplicitWait() {
        return Long.parseLong(getProperty("implicit.wait"));
    }

    public static long getCommandTimeout() {
        return Long.parseLong(getProperty("command.timeout"));
    }

    public static boolean getForceAppLaunch() {
        return Boolean.parseBoolean(getProperty("forceAppLaunch"));
    }

    public static String getIPAddress() {return getProperty("appium.server.ip");}

    public static int getIPPort() {return getIntProperty("appium.server.port", 4723);}
}
