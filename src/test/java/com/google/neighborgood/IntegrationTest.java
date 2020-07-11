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

import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.List;
import java.util.concurrent.TimeUnit;
import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class IntegrationTest {

  private WebDriver driver;
  private WebDriverWait wait;

  private final String USER_NICKNAME = "Mafe";
  private final String USER_EMAIL = "123456@example.com";
  private final String USER_ADDRESS = "123 Street Name, City, ST";
  private final String USER_PHONE = "1231231234";

  @BeforeClass
  public static void setupClass() {
    WebDriverManager.chromedriver().setup();
  }

  @Before
  public void setupTest() {
    driver = new ChromeDriver();
    wait = new WebDriverWait(driver, 5);
  }

  @After
  public void teardown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  public void test() {
    driver.get("http://localhost:8080/");
    By loginMessage = By.id("loginLogoutMessage");
    wait.until(ExpectedConditions.presenceOfElementLocated(loginMessage));
    WebElement loginElement = driver.findElement(loginMessage);
    String actualLoginText = loginElement.getText();

    // Guest user should expect to see login message
    assertEquals("Login to help out a neighbor!", actualLoginText);

    By addTaskButton = By.id("addtaskbutton");
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    List<WebElement> addTaskButtonElement = driver.findElements(addTaskButton);

    // Add task button should be missing when user is not logged in
    assertTrue(addTaskButtonElement.isEmpty());

    By dashboardIcon = By.className("dashboard-icon");
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    List<WebElement> dashboardIconsElement = driver.findElements(dashboardIcon);

    // Dashboard icon (userpage or admin page) buttons
    // should be missing when user is not logged in
    assertTrue(dashboardIconsElement.isEmpty());

    loginElement.click();
    By emailInput = By.id("email");
    WebElement emailInputElement = driver.findElement(emailInput);

    // In order to mimic a true new user situation the user must
    // not have filled out its details before, so if you must run
    // this test multiple times, make sure to do `mvn clean` after
    // each test to clear the stored userinfo or clear all user
    // entities from the admin console
    emailInputElement.clear();
    emailInputElement.sendKeys(USER_EMAIL);
    By loginButton = By.id("btn-login");
    WebElement loginButtonElement = driver.findElement(loginButton);
    loginButtonElement.click();

    // User should now be logged in and redirected to the
    // account page to enter their user info
    assertTrue(driver.getCurrentUrl().contains("/account.jsp"));
    assertEquals("My Personal Info", driver.getTitle());

    By nicknameInput = By.id("nickname-input");
    WebElement nicknameInputElement = driver.findElement(nicknameInput);
    nicknameInputElement.sendKeys(USER_NICKNAME);

    By addressInput = By.id("address-input");
    WebElement addressInputElement = driver.findElement(addressInput);
    addressInputElement.sendKeys(USER_ADDRESS);

    By phoneInput = By.id("phone-input");
    WebElement phoneInputElement = driver.findElement(phoneInput);
    phoneInputElement.sendKeys(USER_PHONE);

    By submitButton = By.id("submit-button");
    WebElement submitButtonElement = driver.findElement(submitButton);
    submitButtonElement.click();

    // After new user fills out user info, they should be redirected to userpage
    assertTrue(driver.getCurrentUrl().contains("/user_profile.jsp"));
    assertEquals("My Account", driver.getTitle());

    driver.get("http://localhost:8080/");

    By logoutMessage = By.id("login-logout");
    WebElement logoutElement = driver.findElement(logoutMessage);
    String actualLogoutText = logoutElement.getText();

    // Should show a custom logout message with user's nickname
    assertEquals(USER_NICKNAME + " | Logout", actualLogoutText);
  }
}
