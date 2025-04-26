package com.qa.crm.base;

import java.util.Properties;

import org.openqa.selenium.WebDriver;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Optional;
import org.testng.annotations.Parameters;
import org.testng.asserts.SoftAssert;

import com.qa.crm.factory.DriverFactory;
import com.qa.crm.pages.ContactsPage;
import com.qa.crm.pages.FormPage;
import com.qa.crm.pages.HomePage;
import com.qa.crm.pages.LoginPage;
import com.qa.crm.pages.PersonDetailsPage;

import io.qameta.allure.Step;

public class BaseTest 
{
	
	WebDriver driver;
	DriverFactory df;
	protected Properties prop;
	
	protected LoginPage loginPage;
	protected HomePage homePage;
	protected PersonDetailsPage personDetailPage;
	protected FormPage formPage;
	protected ContactsPage contactPage;
	
	protected SoftAssert softAssert;
	
	@Step("Setup with browser : {0}")
	@BeforeTest
	@Parameters({"browser"})
	public void setup(@Optional String browserName)
	{
		df = new DriverFactory();
		prop = df.initProp();
		
		//Check if the browser parameter is coming from testng.xml or not.
		if(browserName!=null)
		{
			prop.setProperty("browser", browserName);
		}
		driver = df.initDriver(prop);
		loginPage = new LoginPage(driver);
		softAssert = new  SoftAssert();
	}
	
	@Step("Close the browser")
	@AfterTest
	public void tearDown()
	{
		driver.close();
	}
}
