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
  private static int helperPoints = 0;
  // recentTask instance used to reference tasks throughout the tests
  private static HashMap<String, String> recentTask = new HashMap<String, String>();

  @BeforeClass
  /**
   * Sets up test class by initializing driver, wait, js executor, and clearing all datastore
   * entities
   */
  public static void setupClass() {
    WebDriverManager.chromedriver().setup();
    driver = new ChromeDriver();
    // FluentWait set to timeout after 60 seconds of waiting for a WebElement to be returned and
    // polling every second
    wait =
        new FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(60))
            .pollingEvery(Duration.ofSeconds(1))
            .ignoring(NoSuchElementException.class);

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
  /**
   * Tests functionality of a guest user who logs in for the first time in the site and has to input
   * their new user info
   */
  public void _01_Homepage_AsNewGuestUser_LoginAndInputUserInfo() {
    driver.get("http://localhost:8080/");
    WebElement loginElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("loginLogoutMessage"));
              }
            });

    // Guest user should expect to see login message
    assertEquals(
        "Homepage login message as guest user",
        "Login to help out a neighbor!",
        loginElement.getText());

    By addTaskButton = By.id("addtaskbutton");
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    List<WebElement> addTaskButtonElement = driver.findElements(addTaskButton);
    // Add task button should be missing when user is not logged in
    assertTrue(
        "Add task button must not be present for guest users", addTaskButtonElement.isEmpty());

    By dashboardIcon = By.className("dashboard-icon");
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    List<WebElement> dashboardIconsElement = driver.findElements(dashboardIcon);
    // Dashboard icons should be missing when user is not logged in
    assertTrue(
        "Dashboard icons must not be present for guest users", dashboardIconsElement.isEmpty());

    // Element holding message displayed when there are no tasks
    WebElement taskResultsMessageElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("no-tasks-message"));
              }
            });
    // Message alerting user there are no tasks nearby should be displayed since there are no tasks
    // yet in the site
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
  /** Tests functionality of adding tasks from the homepage */
  public void _02_Homepage_AsLoggedUser_AddTask() {
    // Confirm that the user is still logged in
    verifyLoggedUserHomePage(USER_NICKNAME);

    // Randomizes task contents
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
  /** Tests functionality of adding tasks from the userpage */
  public void _03_UserPage_AsLoggedUser_AddTask() {
    goToUserPage();

    // Randomizes task contents
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
  /** Tests functionality of logging out */
  public void _04_Homepage_AsLoggedUser_LogOut() {
    // Logs out by using 'loginLogoutMessage' element id;
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
  /** Tests functionality of a helper helping out with a task */
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
  /** Tests functionality of having a helper mark a task as complete */
  public void _06_Userpage_AsLoggedHelper_CompleteTask() {
    completeTaskAsHelper();

    // Location of first task listed in the complete task table in userpage
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
  /** Tests functionality of neighbor user verifying that helper did indeed complete a task */
  public void _07_Userpage_AsLoggedUser_VerifyCompletedTask() {
    // Logs out by using 'logout-href' element id;
    logOut("logout-href");
    loginUser(USER_EMAIL);
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    // Location of first task listed in the await verification table
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

    // Button to verify a task has been completed
    WebElement verifyCompleteElem =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(awaitVerifTaskXPath + "/td[4]/button"));
              }
            });

    // Clicking on verify button triggers an alert confirmation window
    js.executeScript("arguments[0].click();", verifyCompleteElem);

    // Driver will try to accept the alert to verify the task every second for a minute
    for (int i = 0; i < 60; i++) {
      try {
        driver.switchTo().alert().accept();
        break;
      } catch (NoAlertPresentException e) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
      }
    }
    // update recent task
    recentTask.put("status", "COMPLETE");

    WebElement taskStatusAfter =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(awaitVerifTaskXPath + "/td[3]"));
              }
            });
    assertEquals(recentTask.get("status"), taskStatusAfter.getText());

    // updates helper's total points
    helperPoints += Integer.parseInt(recentTask.get("points"));
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }

  @Test
  /** Verifies that the completed task shows as completed on the helper's user profile */
  public void _08_Userpage_AsHelper_CompletedTask() {
    logOut("logout-href");
    loginUser(USER_EMAIL_HELPER);
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    // Helper's total points
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
    // Verifies that the task got updated to the COMPLETED status
    assertEquals(recentTask.get("status"), completedTaskStatus.getText());
  }

  @Test
  /** Test functionality of disapproving a helper's completed task */
  public void _09_Userpage_AsLoggedUser_DisapproveTask() {
    backToHome();
    updateRecentTask(); // updates the recent task we have saved with the most recent task in the
    // homepage
    helpOut();
    goToUserPage();
    goToOfferHelp();
    completeTaskAsHelper();
    logOut("logout-href");
    loginUser(USER_EMAIL);
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    // Disapprove task button.
    // this is located in row 2 for this order of events but we probably need a better sorting
    // system for completed tasks so that it sorts by time of completion (making it easier to
    // find where the most recently approved/completed task is and easier to test in the future)
    WebElement disapproveCompleteElem =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(
                    By.xpath("//tbody[@id='await-verif-body']/tr[2]/td[5]/button"));
              }
            });
    // Clicking disapprove triggers an alert confirmation window
    js.executeScript("arguments[0].click();", disapproveCompleteElem);
    // Driver will try to accept the alert to verify the task every second for a minute
    for (int i = 0; i < 60; i++) {
      try {
        driver.switchTo().alert().accept();
        break;
      } catch (NoAlertPresentException e) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
      }
    }
    // updates recentTask
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
  /** Test functionality of a helper abandoning a task */
  public void _10_UserPage_AsHelper_AbandonTask() {
    logOut("logout-href");
    loginUser(USER_EMAIL_HELPER);
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    goToOfferHelp();
    // Abandon task button element
    WebElement abandonTaskElem =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(
                    By.xpath("//tbody[@id='offer-help-body']/tr/td[6]/button"));
              }
            });
    // Clicking abandon triggers and alert confirmation window
    js.executeScript("arguments[0].click();", abandonTaskElem);
    // Driver will try to accept the alert to verify the task every second for a minute
    for (int i = 0; i < 60; i++) {
      try {
        driver.switchTo().alert().accept();
        break;
      } catch (NoAlertPresentException e) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
      }
    }
    // Updates recentTask
    recentTask.put("status", "OPEN");
    backToHome();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    // verifies that abandoned task is now the most recent task in homepage
    verifyNewTaskHomepage();
  }

  /** Clears entities from Datastore so `mvn clean` isn't necessary before test class */
  private static void clearAllDatastoreEntities(WebDriver driver) {
    driver.get("http://localhost:8080/_ah/admin");
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
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
    WebElement listButtonElement;
    WebElement allKeysElement;
    WebElement deleteButtonElement;
    for (int j = 1; j < allEntityKinds.size(); j++) {
      listButtonElement =
          wait.until(
              new Function<WebDriver, WebElement>() {
                public WebElement apply(WebDriver driver) {
                  return driver.findElement(By.id("list_button"));
                }
              });
      js.executeScript("arguments[0].click();", listButtonElement);
      driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
      allKeysElement =
          wait.until(
              new Function<WebDriver, WebElement>() {
                public WebElement apply(WebDriver driver) {
                  return driver.findElement(By.id("allkeys"));
                }
              });
      js.executeScript("arguments[0].click();", allKeysElement);
      deleteButtonElement =
          wait.until(
              new Function<WebDriver, WebElement>() {
                public WebElement apply(WebDriver driver) {
                  return driver.findElement(By.id("delete_button"));
                }
              });
      // Triggers an alert confirmation window
      js.executeScript("arguments[0].click();", deleteButtonElement);
      // Driver will try to accept the alert to verify the task every second for a minute
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

  /** Adds task with provided details, points, and category index */
  private void addTask(String details, String points, int categoryIndex) {

    // Stores recentTask contents so tests can reference it
    recentTask.put("detail", details);
    recentTask.put("points", points);
    recentTask.put("category", TASK_CATEGORIES[categoryIndex]);
    recentTask.put("nickname", USER_NICKNAME);
    recentTask.put("status", "OPEN");
    recentTask.put("helper", "N/A");

    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("create-task-button"));
              }
            })
        .click();

    boolean createTaskModalElementDisplayed =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.id("createTaskModal"));
                  }
                })
            .isDisplayed();

    // After clicking on the add task button, the modal should be displayed
    assertTrue("Create task modal should be displayed", createTaskModalElementDisplayed);

    // Inputs task details using Javascript Executor
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

    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  /**
   * Verifies that newly added task's details are correctly display in userpage's need help table
   */
  private void verifyNewTaskUserPage() {
    // Location of most recent task in user page's need help
    String taskRowXPath = "//table[@id='need-help']/tbody/tr[1]";

    WebElement rowTaskDetails =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskRowXPath + "/td[1]"));
              }
            });
    assertEquals(recentTask.get("detail"), rowTaskDetails.getText());

    WebElement rowHelper =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskRowXPath + "/td[2]"));
              }
            });
    assertEquals(recentTask.get("helper"), rowHelper.getText());

    WebElement rowStatus =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskRowXPath + "/td[3]"));
              }
            });
    assertEquals(recentTask.get("status"), rowStatus.getText());
  }

  /** Verifies that newly added tasks are displayed properply in homepage */
  private void verifyNewTaskHomepage() {

    // First task location in homepage
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

  /** Logs a new user in and provides the user info details to fill out in the form */
  private void loginNewUser(
      String email, String nickname, String address, String phone, String zipcode, String country) {
    loginUser(email);
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    // User should now be logged in and redirected to the
    // account page to enter their user info
    assertTrue("User should be in account page", driver.getCurrentUrl().contains("/account.jsp"));
    assertEquals("My Personal Info", driver.getTitle());

    // Uses JS executor to fill out user info form
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
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  /** Logs in users that already have their information saved (not new users) */
  private void loginUser(String email) {
    // login link element
    WebElement loginElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("loginLogoutMessage"));
              }
            });
    js.executeScript("arguments[0].click();", loginElement);

    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    // enters user email
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

  /** Sends driver back to the homepage */
  private void backToHome() {
    WebElement backToHomeElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("backtohome"));
              }
            });
    js.executeScript("arguments[0].click();", backToHomeElement);
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  /** Sends driver to User Page */
  private void goToUserPage() {
    WebElement goToUserPageElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath("//div[@id='dashboard-icon-container']/a"));
              }
            });
    js.executeScript("arguments[0].click();", goToUserPageElement);
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  /** Sends driver to the offer help table within the user page */
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

  /** Verifies that logged user's details are correctly displayed in userpage */
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

  /** Verifies that logged user's details are correctly displayed in homepage */
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

  /** Helper claims a task from the homepage */
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

    WebElement neighborTaskDetails =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[2]/div[2]"));
              }
            });
    assertEquals(recentTask.get("detail"), neighborTaskDetails.getText());

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

    // Updates recentTask
    recentTask.put("helper", USER_NICKNAME_HELPER);
    recentTask.put("status", "IN PROGRESS");
  }

  /**
   * Verifies that after claiming a task/offering help with a task, that it displays correctly in
   * the user page's offer help table
   */
  private void verifyOfferHelpTask() {
    // Location of most recent task in offer help table in userpage
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

  /** Logs out user - takes logout link id as a parameter */
  private void logOut(String logoutId) {
    WebElement logoutLinkElem =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id(logoutId));
              }
            });
    js.executeScript("arguments[0].click();", logoutLinkElem);
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  /**
   * Updates the stored instance of recentTask with the most recent open task's contents displayed
   * in the homepage
   */
  private void updateRecentTask() {
    recentTask.clear();

    // Most recent task location in homepage
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
    recentTask.put("helper", "N/A");
  }

  /** Has helper mark a task as complete */
  private void completeTaskAsHelper() {
    // Location of most recent task in offer help table
    String taskMarkCompleteXPath = "//tbody[@id='offer-help-body']/tr[1]/td[5]/button";

    WebElement markCompleteElement =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskMarkCompleteXPath));
              }
            });
    // Triggers an alert confirmation window
    js.executeScript("arguments[0].click();", markCompleteElement);
    // Driver will try to accept the alert to verify the task every second for a minute
    for (int i = 0; i < 60; i++) {
      try {
        driver.switchTo().alert().accept();
        break;
      } catch (NoAlertPresentException e) {
        driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
      }
    }
    // Updates recentTask
    recentTask.put("status", "COMPLETE: AWAIT VERIFICATION");
  }
}
