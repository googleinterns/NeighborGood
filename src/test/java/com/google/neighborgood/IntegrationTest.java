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
import static org.openqa.selenium.support.ui.ExpectedConditions.*;

import io.github.bonigarcia.wdm.WebDriverManager;
import java.time.Duration;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.function.Function;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.FixMethodOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.junit.runners.MethodSorters;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
public class IntegrationTest {

  private static WebDriver driver;
  private static FluentWait<WebDriver> wait;
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
    wait =
        new FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(60))
            .pollingEvery(Duration.ofSeconds(1))
            .ignoring(NoSuchElementException.class);
    ;

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
    WebElement loginElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("loginLogoutMessage"));
              }
            });
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

    WebElement taskResultsMessageElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("no-tasks-message"));
              }
            });

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

    verifyNewTaskUserPage();
    backToHome();
    verifyNewTaskHomepage();
  }

  @Test
  public void _03_UserPage_AsLoggedUser_AddTask() {
    goToUserPage();

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

    verifyNewTaskUserPage();
    backToHome();
    verifyNewTaskHomepage();
  }

  @Test
  public void _04_Homepage_AsLoggedUser_LogOut() {
    // Logs out first from previous user session
    logOut("loginLogoutMessage");
    WebElement loginMessageElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("loginLogoutMessage"));
              }
            });
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
    completeTaskAsHelper();

    String taskCompletedXPath = "//tbody[@id='complete-task-body']/tr[1]";

    WebElement taskDetails =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskCompletedXPath + "/td[1]"));
              }
            });
    assertEquals(recentTask.get("detail"), taskDetails.getText());

    WebElement taskStatus =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskCompletedXPath + "/td[2]"));
              }
            });
    assertEquals(recentTask.get("status"), taskStatus.getText());

    WebElement taskNeighbor =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskCompletedXPath + "/td[3]"));
              }
            });
    assertEquals(recentTask.get("nickname"), taskNeighbor.getText());

    WebElement taskPoints =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskCompletedXPath + "/td[4]"));
              }
            });
    assertEquals(recentTask.get("points"), taskPoints.getText());
  }

  @Test
  public void _07_Userpage_AsLoggedUser_VerifyCompletedTask() {
    logOut("logout-href");
    loginUser(USER_EMAIL);
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

    String awaitVerifTaskXPath = "//tbody[@id='await-verif-body']/tr[1]";

    WebElement taskDetail =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(awaitVerifTaskXPath + "/td[1]"));
              }
            });
    assertEquals(recentTask.get("detail"), taskDetail.getText());

    WebElement taskHelper =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(awaitVerifTaskXPath + "/td[2]"));
              }
            });
    assertEquals(recentTask.get("helper"), taskHelper.getText());

    WebElement taskStatus =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(awaitVerifTaskXPath + "/td[3]"));
              }
            });
    assertEquals(recentTask.get("status"), taskStatus.getText());

    WebElement verifyCompleteElem =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(awaitVerifTaskXPath + "/td[4]/button"));
              }
            });
    js.executeScript("arguments[0].click();", verifyCompleteElem);
    for (int i = 0; i < 60; i++) {
      try {
        driver.switchTo().alert().accept();
        break;
      } catch (NoAlertPresentException e) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
      }
    }
    recentTask.put("status", "COMPLETE");

    WebElement taskStatusAfter =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(awaitVerifTaskXPath + "/td[3]"));
              }
            });
    assertEquals(recentTask.get("status"), taskStatusAfter.getText());

    System.out.println("\n\n\n recent task points before adding: " + recentTask.get("points"));
    System.out.println("\n\n\n helperpoints before adding: " + helperPoints);
    helperPoints += Integer.parseInt(recentTask.get("points"));
    System.out.println("\n\n\n helperpoints after adding: " + helperPoints + "\n\n\n");
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  @Test
  public void _08_Userpage_AsHelper_CompletedTask() {
    logOut("logout-href");
    loginUser(USER_EMAIL_HELPER);
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

    WebElement points =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("points"));
              }
            });
    assertEquals("My current points: " + Integer.toString(helperPoints) + "pts", points.getText());
    goToOfferHelp();
    WebElement completedTaskStatus =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(
                    By.xpath("//tbody[@id='complete-task-body']/tr[1]/td[2]"));
              }
            });
    assertEquals(recentTask.get("status"), completedTaskStatus.getText());
  }

  @Test
  public void _09_Userpage_AsLoggedUser_DisapproveTask() {
    backToHome();
    updateRecentTask();
    helpOut();
    goToUserPage();
    goToOfferHelp();
    completeTaskAsHelper();
    logOut("logout-href");
    loginUser(USER_EMAIL);
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

    // this is row 2 for now but we probably need a better sorting system for completed tasks
    // so that it sorts by time of completion (makes it easier to find and test)
    WebElement disapproveCompleteElem =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(
                    By.xpath("//tbody[@id='await-verif-body']/tr[2]/td[5]/button"));
              }
            });
    js.executeScript("arguments[0].click();", disapproveCompleteElem);
    for (int i = 0; i < 60; i++) {
      try {
        driver.switchTo().alert().accept();
        break;
      } catch (NoAlertPresentException e) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
      }
    }
    recentTask.put("status", "IN PROGRESS");

    WebElement taskStatus =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath("//tbody[@id='need-help-body']/tr[1]/td[3]"));
              }
            });
    assertEquals(recentTask.get("status"), taskStatus.getText());

    WebElement taskDetail =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath("//tbody[@id='need-help-body']/tr[1]/td[1]"));
              }
            });
    assertEquals(recentTask.get("detail"), taskDetail.getText());

    WebElement taskHelper =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath("//tbody[@id='need-help-body']/tr[1]/td[2]"));
              }
            });
    assertEquals(recentTask.get("helper"), taskHelper.getText());
  }

  @Test
  public void _10_UserPage_AsHelper_AbandonTask() {
    logOut("logout-href");
    loginUser(USER_EMAIL_HELPER);
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
    goToOfferHelp();
    WebElement abandonTaskElem =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(
                    By.xpath("//tbody[@id='offer-help-body']/tr/td[6]/button"));
              }
            });
    js.executeScript("arguments[0].click();", abandonTaskElem);
    for (int i = 0; i < 60; i++) {
      try {
        driver.switchTo().alert().accept();
        break;
      } catch (NoAlertPresentException e) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
      }
    }
    recentTask.put("status", "OPEN");
    backToHome();
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
    verifyNewTaskHomepage();
  }

  private static void clearAllDatastoreEntities(WebDriver driver) {
    // Clears datastore entities for start of test
    driver.get("http://localhost:8080/_ah/admin");
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
    WebElement entityKindSelect =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("kind_input"));
              }
            });
    // Retrieves number of Entity Kind select options to iterate through them
    Select kindSelect = new Select(entityKindSelect);
    List<WebElement> allEntityKinds = kindSelect.getOptions();
    By listButton;
    By allKeys;
    By deleteButton;
    for (int j = 1; j < allEntityKinds.size(); j++) {
      WebElement listButtonElement =
          wait.until(
              new Function<WebDriver, WebElement>() {
                public WebElement apply(WebDriver driver) {
                  return driver.findElement(By.id("list_button"));
                }
              });
      js.executeScript("arguments[0].click();", listButtonElement);
      driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
      WebElement allKeysElement =
          wait.until(
              new Function<WebDriver, WebElement>() {
                public WebElement apply(WebDriver driver) {
                  return driver.findElement(By.id("allkeys"));
                }
              });
      js.executeScript("arguments[0].click();", allKeysElement);
      WebElement deleteButtonElement =
          wait.until(
              new Function<WebDriver, WebElement>() {
                public WebElement apply(WebDriver driver) {
                  return driver.findElement(By.id("delete_button"));
                }
              });
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
    recentTask.put("status", "OPEN");
    recentTask.put("helper", "N/A");

    WebElement addTaskButtonElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("create-task-button"));
              }
            });
    addTaskButtonElement.click();

    WebElement createTaskModalElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("createTaskModal"));
              }
            });
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

    // After clicking on the add task button, the modal should be displayed
    assertTrue("Create task modal should be displayed", createTaskModalElement.isDisplayed());

    js.executeScript("document.getElementById('task-detail-input').value='" + details + "';");
    js.executeScript("document.getElementById('rewarding-point-input').value='" + points + "';");
    js.executeScript(
        "document.getElementById('category-input').value='"
            + TASK_CATEGORIES[categoryIndex]
            + "';");

    WebElement submitButtonElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("submit-create-task"));
              }
            });
    js.executeScript("arguments[0].click();", submitButtonElement);

    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
  }

  private void verifyNewTaskUserPage() {
    // Verify that inputted task info is correctly displayed in need help table
    String taskRowXPath = "//table[@id='need-help']/tbody/tr[1]";
    WebElement rowTaskDetails =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskRowXPath + "/td[1]"));
              }
            });
    assertEquals(recentTask.get("detail"), rowTaskDetails.getText());

    WebElement rowHelperActual =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskRowXPath + "/td[2]"));
              }
            });
    assertEquals(recentTask.get("helper"), rowHelperActual.getText());

    WebElement rowStatusActual =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskRowXPath + "/td[3]"));
              }
            });
    assertEquals(recentTask.get("status"), rowStatusActual.getText());
  }

  private void verifyNewTaskHomepage() {
    String taskXPath = "//div[@id='tasks-list']/div[1]/div[2]";

    WebElement taskDetailsActual =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[2]"));
              }
            });
    assertEquals(recentTask.get("detail"), taskDetailsActual.getText());

    WebElement taskNicknameActual =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[1]/div[1]"));
              }
            });
    assertEquals(recentTask.get("nickname"), taskNicknameActual.getText());

    WebElement taskCategoryActual =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[3]/div[1]"));
              }
            });
    assertEquals("#" + recentTask.get("category"), taskCategoryActual.getText());
  }

  private void loginNewUser(
      String email, String nickname, String address, String phone, String zipcode, String country) {
    loginUser(email);
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);

    // User should now be logged in and redirected to the
    // account page to enter their user info
    assertTrue(driver.getCurrentUrl().contains("/account.jsp"));
    assertEquals("My Personal Info", driver.getTitle());

    js.executeScript("document.getElementById('nickname-input').value='" + nickname + "';");
    js.executeScript("document.getElementById('edit-address-input').value='" + address + "';");
    js.executeScript("document.getElementById('edit-zipcode-input').value='" + zipcode + "';");
    js.executeScript("document.getElementById('edit-country-input').value='" + country + "';");
    js.executeScript("document.getElementById('phone-input').value='" + phone + "';");

    WebElement submitButtonElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("submit-button"));
              }
            });
    js.executeScript("arguments[0].click();", submitButtonElement);
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
  }

  private void loginUser(String email) {
    WebElement loginElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("loginLogoutMessage"));
              }
            });
    js.executeScript("arguments[0].click();", loginElement);

    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
    js.executeScript("document.getElementById('email').value='" + email + "';");

    WebElement loginButtonElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("btn-login"));
              }
            });
    js.executeScript("arguments[0].click();", loginButtonElement);
  }

  private void verifyLoggedUserUserPage(String nickname) {
    WebElement logoutActualMessage =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("log-out-link"));
              }
            });
    // Userpage should show a custom logout message with user's nickname
    assertEquals(nickname + " | Logout", logoutActualMessage.getText());
  }

  private void backToHome() {
    WebElement backToHomeElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("backtohome"));
              }
            });
    js.executeScript("arguments[0].click();", backToHomeElement);
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
  }

  private void goToUserPage() {
    WebElement goToUserPageElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath("//div[@id='dashboard-icon-container']/a"));
              }
            });
    js.executeScript("arguments[0].click();", goToUserPageElement);
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
  }

  private void goToOfferHelp() {
    WebElement offerHelpElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("offer-help-button"));
              }
            });
    js.executeScript("arguments[0].click();", offerHelpElement);
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  private void verifyLoggedUserHomePage(String nickname) {
    WebElement actualLogoutText =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("login-logout"));
              }
            });

    // Homepage should show a custom logout message with user's nickname
    assertEquals(nickname + " | Logout", actualLogoutText.getText());
  }

  private void helpOut() {
    String taskXPath = "//div[@id='tasks-list']/div[1]";

    WebElement taskNeighborNickname =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[2]/div[1]/div[1]"));
              }
            });
    assertEquals(recentTask.get("nickname"), taskNeighborNickname.getText());
    // expectedNicknameAndDetails[0] = driver.findElement(taskNeighborNickname).getText();

    WebElement neighborTaskDetails =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[2]/div[2]"));
              }
            });
    assertEquals(recentTask.get("detail"), neighborTaskDetails.getText());
    // expectedNicknameAndDetails[1] = driver.findElement(neighborTaskDetails).getText();

    WebElement helpOutElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[2]/div[1]/div[2]"));
              }
            });
    js.executeScript("arguments[0].click();", helpOutElement);

    WebElement confirmHelpElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[1]/a"));
              }
            });
    js.executeScript("arguments[0].click();", confirmHelpElement);
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    recentTask.put("helper", USER_NICKNAME_HELPER);
    recentTask.put("status", "IN PROGRESS");
  }

  private void verifyOfferHelpTask() {
    String offerHelpRowXPath = "//tbody[@id='offer-help-body']/tr[1]";

    WebElement taskDetails =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(offerHelpRowXPath + "/td[1]"));
              }
            });
    assertEquals(recentTask.get("detail"), taskDetails.getText());

    WebElement taskStatus =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(offerHelpRowXPath + "/td[2]"));
              }
            });
    assertEquals(recentTask.get("status"), taskStatus.getText());

    WebElement taskNeighbor =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(offerHelpRowXPath + "/td[3]"));
              }
            });
    assertEquals(recentTask.get("nickname"), taskNeighbor.getText());
  }

  private void logOut(String logoutId) {
    WebElement logoutLinkElem =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id(logoutId));
              }
            });
    js.executeScript("arguments[0].click();", logoutLinkElem);
    driver.manage().timeouts().implicitlyWait(15, TimeUnit.SECONDS);
  }

  private void updateRecentTask() {
    recentTask.clear();
    String taskXPath = "//div[@id='tasks-list']/div[1]/div[2]";

    WebElement taskNickname =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[1]/div[1]"));
              }
            });
    recentTask.put("nickname", taskNickname.getText());

    WebElement taskDetail =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[2]"));
              }
            });
    recentTask.put("detail", taskDetail.getText());

    WebElement taskCategory =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[3]/div[1]"));
              }
            });
    recentTask.put("category", taskCategory.getText().substring(1));

    recentTask.put("status", "OPEN");
  }

  private void completeTaskAsHelper() {
    String taskMarkCompleteXPath = "//tbody[@id='offer-help-body']/tr[1]/td[5]/button";
    WebElement markCompleteElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskMarkCompleteXPath));
              }
            });
    js.executeScript("arguments[0].click();", markCompleteElement);
    for (int i = 0; i < 60; i++) {
      try {
        driver.switchTo().alert().accept();
        break;
      } catch (NoAlertPresentException e) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
      }
    }
    recentTask.put("status", "COMPLETE: AWAIT VERIFICATION");
  }
}
