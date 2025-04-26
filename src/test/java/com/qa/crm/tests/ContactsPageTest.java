package com.qa.crm.tests;
import java.awt.desktop.AppHiddenEvent;
import java.io.IOException;

import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.openqa.selenium.By;
import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;
import com.qa.crm.base.BaseTest;
import com.qa.crm.constants.AppConstants;
import com.qa.crm.listeners.AnnotationTransformer;
import com.qa.crm.listeners.ExtentReportListener;
import com.qa.crm.utils.*;
@Listeners({AnnotationTransformer.class,ExtentReportListener.class})
public class ContactsPageTest extends BaseTest
{
	@BeforeClass
	public void homeSetup()
	{
		homePage = loginPage.doLogin(prop.getProperty("username"),prop.getProperty("password"));
		contactPage = homePage.navigateToContactPage();
	}
	
	@Test(priority = 1)
	public void verifyContactsPageLabelTest()
	{
		boolean flag = contactPage.verifyContactsLabel();
		Assert.assertTrue(flag,"Failed to verify Contacts Page !!!");
		
	}
	
	@DataProvider
	public Object[][] getCRMTestData() 
	{
		return ExcelUtil.getTestData(AppConstants.CONTACT_SHEET_NAME);
	}
	
	@Test(priority = 2,dataProvider = "getCRMTestData")
	public void createNewContactTest(String fName, String lName, String company) throws InterruptedException
	{
		contactPage.createNewContact(fName, lName, company);
		homePage.navigateToContactPage();
		
		
	}

	@DataProvider
	public Object[][] getSearchData()
	{
		return new Object[][]
				{
					{"Joe","Joe Simon"},
					{"Bret","Bret Lee"},
					{"Andy","Andy Flower"}
				};
	}
	@Test(priority = 3,dataProvider = "getSearchData")
	public void searchTest(String searchKey, String searchValue)
	{
		contactPage = homePage.doSearch(searchKey);
		softAssert.assertEquals(contactPage.getResultsSearchCount(),contactPage.getcontactCount());
		personDetailPage = contactPage.selectContact(searchValue); 
		softAssert.assertEquals(personDetailPage.getPersonHeader(), searchValue);
		softAssert.assertAll();
		
	}
}
