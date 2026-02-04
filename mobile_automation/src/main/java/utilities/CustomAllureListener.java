package utilities;

import io.qameta.allure.Allure;
import io.qameta.allure.AllureLifecycle;
import io.qameta.allure.model.Status;
import io.qameta.allure.model.StepResult;
import io.qameta.allure.model.StatusDetails;
import io.qameta.allure.model.TestResultContainer;

import java.util.Map;
import java.util.HashMap;
import io.qameta.allure.testng.AllureTestNg;
import org.apache.logging.log4j.LogManager;
import org.testng.ITestContext;
import org.testng.ITestResult;

import java.util.Arrays;
import java.util.UUID;

/**
 * Custom Allure listener for TestNG that enhances test reporting with additional details.
 * This class extends AllureTestNg and implements ITestListener to provide custom test execution handling.
 */
public class CustomAllureListener extends AllureTestNg {

    private static final String CONTAINER_OPERATION_MESSAGE = "Container operation: {}";
    private static final ThreadLocal<String> currentStep = new ThreadLocal<>();
    private static final ThreadLocal<Map<String, TestResultContainer>> activeContainers = 
        ThreadLocal.withInitial(HashMap::new);
    private final AllureLifecycle lifecycle = Allure.getLifecycle();
    
    @Override
    public void onStart(ITestContext context) {
        String uuid = UUID.randomUUID().toString();
        Map<String, TestResultContainer> containers = activeContainers.get();
        
        // Create and start a test container
        TestResultContainer container = new TestResultContainer()
            .setUuid(uuid)
            .setName(context.getName())
            .setStart(System.currentTimeMillis());
            
        containers.put(uuid, container);
        
        try {
            lifecycle.startTestContainer(container);
        } catch (Exception e) {
            LogManager.getLogger(getClass()).trace(CONTAINER_OPERATION_MESSAGE, e.getMessage());
        }
        super.onStart(context);
    }
    
    @Override
    public void onFinish(ITestContext context) {
        Map<String, TestResultContainer> containers = activeContainers.get();
        try {
            // Create a new HashMap to avoid concurrent modification
            new HashMap<>(containers).forEach((containerId, container) -> {
                try {
                    // Update container with end time before stopping
                    container.setStop(System.currentTimeMillis());
                    lifecycle.updateTestContainer(containerId, c -> c.setStop(container.getStop()));
                    lifecycle.stopTestContainer(containerId);
                    lifecycle.writeTestContainer(containerId);
                } catch (Exception e) {
                    LogManager.getLogger(getClass()).trace(CONTAINER_OPERATION_MESSAGE, e.getMessage());
                } finally {
                    containers.remove(containerId);
                }
            });
        } finally {
            containers.clear();
            activeContainers.remove();
        }
        super.onFinish(context);
    }
    
    @Override
    public void onTestStart(ITestResult result) {
        // Create a new container for this test class if it doesn't exist
        String containerId = UUID.randomUUID().toString();
        Map<String, TestResultContainer> containers = activeContainers.get();
            
        TestResultContainer container = new TestResultContainer()
            .setUuid(containerId)
            .setName(result.getTestClass().getName())
            .setStart(System.currentTimeMillis());
            
        containers.put(containerId, container);
            
        try {
            lifecycle.startTestContainer(container);
        } catch (Exception e) {
            LogManager.getLogger(getClass()).trace(CONTAINER_OPERATION_MESSAGE, e.getMessage());
        }
        
        // Let parent handle the test case setup
        super.onTestStart(result);

        try {
            String uuid = UUID.randomUUID().toString();
            String description = result.getMethod().getDescription();
            String stepName = String.format("%s.%s",
                    result.getTestClass().getName(),
                    result.getMethod().getMethodName());

            if (description != null && !description.isEmpty()) {
                stepName = description;
            }

            // Create step result with test details
            StepResult stepResult = new StepResult()
                    .setName(stepName)
                    .setDescription(description != null ? description : "")
                    .setStatus(Status.PASSED);

            // Get or create test case UUID
            String testCaseId = (String) result.getAttribute("allure.test.case.id");
            if (testCaseId == null) {
                testCaseId = UUID.randomUUID().toString();
                result.setAttribute("allure.test.case.id", testCaseId);
            }

            lifecycle.startStep(testCaseId, uuid, stepResult);
            currentStep.set(uuid);
        } catch (Exception e) {
            LogManager.getLogger(getClass()).error("Error starting Allure step: {}", e.getMessage(), e);
        }
    }

    @Override
    public void onTestSuccess(ITestResult result) {
        stopCurrentStep(Status.PASSED);
        super.onTestSuccess(result);
    }

    @Override
    public void onTestFailure(ITestResult result) {
        stopCurrentStep(Status.FAILED, result.getThrowable());
        super.onTestFailure(result);
    }

    @Override
    public void onTestSkipped(ITestResult result) {
        stopCurrentStep(Status.SKIPPED);
        super.onTestSkipped(result);
    }

    @Override
    public void onTestFailedButWithinSuccessPercentage(ITestResult result) {
        onTestFailure(result);
    }

    private void stopCurrentStep(Status status) {
        stopCurrentStep(status, null);
    }

    private void stopCurrentStep(Status status, Throwable throwable) {
        String uuid = currentStep.get();
        if (uuid != null) {
            try {
                if (throwable != null) {
                    lifecycle.updateStep(uuid, step -> {
                        step.setStatus(status);
                        step.setStatusDetails(new StatusDetails()
                                .setMessage(throwable.getMessage())
                                .setTrace(Arrays.toString(throwable.getStackTrace())));
                    });
                } else {
                    lifecycle.updateStep(uuid, step -> step.setStatus(status));
                }
                lifecycle.stopStep(uuid);
            } finally {
                currentStep.remove();
            }
        }
    }
}