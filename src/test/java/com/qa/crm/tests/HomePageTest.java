package com.qa.crm.tests;

import java.util.List;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.qa.crm.base.BaseTest;
import com.qa.crm.constants.AppConstants;
import com.qa.crm.listeners.AnnotationTransformer;
import com.qa.crm.listeners.ExtentReportListener;
@Listeners({AnnotationTransformer.class,ExtentReportListener.class})
public class HomePageTest extends BaseTest
{
	@BeforeClass
	public void homeSetup()
	{
		homePage = loginPage.doLogin(prop.getProperty("username"),prop.getProperty("password"));
	}
	
	@Test(priority = 1)
	public void homePageTitleTest()
	{
		String actualTitle = homePage.getHomePageTitle();
		Assert.assertEquals(actualTitle,AppConstants.HOME_PAGE_TITLE);
	}
	
	@Test(priority = 2)
	public void isHomePageIconTest()
	{
		Assert.assertTrue(homePage.isHomePageIconExists());
	}
	
	@Test(priority = 3)
	public void isCalendarPageIconTest()
	{
		Assert.assertTrue(homePage.isCalendarIconExists());
	}
	
	@Test(priority = 4)
	public void isContactsPageIconTest()
	{
		Assert.assertTrue(homePage.isContactsIconExists());
	}
	
	@Test(priority = 5)
	public void homePageHeadersCountTest()
	{
		Assert.assertEquals(homePage.getTotalHomePageHeaderCount(),AppConstants.HOME_PAGE_HEADERS_COUNT);
	}
	
	@Test(priority = 6)
	public void homePageHeadersTest()
	{
		List<String> headersList = homePage.getHomePageHeaders();
		Assert.assertEquals(headersList,AppConstants.EXPECTED_HOME_HEADERS_LIST);
	}
	
}
