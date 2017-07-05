package com.mistong.base;

import java.io.IOException;

import org.apache.log4j.Logger;
import org.testng.Assert;
import org.testng.ITestContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.AfterTest;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.BeforeTest;

import com.mistong.util.LogConfiguration;
import com.mistong.util.PropertiesDataProvider;
import com.mistong.util.SeleniumUtil;


public class BaseParpare {
	static Logger logger = Logger.getLogger(BaseParpare.class.getName());// 使用Log4j，获取日志记录器，负责记录日志信息
	protected SeleniumUtil seleniumUtil = null;
	protected static String driverConfgFilePath = "config/driver.properties";// 配置文件地址
	protected static String moduleName; // 模块的名字
	protected static String functionName; // 功能的名字
	protected static String caseName; // 用例编号
	protected static String webUrl = "";// 网页地址
	protected static String browserName;// 浏览器名称
	protected static int timeOut = 0;// 页面元素超时设置(秒)
	protected static String system;// 测试系统类型
	protected ITestContext testContext = null;// 添加成员变量来获取beforeClass传入的context参数

	/**
	 * 
	 * @Description: 测试前准备工作
	 * @param
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-10-26
	 */
	@BeforeTest
	public void beforeTest() {
		//用于动态生成各个模块中的每条用例的日志
		LogConfiguration.initLog(getModuleName(), this.getClass()
				.getSimpleName());
		logger.info("====================================UI测试开始====================================");
	}

	@BeforeClass
	/**启动浏览器并打开测试页面*/
	public void startTest(ITestContext context) {
		system = PropertiesDataProvider.getTestData(driverConfgFilePath,
				"system");
		// 这里得到context值
		this.testContext = context;
		seleniumUtil = new SeleniumUtil();
		switch (system) {
		case "web":
			// 从属性文件中获取浏览器的属性值
			browserName = PropertiesDataProvider.getTestData(
					driverConfgFilePath, "browserName");
			timeOut = Integer.valueOf(PropertiesDataProvider.getTestData(
					driverConfgFilePath, "timeOut"));
			webUrl = PropertiesDataProvider.getTestData(driverConfgFilePath,
					"testurl");
			try {
				// 启动浏览器launchBrowser方法可以自己看看，主要是打开浏览器，输入测试地址，并最大化窗口
				seleniumUtil.launchBrowser(browserName, webUrl, timeOut);
			} catch (Exception e) {
				logger.error("浏览器不能正常工作，请检查是不是被手动关闭或者其他原因", e);
			}
			break;
		case "android":
			//使用命令行方式启动运行appium
			try {
				Runtime.getRuntime().exec("cmd /k start appium");
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			try {
				Thread.sleep(10000);
			} catch (InterruptedException e1) {
				e1.printStackTrace();
			}
			seleniumUtil.launchAndroidDriver();
			break;
		case "ios":

		default:
			break;
		}
		moduleName = getModuleName();
		functionName = getFunctionName();
		caseName = getCaseName();
		// 设置一个testng上下文属性，将webDriver存起来，之后可以使用context随时取到
		testContext.setAttribute("webDriver", seleniumUtil.driver);
		testContext.setAttribute("moduleName", moduleName);
		testContext.setAttribute("functionName", functionName);
		testContext.setAttribute("CaseName", caseName);
		logger.info(moduleName + "模块" + functionName + "功能" + caseName
				+ "用例的UI测试开始");
	}

	/**
	 * 
	 * @Description: UI测试完成后需要做的相关工作
	 * @param
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-10-26
	 */
	@AfterClass
	public void endTest() {
		system = PropertiesDataProvider.getTestData(driverConfgFilePath,
				"system");
		switch (system) {
		case "web":
			if (seleniumUtil.driver != null) {
				seleniumUtil.quit();
				break;
			} else {
				logger.error("浏览器driver没有获得对象,退出操作失败");
				Assert.fail("浏览器driver没有获得对象,退出操作失败");
			}
		case "android":
			if (seleniumUtil.androidDriver != null) {
				seleniumUtil.quit();
				break;
			} else {
				logger.error("driver没有获得对象,退出操作失败");
				Assert.fail("driver没有获得对象,退出操作失败");
			}
		case "ios":
			
		default:
			break;
		}
		logger.info(moduleName + "模块" + functionName + "功能" + caseName
				+ "用例的UI测试完毕");
	}

	/**
	 * 
	 * @Description: UI测试完成后需要做的相关工作,结束appium需先结束node.exe进程、再结束cmd.exe进程
	 * @param 无
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-10-26
	 */
	@AfterTest
	public void AfterTest() {
		try {
			Runtime.getRuntime().exec("taskkill /F /IM node.exe");
			Runtime.getRuntime().exec("taskkill /F /IM cmd.exe");
		} catch (IOException e) {
			e.printStackTrace();
		}
		logger.info("====================================UI测试完毕====================================");
	}
	

	/**
	 * 
	 * @Description: 获取模块名
	 * @param @return
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-10-26
	 */
	public String getModuleName() {
		String className = this.getClass().getName();
		String moduleName, startStr;
		int lastDotIndexNum, secondLastDotIndexNum;
		startStr = "testcases.";
		lastDotIndexNum = className.lastIndexOf("."); // 取得最后一个.的index
		secondLastDotIndexNum = className.indexOf(startStr) + startStr.length();
		moduleName = className
				.substring(secondLastDotIndexNum, lastDotIndexNum); // 取到模块的名称
		return moduleName;
	}

	/**
	 * 
	 * @Description: 获取功能名
	 * @param @return
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-10-26
	 */
	public String getFunctionName() {
		String className = this.getClass().getName();
		int underlineIndexNum = className.indexOf("_"); // 取得第一个_的index
		if (underlineIndexNum > 0) {
			functionName = className.substring(className.lastIndexOf(".") + 1,
					underlineIndexNum); // 取到模块的名称
		}
		return functionName;
	}

	/**
	 * 
	 * @Description: 获取用例编号
	 * @param @return
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-10-26
	 */
	public String getCaseName() {
		String className = this.getClass().getName();
		int firstUnderLine = className.indexOf("_"); // 取得第一个_的index
		int lastUnderLine = className.lastIndexOf("_"); // 取得最后一个_的index
		if (firstUnderLine > 0) {
			caseName = className.substring(firstUnderLine + 1, lastUnderLine); // 取到用例编号
		}
		return caseName;
	}

}
