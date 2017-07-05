package com.mistong.util;

import io.appium.java_client.MobileElement;
import io.appium.java_client.android.AndroidDriver;
import io.appium.java_client.android.AndroidElement;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.apache.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.NoAlertPresentException;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.ie.InternetExplorerDriver;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.remote.DesiredCapabilities;
import org.openqa.selenium.support.ui.ExpectedCondition;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;

/**
 * 
 * ClassName: SeleniumUtil
 * 
 * @Description: 包装所有selenium的操作以及通用方法
 * @author 吴丁飞
 * @date 2016-11-4
 */
public class SeleniumUtil {
	/** 使用Log4j，第一步就是获取日志记录器，这个记录器将负责控制日志信息 */
	public static Logger logger = Logger
			.getLogger(SeleniumUtil.class.getName());
	public ITestResult it = null;
	public WebDriver driver = null;
	public WebDriver window = null;
	public AndroidDriver<MobileElement> androidDriver = null;
	public String current_handles = "";
	public String driverConfgFilePath = "config/driver.properties";

	/**
	 * 
	 * @Description: 用于获取测试系统类型
	 * @param @return
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public String getSystem() {
		return PropertiesDataProvider
				.getTestData(driverConfgFilePath, "system");
	}

	/**
	 * 
	 * @Description: 运行浏览器,并打开测试地址
	 * @param @param browserName 浏览器名称，可为chrome,firefox,ie
	 * @param @param webUrl 测试的URL地址
	 * @param @param timeOut 页面加载超时设置
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-10-26
	 */
	public void launchBrowser(String browserName, String webUrl, int timeOut) {
		driver = selectExplorerByName(browserName);
		try {
			maxWindow(browserName);
			waitForPageLoading(timeOut);
			setScriptTimeout(timeOut);
			get(webUrl);
		} catch (TimeoutException e) {
			logger.warn("注意：页面没有完全加载出来，刷新重试！！");
			refresh();
			JavascriptExecutor js = (JavascriptExecutor) driver;
			String status = (String) js
					.executeScript("return document.readyState");
			logger.info("打印状态：" + status);
		}

	}

	/**
	 * 
	 * @Description: 根据浏览器名称来启动WebDriver
	 * @param @param browser 浏览器名称，可为chrome,firefox,ie
	 * @param @return
	 * @return WebDriver
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-10-26
	 */
	public WebDriver selectExplorerByName(String browser) {
		Properties props = System.getProperties(); // 获得系统属性集
		String currentPlatform = props.getProperty("os.name"); // 操作系统名称
		logger.info("当前操作系统是:[" + currentPlatform + "]");
		logger.info("启动测试浏览器：[" + browser + "]");
		// 从testNG的配置文件读取参数driverConfgFilePath的值
		String driverConfgFilePath = "config/driver.properties";
		/** 声明好驱动的路径 */
		String chromedriver = PropertiesDataProvider.getTestData(
				driverConfgFilePath, "chromedriver");
		String iedriver = PropertiesDataProvider.getTestData(
				driverConfgFilePath, "iedriver");
		if (currentPlatform.toLowerCase().contains("win")) { // 如果是windows平台
			if (browser.equalsIgnoreCase("ie")) {
				System.setProperty("webdriver.ie.driver", iedriver);
				// IE的常规设置，便于执行自动化测试
				DesiredCapabilities ieCapabilities = DesiredCapabilities
						.internetExplorer();
				ieCapabilities
						.setCapability(
								InternetExplorerDriver.INTRODUCE_FLAKINESS_BY_IGNORING_SECURITY_DOMAINS,
								true);
				// 返回ie浏览器对象
				return new InternetExplorerDriver(ieCapabilities);
			} else if (browser.equalsIgnoreCase("chrome")) {
				System.setProperty("webdriver.chrome.driver", chromedriver);
				// 返回谷歌浏览器对象
				return new ChromeDriver();
			} else if (browser.equalsIgnoreCase("firefox")) {
				// 返回火狐浏览器对象
				return new FirefoxDriver();

			} else {
				logger.error("【" + browser + "】" + "浏览器不使用于【" + currentPlatform
						+ "】操作系统");
				Assert.fail("【" + browser + "】" + "浏览器不使用于【" + currentPlatform
						+ "】操作系统");
			}
		} else
			logger.error("【" + currentPlatform + "】操作系统不支持该自动化测试框架,请更换操作系统！");
		Assert.fail("【" + currentPlatform + "】操作系统不支持该自动化测试框架,请更换操作系统！");
		return null;
	}

