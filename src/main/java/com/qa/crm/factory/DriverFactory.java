package com.qa.crm.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Properties;

import org.apache.commons.io.FileUtils;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.io.FileHandler;
import org.openqa.selenium.remote.RemoteWebDriver;

import com.qa.crm.errors.AppError;
import com.qa.crm.exceptions.BrowserException;
import com.qa.crm.exceptions.*;
import io.github.bonigarcia.wdm.WebDriverManager;
import io.qameta.allure.Step;

public class DriverFactory 
{
	/**
	 * This method is used to init the driver on the basis of given browsername.
	 * @param browserName
	 * @return it returns driver
	 */
	WebDriver driver;
	Properties prop;
	public static String isHighlight;
	//Thread Local class is used to distribute the driver across all the threads.
	public static ThreadLocal<WebDriver>tlDriver = new ThreadLocal<WebDriver>();
	OptionsManager optionManager;
	
	@Step("Initialize WebDriver with prop : {0}")
	public WebDriver initDriver(Properties prop)
	{
		isHighlight = prop.getProperty("highlight");
		
		optionManager = new OptionsManager(prop);
		
		String browserName = prop.getProperty("browser");
		System.out.println("Browser Name : "+browserName);
		switch(browserName.toLowerCase().trim())
		{
		case "chrome":
			if(Boolean.parseBoolean(prop.getProperty("remote")))
			{
				// Run testcase on remote/container
				init_remoteDriver("chrome");
			}
			else
			{
				// Run testcase in local
				setupDriver("chrome");
				tlDriver.set(new ChromeDriver(optionManager.getChromeOptions()));
			}
			
			
			break;
		case "firefox":
			if(Boolean.parseBoolean(prop.getProperty("remote")))
			{
				// Run testcase on remote/container
				init_remoteDriver("firefox");
			}
			else
			{
				// Run testcase in local
			setupDriver("firefox");
			tlDriver.set(new FirefoxDriver(optionManager.getFirefoxOptions()));
			}
			
			break;
		case "safari":
			WebDriverManager.safaridriver().setup();
			tlDriver.set(new SafariDriver());
			break;
		case "ie":
			WebDriverManager.iedriver().setup();
			tlDriver.set(new InternetExplorerDriver());
			break;		
		case "edge":
			if(Boolean.parseBoolean(prop.getProperty("remote")))
			{
				// Run testcase on remote/container
				init_remoteDriver("edge");
			}
			else
			{
				// Run testcase in local
				WebDriverManager.edgedriver().setup();
				tlDriver.set(new EdgeDriver(optionManager.getEdgeOptions()));
			}
			break;	

		default:
			System.out.println(AppError.INVALID_BROWSER_MSG + browserName + "is invalid browser");
			throw new BrowserException(AppError.INVALID_BROWSER_MSG + browserName);	

		}
		getDriver().manage().window().maximize();
		getDriver().manage().deleteAllCookies();
		getDriver().get(prop.getProperty("url"));
		return getDriver();
	}
	
	/**
	 * Resolves the browser driver. Inside the docker/k8s image the driver is installed by apt,
	 * so we point selenium straight at it. On a laptop, webdrivermanager downloads it.
	 * @param browserName
	 */
	private void setupDriver(String browserName)
	{
		String driverPath = prop.getProperty("driverpath");
		if(driverPath!=null && !driverPath.trim().isEmpty())
		{
			System.out.println("Using driver from image: " + driverPath);
			System.setProperty("webdriver." + browserName.toLowerCase().trim() + ".driver", driverPath);
			return;
		}
		switch(browserName.toLowerCase().trim())
		{
		case "chrome":
			WebDriverManager.chromedriver().setup();
			break;
		case "firefox":
			WebDriverManager.firefoxdriver().setup();
			break;
		default:
			break;
		}
	}

