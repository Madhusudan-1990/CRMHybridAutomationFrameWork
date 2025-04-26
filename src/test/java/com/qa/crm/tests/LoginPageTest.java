package com.qa.crm.tests;

import org.testng.Assert;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.google.errorprone.annotations.Keep;
import com.qa.crm.base.BaseTest;
import com.qa.crm.constants.AppConstants;
import com.qa.crm.listeners.AnnotationTransformer;
import com.qa.crm.listeners.ExtentReportListener;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.qameta.allure.Issue;
import io.qameta.allure.Link;
import io.qameta.allure.Owner;
import io.qameta.allure.Severity;
import io.qameta.allure.SeverityLevel;
import io.qameta.allure.Story;
@Listeners({AnnotationTransformer.class,ExtentReportListener.class})
@Epic("Epic SUP-1000 : Design CRM Login Page")
@Feature("Feature SUP-1010 : Login Feature")
@Story("Story SUP-1020 : All features related to login page")
@Owner("Madhusudan Jayasimha")
@Link(name = "Login Page",url="https://ui.cogmento.com/")
public class LoginPageTest extends BaseTest
{
	@Severity(SeverityLevel.NORMAL)
	@Description("Login Page Title Test")
	@Feature("Feature 400 : Title Test Feature")
	@Owner("Madhusudan Jayasimha")
	@Test
	public void loginPageTitleTest()
	{
		String actualTitle = loginPage.getLoginPageTitle();
		Assert.assertEquals(actualTitle,AppConstants.LOGIN_PAGE_TITLE);
	}
	
	@Severity(SeverityLevel.NORMAL)
	@Description("Login Page URL Test")
	@Feature("Feature 400 : URL Test Feature")
	@Owner("Madhusudan Jayasimha")
	@Test
	public void loginPageURLTest()
	{
		String actualUrl = loginPage.getCurrentUrl();
		Assert.assertTrue(actualUrl.contains(AppConstants.LOGIN_PAGE_FRACTION_URL));
	}
	@Severity(SeverityLevel.MINOR)
	@Description("Login Page BellIcon Test")
	@Feature("Feature 400 : BellIcon Test Feature")
	@Owner("Madhusudan Jayasimha")
	@Test
	public void bellIconExist()
	{
		Assert.assertTrue(loginPage.isBellIconImagePresent());
	}
	@Severity(SeverityLevel.BLOCKER)
	@Description("Login Page Login Test")
	@Feature("Feature 400 : Login Test Feature")
	@Test(priority = Integer.MAX_VALUE)
	@Owner("Madhusudan Jayasimha")
	@Issue("JiraTicket - SUP100202")
	public void loginTest()
	{
		homePage = loginPage.doLogin(prop.getProperty("username"),prop.getProperty("password"));
		Assert.assertEquals(homePage.getHomePageTitle(),AppConstants.LOGIN_PAGE_TITLE);
	}
}
