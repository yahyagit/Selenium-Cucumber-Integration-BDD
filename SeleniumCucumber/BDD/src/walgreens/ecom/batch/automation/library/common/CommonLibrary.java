package walgreens.ecom.batch.automation.library.common;

import static org.junit.Assert.assertEquals;

import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.Vector;

import javax.sql.rowset.CachedRowSet;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.commons.lang.StringUtils;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoSuchElementException;
import org.openqa.selenium.Point;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.openqa.selenium.support.ui.Select;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import walgreens.ecom.batch.framework.EcommTestRunner;
import walgreens.ecom.batch.framework.common.BrowserFactory;
import walgreens.ecom.batch.framework.common.beans.OrderBean;
import walgreens.ecom.batch.framework.common.beans.ScenarioBean;
import walgreens.ecom.batch.framework.common.beans.StepBean;
import walgreens.ecom.batch.framework.common.constants.IGlobalVariables;
import walgreens.ecom.batch.framework.common.dao.AutomationDBManager;
import walgreens.ecom.batch.framework.common.dao.QualityCenterDBManager;
import walgreens.ecom.batch.framework.common.dao.TestDataDBManager;
import walgreens.ecom.batch.framework.common.utils.RemoteShellUtils;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.Session;

import cucumber.table.DataTable;

public class CommonLibrary implements IGlobalVariables {

	public static Map<String, OrderBean> orderBeansMap = null;

