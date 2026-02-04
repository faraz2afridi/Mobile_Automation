# Mobile Automation Framework

A robust and scalable mobile test automation framework built with Appium, TestNG, and Allure Reporting. This framework is designed for automated testing of mobile applications on Android platforms.

## 🚀 Features

- **Cross-Platform Testing**: Supports testing on both Android platforms
- **Page Object Model**: Implements the Page Object Model (POM) design pattern for better test maintenance
- **Allure Reporting**: Comprehensive test reporting with Allure Framework
- **Data-Driven Testing**: Support for data-driven testing using TestNG
- **Parallel Execution**: Configured for parallel test execution
- **CI/CD Ready**: Easily integrable with CI/CD pipelines

## 🛠️ Prerequisites

- Java 11 or higher
- Maven 3.6.0 or higher
- Appium Server 2.0 or higher
- Android SDK
- Node.js (for Appium installation)
- Allure Command Line Tool

## 🚀 Setup Instructions

### 1. Clone the Repository
```bash
git clone <repository-url>
cd mobile_automation
```

### 2. Install Dependencies
```bash
mvn clean install
```

### 3. Start Appium Server
```bash
appium
```

### 4. Run Tests
```bash
# Run all tests
mvn clean test

# Generate Allure report
mvn allure:serve
```

## 🏗️ Project Structure

```
mobile_automation/
├── src/
│   └── test/
│       └── java/
│           ├── pages/          # Page Object classes
│           ├── tests/          # Test classes
│           ├── factory/        # Test setup and teardown
│           └── helper/         # Helper classes and utilities
├── target/                    # Compiled classes and test reports
├── pom.xml                   # Maven configuration
└── testng.xml                # TestNG test suite configuration
```

## 📊 Test Execution

### Running Specific Tests
```bash
# Run a specific test class
mvn test -Dtest=LoginTest

# Run tests with specific groups
mvn test -Dgroups=smoke
```

### Viewing Reports
After test execution, generate and view the Allure report:
```bash
mvn allure:serve
```

## 🧪 Test Cases

### Current Test Coverage
- Corporate Login functionality
- Dashboard navigation

## 🔧 Configuration

### Environment Setup
1. Set up Android SDK and required environment variables
2. Install Appium globally:
   ```bash
   npm install -g appium
   ```
3. Install Appium Doctor to verify setup:
   ```bash
   npm install -g appium-doctor
   appium-doctor --android
   ```

### Framework Configuration
- `testng.xml`: Configure test suites and test groups
- `pom.xml`: Manage project dependencies and build configuration
- `allure.properties`: Allure report configuration

## 🤝 Contributing

1. Fork the repository
2. Create your feature branch (`git checkout -b feature/AmazingFeature`)
3. Commit your changes (`git commit -m 'Add some AmazingFeature'`)
4. Push to the branch (`git push origin feature/AmazingFeature`)
5. Open a Pull Request

## 📄 License

This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.

## 📧 Contact

For any queries, please contact the project maintainers.
