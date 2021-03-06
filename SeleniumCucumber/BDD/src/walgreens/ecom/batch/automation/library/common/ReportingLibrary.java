package walgreens.ecom.batch.automation.library.common;

import static org.junit.Assert.assertEquals;
import java.awt.Font;
import java.awt.Graphics2D;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import javax.imageio.ImageIO;
import org.apache.poi.common.usermodel.Hyperlink;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFFont;
import org.apache.poi.hssf.usermodel.HSSFHyperlink;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.CellStyle;
import org.apache.poi.ss.usermodel.CreationHelper;
import org.apache.poi.ss.usermodel.IndexedColors;
import org.ini4j.Config;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.support.events.EventFiringWebDriver;
import org.apache.commons.lang.StringUtils;
import walgreens.ecom.batch.framework.EcommTestRunner;
import walgreens.ecom.batch.framework.common.beans.ScenarioBean;
import walgreens.ecom.batch.framework.common.beans.StepBean;
import walgreens.ecom.batch.framework.common.utils.CucumberUtils;

public class ReportingLibrary extends CommonLibrary {

	public static String FAIL_SHEET = "FailTCs";
	public static String PASS_SHEET = "PassTCs";
	public static String COMPUTE_SCENARIO_STATUS = "computeScenarioStatus";
	public static String SUMMARY_SHEET = "summarysheetreport";

