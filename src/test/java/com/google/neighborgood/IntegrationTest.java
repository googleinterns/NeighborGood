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
import static org.openqa.selenium.support.ui.ExpectedConditions.visibilityOf;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Ignore;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

@Ignore
@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {

  private static WebDriver driver;
  private static WebDriverWait fluentWait;
  // private static FluentWait<WebDriver> fluentwait;
  private static JavascriptExecutor js;

  private final String USER_NICKNAME = "Mafe";
  private final String USER_NICKNAME_HELPER = "Helper";
  private final String USER_EMAIL = "123456@example.com";
  private final String USER_EMAIL_HELPER = "helper@example.com";
  private final String USER_ADDRESS = "123 Street Name, City, ST";
  private final String USER_ZIPCODE = "59715";
  private final String USER_COUNTRY = "United States";
  private final String USER_PHONE = "1231231234";
  private final String TASK_DETAIL = "Help!";
  private final String[] TASK_CATEGORIES = {"garden", "shopping", "pets", "misc"};
  private static HashMap<String, String> recentTask = new HashMap<String, String>();
  private static int helperPoints = 0;

  @BeforeClass
  public static void setupClass() {
    WebDriverManager.chromedriver().setup();
    driver = new ChromeDriver();
    // ChromeOptions options = new ChromeOptions();
    // options.addArguments("--headless");
    // options.addArguments("--disable-gpu");
    // driver = new ChromeDriver(options);
    fluentWait =
        new WebDriverWait(driver, 20)
            .withTimeout(30, TimeUnit.SECONDS)
            .pollingEvery(1, TimeUnit.SECONDS)
            .ignoring(Exception.class);
    /**
     * fluentwait = new FluentWait<WebDriver>(driver) .withTimeout(30, TimeUnit.SECONDS)
     * .pollingEvery(1, TimeUnit.SECONDS) .ignoring(Exception.class);
     */
    js = (JavascriptExecutor) driver;
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
    fluentWait.until(presenceOfElementLocated(loginMessage));
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
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    List<WebElement> dashboardIconsElement = driver.findElements(dashboardIcon);

    // Dashboard icon (userpage or admin page) buttons
    // should be missing when user is not logged in
    assertTrue(
        "Dashboard icons must not be present for guest users", dashboardIconsElement.isEmpty());

    By taskResultsMessage = By.id("no-tasks-message");
    fluentWait.until(presenceOfElementLocated(taskResultsMessage));
    WebElement taskResultsMessageElement = driver.findElement(taskResultsMessage);
    fluentWait.until(visibilityOf(taskResultsMessageElement));

    // Message alerting user there are no tasks nearby should be displayed
    assertTrue(
        "No tasks in neighborhood message should be displayed",
        taskResultsMessageElement.isDisplayed());

    loginNewUser(USER_EMAIL, USER_NICKNAME, USER_ADDRESS, USER_PHONE, USER_ZIPCODE, USER_COUNTRY);

    // After new user fills out user info, they should be redirected to userpage
    assertTrue("User in user profile page", driver.getCurrentUrl().contains("/user_profile.jsp"));
    assertEquals("My Account", driver.getTitle());

    verifyLoggedUserUserPage(USER_NICKNAME);
    backToHome();
    verifyLoggedUserHomePage(USER_NICKNAME);
  }

  @Test
  public void _02_Homepage_AsLoggedUser_AddTask() {
    driver.get("http://localhost:8080/");

    // Confirm that the user is still logged in
    verifyLoggedUserHomePage(USER_NICKNAME);

    Random random = new Random();
    String taskDetail = TASK_DETAIL + random.nextInt(1000);
    String rewardPoints = Integer.toString(random.nextInt(201));
    int categoryOptionIndex = random.nextInt(TASK_CATEGORIES.length);
    String taskCategory = TASK_CATEGORIES[categoryOptionIndex];

    addTask(taskDetail, rewardPoints, categoryOptionIndex);

    // User should be redirected to user profile page after adding a task
    assertTrue(
        "User should be in user profile page",
        driver.getCurrentUrl().contains("/user_profile.jsp"));

    verifyNewTaskUserPage(taskDetail);
    backToHome();
    verifyNewTaskHomepage(taskDetail, taskCategory);
  }

  @Test
  public void _03_UserPage_AsLoggedUser_AddTask() {
    driver.get("http://localhost:8080/user_profile.jsp");

    Random random = new Random();
    String taskDetail = TASK_DETAIL + random.nextInt(1000);
    String rewardPoints = Integer.toString(random.nextInt(201));
    int categoryOptionIndex = random.nextInt(TASK_CATEGORIES.length);
    String taskCategory = TASK_CATEGORIES[categoryOptionIndex];

    addTask(taskDetail, rewardPoints, categoryOptionIndex);

    // User should be redirected to user profile page after adding a task
    assertTrue(
        "User should be in user profile page",
        driver.getCurrentUrl().contains("/user_profile.jsp"));

    verifyNewTaskUserPage(taskDetail);
    backToHome();
    verifyNewTaskHomepage(taskDetail, taskCategory);
  }

  @Test
  public void _04_Homepage_AsLoggedUser_LogOut() {
    // Logs out first from previous user session
    logOut("loginLogoutMessage");
    By loginMessage = By.id("loginLogoutMessage");
    fluentWait.until(presenceOfElementLocated(loginMessage));
    WebElement loginMessageElement = driver.findElement(loginMessage);
    assertEquals("Login to help out a neighbor!", loginMessageElement.getText());
  }

  @Test
  public void _05_Homepage_AsLoggedHelper_HelpOut() {
    loginNewUser(
        USER_EMAIL_HELPER,
        USER_NICKNAME_HELPER,
        USER_ADDRESS,
        USER_PHONE,
        USER_ZIPCODE,
        USER_COUNTRY);
    verifyLoggedUserUserPage(USER_NICKNAME_HELPER);
    backToHome();
    verifyLoggedUserHomePage(USER_NICKNAME_HELPER);
    helpOut();
    goToUserPage();
    goToOfferHelp();
    verifyOfferHelpTask();
  }

  @Test
  public void _06_Userpage_AsLoggedHelper_CompleteTask() {
    String taskMarkCompleteXPath = "//tbody[@id='offer-help-body']/tr[1]/td[4]/button";
    By markComplete = By.xpath(taskMarkCompleteXPath);
    fluentWait.until(presenceOfElementLocated(markComplete));
    WebElement markCompleteElement = driver.findElement(markComplete);
    js.executeScript("arguments[0].click();", markCompleteElement);
    for (int i = 0; i < 60; i++) {
      try {
        driver.switchTo().alert().accept();
        break;
      } catch (NoAlertPresentException e) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
      }
    }

    String taskCompletedXPath = "//tbody[@id='complete-task-body']/tr[1]";

    By taskDetails = By.xpath(taskCompletedXPath + "/td[1]");
    fluentWait.until(presenceOfElementLocated(taskDetails));
    assertEquals(recentTask.get("detail"), driver.findElement(taskDetails).getText());

    By taskStatus = By.xpath(taskCompletedXPath + "/td[2]");
    fluentWait.until(presenceOfElementLocated(taskStatus));
    assertEquals("COMPLETE: AWAIT VERIFICATION", driver.findElement(taskStatus).getText());

    By taskNeighbor = By.xpath(taskCompletedXPath + "/td[3]");
    fluentWait.until(presenceOfElementLocated(taskNeighbor));
    assertEquals(recentTask.get("nickname"), driver.findElement(taskNeighbor).getText());

    By taskPoints = By.xpath(taskCompletedXPath + "/td[4]");
    fluentWait.until(presenceOfElementLocated(taskPoints));
    assertEquals(recentTask.get("points"), driver.findElement(taskPoints).getText());
  }

  @Test
  public void _07_Userpage_AsLoggedUser_VerifyCompletedTask() {
    logOut("logout-href");
    loginUser(USER_EMAIL);
    fluentWait.until(urlContains("/user_profile.jsp"));

    String awaitVerifTaskXPath = "//tbody[@id='await-verif-body']/tr[1]";

    By taskDetail = By.xpath(awaitVerifTaskXPath + "/td[1]");
    fluentWait.until(presenceOfElementLocated(taskDetail));
    assertEquals(recentTask.get("detail"), driver.findElement(taskDetail).getText());

    By taskHelper = By.xpath(awaitVerifTaskXPath + "/td[2]");
    fluentWait.until(presenceOfElementLocated(taskHelper));
    assertEquals(recentTask.get("helper"), driver.findElement(taskHelper).getText());

    By taskStatus = By.xpath(awaitVerifTaskXPath + "/td[3]");
    fluentWait.until(presenceOfElementLocated(taskStatus));
    assertEquals("COMPLETE: AWAIT VERIFICATION", driver.findElement(taskStatus).getText());

    By verifyComplete = By.xpath(awaitVerifTaskXPath + "/td[4]/button");
    fluentWait.until(presenceOfElementLocated(verifyComplete));
    WebElement verifyCompleteElem = driver.findElement(verifyComplete);
    js.executeScript("arguments[0].click();", verifyCompleteElem);
    for (int i = 0; i < 60; i++) {
      try {
        driver.switchTo().alert().accept();
        break;
      } catch (NoAlertPresentException e) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
      }
    }
    fluentWait.until(presenceOfElementLocated(taskStatus));
    assertEquals("COMPLETE", driver.findElement(taskStatus).getText());

    helperPoints += Integer.parseInt(recentTask.get("points"));
  }

  @Test
  public void _08_Userpage_AsHelper_CompletedTask() {
    logOut("logout-href");
    loginUser(USER_EMAIL_HELPER);
    fluentWait.until(urlContains("/user_profile.jsp"));
    By points = By.id("points");
    fluentWait.until(presenceOfElementLocated(points));
    assertEquals(
        "My current points: " + Integer.toString(helperPoints) + "pts",
        driver.findElement(points).getText());
    goToOfferHelp();
    By completedTaskStatus = By.xpath("//tbody[@id='complete-task-body']/tr[1]/td[2]");
    fluentWait.until(presenceOfElementLocated(completedTaskStatus));
    assertEquals("COMPLETE", driver.findElement(completedTaskStatus).getText());
  }

  private static void clearAllDatastoreEntities(WebDriver driver) {
    // Clears datastore entities for start of test
    driver.get("http://localhost:8080/_ah/admin");
    fluentWait.until(urlContains("/_ah/admin"));
    By entityKindSelect = By.id("kind_input");
    fluentWait.until(presenceOfElementLocated(entityKindSelect));
    // Retrieves number of Entity Kind select options to iterate through them
    Select kindSelect = new Select(driver.findElement(entityKindSelect));
    List<WebElement> allEntityKinds = kindSelect.getOptions();
    By listButton;
    By allKeys;
    By deleteButton;
    for (int j = 1; j < allEntityKinds.size(); j++) {
      listButton = By.id("list_button");
      fluentWait.until(presenceOfElementLocated(listButton));
      WebElement listButtonElement = driver.findElement(listButton);
      js.executeScript("arguments[0].click();", listButtonElement);
      fluentWait.until(urlContains("/datastore?"));
      allKeys = By.id("allkeys");
      fluentWait.until(presenceOfElementLocated(allKeys));
      WebElement allKeysElement = driver.findElement(allKeys);
      js.executeScript("arguments[0].click();", allKeysElement);
      deleteButton = By.id("delete_button");
      fluentWait.until(presenceOfElementLocated(deleteButton));
      WebElement deleteButtonElement = driver.findElement(deleteButton);
      js.executeScript("arguments[0].click();", deleteButtonElement);
      for (int i = 0; i < 60; i++) {
        try {
          driver.switchTo().alert().accept();
          break;
        } catch (NoAlertPresentException e) {
          driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }
      }
    }
  }

  private void addTask(String details, String points, int categoryIndex) {
    recentTask.put("detail", details);
    recentTask.put("points", points);
    recentTask.put("category", TASK_CATEGORIES[categoryIndex]);
    recentTask.put("nickname", USER_NICKNAME);

    By addTaskButton = By.id("create-task-button");
    fluentWait.until(presenceOfElementLocated((addTaskButton)));
    WebElement addTaskButtonElement = driver.findElement(addTaskButton);
    addTaskButtonElement.click();

    By createTaskModal = By.id("createTaskModal");
    fluentWait.until(presenceOfElementLocated(createTaskModal));
    WebElement createTaskModalElement = driver.findElement(createTaskModal);
    fluentWait.until(visibilityOf(createTaskModalElement));

    // After clicking on the add task button, the modal should be displayed
    assertTrue("Create task modal should be displayed", createTaskModalElement.isDisplayed());

    js.executeScript("document.getElementById('task-detail-input').value='" + details + "';");
    js.executeScript("document.getElementById('rewarding-point-input').value='" + points + "';");
    js.executeScript(
        "document.getElementById('category-input').value='"
            + TASK_CATEGORIES[categoryIndex]
            + "';");

    By submitButton = By.id("submit-create-task");
    fluentWait.until(presenceOfElementLocated(submitButton));
    WebElement submitButtonElement = driver.findElement(submitButton);
    js.executeScript("arguments[0].click();", submitButtonElement);

    fluentWait.until(urlContains("/user_profile.jsp"));
  }

  private void verifyNewTaskUserPage(String expectedDetails) {
    // Verify that inputted task info is correctly displayed in need help table
    String taskRowXPath = "//table[@id='need-help']/tbody/tr[1]";
    By rowTaskDetails = By.xpath(taskRowXPath + "/td[1]");
    fluentWait.until(presenceOfElementLocated(rowTaskDetails));
    String rowDetailsActual = driver.findElement(rowTaskDetails).getText();
    assertEquals(expectedDetails, rowDetailsActual);

    By rowTaskHelper = By.xpath(taskRowXPath + "/td[2]");
    fluentWait.until(presenceOfElementLocated(rowTaskHelper));
    String rowHelperActual = driver.findElement(rowTaskHelper).getText();
    assertEquals("N/A", rowHelperActual);

    By rowTaskStatus = By.xpath(taskRowXPath + "/td[3]");
    fluentWait.until(presenceOfElementLocated(rowTaskStatus));
    String rowStatusActual = driver.findElement(rowTaskStatus).getText();
    assertEquals("OPEN", rowStatusActual);
  }

  private void verifyNewTaskHomepage(String expectedDetails, String expectedCategory) {
    String taskXPath = "//div[@id='tasks-list']/div[1]/div[2]";

    By taskDetails = By.xpath(taskXPath + "/div[2]");
    fluentWait.until(presenceOfElementLocated(taskDetails));
    String taskDetailsActual = driver.findElement(taskDetails).getText();
    assertEquals(expectedDetails, taskDetailsActual);

    By taskNickname = By.xpath(taskXPath + "/div[1]/div[1]");
    fluentWait.until(presenceOfElementLocated(taskNickname));
    String taskNicknameActual = driver.findElement(taskNickname).getText();
    assertEquals(USER_NICKNAME, taskNicknameActual);

    By taskCategory = By.xpath(taskXPath + "/div[3]/div[1]");
    fluentWait.until(presenceOfElementLocated(taskCategory));
    String taskCategoryActual = driver.findElement(taskCategory).getText();
    assertEquals("#" + expectedCategory, taskCategoryActual);
  }

  private void loginNewUser(
      String email, String nickname, String address, String phone, String zipcode, String country) {
    loginUser(email);
    fluentWait.until(urlContains("/account.jsp"));

    // User should now be logged in and redirected to the
    // account page to enter their user info
    assertTrue(driver.getCurrentUrl().contains("/account.jsp"));
    assertEquals("My Personal Info", driver.getTitle());

    js.executeScript("document.getElementById('nickname-input').value='" + nickname + "';");
    js.executeScript("document.getElementById('edit-address-input').value='" + address + "';");
    js.executeScript("document.getElementById('edit-zipcode-input').value='" + zipcode + "';");
    js.executeScript("document.getElementById('edit-country-input').value='" + country + "';");
    js.executeScript("document.getElementById('phone-input').value='" + phone + "';");

    By submitButton = By.id("submit-button");
    fluentWait.until(presenceOfElementLocated(submitButton));
    WebElement submitButtonElement = driver.findElement(submitButton);
    js.executeScript("arguments[0].click();", submitButtonElement);
    fluentWait.until(urlContains("/user_profile.jsp"));
  }

  private void loginUser(String email) {
    By loginMessage = By.id("loginLogoutMessage");
    fluentWait.until(presenceOfElementLocated(loginMessage));
    WebElement loginElement = driver.findElement(loginMessage);

    js.executeScript("arguments[0].click();", loginElement);

    fluentWait.until(urlContains("_ah/login?continue=%2Faccount.jsp"));
    js.executeScript("document.getElementById('email').value='" + email + "';");

    By loginButton = By.id("btn-login");
    fluentWait.until(presenceOfElementLocated(loginButton));
    WebElement loginButtonElement = driver.findElement(loginButton);
    js.executeScript("arguments[0].click();", loginButtonElement);
  }

  private void verifyLoggedUserUserPage(String nickname) {
    By logoutMessage = By.id("log-out-link");
    fluentWait.until(presenceOfElementLocated(logoutMessage));
    String logoutActualMessage = driver.findElement(logoutMessage).getText();
    // Userpage should show a custom logout message with user's nickname
    assertEquals(nickname + " | Logout", logoutActualMessage);
  }

  private void backToHome() {
    By backToHome = By.id("backtohome");
    WebElement backToHomeElement = driver.findElement(backToHome);
    js.executeScript("arguments[0].click();", backToHomeElement);
    fluentWait.until(urlContains("/index.jsp"));
  }

  private void goToUserPage() {
    By goToUserPage = By.xpath("//div[@id='dashboard-icon-container']/a");
    fluentWait.until(presenceOfElementLocated(goToUserPage));
    WebElement goToUserPageElement = driver.findElement(goToUserPage);
    js.executeScript("arguments[0].click();", goToUserPageElement);
    fluentWait.until(urlContains("/user_profile.jsp"));
  }

  private void goToOfferHelp() {
    By offerHelpButton = By.id("offer-help-button");
    fluentWait.until(presenceOfElementLocated(offerHelpButton));
    WebElement offerHelpElement = driver.findElement(offerHelpButton);
    js.executeScript("arguments[0].click();", offerHelpElement);
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  private void verifyLoggedUserHomePage(String nickname) {
    By logoutMessage = By.id("login-logout");
    fluentWait.until(presenceOfElementLocated(logoutMessage));
    String actualLogoutText = driver.findElement(logoutMessage).getText();

    // Homepage should show a custom logout message with user's nickname
    assertEquals(nickname + " | Logout", actualLogoutText);
  }

  private void helpOut() {

    // String[] expectedNicknameAndDetails = new String[2];

    String taskXPath = "//div[@id='tasks-list']/div[1]";

    By taskNeighborNickname = By.xpath(taskXPath + "/div[2]/div[1]/div[1]");
    fluentWait.until(presenceOfElementLocated(taskNeighborNickname));
    assertEquals(recentTask.get("nickname"), driver.findElement(taskNeighborNickname).getText());
    // expectedNicknameAndDetails[0] = driver.findElement(taskNeighborNickname).getText();

    By neighborTaskDetails = By.xpath(taskXPath + "/div[2]/div[2]");
    fluentWait.until(presenceOfElementLocated(neighborTaskDetails));
    assertEquals(recentTask.get("detail"), driver.findElement(neighborTaskDetails).getText());
    // expectedNicknameAndDetails[1] = driver.findElement(neighborTaskDetails).getText();

    By helpOutButton = By.xpath(taskXPath + "/div[2]/div[1]/div[2]");
    fluentWait.until(presenceOfElementLocated(helpOutButton));
    WebElement helpOutElement = driver.findElement(helpOutButton);
    js.executeScript("arguments[0].click();", helpOutElement);

    By confirmHelp = By.xpath(taskXPath + "/div[1]/a");
    fluentWait.until(presenceOfElementLocated(confirmHelp));
    WebElement confirmHelpElement = driver.findElement(confirmHelp);
    js.executeScript("arguments[0].click();", confirmHelpElement);
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    recentTask.put("helper", USER_NICKNAME_HELPER);
  }

  private void verifyOfferHelpTask() {
    String offerHelpRowXPath = "//tbody[@id='offer-help-body']/tr[1]";

    By taskDetails = By.xpath(offerHelpRowXPath + "/td[1]");
    fluentWait.until(presenceOfElementLocated(taskDetails));
    assertEquals(recentTask.get("detail"), driver.findElement(taskDetails).getText());
    By taskStatus = By.xpath(offerHelpRowXPath + "/td[2]");
    fluentWait.until(presenceOfElementLocated(taskStatus));
    assertEquals("IN PROGRESS", driver.findElement(taskStatus).getText());
    By taskNeighbor = By.xpath(offerHelpRowXPath + "/td[3]");
    fluentWait.until(presenceOfElementLocated(taskNeighbor));
    assertEquals(recentTask.get("nickname"), driver.findElement(taskNeighbor).getText());
  }

  private void logOut(String logoutId) {
    By logoutLink = By.id(logoutId);
    fluentWait.until(presenceOfElementLocated(logoutLink));
    WebElement logoutLinkElem = driver.findElement(logoutLink);
    js.executeScript("arguments[0].click();", logoutLinkElem);
    fluentWait.until(urlContains("/index.jsp"));
  }
}
