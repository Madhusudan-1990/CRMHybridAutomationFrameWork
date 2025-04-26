package com.qa.crm.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.qa.crm.constants.AppConstants;
import com.qa.crm.utils.ElementUtil;

public class ContactsPage 
{
	private WebDriver driver;
	private ElementUtil eleUtil;
	
	private By contactsLabel = By.xpath("//span[text()='Contacts' and @class='selectable ']");
	private By firstName = By.name("first_name");
	private By lastName = By.name("last_name");
	private By company = By.xpath("//label[.='Company']/parent::div/div/input");
	private By saveBtn = By.xpath("//button[@class='ui linkedin button']");
	private String beforeXpath = "//a[.='";
	private String afterXpath = "']/parent::td/parent::tr/td[1]";
	private WebElement contactSelect;
	private By createContactBtn = By.xpath("//button[.='Create']");
	private By searchHeader = By.xpath("//span[@class='selectable ']");
	private By results = By.xpath("//td/a");
	private By contactsCount = By.xpath("//div[.='Contact']/parent::div/div[1]/div");
	public ContactsPage(WebDriver driver)
	{
		this.driver = driver;
		eleUtil = new ElementUtil(driver);
	}
	
	public boolean verifyContactsLabel()
	{
		return eleUtil.isElementDisplayed(contactsLabel);
	}
	
	
	public void selectContactsByName(String name)
	{
		contactSelect =	driver.findElement(By.xpath(beforeXpath+name+afterXpath));
		contactSelect.click();
	}
	public boolean contactSelected()
	{
		return contactSelect.isSelected();
	}
	
	public PersonDetailsPage createNewContact(String ftName, String ltName, String comp) throws InterruptedException
	{
		eleUtil.doClick(createContactBtn,AppConstants.DEFAULT_SHORT_TIME_OUT);
		eleUtil.doSendKeys(firstName, ftName);
		eleUtil.doSendKeys(lastName, ltName);
		eleUtil.doSendKeys(company, comp);
		eleUtil.doClick(saveBtn);
		eleUtil.addThreadSleep(AppConstants.DEFAULT_THREAD_SLEEP_TIME);
		return new PersonDetailsPage(driver);
	}
	
	public String getSearchHeader()
	{
		String searchHeaderValue = eleUtil.waitForElementVisible(searchHeader,AppConstants.DEFAULT_SHORT_TIME_OUT).getText(); 
		return searchHeaderValue;
	}
	
	public int getcontactCount()
	{
		String contactCountText = eleUtil.doGetElementText(contactsCount);
		int contactCount = Integer.parseInt(contactCountText);
		System.out.println("Contact count =======> "+contactCount);
		return contactCount;
	}
	public int getResultsSearchCount()
	{
		int resultCount = eleUtil.waitForElementsVisible(results,AppConstants.DEFAULT_SHORT_TIME_OUT).size();
		System.out.println("Search result count =======> "+resultCount);
		return resultCount;
	}
	
	public PersonDetailsPage selectContact(String contactName)
	{
		//Dynamic By Locators(Dynamic Contact Name)
		System.out.println("Selecting the Contact : "+contactName);
		eleUtil.doClick(By.linkText(contactName),AppConstants.DEFAULT_MEDIUM_TIME_OUT);
		return new PersonDetailsPage(driver);
	}
	
	
}	
