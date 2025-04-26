package com.qa.crm.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.qa.crm.constants.AppConstants;
import com.qa.crm.utils.ElementUtil;

public class PersonDetailsPage 
{
	private WebDriver driver;
	private ElementUtil eleUtil;
	
	private By personHeader = By.xpath("//span[@class='selectable ']");
	private By contactsIcon = By.xpath("//i[@class = 'users icon']");
	public PersonDetailsPage(WebDriver driver)
	{
		this.driver = driver;
		eleUtil = new ElementUtil(driver);
		
	}
	
	public String getPersonHeader()
	{
		String personHeaderValue = eleUtil.waitForElementVisible(personHeader, AppConstants.DEFAULT_SHORT_TIME_OUT).getText();
		System.out.println("Person Header =======> "+personHeaderValue);
		return personHeaderValue;
	}
}
