package com.csci599.internationalization;

import java.util.concurrent.TimeUnit;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.firefox.FirefoxDriver;

public class WebDriverSingleton {
	static WebDriverSingleton instance = null;
	static WebDriver driver = null;

	private WebDriverSingleton() {
			driver = new FirefoxDriver();
	}

	public static WebDriverSingleton getInstance() {
		if (instance == null) {
			instance = new WebDriverSingleton();
		}
		return instance;
	}

	public WebDriver getDriver() {
		return driver;
	}

	public static void closeDriver() {
		if (instance != null) {
			driver.quit();
			instance = null;
		}
	}

	public static void restartDriver() {
		closeDriver();
		getInstance();
	}

	public void loadPage(String htmlFileFullPath) {
		System.out.println("Loading page " + htmlFileFullPath);
		String urlString = "";

		urlString = "file:///" + htmlFileFullPath;

		try {
			driver.manage().timeouts().pageLoadTimeout(10, TimeUnit.SECONDS);
			driver.get(urlString);
		} catch (Exception e) {
			// restart the browser
			restartDriver();
			driver.get(urlString);
		}
	}
}
