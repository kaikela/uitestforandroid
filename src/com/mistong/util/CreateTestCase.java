package com.mistong.util;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.testng.Assert;
import jxl.read.biff.BiffException;

/**
 * 
 * ClassName: TestCaseFactoryForSingle
 * 
 * @Description: 根据测试用例填写完res/testcase、res/page下面的excel后，运行该类能够自动生成指定模块用例的测试代码
 * @author 吴丁飞
 * @date 2016-11-5
 */
public class CreateTestCase {
	public static void main(String[] args) {
		TestCaseFactory("login", "Login");
	}

	/**
	 * 
	 * @Description: 根据模块名、功能名(excel名称)来自动生成测试用例脚本
	 * @param @param moduleName 模块名
	 * @param @param functionName 功能名
	 * @return void
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-5
	 */
	public static void TestCaseFactory(String moduleName, String functionName) {
		final String caseFolder = "src/com/mistong/testcases/";// 测试代码包路径
		File sourceFile = null;// 测试用例excel源文件
		String sheetName = null;// 测试用例excel中sheet的名字
		int sheetNum = 0;// sheet的号码
		try {
			// 如果包名不存在，就新建
			File packAge = new File(caseFolder + "/" + moduleName);
			if (packAge.exists()) {
				System.out.println(moduleName + "包已经存在！");
				System.out.println("正在生成用例到" + moduleName + "包下，请稍等...");
			} else {
				packAge.mkdir();
				System.out.println(moduleName + "包已创建！");
				System.out.println("正在生成用例到" + moduleName + "包下，请稍等...");
			}

			// 根据传入的excel文件路径获得模块中sheet数量,也就是用例个数
			for (int i = 0; i < getSheetNum(getExcelRelativePath(functionName)); i++) {
				if (i == getSheetNum(getExcelRelativePath(functionName)) - 1) {
					// 如果只有一个sheet的时候，跳出循环不进行自动生成代码操作，因为没有可以生成的。
					break;
				}
				try {
					sheetName = getSheetName(i + 1,
							getExcelRelativePath(functionName)); // 取得sheetName，从i+1开始
					sheetNum = getSheetNum(getExcelRelativePath(functionName));
				} catch (BiffException e1) {
					e1.printStackTrace();
				}
				sourceFile = new File(caseFolder + moduleName.toLowerCase()
						+ File.separator + functionName + "_" + sheetName
						+ "_Test.java");// 创建测试用例源码，指定存放路径
				FileWriter writer = new FileWriter(sourceFile);

				// 生成测试用例代码的头文件
				writer.write("package com.mistong.testcases." + moduleName
						+ "; \n" + "\n"
						+ "import org.testng.annotations.Test;\n"
						+ "import com.mistong.base.BaseParpare;\n"
						+ "import com.mistong.util.KeyWordDriver;\n" + "\n"
						+ "public class " + functionName + "_" + sheetName
						+ "_Test extends BaseParpare { \n");

				// @Test的主体部分，也就是测试用例的方法
				writer.write("	@Test \n"
						+ "	public void"
						+ " "
						+ sheetName.substring(sheetName.indexOf("_") + 1,
								sheetName.length()) + "() { \n"
						+ "		KeyWordDriver driver = new KeyWordDriver();\n"
						+ "		driver.runScript(seleniumUtil, testContext);\n"
						+ "	}\n");
				// 代码结尾大括号
				writer.write("}");
				writer.close();
			}
		} catch (IOException e) {
			Assert.fail("IO异常", e);
		}
		System.out.println("模块[" + moduleName + "]下[" + functionName
				+ "]功能的用例已经生成完毕，共计：" + (sheetNum - 1) + "条，请到" + caseFolder
				+ functionName.toLowerCase() + "路径下查阅！");
	}

	/**
	 * 
	 * @Description: 获得excel的相对路径
	 * @param @param functionName 循环模块名称的角标
	 * @param @return 得到对应index的模块名字
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-5
	 */
	public static String getExcelRelativePath(String functionName) {
		String dir = "res/testcase";
		String path = "";
		File file = new File(dir + File.separator + functionName + ".xlsx");
		path = file.getPath();
		return path;
	}

	/**
	 * 
	 * @Description: 获得当前excel的sheet数量 - 每个模块功能下的用例数
	 * @param @param filePath 文件路径
	 * @param @return 获得excel的sheet数量
	 * @param @throws FileNotFoundException
	 * @param @throws IOException
	 * @return int
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-5
	 */
	public static int getSheetNum(String filePath)
			throws FileNotFoundException, IOException {
		int casesNum = 0;
		@SuppressWarnings("resource")
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(new File(
				filePath)));
		casesNum = workbook.getNumberOfSheets();
		return casesNum;
	}

	/**
	 * 
	 * @Description: 获取指定excel文件的sheet页名称
	 * @param @param sheetIndex sheet的位置
	 * @param @param filePath excel文件路径相对的
	 * @param @return 返回sheet的名字
	 * @param @throws BiffException
	 * @param @throws IOException
	 * @return String
	 * @throws
	 * @author 吴丁飞
	 * @date 2016-11-5
	 */
	public static String getSheetName(int sheetIndex, String filePath)
			throws BiffException, IOException {
		String casesName = "";
		@SuppressWarnings("resource")
		XSSFWorkbook workbook = new XSSFWorkbook(new FileInputStream(filePath));
		casesName = workbook.getSheetName(sheetIndex);
		return casesName;

	}

}
