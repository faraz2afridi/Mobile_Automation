package utilities;

import io.appium.java_client.AppiumDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.PointerInput;
import org.openqa.selenium.interactions.Sequence;

import java.time.Duration;
import java.util.Collections;

public class SwipeUtils {

    private final AppiumDriver driver;

    public SwipeUtils(AppiumDriver driver) {
        this.driver = driver;
    }

    /**
     * Swipe left within a specific element (like a carousel or horizontal list)
     * @param element element inside which swipe should happen
     * @param durationMs swipe duration in milliseconds
     */
    public void swipeLeftInsideElement(WebElement element, int durationMs) {
        // Get element boundaries
        int startX = element.getRect().x + (int)(element.getRect().width * 0.8);
        int endX   = element.getRect().x + (int)(element.getRect().width * 0.2);
        int centerY = element.getRect().y + (element.getRect().height / 2);

        performSwipe(startX, centerY, endX, centerY, durationMs);
    }

    private void performSwipe(int startX, int startY, int endX, int endY, int durationMs) {
        PointerInput finger = new PointerInput(PointerInput.Kind.TOUCH, "finger");
        Sequence swipe = new Sequence(finger, 1);

        swipe.addAction(finger.createPointerMove(Duration.ZERO, PointerInput.Origin.viewport(), startX, startY));
        swipe.addAction(finger.createPointerDown(PointerInput.MouseButton.LEFT.asArg()));
        swipe.addAction(finger.createPointerMove(Duration.ofMillis(durationMs),
                PointerInput.Origin.viewport(), endX, endY));
        swipe.addAction(finger.createPointerUp(PointerInput.MouseButton.LEFT.asArg()));

        driver.perform(Collections.singletonList(swipe));
    }
}