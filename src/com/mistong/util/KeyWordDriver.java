package com.mistong.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Set;

import org.apache.log4j.Logger;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.testng.Assert;
import org.testng.ITestContext;

/**
 * 
 * ClassName: KeyWordDriver
 * 
 * @Description: 该类实现关键字驱动
 * @author 吴丁飞
 * @date 2016-11-6
 */
public class KeyWordDriver {
	public static Logger logger = Logger.getLogger(KeyWordDriver.class
			.getName());
	static String fileDir = "res/page/";
	public static Alert a = null;

	protected static String moduleName; // 模块的名字
	protected static String caseName; // 用例编号
	protected static String excelName;// excel文件名称
	
	public String current_handles = "";
	public WebDriver window = null;

	/**
	 * 
	 * @Description: 根据不同的元素定位的方式元素定位的属性值来获取by对象，以便driver调用by对象进行元素定位
	 * @param @param locateWay
	 *        元素定位的方式，有八种，分别为：id、name、className、tagname、linktext
	 *        、partialLinkText、xpath、css
	 * @param @param locateValue 元素定位的属性值
	 * @param @return
	 * @return By 返回by对象
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public By getLocateWay(String locateWay, String locateValue) {
		By elementLocator = null;
		switch (locateWay) {
		case "id":
			elementLocator = By.id(locateValue);
			break;
		case "name":
			elementLocator = By.name(locateValue);
			break;
		case "className":
			elementLocator = By.className(locateValue);
			break;
		case "tagName":
			elementLocator = By.tagName(locateValue);
			break;
		case "linkText":
			elementLocator = By.linkText(locateValue);
			break;
		case "partialLinkText":
			elementLocator = By.partialLinkText(locateValue);
			break;
		case "xpath":
			elementLocator = By.xpath(locateValue);
			break;
		case "css":
			elementLocator = By.cssSelector(locateValue);
			break;
		default:
			Assert.fail("你选择的定位方式：【" + locateWay + "】不支持,请使用支持的定位方式!");
			break;
		}
		return elementLocator;
	}

	/**
	 * 
	 * @Description: 根据testcase中的元素定位列，去取得page页中的 定位方式和定位值
	 * @param @param sheet 测试用例表中的sheet
	 * @param @param rowIndex 测试用例表中的行index
	 * @param @param columnIndex 测试用例表中的定位列的index
	 * @param @param pageName
	 * @param @return
	 * @return String[] 从page表中返回定位方式和定位值的字符数组
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	@SuppressWarnings("resource")
	public String[] getElementLocator(Sheet sheet, int rowIndex,
			int columnIndex, String pageName) {

		XSSFWorkbook workBook = null;
		// 定位方式
		String elementLocatorWay = null;
		// 定位值
		String elementLocatorValue = null;
		// sheet表
		Sheet workSheet = null;
		// page excel路径
		String excelFilePath = fileDir + pageName + ".xlsx";
		// 获取定位列的值
		String locator = sheet.getRow(rowIndex).getCell(columnIndex)
				.getStringCellValue();
		// 用.分割开元素定位值
		String locatorSplit[] = locator.split("\\.");
		try {
			workBook = new XSSFWorkbook(new FileInputStream(new File(
					excelFilePath)));
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		workSheet = workBook.getSheetAt(0); // 取得第一个sheet
		int rowNum = workSheet.getPhysicalNumberOfRows();// 获得这个sheet的实际有效行数                                       
		for (int i = 0; i < rowNum; i++) {
			// 如果获取到的别名和指定的别名相同，就存储当前行的定位值和定位方式
			if (workSheet.getRow(i).getCell(0).getStringCellValue()
					.equalsIgnoreCase(locatorSplit[1])) {
				elementLocatorWay = workSheet.getRow(i).getCell(1)
						.getStringCellValue();
				elementLocatorValue = workSheet.getRow(i).getCell(2)
						.getStringCellValue();
				break;
			}
		}
		return new String[] { elementLocatorWay, elementLocatorValue };

	}

	/**
	 * 
	 * @Description: 读取excel中每个sheet的操作步骤，进而生成测试用例
	 * @param @param moduleName 模块名
	 * @param @param excelName excel文件的名字
	 * @param @param caseName excel中sheet的名字
	 * @param @param seleniumUtil 引用seleniumUtil
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void runScript(SeleniumUtil seleniumUtil, ITestContext context) {
		moduleName = (String) context.getAttribute("moduleName");
		excelName = (String) context.getAttribute("functionName");
		caseName = (String) context.getAttribute("CaseName");
		FileInputStream inputStream = null;
		XSSFWorkbook workBook = null;
		String locateSplit[] = null;// 页面sheet中的定位方式和定位值拆解
		String locator = null;// 用例页面的定位列
		String[] Datas;
		String[] subDatas;
		String file = "res/testcase/" + excelName + ".xlsx";
		try {
			inputStream = new FileInputStream(file);// 读取功能模块
		} catch (FileNotFoundException e) {
			logger.error("文件：" + file + "不存在");
			Assert.fail("文件：" + file + "不存在");
		}
		try {
			workBook = new XSSFWorkbook(inputStream);
		} catch (IOException e) {
			logger.error("IO异常");
			Assert.fail("IO异常");
		}
		/** 取得指定的case名字 */
		Sheet sheet = workBook.getSheet(caseName);
		/** 获得的实际行数 */
		int rows = sheet.getPhysicalNumberOfRows();
		/** excel中的测试数据 */
		String testData = null;
		// 获取首行的单元格数
		int cellsNumInOneRow = sheet.getRow(0).getPhysicalNumberOfCells();
		// 声明一个数组存储列值的角标
		String column[] = new String[cellsNumInOneRow];
		// 声明一个迭代器
		Iterator<Cell> cell = sheet.getRow(0).iterator();
		int ii = 0;
		while (cell.hasNext()) {
			column[ii] = String.valueOf(cell.next());
			ii++;
		}
		// 定义动作列的角标
		int actionColumnIndex = 0;
		// 定义元素定位列的角标
		int locateColumnIndex = 0;
		// 定义测试数据列的角标
		int testDataColumnIndex = 0;
		// 动态获取这几个关键列所在位置
		for (int i = 0; i < column.length; i++) {
			if (column[i].equals("动作")) {
				actionColumnIndex = i;
			}
			if (column[i].equals("元素定位")) {
				locateColumnIndex = i;
			}
			if (column[i].equals("测试数据")) {
				testDataColumnIndex = i;
			}

		}