	// RAM (1/14/13) - EXCEL POI related values
	public static FileInputStream objfileInputStream = null;
	public static FileOutputStream objfileOutputStram = null;
	public static HSSFWorkbook objWorkbook = null;
	public static HSSFSheet objSheet, objScnearioSheet, objTestCaseSheet = null;
	public static HSSFRow objRow = null;
	public static HSSFCell objCell = null;

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Report the Status of the Individual Steps in
	 *          Cucumber Reporting Format
	 * @Input - HashMap
	 * @author - Imran Aslam
	 * @Created - March 23rd, 2012
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	public static void ReportItCucumber(EventFiringWebDriver browser, ScenarioBean scenario, StepBean step) {
		try {
			if (CommonLibrary.PASS.equalsIgnoreCase(step.getStepStatus())) {
				assertEquals(CommonLibrary.PASS, step.getStepStatus());
			} else if ("pass".equalsIgnoreCase(step.getStepStatus())) {
				assertEquals("pass", step.getStepStatus());
			} else if (CommonLibrary.WARNING.equalsIgnoreCase(step.getStepStatus())) {
				assertEquals(CommonLibrary.WARNING, step.getStepStatus());
			} else if ("warning".equalsIgnoreCase(step.getStepStatus())) {
				assertEquals("warning", step.getStepStatus());
			} else if (CommonLibrary.FAIL.equalsIgnoreCase(step.getStepStatus()) || "fail".equalsIgnoreCase(step.getStepStatus())) {
				assertEquals(CommonLibrary.PASS, step.getStepStatus());
			}
		} catch (Exception e) {
			LogIt(e, null, step);
		} finally {
			System.out.println("Status of the step: " + step.getStepName() + ": " + step.getStepStatus());
		}
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Report the Status To ALM
	 * @Input - HashMap
	 * @author - Imran Aslam
	 * @Created - March 23rd, 2012
	 * @Modified By - Siva, Ram (1/10/13)
	 * @Modified Date - Nov-29-12
	 ************************************************************************ 
	 */
	public static void ReportItALM(EventFiringWebDriver browser, ScenarioBean scenario, StepBean step) {
		try {
			String ScenarioStatus = scenario.getScenarioStatus();
			String QCStatus = null;
			if (scenario.getScenarioStatus() == WARNING) {
				ScenarioStatus = FAIL;

			}

			if (scenario.getScenarioStatus().equalsIgnoreCase("WARNING") || scenario.getScenarioStatus().equalsIgnoreCase("FAIL")) {
				QCStatus = "FAIL";
			} else if (scenario.getScenarioStatus().equalsIgnoreCase("PASS")) {
				QCStatus = "PASS";
			}

			Runtime.getRuntime().exec("cmd /c start " + "cscript " + System.getProperty("user.dir") + "\\Extensions\\QCUpdate.vbs" + " " + scenario.getScenarioId() + " " + QCStatus + " " + "60");
			Runtime.getRuntime().exec("cmd /c start " + System.getProperty("user.dir") + "\\Extensions\\QCLogout.vbs");
		} catch (Exception e) {
			LogIt(e, null, step);
		}
	}

	/************************************************************************
	 * @Purpose - Method to Report the Status of the Individual Steps to Excel
	 *          sheet
	 * @author - Siva Santhi Reddy
	 * @Created - Sep 11, 2012
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	public static void ReportItExcel(EventFiringWebDriver browser, ScenarioBean scenario, StepBean step) {
		POIFSFileSystem pfs = null;
		HSSFWorkbook workbook = null;

		try {
			File excelDir = new File(EcommTestRunner.excelReportDirectory);
			if (!excelDir.exists()) {
				excelDir.mkdirs();
			}
			String excelFilePath = EcommTestRunner.excelReportDirectory + File.separator + "Report-" + EcommTestRunner.currentDate + ".xls";
			File ScreenshotDir = new File(EcommTestRunner.screenshotDirectory);
			if (!ScreenshotDir.exists()) {
				ScreenshotDir.mkdirs();
			}
			String strScreenshotMainPath = EcommTestRunner.screenshotDirectory + File.separator + scenario.getScenarioId();
			boolean isFileExists = new File(excelFilePath).exists();
			if (!isFileExists) {
				workbook = createExcelWorkbook(excelFilePath);
			} else {
				pfs = new POIFSFileSystem(new FileInputStream(new File(excelFilePath)));
				workbook = new HSSFWorkbook(pfs);
			}

			if (CommonLibrary.FAIL.equalsIgnoreCase(scenario.getScenarioStatus()) || "fail".equalsIgnoreCase(scenario.getScenarioStatus())
					|| CommonLibrary.WARNING.equalsIgnoreCase(scenario.getScenarioStatus()) || "warning".equalsIgnoreCase(scenario.getScenarioStatus())) {
				if (workbook.getSheet(FAIL_SHEET) == null) {
					workbook = createExcelSheet(excelFilePath, workbook, FAIL_SHEET);
				}
			} else {
				if (workbook.getSheet(PASS_SHEET) == null) {
					workbook = createExcelSheet(excelFilePath, workbook, PASS_SHEET);
				}
			}

			if (CommonLibrary.FAIL.equalsIgnoreCase(scenario.getScenarioStatus()) || "fail".equalsIgnoreCase(scenario.getScenarioStatus())
					|| CommonLibrary.WARNING.equalsIgnoreCase(scenario.getScenarioStatus()) || "warning".equalsIgnoreCase(scenario.getScenarioStatus())) {
				if (workbook.getSheet(FAIL_SHEET) != null) {
					removeMatchingRows(scenario.getScenarioId(), excelFilePath, workbook, FAIL_SHEET);
				}
				if (workbook.getSheet(PASS_SHEET) != null) {
					removeMatchingRows(scenario.getScenarioId(), excelFilePath, workbook, PASS_SHEET);
				}
				if (workbook.getSheet(FAIL_SHEET) != null) {
					postFailTestData(browser, scenario, step, excelFilePath, workbook, FAIL_SHEET, strScreenshotMainPath);
				}
			} else {
				if (workbook.getSheet(FAIL_SHEET) != null) {
					removeMatchingRows(scenario.getScenarioId(), excelFilePath, workbook, FAIL_SHEET);
				}
				if (workbook.getSheet(PASS_SHEET) != null) {
					removeMatchingRows(scenario.getScenarioId(), excelFilePath, workbook, PASS_SHEET);
				}
				if (workbook.getSheet(PASS_SHEET) != null) {
					postPassTestData(browser, scenario, step, excelFilePath, workbook, PASS_SHEET);
				}
			}
		} catch (Exception e) {
			LogIt(e, null, step);
		}
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to remove the Matching rows in the Report Excel sheet
	 * @Input -
	 * @author - Imran Aslam
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	private static void removeMatchingRows(String rowId, String excelFilePath, HSSFWorkbook workbook, String sheetName) {
		FileOutputStream fos = null;
		ArrayList<Integer> matchingRowsIndexes = null;

		try {
			matchingRowsIndexes = (ArrayList<Integer>) findMatchingRowList(rowId, workbook, sheetName);
			while (matchingRowsIndexes != null && matchingRowsIndexes.size() > 0) {
				int lastRowNum = workbook.getSheet(sheetName).getLastRowNum();
				int rowIndex = matchingRowsIndexes.get(0).intValue();

				HSSFRow row = workbook.getSheet(sheetName).getRow(rowIndex);
				if (row != null) {
					workbook.getSheet(sheetName).removeRow(row);
				}

				if (rowIndex >= 0 && rowIndex < lastRowNum) {
					workbook.getSheet(sheetName).shiftRows(rowIndex + 1, lastRowNum, -1);
				}
				fos = new FileOutputStream(excelFilePath);
				workbook.write(fos);

				if (fos != null) {
					fos.close();
				}

				matchingRowsIndexes = (ArrayList<Integer>) findMatchingRowList(rowId, workbook, sheetName);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to find the Matching rows list in the Excel Report
	 *          sheet
	 * @Input -
	 * @author - Imran Aslam
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	private static List<Integer> findMatchingRowList(String rowId, HSSFWorkbook workbook, String sheetName) {
		ArrayList<Integer> matchingRowsIndexes = null;
		if (workbook.getSheet(sheetName) != null) {
			int lastRowNum = workbook.getSheet(sheetName).getLastRowNum();

			for (int i = 1; i <= lastRowNum; i++) {
				HSSFRow hssfRow = workbook.getSheet(sheetName).getRow(i);
				if (hssfRow != null) {
					HSSFCell cell = hssfRow.getCell(0);
					if (cell != null) {
						String firstCellValue = cell.getStringCellValue();
						if ((firstCellValue != null && firstCellValue.equalsIgnoreCase(rowId)) || (cell.getCellType() == cell.CELL_TYPE_BLANK)) {
							if (matchingRowsIndexes == null) {
								matchingRowsIndexes = new ArrayList<Integer>();
							}
							matchingRowsIndexes.add(Integer.valueOf(hssfRow.getRowNum()));
						}
					} else if (cell == null) {
						if (matchingRowsIndexes == null) {
							matchingRowsIndexes = new ArrayList<Integer>();
						}
						matchingRowsIndexes.add(Integer.valueOf(hssfRow.getRowNum()));
					}
				} else {
					if (i <= lastRowNum) {
						if (matchingRowsIndexes == null) {
							matchingRowsIndexes = new ArrayList<Integer>();
						}
						matchingRowsIndexes.add(Integer.valueOf(i));
					}
				}
			}
		}
		return matchingRowsIndexes;

	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Updating the Fail Scenario data in the Report Excel
	 *          sheet
	 * @Input -
	 * @author - Imran Aslam
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	private static void postFailTestData(EventFiringWebDriver browser, ScenarioBean scenario, StepBean step, String excelFilePath, HSSFWorkbook workbook, String sheetName, String strScreenshotMainPath)
	throws Exception {
		FileOutputStream fos = null;
		HSSFRow objRow = null;
		HSSFCell objCell = null;
		int lastRowNum = 0;
		List<StepBean> stepsList = scenario.getStepBeans();
		int listSize = stepsList.size();

		for (int i = 0; i < listSize; i++) {
			step = stepsList.get(i);
			if (CommonLibrary.FAIL.equalsIgnoreCase(step.getStepStatus()) || "fail".equalsIgnoreCase(step.getStepStatus()) || CommonLibrary.WARNING.equalsIgnoreCase(step.getStepStatus())
					|| "warning".equalsIgnoreCase(step.getStepStatus())) {
				lastRowNum = workbook.getSheet(sheetName).getLastRowNum();
				objRow = workbook.getSheet(sheetName).createRow(lastRowNum + 1);

				objCell = objRow.createCell(0);
				objCell.setCellValue(scenario.getScenarioId());

				objCell = objRow.createCell(1);
				if (CommonLibrary.FAIL.equalsIgnoreCase(scenario.getScenarioStatus()) || "fail".equalsIgnoreCase(scenario.getScenarioStatus())) {
					objCell.setCellValue("FAIL");
				} else if (CommonLibrary.WARNING.equalsIgnoreCase(scenario.getScenarioStatus()) || "warning".equalsIgnoreCase(scenario.getScenarioStatus())) {
					objCell.setCellValue("WARNING");
				}

				objCell = objRow.createCell(2);
				if (CommonLibrary.FAIL.equalsIgnoreCase(step.getStepStatus()) || "fail".equalsIgnoreCase(step.getStepStatus())) {
					objCell.setCellValue("FAIL");
				} else if (CommonLibrary.WARNING.equalsIgnoreCase(step.getStepStatus()) || "warning".equalsIgnoreCase(step.getStepStatus())) {
					objCell.setCellValue("WARNING");
				}

				objCell = objRow.createCell(3);
				objCell.setCellValue(scenario.getScenarioFeatureName());

				objCell = objRow.createCell(4);
				objCell.setCellValue(step.getStepName());

				objCell = objRow.createCell(5);
				objCell.setCellValue(createMessegesString(step));

				objCell = objRow.createCell(6);
				objCell.setCellValue(EcommTestRunner.currentBrowserName);

				objCell = objRow.createCell(7);
				objCell.setCellValue(scenario.getScenarioCurrentUrl());

				if (scenario.isALMReportEnabled()) {
					objCell = objRow.createCell(8);
					objCell.setCellValue("yes");
				}
				if (CommonLibrary.FAIL.equalsIgnoreCase(scenario.getScenarioStatus()) || "fail".equalsIgnoreCase(scenario.getScenarioStatus())
						|| CommonLibrary.WARNING.equalsIgnoreCase(scenario.getScenarioStatus()) || "warning".equalsIgnoreCase(scenario.getScenarioStatus())) {
					objCell = objRow.createCell(9);
					objCell.setCellValue("FAIL_SCREENSHOT");

					// Printing required screenshot in the Report path and
					// attaching Hyperlink to the Report Excel sheet.
					Calendar cal = Calendar.getInstance();
					String timeinmills = Long.toString(cal.getTimeInMillis());
					String FinalScreenshotPath = strScreenshotMainPath + "_Screenshot_" + EcommTestRunner.currentDateTime + "_" + timeinmills + ".png";
					File strscreenshotFile = new File(FinalScreenshotPath);
					ImageIO.write(step.getScreenShot(), "png", strscreenshotFile);
					System.out.println("HTML path  :" + Config.getSystemProperty("user.dir") + "\\" + strscreenshotFile);
					CreationHelper createHelper = workbook.getCreationHelper();
					Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
					link.setAddress(Config.getSystemProperty("user.dir") + "\\" + strscreenshotFile);
					objCell.setHyperlink((org.apache.poi.ss.usermodel.Hyperlink) link);
				}
				fos = new FileOutputStream(excelFilePath);
				workbook.write(fos);
				if (fos != null) {
					fos.close();
				}
			}
		}
	}

	private static String createMessegesString(StepBean step) {
		String messages = null, messageValue=null;
		int msgCnt = 0;
		Map<String, String> messagesMap = null;
		try {
			messagesMap = step.getStepStatusMessages();
			if (messagesMap == null || (messagesMap != null && messagesMap.size() == 0)) {
				if (step.getStatusDescription() != null) {
					messages = step.getStatusDescription();
				}
			} else {
				Iterator<Entry<String, String>> it = messagesMap.entrySet().iterator();
				while (it.hasNext()) {
					msgCnt += 1; // RAM - 1/14/13 - Added message count to give
					// the number of messages in a clean manner
					Map.Entry<String, String> message = (Map.Entry<String, String>) it.next();
					messageValue=message.getValue();
					if (messageValue==null)
					{
						messageValue="Looks like a Null Pointer Exception has occured. Please check the console/source code for any expcetion/null values.";
					}
					if (messages == null) {
						messages = msgCnt + ". " + message.getKey() + " -> " + messageValue + "\n";
					} else {
						messages += msgCnt + ". " + message.getKey() + " -> " + messageValue + "\n";
					}
					it.remove();
				}
			}
		} catch (Exception e) {
			System.out.println(e.getMessage());
		}

		return messages;
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Update the Pass Scenarios data in the Report Excel
	 *          sheet
	 * @Input -
	 * @author - Imran Aslam
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	private static void postPassTestData(EventFiringWebDriver browser, ScenarioBean scenario, StepBean step, String excelFilePath, HSSFWorkbook workbook, String sheetName) throws IOException {
		// createExcelSheet(excelFilePath, PASS_SHEET);

		FileOutputStream fos = null;
		POIFSFileSystem pfs = null;
		HSSFRow objRow = null;
		HSSFCell objCell = null;

		pfs = new POIFSFileSystem(new FileInputStream(new File(excelFilePath)));
		workbook = new HSSFWorkbook(pfs);
		objRow = workbook.getSheet(sheetName).createRow(workbook.getSheet(sheetName).getLastRowNum() + 1);

		objCell = objRow.createCell(0);
		objCell.setCellValue(scenario.getScenarioId());

		objCell = objRow.createCell(1);
		if (CommonLibrary.PASS.equalsIgnoreCase(scenario.getScenarioStatus()) || "pass".equalsIgnoreCase(scenario.getScenarioStatus())) {
			objCell.setCellValue("PASS");
		} else if ("disabled".equalsIgnoreCase(scenario.getScenarioStatus())) {
			objCell.setCellValue("DISABLED");
		}

		objCell = objRow.createCell(2);
		objCell.setCellValue(scenario.getScenarioFeatureName());
		objCell = objRow.createCell(3);
		objCell.setCellValue(EcommTestRunner.currentBrowserName);

		if (scenario.isALMReportEnabled()) {
			objCell = objRow.createCell(4);
			objCell.setCellValue("yes");
		}

		fos = new FileOutputStream(excelFilePath);
		workbook.write(fos);
		if (fos != null) {
			fos.close();
		}

		System.out.println("Scenario Status: " + scenario.getScenarioStatus());
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Create Excel sheet
	 * @Input -
	 * @author - Imran Aslam
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	public static HSSFWorkbook createExcelSheet(String excelFilePath, HSSFWorkbook workbook, String sheetName) throws IOException {
		FileOutputStream fos = null;
		try {
			if (workbook.getSheet(sheetName) == null) {
				fos = new FileOutputStream(excelFilePath);
				workbook.createSheet(sheetName);
				workbook.write(fos);
				if (fos != null) {
					fos.close();
				}

				// Create Header for the newly Created Sheet
				if (FAIL_SHEET.equalsIgnoreCase(sheetName)) {
					createFailReportHeader(workbook, excelFilePath, sheetName);
				} else if (PASS_SHEET.equalsIgnoreCase(sheetName)) {
					createPassReportHeader(workbook, excelFilePath, sheetName);
				} else if (SUMMARY_SHEET.equalsIgnoreCase(sheetName)) {
					;
				}
			}
		} catch (Exception e) {
			LogIt(e, null, null);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}

		return workbook;
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Create Excel Work book
	 * @Input -
	 * @author - Imran Aslam
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	public static HSSFWorkbook createExcelWorkbook(String excelFilePath) throws IOException {
		HSSFWorkbook workbook = null;
		try {
			if (new File(excelFilePath).createNewFile()) {
				workbook = new HSSFWorkbook();
			}
		} catch (Exception e) {
			LogIt(e, null, null);
		}
		return workbook;
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Create Failure Report Header in the Report Excel
	 *          sheet.
	 * @Input -
	 * @author - Imran Aslam
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	private static void createFailReportHeader(HSSFWorkbook workbook, String excelFilePath, String sheetName) throws IOException {
		FileOutputStream fos = null;
		try {
			HSSFCellStyle style = workbook.createCellStyle();

			style.setBorderBottom(HSSFCellStyle.BORDER_THICK);
			style.setBorderTop(HSSFCellStyle.BORDER_THICK);
			style.setBorderLeft(HSSFCellStyle.BORDER_THICK);
			style.setBorderRight(HSSFCellStyle.BORDER_THICK);

			style.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			// creating a custom palette for the workbook
			// HSSFPalette palette = workbook.getCustomPalette();
			// HSSFColor myColor = palette.addColor((byte) 153, (byte) 0, (byte)
			// 0);

			// style.setFillBackgroundColor(HSSFColor.BLUE.index);
			style.setFillForegroundColor(HSSFColor.BLUE.index);

			HSSFSheet excelSheet = workbook.getSheet(sheetName);
			HSSFRow objRow = excelSheet.createRow(0);

			HSSFCell objCell = objRow.createCell(0);
			objCell.setCellValue("ScenarioID");
			objCell.setCellStyle(style);
			objCell = objRow.createCell(1);
			objCell.setCellValue("ScenarioStatus");
			objCell = objRow.createCell(2);
			objCell.setCellValue("StepStatus");
			objCell = objRow.createCell(3);
			objCell.setCellValue("FeatureName:");
			objCell = objRow.createCell(4);
			objCell.setCellValue("ScenarioStepName");
			objCell = objRow.createCell(5);
			objCell.setCellValue("StepStatusDescription");
			objCell = objRow.createCell(6);
			objCell.setCellValue("BrowserName");
			objCell = objRow.createCell(7);
			objCell.setCellValue("ScenarioCurrentUrl");
			objCell = objRow.createCell(8);
			objCell.setCellValue("AlmReport");
			objCell = objRow.createCell(9);
			objCell.setCellValue("Screenshot");

			fos = new FileOutputStream(excelFilePath);
			workbook.write(fos);
			if (fos != null) {
				fos.close();
			}
		} catch (Exception e) {
			LogIt(e, null, null);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Create Pass Scenarios Header template in the Report
	 *          Excel sheet.
	 * @Input -
	 * @author - Imran Aslam
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	private static void createPassReportHeader(HSSFWorkbook workbook, String excelFilePath, String sheetName) throws IOException {
		FileOutputStream fos = null;
		try {
			HSSFSheet excelSheet = workbook.getSheet(sheetName);
			HSSFRow objRow = excelSheet.createRow(0);

			HSSFCell objCell = objRow.createCell(0);
			objCell.setCellValue("ScenarioID");
			objCell = objRow.createCell(1);
			objCell.setCellValue("ScenarioStatus:");
			objCell = objRow.createCell(2);
			objCell.setCellValue("FeatureName:");
			objCell = objRow.createCell(3);
			objCell.setCellValue("BrowserName");
			objCell = objRow.createCell(4);
			objCell.setCellValue("AlmReport");
			fos = new FileOutputStream(excelFilePath);
			workbook.write(fos);
		} catch (Exception e) {
			LogIt(e, null, null);
		} finally {
			if (fos != null) {
				fos.close();
			}
		}
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to removing the Empty rows in the Excel sheet
	 * @Input -
	 * @author - Imran Aslam
	 * @Created -
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	private static void removeEmptyRows(HSSFWorkbook workbook, String sheetName) {
		HSSFSheet hsheet = workbook.getSheet(sheetName);

		boolean stop = false;
		boolean nonBlankRowFound;
		short c;
		HSSFRow lastRow = null;
		HSSFCell cell = null;

		while (stop == false) {
			nonBlankRowFound = false;
			lastRow = hsheet.getRow(hsheet.getLastRowNum());
			for (c = lastRow.getFirstCellNum(); c <= lastRow.getLastCellNum(); c++) {
				cell = lastRow.getCell(c);
				if (cell != null && lastRow.getCell(c).getCellType() != HSSFCell.CELL_TYPE_BLANK) {
					nonBlankRowFound = true;
				}
			}
			if (nonBlankRowFound == true) {
				stop = true;
			} else {
				hsheet.removeRow(lastRow);
			}
		}// End of while

	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to Take the Browser Screenshot
	 * @Input -
	 * @author - Siva Santhi Reddy
	 * @Created -24-Oct-12
	 * @Modified By -Siva Santhi Reddy
	 * @Modified Date - 01-Nov-12
	 ************************************************************************ 
	 */
	public static BufferedImage takeScreenshot(EventFiringWebDriver browser) throws Exception {
		BufferedImage bufferedImage = null;
		try {
			File ScreenshotFile = browser.getScreenshotAs(OutputType.FILE);
			Image ScreenshotImage = ImageIO.read(ScreenshotFile);
			bufferedImage = new BufferedImage(ScreenshotImage.getWidth(null), ScreenshotImage.getHeight(null), BufferedImage.TYPE_INT_RGB);
			Graphics2D g = bufferedImage.createGraphics();
			g.drawImage(ScreenshotImage, null, null);

		} catch (Exception e) {
			LogIt(e, null, null);
		}

		return bufferedImage;
	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to create the current run report structure + OverAll
	 *          Summary Sheet
	 * @Input -
	 * @author - Ram
	 * @Created -14-JAN-13
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */
	public static void createCurrentRunReportPath() throws Exception {

		try {
			String strCurrDateReportPath = null, strCurrRunReportPath = null, strScreenshotsPath = null, strRunReportPath = null, strCurrDateTime = null;

			// Create the overall date report folder e.g. REPORT_1_1_2013

			strCurrDateReportPath = (String) CucumberUtils.getCurrentDate();
			System.out.println("@@@createCurrentRunReportPath::strCurrDateReportPath :::"+strCurrDateReportPath);
			//report/excel/staging/functional

			File currDatefolder = new File(EcommTestRunner.excelReportDirectory + File.separator + strCurrDateReportPath);
			System.out.println("@@@createCurrentRunReportPath::currDatefolder :::"+currDatefolder);

			//report/excel/staging/functional/03-22-2014
			if (!currDatefolder.exists())
				currDatefolder.mkdir();

			// Create the current run report folder e.g.
			// RUN_1-11-2013_01_27_00_PM
			strCurrDateTime = CucumberUtils.getCurrentDateTime();
			strCurrRunReportPath = (String) "RUN_" + strCurrDateTime;
			System.out.println("@@@createCurrentRunReportPath::strCurrRunReportPath :::"+strCurrRunReportPath);
			//strCurrRunReportPath = report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM

			File currRunFolder = new File(EcommTestRunner.excelReportDirectory + File.separator + strCurrDateReportPath + File.separator + strCurrRunReportPath);
			System.out.println("@@@createCurrentRunReportPath::currRunFolder :::"+currRunFolder);
			if (!currRunFolder.exists())
				currRunFolder.mkdir();

			// Current full run report path
			strRunReportPath = EcommTestRunner.excelReportDirectory + File.separator + strCurrDateReportPath + File.separator + strCurrRunReportPath;
			System.out.println("@@@createCurrentRunReportPath::strRunReportPath :::"+strRunReportPath);
			EcommTestRunner.runReportPath = strRunReportPath;
			System.out.println("@@@createCurrentRunReportPath::EcommTestRunner.runReportPath ::: "+EcommTestRunner.runReportPath );
			//strRunReportPath = runReportPath = report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM
			// Create the screen-shots folder e.g.screenshots
			strScreenshotsPath = strRunReportPath + File.separator + "Screenshots";
			System.out.println("@@@createCurrentRunReportPath::strScreenshotsPath :::"+strScreenshotsPath );
			//  strScreenshotsPath = report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM/Screenshots
			File screenshotsFolder = new File(strScreenshotsPath);
			if (!screenshotsFolder.exists())
				screenshotsFolder.mkdir();
			EcommTestRunner.screenshotDirectory = strScreenshotsPath;
			System.out.println("@@@createCurrentRunReportPath::  EcommTestRunner.screenshotDirectory ::"+  EcommTestRunner.screenshotDirectory  );
			//strScreenshotsPath = screenshotDirectory = report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM/Screenshots
			// Create the Over All Run Summary xls file
			// (  report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM, OVERALL_RUN_SUMMARY_1-11-2013_01_27_00_PM,SUMMARY_DASHBOARD )
			createExcelWorkBook(strRunReportPath, EcommTestRunner.config.getString("overAllSummaryFile") + "_" + strCurrDateTime, "SUMMARY_DASHBOARD");
			// (  report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM, OVERALL_RUN_SUMMARY_1-11-2013_01_27_00_PM,PASS_SCENARIOS )
			createExcelWorkSheet(strRunReportPath, EcommTestRunner.config.getString("overAllSummaryFile") + "_" + strCurrDateTime, "PASS_SCENARIOS");
			// (  report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM, OVERALL_RUN_SUMMARY_1-11-2013_01_27_00_PM,FAIL_SCENARIOS )
			createExcelWorkSheet(strRunReportPath, EcommTestRunner.config.getString("overAllSummaryFile") + "_" + strCurrDateTime, "FAIL_SCENARIOS");
			// (  report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM, OVERALL_RUN_SUMMARY_1-11-2013_01_27_00_PM,DISABLED_SCENARIOS )
			createExcelWorkSheet(strRunReportPath, EcommTestRunner.config.getString("overAllSummaryFile") + "_" + strCurrDateTime, "DISABLED_SCENARIOS");
			System.out.println("@@@@@@@@runReportPath::::"+EcommTestRunner.runReportPath);
			//report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM/OVERALL_RUN_SUMMARY

			EcommTestRunner.overAllSummaryFile = EcommTestRunner.runReportPath + File.separator + EcommTestRunner.config.getString("overAllSummaryFile") + "_" + strCurrDateTime + ".xls";
			//report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM/OVERALL_RUN_SUMMARY
			System.out.println("EcommTestRunner.overAllSummaryFile:::"+EcommTestRunner.overAllSummaryFile);
			createOverAllSummaryExcelHeader(EcommTestRunner.overAllSummaryFile);

		} catch (Exception e) {
			LogIt(e, null, null);
		}

	}

	/**
	 *********************************************************************** 
	 * @Purpose - Adds a new workbook, optional parameter - SheetName
	 * @author - Ramgopal Narayanan
	 * @Created - 14 JAN 2013
	 * @ModifiedBy -
	 * @ModifiedDate -
	 *********************************************************************** 
	 */
	// (  report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM, OVERALL_RUN_SUMMARY_1-11-2013_01_27_00_PM,SUMMARY_DASHBOARD ))
	public static synchronized void createExcelWorkBook(String strFilePath, String strFilename, String strSheetName) throws Exception {
		try {
			// Create the file only if doesn't exists
			if (!new File(strFilePath + File.separator + strFilename + ".xls").exists())
				// report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM/OVERALL_RUN_SUMMARY_1-11-2013_01_27_00_PM.xls
				objfileOutputStram = new FileOutputStream(strFilePath + File.separator + strFilename + ".xls");
			objWorkbook = new HSSFWorkbook();
			if (StringUtils.isNotBlank(strSheetName))
				objWorkbook.createSheet(strSheetName);
			objWorkbook.write(objfileOutputStram);
			objfileOutputStram.close();

		} catch (Exception e) {
			LogIt(e, null, null);
		}

	}

	/**
	 *********************************************************************** 
	 * @Purpose - Adds a new sheet to an existing workbook
	 * @author - Ramgopal Narayanan
	 * @Created - 14 JAN 2013
	 * @ModifiedBy -
	 * @ModifiedDate -
	 *********************************************************************** 
	 */
	// (  report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM, OVERALL_RUN_SUMMARY_1-11-2013_01_27_00_PM,PASS_SCENARIOS )
	public static synchronized void createExcelWorkSheet(String strFilePath, String strFilename, String strSheetName) throws Exception {
		try {
			if (new File(strFilePath + File.separator + strFilename + ".xls").exists())
				//report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM/OVERALL_RUN_SUMMARY_03-22-2014-23-59-33-PM.xls
				objfileInputStream = new FileInputStream(strFilePath + File.separator + strFilename + ".xls");
			objWorkbook = new HSSFWorkbook(objfileInputStream);

			// Create the sheet only when it doesn't exist
			if (objWorkbook.getNumberOfSheets() == 0) {
				objWorkbook.createSheet(strSheetName);
				objfileOutputStram = new FileOutputStream(strFilePath + File.separator + strFilename + ".xls");
				objWorkbook.write(objfileOutputStram);
				objfileInputStream.close();
				objfileOutputStram.close();
			} else {
				for (int intSheetCtr = 0; intSheetCtr < objWorkbook.getNumberOfSheets(); intSheetCtr++) {

					if (!objWorkbook.getSheetName(intSheetCtr).equalsIgnoreCase(strSheetName))
						objWorkbook.createSheet(strSheetName);
					objfileOutputStram = new FileOutputStream(strFilePath + File.separator + strFilename + ".xls");
					objWorkbook.write(objfileOutputStram);
					objfileInputStream.close();
					objfileOutputStram.close();
					break;
				}
			}

		} catch (Exception e) {
			LogIt(e, null, null);
		}

	}

	/**
	 *********************************************************************** 
	 * @Purpose - Creates the header for the OverAll Run Summary File
	 * @author - Ramgopal Narayanan
	 * @Created - 14 JAN 2013
	 * @ModifiedBy -
	 * @ModifiedDate -
	 *********************************************************************** 
	 */
	//report/excel/staging/functional/03-22-2014/RUN_03-22-2014-20-35-36-PM/OVERALL_RUN_SUMMARY
	public static void createOverAllSummaryExcelHeader(String strExcelFileName) throws Exception {
		try {
			// 1. Create the Over All Run Summary File IS
			objfileInputStream = new FileInputStream(strExcelFileName);
			objWorkbook = new HSSFWorkbook(objfileInputStream);

			// 2. Cell Style
			CellStyle cellStyle = objWorkbook.createCellStyle();
			HSSFFont cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellFont.setColor(IndexedColors.BLACK.getIndex());
			cellStyle.setFont(cellFont);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);

			// 2.a. PASS SCENARIOS
			objSheet = objWorkbook.getSheet("PASS_SCENARIOS");
			objRow = objSheet.createRow(0);

			objCell = objRow.createCell(0);
			objCell.setCellValue("SCENARIO_ID");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(2);
			objCell.setCellValue("MODULE");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(3);
			objCell.setCellValue("FEATURE_FILE_NAME");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(1);
			objCell.setCellValue("SCENARIO_STATUS");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(4);
			objCell.setCellValue("EXECUTION_DURATION");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(6);
			objCell.setCellValue("DETAILED_RESULTS");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(5);
			objCell.setCellValue("BROWSER");
			objCell.setCellStyle(cellStyle);

			// 2.b. FAIL SCENARIOS
			objSheet = objWorkbook.getSheet("FAIL_SCENARIOS");
			objRow = objSheet.createRow(0);

			objCell = objRow.createCell(0);
			objCell.setCellValue("SCENARIO_ID");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(2);
			objCell.setCellValue("MODULE");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(3);
			objCell.setCellValue("FEATURE_FILE_NAME");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(1);
			objCell.setCellValue("SCENARIO_STATUS");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(4);
			objCell.setCellValue("EXECUTION_DURATION");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(6);
			objCell.setCellValue("DETAILED_RESULTS");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(5);
			objCell.setCellValue("BROWSER");
			objCell.setCellStyle(cellStyle);

			// 2.c. DISABLED SCENARIOS
			objSheet = objWorkbook.getSheet("DISABLED_SCENARIOS");
			objRow = objSheet.createRow(0);

			objCell = objRow.createCell(0);
			objCell.setCellValue("SCENARIO_ID");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(2);
			objCell.setCellValue("MODULE");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(3);
			objCell.setCellValue("FEATURE_FILE_NAME");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(1);
			objCell.setCellValue("SCENARIO_STATUS");
			objCell.setCellStyle(cellStyle);

			// Write the file
			objfileOutputStram = new FileOutputStream(strExcelFileName);
			objWorkbook.write(objfileOutputStram);
			objfileInputStream.close();
			objfileOutputStram.close();

		} catch (Exception e) {
			LogIt(e, null, null);
		}

	}

	/**
	 *********************************************************************** 
	 * @Purpose - Creates the header for the individual scenario results sheet
	 * @author - Ramgopal Narayanan
	 * @Created - 14 JAN 2013
	 * @ModifiedBy -
	 * @ModifiedDate -
	 *********************************************************************** 
	 */
	public static synchronized void createScenarioSummaryExcelHeader(String strExcelFileName) throws Exception {
		try {
			// 1. Create the Individual Scenario Summary File IS
			objfileInputStream = new FileInputStream(strExcelFileName);
			objWorkbook = new HSSFWorkbook(objfileInputStream);

			// 2. Cell Style
			CellStyle cellStyle = objWorkbook.createCellStyle();
			HSSFFont cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellFont.setColor(IndexedColors.BLACK.getIndex());
			cellStyle.setFont(cellFont);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);

			// 3. Scenario Summary Sheet Values
			objSheet = objWorkbook.getSheet("SCENARIO_SUMMARY");
			objRow = objSheet.createRow(0);

			objCell = objRow.createCell(0);
			objCell.setCellValue("STEP_NAME");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(1);
			objCell.setCellValue("DESCRIPTION");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(2);
			objCell.setCellValue("EXECUTION_DATE_TIME");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(3);
			objCell.setCellValue("STEP_STATUS");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(4);
			objCell.setCellValue("SCREENSHOT");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(5);
			objCell.setCellValue("BROWSER_NAME");
			objCell.setCellStyle(cellStyle);

			objCell = objRow.createCell(6);
			objCell.setCellValue("APPLICATION_URL");
			objCell.setCellStyle(cellStyle);

			objfileOutputStram = new FileOutputStream(strExcelFileName);
			objWorkbook.write(objfileOutputStram);
			objfileInputStream.close();
			objfileOutputStram.close();

		} catch (Exception e) {
			LogIt(e, null, null);
		}

	}