	private void init_remoteDriver(String browserName)
	{
		try {
		switch(browserName.toLowerCase().trim())
		{
		
		case "chrome":
		tlDriver.set(new RemoteWebDriver(new URL(prop.getProperty("huburl")),optionManager.getChromeOptions()));
				break;
				
		case "firefox":
			tlDriver.set(new RemoteWebDriver(new URL(prop.getProperty("huburl")),optionManager.getFirefoxOptions()));
			break;
			
		default:
			System.out.println("Please pass valid Remote Browser Name ....");
			throw new BrowserException(AppError.INVALID_BROWSER_MSG);

		} 
		
		}
		catch (MalformedURLException e) {
			
			e.printStackTrace();
		}
		
	}

	/**
	 * This method is used to return the driver  with threadlocal
	 * @return
	 */
	public static WebDriver getDriver()
	{
		return tlDriver.get();
	}

	/**
	 * This method is used to initialize the property from the config file.
	 * @return
	 */
	
	//mvn clean install -Denv="qa"
	public Properties initProp()
	{
		prop = new Properties();
		FileInputStream fis = null;

		String envName = System.getProperty("env");
		if(envName==null)
		{
			//running inside docker/k8s, config is injected as an env var
			envName = System.getenv("ENV");
		}
		System.out.println("Running tests on env : " +envName);
		try {
			if(envName==null)
			{

				fis = new FileInputStream("./src/test/resources/config/qa.config.properties");
			}
			else
			{
				switch(envName)
				{
				case "qa":
					fis = new FileInputStream("./src/test/resources/config/qa.config.properties");
					break;
				case "dev":
					fis = new FileInputStream("./src/test/resources/config/dev.config.properties");
					break;
				case "stage":
					fis = new FileInputStream("./src/test/resources/config/stage.config.properties");
					break;
				case "uat":
					fis = new FileInputStream("./src/test/resources/config/uat.config.properties");
					break;
				case "prod":
					fis = new FileInputStream("./src/test/resources/config/config.properties");
					break;
				default:
					System.out.println("Please provide valid env name..." +envName);
					throw new FrameworkException("INVALID ENV NAME");
				}
			}
			prop.load(fis);
		}
		catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		//docker/kubernetes overrides: container env vars always win over the .properties file
		overrideFromEnv(prop, "ENV_NAME");
		overrideFromEnv(prop, "BROWSER");
		overrideFromEnv(prop, "URL");
		overrideFromEnv(prop, "USERNAME");
		overrideFromEnv(prop, "PASSWORD");
		overrideFromEnv(prop, "HEADLESS");
		overrideFromEnv(prop, "INCOGNITO");
		overrideFromEnv(prop, "HIGHLIGHT");
		overrideFromEnv(prop, "REMOTE");
		overrideFromEnv(prop, "HUB_URL");
		overrideFromEnv(prop, "CONTAINER");
		overrideFromEnv(prop, "CHROME_BINARY");
		overrideFromEnv(prop, "DRIVER_PATH");
		overrideFromEnv(prop, "SHARD_INDEX");
		overrideFromEnv(prop, "SHARD_COUNT");
		return prop;
	}

	/**
	 * Overriding single property with the env variable, if provided.
	 * e.g. HUB_URL=http://selenium-hub:4444/wd/hub docker compose up tests
	 * @param prop
	 * @param key
	 */
	private void overrideFromEnv(Properties prop, String key)
	{
		String value = System.getenv(key);
		if(value!=null && !value.trim().isEmpty())
		{
			//HUB_URL -> huburl
			prop.setProperty(key.replace("_", "").toLowerCase(), value.trim());
			System.out.println("Overriding [" + key.replace("_", "").toLowerCase() + "] with env variable [" + key + "] = " + value);
		}
	}
	
	/**
	 * take screenshot
	 */

	public static String getScreenshot(String methodName) {
		File srcFile = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);// temp dir
		String path = System.getProperty("user.dir") + "/screenshot/" + methodName + "_" + System.currentTimeMillis()+ ".png";
		File destination = new File(path);
		try {
			FileUtils.copyFile(srcFile, destination);
		} catch (IOException e) {
			e.printStackTrace();
		}

		return path;
	}
}
