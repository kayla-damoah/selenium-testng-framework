package com.kayla.qa.base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

/** Builds a WebDriver from -Dbrowser=chrome|firefox and -Dheadless=true|false. Selenium Manager fetches the driver binary. */
public final class DriverFactory {

    private DriverFactory() {}

    public static WebDriver create() {
        boolean headless = Boolean.parseBoolean(System.getProperty("headless", "true"));
        String browser = System.getProperty("browser", "chrome").toLowerCase();

        switch (browser) {
            case "firefox": {
                FirefoxOptions options = new FirefoxOptions();
                if (headless) options.addArguments("-headless");
                return new FirefoxDriver(options);
            }
            case "chrome": {
                ChromeOptions options = new ChromeOptions();
                if (headless) options.addArguments("--headless=new");
                options.addArguments("--window-size=1400,1000", "--no-sandbox", "--disable-dev-shm-usage");
                return new ChromeDriver(options);
            }
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }
}
