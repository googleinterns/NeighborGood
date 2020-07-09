// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

import static org.junit.Assert.assertEquals;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;

import com.gargoylesoftware.htmlunit.BrowserVersion;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class ChromeTest {

  private WebDriver driver;

  @Before
  public void setUp() {
    driver = new HtmlUnitDriver(BrowserVersion.CHROME);
  }

  @After
  public void teardown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  public void test() {
    WebDriverWait wait = new WebDriverWait(driver, 30);
    driver.get("https://neighborgood-step.appspot.com/");
    By loginLogout = By.id("title");
    wait.until(presenceOfElementLocated(loginLogout));
    WebElement element = driver.findElement(loginLogout);
    String actualElementText = element.getText();
    assertEquals("Login to help out a neighbor!", actualElementText);
  }
}
