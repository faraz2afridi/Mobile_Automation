package helper;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.io.File;
import java.io.IOException;

public class TestDataReader {
    private static final String TEST_DATA_PATH = "src/main/java/testData/TestData.json";
    private static volatile JsonNode testData;
    private static final Object lock = new Object();

    private static void loadTestData() {
        if (testData == null) {
            synchronized (lock) {
                if (testData == null) {
                    ObjectMapper mapper = new ObjectMapper();
                    try {
                        testData = mapper.readTree(new File(TEST_DATA_PATH));
                    } catch (IOException e) {
                        throw new RuntimeException("Failed to load test data from " + TEST_DATA_PATH, e);
                    }
                }
            }
        }
    }
    
    public static String getValue(String key) {
        loadTestData();
        return testData.path(key).asText();
    }
}
