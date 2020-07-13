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

import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {

  private static WebDriver driver;
  private static WebDriverWait wait;

  private final String USER_NICKNAME = "Mafe";
  private final String USER_EMAIL = "123456@example.com";
  private final String USER_ADDRESS = "123 Street Name, City, ST";
  private final String USER_PHONE = "1231231234";
  private final String TASK_DETAIL = "Help!";
  private final int GARDEN_CATEGORY = 0;
  private final int SHOPPING_CATEGORY = 1;
  private final int PETS_CATEGORY = 2;
  private final int MISC_CATEGORY = 3;
  private final int NUM_OF_CATEGORIES = 4;

  private int taskCount = 0;

  private static void clearAllDatastoreEntities(WebDriver driver) {
    // Clears datastore entities for start of test
    driver.get("http://localhost:8080/_ah/admin");
    By entityKindSelect = By.id("kind_input");
    wait.until(presenceOfElementLocated(entityKindSelect));
    // Retrieves number of Entity Kind select options to iterate through them
    Select kindSelect = new Select(driver.findElement(entityKindSelect));
    List<WebElement> allEntityKinds = kindSelect.getOptions();
    By listButton;
    By allKeys;
    By deleteButton;
    System.out.println("\n\n" + allEntityKinds.size() + "\n\n");
    for (int i = 1; i < allEntityKinds.size(); i++) {
      listButton = By.id("list_button");
      wait.until(presenceOfElementLocated(listButton));
      driver.findElement(listButton).click();
      allKeys = By.id("allkeys");
      wait.until(presenceOfElementLocated(allKeys));
      driver.findElement(allKeys).click();
      deleteButton = By.id("delete_button");
      wait.until(presenceOfElementLocated(deleteButton));
      driver.findElement(deleteButton).click();
      driver.switchTo().alert().accept();
    }
  }

  private void addTask(String details, String points, int categoryIndex) {

    By addTaskButton = By.id("create-task-button");
    wait.until(presenceOfElementLocated((addTaskButton)));
    WebElement addTaskButtonElement = driver.findElement(addTaskButton);
    addTaskButtonElement.click();

    By createTaskModal = By.id("createTaskModal");
    wait.until(presenceOfElementLocated(createTaskModal));
    WebElement createTaskModalElement = driver.findElement(createTaskModal);

    // After clicking on the add task button, the modal should be displayed
    // assertTrue("Create task modal should be displayed", createTaskModalElement.isDisplayed());

    By taskDetailInput = By.id("task-detail-input");
    wait.until(presenceOfElementLocated(taskDetailInput));
    WebElement taskDetailInputElement = driver.findElement(taskDetailInput);
    taskDetailInputElement.sendKeys(details);

    // Input task reward pts
    By rewardPointInput = By.id("rewarding-point-input");
    wait.until(presenceOfElementLocated(rewardPointInput));
    WebElement rewardPointInputElement = driver.findElement(rewardPointInput);
    rewardPointInputElement.clear();
    rewardPointInputElement.sendKeys(points);

    // Input task category
    By categoryInput = By.id("category-input");
    wait.until(presenceOfElementLocated(categoryInput));
    Select categoryInputElement = new Select(driver.findElement(categoryInput));
    categoryInputElement.selectByIndex(categoryIndex);

    By submitButton = By.id("submit-create-task");
    wait.until(presenceOfElementLocated(submitButton));
    driver.findElement(submitButton).click();
  }

  private void verifyNewTaskUserPage(String expectedDetails) {
    // Verify that inputted task info is correctly displayed in need help table
    String taskRowXPath = "//table[@id='need-help']/tbody/tr[1]";
    By rowTaskDetails = By.xpath(taskRowXPath + "/td[1]");
    wait.until(presenceOfElementLocated(rowTaskDetails));
    String rowDetailsActual = driver.findElement(rowTaskDetails).getText();
    assertEquals(expectedDetails, rowDetailsActual);

    By rowTaskHelper = By.xpath(taskRowXPath + "/td[2]");
    wait.until(presenceOfElementLocated(rowTaskHelper));
    String rowHelperActual = driver.findElement(rowTaskHelper).getText();
    assertEquals("N/A", rowHelperActual);

    By rowTaskStatus = By.xpath(taskRowXPath + "/td[3]");
    wait.until(presenceOfElementLocated(rowTaskStatus));
    String rowStatusActual = driver.findElement(rowTaskStatus).getText();
    assertEquals("OPEN", rowStatusActual);
  }

  @BeforeClass
  public static void setupClass() {
    WebDriverManager.chromedriver().setup();
    driver = new ChromeDriver();
    // ChromeOptions options = new ChromeOptions();
    // options.addArguments("--headless");
    // options.addArguments("--disable-gpu");
    // driver = new ChromeDriver(options);
    wait = new WebDriverWait(driver, 30);
    clearAllDatastoreEntities(driver);
  }

  @AfterClass
  public static void teardown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  public void _01_Homepage_AsNewGuestUser_LoginAndInputUserInfo() {
    driver.get("http://localhost:8080/");
    By loginMessage = By.id("loginLogoutMessage");
    wait.until(presenceOfElementLocated(loginMessage));
    WebElement loginElement = driver.findElement(loginMessage);
    String actualLoginText = loginElement.getText();

    // Guest user should expect to see login message
    assertEquals(
        "Homepage login message as guest user", "Login to help out a neighbor!", actualLoginText);

    By addTaskButton = By.id("addtaskbutton");
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    List<WebElement> addTaskButtonElement = driver.findElements(addTaskButton);

    // Add task button should be missing when user is not logged in
    assertTrue(
        "Add task button must not be present for guest users", addTaskButtonElement.isEmpty());

    By dashboardIcon = By.className("dashboard-icon");
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
    List<WebElement> dashboardIconsElement = driver.findElements(dashboardIcon);

    // Dashboard icon (userpage or admin page) buttons
    // should be missing when user is not logged in
    assertTrue(
        "Dashboard icons must not be present for guest users", dashboardIconsElement.isEmpty());

    By taskResultsMessage = By.id("no-tasks-message");
    wait.until(presenceOfElementLocated(taskResultsMessage));
    WebElement taskResultsMessageElement = driver.findElement(taskResultsMessage);

    // Message alerting user there are no tasks nearby should be displayed
    // assertTrue(
    //    "No tasks in neighborhood message should be displayed",
    //    taskResultsMessageElement.isDisplayed());

    loginElement.click();
    By emailInput = By.id("email");
    wait.until(presenceOfElementLocated(emailInput));
    WebElement emailInputElement = driver.findElement(emailInput);

    emailInputElement.clear();
    emailInputElement.sendKeys(USER_EMAIL);
    By loginButton = By.id("btn-login");
    wait.until(presenceOfElementLocated(loginButton));
    WebElement loginButtonElement = driver.findElement(loginButton);
    loginButtonElement.click();
    wait.until(urlContains("/account.jsp"));

    // User should now be logged in and redirected to the
    // account page to enter their user info
    assertTrue(driver.getCurrentUrl().contains("/account.jsp"));
    assertEquals("My Personal Info", driver.getTitle());

    By nicknameInput = By.id("nickname-input");
    wait.until(presenceOfElementLocated(nicknameInput));
    WebElement nicknameInputElement = driver.findElement(nicknameInput);
    nicknameInputElement.sendKeys(USER_NICKNAME);

    By addressInput = By.id("address-input");
    wait.until(presenceOfElementLocated(addressInput));
    WebElement addressInputElement = driver.findElement(addressInput);
    addressInputElement.sendKeys(USER_ADDRESS);

    By phoneInput = By.id("phone-input");
    wait.until(presenceOfElementLocated(phoneInput));
    WebElement phoneInputElement = driver.findElement(phoneInput);
    phoneInputElement.sendKeys(USER_PHONE);

    By submitButton = By.id("submit-button");
    wait.until(presenceOfElementLocated(submitButton));
    WebElement submitButtonElement = driver.findElement(submitButton);
    submitButtonElement.click();

    // After new user fills out user info, they should be redirected to userpage
    assertTrue("User in user profile page", driver.getCurrentUrl().contains("/user_profile.jsp"));
    assertEquals("My Account", driver.getTitle());

    By userpageLogoutMessage = By.id("log-out-link");
    wait.until(presenceOfElementLocated(userpageLogoutMessage));
    String actualyUPLogoutM = driver.findElement(userpageLogoutMessage).getText();
    // Userpage should show a custom logout message with user's nickname
    assertEquals(
        "Userpage login message as logged user", USER_NICKNAME + " | Logout", actualyUPLogoutM);

    By backToHome = By.id("backtohome");
    wait.until(presenceOfElementLocated(backToHome));
    driver.findElement(backToHome).click();
    wait.until(urlContains("/index.jsp"));

    By logoutMessage = By.id("login-logout");
    wait.until(presenceOfElementLocated(logoutMessage));
    WebElement logoutElement = driver.findElement(logoutMessage);
    String actualLogoutText = logoutElement.getText();

    // Homepage should show a custom logout message with user's nickname
    assertEquals(
        "Homepage login message as logged user", USER_NICKNAME + " | Logout", actualLogoutText);
  }

  @Test
  public void _02_Homepage_AsLoggedUser_AddTask() {
    driver.get("http://localhost:8080/");
    By logoutMessage = By.id("login-logout");
    wait.until(presenceOfElementLocated(logoutMessage));
    WebElement logoutElement = driver.findElement(logoutMessage);
    String actualLogoutText = logoutElement.getText();

    // Confirm that the user is still logged in
    assertEquals(USER_NICKNAME + " | Logout", actualLogoutText);

    Random random = new Random();

    String taskDetail = TASK_DETAIL + random.nextInt(1000);
    String rewardPoints = Integer.toString(random.nextInt(201));
    int categoryOptionIndex = random.nextInt(NUM_OF_CATEGORIES);

    addTask(taskDetail, rewardPoints, categoryOptionIndex);

    // User should be redirected to user profile page after adding a task
    assertTrue(
        "User should be in user profile page",
        driver.getCurrentUrl().contains("/user_profile.jsp"));
    taskCount++;

    verifyNewTaskUserPage(taskDetail);

    // TODO: After merging leonard's map branch, include test that shows
    // task in homepage as well.
  }

  @Test
  public void _03_UserPage_AsLoggedUser_AddTask() {
    driver.get("http://localhost:8080/user_profile.jsp");

    Random random = new Random();

    String taskDetail = TASK_DETAIL + random.nextInt(1000);
    String rewardPoints = Integer.toString(random.nextInt(201));
    int categoryOptionIndex = random.nextInt(NUM_OF_CATEGORIES);

    addTask(taskDetail, rewardPoints, categoryOptionIndex);

    // User should be redirected to user profile page after adding a task
    assertTrue(
        "User should be in user profile page",
        driver.getCurrentUrl().contains("/user_profile.jsp"));
    taskCount++;

    verifyNewTaskUserPage(taskDetail);

    // TODO: After merging leonard's map branch, include test that shows
    // task in homepage as well.
  }
}
