package com.qa.crm.constants;

import java.util.List;

public class AppConstants 
{
	public static final int DEFAULT_SHORT_TIME_OUT=5;
	public static final int DEFAULT_MEDIUM_TIME_OUT=10;
	public static final int DEFAULT_LONG_TIME_OUT=40;
	public static final int DEFAULT_ULTRA_LONG_TIME_OUT=1200;
	public static final String LOGIN_PAGE_TITLE="Cogmento CRM";
	public static final String HOME_PAGE_TITLE="Cogmento CRM";
	
	public static final int HOME_PAGE_HEADERS_COUNT = 4;
	
	public static final String LOGIN_PAGE_FRACTION_URL="cogmento";
	
	public static final List<String> EXPECTED_HOME_HEADERS_LIST = List.of("Release Notes","Contacts activity stream","Deals","Today");
	
	public static final String FORM_PAGE_HEADER_SUFFIX=" [Active]";
	public static final long DEFAULT_THREAD_SLEEP_TIME = 5000;
	
	//*********************************Sheet  Names*************************//
	public static  final String CONTACT_SHEET_NAME = "contacts";
	public static  final String FORM_SHEET_NAME = "forms";
	
	
}
