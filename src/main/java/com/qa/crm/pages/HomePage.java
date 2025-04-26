package com.qa.crm.pages;

import java.util.List;
import java.util.stream.Collectors;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;

import com.qa.crm.constants.AppConstants;
import com.qa.crm.utils.ElementUtil;

public class HomePage 
{
	private WebDriver driver;
	private ElementUtil eleUtil;
	private By homeIcon = By.xpath("//i[@class = 'home icon']");
	private By headers = By.cssSelector("div[class='header']");
	private By search = By.xpath("//input[@placeholder='Search']");
	private By searchIcon = By.xpath("//i[@class='black search icon']");
	private By calendarIcon = By.xpath("//i[@class = 'calendar icon']");
	private By contactsIcon = By.xpath("//i[@class = 'users icon']");
	private By formIcon = By.xpath("//i[@class = 'wpforms icon']");
	private By formTab = By.xpath("//span[.='Forms']");
	
	public HomePage(WebDriver driver)
	{
		this.driver = driver;
		eleUtil = new ElementUtil(driver);
	}
	
	public String getHomePageTitle()
	{
		String homePageTitle = eleUtil.waitForTitleContainsAndReturn(AppConstants.HOME_PAGE_TITLE, AppConstants.DEFAULT_SHORT_TIME_OUT);
		System.out.println("Home Page Title : "+homePageTitle);
		return homePageTitle;
	}
	
	public int getTotalHomePageHeaderCount()
	{
		return eleUtil.waitForElementsVisible(headers, AppConstants.DEFAULT_LONG_TIME_OUT).size();
	}
	public List<String> getHomePageHeaders()
	{
		List<WebElement> headersList = eleUtil.waitForElementsPresence(headers, AppConstants.DEFAULT_LONG_TIME_OUT);
		List<String> headersValueList = headersList.parallelStream().map(headers->headers.getText()).collect(Collectors.toList());
		System.out.println(headersValueList);
		return headersValueList;
	}
	public boolean isHomePageIconExists()
	{
		eleUtil.waitForElementsVisible(homeIcon,  AppConstants.DEFAULT_MEDIUM_TIME_OUT);
		return eleUtil.isElementDisplayed(homeIcon);
	}
	public boolean isCalendarIconExists()
	{
		eleUtil.waitForElementsVisible(calendarIcon,  AppConstants.DEFAULT_MEDIUM_TIME_OUT);
		return eleUtil.isElementDisplayed(calendarIcon);
	}
	public boolean isContactsIconExists()
	{
		eleUtil.waitForElementsVisible(contactsIcon,  AppConstants.DEFAULT_MEDIUM_TIME_OUT);
		return eleUtil.isElementDisplayed(contactsIcon);
	}
	public ContactsPage doSearch(String searchKey)
	{
		System.out.println("Search Key ==> "+ searchKey);
		eleUtil.doSendKeys(search, searchKey);
		eleUtil.pressEnterViaKeyboard(AppConstants.DEFAULT_THREAD_SLEEP_TIME);
		return new ContactsPage(driver);
	}
	
	public FormPage navigateToFormPage()
	{
		eleUtil.waitForElementsVisible(formIcon,  AppConstants.DEFAULT_LONG_TIME_OUT);
		try {
			eleUtil.ParentChildMenu(formIcon, formTab);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new FormPage(driver);
	}
	
	public ContactsPage navigateToContactPage()
	{
		eleUtil.doClick(contactsIcon, AppConstants.DEFAULT_LONG_TIME_OUT);
		return new ContactsPage(driver);
	}
}
