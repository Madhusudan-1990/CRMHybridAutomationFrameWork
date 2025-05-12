package com.qa.crm.factory;

import java.util.Properties;

import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxOptions;

public class OptionsManager 
{
	private Properties prop;
	private ChromeOptions co;
	private FirefoxOptions fo;
	private EdgeOptions eo;
	public OptionsManager(Properties prop)
	{
		this.prop = prop;
	}
	
	public ChromeOptions getChromeOptions()
	{
		co = new ChromeOptions();
		if(Boolean.parseBoolean(prop.getProperty("headless")))
		{
			System.out.println(".....Running Chrome in headless mode.....");
			co.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
		}
		if(Boolean.parseBoolean(prop.getProperty("incognito")))
		{
			System.out.println(".....Running Chrome in incognito mode.....");
			co.addArguments("--incognito");
		}
		if(Boolean.parseBoolean(prop.getProperty("remote")))
		{
			System.out.println(".....Running Chrome in remote mode.....");
			co.setCapability("browserName", "chrome");
//			co.setCapability("enableVNC", true);	
		}
		return co;
	}
	
	public FirefoxOptions getFirefoxOptions()
	{
		fo = new FirefoxOptions();
		if(Boolean.parseBoolean(prop.getProperty("headless")))
		{
			System.out.println(".....Running Firefox in headless mode.....");
			fo.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
		}
		if(Boolean.parseBoolean(prop.getProperty("incognito")))
		{
			System.out.println(".....Running Firefox in incognito mode.....");
			fo.addArguments("--incognito");
		}
		if(Boolean.parseBoolean(prop.getProperty("remote")))
		{
			System.out.println(".....Running Firefox in remote mode.....");
			fo.setCapability("browserName", "firefox");
//			fo.setCapability("enableVNC", true);	
		}
		return fo;
	}
	
	public EdgeOptions getEdgeOptions()
	{
		eo = new EdgeOptions();
		if(Boolean.parseBoolean(prop.getProperty("headless")))
		{
			System.out.println(".....Running Edge in headless mode.....");
			eo.addArguments("--headless", "--disable-gpu", "--window-size=1920,1080");
		}
		if(Boolean.parseBoolean(prop.getProperty("incognito")))
		{
			System.out.println(".....Running Edge in incognito mode.....");
			eo.addArguments("--inPrivate");
		}
		if(Boolean.parseBoolean(prop.getProperty("remote")))
		{
			System.out.println(".....Running Edge in remote mode.....");
			eo.setCapability("browserName", "edge");
//			fo.setCapability("enableVNC", true);	
		}
		return eo;
	}
}
