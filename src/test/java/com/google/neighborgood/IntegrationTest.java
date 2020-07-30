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
import java.util.Map;
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
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.FluentWait;

@RunWith(JUnit4.class)
@FixMethodOrder(MethodSorters.NAME_ASCENDING)
/* Due to how the interactions from one test build up onto the other as part of this integration test, the FixMethodOrder decorator is used to guarantee order of execution of tests. */
public class IntegrationTest {

  private static ChromeDriver driver;
  private static FluentWait<WebDriver> wait;
  private static JavascriptExecutor js;

  private final String USER_NICKNAME = "Mafe";
  private final String USER_NICKNAME_FARAWAY = "Faraway User";
  private final String USER_NICKNAME_HELPER = "Helper";
  private final String USER_EMAIL = "123456@example.com";
  private final String USER_EMAIL_HELPER = "helper@example.com";
  private final String USER_EMAIL_FARAWAY = "faraway@example.com";
  private final String USER_ADDRESS = "123 Street Name, City, ST";
  private final String USER_ZIPCODE = "90036";
  private final String USER_COUNTRY = "United States";
  private final String USER_LAT = "34.072984";
  private final String USER_LNG = "-118.349740";
  private final String FARAWAY_ZIPCODE = "59715";
  private final String FARAWAY_LAT = "45.681153";
  private final String FARAWAY_LNG = "-111.041873";
  private final String TASK_DETAIL =
      "Help! this is a detailed version of the task where I give a lot of random information";
  private final String TASK_OVERVIEW = "Help!";
  private final String[] TASK_CATEGORIES = {"garden", "shopping", "pets", "misc"};
  private static int helperPoints = 0;
  private static int userTaskCount = 0;
  private static int openTotalTaskCount = 0;
  // recentTask instance used to reference tasks throughout the tests
  private static HashMap<String, String> recentTask = new HashMap<String, String>();

  @BeforeClass
  /* Sets up test class by initializing driver, wait, js executor, and clearing all datastore entities */
  public static void setupClass() {
    WebDriverManager.chromedriver().setup();
    driver = new ChromeDriver();
    Map coordinates =
        new HashMap() {
          {
            put("latitude", 34.080634);
            put("longitude", -118.356463);
            put("accuracy", 1);
          }
        };
    driver.executeCdpCommand("Emulation.setGeolocationOverride", coordinates);
    // FluentWait set to timeout after 60 seconds of waiting for a WebElement to be returned and
    // polling every second
    wait =
        new FluentWait<WebDriver>(driver)
            .withTimeout(Duration.ofSeconds(20))
            .pollingEvery(Duration.ofSeconds(1))
            .ignoring(NoSuchElementException.class);

    js = (JavascriptExecutor) driver;
    clearAllDatastoreEntities();
  }

  @AfterClass
  public static void teardown() {
    if (driver != null) {
      driver.quit();
    }
  }