	/**
	 ************************************************************************ 
	 * @Purpose - Method to create a folder in the specified parent folder path
	 * @Input - Parent folder path, Name of fodler to create
	 * @author - Ram
	 * @Created -14-JAN-13
	 * @Modified By -
	 * @Modified Date -
	 ************************************************************************ 
	 */

	public static synchronized String createFolder(String strParenFolderPath, String strFolderToCreate) throws Exception {
		try {
			File fFolderPath = new File(strParenFolderPath + File.separator + strFolderToCreate);
			if (!fFolderPath.exists())
				fFolderPath.mkdir();
			return strParenFolderPath + File.separator + strFolderToCreate;

		} catch (Exception e) {
			LogIt(e, null, null);
			return null;
		}
	}

	/**
	 *********************************************************************** 
	 * @Purpose - Update the scenario sheet with individual step status
	 * @author - Ramgopal Narayanan
	 * @Created - 14 JAN 2013
	 * @ModifiedBy -
	 * @ModifiedDate -
	 *********************************************************************** 
	 */
	public static synchronized void updateStepStatus(EventFiringWebDriver browser, ScenarioBean scenario, StepBean step) throws Exception {

		Date now = new Date();
		objfileInputStream = new FileInputStream(scenario.getScenarioReportFile() + ".xls");
		objWorkbook = new HSSFWorkbook(objfileInputStream);
		objSheet = objWorkbook.getSheet("SCENARIO_SUMMARY");
		int intRowToWrire = objSheet.getPhysicalNumberOfRows();

		// A. Cell Style
		CellStyle cellStyle = objWorkbook.createCellStyle();
		HSSFFont cellFont = objWorkbook.createFont();
		cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

		// B. Write the result
		objRow = objSheet.createRow(intRowToWrire);

		// 1. Step Name
		objCell = objRow.createCell(0);
		objCell.setCellValue(step.getStepName().toUpperCase());

		// 2. Date & Time
		objCell = objRow.createCell(2);
		objCell.setCellValue(DateFormat.getInstance().format(now));

		// 3. Step Status
		if (step.isFirstStep() || step.isFinalStep()) {
			cellStyle.setFillForegroundColor(IndexedColors.LIGHT_YELLOW.getIndex());
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellFont.setColor(IndexedColors.BROWN.getIndex());
			cellStyle.setFont(cellFont);

			// 1. Step Name
			objCell = objRow.createCell(0);
			objCell.setCellValue(step.getStepName().toUpperCase());
			objCell.setCellStyle(cellStyle);

			// 2. Description
			objCell = objRow.createCell(1);
			objCell.setCellValue("");
			objCell.setCellStyle(cellStyle);

			// 3. Date & Time
			objCell = objRow.createCell(2);
			objCell.setCellValue(DateFormat.getInstance().format(now));
			objCell.setCellStyle(cellStyle);

			// 4. Status
			if (step.isFirstStep()) {
				objCell = objRow.createCell(3);
				objCell.setCellValue("START");
				objCell.setCellStyle(cellStyle);
			} else if (step.isFinalStep()) {
				objCell = objRow.createCell(3);
				objCell.setCellValue("END");
				objCell.setCellStyle(cellStyle);
			}

			// 5. Screen-shot
			objCell = objRow.createCell(4);
			objCell.setCellValue("");
			objCell.setCellStyle(cellStyle);

			// 6. Current Browser Name
			objCell = objRow.createCell(5);
			objCell.setCellValue(EcommTestRunner.currentBrowserName);
			objCell.setCellStyle(cellStyle);

			// 7. Current Scenario URL
			objCell = objRow.createCell(6);
			objCell.setCellValue(scenario.getScenarioCurrentUrl());
			objCell.setCellStyle(cellStyle);

		} else if (step.getStepStatus().equalsIgnoreCase("PASS") || step.getStepStatus().equals(PASS)) {
			cellFont.setColor(IndexedColors.GREEN.getIndex());
			cellStyle.setFont(cellFont);
			objCell = objRow.createCell(3);
			objCell.setCellValue("PASS");
			objCell.setCellStyle(cellStyle);

			// Take Screenshot
			if (EcommTestRunner.config.getString("takePassScreenshot").equalsIgnoreCase("YES")) {
				String strScreenShotFileName = System.getProperty("user.dir") + File.separator + EcommTestRunner.screenshotDirectory + File.separator + scenario.getScenarioNumber() + "_"
				+ EcommTestRunner.runVDI.toUpperCase() + "_" + scenario.getScenarioFeatureName() + "_" + CucumberUtils.getCurrentDateTime() + "_PASS";
				File fScreenshotFile = new File(strScreenShotFileName + ".png");
				ImageIO.write(takeScreenshot(browser), "png", fScreenshotFile);

				CreationHelper createHelper = objWorkbook.getCreationHelper();
				// cell style for hyperlinks. by default hypelrinks are blue and
				// underlined
				CellStyle linkStyle = objWorkbook.createCellStyle();
				HSSFFont linkFont = objWorkbook.createFont();
				linkFont.setUnderline((byte) Font.BOLD);
				linkFont.setColor(IndexedColors.BLUE.getIndex());
				linkStyle.setFont(linkFont);

				// Sheet & cell details
				objSheet = objWorkbook.getSheet("SCENARIO_SUMMARY");
				objCell = objRow.createCell(4);
				objCell.setCellValue("PASS_SCREENSHOT");

				Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
				link.setAddress(strScreenShotFileName + ".png");
				objCell.setHyperlink((org.apache.poi.ss.usermodel.Hyperlink) link);
				objCell.setCellStyle(linkStyle);
			}
		} else if (step.getStepStatus().equalsIgnoreCase("WARNING") || step.getStepStatus().equals(WARNING)) {
			scenario.setWarningStatus(true);
			cellFont.setColor(IndexedColors.ORANGE.getIndex());
			cellStyle.setFont(cellFont);
			objCell = objRow.createCell(3);
			objCell.setCellValue("WARNING");
			objCell.setCellStyle(cellStyle);

			// 4. Description
			objCell = objRow.createCell(1);
			objCell.setCellValue(createMessegesString(step));

			// 5. Take Screenshot
			String strScreenShotFileName = System.getProperty("user.dir") + File.separator + EcommTestRunner.screenshotDirectory + File.separator + scenario.getScenarioNumber() + "_"
			+ EcommTestRunner.runVDI.toUpperCase() + "_" + scenario.getScenarioFeatureName() + "_" + CucumberUtils.getCurrentDateTime() + "_WARNING";
			File fScreenshotFile = new File(strScreenShotFileName + ".png");
			ImageIO.write(takeScreenshot(browser), "png", fScreenshotFile);

			CreationHelper createHelper = objWorkbook.getCreationHelper();
			// cell style for hyperlinks. by default hypelrinks are blue and
			// underlined
			CellStyle linkStyle = objWorkbook.createCellStyle();
			HSSFFont linkFont = objWorkbook.createFont();
			linkFont.setUnderline((byte) Font.BOLD);
			linkFont.setColor(IndexedColors.BLUE.getIndex());
			linkStyle.setFont(linkFont);

			// Sheet & cell details
			objSheet = objWorkbook.getSheet("SCENARIO_SUMMARY");
			objCell = objRow.createCell(4);
			objCell.setCellValue("WARNING_SCREENSHOT");

			Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
			link.setAddress(strScreenShotFileName + ".png");
			objCell.setHyperlink((org.apache.poi.ss.usermodel.Hyperlink) link);
			objCell.setCellStyle(linkStyle);

		} else if (step.getStepStatus().equalsIgnoreCase("FAIL") || step.getStepStatus().equals(FAIL)) {
			scenario.setScenarioEndDttm(new Date());
			cellFont.setColor(IndexedColors.RED.getIndex());
			cellStyle.setFont(cellFont);
			objCell = objRow.createCell(3);
			objCell.setCellValue("FAIL");
			objCell.setCellStyle(cellStyle);

			// 4. Description
			objCell = objRow.createCell(1);
			objCell.setCellValue(createMessegesString(step));

			// 5. Take Screenshot
			String strScreenShotFileName = System.getProperty("user.dir") + File.separator + EcommTestRunner.screenshotDirectory + File.separator + scenario.getScenarioNumber() + "_"
			+ EcommTestRunner.runVDI.toUpperCase() + "_" + scenario.getScenarioFeatureName() + "_" + CucumberUtils.getCurrentDateTime() + "_FAIL";
			File fScreenshotFile = new File(strScreenShotFileName + ".png");
			ImageIO.write(takeScreenshot(browser), "png", fScreenshotFile);

			CreationHelper createHelper = objWorkbook.getCreationHelper();
			// cell style for hyperlinks. by default hypelrinks are blue and
			// underlined
			CellStyle linkStyle = objWorkbook.createCellStyle();
			HSSFFont linkFont = objWorkbook.createFont();
			linkFont.setUnderline((byte) Font.BOLD);
			linkFont.setColor(IndexedColors.BLUE.getIndex());
			linkStyle.setFont(linkFont);

			// Sheet & cell details
			objSheet = objWorkbook.getSheet("SCENARIO_SUMMARY");
			objCell = objRow.createCell(4);
			objCell.setCellValue("FAIL_SCREENSHOT");

			Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
			link.setAddress(strScreenShotFileName + ".png");
			objCell.setHyperlink((org.apache.poi.ss.usermodel.Hyperlink) link);
			objCell.setCellStyle(linkStyle);

		}
		// All other steps would use the below without any cell formatting
		if (!step.isFirstStep() && !step.isFinalStep()) {
			// 6. Current Browser Name
			objCell = objRow.createCell(5);
			objCell.setCellValue(EcommTestRunner.currentBrowserName);

			// 7. Current Scenario URL
			objCell = objRow.createCell(6);
			objCell.setCellValue(scenario.getScenarioCurrentUrl());
		}

		// Report back the total execution time at the end of the last step per
		// scenario
		if (step.isFinalStep() || (step.getStepStatus().equalsIgnoreCase("FAIL") || step.getStepStatus().equals(FAIL))) {
			// Calculate the time difference
			long timeDifference = scenario.getScenarioEndDttm().getTime() - scenario.getScenarioCreateDttm().getTime();
			timeDifference /= 1000L;

			// Set the execution time back to the bean
			scenario.setScenarioExecutionTime((int) (long) timeDifference);
			String timeDifferenceDetailed = (new StringBuilder(String.valueOf(Long.toString(timeDifference / 60L)))).append(" MIN(S), ").append(Long.toString(timeDifference % 60L)).append(" SEC(S)")
			.toString();

			intRowToWrire = objSheet.getPhysicalNumberOfRows();
			cellStyle = objWorkbook.createCellStyle();
			cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			objRow = objSheet.createRow(intRowToWrire);
			cellStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellStyle.setFont(cellFont);
			objCell = objRow.createCell(2);
			objCell.setCellValue("EXECUTION DURATION");
			objCell.setCellStyle(cellStyle);
			objCell = objRow.createCell(3);
			objCell.setCellValue(timeDifferenceDetailed);
			objCell.setCellStyle(cellStyle);
		}

		objfileOutputStram = new FileOutputStream(scenario.getScenarioReportFile() + ".xls");
		objWorkbook.write(objfileOutputStram);
		objfileInputStream.close();
		objfileOutputStram.close();
	}