	/**
	 * 
	 * @Description: 用于获取到AndroidDriver
	 * @param @return 返回一个AndroidDriver实例
	 * @return AndroidDriver<MobileElement>
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-10-26
	 */
	public AndroidDriver<MobileElement> launchAndroidDriver() {
		String automationName = PropertiesDataProvider.getTestData(
				driverConfgFilePath, "automationName");
		String platformName = PropertiesDataProvider.getTestData(
				driverConfgFilePath, "platformName");
		String platformVersion = PropertiesDataProvider.getTestData(
				driverConfgFilePath, "platformVersion");
		String deviceName = PropertiesDataProvider.getTestData(
				driverConfgFilePath, "deviceName");
		String udid = PropertiesDataProvider.getTestData(driverConfgFilePath,
				"udid");
		String appPackage = PropertiesDataProvider.getTestData(
				driverConfgFilePath, "appPackage");
		String appActivity = PropertiesDataProvider.getTestData(
				driverConfgFilePath, "appActivity");
		DesiredCapabilities capabilities = new DesiredCapabilities();
		capabilities.setCapability("automationName", automationName);
		capabilities.setCapability("platformName", platformName);
		capabilities.setCapability("platformVersion", platformVersion);
		capabilities.setCapability("deviceName", deviceName);
		capabilities.setCapability("udid", udid);
		capabilities.setCapability("appPackage", appPackage);
		capabilities.setCapability("appActivity", appActivity);
		try {
			androidDriver = new AndroidDriver<MobileElement>(new URL(
					"http://127.0.0.1:4723/wd/hub"), capabilities);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return androidDriver;
	}

	/**
	 * 
	 * @Description: 最大化浏览器
	 * @param @param browserName 浏览器名称，可为chrome,firefox,ie
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-10-26
	 */
	public void maxWindow(String browserName) {
		logger.info("最大化浏览器:" + browserName);
		driver.manage().window().maximize();
	}

	/**
	 * 
	 * @Description: 等待页面加载完成
	 * @param @param pageLoadTime 页面加载时的超时时间。
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void waitForPageLoading(int pageLoadTime) {
		driver.manage().timeouts()
				.pageLoadTimeout(pageLoadTime, TimeUnit.SECONDS);

	}

	/**
	 * 
	 * @Description: 打开测试页面
	 * @param @param url 测试地址
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void get(String url) {
		driver.get(url);
		logger.info("打开测试页面:[" + url + "]");
	}

	/**
	 * 
	 * @Description: 用于获取Element元素
	 * @param @param by by对象
	 * @param @return
	 * @return T 泛型
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	@SuppressWarnings("unchecked")
	public <T> T findElementBy(By by) {
		switch (getSystem()) {
		case "web":
			return (T) driver.findElement(by);
		case "android":
			return (T) androidDriver.findElement(by);
		default:
			return null;
		}

	}

	/**
	 * 
	 * @Description: 用于获取Element元素集合
	 * @param @param by by对象
	 * @param @return
	 * @return List<T> 泛型list
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	@SuppressWarnings("unchecked")
	public <T> List<T> findElementsBy(By by) {
		switch (getSystem()) {
		case "web":
			return (List<T>) driver.findElements(by);
		case "android":
			return (List<T>) androidDriver.findElements(by);
		default:
			return null;
		}
	}

	/**
	 * 
	 * @Description: 执行点击操作
	 * @param @param by Element对象
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void click(By by) {
		try {
			logger.info("对【" + by + "】对象进行点击操作");
			if (getSystem().equals("web")) {
				((WebElement) findElementBy(by)).click();
			} else {
				((AndroidElement) findElementBy(by)).click();
			}
		} catch (Exception e) {
			logger.error("【" + by + "】对象不存在!");
		}

	}

	/**
	 * 
	 * @Description: 向输入框输入内容
	 * @param @param byElement by对象
	 * @param @param key 输入内容
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void type(By byElement, String key) {
		try {
			logger.info("正在输入：[" + key + "] 到 [" + byElement + "]");
			switch (getSystem()) {
			case "web":
				((WebElement) findElementBy(byElement)).clear();
				((WebElement) findElementBy(byElement)).sendKeys(key);
				break;
			case "android":
				((AndroidElement) findElementBy(byElement)).clear();
				((AndroidElement) findElementBy(byElement)).sendKeys(key);
				break;
			default:
				break;
			}
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("输入 [" + key + "] 到 元素[" + byElement + "]失败");
			Assert.fail("输入 [" + key + "] 到 元素[" + byElement + "]失败", e);
		}
	}

	/**
	 * 
	 * @Description: 清空输入框内容
	 * @param @param byElement by对象
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void clear(By byElement) {
		try {
			switch (getSystem()) {
			case "web":
				((WebElement) findElementBy(byElement)).clear();
				break;
			case "android":
				((AndroidElement) findElementBy(byElement)).clear();
				break;
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("清除元素 [" + byElement + "] 上的内容失败!");
		}
		logger.info("清除元素 [" + byElement + "]上的内容");
	}

	/**
	 * 
	 * @Description: 暂停当前用例的执行，暂停的时间为：sleepTime,单位为秒
	 * @param @param sleepTime 暂停时间为,单位为秒
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void pause(int sleepTime) {
		if (sleepTime <= 0) {
			return;
		}
		try {
			TimeUnit.SECONDS.sleep(sleepTime);
			logger.info("暂停:" + sleepTime + "秒");
		} catch (InterruptedException e) {
			e.printStackTrace();
		}

	}

	/**
	 * 
	 * @Description: 在给定的时间内去查找元素，如果没找到则超时，抛出异常,该方法针对web
	 * @param @param By by对象
	 * @param @param timeOut 超时时间，单位为毫秒
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void waitForElementToLoad(final By By, int timeOut) {
		logger.info("开始查找元素[" + By + "]");
		try {
			(new WebDriverWait(driver, timeOut))
					.until(new ExpectedCondition<Boolean>() {

						public Boolean apply(WebDriver driver) {
							WebElement element = driver.findElement(By);
							return element.isDisplayed();
						}
					});
		} catch (TimeoutException e) {
			logger.error("超时!! " + timeOut + " 秒之后还没找到元素 [" + By + "]");
			Assert.fail("超时!! " + timeOut + " 秒之后还没找到元素 [" + By + "]");

		}
		logger.info("找到了元素 [" + By + "]");
	}

	/**
	 * 
	 * @Description: 切换frame,根据String类型（frame名字）
	 * @param @param frameDesc frame名字
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-06
	 */
	public void inFrame(String frameDesc) {
		switch (getSystem()) {
		case "web":
			logger.info("切换到第frame:" + frameDesc);
			driver.switchTo().frame(frameDesc);
			break;
		case "android":
			logger.info("切换到第frame:" + frameDesc);
			androidDriver.switchTo().frame(frameDesc);
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @Description: 切换frame,根据frame在当前页面中的顺序来定位
	 * @param @param frameNum frame顺序
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-06
	 */
	public void inFrame(int frameNum) {
		logger.info("切换到第" + frameNum + "个frame");
		driver.switchTo().frame(frameNum);
	}

	/**
	 * 
	 * @Description: 切换frame - 根据页面元素定位
	 * @param @param byElement 页面中的Element对象
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-10-27
	 */
	public void switchFrame(By byElement) {
		try {
			logger.info("开始切换到frame [" + byElement + "]");
			switch (getSystem()) {
			case "web":
				driver.switchTo().frame((WebElement) findElementBy(byElement));
				break;
			case "android":
				androidDriver.switchTo().frame(
						(AndroidElement) findElementBy(byElement));
				break;
			default:
				break;
			}
		} catch (Exception e) {
			logger.error("切换到frame [" + byElement + "] 失败");
			Assert.fail("切换到frame [" + byElement + "] 失败");
		}
		logger.info("切换到frame [" + byElement + "] 成功");
	}

	/**
	 * 
	 * @Description: 跳出frame,返回到默认的frame
	 * @param
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void outFrame() {
		switch (getSystem()) {
		case "web":
			driver.switchTo().defaultContent();
			logger.info("跳出当前frame,返回到默认的frame");
			break;
		case "android":
			androidDriver.switchTo().defaultContent();
			logger.info("跳出当前frame,返回到默认的frame");
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @Description: 选择下拉选项 -根据文本内容
	 * @param @param by by对象
	 * @param @param text 文本内容
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void selectByText(By by, String text) {
		Select s;
		try {
			if (getSystem().equals("web")) {
				s = new Select(driver.findElement(by));
				logger.info("对【" + by + "】进行下拉选择操作");
				s.selectByVisibleText(text);
			} else {
				s = new Select(androidDriver.findElement(by));
				logger.info("对【" + by + "】进行下拉选择操作");
				s.selectByVisibleText(text);
			}
		} catch (Exception e) {
			logger.error("【" + by + "】对象不存在");
		}
	}

	/**
	 * 
	 * @Description: 选择下拉选项 -根据index
	 * @param @param by by对象
	 * @param @param index index值
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void selectByIndex(By by, int index) {
		Select s;
		try {
			if (getSystem().equals("web")) {
				s = new Select(driver.findElement(by));
				logger.info("对【" + by + "】进行下拉选择操作");
				s.selectByIndex(index);
			} else {
				s = new Select(androidDriver.findElement(by));
				logger.info("对【" + by + "】进行下拉选择操作");
				s.selectByIndex(index);
			}
		} catch (Exception e) {
			logger.error("【" + by + "】对象不存在");
		}
	}

	/**
	 * 
	 * @Description: 选择下拉选项 -根据value
	 * @param @param by by对象
	 * @param @param value value值
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void selectByValue(By by, String value) {
		Select s;
		try {
			if (getSystem().equals("web")) {
				s = new Select(driver.findElement(by));
				logger.info("对【" + by + "】进行下拉选择操作");
				s.selectByValue(value);
			} else {
				s = new Select(androidDriver.findElement(by));
				logger.info("对【" + by + "】进行下拉选择操作");
				s.selectByValue(value);
			}
		} catch (Exception e) {
			logger.error("【" + by + "】对象不存在");
		}
	}

	/**
	 * 
	 * @Description: 判断文本是不是和需求要求的文本一致
	 * @param @param actual 实际值
	 * @param @param expected 预期值
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void verify(Object actual, Object expected) {
		try {
			Assert.assertEquals(actual, expected);
			logger.info("检查点通过，找到了期望的结果: [" + expected + "]");
		} catch (AssertionError e) {
			logger.error("期望的结果为 [" + expected + "] 但是找到了 [" + actual + "]");
			//Assert.fail("期望的结果为 [" + expected + "] 但是找到了 [" + actual + "]");
		}
	}

	/**
	 * 
	 * @Description: 获得元素属性的文本
	 * @param @param elementLocator by对象
	 * @param @param attribute 元素的属性
	 * @param @return
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public String getAttributeText(By elementLocator, String attribute) {
		String attributeText = null;
		switch (getSystem()) {
		case "web":
			attributeText = driver.findElement(elementLocator)
					.getAttribute(attribute).trim();
			logger.info("获取到元素【" + elementLocator + "】的" + attribute + "属性的值为:"
					+ attributeText);
			break;
		case "android":
			attributeText = androidDriver.findElement(elementLocator)
					.getAttribute(attribute).trim();
			logger.info("获取到元素【" + elementLocator + "】的" + attribute + "属性的值为:"
					+ attributeText);
			break;
		default:
			break;
		}
		return attributeText;
	}

	/**
	 * 
	 * @Description: 获得页面的标题
	 * @param @return
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public String getTitle() {
		String Title;
		switch (getSystem()) {
		case "web":
			Title = driver.getTitle();
			logger.info("获取网页标题，标题内容为：" + Title);
			return Title;
		case "android":
			Title = androidDriver.getTitle();
			logger.info("获取网页标题，标题内容为：" + Title);
			return Title;
		default:
			return null;
		}
	}

	/**
	 * 
	 * @Description: 获取当前页面的URL
	 * @param @return
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public String getPageURL() {
		String url = null;
		switch (getSystem()) {
		case "web":
			url = driver.getCurrentUrl();
			logger.info("获取当前页面的URL,URL地址为：" + url);
			break;
		case "android":
			url = androidDriver.getCurrentUrl();
			logger.info("获取当前页面的URL,URL地址为：" + url);
			break;
		default:
			break;
		}
		return url;

	}

	/**
	 * 
	 * @Description: 获得元素的文本
	 * @param @param elementLocator by对象
	 * @param @return
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public String getText(By elementLocator) {
		String text = null;
		switch (getSystem()) {
		case "web":
			text = driver.findElement(elementLocator).getText().trim();
			logger.info("获取【" + elementLocator + "】元素的文本为:" + text);
			break;
		case "android":
			text = androidDriver.findElement(elementLocator).getText().trim();
			logger.info("获取【" + elementLocator + "】元素的文本为:" + text);
			break;
		default:
			break;
		}
		return text;
	}

	/**
	 * 
	 * @Description: 进入新窗口，针对web
	 * @param @param byElement by对象
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void switchNewWindow(By byElement) {
		Set<String> all_handles;
		Iterator<String> it;
		// 获取当前页面句柄
		current_handles = driver.getWindowHandle();
		// 点击某个链接会弹出一个新窗口
		click(byElement);
		// 接下来会有新的窗口打开，获取所有窗口句柄
		all_handles = driver.getWindowHandles();
		// 循环判断，把当前句柄从所有句柄中移除，剩下的就是你想要的新窗口
		it = all_handles.iterator();
		while (it.hasNext()) {
			if (current_handles == it.next())
				continue;
			// 跳入新窗口,并获得新窗口的driver - newWindow
			logger.info("点击【" + byElement + "】对象，并进入新窗口。");
			window = driver.switchTo().window(it.next());
		}
	}

	/**
	 * 
	 * @Description: 回到原始窗口
	 * @param
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void backToOriginalWindow() {
		window.close();
		switch (getSystem()) {
		case "web":
			logger.info("返回到原始窗口");
			driver.switchTo().window(current_handles);
			break;
		case "android":
			logger.info("返回到原始窗口");
			androidDriver.switchTo().window(current_handles);
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @Description: 等待alert出现
	 * @param @param waitMillisecondsForAlert 等待时间，单位：毫秒
	 * @param @return
	 * @param @throws NoAlertPresentException
	 * @return Alert
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public Alert switchToPromptedAlertAfterWait(long waitMillisecondsForAlert)
			throws NoAlertPresentException {
		final int ONE_ROUND_WAIT = 200;
		NoAlertPresentException lastException = null;
		Alert alert = null;

		long endTime = System.currentTimeMillis() + waitMillisecondsForAlert;

		for (long i = 0; i < waitMillisecondsForAlert; i += ONE_ROUND_WAIT) {

			try {
				switch (getSystem()) {
				case "web":
					alert = driver.switchTo().alert();
					break;
				case "android":
					alert = androidDriver.switchTo().alert();
					break;
				default:
					break;
				}
				return alert;
			} catch (NoAlertPresentException e) {
				lastException = e;
			}
			try {
				Thread.sleep(ONE_ROUND_WAIT);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}

			if (System.currentTimeMillis() > endTime) {
				break;
			}
		}
		throw lastException;
	}

	/**
	 * 
	 * @Description: 执行JavaScript 方法和对象,
	 *               用法：seleniumUtil.executeJS("arguments[0].click();"
	 *               ,seleniumUtil.findElementBy(loginPage.MOP_TAB_ORDERCLOSE));
	 * @param @param js JavaScript语句
	 * @param @param args 对象
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void executeJS(String js, Object... args) {
		switch (getSystem()) {
		case "web":
			((JavascriptExecutor) driver).executeScript(js, args);
			logger.info("执行JavaScript语句：[" + js + "]");
			break;
		case "android":
			((JavascriptExecutor) androidDriver).executeScript(js, args);
			logger.info("执行JavaScript语句：[" + js + "]");
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @Description: 执行JavaScript 方法
	 * @param @param js JavaScript语句
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void executeJS(String js) {
		switch (getSystem()) {
		case "web":
			((JavascriptExecutor) driver).executeScript(js);
			logger.info("执行JavaScript语句：[" + js + "]");
			break;
		case "android":
			((JavascriptExecutor) androidDriver).executeScript(js);
			logger.info("执行JavaScript语句：[" + js + "]");
			break;
		default:
			break;
		}

	}

	/**
	 * 
	 * @Description: 刷新，针对web
	 * @param
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void refresh() {
		driver.navigate().refresh();
		logger.info("页面刷新成功！");
	}

	/**
	 * 
	 * @Description: 后退，针对web
	 * @param
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void back() {
		logger.info("浏览器执行返回操作");
		driver.navigate().back();
	}

	/**
	 * 
	 * @Description: 前进，针对web
	 * @param
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void forward() {
		logger.info("浏览器执行前进操作");
		driver.navigate().forward();
	}

	/**
	 * 
	 * @Description: 上传文件，需要点击弹出上传照片的窗口才行，针对web
	 * @param @param browser 使用的浏览器名称
	 * @param @param file 需要上传的文件及文件名
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void handleUpload(String browser, File file) {
		String filePath = file.getAbsolutePath();
		String executeFile = "res/script/autoit/Upload.exe";
		String cmd = "\"" + executeFile + "\"" + " " + "\"" + browser + "\""
				+ " " + "\"" + filePath + "\"";
		try {
			logger.info("正在上传文件：" + filePath);
			Process p = Runtime.getRuntime().exec(cmd);
			p.waitFor();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * 
	 * @Description: 销毁driver或者androidDriver实例
	 * @param
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void quit() {
		switch (getSystem()) {
		case "web":
			logger.info("退出WebDriver实例");
			driver.quit();
			break;
		case "android":
			logger.info("退出AndroidDriver实例");
			androidDriver.quit();
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @Description: 关闭页面
	 * @param
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void close() {
		switch (getSystem()) {
		case "web":
			logger.info("关闭当前页面");
			driver.close();
			break;
		case "android":
			logger.info("关闭当前页面");
			androidDriver.close();
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @Description: 检查对象是否是灰化状态(即是否可用)
	 * @param @param by
	 * @param @return
	 * @return boolean
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public boolean isEnabled(By by) {
		switch (getSystem()) {
		case "web":
			return driver.findElement(by).isEnabled();
		case "android":
			return androidDriver.findElement(by).isEnabled();
		default:
			return false;
		}
	}

	/**
	 * 
	 * @Description: 模拟键盘操作
	 * @param @param Element 页面中的Element对象
	 * @param @param key 键盘上的功能键 比如ctrl ,alt等,即Keys.CONTROL，Keys.ALT
	 * @param @param keyword 键盘上的字母
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-06
	 */
	public <T> void pressKeysOnKeyboard(T Element, Keys key, String keyword) {
		try {
			logger.info("对【" + Element + "】对象进行" + key + "键盘操作");
			if (getSystem().equals("web")) {
				((WebElement) Element).sendKeys(Keys.chord(key, keyword));
			} else {
				((AndroidElement) Element).sendKeys(Keys.chord(key, keyword));
			}
		} catch (Exception e) {
			logger.error("【" + Element + "】对象不存在");
		}
	}

	/**
	 * 
	 * @Description: 检查checkbox等对象是不是勾选
	 * @param @param by 页面中的Element对象
	 * @param @return
	 * @return boolean 返回true、false
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-06
	 */
	public boolean doesCheckboxSelected(By by) {
		boolean flag = false;
		switch (getSystem()) {
		case "web":
			if (((WebElement) findElementBy(by)).isSelected() == true) {
				logger.info("CheckBox: "
						+ getLocatorByElement((WebElement) findElementBy(by),
								">") + " 被勾选");
				flag = true;
			} else {
				logger.info("CheckBox: "
						+ getLocatorByElement((WebElement) findElementBy(by),
								">") + " 没有被勾选");
				flag = false;
			}
			break;
		case "android":
			if (((AndroidElement) findElementBy(by)).isSelected() == true) {
				logger.info("CheckBox: "
						+ getLocatorByElement(
								(AndroidElement) findElementBy(by), ">")
						+ " 被勾选");
				flag = true;
			} else {
				logger.info("CheckBox: "
						+ getLocatorByElement(
								(AndroidElement) findElementBy(by), ">")
						+ " 没有被勾选");
				flag = false;
			}
			break;
		default:
			break;
		}
		return flag;

	}

	/**
	 * 
	 * @Description: 获得当前select选择的值
	 * @param @param by by对象
	 * @param @return
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public String getCurrentSelectValue(By by) {
		Select s;
		switch (getSystem()) {
		case "web":
			s = new Select(driver.findElement(by));
			return s.getFirstSelectedOption().getText().trim();
		case "android":
			s = new Select(androidDriver.findElement(by));
			return s.getFirstSelectedOption().getText().trim();
		default:
			return null;
		}
	}

	/**
	 * 
	 * @Description: 获取下拉列表的所有选项
	 * @param @param by By元素对象
	 * @param @return 返回所有下拉列表中的选项，如option1,option2,……
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-8
	 */
	public String getSelectOption(By by) {
		String value = null;
		Select s;
		List<WebElement> options;
		switch (getSystem()) {
		case "web":
			s = new Select(driver.findElement(by));
			options = s.getOptions();
			for (int i = 0; i < options.size(); i++) {
				value = value + "," + options.get(i).getText();
			}
			break;
		case "android":
			s = new Select(androidDriver.findElement(by));
			options = s.getOptions();
			for (int i = 0; i < options.size(); i++) {
				value = value + "," + options.get(i).getText();
			}
			break;
		default:
			break;
		}
		return value.replace("null,", "");

	}

	/**
	 * 
	 * @Description: 模拟鼠标操作 - 鼠标移动到指定元素
	 * @param @param by By元素对象
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-11
	 */
	public void mouseMoveToElement(By by) {
		Actions builder = null;
		Actions mouse = null;
		switch (getSystem()) {
		case "web":
			builder = new Actions(driver);
			mouse = builder.moveToElement(driver.findElement(by));
			break;
		case "android":
			builder = new Actions(androidDriver);
			mouse = builder.moveToElement(androidDriver.findElement(by));
			break;
		default:
			break;
		}
		logger.info("鼠标移动到【" + by + "】对象上");
		mouse.perform();
	}

	/**
	 * 
	 * @Description: 模拟鼠标操作 - 鼠标右击
	 * @param @param by By元素对象
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-11
	 */
	public void mouseRightClick(By by) {
		Actions builder = null;
		Actions mouse = null;
		switch (getSystem()) {
		case "web":
			builder = new Actions(driver);
			mouse = builder.contextClick((WebElement) findElementBy(by));
			break;
		case "android":
			builder = new Actions(androidDriver);
			mouse = builder.contextClick((AndroidElement) findElementBy(by));
			break;
		default:
			break;
		}
		logger.info("对【" + by + "】对象进行鼠标右击操作");
		mouse.perform();
	}

	/**
	 * 
	 * @Description: 根据元素来获取此元素的定位值
	 * @param @param element element对象
	 * @param @param expectText 要查找的字符
	 * @param @return
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-11
	 */
	public <T> String getLocatorByElement(T element, String expectText) {
		String text = null;
		String expect = null;
		switch (getSystem()) {
		case "web":
			text = ((WebElement) element).toString();
			break;
		case "android":
			text = ((AndroidElement) element).toString();
			break;
		default:
			break;
		}
		try {
			expect = text.substring(text.indexOf(expectText) + 1,
					text.length() - 1);
		} catch (Exception e) {
			e.printStackTrace();
			logger.error("查找字符串[" + expectText + "]失败，未找到");

		}
		return expect;
	}

	/**
	 * 
	 * @Description: 页面过长时候滑动页面 window.scrollTo(左边距,上边距)
	 * @param @param x
	 * @param @param y
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-6
	 */
	public void scrollPage(int x, int y) {
		String js = "window.scrollTo(" + x + "," + y + ");";
		switch (getSystem()) {
		case "web":
			logger.info("滑动到页面的【" + x + "," + y + "】坐标上");
			((JavascriptExecutor) driver).executeScript(js);
			break;
		case "android":
			logger.info("滑动到页面的【" + x + "," + y + "】坐标上");
			((JavascriptExecutor) androidDriver).executeScript(js);
			break;
		default:
			break;
		}
	}

	/**
	 * 
	 * @Description: 对于windows GUI弹出框，要求输入用户名和密码时，seleniumm不能直接操作，
	 *               需要借助http://modifyusername:modifypassword@yoururl 这种方法
	 * @param @param username
	 * @param @param password
	 * @param @param url
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-10-27
	 */
	public void optOnWinGUI(String username, String password, String url) {
		try {
			if (getSystem().equals("web")) {
				driver.get(username + ":" + password + "@" + url);
			} else {
				androidDriver.get(username + ":" + password + "@" + url);
			}
		} catch (Exception e) {
		}
	}

	/**
	 * 
	 * @Description: 识别对象时的超时时间
	 * @param @param timeOut 超时时间，单位为秒
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-06
	 */
	public void implicitlyWait(int timeOut) {
		try {
			if (getSystem().equals("web")) {
				logger.info("设置识别对象的超时时间为:" + timeOut + "秒");
				driver.manage().timeouts()
						.implicitlyWait(timeOut, TimeUnit.SECONDS);
			} else {
				logger.info("设置识别对象的超时时间为:" + timeOut + "秒");
				androidDriver.manage().timeouts()
						.implicitlyWait(timeOut, TimeUnit.SECONDS);
			}
		} catch (Exception e) {
		}

	}

	/**
	 * 
	 * @Description: 异步脚本的超时时间。driver可以异步执行脚本，这个是设置异步执行脚本脚本返回结果的超时时间
	 * @param @param timeOut 超时时间，单位为秒
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-06
	 */
	public void setScriptTimeout(int timeOut) {
		try {
			if (getSystem().equals("web")) {
				logger.info("设置异步脚本的超时时间为:" + timeOut + "秒");
				driver.manage().timeouts()
						.setScriptTimeout(timeOut, TimeUnit.SECONDS);
			} else {
				logger.info("设置异步脚本的超时时间为:" + timeOut + "秒");
				androidDriver.manage().timeouts()
						.setScriptTimeout(timeOut, TimeUnit.SECONDS);
			}
		} catch (Exception e) {
		}
	}

}