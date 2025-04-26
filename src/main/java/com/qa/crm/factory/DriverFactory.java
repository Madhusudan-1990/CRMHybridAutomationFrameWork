package com.qa.crm.factory;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
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
	
	@Step("Initialize WebDriver with prop : {0}")
	public WebDriver initDriver(Properties prop)
	{
		isHighlight = prop.getProperty("highlight");
		
		OptionsManager optionManager = new OptionsManager(prop);
		
		String browserName = prop.getProperty("browser");
		System.out.println("Browser Name : "+browserName);
		switch(browserName.toLowerCase().trim())
		{
		case "chrome":
			WebDriverManager.chromedriver().setup();
			tlDriver.set(new ChromeDriver(optionManager.getChromeOptions()));
			break;
		case "firefox":
			WebDriverManager.firefoxdriver().setup();
			tlDriver.set(new FirefoxDriver(optionManager.getFirefoxOptions()));
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
			WebDriverManager.edgedriver().setup();
			tlDriver.set(new EdgeDriver(optionManager.getEdgeOptions()));
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
		System.out.println("Running tests on env : " +envName);
		try {
			if(envName==null)
			{

				fis = new FileInputStream("./src/test/resources/config/config.properties");
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
		return prop;
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