  @Test
  /* Tests functionality of a guest user who logs in for the first time in the site and has to input their new user info */
  public void _01_Homepage_AsNewGuestUser_LoginAndInputUserInfo() {
    driver.navigate().to("http://localhost:8080/");
    ifLaggingThenRefresh();
    String loginElement =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.id("loginLogoutMessage"));
                  }
                })
            .getText();

    // Guest user should expect to see login message
    assertEquals(
        "Homepage login message as guest user", "Login to help out a neighbor!", loginElement);

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
    boolean taskResultsMessageDisplayed =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.id("no-tasks-message"));
                  }
                })
            .isDisplayed();
    // Message alerting user there are no tasks nearby should be displayed since there are no tasks
    // yet in the site
    assertTrue("No tasks in neighborhood message should be displayed", taskResultsMessageDisplayed);

    loginNewUser(
        USER_EMAIL, USER_NICKNAME, USER_ADDRESS, USER_ZIPCODE, USER_COUNTRY, USER_LAT, USER_LNG);
    ifLaggingThenRefresh();

    // After new user fills out user info, they should be redirected to userpage
    assertTrue("User in user profile page", driver.getCurrentUrl().contains("/user_profile.jsp"));
    assertEquals("My Account", driver.getTitle());

    // Verifies logged user in userpage and then in homepage
    verifyLoggedUser(USER_NICKNAME, "log-out-link");
    backToHome();
    verifyLoggedUser(USER_NICKNAME, "login-logout");
  }

  @Test
  /* Tests functionality of adding tasks from the homepage */
  public void _02_Homepage_AsLoggedUser_AddTask() {
    // Confirm that the user is still logged in
    verifyLoggedUser(USER_NICKNAME, "login-logout");

    // Randomizes task contents
    Random random = new Random();
    String taskDetail = TASK_DETAIL + random.nextInt(1000);
    String taskOverview = TASK_OVERVIEW + random.nextInt(1000);
    String rewardPoints = Integer.toString(random.nextInt(201));
    int categoryOptionIndex = random.nextInt(TASK_CATEGORIES.length);
    String taskCategory = TASK_CATEGORIES[categoryOptionIndex];

    addTask(taskDetail, rewardPoints, categoryOptionIndex, taskOverview, USER_NICKNAME);

    // User should be redirected to user profile page after adding a task
    assertTrue(
        "User should be in user profile page",
        driver.getCurrentUrl().contains("/user_profile.jsp"));

    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    // Verifies that newly added task's details are correctly display in userpage's need help table
    verifyNewTaskUserPage();
    backToHome();
    ifLaggingThenRefresh();
    // Verifies that newly added tasks are displayed properply in homepage
    verifyNewTaskHomepage(openTotalTaskCount);
  }

  @Test
  /* Tests functionality of adding tasks from the userpage */
  public void _03_UserPage_AsLoggedUser_AddTask() {
    goToUserPage();
    ifLaggingThenRefresh();

    // Adds 8 tasks
    for (int i = 0; i < 8; i++) {
      // Randomizes task contents
      Random random = new Random();
      String taskDetail = TASK_DETAIL + random.nextInt(1000);
      String taskOverview = TASK_OVERVIEW + random.nextInt(1000);
      String rewardPoints = Integer.toString(random.nextInt(201));
      int categoryOptionIndex = random.nextInt(TASK_CATEGORIES.length);
      String taskCategory = TASK_CATEGORIES[categoryOptionIndex];

      addTask(taskDetail, rewardPoints, categoryOptionIndex, taskOverview, USER_NICKNAME);
    }

    // User should be redirected to user profile page after adding a task
    assertTrue(
        "User should be in user profile page",
        driver.getCurrentUrl().contains("/user_profile.jsp"));

    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    // Verifies that newly added task's details are correctly display in userpage's need help table
    verifyNewTaskUserPage();
    backToHome();
    ifLaggingThenRefresh();
    // Verifies that newly added tasks are displayed properply in homepage
    verifyNewTaskHomepage(openTotalTaskCount);
  }

  @Test
  /* Tests functionality of logging out */
  public void _04_Homepage_AsLoggedUser_LogOut() {
    // Logs out by using 'loginLogoutMessage' element id;
    logOut("loginLogoutMessage");
    String loginMessageElement =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.id("loginLogoutMessage"));
                  }
                })
            .getText();
    assertEquals("Login to help out a neighbor!", loginMessageElement);
  }

  @Test
  /* Tests functionality of a helper helping out with a task */
  public void _05_Homepage_AsLoggedHelper_HelpOut() {
    loginNewUser(
        USER_EMAIL_HELPER,
        USER_NICKNAME_HELPER,
        USER_ADDRESS,
        USER_ZIPCODE,
        USER_COUNTRY,
        USER_LAT,
        USER_LNG);
    ifLaggingThenRefresh();
    // Verifies logged user in userpage and then in homepage
    verifyLoggedUser(USER_NICKNAME_HELPER, "log-out-link");
    backToHome();
    ifLaggingThenRefresh();
    verifyLoggedUser(USER_NICKNAME_HELPER, "login-logout");
    helpOut();
    goToUserPage();
    ifLaggingThenRefresh();
    goToOfferHelp();
    // Verifies that after offering help with a task it displays correctly in the user page's offer
    // help table
    verifyOfferHelpTask();
  }

  @Test
  /* Tests functionality of having a helper mark a task as complete */
  public void _06_Userpage_AsLoggedHelper_CompleteTask() {
    completeTaskAsHelper();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    // Refreshes page due to flakiness of test caused by the partial refresh of the div in the page.
    // Without refreshing this often resulted in a a stale element error
    driver.navigate().refresh();
    goToOfferHelp();

    // Location of first task listed in the complete task table in userpage
    String taskCompletedXPath = "//tbody[@id='complete-task-body']/tr[1]";

    // Content targets that will be verified
    String[] taskContentsTarget = {"overview", "status", "nickname", "points"};

    // Iterates over task elements and compares the element's text with that of the stored
    // recentTask
    for (int i = 0; i < taskContentsTarget.length; i++) {
      String fullXPath = taskCompletedXPath + "/td[" + (i + 1) + "]";
      String taskContentItem =
          wait.until(
                  new Function<WebDriver, WebElement>() {
                    public WebElement apply(WebDriver driver) {
                      return driver.findElement(By.xpath(fullXPath));
                    }
                  })
              .getText();
      assertEquals(recentTask.get(taskContentsTarget[i]), taskContentItem);
    }
    // Opens up task details modal to verify its contents
    verifyTaskDetails(taskCompletedXPath + "/td[1]");
  }

  @Test
  /* Tests functionality of neighbor user verifying that helper did indeed complete a task */
  public void _07_Userpage_AsLoggedUser_VerifyCompletedTask() {
    // Logs out by using 'logout-href' element id;
    logOut("logout-href");
    loginUser(USER_EMAIL);
    ifLaggingThenRefresh();

    // Location of first task listed in the await verification table
    String awaitVerifTaskXPath = "//tbody[@id='await-verif-body']/tr[1]";

    // Content targets that will be verified
    String[] taskContentsTarget = {"overview", "helper", "status"};

    // Iterates over task elements and compares the element's text with that of the stored
    // recentTask
    for (int i = 0; i < taskContentsTarget.length; i++) {
      String fullXPath = awaitVerifTaskXPath + "/td[" + (i + 1) + "]";
      String taskContentItem =
          wait.until(
                  new Function<WebDriver, WebElement>() {
                    public WebElement apply(WebDriver driver) {
                      return driver.findElement(By.xpath(fullXPath));
                    }
                  })
              .getText();
      assertEquals(recentTask.get(taskContentsTarget[i]), taskContentItem);
    }
    // Opens up task details modal to verify its contents
    verifyTaskDetails(awaitVerifTaskXPath + "/td[1]");

    // click button to verify a task has been completed
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(awaitVerifTaskXPath + "/td[4]/button"));
              }
            })
        .click();

    // Clicking on verify button triggers an alert confirmation window
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
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    // Refreshes page due to flakiness of test caused by the partial refresh of the div in the page.
    // Without refreshing this often resulted in a a stale element error
    driver.navigate().refresh();
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);

    String taskStatusAfter =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.xpath(awaitVerifTaskXPath + "/td[3]"));
                  }
                })
            .getText();
    assertEquals(recentTask.get("status"), taskStatusAfter);

    // updates helper's total points
    helperPoints += Integer.parseInt(recentTask.get("points"));
    driver.manage().timeouts().implicitlyWait(10, TimeUnit.SECONDS);
  }

  @Test
  /* Verifies that the completed task shows as completed on the helper's user profile */
  public void _08_Userpage_AsHelper_CompletedTask() {
    logOut("logout-href");
    ifLaggingThenRefresh();
    loginUser(USER_EMAIL_HELPER);
    ifLaggingThenRefresh();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    // Helper's total points
    String points =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.id("points"));
                  }
                })
            .getText();
    assertEquals("My current points: " + Integer.toString(helperPoints) + "pts", points);
    goToOfferHelp();
    String completedTaskStatus =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(
                        By.xpath("//tbody[@id='complete-task-body']/tr[1]/td[2]"));
                  }
                })
            .getText();
    // Verifies that the task got updated to the COMPLETED status
    assertEquals(recentTask.get("status"), completedTaskStatus);
  }

  @Test
  /* Test functionality of disapproving a helper's completed task */
  public void _09_Userpage_AsLoggedUser_DisapproveTask() {
    backToHome();
    ifLaggingThenRefresh();
    updateRecentTask(); // updates the recent task we have saved with the most recent task in the
    // homepage
    helpOut();
    goToUserPage();
    ifLaggingThenRefresh();
    goToOfferHelp();
    completeTaskAsHelper();
    logOut("logout-href");
    ifLaggingThenRefresh();
    loginUser(USER_EMAIL);
    ifLaggingThenRefresh();

    // click on disapprove task button.
    // this is located in row 2 for this order of events but we probably need a better sorting
    // system for completed tasks so that it sorts by time of completion (making it easier to
    // find where the most recently approved/completed task is and easier to test in the future)
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(
                    By.xpath("//tbody[@id='await-verif-body']/tr[2]/td[5]/button"));
              }
            })
        .click();
    // Clicking disapprove triggers an alert confirmation window
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

    // Refreshes page due to flakiness of test caused by the partial refresh of the div in the page.
    // Without refreshing this often resulted in a a stale element error
    driver.navigate().refresh();

    // Content targets that will be verified
    String[] taskContentsTarget = {"overview", "helper", "status"};

    // Iterates over task elements and compares the element's text with that of the stored
    // recentTask
    for (int i = 0; i < taskContentsTarget.length; i++) {
      String fullXPath = "//tbody[@id='need-help-body']/tr[1]/td[" + (i + 1) + "]";
      String taskContentItem =
          wait.until(
                  new Function<WebDriver, WebElement>() {
                    public WebElement apply(WebDriver driver) {
                      return driver.findElement(By.xpath(fullXPath));
                    }
                  })
              .getText();
      assertEquals(recentTask.get(taskContentsTarget[i]), taskContentItem);
    }
    // Opens up task details modal to verify its contents
    verifyTaskDetails("//tbody[@id='need-help-body']/tr[1]/td[1]");
  }

  @Test
  /* Test functionality of a helper abandoning a task */
  public void _10_UserPage_AsHelper_AbandonTask() {
    logOut("logout-href");
    ifLaggingThenRefresh();
    loginUser(USER_EMAIL_HELPER);
    ifLaggingThenRefresh();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    goToOfferHelp();
    // clicks on abandon task button element
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(
                    By.xpath("//tbody[@id='offer-help-body']/tr/td[6]/button"));
              }
            })
        .click();
    // Clicking abandon triggers and alert confirmation window
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
    openTotalTaskCount++;
    backToHome();
    ifLaggingThenRefresh();
    // verifies that abandoned task is now the most recent task in homepage
    verifyNewTaskHomepage(openTotalTaskCount);
  }

  @Test
  /* Test functionality of a user scrolling to bottom of page to load more tasks  */
  public void _11_Homepage_ScollToBottom_LoadMoreTasks() throws InterruptedException {

    goToUserPage();
    ifLaggingThenRefresh();

    // First add more tasks to have more tasks to load on scroll, at the end there should be 14 open
    // tasks
    for (int i = openTotalTaskCount; i < 15; i++) {
      // Randomizes task contents
      Random random = new Random();
      String taskDetail = TASK_DETAIL + random.nextInt(1000);
      String taskOverview = TASK_OVERVIEW + random.nextInt(1000);
      String rewardPoints = Integer.toString(random.nextInt(201));
      int categoryOptionIndex = random.nextInt(TASK_CATEGORIES.length);
      String taskCategory = TASK_CATEGORIES[categoryOptionIndex];

      addTask(taskDetail, rewardPoints, categoryOptionIndex, taskOverview, USER_NICKNAME_HELPER);
    }

    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    backToHome();
    ifLaggingThenRefresh();

    // Before scrolling down only 10 tasks should have been fetched and therefore displayed
    List<WebElement> tasksBeforeScrolling =
        wait.until(
            new Function<WebDriver, List<WebElement>>() {
              public List<WebElement> apply(WebDriver driver) {
                return driver.findElements(By.xpath("//div[@class='task']"));
              }
            });
    assertEquals((long) 10, tasksBeforeScrolling.size());

    // Scrolls to the bottom of the page
    js.executeScript("window.scrollTo(0, document.body.scrollHeight)");
    Thread.sleep(3000);

    // After scrolling all open tasks added should have been fetched and therefore displayed
    List<WebElement> tasksAfterScrolling =
        wait.until(
            new Function<WebDriver, List<WebElement>>() {
              public List<WebElement> apply(WebDriver driver) {
                return driver.findElements(By.xpath("//div[@class='task']"));
              }
            });
    assertEquals((long) openTotalTaskCount, tasksAfterScrolling.size());
  }

  @Test
  /* Test functionality of a user searching tasks by a different location */
  public void _12_Homepage_AsFarawayUser_SearchByDiffLocation() throws InterruptedException {

    logOut("loginLogoutMessage");

    // logins and creates a new user with faraway user info (in this case using a 59715 zipcode)
    loginNewUser(
        USER_EMAIL_FARAWAY,
        USER_NICKNAME_FARAWAY,
        USER_ADDRESS,
        FARAWAY_ZIPCODE,
        USER_COUNTRY,
        FARAWAY_LAT,
        FARAWAY_LNG);

    // Adds three new tasks - these tasks will all be in 59715, US neighborhood
    for (int i = 0; i < 3; i++) {
      // Randomizes task contents
      Random random = new Random();
      String taskDetail = TASK_DETAIL + random.nextInt(1000);
      String taskOverview = TASK_OVERVIEW + random.nextInt(1000);
      String rewardPoints = Integer.toString(random.nextInt(201));
      int categoryOptionIndex = random.nextInt(TASK_CATEGORIES.length);
      String taskCategory = TASK_CATEGORIES[categoryOptionIndex];

      addTask(taskDetail, rewardPoints, categoryOptionIndex, taskOverview, USER_NICKNAME_FARAWAY);
    }
    backToHome();

    // Enters zipcode of a different neighborhood (59715) and waits for autocomplete suggestions to
    // load
    WebElement placeInput =
        wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("place-input"));
              }
            });
    placeInput.click();
    placeInput.sendKeys("59715");
    Thread.sleep(2000);

    // clicks first option from place autocomplete search box and waits for newly fetched tasks to
    // be displayed
    Actions act = new Actions(driver);
    act.moveToElement(placeInput).moveByOffset(20, 20).click().perform();
    Thread.sleep(2000);

    // verifies most recently added task and that the total expected displayed tasks is 3
    verifyNewTaskHomepage(3);
  }

  /* Clears entities from Datastore before test class */
  private static void clearAllDatastoreEntities() {
    driver.get("http://localhost:8080/_ah/admin");
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    // Number of entity pages that can be deleted
    Long numOfSelectOptions =
        (Long) js.executeScript("return document.getElementById('kind_input').options.length;");
    while (numOfSelectOptions > 1) {
      // clicks on list button
      wait.until(
              new Function<WebDriver, WebElement>() {
                public WebElement apply(WebDriver driver) {
                  return driver.findElement(By.id("list_button"));
                }
              })
          .click();
      driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
      // clicks on all keys button
      wait.until(
              new Function<WebDriver, WebElement>() {
                public WebElement apply(WebDriver driver) {
                  return driver.findElement(By.id("allkeys"));
                }
              })
          .click();
      // clicks on delete button
      wait.until(
              new Function<WebDriver, WebElement>() {
                public WebElement apply(WebDriver driver) {
                  return driver.findElement(By.id("delete_button"));
                }
              })
          .click();
      // Triggers an alert confirmation window
      // Driver will try to accept the alert to verify the task every second for a minute
      for (int i = 0; i < 60; i++) {
        try {
          driver.switchTo().alert().accept();
          break;
        } catch (NoAlertPresentException e) {
          driver.manage().timeouts().implicitlyWait(1, TimeUnit.SECONDS);
        }
      }
      numOfSelectOptions =
          (Long) js.executeScript("return document.getElementById('kind_input').options.length;");
    }
  }

  /* Adds task with provided details, points, and category index */
  private void addTask(
      String details, String points, int categoryIndex, String overview, String userNickname) {

    // Stores recentTask contents so tests can reference it
    recentTask.put("detail", details);
    recentTask.put("overview", overview);
    recentTask.put("points", points);
    recentTask.put("category", TASK_CATEGORIES[categoryIndex]);
    recentTask.put("nickname", userNickname);
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
    js.executeScript("document.getElementById('task-overview-input').value='" + overview + "';");
    js.executeScript("document.getElementById('reward-input').value='" + points + "';");
    js.executeScript(
        "document.getElementById('category-input').value='"
            + TASK_CATEGORIES[categoryIndex]
            + "';");
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    // clicks on submit create task button

    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("submit-create-task"));
              }
            })
        .click();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    userTaskCount++;
    openTotalTaskCount++;
  }

  /* Verifies that newly added task's details are correctly display in userpage's need help table */
  private void verifyNewTaskUserPage() {
    // Location of most recent task in user page's need help
    String taskRowXPath = "//table[@id='need-help']/tbody/tr[1]";

    // Content targets that will be verified
    String[] taskContentsTarget = {"overview", "helper", "status"};

    // Iterates over task elements and compares the element's text with that of the stored
    // recentTask
    for (int i = 0; i < taskContentsTarget.length; i++) {
      String fullXPath = taskRowXPath + "/td[" + (i + 1) + "]";
      String taskContentItem =
          wait.until(
                  new Function<WebDriver, WebElement>() {
                    public WebElement apply(WebDriver driver) {
                      return driver.findElement(By.xpath(fullXPath));
                    }
                  })
              .getText();
      assertEquals(recentTask.get(taskContentsTarget[i]), taskContentItem);
    }
    // Opens up task details modal to verify its contents
    verifyTaskDetails(taskRowXPath + "/td[1]");
    // Verifies the total number of tasks shown in the userpage against what they should be
    List<WebElement> notCompletedTasks =
        wait.until(
            new Function<WebDriver, List<WebElement>>() {
              public List<WebElement> apply(WebDriver driver) {
                return driver.findElements(By.xpath("//tbody[@id='need-help-body']/tr"));
              }
            });
    List<WebElement> completedTasks =
        wait.until(
            new Function<WebDriver, List<WebElement>>() {
              public List<WebElement> apply(WebDriver driver) {
                return driver.findElements(By.xpath("//tbody[@id='await-verif-body']/tr"));
              }
            });
    assertEquals(userTaskCount, notCompletedTasks.size() + completedTasks.size());
  }

  /* Verifies that newly added tasks are displayed properply in homepage */
  private void verifyNewTaskHomepage(int expectedNumOfTasksDisplayed) {

    // First task location in homepage
    String taskXPath = "//div[@id='tasks-list']/div[1]";

    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    String taskOverviewActual =
        (String)
            js.executeScript("return document.getElementsByClassName('task-content')[0].innerText");

    assertEquals(recentTask.get("overview"), taskOverviewActual);

    String taskNicknameActual =
        (String)
            js.executeScript(
                "return document.getElementsByClassName('user-nickname')[0].innerText");

    assertEquals(recentTask.get("nickname"), taskNicknameActual);

    String taskCategoryActual =
        (String)
            js.executeScript(
                "return document.getElementsByClassName('task-category')[0].innerText");

    assertEquals(recentTask.get("category"), taskCategoryActual.substring(1));
    // Opens up task details modal to verify its contents
    verifyTaskDetails(taskXPath);
    // Verifies the total number of tasks shown in the homepage against what they should be
    List<WebElement> tasks =
        wait.until(
            new Function<WebDriver, List<WebElement>>() {
              public List<WebElement> apply(WebDriver driver) {
                return driver.findElements(By.xpath("//div[@class='task']"));
              }
            });
    assertEquals(expectedNumOfTasksDisplayed, tasks.size());
  }

  /* Logs a new user in and provides the user info details to fill out in the form */
  private void loginNewUser(
      String email,
      String nickname,
      String address,
      String zipcode,
      String country,
      String lat,
      String lng) {
    loginUser(email);
    ifLaggingThenRefresh();
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
    js.executeScript("document.getElementById('lat-input').value='" + lat + "';");
    js.executeScript("document.getElementById('lng-input').value='" + lng + "';");

    // clicks on submit button
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("submit-button"));
              }
            })
        .click();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  /* Logs in users that already have their information saved (not new users) */
  private void loginUser(String email) {
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    // clicks on login link element
    for (int i = 0; i < 3; i++) {
      if (!driver.getCurrentUrl().contains("_ah/login")) {
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.id("loginLogoutMessage"));
                  }
                })
            .click();
        driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
      }
    }
    // enters user email
    js.executeScript("document.getElementById('email').value='" + email + "';");

    // clicks on login button
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("btn-login"));
              }
            })
        .click();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  /* Sends driver back to the homepage */
  private void backToHome() {
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    // clicks on back to home button
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("backtohome"));
              }
            })
        .click();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  /* Sends driver to User Page */
  private void goToUserPage() {
    // clicks on userpage button
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath("//div[@id='dashboard-icon-container']/a"));
              }
            })
        .click();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  /* Sends driver to the offer help table within the user page */
  private void goToOfferHelp() {
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    // clicks on offer help button
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("offer-help-button"));
              }
            })
        .click();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  /* Verifies that logged user's details are correctly displayed */
  private void verifyLoggedUser(String nickname, String elementId) {
    String logoutActualMessage =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.id(elementId));
                  }
                })
            .getText();
    // Userpage should show a custom logout message with user's nickname
    assertEquals(nickname + " | Logout", logoutActualMessage);
  }

  /* Function that has Helper claim a task from the homepage */
  private void helpOut() {
    String taskXPath = "//div[@id='tasks-list']/div[1]";

    String taskNeighborNickname =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.xpath(taskXPath + "/div[2]/div[1]/div[1]"));
                  }
                })
            .getText();
    assertEquals(recentTask.get("nickname"), taskNeighborNickname);

    String neighborTaskOverview =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.xpath(taskXPath + "/div[2]/div[2]"));
                  }
                })
            .getText();
    assertEquals(recentTask.get("overview"), neighborTaskOverview);

    // Opens up task details modal to verify its contents
    verifyTaskDetails(taskXPath);

    // clicks on offer help button
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[2]/div[1]/div[2]"));
              }
            })
        .click();

    // clicks on confirm help button
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXPath + "/div[1]/a"));
              }
            })
        .click();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    // Updates recentTask
    recentTask.put("helper", USER_NICKNAME_HELPER);
    recentTask.put("status", "IN PROGRESS");

    openTotalTaskCount--;
  }

  /* Verifies that after claiming a task/offering help with a task, that it displays correctly in the user page's offer help table */
  private void verifyOfferHelpTask() {
    // Location of most recent task in offer help table in userpage
    String offerHelpRowXPath = "//tbody[@id='offer-help-body']/tr[1]";

    // Content targets that will be verified
    String[] taskContentsTarget = {"overview", "status", "nickname"};
    // Iterates over task elements and compares the element's text with that of the stored
    // recentTask
    for (int i = 0; i < taskContentsTarget.length; i++) {
      String fullXPath = offerHelpRowXPath + "/td[" + (i + 1) + "]";
      String taskContentItem =
          wait.until(
                  new Function<WebDriver, WebElement>() {
                    public WebElement apply(WebDriver driver) {
                      return driver.findElement(By.xpath(fullXPath));
                    }
                  })
              .getText();
      assertEquals(recentTask.get(taskContentsTarget[i]), taskContentItem);
    }
    // Opens up task details modal to verify its contents
    verifyTaskDetails(offerHelpRowXPath + "/td[1]");
  }

  /*  Logs out user - takes logout link id as a parameter */
  private void logOut(String logoutId) {
    // clicks on logout button
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id(logoutId));
              }
            })
        .click();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
  }

  /* Updates the stored instance of recentTask with the most recent open task's contents displayed in the homepage */
  private void updateRecentTask() {
    recentTask.clear();

    // Most recent task location in homepage
    String taskXPath = "//div[@id='tasks-list']/div[1]/div[2]";

    String taskNickname =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.xpath(taskXPath + "/div[1]/div[1]"));
                  }
                })
            .getText();
    recentTask.put("nickname", taskNickname);

    String taskOverview =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.xpath(taskXPath + "/div[2]"));
                  }
                })
            .getText();
    recentTask.put("overview", taskOverview);

    String taskCategory =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.xpath(taskXPath + "/div[3]/div[1]"));
                  }
                })
            .getText();
    recentTask.put("category", taskCategory.substring(1));

    String taskDetail = getTaskDetails(taskXPath + "/div[2]");
    recentTask.put("detail", taskDetail);

    recentTask.put("status", "OPEN");
    recentTask.put("helper", "N/A");
  }

  /* Has helper mark a task as complete */
  private void completeTaskAsHelper() {
    // Location of most recent task in offer help table
    String taskMarkCompleteXPath = "//tbody[@id='offer-help-body']/tr[1]/td[5]/button";

    // clicks on mark task as complete button
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskMarkCompleteXPath));
              }
            })
        .click();
    // Triggers an alert confirmation window
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

  /* Attempts to auto refresh the page if the page is lagging */
  private void ifLaggingThenRefresh() {
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    while (js.executeScript("return document.readyState").equals("loading")) {
      driver.navigate().refresh();
      driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);
    }
  }

  /* clicks on task overview to open up task detail modal */
  private void verifyTaskDetails(String taskXpath) {
    String taskDetail = getTaskDetails(taskXpath);
    assertEquals(recentTask.get("detail"), taskDetail);
  }

  private String getTaskDetails(String taskXpath) {
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.xpath(taskXpath));
              }
            })
        .click();

    String taskDetail =
        wait.until(
                new Function<WebDriver, WebElement>() {
                  public WebElement apply(WebDriver driver) {
                    return driver.findElement(By.id("task-detail-container"));
                  }
                })
            .getText();

    // close modal
    wait.until(
            new Function<WebDriver, WebElement>() {
              public WebElement apply(WebDriver driver) {
                return driver.findElement(By.id("task-info-close-button"));
              }
            })
        .click();
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    return taskDetail;
  }
}