	/**
	 *********************************************************************** 
	 * @Purpose - Update the overall sheet with scenario status
	 * @author - Ramgopal Narayanan
	 * @Created - 14 JAN 2013
	 * @ModifiedBy -
	 * @ModifiedDate -
	 *********************************************************************** 
	 */
	public static synchronized void updateScearnioStatus(EventFiringWebDriver browser, ScenarioBean scenario, StepBean step) throws Exception {

		StringBuilder totalFailScenarios=new StringBuilder();
		objfileInputStream = new FileInputStream(EcommTestRunner.overAllSummaryFile);
		objWorkbook = new HSSFWorkbook(objfileInputStream);

		// Update based on the overall scenario status
		if (scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("PASS")) {
			objSheet = objWorkbook.getSheet("PASS_SCENARIOS");
			EcommTestRunner.passCounter++;
		} else if (scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("FAIL")) {
			objSheet = objWorkbook.getSheet("FAIL_SCENARIOS");
			EcommTestRunner.failuresCounter++;
		} else if (scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("DISABLED")) {
			objSheet = objWorkbook.getSheet("DISABLED_SCENARIOS");
			EcommTestRunner.disabledCouter++;
		}
		int intRowToWrire = objSheet.getPhysicalNumberOfRows();

		// A. Cell Style
		CellStyle cellStyle = objWorkbook.createCellStyle();
		HSSFFont cellFont = objWorkbook.createFont();
		cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);

