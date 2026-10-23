package com.kayla.qa.base;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.testng.ITestResult;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;

/** One browser per test method, stored per thread so classes can run in parallel. */
public abstract class BaseTest {

    private static final ThreadLocal<WebDriver> DRIVER = new ThreadLocal<>();

    protected WebDriver driver() {
        return DRIVER.get();
    }

    @BeforeMethod(alwaysRun = true)
    public void setUp() {
        DRIVER.set(DriverFactory.create());
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown(ITestResult result) {
        WebDriver driver = DRIVER.get();
        if (driver == null) return;
        try {
            if (result.getStatus() == ITestResult.FAILURE) {
                saveScreenshot(driver, result.getMethod().getMethodName());
            }
        } finally {
            driver.quit();
            DRIVER.remove();
        }
    }

    private void saveScreenshot(WebDriver driver, String name) {
        try {
            File src = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            Path dir = Paths.get("screenshots");
            Files.createDirectories(dir);
            Files.copy(src.toPath(), dir.resolve(name + ".png"), java.nio.file.StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            System.err.println("Could not save screenshot: " + e.getMessage());
        }
    }
}