		// 循环每行的操作，根据switch来判断每行的操作是什么，然后转换成具体的代码，从第二行开始循环，因为第一行是列的说明数据。
		for (int i = 1; i < rows; i++) {
			logger.info("正在解析执行" + moduleName + "模块的excel:[" + excelName
					+ ".xlsx]中的sheet(用例)：[" + caseName + "]的第" + i + "行步骤...");
			String action = sheet.getRow(i).getCell(actionColumnIndex)
					.getStringCellValue();
			Row row = sheet.getRow(i);
			if (row != null) {
				switch (action) {
				case "导航链接":
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					seleniumUtil.get(testData);
					break;

				case "点击":
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					seleniumUtil.click(getLocateWay(locateSplit[0],
							locateSplit[1]));
					break;

				case "输入":
					// 先设置Cell的类型，然后就可以把纯数字作为String类型读进来了
					sheet.getRow(i).getCell(testDataColumnIndex)
							.setCellType(Cell.CELL_TYPE_STRING);
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue(); // 测试数据
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的元素定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]); // 找到定位方式、定位值
					seleniumUtil.type(
							getLocateWay(locateSplit[0], locateSplit[1]),
							testData);
					break;

				case "清除":
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					seleniumUtil.clear(getLocateWay(locateSplit[0],
							locateSplit[1]));
					break;

				case "暂停":
					// 先设置Cell的类型，然后就可以把纯数字作为String类型读进来了
					sheet.getRow(i).getCell(testDataColumnIndex)
							.setCellType(Cell.CELL_TYPE_STRING);
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					seleniumUtil.pause(Integer.parseInt(testData));
					break;

				case "等待元素":
					// 先设置Cell的类型，然后就可以把纯数字作为String类型读进来了
					sheet.getRow(i).getCell(testDataColumnIndex)
							.setCellType(Cell.CELL_TYPE_STRING);
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					seleniumUtil.waitForElementToLoad(
							getLocateWay(locateSplit[0], locateSplit[1]),
							Integer.parseInt(testData));
					break;

				case "选择下拉列表--Text":
					// 先设置Cell的类型，然后就可以把纯数字作为String类型读进来了
					sheet.getRow(i).getCell(testDataColumnIndex)
							.setCellType(Cell.CELL_TYPE_STRING);
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					seleniumUtil.selectByText(
							getLocateWay(locateSplit[0], locateSplit[1]),
							testData);
					break;

				case "选择下拉列表--Index":
					// 先设置Cell的类型，然后就可以把纯数字作为String类型读进来了
					sheet.getRow(i).getCell(testDataColumnIndex)
							.setCellType(Cell.CELL_TYPE_STRING);
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					seleniumUtil.selectByIndex(
							getLocateWay(locateSplit[0], locateSplit[1]),
							Integer.parseInt(testData));
					break;

				case "选择下拉列表--Value":
					// 先设置Cell的类型，然后就可以把纯数字作为String类型读进来了
					sheet.getRow(i).getCell(testDataColumnIndex)
							.setCellType(Cell.CELL_TYPE_STRING);
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					seleniumUtil.selectByValue(
							getLocateWay(locateSplit[0], locateSplit[1]),
							testData);
					break;

				case "检查文本":
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					seleniumUtil.verify(seleniumUtil.getText(getLocateWay(
							locateSplit[0], locateSplit[1])), testData);
					break;

				case "检查文本--属性":
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					Datas = testData.split(",");
					seleniumUtil.verify(seleniumUtil.getAttributeText(
							getLocateWay(locateSplit[0], locateSplit[1]),
							Datas[0]), Datas[1]);
					break;

				case "检查网页标题":
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					seleniumUtil.verify(seleniumUtil.getTitle(), testData);
					break;

				case "检查页面的URL是否正确":
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					seleniumUtil.verify(seleniumUtil.getPageURL(), testData);
					break;

				case "数据创建或销毁":
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					Datas = testData.split(";");
					logger.info("正在执行数据初始化、数据销毁操作。。。。。。。");
					for (int j = 0; j < Datas.length; j++) {
						subDatas = Datas[j].split(":");
						logger.info("正在执行sql:"+subDatas[1]);
						DBHelper.executeNonQuery(subDatas[0].replace("\n", ""), subDatas[1]);
					}
					break;

				case "数据库检查":
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					Datas = testData.split(":");
					ResultSet rs = DBHelper.executeQuery(Datas[0], Datas[1]);
					String sqlData = null;
					try {
						while (rs.next()) {
							sqlData = rs.getString(1);
							logger.info("执行SQL查询操作,查询结果为：" + sqlData);
						}
					} catch (SQLException e) {
						e.printStackTrace();
					}
					DBHelper.free(rs);
					seleniumUtil.verify(seleniumUtil.getText(getLocateWay(
							locateSplit[0], locateSplit[1])), sqlData);
					break;

				case "进入iFrame":
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					seleniumUtil.switchFrame(getLocateWay(locateSplit[0],
							locateSplit[1]));
					break;

				case "跳出iFrame":
					seleniumUtil.outFrame();
					break;

				case "进入Tab":
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					seleniumUtil.switchNewWindow(getLocateWay(locateSplit[0],
							locateSplit[1]));
					break;

				case "跳出Tab":
					seleniumUtil.backToOriginalWindow();
					break;
				
				case "进入新窗口":
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					Set<String> all_handles;
					Iterator<String> it;
					// 获取当前页面句柄
					current_handles = seleniumUtil.driver.getWindowHandle();
					// 点击某个链接会弹出一个新窗口
					seleniumUtil.click(getLocateWay(locateSplit[0],
							locateSplit[1]));
					// 接下来会有新的窗口打开，获取所有窗口句柄
					all_handles = seleniumUtil.driver.getWindowHandles();
					// 循环判断，把当前句柄从所有句柄中移除，剩下的就是你想要的新窗口
					it = all_handles.iterator();
					while (it.hasNext()) {
						if (current_handles == it.next())
							continue;
						// 跳入新窗口,并获得新窗口的driver - newWindow
						logger.info("点击【" + getLocateWay(locateSplit[0],
								locateSplit[1]) + "】对象，并进入新窗口。");
						window = seleniumUtil.driver.switchTo().window(it.next());
					}
					break;

				case "返回原窗口":
					window.close();
					switch (seleniumUtil.getSystem()) {
					case "web":
						logger.info("返回到原始窗口");
						seleniumUtil.driver.switchTo().window(current_handles);
						break;
					case "android":
						logger.info("返回到原始窗口");
						seleniumUtil.androidDriver.switchTo().window(current_handles);
						break;
					default:
						break;
					}
					break;

				case "接受alert弹窗":
					// 先设置Cell的类型，然后就可以把纯数字作为String类型读进来了
					sheet.getRow(i).getCell(testDataColumnIndex)
							.setCellType(Cell.CELL_TYPE_STRING);
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					a = seleniumUtil.switchToPromptedAlertAfterWait(Long
							.parseLong(testData));
					a.accept();
					break;

				case "取消alert弹窗":
					// 先设置Cell的类型，然后就可以把纯数字作为String类型读进来了
					sheet.getRow(i).getCell(testDataColumnIndex)
							.setCellType(Cell.CELL_TYPE_STRING);
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					a = seleniumUtil.switchToPromptedAlertAfterWait(Long
							.parseLong(testData));
					a.dismiss();
					break;

				case "执行JS点击":
					locator = sheet.getRow(i).getCell(locateColumnIndex)
							.getStringCellValue();// 获取步骤中的定位
					locateSplit = getElementLocator(sheet, i,
							locateColumnIndex, locator.split("\\.")[0]);
					seleniumUtil.executeJS("arguments[0].click();",
							seleniumUtil.findElementBy(getLocateWay(
									locateSplit[0], locateSplit[1])));
					break;

				case "刷新页面":
					seleniumUtil.refresh();
					break;

				case "前进页面":
					seleniumUtil.back();
					break;

				case "后退页面":
					seleniumUtil.forward();
					break;

				case "上传文件":
					testData = sheet.getRow(i).getCell(testDataColumnIndex)
							.getStringCellValue();
					String uploadValues[] = testData.split(",");
					seleniumUtil.handleUpload(uploadValues[0], new File(
							uploadValues[1]));
					break;

				default:
					logger.error("你输入的操作：[" + action + "]不被支持，请自行添加");
					Assert.fail("你输入的操作：[" + action + "]不被支持，请自行添加");

				}
			}
		}
	}

}
