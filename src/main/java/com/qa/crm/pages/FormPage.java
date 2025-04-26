package com.qa.crm.pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;

import com.qa.crm.constants.AppConstants;
import com.qa.crm.utils.ElementUtil;

public class FormPage 
{
	private WebDriver driver;
	private ElementUtil eleUtil;
	
	private By addBtn = By.xpath("//button[@class='ui linkedin button']");
	private By newFormHeader = By.xpath("//span[text()='New Form']");
	private By formName = By.name("name");
	private By introductionText = By.xpath("//textarea[@name='intro']");
	private By completionText = By.xpath("//textarea[@name='outro']");
	private By saveBtn = By.xpath("//button[@class='ui linkedin button']");
	private By savedFormName = By.xpath("//span[@class='selectable ']");
	private By formIcon = By.xpath("//i[@class = 'wpforms icon']");
	private By formTab = By.xpath("//span[.='Forms']");
	
	public FormPage(WebDriver driver)
	{
		this.driver = driver;
		eleUtil = new ElementUtil(driver);
	}
	public boolean addForm()
	{
		eleUtil.doClick(addBtn);
		return eleUtil.isElementDisplayed(newFormHeader);
	}
	public String submitForm(String formNameText, String introText, String completeText,String savedFormNameText)
	{
		
		eleUtil.doSendKeys(formName, formNameText);
		eleUtil.doSendKeys(introductionText, introText);
		eleUtil.doSendKeys(completionText, completeText);
		eleUtil.doClick(saveBtn);
		savedFormNameText = eleUtil.doGetElementText(savedFormName)+AppConstants.FORM_PAGE_HEADER_SUFFIX;
		return savedFormNameText;
		
	}
	public String createdFormHeader() throws InterruptedException
	{
		eleUtil.addThreadSleep(AppConstants.DEFAULT_THREAD_SLEEP_TIME);
		String savedFormNameText = eleUtil.doGetElementText(savedFormName);
		return savedFormNameText;
	}
	
	public FormPage navigateToFormPage() throws InterruptedException
	{
		eleUtil.addThreadSleep(AppConstants.DEFAULT_THREAD_SLEEP_TIME);
		try {
			eleUtil.ParentChildMenu(formIcon, formTab);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new FormPage(driver);
	}
}