	/**
	 ************************************************************* 
	 * @Purpose - Method to search a Product using Serach Text Box
	 * @author - Imran
	 * @Created -
	 * @Modified By -Siva
	 * @Modified Date -15-Nov-12
	 ************************************************************* 
	 */
	public static void searchProduct(EventFiringWebDriver browser, StepBean stepBean, Map<String, List<String>> dataMap) {
		String status = PASS;
		String HomePageSearch_INPUT = "input[id*=query]~CSS";
		// String SearchPageHeading_TEXT =
		// "//h1[contains(text(),'Search Results for')]~XPATH";
		Map<String, String> messagesMap = null;
		try {
			if (messagesMap == null) {
				messagesMap = new HashMap<String, String>();
				stepBean.setStepStatusMessages(messagesMap);
			}
			String keyword = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "Keyword");
			if (isElementPresentVerification(HomePageSearch_INPUT, browser)) {
				WebElement searchBox = getElementByProperty(HomePageSearch_INPUT, browser);
				searchBox.clear();
				searchBox.sendKeys(keyword);
				searchBox.submit();
			} else {
				throw new Exception("Selenium is not able to verify the Search Text Box!");
			}
			/*
			 * if (!isElementPresentVerification(SearchPageHeading_TEXT,
			 * browser)) { status = WARNING; statusDescription =
			 * "Not able to Verify the Search Results page after searching for the product."
			 * ; }
			 */
		} catch (Exception e) {
			status = FAIL;
			messagesMap.put("An Exception Occured:", e.getMessage());
			LogIt(e, null, stepBean);
		} finally {
			stepBean.setStepStatus(status);
		}
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to select Family Member(Old)
	 * @author - Imran
	 * @Created -
	 * @Modified By -Saravanan M
	 * @Modified Date -26-Aug-2013
	 ************************************************************* 
	 */
	public static void familyMemberSelector(EventFiringWebDriver browser, StepBean stepBean, Map<String, List<String>> dataMap) {
		String status = FAIL;
		String FamilyMemberSelector_SELECT = "//select[@id='selectFamilyMember' or 'selectMemberView']~XPATH";
		String ManageYourPrescriptions_H = "//div[@id='header_bar']//h1[contains(text(),'Manage Prescriptions')]~XPATH";
		Select selectBox = null;
		String familyMemberOption = null;
		Map<String, String> messagesMap = null;
		try {
			if (messagesMap == null) {
				messagesMap = new HashMap<String, String>();
				stepBean.setStepStatusMessages(messagesMap);
			}
			familyMemberOption = dataMap.get("FamilyMemberOption").get(0);
			// Added Saravanan M -26-Aug-2013-In order to select the family
			// member from Refill hub page
			if (isElementPresentVerification(ManageYourPrescriptions_H, browser)) {
				if (refillhubFamilyMemberSelector(browser, familyMemberOption)) {
					status = PASS;
				}
			} else if (StringUtils.isNotBlank(familyMemberOption)) {
				if ("-".equals(familyMemberOption)) {
					// No Change in Selection
					status = PASS;
				} else {
					WebElement familyMemberSelector = getElementByProperty(FamilyMemberSelector_SELECT, browser);
					selectBox = new Select(familyMemberSelector);
					if ("self".equalsIgnoreCase(familyMemberOption)) {
						if (selectBox != null) {
							selectBox.selectByIndex(0);
							status = PASS;
						}
					} else {
						if (selectBox != null) {
							selectBox.selectByIndex(Integer.parseInt(familyMemberOption));
							status = PASS;
						}
					}
				}
			}
		} catch (Exception e) {
			status = FAIL;
			messagesMap.put("An Exception Occured:", e.getMessage());
			LogIt(e, null, stepBean);
		} finally {
			stepBean.setStepStatus(status);
		}
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to select Family Member(Old)
	 * @author - Ram
	 * @Created - 23-Aug-2013
	 * @Modified By -Saravanan M
	 * @Modified Date -23-OCT-2013
	 *************************************************************/
	public static boolean refillhubFamilyMemberSelector(EventFiringWebDriver browser, String familyMemberOption) {
		boolean memberSelection = false;
		String FamilyMemberSelector_SELECT = "//div[contains(@class,'FamilyMember')]//ul[contains(@class,'Options')]";
		String FamilyMemberSelectArrow_BTN = "//span[contains(@class,'selectArrow')]~XPATH";
		String FamilyMemberCurrentlySelected_ELM = "//div[contains(@class,'FamilyMember')]//span[contains(@class,'selected')]";
		int TotalMembers = 0;
		Map<String, String> messagesMap = null;
		try {
			if (messagesMap == null) {
				messagesMap = new HashMap<String, String>();
			}

			if (StringUtils.isNotBlank(familyMemberOption)) {
				if ("-".equals(familyMemberOption)) {
					// No Change in Selection
					memberSelection = true;
				} else {

					if (isElementPresentVerification(FamilyMemberSelector_SELECT, browser)) {

						List<WebElement> familyMemberSelector = browser.findElements(By.xpath(FamilyMemberSelector_SELECT + "/li[contains(@class,'selectOption')]/a"));
						TotalMembers = familyMemberSelector.size();
						getElementByProperty(FamilyMemberSelectArrow_BTN, browser).click();
						Thread.sleep(OBJECT_WAIT_THRESHOLD);

						String CurrentUser=browser.findElement(By.xpath(FamilyMemberCurrentlySelected_ELM)).getText();
						if ("self".equalsIgnoreCase(familyMemberOption)) {
							if(!CurrentUser.contains("You")){
								browser.findElement(By.xpath("//div[contains(@class,'FamilyMember')]//ul[contains(@class,'Options')]/li[1]")).click();
								if (!getElementByProperty(FamilyMemberCurrentlySelected_ELM, browser).getText().contains("You"))
									messagesMap.put("Unable to select member", "The current member selected is not YOU/SELF");
								memberSelection = false;
							}else{
								memberSelection = true;
							}
						} else {
							if (Integer.parseInt(familyMemberOption) != TotalMembers && Integer.parseInt(familyMemberOption) <= TotalMembers) {
								browser.findElement(By.xpath(FamilyMemberSelector_SELECT + "//li[contains(@class,'selectOption')][" + familyMemberOption + "]/a")).sendKeys(Keys.RETURN);
								Thread.sleep(OBJECT_WAIT_THRESHOLD);
								Thread.sleep(8000);
								if (getElementByProperty(FamilyMemberCurrentlySelected_ELM, browser).getText().contains("You")) {
									messagesMap.put("Unable to select member", "The member was not selected properly");
									memberSelection = false;
								} else {
									memberSelection = true;
								}
							} else {
								messagesMap.put("Unable to select member", "The member to be selected does not exist");
								memberSelection = false;
							}
						}
					} else {
						throw new Exception("Family Member Drop down is not available. Cannot proceed further.");
					}
				}
			}
		} catch (Exception e) {
			memberSelection = false;
			messagesMap.put("An Exception Occured:", e.getMessage());
			LogIt(e, null, null);
		}
		return memberSelection;
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to select Family Member
	 * @author - Imran
	 * @Created -
	 * @Modified By -Saravanan M
	 * @Modified Date - 26-Aug-2013
	 ************************************************************* 
	 */
	public static boolean familyMemberSelector(EventFiringWebDriver browser, String familyMemberOption) {
		String FamilyMemberSelector_SELECT = "//select[@name='selectFamilyMember' or @id='selectMemberView']~XPATH";
		String ManageYourPrescriptions_H = "//div[@id='header_bar']//h1[contains(text(),'Manage Prescriptions')]~XPATH";
		Select selectBox = null;
		boolean isFamilyMemberSelected = false;
		try {

			if (isElementPresentVerification(ManageYourPrescriptions_H, browser)) {
				isFamilyMemberSelected = refillhubFamilyMemberSelector(browser, familyMemberOption);
			} else if (StringUtils.isNotBlank(familyMemberOption)) {
				if ("-".equals(familyMemberOption)) {
					;// No Change in Selection
				} else {
					if (isElementPresentVerification(FamilyMemberSelector_SELECT, browser)) {
						WebElement familyMemberSelector = getElementByProperty(FamilyMemberSelector_SELECT, browser);
						selectBox = new Select(familyMemberSelector);
						if ("self".equalsIgnoreCase(familyMemberOption)) {
							if (selectBox != null) {
								selectBox.selectByIndex(0);
								isFamilyMemberSelected = true;
							}
						} else {
							if (selectBox != null) {
								selectBox.selectByIndex(Integer.parseInt(familyMemberOption));
								isFamilyMemberSelected = true;
							}
						}
					}
				}
			}
		} catch (Exception e) {
			LogIt(e, null, null);
		}
		return isFamilyMemberSelected;
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to verify if an element is present and click on it
	 * @author - Chezhiyan. E
	 * @Created - 22-Nov-2011
	 * @Modified By - Imran Aslam
	 * @Modified Date - March 20th, 2012
	 ************************************************************* 
	 */
	public static boolean isElementPresentVerifyClick(String objectProperty, EventFiringWebDriver browser) {
		boolean isVerifiedAndClicked = false;
		try {
			if (isElementPresentVerification(objectProperty, browser)) {
				getElementByProperty(objectProperty, browser).click();
				// getElementByProperty(objectProperty,
				// browser).sendKeys(Keys.RETURN);
				isVerifiedAndClicked = true;
			}
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
		return isVerifiedAndClicked;
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to verify if an element is present in the application
	 * @Input - Object Name for Report Description, Object Name from OR
	 * @author - Chezhiyan E
	 * @Created - 21 Nov 2011
	 * @Modified By - Imran Aslam
	 * @Modified Date - March 20th, 2012
	 ************************************************************* 
	 */
	public static boolean isElementPresentVerification(String objectProperty, EventFiringWebDriver browser) {
		boolean isElementPresent = false;
		WebElement webElement = null;
		try {
			webElement = getElementByProperty(objectProperty, browser);
			if (webElement != null) {
				isElementPresent = true;
			} else {
				Thread.sleep(OBJECT_WAIT_THRESHOLD);
				webElement = getElementByProperty(objectProperty, browser);

				if (webElement != null) {
					isElementPresent = true;
				}
			}
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);

		}
		return isElementPresent;
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to find element by property defined
	 * @author - Ramgopal Narayanan
	 * @Created - 09 Nov 2011
	 * @Modified By - 1. Mohana Janakavalli K (included highlight)
	 * @Modified Date - Feb 08, 2013
	 ************************************************************* 
	 */
	public static WebElement getElementByProperty(String objectProperty, EventFiringWebDriver browser) {
		String propertyType = null;
		WebElement webElement = null;
		try {
			propertyType = StringUtils.substringAfter(objectProperty, "~");
			objectProperty = StringUtils.substringBefore(objectProperty, "~");

			try{
				if(browser.findElement(By.xpath("//div[@id='fsrOverlay']"))!=null){
					browser.findElement(By.xpath("//a[@class='fsrCloseBtn']")).click();
				}
			}catch(NoSuchElementException e){
			}

			if (propertyType.equalsIgnoreCase("CSS")) {
				webElement = browser.findElement(By.cssSelector(objectProperty));
				highlightElement(webElement, browser);
			} else if (propertyType.equalsIgnoreCase("XPATH")) {
				webElement = browser.findElement(By.xpath(objectProperty));
				highlightElement(webElement, browser);
			} else if (propertyType.equalsIgnoreCase("ID")) {
				webElement = browser.findElement(By.id(objectProperty));
				// highlightElement(webElement, browser);
			} else if (propertyType.equalsIgnoreCase("NAME")) {
				webElement = browser.findElement(By.name(objectProperty));
				highlightElement(webElement, browser);
			} else if (propertyType.equalsIgnoreCase("LINKTEXT")) {
				webElement = browser.findElement(By.linkText(objectProperty));
				highlightElement(webElement, browser);
			} else {
				webElement = browser.findElement(By.xpath(objectProperty));
				highlightElement(webElement, browser);
			}
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
		return webElement;
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to find List of elements by link text
	 * @author - Ramgopal Narayanan
	 * @Created - 09 Nov 2011
	 * @Modified By - Imran Aslam
	 * @Modified Date - March 15, 2012
	 ************************************************************* 
	 */
	public static List<WebElement> getElementsListByLinkText(String objectLinkText, EventFiringWebDriver browser) throws Exception {
		List<WebElement> webElements = null;
		try {
			webElements = browser.findElements(By.linkText(objectLinkText));
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
		return webElements;
	}

	/*
	 * RAM - 12/16/12 - THIS METHOD IS NO LONGER USED AS List<WebElement>
	 * getElementsListByLinkText WILL TAKE CARE 1 OR N ELEMENTS
	 * 
	 * ************************************************************
	 * 
	 * @Purpose - Method to find List of elements by link text
	 * 
	 * @author - Ramgopal Narayanan
	 * 
	 * @Created - 09 Nov 2011
	 * 
	 * @Modified By - Imran Aslam
	 * 
	 * @Modified Date - March 15, 2012
	 * ************************************************************
	 * 
	 * public static WebElement getElementByLinkText(String objectLinkText,
	 * String flowType, EventFiringWebDriver browser) { WebElement webElement =
	 * null; try { webElement =
	 * browser.findElement(By.linkText(objectLinkText)); } catch (Exception e) {
	 * LogIt(null, e.getMessage(), null); } return webElement; }
	 */

	/**
	 ************************************************************* 
	 * @Purpose - Method to navigate to an URL
	 * @author - Ramgopal Narayanan
	 * @Created - 09 Nov 2011
	 * @Modified By - Imran Aslam
	 * @Modified Date - March 15, 2012
	 ************************************************************* 
	 */

	public static void getUrl(EventFiringWebDriver browser, String pUrl) {
		browser.get(pUrl);
		if (EcommTestRunner.maximizeBrowser.equalsIgnoreCase("Yes"))
			browser.manage().window().maximize();
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to navigate back to page
	 * @author - Ramgopal Narayanan
	 * @Created - 19 DEC 2012
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************* 
	 */

	public static void browserBack(EventFiringWebDriver browser) {
		browser.navigate().back();
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to switch to any overlay
	 * @author - Chezhiyan. E
	 * @Created - 22-Nov-2011
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************* 
	 */
	public static void switchToOverlay(EventFiringWebDriver browser) {
		try {
			Thread.sleep(3000);
			browser.switchTo().frame(getElementByProperty(Overlay_FRM, browser));
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to switch to default content or page
	 * @author - Chezhiyan. E
	 * @Created - 22-Nov-2011
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************* 
	 */
	public static void switchToDefault(EventFiringWebDriver browser) {
		try {
			Thread.sleep(3000);
			browser.switchTo().defaultContent();
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to select a particular Value from any List box
	 * @Input -
	 * @author - Chezhiyan. E
	 * @Created - 22-Nov-2011
	 * @Modified By - Imran Aslam
	 * @Modified Date - March 20th, 2012
	 ************************************************************* 
	 */
	public static void genericListBoxOptionSelector(WebElement objectName, String strSelectOption) {
		try {
			new Select(objectName).selectByVisibleText(strSelectOption);
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
	}

	public static EventFiringWebDriver getNewBrowser(long threadId) {
		return BrowserFactory.getNewBrowser(threadId);
	}

	public static EventFiringWebDriver getBrowser(long threadId) {
		return BrowserFactory.getBrowser(threadId);
	}

	public static EventFiringWebDriver getCSCBrowser(long threadId) {
		return BrowserFactory.getCSCBrowser(threadId);
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to clear existing example text in a field and then
	 *          enter required data
	 * @Input - Element to enter text, Text Value
	 * @author - Chezhiyan. E
	 * @Created - 27 Dec 2011
	 * @Modified By - Imran Aslam
	 * @Modified Date - March 21st, 2012
	 ************************************************************* 
	 */
	public static boolean clearAndEnterText(String ElementName, String Text, EventFiringWebDriver browser) {
		boolean isTextEnteredResult = false;
		try {
			if ("-".equals(Text)) {
				// ignore this field
				isTextEnteredResult = true;
			} else {
				WebElement textBox = getElementByProperty(ElementName, browser);
				textBox.clear();
				textBox.click();
				textBox.sendKeys(Text);
				isTextEnteredResult = true;
			}
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
		return isTextEnteredResult;
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to select from the drop down by Visible Text
	 * @Input - Properties file name, value to fetch
	 * @author - Imran Aslam
	 * @Created - 27 Dec 2011
	 * @Modified By - Imran Aslam
	 * @Modified Date - March 21st, 2012
	 ************************************************************* 
	 */
	public static boolean selectByVisibleText(String ElementName, String visibleText, EventFiringWebDriver browser) {
		boolean isSelectionMadeResult = false;
		try {
			if ("-".equals(visibleText)) {
				// ignore this field
				isSelectionMadeResult = true;
			} else {
				WebElement elementSelect = getElementByProperty(ElementName, browser);
				Select dropDown = new Select(elementSelect);
				dropDown.selectByVisibleText(visibleText);
				isSelectionMadeResult = true;
			}
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
		return isSelectionMadeResult;
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to ge the Horizontal Data from the feature file step
	 *          using Hash Map.
	 * @author - Imran
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************* 
	 */
	public static Map<String, List<String>> getHorizontalData(DataTable dataTable) {
		Map<String, List<String>> dataMap = null;
		try {
			dataMap = new HashMap<String, List<String>>();
			System.out.println("dataTable.raw()"+dataTable.raw());
			System.out.println("dataTable.raw().get(0)::"+dataTable.raw().get(0));
			List<String> headingRow = dataTable.raw().get(0);
			for (String string : headingRow) {
				System.out.println("headingRow::"+string);
			}
			System.out.println("dataTable.getGherkinRows()::"+dataTable.getGherkinRows());
			int size = dataTable.getGherkinRows().size();
			System.out.println("sizesizesize:::"+size);
			int dataTableRowsCount = size - 1;
			System.out.println("dataTableRowsCount::"+dataTableRowsCount);
			ArrayList<String> totalRowCount = new ArrayList<String>();
			totalRowCount.add(Integer.toString(dataTableRowsCount));
			System.out.println("totalRowCount::"+totalRowCount);
			dataMap.put("totalRowCount", totalRowCount);
			System.out.println("headingRow.size()::"+headingRow.size());
			for (int i = 0; i < headingRow.size(); i++) {
				List<String> dataList = new ArrayList<String>();
				System.out.println("headingRow.get("+i+"):^^"+headingRow.get(i));
				System.out.println("dataList.size()%%%%%%%%%%%::"+dataList.size());
				dataMap.put(headingRow.get(i), dataList);
				System.out.println("dataTableRowsCount::"+dataTableRowsCount);
				for (int j = 1; j <= dataTableRowsCount; j++) {
					System.out.println("dataTable.raw().get("+j+")::"+dataTable.raw().get(j));
					List<String> dataRow = dataTable.raw().get(j);
					System.out.println("dataRowdataRowdataRowdataRow:"+dataRow);
					System.out.println("dataRow.get("+i+")"+dataRow.get(i));
					dataList.add(dataRow.get(i));
				}
			}
		} catch (Exception e) {
			LogIt(e, null, null);
		}
		return dataMap;
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to ge the Vertical Data from the feature file step
	 *          using Hash Map.
	 * @author - Imran
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************* 
	 */
	public static Map<String, List<String>> getVerticalData(DataTable dataTable) {
		Map<String, List<String>> dataMap = null;
		try {
			int dataTableRowsCount = dataTable.getGherkinRows().size();
			dataMap = new HashMap<String, List<String>>();
			for (int k = 0; k < dataTableRowsCount; k++) {
				List<String> dataRow = dataTable.raw().get(k);
				String key = dataRow.get(0);
				dataRow.remove(0);
				dataMap.put(key, dataRow);
			}
		} catch (Exception e) {
			LogIt(e, null, null);
		}
		return dataMap;
	}

	public static ScenarioBean getScenario(String scenarioNumber) {
		ScenarioBean scenarioBean = null;
		try {
			/*
			 * if (StringUtils.isBlank(scenarioNumber)) { scenarioNumber = "99";
			 * }
			 */
			if (EcommTestRunner.registeredScenariosMap.get(scenarioNumber) == null) {
				System.out.println("EcommTestRunner.registeredScenariosMap.get(scenarioNumber)::"+EcommTestRunner.registeredScenariosMap.get(scenarioNumber));
				System.out.println("ifififififififififififififififififififififififif::::"+scenarioNumber);
				scenarioBean = new ScenarioBean();
				scenarioBean.setScenarioNumber(scenarioNumber);
				ArrayList<StepBean> stepBean = new ArrayList<StepBean>();
				scenarioBean.setStepBeans(stepBean);
				scenarioBean.setStepBeans(new ArrayList<StepBean>());
				scenarioBean.setSharedStepProperties(new HashMap<String, String>());
				System.out.println("scenarioBean.getScenarioNumber()"+scenarioBean.getScenarioNumber());
				System.out.println("scenarioBean.getStepBeans()"+scenarioBean.getStepBeans());
				System.out.println("scenarioBean.getSharedStepProperties()"+scenarioBean.getSharedStepProperties());
				QualityCenterDBManager.addScenarioData(scenarioBean);
				EcommTestRunner.registeredScenariosMap.put(scenarioNumber, scenarioBean);
				ScenarioBean scenarioBean2 = EcommTestRunner.registeredScenariosMap.get(scenarioNumber);
				System.out.println("EcommTestRunner.registeredScenariosMap.get(scenarioNumber)::"+EcommTestRunner.registeredScenariosMap.get(scenarioNumber));
			} else {
				System.out.println("elseelseelseelseelseelseelseelseelseelseelse");
				scenarioBean = EcommTestRunner.registeredScenariosMap.get(scenarioNumber);
			}
		} catch (Exception e) {
			LogIt(e, null, null);
		}
		return scenarioBean;
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Report the Status of the Individual Steps
	 * @Input - HashMap
	 * @author - Imran Aslam
	 * @Created - March 23rd, 2012
	 * @Modified By - Siva
	 * @Modified Date - 31-Oct-2012
	 * @Modified comments- Updated the Screenshot Report Logic.
	 ************************************************************************ 
	 */
	public static void ReportIt(EventFiringWebDriver browser, ScenarioBean scenario, StepBean step) {
		try {

			// RAM - 1/16/13 - New Reporting Section Starts Here
			// -----------------------------------------------------------------------------------------------------------------

			// 1. Get the current step status & update the same
			// *************************************************
			if ((step.isFirstStep() || step.isFinalStep()) && (!step.getStepStatus().equalsIgnoreCase("DISABLED"))) {
				// a. START OR END
				ReportingLibrary.updateStepStatus(browser, scenario, step);
				if (step.isFirstStep())
					step.setFirstStep(false);

			} else if ((step.getStepStatus().equalsIgnoreCase("PASS") || step.getStepStatus().equals(PASS)) && (!step.isFirstStep() || !step.isFinalStep())) {
				// b. PASS
				ReportingLibrary.updateStepStatus(browser, scenario, step);
				if (scenario.isWarningStatus())
					scenario.setScenarioOverAllReportingStatus("FAIL");
				else
					scenario.setScenarioOverAllReportingStatus("PASS");
			} else if ((step.getStepStatus().equalsIgnoreCase("WARNING") || step.getStepStatus().equals(WARNING))
					|| (step.getStepStatus().equalsIgnoreCase("FAIL") || step.getStepStatus().equals(FAIL))) {
				// c. FAIL/WARNING
				ReportingLibrary.updateStepStatus(browser, scenario, step);
				scenario.setScenarioOverAllReportingStatus("FAIL");
			} else if (step.getStepStatus().equalsIgnoreCase("DISABLED"))
				// d. DISABLED FOR EXECUTION - MARKED NO FOR EXECUTION
				scenario.setScenarioOverAllReportingStatus("DISABLED");

			// 2. Update the overall scenario status
			// *************************************
			if (scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("PASS") && step.isFinalStep()) {

				// PASS SCENARIO
				scenario.setScenarioStatus(PASS);
				ReportingLibrary.updateScearnioStatus(browser, scenario, step);
			} else if (scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("DISABLED")) {
				// DIABLED SCENARIO
				ReportingLibrary.updateScearnioStatus(browser, scenario, step);
				assertEquals(CommonLibrary.PASS, CommonLibrary.FAIL);
			} else if (scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("FAIL") && step.isFinalStep()) {
				// SCENARIO WITH WARNING GOES TILL THE LAST WITH NO FAIL BUT
				// OVERALL IS STILL FAIL
				ReportingLibrary.updateScearnioStatus(browser, scenario, step);
			} else if (scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("FAIL") && (step.getStepStatus().equalsIgnoreCase("FAIL") || step.getStepStatus().equals(FAIL))) {
				// COME OUT OF THE RUN EVEN IF THE SIGLE STEP FAILS
				ReportingLibrary.updateScearnioStatus(browser, scenario, step);
				scenario.setScenarioStatus(FAIL);
				assertEquals(CommonLibrary.PASS, step.getStepStatus());
			}
			// -----------------------------------------------------------------------------------------------------------------

			// Screenshot implementation
			if (browser != null && !step.getStepStatus().equals(PASS)) {
				BufferedImage BufferedScreenshotImage = ReportingLibrary.takeScreenshot(browser);
				step.setScreenShot(BufferedScreenshotImage);
			}

			// if (step.getStepStatus().equals(FAIL)) {
			// scenario.setScenarioStatus(FAIL);
			// scenario.setExcelReportEnabled(true);
			// ReportingLibrary.ReportItExcel(browser, scenario, step);
			// assertEquals(CommonLibrary.PASS, step.getStepStatus());
			// }

			// Excel Report
			// if (("disabled".equals(scenario.getScenarioStatus()) ||
			// step.isFinalStep()) && scenario.isExcelReportEnabled()) {
			// ReportingLibrary.ReportItExcel(browser, scenario, step);
			// }
			// ALM results upload
			// if (step.isFinalStep() && scenario.isALMReportEnabled()) {
			// ReportingLibrary.ReportItALM(browser, scenario, step);
			// }
			if (EcommTestRunner.enableCucumberReport.equalsIgnoreCase("yes") && !scenario.isExcelReportEnabled() && scenario.isCucumberReportEnabled()) {
				if (scenario.isEnableExecution()) {
					if (CommonLibrary.FAIL.equalsIgnoreCase(step.getStepStatus()) || CommonLibrary.WARNING.equalsIgnoreCase(step.getStepStatus()) || "fail".equalsIgnoreCase(step.getStepStatus())
							|| "warning".equalsIgnoreCase(step.getStepStatus()) && ("regression".equals(EcommTestRunner.runMode) || "smoke".equals(EcommTestRunner.runMode))) {
						if (EcommTestRunner.customReportOutput == null) {
							EcommTestRunner.reportFileWriter = new FileWriter(EcommTestRunner.customReportPath);
							EcommTestRunner.customReportOutput = new BufferedWriter(EcommTestRunner.reportFileWriter);
						}

						if (CommonLibrary.FAIL.equalsIgnoreCase(step.getStepStatus())) {
							EcommTestRunner.customReportOutput.write("--------------------- FAILURE - " + scenario.getScenarioTagName()
									+ "---------------------------------------------------------------------------------------\n");
							EcommTestRunner.failuresCounter++;
						} else if (CommonLibrary.WARNING.equalsIgnoreCase(step.getStepStatus())) {
							EcommTestRunner.customReportOutput.write("--------------------- WARNING - " + scenario.getScenarioTagName()
									+ "---------------------------------------------------------------------------------------\n");
							EcommTestRunner.warningsCounter++;
						}
						EcommTestRunner.customReportOutput.write("FeatureName: " + scenario.getScenarioFeatureName() + "\n");
						EcommTestRunner.customReportOutput.write("ScenarioTagName: @" + scenario.getScenarioTagName() + "\n");
						EcommTestRunner.customReportOutput.write("ScenarioDescription: " + scenario.getScenarioDescription() + "\n");
						EcommTestRunner.customReportOutput.write("ScenarioStep: " + step.getStepName() + "\n");
						EcommTestRunner.customReportOutput.write("StepStatusDescription: " + step.getStatusDescription() + "\n");
						EcommTestRunner.customReportOutput.write("BrowserName: " + EcommTestRunner.currentBrowserName + "\n");
						EcommTestRunner.customReportOutput.write("ScenarioCurrentUrl: " + scenario.getScenarioCurrentUrl() + "\n");
						EcommTestRunner.customReportOutput.write("----------------------------------------------------------------------------------------------------------------------\n\n\n");
					}
				}

				ReportingLibrary.ReportItCucumber(browser, scenario, step);
			}

			/*
			 * } else { step.setStepCurrentUrl(EcommTestRunner.config.getString(
			 * "walgreensURL"));
			 * scenario.setScenarioBrowserName(EcommTestRunner.
			 * currentBrowserName); if (scenario.isScenarioInitialize()) {
			 * ReportingDBManager.insertScenario(scenario); } else {
			 * ReportingDBManager.insertStep(scenario, step); //
			 * ServerReportsDBManager.updateScenario(scenario, step);
			 * 
			 * File scrFile = ((TakesScreenshot)
			 * browser).getScreenshotAs(OutputType.FILE);
			 * FileUtils.copyFile(scrFile, new
			 * File("c:\\temp\\screenshot.png")); // byte[] image = //
			 * ((TakesScreenshot)browser).getScreenshotAs(OutputType.BYTES); } }
			 */
		} catch (Exception e) {
			LogIt(e, null, step);
			assertEquals(CommonLibrary.PASS, CommonLibrary.FAIL);
		}
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Log the Exceptions
	 * @Input - HashMap
	 * @author - Imran Aslam
	 * @Created - May 21st, 2012
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	public static void LogIt(Exception e, String eMsg, StepBean step) {
		EcommTestRunner.LogIt(e, eMsg, step);
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to get Test data from the Excel sheet using Input File
	 *          Name, Sheet Name, Row ID and Required Column Name
	 * @author - Imran
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************* 
	 */
	public static String getTestData(String inputFileName, String dataSheetName, String rowID, String columnName) {
		String testData = null;
		try {
			if (!"-".equals(inputFileName) && !"-".equals(dataSheetName) && !"-".equals(rowID) && !"-".equals(columnName)) {
				testData = TestDataDBManager.getTestData(inputFileName, dataSheetName, rowID, columnName);
			}
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
		return testData;
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to get scenario data from the xls using the Name, Sheet
	 *          Name, Row ID and Required Column Name
	 * @author - Ram
	 * @Created - 2-JAN-13
	 * @ModifiedBy -
	 * @ModifiedDate -
	 ************************************************************* 
	 */
	public static String getQCFileData(String inputFileName, String dataSheetName, String rowID, String columnName) {
		System.out.println("getQCFileData:::");
		String qcFileData = null;
		try {
			if (!"-".equals(inputFileName) && !"-".equals(dataSheetName) && !"-".equals(rowID) && !"-".equals(columnName)) {
				qcFileData = QualityCenterDBManager.getQCFileData(inputFileName, dataSheetName, rowID, columnName);
				System.out.println("%%%%%getQCFileData::::qcFileData::"+qcFileData);
			}
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
		return qcFileData;
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to find Child element by property defined
	 * @author - Imran
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************* 
	 */
	public static WebElement getChildElementPropertyFromParent(WebElement ParentElement, String ChildElement, String strChildElementType) throws Exception {
		try {
			if (strChildElementType.equalsIgnoreCase("CSS"))
				return ParentElement.findElement(By.cssSelector(ChildElement));
			else if (strChildElementType.equalsIgnoreCase("XPATH"))
				return ParentElement.findElement(By.xpath(ChildElement));
			else if (strChildElementType.equalsIgnoreCase("ID"))
				return ParentElement.findElement(By.id(ChildElement));
			else if (strChildElementType.equalsIgnoreCase("NAME"))
				return ParentElement.findElement(By.name(ChildElement));
			else if (strChildElementType.equalsIgnoreCase("LINKTEXT"))
				return ParentElement.findElement(By.linkText(ChildElement));
			else if (strChildElementType.equalsIgnoreCase("TAG"))
				return ParentElement.findElement(By.tagName(ChildElement));
			else
				return null;

		} catch (org.openqa.selenium.NoSuchElementException nsee) {
			return null;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Generate Random Email Address
	 * @Input -
	 * @author - Siva Santhi Reddy
	 * @Created - 17 July 2012
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	public static String randomEmailGenerator() throws Exception {
		String emailAddress = null;
		try {
			Calendar cal = Calendar.getInstance();
			String timeinmills = Long.toString(cal.getTimeInMillis());
			emailAddress = randomNameGenerator() + "_" + timeinmills + "@testbiz.com";
		} catch (Exception e) {
			System.out.println("Exception is :" + e);
		}
		return emailAddress;
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Generate Random Name
	 * @Input -
	 * @author - Siva Santhi Reddy
	 * @Created -15-MAR-2013
	 * @Modified By -Siva
	 * @Modified Date -21-MAR-2013
	 ************************************************************************ 
	 */
	public static String randomNameGenerator() throws Exception {
		String name = null;
		try {
			final String BaseNameString = "ABCDEFGHIJKLMNOPQRSTUVWXYZ";
			final java.util.Random rand = new java.util.Random();
			final Set<String> identifiers = new HashSet<String>();
			StringBuilder namebuilder = new StringBuilder();
			while (namebuilder.toString().length() == 0) {
				int length = rand.nextInt(2) + 5;
				for (int i = 0; i < length; i++) {
					namebuilder.append(BaseNameString.charAt(rand.nextInt(BaseNameString.length())));
				}
				if (identifiers.contains(namebuilder.toString())) {
					namebuilder = new StringBuilder();
				}
			}
			name = namebuilder.toString();

		} catch (Exception e) {
			System.out.println("Exception is :" + e);
		}
		return name;
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Generate Random Phone number
	 * @Input -
	 * @author - Siva Santhi Reddy
	 * @Created -21-MAR-2013
	 * @Modified By -SARAVANAN M
	 * @Modified Date -04-MAR-2014
	 ************************************************************************ 
	 */
	public static String randomPhoneNumberGenerator() throws Exception {
		String phone = null;
		try {
			final String BasePhoneNumberString = "9876543210";
			final java.util.Random rand = new java.util.Random();
			final Set<String> identifiers = new HashSet<String>();
			StringBuilder phonebuilder = new StringBuilder();
			while (phonebuilder.toString().length() == 0) {
				for (int i = 0; i < 10; i++) {
					phonebuilder.append(BasePhoneNumberString.charAt(rand.nextInt(BasePhoneNumberString.length())));
				}
				if (identifiers.contains(phonebuilder.toString())) {
					phonebuilder = new StringBuilder();
				}
			}
			phone = phonebuilder.toString();

			// This is to change the first digit if it is Zero(0)
			// SIMEON - 08/20/13 - Get random number from 0 to 8 and use charAt
			// to get digit from BPNString
			// --------------------------------
			if (phone.startsWith("0")) {
				//incase the caracter at "rand.nextInt(BasePhoneNumberString.length() - 1" is "0" then it ll be appended. 
				//To avoid this ReplaceFirst concept is used.
				//SARAVANAN M - MAR-04
				//		Character r = BasePhoneNumberString.charAt(rand.nextInt(BasePhoneNumberString.length() - 1)); // -1
				// to
				// exclude
				// 0
				//		phone = r.toString() + phone.substring(1);
				phone=phone.replaceFirst("0", "5");
			}

			//based on the experience of getting <10 digits phone number added the below statement - SARAVANAN M
			if(phone.length()<10){
				for(int apndInc=0;apndInc<(10-phone.length());apndInc++){
					phone=phone+"0";
				}
			}
			// --------------------------------

			// Doesn't work bc strings are immutable & replace would change all
			// occurrances of 0 - Simeon
			// for (int i = 0; i < 20; i++) {
			// if (phone.charAt(0) == '0') {
			// phone.replace(phone.charAt(0), (char)
			// rand.nextInt(BasePhoneNumberString.length()));
			// } else {
			// break;
			// }
			// }

		} catch (Exception e) {
			System.out.println("Exception is :" + e);
		}
		return phone;
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Generate Random DOB
	 * @Input -
	 * @author - Siva Santhi Reddy
	 * @Created -21-MAR-2013
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	public static String randomDOBGenerator() throws Exception {
		String date = null;
		String month = null;
		String year = null;
		String dob = null;
		try {
			final java.util.Random rand = new java.util.Random();

			date = Integer.toString(rand.nextInt(3)) + Integer.toString(rand.nextInt(8));
			month = Integer.toString(rand.nextInt(2)) + Integer.toString(rand.nextInt(3));
			year = "19" + Integer.toString(rand.nextInt(9)) + Integer.toString(rand.nextInt(9));

			// Incase if date or Month generate "00" string then change that
			// value.
			if (date.equals("00")) {
				date = "01";
			}
			if (month.equals("00")) {
				month = "10";
			}
			dob = month + ":" + date + ":" + year;

			// System.err.println("DOB is:" + dob);
		} catch (Exception e) {
			System.out.println("Exception is :" + e);
		}
		return dob;
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to close overlay
	 * @Input -
	 * @author - Siva Santhi Reddy
	 * @Created - 17 July 2012
	 * @Modified By -Hasan
	 * @Modified Date -20-Dec-12
	 ************************************************************************ 
	 */

	public static void closeOverlay(EventFiringWebDriver browser, StepBean stepBean, Map<String, List<String>> dataMap) {
		String status = PASS;
		String OverlayClose_BTN = "//a[@id='overlayClose']~XPATH";
		String GenericPageHeader = "//h1[contains(text(),'ABC')]~XPATH";
		Map<String, String> messagesMap = null;
		try {
			if (messagesMap == null) {
				messagesMap = new HashMap<String, String>();
				stepBean.setStepStatusMessages(messagesMap);
			}

			String ValidationType = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "ValidationType");
			String TargetPageheader = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "PageHeader");
			String TargetPageObject = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "TargetPageObject");

			if (!isElementPresentVerifyClick(OverlayClose_BTN, browser))
				throw new Exception("Not able to click on Close button in the Overlay");

			switchToDefault(browser);

			if (ValidationType.equalsIgnoreCase("Header")) {
				GenericPageHeader = GenericPageHeader.replace("ABC", TargetPageheader);
				if (!isElementPresentVerification(GenericPageHeader, browser)) {
					messagesMap.put("Target Page Header", "Not able to identify the required Target Page Header");
					status = WARNING;
				}
			} else {
				if (!isElementPresentVerification(TargetPageObject, browser)) {
					messagesMap.put("Target Page Object/Header", "Not able to identify the required Target Page");
					status = WARNING;
				}
			}

		} catch (Exception e) {
			status = FAIL;
			messagesMap.put("An Exception Occured:", e.getMessage());
			LogIt(e, null, stepBean);
		} finally {
			stepBean.setStepStatus(status);
		}
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to navigate to Walgreens Home Page (Using Direct URL)
	 * @Input -
	 * @author - Siva Santhi Reddy
	 * @Created - 17 July 2012
	 * @Modified By -Siva
	 * @Modified Date -15-Nov-12
	 ************************************************************************ 
	 */
	public static void navigatintoWalgreensApp(EventFiringWebDriver browser, StepBean stepBean, Map<String, List<String>> dataMap) {
		String status = PASS;
		String ShoppingCartUrl = "store/checkout/cart.jsp";
		String requiredWagUrl = null;
		String ShoppingCart_H1 = "//h1[contains(text(),'Shopping Cart')]~XPATH";

		Map<String, String> messagesMap = null;
		try {
			if (messagesMap == null) {
				messagesMap = new HashMap<String, String>();
				stepBean.setStepStatusMessages(messagesMap);
			}
			requiredWagUrl = EcommTestRunner.config.getString("walgreensURL");
			if (dataMap.get("PageName").get(0).equalsIgnoreCase("CartPage")) {
				requiredWagUrl = requiredWagUrl + ShoppingCartUrl;
				getUrl(browser, requiredWagUrl);
				if (!isElementPresentVerification(ShoppingCart_H1, browser)) {
					messagesMap.put("Shopping cart Page", "Page is not navigated to Shopping cart Page");
					status = WARNING;
					// throw new
					// Exception("Page is not navigated to Shopping cart Page");
				}
			} else if (dataMap.get("PageName").get(0).equalsIgnoreCase("-")) {
				getUrl(browser, requiredWagUrl);// if we are not going to
				// specify anything in the data,
				// by defualt we will have WAG
				// URL.

			}

		} catch (Exception e) {
			status = WARNING;
			messagesMap.put("An Exception Occured:", e.getMessage());
			LogIt(e, null, stepBean);
		} finally {
			stepBean.setStepStatus(status);
		}
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Hover over a drop down list object and select one of
	 *          the options from the list
	 * @Input -
	 * @author - Ram
	 * @Created -
	 * @Modified By - Siva,Saravanan,Ram
	 * @Modified Date -3-June-2013,6/6/13, 07/1/13
	 ************************************************************************ 
	 */
	public static boolean performHoverClickAction(final WebElement MenuToBeClicked, final String MenuOptionToBeClicked, EventFiringWebDriver browser) throws Exception {
		boolean isVerifiedAndClicked = false;
		try {

			// RAM - 06/11/13 - Move the mouse pointer away from the window to
			// enable focus.
			Robot robot = new Robot();
			Point coordinates = browser.manage().window().getPosition();
			robot.mouseMove(coordinates.getX(), coordinates.getY() + 5);

			Thread.sleep(OBJECT_WAIT_THRESHOLD);
			Actions builder = new Actions(browser);
			builder.moveToElement(MenuToBeClicked).build().perform();

			// RAM - 6/5/13 - Added to click on the object given when the menu
			// is displayed after hover
			Thread.sleep(OBJECT_WAIT_THRESHOLD);
			if (isElementPresentVerifyClick(MenuOptionToBeClicked, browser)) {
				isVerifiedAndClicked = true;
			}

			// RAM - 07/01/2013 - Added code to bring back pointer inside app
			// for specific pages like DWAR.
			if (!isVerifiedAndClicked) {

				robot.mouseMove(coordinates.getX() + 10, coordinates.getY() + 120);
				Thread.sleep(OBJECT_WAIT_THRESHOLD);
				builder.moveToElement(MenuToBeClicked).build().perform();
				Thread.sleep(OBJECT_WAIT_THRESHOLD);
				// RAM - 6/5/13 - Added to click on the object given when the
				// menu
				// is displayed after hover
				if (isElementPresentVerifyClick(MenuOptionToBeClicked, browser)) {
					isVerifiedAndClicked = true;
				}
			}

			// ----------------------------------------------------------------------------

		} catch (Exception e) {
			e.printStackTrace();
			isVerifiedAndClicked = false;
		}
		return isVerifiedAndClicked;
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to validate the link/object navigation/validation in a
	 *          page
	 * @Input -
	 * @author - Ram
	 * @Created - 17 Dec 2012
	 * @ModifiedBy - Siva
	 * @ModifiedDate - 7-JUN-2013
	 ************************************************************************ 
	 */
	public static void clickObjectAndVerify(EventFiringWebDriver browser, StepBean stepBean, Map<String, List<String>> dataMap) {

		// Report
		String status = PASS;

		// Generic Object Defenition
		String GenericLink_LNK = "*[title*='LINK_TITLE']~CSS";
		String GenericHeaderText_ELM = "//div[contains(@id,'header')]//*[contains(text(),'HEADER_TEXT')]~XPATH";
		String Loader_IMG = "//span[@id='loaderImg']/img[@alt='loader']~XPATH";
		// Object to be used
		String ObjLinkToClick = null;
		String ObjHeaderToValidate = null;

		// Variables
		String strLinkTitle = null;
		String strExpectedPageHeader = null;
		String strValidationType = null; // HEADER or OBJECT
		String strObjectTypeToClick = null; // OBJECT or TITLE
		String strObjectLocators = null; // 1. Contains the object locator from
		// the excel when the validation type =
		// OBJECT
		// 2. Give multiple objects by
		// seperating with a COMMA eg:
		// OBJ1~CSS,OBJ2~CSS
		String strObjectToVerify = null; // The individual object to verify
		String strSourcePageURL = null; // URL of the page from which we would
		// Thanigaivelan - Target page URL Verification method added to this.
		// The Destination URL verification code will work only if you give
		// "VerifyDestURL" Hash map column Value as "Yes"
		// It also Verifies the Protocol type.
		// ex.1: To test a URL like "https://qa1.walgreens.com/login.jsp" you
		// can give "https:/login.jsp"
		// ex.2: To test a URL like "http://qa1.walgreens.com/pharmacy" you can
		// give "pharmacy"
		String strDestinationPageURL = null; // The URL Navigated on Clicking
		// the Object
		// click the links for validation
		int COLUMN_NUMBER = 0; // Column number to fetch
		String strCellValue = null; // Value that comes from the array list
		String Overlay = null;
		// Data
		Map<String, String> messagesMap = null;

		// Arry List
		List<String> arrColumnValuesToFetch = null;
		String WinHandleBefore = null;

		try {
			if (messagesMap == null) {
				messagesMap = new HashMap<String, String>();
				stepBean.setStepStatusMessages(messagesMap);
			}
			int intTotalRowCount = Integer.parseInt(dataMap.get("totalRowCount").get(0));

			// RAM - 12/27/12 - To brach b/w ALL rows and seperate rows
			// ********************************************************

			String strRowIDType = dataMap.get("RowId").get(0);

			// ITERATE THRU' ALL ROWS AND CLICK LINKS/VERIFY
			if (strRowIDType.equalsIgnoreCase("ALL")) {

				arrColumnValuesToFetch = readAllValuesFromAColumnInExcel(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), COLUMN_NUMBER);

				for (int intRowCnt = 0; intRowCnt <= arrColumnValuesToFetch.size() - 1; intRowCnt++) {

					strCellValue = arrColumnValuesToFetch.get(intRowCnt);
					// 1. Get the URL & navigate to the source page
					strSourcePageURL = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), strCellValue, "PageURL");

					if (!strSourcePageURL.equalsIgnoreCase("-")) {
						strSourcePageURL = EcommTestRunner.config.getString("walgreensURL") + strSourcePageURL;
						CommonLibrary.getUrl(browser, strSourcePageURL);
					}
					// 2. Get the input object type 1. OBJECT 2. TITLE and click
					// on
					// it
					strObjectTypeToClick = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), strCellValue, "ObjectType");

					if (strObjectTypeToClick.equalsIgnoreCase("TITLE")) {

						// 2.a. Get the link title
						strLinkTitle = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), strCellValue, "LinkTitleValue");
						ObjLinkToClick = GenericLink_LNK.replace("LINK_TITLE", strLinkTitle.trim());
					} else if (strObjectTypeToClick.equalsIgnoreCase("OBJECT")) {

						// 2.b. Get the object property of the object to be
						// clicked
						ObjLinkToClick = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), strCellValue, "LinkObject").trim();
					}
					if (dataMap.containsKey("ClickObjectOverlay")) {
						Overlay = dataMap.get("ClickObjectOverlay").get(0);
						if (Overlay.equalsIgnoreCase("yes")) {
							switchToOverlay(browser);
						} else {
							switchToDefault(browser);
						}
					}
					if (isElementPresentVerifyClick(ObjLinkToClick, browser)) {

						if (dataMap.containsKey("TargetOverlay")) {
							Overlay = dataMap.get("TargetOverlay").get(0);
							if (Overlay.equalsIgnoreCase("yes")) {
								switchToOverlay(browser);
							} else {
								switchToDefault(browser);
							}
						}
						// 3. Get the validation type
						strValidationType = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), strCellValue, "ValidationType");
						if (strValidationType.equalsIgnoreCase("HEADER")) {
							// 4.a. Verify if the target page header is
							// displayed

							strExpectedPageHeader = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), strCellValue, "ExpectedTargetPageHeader");
							ObjHeaderToValidate = GenericHeaderText_ELM.replace("HEADER_TEXT", strExpectedPageHeader.trim());
							if (isElementPresentVerification(ObjHeaderToValidate, browser)) {
								// messagesMap.put("Verify if the target page header is displayed",
								// "The target page header , " +
								// strExpectedPageHeader.toUpperCase() +
								// " displayed as expected.");
							} else {
								messagesMap.put("Verify if the target page header is displayed", "The target page header , " + strExpectedPageHeader.toUpperCase() + " is not displayed as expected.");
								status = WARNING;
							}
						} else if (strValidationType.equalsIgnoreCase("OBJECT")) {
							// 4.b. Verify if the object pertaining to target
							// page
							// is displayed as expected

							strObjectLocators = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), strCellValue, "ObjectLocators");
							// Chezhiyan - Replacing the delimiter with "`" as
							// there's a chance that the object definition might
							// contains "," in it.
							String[] arrObjectLocators = strObjectLocators.split("`");

							for (int intObjLocCnt = 0; intObjLocCnt < arrObjectLocators.length; intObjLocCnt++) {
								strObjectToVerify = arrObjectLocators[intObjLocCnt].trim();
								if (isElementPresentVerification(strObjectToVerify, browser)) {
									// messagesMap.put("Verify if the target page object is displayed",
									// "The target page object , " +
									// strObjectToVerify.toUpperCase() +
									// " displayed as expected.");
								} else {
									messagesMap.put("Verify if the target page object is displayed", "The target page object , " + strObjectToVerify + " is not displayed as expected.");
									status = WARNING;
								}
							}

						}

						/*
						 * if (dataMap.containsKey("TargetOverlay")) { Overlay =
						 * dataMap.get("TargetOverlay").get(0); if
						 * (Overlay.equalsIgnoreCase("yes")) {
						 * switchToOverlay(browser); } else {
						 * switchToDefault(browser); } }
						 */
						// Verifying the Navigated page URL
						if (dataMap.containsKey("VerifyDestURL") && dataMap.get("VerifyDestURL").get(0) != null && dataMap.get("VerifyDestURL").get(0).equalsIgnoreCase("YES")) {
							String protocol = "";
							strDestinationPageURL = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), strCellValue, "ExpectedTargetPageURL");
							if (strDestinationPageURL.contains(":")) {
								protocol = strDestinationPageURL.split(":")[0].toString();
								strDestinationPageURL = EcommTestRunner.config.getString("walgreensURL").replace("http", protocol) + strDestinationPageURL.split(":")[1].toString();
							} else {
								strDestinationPageURL = EcommTestRunner.config.getString("walgreensURL") + strDestinationPageURL;
							}
							if (!browser.getCurrentUrl().toString().contains(strDestinationPageURL)) {
								messagesMap.put("Verify the Destination URL navigated on clicking Object: " + ObjLinkToClick.toString(), "The URL, " + strDestinationPageURL.toUpperCase()
										+ " is Not navigated as Expected");
								status = FAIL;
							}
						}
					} else {
						messagesMap.put("Verify link is present and clicked", "The object, " + ObjLinkToClick.toUpperCase() + " is not displayed. Cannot proceed with the validation.");
						status = WARNING;
					}
				}

			} else { // IF COMING FROM THE STEP AS SEPERATE ROW ID's

				for (int intRowCnt = 0; intRowCnt < intTotalRowCount; intRowCnt++) {
					// 1. Get the URL & navigate to the source page
					strSourcePageURL = getTestData(dataMap.get("InputFileName").get(intRowCnt), dataMap.get("SheetName").get(intRowCnt), dataMap.get("RowId").get(intRowCnt), "PageURL");
					if (strSourcePageURL != null && !strSourcePageURL.equalsIgnoreCase("-")) {
						if (strSourcePageURL.equalsIgnoreCase("PreviousPage")) {
							browserBack(browser);
						} else {
							strSourcePageURL = EcommTestRunner.config.getString("walgreensURL") + strSourcePageURL;
							CommonLibrary.getUrl(browser, strSourcePageURL);
						}

					}
					// 2. Get the input object type 1. OBJECT 2. TITLE and click
					// on
					// it
					strObjectTypeToClick = getTestData(dataMap.get("InputFileName").get(intRowCnt), dataMap.get("SheetName").get(intRowCnt), dataMap.get("RowId").get(intRowCnt), "ObjectType");

					if (strObjectTypeToClick.equalsIgnoreCase("TITLE")) {

						// 2.a. Get the link title
						strLinkTitle = getTestData(dataMap.get("InputFileName").get(intRowCnt), dataMap.get("SheetName").get(intRowCnt), dataMap.get("RowId").get(intRowCnt), "LinkTitleValue");
						ObjLinkToClick = GenericLink_LNK.replace("LINK_TITLE", strLinkTitle.trim());
					} else if (strObjectTypeToClick.equalsIgnoreCase("OBJECT")) {

						// 2.b. Get the object property of the object to be
						// clicked
						ObjLinkToClick = getTestData(dataMap.get("InputFileName").get(intRowCnt), dataMap.get("SheetName").get(intRowCnt), dataMap.get("RowId").get(intRowCnt), "LinkObject").trim();
					}
					if (dataMap.containsKey("ClickObjectOverlay")) {
						Overlay = dataMap.get("ClickObjectOverlay").get(intRowCnt);
						if (Overlay.equalsIgnoreCase("yes")) {
							switchToOverlay(browser);
						} else if (Overlay.equalsIgnoreCase("No") || Overlay.equals("-")) {
							switchToDefault(browser);
						}
					} else {
						switchToDefault(browser);
					}
					if (isElementPresentVerifyClick(ObjLinkToClick, browser)) {

						if (dataMap.containsKey("TargetOverlay")) {
							Overlay = dataMap.get("TargetOverlay").get(intRowCnt);
							if (Overlay.equalsIgnoreCase("yes")) {
								switchToOverlay(browser);
								do {
								} while (isElementPresentVerification(Loader_IMG, browser));
							} else if (Overlay.equalsIgnoreCase("No") || Overlay.equals("-")) {
								switchToDefault(browser);
							}
						} else {
							switchToDefault(browser);
						}
						if (dataMap.containsKey("ClickObjectinTab")) {
							String ClickObjectinTab = dataMap.get("ClickObjectinTab").get(intRowCnt);
							if (ClickObjectinTab.equalsIgnoreCase("Yes")) {
								String ChildObject = getTestData(dataMap.get("InputFileName").get(intRowCnt), dataMap.get("SheetName").get(intRowCnt), dataMap.get("RowId").get(intRowCnt),
								"ChildObject");
								if (!performHoverClickAction(getElementByProperty(ObjLinkToClick, browser), ChildObject, browser)) {
									messagesMap.put("Object in Menu", "Not able to perform Hover click Action");
									status = WARNING;
								}
							}
						}
						if (dataMap.get("TargetNewWindow") != null) {
							if (dataMap.get("TargetNewWindow").get(intRowCnt).equalsIgnoreCase("Yes")) {
								WinHandleBefore = browser.getWindowHandle();
								for (String NewWinHandle : browser.getWindowHandles()) {
									browser.switchTo().window(NewWinHandle);
								}
							}
						}
						// 3. Get the validation type
						strValidationType = getTestData(dataMap.get("InputFileName").get(intRowCnt), dataMap.get("SheetName").get(intRowCnt), dataMap.get("RowId").get(intRowCnt), "ValidationType");
						if (strValidationType.equalsIgnoreCase("HEADER")) {
							// 4.a. Verify if the target page header is
							// displayed

							strExpectedPageHeader = getTestData(dataMap.get("InputFileName").get(intRowCnt), dataMap.get("SheetName").get(intRowCnt), dataMap.get("RowId").get(intRowCnt),
							"ExpectedTargetPageHeader");
							ObjHeaderToValidate = GenericHeaderText_ELM.replace("HEADER_TEXT", strExpectedPageHeader.trim());
							if (isElementPresentVerification(ObjHeaderToValidate, browser)) {
								// messagesMap.put("Verify if the target page header is displayed",
								// "The target page header , " +
								// strExpectedPageHeader.toUpperCase() +
								// " displayed as expected.");
							} else {
								messagesMap.put("Verify if the target page header is displayed", "The target page header , " + strExpectedPageHeader.toUpperCase() + " is not displayed as expected.");
								status = WARNING;
							}
						} else if (strValidationType.equalsIgnoreCase("OBJECT")) {
							// 4.b. Verify if the object pertaining to target
							// page
							// is displayed as expected

							strObjectLocators = getTestData(dataMap.get("InputFileName").get(intRowCnt), dataMap.get("SheetName").get(intRowCnt), dataMap.get("RowId").get(intRowCnt), "ObjectLocators");
							// Chezhiyan - Replacing the delimiter with "`" as
							// there's a chance that the object definition might
							// contains "," in it.
							String[] arrObjectLocators = strObjectLocators.split("`");

							for (int intObjLocCnt = 0; intObjLocCnt < arrObjectLocators.length; intObjLocCnt++) {
								strObjectToVerify = arrObjectLocators[intObjLocCnt].trim();
								if (isElementPresentVerification(strObjectToVerify, browser)) {
									// messagesMap.put("Verify if the target page object is displayed",
									// "The target page object , " +
									// strObjectToVerify.toUpperCase() +
									// " displayed as expected.");
								} else {
									messagesMap.put("Verify if the target page object is displayed", "The target page object , " + strObjectToVerify + " is not displayed as expected.");
									status = WARNING;
								}
							}

						}

						/*
						 * if (dataMap.containsKey("TargetOverlay")) { Overlay =
						 * dataMap.get("TargetOverlay").get(0); if
						 * (Overlay.equalsIgnoreCase("yes")) {
						 * switchToOverlay(browser); } else {
						 * switchToDefault(browser); } }
						 */
						// Verifying the Navigated page URL
						if (dataMap.containsKey("VerifyDestURL") && dataMap.get("VerifyDestURL").get(0) != null && dataMap.get("VerifyDestURL").get(0).equalsIgnoreCase("YES")) {
							String protocol = "";
							strDestinationPageURL = getTestData(dataMap.get("InputFileName").get(intRowCnt), dataMap.get("SheetName").get(intRowCnt), dataMap.get("RowId").get(intRowCnt),
							"ExpectedTargetPageURL");

							if (strDestinationPageURL.contains(":")) {
								protocol = strDestinationPageURL.split(":")[0].toString();
								strDestinationPageURL = EcommTestRunner.config.getString("walgreensURL").replace("http", protocol) + strDestinationPageURL.split(":")[1].toString();
							} else {
								strDestinationPageURL = EcommTestRunner.config.getString("walgreensURL") + strDestinationPageURL;
							}
							if (!browser.getCurrentUrl().toString().contains(strDestinationPageURL)) {
								messagesMap.put("Verify the Destination URL navigated on clicking Object: " + ObjLinkToClick.toString(), "The URL, " + strDestinationPageURL.toUpperCase()
										+ " is Not navigated as Expected");
								status = FAIL;
							}
						}
					} else {
						messagesMap.put("Verify link is present and clicked", "The object, " + ObjLinkToClick.toUpperCase() + " is not displayed. Cannot proceed with the validation. ROW="+dataMap.get("RowId").get(intRowCnt));
						status = WARNING;
					}
					if (dataMap.get("TargetNewWindow") != null) {
						if (dataMap.get("TargetNewWindow").get(intRowCnt).equalsIgnoreCase("Yes")) {
							browser.close();
							browser.switchTo().window(WinHandleBefore);
						}
					}
				}
			}

		} catch (Exception e) {
			status = WARNING;
			messagesMap.put("An Exception Occured:", e.getMessage());
			LogIt(e, null, stepBean);
		} finally {
			stepBean.setStepStatus(status);
		}
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to perform the check box operation
	 * @Input - Object Name, Operation
	 * @author - Mohana Janakavalli K
	 * @Created - 18-Dec-2012
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */

	public static Boolean checkBoxSelection(EventFiringWebDriver browser, String Element, String Operation) {
		boolean isCheckBoxSelection = false;
		int flag = 0;

		try {

			if (isElementPresentVerification(Element, browser)) {
				if (Operation.equalsIgnoreCase("SELECT")) {
					do {
						getElementByProperty(Element, browser).click();
						flag++;
					} while ((!getElementByProperty(Element, browser).isSelected()) && (flag <= 5));

					if (getElementByProperty(Element, browser).isSelected()) {
						isCheckBoxSelection = true;
					} else {
						isCheckBoxSelection = false;
					}
				} else if (Operation.equalsIgnoreCase("DESELECT")) {
					do {
						getElementByProperty(Element, browser).click();
						flag++;
					} while ((getElementByProperty(Element, browser).isSelected()) && (flag <= 5));
					if (!getElementByProperty(Element, browser).isSelected()) {
						isCheckBoxSelection = true;
					} else {
						isCheckBoxSelection = false;
					}
				} else {
					isCheckBoxSelection = false;
				}
			} else {
				isCheckBoxSelection = false;
			}
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
		return isCheckBoxSelection;
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to verify if Check box is selected or not
	 * @Input - Object Name, Check Status
	 * @author - Mohana Janakavalli K
	 * @Created - 18-Dec-2012
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */

	public static boolean isCheckBoxSelected(EventFiringWebDriver browser, String Element, String CheckStatus) {
		boolean isCheckBoxSelected = false;
		try {
			if (isElementPresentVerification(Element, browser)) {
				if (getElementByProperty(Element, browser).isSelected()) {
					if (CheckStatus.equalsIgnoreCase("YES")) {
						isCheckBoxSelected = true;
					} else {
						isCheckBoxSelected = false;
					}
				} else {
					if (CheckStatus.equalsIgnoreCase("NO")) {
						isCheckBoxSelected = true;
					} else {
						isCheckBoxSelected = false;
					}
				}
			} else {
				isCheckBoxSelected = false;
			}
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
		return isCheckBoxSelected;
	}

	/*
	 * RAM - 12/16/12 - THIS METHOD IS NO LONGER USED TILL WE FIND A WAY TO USE
	 * WEB DRIVER WAIT
	 * 
	 * ************************************************************
	 * 
	 * @Purpose - Method to wait till an element is displayed (the wait time is
	 * set to 15 seconds by default, since if the object is not displayed even
	 * after 15 secs, the script should log that)
	 * 
	 * @author - Chezhiyan. E
	 * 
	 * @Created - 20-Dec-2012
	 * 
	 * @Modified By -
	 * 
	 * @Modified Date -
	 * ************************************************************
	 * 
	 * public static boolean waitTillObjectDisplayed(String objectProperty,
	 * EventFiringWebDriver browser) { int intWaitCtr = 0; boolean
	 * isElementDisplayed = false; try { do { isElementDisplayed =
	 * isElementPresentVerification(objectProperty, browser);
	 * Thread.sleep(1000L); intWaitCtr = intWaitCtr + 1; if (intWaitCtr > 15 &&
	 * isElementDisplayed == false) {
	 * 
	 * isElementDisplayed = false; break; } } while (isElementDisplayed != true
	 * && intWaitCtr <= 15); return isElementDisplayed; } catch (Exception e) {
	 * LogIt(null, e.getMessage(), null); return false; } }
	 */

	/**
	 ************************************************************************ 
	 * @Purpose - Method to fetch all the row values for given COLUMN in a XLS
	 * @Input - WorkBook Name, Sheet Name, Column Number (Starts from 0)
	 * @author - Ram
	 * @Created - 27 Dec 2012
	 * @ModifiedBy -
	 * @ModifiedDate -
	 ************************************************************************ 
	 */

	public static List<String> readAllValuesFromAColumnInExcel(String strWorkBook, String strShetName, int intColumnNumber) throws Exception {

		// EXCEL related objects

		FileInputStream objFileInputStream = null;
		HSSFWorkbook objWorkbook = null;
		HSSFSheet objSheet = null;
		HSSFRow objRow = null;
		HSSFCell objCell = null;
		int intTotalRows = 0;
		// COLUMN NUMBER in the excel from which values needs to be read e.g. ID
		// column will map to Column '0'
		int COLUMN_NUMBER = intColumnNumber;

		// Arry List to hold all the values
		List<String> arrColumnValues = new ArrayList<String>();

		try {

			// 1. Read the XLS
			objFileInputStream = new FileInputStream(EcommTestRunner.automationInputDataFilePath + "\\" + strWorkBook + ".xls");
			objWorkbook = new HSSFWorkbook(objFileInputStream);
			objSheet = objWorkbook.getSheet(strShetName);
			intTotalRows = objSheet.getPhysicalNumberOfRows();

			// 2. Iterate thru' the rows to get all the values pertaining to
			// that column
			for (int rowCnt = 1; rowCnt <= intTotalRows - 1; rowCnt++) {

				objRow = objSheet.getRow(rowCnt);
				objCell = objRow.getCell(COLUMN_NUMBER);
				arrColumnValues.add(objCell.getStringCellValue());
			}

		} catch (Exception e) {

			LogIt(null, e.getMessage(), null);
		}

		// 3. Closing objects created
		objFileInputStream.close();
		objWorkbook = null;
		objSheet = null;
		objRow = null;
		objCell = null;

		// 4. Return the list of values
		return arrColumnValues;

	}

	/**
	 ************************************************************* 
	 * 
	 * @Description - A function to Change the date from one format to another
	 *              format
	 * @author - Siva
	 * @Created - 31 Dec 2012
	 * @Modified By -
	 * @Mod Details -
	 * @Modified Date -
	 ************************************************************* 
	 */
	public static String dateFormatChanger(String dateString, String ActualDateFormat, String ExpectedDateFormat) throws Exception {
		try {
			String formattedDate = null;
			SimpleDateFormat dateFormat = new SimpleDateFormat(ActualDateFormat);
			// Parse the given date string into a Date object.
			Date DateToBeFormatted = dateFormat.parse(dateString);
			dateFormat = new SimpleDateFormat(ExpectedDateFormat);
			formattedDate = dateFormat.format(DateToBeFormatted);
			return formattedDate;
		} catch (Exception e) {
			return null;// returning null in case of failure
		}
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to generate a random number within a range
	 * @author - Ram
	 * @Created - Jan 2012
	 * @Input - MIN, MAX
	 * @ModifiedBy -
	 * @ModifiedDate -
	 ************************************************************* 
	 */

	public static int generateRandomNumber(int MAX, int MIN) throws Exception {
		try {
			int intNumber = 0;
			Random random = new Random();
			intNumber = random.nextInt(MAX - MIN + 1) + MIN;
			return intNumber;
		} catch (Exception e) {
			return 0;
		}
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to Switch Window
	 * @author - Fyrose
	 * @Created - Jan 31 2013
	 * @Input - Partial text of window title/empty string
	 * @ModifiedBy -Saravanan M
	 * @ModifiedDate -Apr 25 2013
	 ************************************************************* 
	 */

	public static void switchToWindow(String WindowTitle, EventFiringWebDriver browser) throws Exception {
		try {
			for (String Handle : browser.getWindowHandles()) {
				browser.switchTo().window(Handle);
				if (!WindowTitle.equals("")) {// Added to switch the last opened
					// window rather than using Window
					// title-Modified By Saravanan
					if (browser.switchTo().window(Handle).getTitle().contains(WindowTitle)) {
						break;
					}
				}
			}

		} catch (Exception e) {

			LogIt(null, e.getMessage(), null);
		}
	}

	/**
	 ************************************************************* 
	 * @Purpose - To highlight an element
	 * @author - Ramgopal Narayanan
	 * @Created - 5 Sep 2012
	 * @ModifiedBy - Mohana JAnakavalli K
	 * @Modified Date - Feb 08, 2013
	 ************************************************************* 
	 */
	public static void highlightElement(WebElement element, EventFiringWebDriver browser) {
		for (int i = 0; i < 1; i++) {
			JavascriptExecutor js = (JavascriptExecutor) browser;
			js.executeScript("arguments[0].setAttribute('style', arguments[1]);", element, "color: black; border: 3px solid black;");
		}
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to click an Object Validates the Expected Page URL
	 * @Input -
	 * @author - Siva Santhi Reddy
	 * @Created - 24-APR-2013
	 * @Modified By -SARAVANAN M
	 * @Modified Date - 11-JULY-2013
	 ************************************************************************ 
	 */
	public static void verifyLinkNavigationPageURL(EventFiringWebDriver browser, StepBean stepBean, Map<String, List<String>> dataMap) {
		String status = PASS;
		String ObjectExistsURL = null;
		String ObjectTobeClicked = null;
		String DestinationPageURL = null;
		String ApplicationURL = null;
		String DestinationPageObjects = null;
		Map<String, String> messagesMap = null;
		try {
			if (messagesMap == null) {
				messagesMap = new HashMap<String, String>();
				stepBean.setStepStatusMessages(messagesMap);
			}
			ObjectExistsURL = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "ObjectExistsURL");
			ObjectTobeClicked = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "ObjectTobeClicked");
			DestinationPageURL = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "DestinationPageURL");
			DestinationPageObjects = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "DestinationPageObjects");

			// if new URL mentioned then only get the new URL
			// #Added SARAVANAN M -11-JULY-2013 -In order to use the direct url
			if(ObjectExistsURL.equalsIgnoreCase("DEFAULTCSC")){
				getUrl(browser, EcommTestRunner.config.getString("cscURL"));
			}else if(ObjectExistsURL.equalsIgnoreCase("DEFAULTDOTCOM")){
				getUrl(browser, EcommTestRunner.config.getString("walgreensURL"));
			}else if (ObjectExistsURL.contains("http:")) {
				getUrl(browser, ObjectExistsURL);
			} else if (ObjectExistsURL != null && !ObjectExistsURL.equals("-")) {
				String FinalURLToNavigate = EcommTestRunner.config.getString("walgreensURL") + ObjectExistsURL;
				getUrl(browser, FinalURLToNavigate);
			} 
			// #Added SARAVANAN M -11-JULY-2013 -In case there's no object to
			// click we can skip this code
			if (!ObjectTobeClicked.equals("-")) {
				if (!isElementPresentVerifyClick(ObjectTobeClicked, browser)) {
					throw new Exception("Not able to Click on Given Object - " + ObjectTobeClicked);
				}
			}

			if (DestinationPageURL != null && !DestinationPageURL.equals("-")) {
				ApplicationURL = browser.getCurrentUrl();
				String ExpectedFinalURL = EcommTestRunner.config.getString("walgreensURL") + DestinationPageURL;
				ApplicationURL = ApplicationURL.replace("https://", "").replace("http://", "");
				ExpectedFinalURL = ExpectedFinalURL.replace("https://", "").replace("http://", "");
				if (!ApplicationURL.contains(ExpectedFinalURL)) {
					messagesMap.put("Destination Page URL", "Expected URL is not found");
					status = WARNING;
				}
			}

			// #Added SARAVANAN M -11-JULY-2013 -to validate the destination
			// page objects
			if (!DestinationPageObjects.equals("-")) {
				if (!DestinationPageObjects.contains("`")) {
					if (!isElementPresentVerification(DestinationPageObjects, browser)) {
						messagesMap.put(DestinationPageObjects, "Object is missing");
						status = WARNING;
					}
				} else {
					String[] arrValidationElement = DestinationPageObjects.split("`");
					for (int k = 0; k < arrValidationElement.length; k++) {
						if (!isElementPresentVerification(arrValidationElement[k], browser)) {
							messagesMap.put(arrValidationElement[k], "Object is missing");
							status = WARNING;
						}
					}
				}
			}

		} catch (Exception e) {
			status = WARNING;
			messagesMap.put("An Exception Occured:", e.getMessage());
			LogIt(e, null, stepBean);
		} finally {
			stepBean.setStepStatus(status);
		}
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to validate Default Selected Option And availability of
	 *          links
	 * @Created -
	 * @Modified By - Siva
	 * @Modified Date -16-MAY-2013
	 ************************************************************* 
	 */
	public static void validateDefaultSelectedOptionAndLinksAvailability(EventFiringWebDriver browser, StepBean stepBean, Map<String, List<String>> dataMap) {
		String status = PASS;
		String Overlay = null;
		Map<String, String> messagesMap = null;
		try {
			if (messagesMap == null) {
				messagesMap = new HashMap<String, String>();
				stepBean.setStepStatusMessages(messagesMap);
			}
			int totalRowCount = Integer.parseInt(dataMap.get("totalRowCount").get(0));
			for (int j = 0; j < totalRowCount; j++) {
				String DefaultSelectedOption = getTestData(dataMap.get("InputFileName").get(j), dataMap.get("SheetName").get(j), dataMap.get("RowId").get(j), "DefaultSelectedOption");
				String VerifyAvailableofObjects = getTestData(dataMap.get("InputFileName").get(j), dataMap.get("SheetName").get(j), dataMap.get("RowId").get(j), "AvailableObjects");
				String VerifyNotAvailableofObects = getTestData(dataMap.get("InputFileName").get(j), dataMap.get("SheetName").get(j), dataMap.get("RowId").get(j), "NotAvailableObjects");

				// Modified by Sasikala J
				if (dataMap.containsKey("ClickObjectOverlay")) {
					Overlay = dataMap.get("ClickObjectOverlay").get(0);
					if (Overlay.equalsIgnoreCase("yes")) {
						switchToOverlay(browser);
					} else {
						switchToDefault(browser);
					}
				}
				if ((DefaultSelectedOption != null && !DefaultSelectedOption.equals("-"))) {
					if (!getElementByProperty(DefaultSelectedOption, browser).isSelected()) {
						messagesMap.put(DefaultSelectedOption, "Option is not Selected By Default");
						status = WARNING;
					}
				}
				if (VerifyNotAvailableofObects != null && !VerifyNotAvailableofObects.equals("-")) {
					String[] individualObjects = VerifyNotAvailableofObects.split("`");
					for (int i = 0; i < individualObjects.length; i++) {
						if (isElementPresentVerification(individualObjects[i], browser)) {
							messagesMap.put(individualObjects[i], "Object Should not be Present");
							status = WARNING;
						}
					}
				}
				if (VerifyAvailableofObjects != null && !VerifyAvailableofObjects.equals("-")) {
					String[] individualObjects = VerifyAvailableofObjects.split("`");
					for (int i = 0; i < individualObjects.length; i++) {
						if (!isElementPresentVerification(individualObjects[i], browser)) {
							messagesMap.put(individualObjects[i], "Object is missing");
							status = WARNING;
						}
					}
				}
			}
		} catch (Exception e) {
			status = FAIL;
			messagesMap.put("An Exception Occured:", e.getMessage());
			LogIt(e, null, stepBean);
		} finally {
			stepBean.setStepStatus(status);
		}
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method verify or Modify a value in XML file.
	 * @author - Siva
	 * @Created - AUG 12 2013
	 * @Input -
	 * @ModifiedBy - SIVA
	 * @ModifiedDate -01 OCT 2013
	 * @Modification - Method Updated to getValue from preffered node and
	 *               Modifying the value of any node in any tier
	 ************************************************************* 
	 */
	public static void verifyOrModifyAttributeinXML(EventFiringWebDriver browser, ScenarioBean scenarioBean, StepBean stepBean, Map<String, List<String>> dataMap) throws Exception {
		String status = PASS;
		String OperationMode = null, FilePath = null, FileName = null, ParentNode = null, subParentNode = null, RequiredNode = null, AttributeValue = null, AttributeName = null, NodeorAttributeChangeValue = null;
		String NodeorAttributeChangeType = null, RetrieveFileName = null, RetrieveSheetName = null, RetrieveRowID = null, RetrieveColumnID = null, mapFileKeyName = null, mapAttributeKeyName = null, duplicateParentNodes = null, parentNodeAttrubuteName = null, parentNodeAttrubuteValue = null;
		Map<String, String> messagesMap = null;
		Map<String, String> getSharedProperties = null;
		Map<String, String> getDBvlaues = null;
		String PutValueRowID = null;
		Node preferredNode = null, requiredAttribute = null;
		boolean isActionPerformed = false, isReqAttributeFound = false;
		try {
			if (messagesMap == null) {
				messagesMap = new HashMap<String, String>();
				stepBean.setStepStatusMessages(messagesMap);
			}
			getSharedProperties = scenarioBean.getSharedStepProperties();
			getDBvlaues = scenarioBean.getscenarioDBvalues();
			int totalRowCount = Integer.parseInt(dataMap.get("totalRowCount").get(0));
			for (int i = 0; i < totalRowCount; i++) {
				FileName = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "FileName");
				OperationMode = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "OperationMode");
				ParentNode = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "ParentNode");
				subParentNode = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "subParentNode");
				RequiredNode = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "RequiredNode");
				AttributeName = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "AttributeName");
				AttributeValue = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "AttributeValue");
				NodeorAttributeChangeType = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "NodeorAttributeChangeType");
				NodeorAttributeChangeValue = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "NodeorAttributeChangeValue");
				mapFileKeyName = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "mapFileKeyName");
				mapAttributeKeyName = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "mapAttributeKeyName");
				duplicateParentNodes = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "duplicateParentNodes");
				parentNodeAttrubuteName = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "parentNodeAttrubuteName");
				parentNodeAttrubuteValue = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "parentNodeAttrubuteValue");
				RetrieveFileName = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "RetrieveFileName");
				RetrieveSheetName = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "RetrieveSheetName");
				RetrieveRowID = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "RetrieveRowID");
				RetrieveColumnID = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "RetrieveColumnID");
				if (FileName != null && FileName.equalsIgnoreCase("getFromDifferentSheet")) {
					FileName = getTestData(RetrieveFileName, RetrieveSheetName, RetrieveRowID, RetrieveColumnID);
				} else if (FileName != null && FileName.equalsIgnoreCase("getFromHashMap")) {
					FileName = getSharedProperties.get(mapFileKeyName);
				}
				// To get the Row value from the same file and sheet to save the
				// node value get from XML file - SARAVANAN M
				if (OperationMode.equalsIgnoreCase("GetValue")) {// ****
					PutValueRowID = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "PutValueRowID");// ****
				}
				// File Path will be always Order_files folder from the Current
				// Directory.
				FilePath = System.getProperty("user.dir") + EcommTestRunner.config.getString("fulfillmentFilesPath") + FileName;
				if ((OperationMode.equalsIgnoreCase("Edit")) && NodeorAttributeChangeValue.equalsIgnoreCase("getFromHashMap")) {
					NodeorAttributeChangeValue = getDBvlaues.get(mapAttributeKeyName);
				}
				if (NodeorAttributeChangeValue != null && NodeorAttributeChangeValue.equalsIgnoreCase("getFromDifferentSheet")) {
					NodeorAttributeChangeValue = getTestData(RetrieveFileName, RetrieveSheetName, RetrieveRowID, RetrieveColumnID);
					System.out.println("NodeorAttributeChangeValue:" + NodeorAttributeChangeValue);
				}
				File fXMLFile = new File(FilePath);
				DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
				DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
				Document document = dBuilder.parse(fXMLFile);
				document.getDocumentElement().normalize();
				System.out.println("Root element :" + document.getDocumentElement().getNodeName());
				if (!(OperationMode.equals("-") && OperationMode == null)) {
					// Verifying for atleast one node to continue the operation
					// - SARAVANAN M
					if (ParentNode != null) {
						int availableParentNodesCount = ((NodeList) document.getElementsByTagName(ParentNode)).getLength();
						// Verifying presence of next node to continue else
						// parent node will be taken as Required node- SARAVANAN
						// M
						if (subParentNode == null) {
							for (int parentNodeCount = 0; parentNodeCount < availableParentNodesCount; parentNodeCount++) {
								preferredNode = document.getElementsByTagName(ParentNode).item(parentNodeCount);
								if (AttributeName != null) {
									requiredAttribute = preferredNode.getAttributes().getNamedItem(AttributeName);
									isReqAttributeFound = true;
									if (NodeorAttributeChangeType != null && NodeorAttributeChangeType.equals("NodeValuebyAttributeName")) {
										if (requiredAttribute.getTextContent().equalsIgnoreCase(AttributeValue)) {
											break;
										} else {
											isReqAttributeFound = false;
										}
									}
								} else {
									requiredAttribute = preferredNode;
									isReqAttributeFound = true;
								}
							}
						} else {
							// finding the no of same kind of Parent Nodes.
							for (int parentNodeCount = 0; parentNodeCount < availableParentNodesCount; parentNodeCount++) {
								boolean isRequiredParentNodeFound = false;
								String parentAttributeValue = null;
								// finding node list in the parent node
								NodeList nList = (NodeList) document.getElementsByTagName(ParentNode).item(parentNodeCount);
								if (duplicateParentNodes != null && duplicateParentNodes.equalsIgnoreCase("Yes")) {
									parentAttributeValue = ((NodeList) document.getElementsByTagName(ParentNode).item(parentNodeCount).getAttributes().getNamedItem(parentNodeAttrubuteName))
									.toString();
									if (parentAttributeValue.contains(parentNodeAttrubuteValue)) {
										isRequiredParentNodeFound = true;
									}
								} else {
									isRequiredParentNodeFound = true;
								}
								if (isRequiredParentNodeFound) {
									// finding the child nodes from the parent
									// node
									NodeList childNodes = ((Node) nList).getChildNodes();
									// running child nodes in a loop
									for (int count = 0; count < childNodes.getLength(); count++) {
										// verifying whether the child node of
										// Parent node is matching to our sub
										// Parent Node.
										if (childNodes.item(count).getNodeName().equalsIgnoreCase(subParentNode)) {
											// Verifying presence of next node
											// to continue else subparent node
											// will be taken as Required node-
											// SARAVANAN M
											if (RequiredNode == null) {
												preferredNode = childNodes.item(count);
												if (AttributeName != null) {
													requiredAttribute = preferredNode.getAttributes().getNamedItem(AttributeName);
													isReqAttributeFound = true;
													if (NodeorAttributeChangeType != null && NodeorAttributeChangeType.equals("NodeValuebyAttributeName")) {
														if (requiredAttribute.getTextContent().equalsIgnoreCase(AttributeValue)) {
															break;
														} else {
															isReqAttributeFound = false;
														}
													}
												} else {
													requiredAttribute = preferredNode;
													isReqAttributeFound = true;
												}
											} else {
												// Finding the sub child nodes
												// from the sub parent node.
												NodeList subChildNodes = ((Node) childNodes.item(count)).getChildNodes();
												// running sub child nodes in a
												// loop
												for (int subCount = 0; subCount < subChildNodes.getLength(); subCount++) {
													// Getting nodes form the
													// sub child nodes.
													Node nNode = subChildNodes.item(subCount);
													// verifying the whether the
													// found node is matching
													// with our required node
													// Verifying presence of
													// required node- SARAVANAN
													// M
													if (nNode.getNodeName().equalsIgnoreCase(RequiredNode)) {
														preferredNode = nNode;
														if (AttributeName != null) {
															requiredAttribute = preferredNode.getAttributes().getNamedItem(AttributeName);
															isReqAttributeFound = true;
															if (NodeorAttributeChangeType != null && NodeorAttributeChangeType.equals("NodeValuebyAttributeName")) {
																if (requiredAttribute.getTextContent().equalsIgnoreCase(AttributeValue)) {
																	break;
																} else {
																	isReqAttributeFound = false;
																}
															}
														} else {
															requiredAttribute = preferredNode;
															isReqAttributeFound = true;
														}
													}
												}
											}
										}
									}
								}
							}
						}
						if (isReqAttributeFound) {
							if (OperationMode.equalsIgnoreCase("Edit")) {
								if ((AttributeName != null) && (!NodeorAttributeChangeType.equals("NodeValuebyAttributeName"))) {
									preferredNode = preferredNode.getAttributes().getNamedItem(AttributeName);
								}
								preferredNode.setTextContent(NodeorAttributeChangeValue);
								isActionPerformed = true;
							}// Getting the value of preferred node using
							// Attribute name- SARAVANAN M
							else if (OperationMode.equalsIgnoreCase("GetValue")) {
								if ((AttributeName != null) && (!NodeorAttributeChangeType.equals("NodeValuebyAttributeName"))) {
									preferredNode = preferredNode.getAttributes().getNamedItem(AttributeName);
								}
								String NodeValue = preferredNode.getTextContent();
								TestDataDBManager.putTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), PutValueRowID, "NodeorAttributeChangeValue", NodeValue);
								isActionPerformed = true;
							}
						}
						TransformerFactory transformerFactory = TransformerFactory.newInstance();
						Transformer transformer = transformerFactory.newTransformer();
						DOMSource source = new DOMSource(document);
						StreamResult result = new StreamResult(new File(FilePath));
						transformer.transform(source, result);
					}
				}
				if (!isActionPerformed) {
					throw new Exception("Given Node was not found. Hence not able to Perform action");
				}
			}
		} catch (Exception e) {
			status = FAIL;
			messagesMap.put("An Exception Occured:", e.getMessage());
			LogIt(e, null, stepBean);
		} finally {
			stepBean.setStepStatus(status);
		}
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to find the required Order file from WINSCP
	 * @author - SARAVANAN M
	 * @Created - 24 SEP 2013
	 * @Input -
	 * @ModifiedBy -
	 * @ModifiedDate -
	 * @Modification -
	 ************************************************************* 
	 */
	public static void findCLOrderXML(EventFiringWebDriver browser, ScenarioBean scenarioBean, StepBean stepBean, Map<String, List<String>> dataMap) throws Exception {
		String status = PASS;
		String host = null, user = null, password = null, passphrase = null, VerifyValSheet = null, VerifyValFile = null, VerifyValRow = null, VerifyValColumn = null, SaveFileNameIn = null;
		String remoteDir = null, ReqFileName = null;
		boolean isFileFound = false;
		Map<String, String> messagesMap = null;
		Map<String, String> sharedProperties = null;
		try {
			if (messagesMap == null) {
				messagesMap = new HashMap<String, String>();
				stepBean.setStepStatusMessages(messagesMap);
			}
			sharedProperties = scenarioBean.getSharedStepProperties();
			if (sharedProperties == null) {
				sharedProperties = new HashMap<String, String>();
				scenarioBean.setSharedStepProperties(sharedProperties);
			}

			int totalRowCount = Integer.parseInt(dataMap.get("totalRowCount").get(0));
			for (int i = 0; i < totalRowCount; i++) {
				// Remote Host Info
				host = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "host");
				user = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "user");
				password = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "password");
				passphrase = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "passphrase");
				remoteDir = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "RemoteDir");
				VerifyValFile = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "VerifyValFile");
				VerifyValSheet = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "VerifyValSheet");
				VerifyValRow = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "VerifyValRow");
				VerifyValColumn = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "VerifyValColumn");
				SaveFileNameIn = getTestData(dataMap.get("InputFileName").get(i), dataMap.get("SheetName").get(i), dataMap.get("RowId").get(i), "SaveFileNameIn");

				String VerifyVal = getTestData(VerifyValFile, VerifyValSheet, VerifyValRow, VerifyValColumn);
				try {
					Session session = RemoteShellUtils.getSession(host, user, password, passphrase);
					System.out.print("\nSFTP 5: open channel");
					Channel channel = session.openChannel("sftp");
					System.out.print("\nSFTP 6: connect channel");
					channel.connect();
					System.out.print("\nSFTP 7: get sftp channel");
					ChannelSftp sftpChannel = (ChannelSftp) channel;
					System.out.print("\nSFTP 8: cd RemoteDir:" + remoteDir);
					System.out.print("\n++current RemoteDir before cd:" + sftpChannel.pwd());
					sftpChannel.cd(remoteDir);
					System.out.print("\nSFTP 9: current RemoteDir:" + sftpChannel.pwd());
					System.out.print("\nSFTP 10: get remote file start .......");

					// getting the list of all files in Orders folder
					@SuppressWarnings("unchecked")
					Vector<ChannelSftp.LsEntry> list = sftpChannel.ls("Orders");
					sftpChannel.cd("Orders");
					// iterate through objects in list, and check for extension
					for (ChannelSftp.LsEntry listEntry : list) {
						String SourceFileName = listEntry.getFilename();
						// Filtering the CL Order File
						if (SourceFileName.startsWith("O") || SourceFileName.endsWith(".XML")) {
							// Reading the filtered XML file with character set
							// 'UTF-8'
							InputStreamReader INSR = new InputStreamReader(sftpChannel.get(SourceFileName), "UTF-8");
							BufferedReader reader = new BufferedReader(INSR);
							String strCurrentLine = "", strAllLines = "";
							// getting all the content from the specific XML
							// file
							while ((strCurrentLine = reader.readLine()) != null) {
								strAllLines += strCurrentLine;
							}
							// Verifying the presence of given string in the
							// file
							if (strAllLines.contains(VerifyVal)) {
								ReqFileName = SourceFileName;
								isFileFound = true;
								break;
							}
							reader.close();
						}
					}
					System.out.print("\nSFTP 12: get remote file done");
					sftpChannel.exit();
					System.out.print("\nSFTP 14: exit channel");
					System.out.print("\nSFTP 16: get File successfully");

				} catch (Exception e) {
					e.printStackTrace();
				}

				if (!isFileFound) {
					throw new Exception("Unable to find the CL Order File");
				} else {
					if (SaveFileNameIn.equalsIgnoreCase("HashMap")) {
						sharedProperties.put("CLOrderXMLFile", ReqFileName);
					} else {
						TestDataDBManager.putTestData("BatchData", "Fulfillment", "CLInboundBatchOrderFile", "RemoteFileName", ReqFileName);
					}
				}
			}
		} catch (Exception e) {
			status = FAIL;
			messagesMap.put("An Exception Occured:", e.getMessage());
			LogIt(e, null, stepBean);
		} finally {
			stepBean.setStepStatus(status);
		}
	}

	/**
	 ************************************************************* 
	 * @Purpose - Read the File and return content of a file to a String
	 * @author - Siva Santhi Reddy P
	 * @throws IOException
	 * @Created - 08 OCT 2013
	 * @Input -
	 * @ModifiedBy -
	 * @ModifiedDate -
	 * @Modification -
	 ************************************************************* 
	 */
	public static String retrieveFileContentToaString(String FilePath) throws IOException {
		String totalFileContent = null;
		try {
			StringBuffer fileData = new StringBuffer();
			BufferedReader reader = new BufferedReader(new FileReader(FilePath));
			char[] buf = new char[1024];
			int numRead = 0;
			while ((numRead = reader.read(buf)) != -1) {
				String readData = String.valueOf(buf, 0, numRead);
				fileData.append(readData);
			}
			reader.close();
			totalFileContent = fileData.toString();
		} catch (Exception e) {
			totalFileContent = null;
		}
		return totalFileContent;
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to get the device data from the device_configuration
	 *          excel file
	 * @author - Ram
	 * @Created - 01/13/2014
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************* 
	 */
	public static String getDeviceConfiguration(String inputFileName, String sheetName, String device, String userAgent) {
		String useragent_string = null;
		String resolution_x = null;
		String resolution_y = null;
		String breakpoint = null;
		String deviceConfiguration = null;
		try {
			if (!"-".equals(inputFileName) && !"-".equals(sheetName) && !"-".equals(device) && !"-".equals(userAgent)) {

				String devicequery = (new StringBuilder("SELECT ")).append("*").append(" FROM [").append(sheetName).append("$]").append(" WHERE DEVICE = '").append(device.toUpperCase()).append("'")
				.append(" AND USERAGENT = '").append(userAgent.toUpperCase()).append("'").toString();

				Connection inputFileConnection = null;
				String excelFileExtension = ".xls";
				inputFileName = inputFileName + excelFileExtension;

				inputFileConnection = DriverManager.getConnection((new StringBuilder("jdbc:odbc:Driver={Microsoft Excel Driver (*" + excelFileExtension + ")};DBQ=")).append(inputFileName)
						.append(";READONLY=FALSE").toString());

				CachedRowSet queryOutput = AutomationDBManager.executeQuery(devicequery, inputFileConnection);
				if (queryOutput.size() == 0) {
					throw new Exception((new StringBuilder("No record found matching the device = ")).append(device).append("and useragent = ").append(userAgent).toString());
				}
				queryOutput.next();
				useragent_string = queryOutput.getString("USERAGENT_STRING");
				resolution_x = queryOutput.getString("RESOLUTION_X");
				resolution_y = queryOutput.getString("RESOLUTION_Y");
				breakpoint = queryOutput.getString("BREAKPOINT_CATEGORY");
				deviceConfiguration = useragent_string + "~" + resolution_x + "~" + resolution_y + "~" + breakpoint;

			}
		} catch (Exception e) {
			LogIt(null, e.getMessage(), null);
		}
		return deviceConfiguration;
	}

	/**
	 ************************************************************* 
	 * @Purpose - Method to create a JRTR bactch job input text file
	 * @author - SARAVANAN M
	 * @Created - 28-FEB-2014
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************* 
	 */
	public static void createJRTRInputFile(EventFiringWebDriver browser,ScenarioBean scenarioBean, StepBean stepBean, Map<String, List<String>> dataMap) {
		String status = PASS;
		String filePath=null,fileContent=null,OutputFilename="WAG_RRI2_C000000375_DATE_000000.txt";
		Map<String, String> sharedProperties = null;
		Map<String, String> messagesMap = null;
		try {
			if (messagesMap == null) {
				messagesMap = new HashMap<String, String>();
				stepBean.setStepStatusMessages(messagesMap);
			}


			sharedProperties = scenarioBean.getSharedStepProperties();
			if (sharedProperties == null) {
				sharedProperties = new HashMap<String, String>();
				scenarioBean.setSharedStepProperties(sharedProperties);
			}

			String FileLocation = getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "FileLocation");
			String PatId=getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "PatId");
			String PatFirstName=getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "PatFirstName");
			String PatLastName=getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "PatLastName");
			String PatRxNumber=getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "PatRxNumber");
			String PatEmail=getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "PatEmail");
			String FulfillmentFilePath=System.getProperty("user.dir") + EcommTestRunner.config.getString("fulfillmentFilesPath");

			if(FileLocation.equalsIgnoreCase("FulfillmentFiles")){
				filePath= FulfillmentFilePath+ "\\SAMPLE_JRTR.txt";
			}else{
				filePath=getTestData(dataMap.get("InputFileName").get(0), dataMap.get("SheetName").get(0), dataMap.get("RowId").get(0), "FilePath");
			}

			//reading the Sample input file content
			fileContent=retrieveFileContentToaString(filePath);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd");
			String CurrentDate = dateFormat.format(Calendar.getInstance().getTime());

			//Checking the presence of JRTR input file and creating the file if not present
			File file = new File(FulfillmentFilePath+"\\", OutputFilename.replace("DATE", CurrentDate));

			if (!file.exists()) {
				try {
					System.out.println("Creating New file.....");//Dont delete
					file.createNewFile();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}

			//Replacing the content with new values
			if(!(PatId.equals("-") || PatId.equals(null))){
				fileContent=fileContent.replaceAll("PATID1", PatId);
			}

			if(!(PatFirstName.equals("-") || PatFirstName.equals(null))){
				fileContent=fileContent.replaceAll("PATFIRSTNAME1", PatFirstName);
			}

			if(!(PatLastName.equals("-") || PatLastName.equals(null))){
				fileContent=fileContent.replaceAll("PATLASTNAME1", PatLastName);
			}

			if(!(PatRxNumber.equals("-") || PatRxNumber.equals(null))){
				fileContent=fileContent.replaceAll("PATRXNUMBER1", PatRxNumber);
			}

			if(!(PatEmail.equals("-") || PatId.equals(null))){
				fileContent=fileContent.replaceAll("PATEMAIL1", PatEmail);
			}

			//Writing the modified content to the input file
			System.out.println("Writing to the file.......");//Dont delete
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			writer.println(fileContent);
			writer.close();

			//Storing the created JRTR Input file name in Shared properties
			sharedProperties.put("JRTRInputFileName", OutputFilename);

		} catch (Exception e) {
			status = FAIL;
			messagesMap.put("An Exception Occured:", e.getMessage());
			LogIt(e, null, stepBean);
		} finally {
			stepBean.setStepStatus(status);
		}
	}
}