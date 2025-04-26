package com.qa.crm.tests;

import org.testng.Assert;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Listeners;
import org.testng.annotations.Test;

import com.qa.crm.base.BaseTest;
import com.qa.crm.constants.AppConstants;
import com.qa.crm.listeners.AnnotationTransformer;
import com.qa.crm.listeners.ExtentReportListener;
import com.qa.crm.utils.ExcelUtil;
@Listeners({AnnotationTransformer.class,ExtentReportListener.class})
public class FormPageTest extends BaseTest
{
	@BeforeClass
	public void formSetup()
	{
		homePage = loginPage.doLogin(prop.getProperty("username"),prop.getProperty("password"));
		formPage = homePage.navigateToFormPage();
	}
	
	@DataProvider
	public Object[][] getFormData()
	{
		return ExcelUtil.getTestData(AppConstants.FORM_SHEET_NAME);
	}
	
	@Test(dataProvider = "getFormData")
	public void formSubmitPageTest(String formNameText, String introText, String completeText) throws InterruptedException
	{
		formPage.addForm();
		formPage.submitForm(formNameText, introText, completeText,formNameText+AppConstants.FORM_PAGE_HEADER_SUFFIX);
		Assert.assertEquals(formNameText+AppConstants.FORM_PAGE_HEADER_SUFFIX,formPage.createdFormHeader());
		formPage.navigateToFormPage();
		
	}

}