		// B. Write the result
		objRow = objSheet.createRow(intRowToWrire);

		// 1. SCENARIO ID
		objCell = objRow.createCell(0);
		objCell.setCellValue(scenario.getScenarioNumber());

		// 2. MODULE OR QC MODULE
		objCell = objRow.createCell(2);
		objCell.setCellValue(scenario.getScenarioModuleName().toUpperCase());

		// 3. FEATURE FILE NAME
		objCell = objRow.createCell(3);
		objCell.setCellValue(scenario.getScenarioFeatureName());

		// 4. BROWSER
		if (!scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("DISABLED")) {
			objCell = objRow.createCell(5);
			objCell.setCellValue(scenario.getScenarioBrowserName().toUpperCase());
		}
		// 5. SCENARIO STATUS
		if (scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("PASS")) {

			cellFont.setColor(IndexedColors.GREEN.getIndex());
			cellStyle.setFont(cellFont);
			objCell = objRow.createCell(1);
			objCell.setCellValue("PASS");
			objCell.setCellStyle(cellStyle);
		} else if (scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("FAIL")) {

			cellFont.setColor(IndexedColors.RED.getIndex());
			cellStyle.setFont(cellFont);
			objCell = objRow.createCell(1);
			objCell.setCellValue("FAIL");
			objCell.setCellStyle(cellStyle);
			totalFailScenarios.append("@" + scenario.getScenarioNumber() + ",");
		} else if (scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("DISABLED")) {
			cellFont.setColor(IndexedColors.GREY_50_PERCENT.getIndex());
			cellStyle.setFont(cellFont);
			objCell = objRow.createCell(1);
			objCell.setCellValue("DISABLED");
			objCell.setCellStyle(cellStyle);
		}

