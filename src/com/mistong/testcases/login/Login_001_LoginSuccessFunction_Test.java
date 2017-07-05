package com.mistong.testcases.login; 

import org.testng.annotations.Test;
import com.mistong.base.BaseParpare;
import com.mistong.util.KeyWordDriver;

public class Login_001_LoginSuccessFunction_Test extends BaseParpare { 
	@Test 
	public void LoginSuccessFunction() { 
		KeyWordDriver driver = new KeyWordDriver();
		driver.runScript(seleniumUtil, testContext);
	}
}