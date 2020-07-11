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
import static org.junit.Assert.assertTrue;
import static org.openqa.selenium.support.ui.ExpectedConditions.presenceOfElementLocated;
import static org.openqa.selenium.support.ui.ExpectedConditions.urlContains;

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
    driver = new HtmlUnitDriver(BrowserVersion.CHROME, true);
  }

  @After
  public void teardown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  public void LoginLinks_AsGuestUser_Login() {
    WebDriverWait wait = new WebDriverWait(driver, 5);
    driver.get("http://localhost:8080/");
    By loginLogoutMessage = By.id("loginLogoutMessage");
    wait.until(presenceOfElementLocated(loginLogoutMessage));
    WebElement loginLogoutElement = driver.findElement(loginLogoutMessage);
    String actualLoginLogouText = loginLogoutElement.getText();

    // Guest user should expect to see login message
    assertEquals("Login to help out a neighbor!", actualLoginLogouText);

    loginLogoutElement.click();
    wait.until(urlContains("/_ah/login?continue=%2Faccount.jsp"));
    By loginButton = By.id("btn-login");
    wait.until(presenceOfElementLocated(loginButton));
    WebElement loginButtonElement = driver.findElement(loginButton);
    loginButtonElement.click();
    wait.until(urlContains("/user_profile.jsp"));

    // User should now be logged in and redirected to userpage
    assertTrue(driver.getCurrentUrl().contains("/user_profile.jsp"));
    assertEquals("My Account", driver.getTitle());

    System.out.println("\n\n\n" + driver.getCurrentUrl() + "\n\n\n");

    // By backToHome = By.id("return-link");
    // wait.until(presenceOfElementLocated(backToHome));
    // WebElement backToHomeElement = driver.findElement(backToHome);
    // backToHomeElement.click();
    // wait.until(urlContains("/index.jsp"));

    // wait.until(presenceOfElementLocated(loginLogoutMessage));
    // loginLogoutElement = driver.findElement(loginLogoutMessage);
    // actualLoginLogouText = loginLogoutElement.getText();

    // Login message should now show option to Logout
    // assertEquals("Logout", actualLoginLogouText);
  }
}