		// 6. EXECUTION DURATION
		String timeDifferenceDetailed = null;
		if (!scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("DISABLED")) {

			timeDifferenceDetailed = (new StringBuilder(String.valueOf(Long.toString(scenario.getScenarioExecutionTime() / 60L)))).append(" MIN(S), ")
			.append(Long.toString(scenario.getScenarioExecutionTime() % 60L)).append(" SEC(S)").toString();
			objCell = objRow.createCell(4);

			objCell.setCellValue(timeDifferenceDetailed);
		}


		// 7. DETAILED RESULTS

		if (!scenario.getScenarioOverAllReportingStatus().equalsIgnoreCase("DISABLED")) {
			String strScenarioResultsSheetFullPath = System.getProperty("user.dir") + File.separator + scenario.getScenarioReportFile() + ".xls";

			CreationHelper createHelper = objWorkbook.getCreationHelper();
			// cell style for hyperlinks. by default hypelrinks are blue and
			// underlined
			CellStyle linkStyle = objWorkbook.createCellStyle();
			HSSFFont linkFont = objWorkbook.createFont();
			linkFont.setUnderline((byte) Font.BOLD);
			linkFont.setColor(IndexedColors.BLUE.getIndex());
			linkStyle.setFont(linkFont);

			objCell = objRow.createCell(6);
			objCell.setCellValue("CLICK HERE");

			Hyperlink link = createHelper.createHyperlink(Hyperlink.LINK_FILE);
			link.setAddress(strScenarioResultsSheetFullPath);
			objCell.setHyperlink((org.apache.poi.ss.usermodel.Hyperlink) link);
			objCell.setCellStyle(linkStyle);
		}

		objfileOutputStram = new FileOutputStream(EcommTestRunner.overAllSummaryFile);
		objWorkbook.write(objfileOutputStram);
		objfileInputStream.close();
		objfileOutputStram.close();

		EcommTestRunner.totalReexecuteFailScenarios+=totalFailScenarios.toString();
	}

	/**
	 *********************************************************************** 
	 * @Purpose - Creates the over all run summary dashboard
	 * @author - Ramgopal Narayanan
	 * @Created - 18 JAN 2013
	 * @ModifiedBy -
	 * @ModifiedDate -
	 *********************************************************************** 
	 */
	public static synchronized void createScenarioSummaryDashboard(String strExcelFileName) throws Exception {
		try {

			objfileInputStream = new FileInputStream(strExcelFileName);
			objWorkbook = new HSSFWorkbook(objfileInputStream);

			// 2. Cell Style
			CellStyle cellStyle = objWorkbook.createCellStyle();
			HSSFFont cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellFont.setColor(IndexedColors.BLACK.getIndex());
			cellStyle.setFont(cellFont);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			// 3. Dashboard Values
			objSheet = objWorkbook.getSheet("SUMMARY_DASHBOARD");

			// a. Over All Run Summary Header
			cellStyle = objWorkbook.createCellStyle();
			cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setFillForegroundColor(IndexedColors.PALE_BLUE.getIndex());
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellFont.setColor(IndexedColors.BLACK.getIndex());
			cellStyle.setFont(cellFont);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			objRow = objSheet.createRow(1);
			objCell = objRow.createCell(1);
			objCell.setCellValue("OVERALL RUN SUMMARY");
			objCell.setCellStyle(cellStyle);

			// b. Total Scenarios
			cellStyle = objWorkbook.createCellStyle();
			cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setFillForegroundColor(IndexedColors.YELLOW.getIndex());
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellFont.setColor(IndexedColors.BLACK.getIndex());
			cellStyle.setFont(cellFont);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			objRow = objSheet.createRow(2);
			objCell = objRow.createCell(1);
			objCell.setCellValue("TOTAL SCENARIOS");
			objCell.setCellStyle(cellStyle);

			cellStyle = objWorkbook.createCellStyle();
			cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			cellFont.setColor(IndexedColors.BLACK.getIndex());
			cellStyle.setFont(cellFont);

			objCell = objRow.createCell(2);
			objCell.setCellValue(Integer.toString(EcommTestRunner.passCounter + EcommTestRunner.failuresCounter + EcommTestRunner.disabledCouter));
			objCell.setCellStyle(cellStyle);

			// c. Pass Scenarios
			cellStyle = objWorkbook.createCellStyle();
			cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setFillForegroundColor(IndexedColors.GREEN.getIndex());
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellFont.setColor(IndexedColors.BLACK.getIndex());
			cellStyle.setFont(cellFont);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			objRow = objSheet.createRow(3);
			objCell = objRow.createCell(1);
			objCell.setCellValue("PASS");
			objCell.setCellStyle(cellStyle);

			cellStyle = objWorkbook.createCellStyle();
			cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			cellFont.setUnderline((byte) Font.BOLD);
			cellFont.setColor(IndexedColors.BLUE.getIndex());
			cellStyle.setFont(cellFont);

			objCell = objRow.createCell(2);
			objCell.setCellValue(Integer.toString(EcommTestRunner.passCounter));

			CreationHelper createHelper1 = objWorkbook.getCreationHelper();
			Hyperlink link1 = createHelper1.createHyperlink(Hyperlink.LINK_DOCUMENT);
			link1.setAddress("'PASS_SCENARIOS'!A1");
			objCell.setHyperlink((org.apache.poi.ss.usermodel.Hyperlink) link1);
			objCell.setCellStyle(cellStyle);

			// d. Fail Scenarios
			cellStyle = objWorkbook.createCellStyle();
			cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setFillForegroundColor(IndexedColors.RED.getIndex());
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellFont.setColor(IndexedColors.BLACK.getIndex());
			cellStyle.setFont(cellFont);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			objRow = objSheet.createRow(4);
			objCell = objRow.createCell(1);
			objCell.setCellValue("FAIL");
			objCell.setCellStyle(cellStyle);

			cellStyle = objWorkbook.createCellStyle();
			cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			cellFont.setUnderline((byte) Font.BOLD);
			cellFont.setColor(IndexedColors.BLUE.getIndex());
			cellStyle.setFont(cellFont);

			objCell = objRow.createCell(2);
			objCell.setCellValue(Integer.toString(EcommTestRunner.failuresCounter));

			CreationHelper createHelper2 = objWorkbook.getCreationHelper();
			Hyperlink link2 = createHelper2.createHyperlink(Hyperlink.LINK_DOCUMENT);
			link2.setAddress("'FAIL_SCENARIOS'!A1");
			objCell.setHyperlink((org.apache.poi.ss.usermodel.Hyperlink) link2);
			objCell.setCellStyle(cellStyle);

			// e. Disabled Scenarios
			cellStyle = objWorkbook.createCellStyle();
			cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setFillForegroundColor(IndexedColors.GREY_25_PERCENT.getIndex());
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellFont.setColor(IndexedColors.BLACK.getIndex());
			cellStyle.setFont(cellFont);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			objRow = objSheet.createRow(5);
			objCell = objRow.createCell(1);
			objCell.setCellValue("DISABLED / NO RUN");
			objCell.setCellStyle(cellStyle);

			cellStyle = objWorkbook.createCellStyle();
			cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);
			cellFont.setUnderline((byte) Font.BOLD);
			cellFont.setColor(IndexedColors.BLUE.getIndex());
			cellStyle.setFont(cellFont);

			objCell = objRow.createCell(2);
			objCell.setCellValue(Integer.toString(EcommTestRunner.disabledCouter));

			CreationHelper createHelper3 = objWorkbook.getCreationHelper();
			Hyperlink link3 = createHelper3.createHyperlink(Hyperlink.LINK_DOCUMENT);
			link3.setAddress("'DISABLED_SCENARIOS'!A1");
			objCell.setHyperlink((org.apache.poi.ss.usermodel.Hyperlink) link3);
			objCell.setCellStyle(cellStyle);

			// f. Total Execution Time
			cellStyle = objWorkbook.createCellStyle();
			cellFont = objWorkbook.createFont();
			cellFont.setBoldweight(HSSFFont.BOLDWEIGHT_BOLD);
			cellStyle.setFillForegroundColor(IndexedColors.BRIGHT_GREEN.getIndex());
			cellStyle.setFillPattern(CellStyle.SOLID_FOREGROUND);
			cellFont.setColor(IndexedColors.BLACK.getIndex());
			cellStyle.setFont(cellFont);
			cellStyle.setBorderBottom(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderTop(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderRight(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setBorderLeft(HSSFCellStyle.BORDER_MEDIUM);
			cellStyle.setAlignment(HSSFCellStyle.ALIGN_CENTER);

			objRow = objSheet.createRow(6);
			objCell = objRow.createCell(1);
			objCell.setCellValue("TOTAL EXECUTION TIME");
			objCell.setCellStyle(cellStyle);

			String strTotalExecTime = EcommTestRunner.totalExecutionTime / 3600 + " HOUR(S) " + (EcommTestRunner.totalExecutionTime % 3600) / 60 + " MIN(S)," + EcommTestRunner.totalExecutionTime % 60
			+ " SEC(S)";

			objCell = objRow.createCell(2);
			objCell.setCellValue(strTotalExecTime);
			objCell.setCellStyle(cellStyle);

			objfileOutputStram = new FileOutputStream(strExcelFileName);
			objWorkbook.write(objfileOutputStram);
			objfileInputStream.close();
			objfileOutputStram.close();

		} catch (Exception e) {
			LogIt(e, null, null);
		}

	}

	/**
	 *********************************************************************** 
	 * @Purpose - Update the results back to ALM post execution
	 * @author - Ramgopal Narayanan
	 * @Created - 18 JAN 2013
	 * @ModifiedBy -
	 * @ModifiedDate -
	 *********************************************************************** 
	 */
	public static synchronized void updateALMStatus(String strExcelFileName) throws Exception {
		try {

			ScenarioBean scenarioBeanMap = null;
			String ALLSCENARIODETAILS = null;
			objfileInputStream = new FileInputStream(strExcelFileName);
			objWorkbook = new HSSFWorkbook(objfileInputStream);

			// PASS SCENARIOS
			objSheet = objWorkbook.getSheet("PASS_SCENARIOS");
			int intSheetTotalRows = objSheet.getPhysicalNumberOfRows();
			if (intSheetTotalRows > 1) {
				for (int rowCnt = 1; rowCnt <= intSheetTotalRows - 1; rowCnt++) {

					objRow = objSheet.getRow(rowCnt);

					objCell = objRow.getCell(0);
					String SCENARIOID = objCell.getStringCellValue();

					scenarioBeanMap = EcommTestRunner.registeredScenariosMap.get(SCENARIOID);
					if (scenarioBeanMap.isALMReportEnabled()) {
						objCell = objRow.getCell(3);
						String FEATUREFILE = objCell.getStringCellValue();
						objCell = objRow.getCell(1);
						String SCENARIOSTATUS = objCell.getStringCellValue();
						HSSFHyperlink objHyperLink;
						objCell = objRow.getCell(6);
						objHyperLink = objCell.getHyperlink();
						String SCEANRIOFILE = objHyperLink.getAddress();

						String INDIVSCENARIODETAILS = SCENARIOID.trim() + "," + FEATUREFILE.trim() + "," + SCENARIOSTATUS.trim() + "," + scenarioBeanMap.getScenarioExecutionTime() + ","
						+ SCEANRIOFILE.trim();

						ALLSCENARIODETAILS = ALLSCENARIODETAILS + "~" + INDIVSCENARIODETAILS;
					}
				}
			}
			// FAIL SCENARIOS
			objSheet = objWorkbook.getSheet("FAIL_SCENARIOS");
			intSheetTotalRows = objSheet.getPhysicalNumberOfRows();

			if (intSheetTotalRows > 1) {
				for (int rowCnt = 1; rowCnt <= intSheetTotalRows - 1; rowCnt++) {

					objRow = objSheet.getRow(rowCnt);

					objCell = objRow.getCell(0);
					String SCENARIOID = objCell.getStringCellValue();

					scenarioBeanMap = EcommTestRunner.registeredScenariosMap.get(SCENARIOID);

					if (scenarioBeanMap.isALMReportEnabled()) {
						objCell = objRow.getCell(3);
						String FEATUREFILE = objCell.getStringCellValue();
						objCell = objRow.getCell(1);
						String SCENARIOSTATUS = objCell.getStringCellValue();
						HSSFHyperlink objHyperLink;
						objCell = objRow.getCell(6);
						objHyperLink = objCell.getHyperlink();
						String SCEANRIOFILE = objHyperLink.getAddress();

						String INDIVSCENARIODETAILS = SCENARIOID.trim() + "," + FEATUREFILE.trim() + "," + SCENARIOSTATUS.trim() + "," + scenarioBeanMap.getScenarioExecutionTime() + ","
						+ SCEANRIOFILE.trim();

						ALLSCENARIODETAILS = ALLSCENARIODETAILS + "~" + INDIVSCENARIODETAILS;
					}

				}
			}

			objfileInputStream.close();
			if (StringUtils.isNotEmpty(ALLSCENARIODETAILS) && StringUtils.isNotBlank(ALLSCENARIODETAILS)) {
				ALLSCENARIODETAILS = ALLSCENARIODETAILS.substring(5, ALLSCENARIODETAILS.length());
				// Start the ALM updating
				System.out.println("===========================================================================================================");
				System.out.println("ALM UPDATE STARTED");
				Process p = Runtime.getRuntime().exec(
						"cmd /c start/wait " + "cscript " + System.getProperty("user.dir") + "/Extensions/ALMUpdate.vbs" + " " + ALLSCENARIODETAILS + " " + System.getProperty("user.dir")
						+ File.separator + EcommTestRunner.screenshotDirectory);

				final int exitVal = p.waitFor();
				if (exitVal == 0) {
					// Send the execution completion email
					if (EcommTestRunner.config.getString("ALMExecutionEmail").equalsIgnoreCase("YES"))
						p = Runtime.getRuntime().exec(
								"cmd /c start/wait " + "cscript " + System.getProperty("user.dir") + "/Extensions/ALMExecutionMail.vbs" + " " + System.getProperty("user.dir") + File.separator
								+ strExcelFileName);
					p.waitFor();
				}
				System.out.println("ALM UPDATE COMPELTED");
				System.out.println("===========================================================================================================");
			}

		} catch (Exception e) {
			LogIt(e, null, null);
		}

	}

}
