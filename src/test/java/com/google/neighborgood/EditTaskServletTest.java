// Copyright 2019 Google LLC
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

package com.google.neighborgood.servlets;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import java.io.*;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.http.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit test on the EditTaskServlet file */
@RunWith(JUnit4.class)
public final class EditTaskServletTest {

  /* Set up the test environment with Datastore and UserService and simulate the situation
   * that the user is logged in as admin, has a specific email and userID.
   */
  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
              new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig())
          .setEnvIsAdmin(true)
          .setEnvIsLoggedIn(true)
          .setEnvEmail("leonardzhang@google.com")
          .setEnvAuthDomain("1234567890")
          .setEnvAttributes(
              ImmutableMap.of(
                  "com.google.appengine.api.users.UserService.user_id_key", "1234567890"));

  private UserService userService;
  private DatastoreService ds;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private Entity userEntity;
  private Entity taskEntity;
  private Entity openEntity;
  private String keyString;
  private String openKeyString;
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalErr = System.err;

  @Before
  public void setUp() {
    helper.setUp();
    userService = UserServiceFactory.getUserService();
    ds = DatastoreServiceFactory.getDatastoreService();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    userEntity = new Entity("UserInfo", "1234567890");
    userEntity.setProperty("nickname", "Leonard");
    userEntity.setProperty("address", "xxx");
    userEntity.setProperty("phone", "xxx");
    userEntity.setProperty("email", "leonardzhang@google.com");
    userEntity.setProperty("userId", "1234567890");
    userEntity.setProperty("country", "US");
    userEntity.setProperty("zipcode", "15213");
    userEntity.setProperty("points", 0);
    ds.put(userEntity);

    taskEntity = new Entity("Task", userEntity.getKey());
    taskEntity.setProperty("detail", "Test task");
    taskEntity.setProperty("timestamp", 123);
    taskEntity.setProperty("reward", 50);
    taskEntity.setProperty("status", "IN PROGRESS");
    taskEntity.setProperty("Owner", "1234567890");
    taskEntity.setProperty("Helper", "1234567890");
    taskEntity.setProperty("Address", "xxx");
    taskEntity.setProperty("zipcode", "15213");
    taskEntity.setProperty("country", "US");
    taskEntity.setProperty("category", "Garden");
    ds.put(taskEntity);

    keyString = KeyFactory.keyToString(taskEntity.getKey());

    openEntity = new Entity("Task", userEntity.getKey());
    openEntity.setProperty("detail", "Open test task");
    openEntity.setProperty("timestamp", 123);
    openEntity.setProperty("reward", 50);
    openEntity.setProperty("status", "OPEN");
    openEntity.setProperty("Owner", "1234567890");
    openEntity.setProperty("Helper", "N/A");
    openEntity.setProperty("Address", "xxx");
    openEntity.setProperty("zipcode", "15213");
    openEntity.setProperty("country", "US");
    openEntity.setProperty("category", "Garden");
    ds.put(openEntity);

    openKeyString = KeyFactory.keyToString(openEntity.getKey());
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void notLoggedInEdgeCaseTest() throws IOException {
    // Simulate the situation that the user has not logged into the account
    helper =
        new LocalServiceTestHelper(
                new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig())
            .setEnvIsAdmin(false)
            .setEnvIsLoggedIn(false);
    helper.setUp();

    // Ensure that the user has not logged in yet
    assertFalse(userService.isUserLoggedIn());

    when(request.getParameter("task-id")).thenReturn(keyString);

    // Try to catch the error message sent by the EditTaskServlet
    System.setErr(new PrintStream(errContent));

    new EditTaskServlet().doPost(request, response);

    // EditTaskServlet should print an error message: "User must be logged in to edit a task"
    assertEquals("User must be logged in to edit a task\n", errContent.toString());

    errContent.reset();
    System.setErr(originalErr);

    // Now verify the error status of the response
    verify(response).setStatus(HttpServletResponse.SC_UNAUTHORIZED);
  }

  @Test
  public void helpOutDoPostTest() throws IOException, EntityNotFoundException {
    // Ensure that the user has already logged in
    assertTrue(userService.isUserLoggedIn());

    when(request.getParameter("task-id")).thenReturn(openKeyString);
    when(request.getParameter("action")).thenReturn("helpout");
    // Ensure that the parameter map of the request contains key "action
    Map<String, String[]> dummyReturn = new HashMap<>();
    dummyReturn.put("action", new String[] {"dummy1"});
    when(request.getParameterMap()).thenReturn(dummyReturn);

    // Send a POST request, which will change the status of the OPEN task to IN PROGRESS
    new EditTaskServlet().doPost(request, response);

    // Now the status of the task should be IN PROGRESS with a helper of ID 1234567890
    openEntity = ds.get(KeyFactory.stringToKey(openKeyString));
    assertEquals("IN PROGRESS", (String) openEntity.getProperty("status"));
    assertEquals("1234567890", (String) openEntity.getProperty("Helper"));
  }

  @Test
  public void editTaskDoPostTest() throws IOException, EntityNotFoundException {
    // Ensure that the user has already logged in
    assertTrue(userService.isUserLoggedIn());

    when(request.getParameter("task-id")).thenReturn(keyString);
    when(request.getParameter("reward-input")).thenReturn("150");
    when(request.getParameter("task-overview-input")).thenReturn("Task Overview");
    when(request.getParameter("task-detail-input")).thenReturn("Edit Test Task");
    when(request.getParameter("category-input")).thenReturn("Misc");

    // Send a POST request, which will change the task detail, reward and category
    new EditTaskServlet().doPost(request, response);

    // Check the stored information of the task
    taskEntity = ds.get(KeyFactory.stringToKey(keyString));
    assertEquals("Edit Test Task", (String) taskEntity.getProperty("detail"));
    assertEquals("IN PROGRESS", (String) taskEntity.getProperty("status"));
    assertEquals("1234567890", (String) taskEntity.getProperty("Owner"));
    assertEquals("1234567890", (String) taskEntity.getProperty("Helper"));
    assertEquals("Misc", (String) taskEntity.getProperty("category"));
    assertEquals(150, (long) taskEntity.getProperty("reward"));
  }

  @Test
  public void invalidRewardPtsTest() throws IOException, EntityNotFoundException {
    // First test the situation where the input reward is not a numeric value
    when(request.getParameter("task-id")).thenReturn(keyString);
    when(request.getParameter("reward-input")).thenReturn("abcdef");
    when(request.getParameter("task-overview-input")).thenReturn("Task Overview");
    when(request.getParameter("task-detail-input")).thenReturn("Edit Test Task");
    when(request.getParameter("category-input")).thenReturn("Misc");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    // Send a POST request, which should print "Please enter a valid integer in the range of 0-200"
    new EditTaskServlet().doPost(request, response);

    writer.flush();
    assertEquals(stringWriter.toString(), "Please enter a valid integer in the range of 0-200\n");

    // Now test the situation that the input numeric value of reward is out of range
    when(request.getParameter("reward-input")).thenReturn("201");
    stringWriter.getBuffer().setLength(0);

    // The response of the POST request should be the same
    new EditTaskServlet().doPost(request, response);

    writer.flush();
    assertEquals(stringWriter.toString(), "Please enter a valid integer in the range of 0-200\n");

    when(request.getParameter("reward-input")).thenReturn("-1");
    stringWriter.getBuffer().setLength(0);

    // The response of the POST request should be the same
    new EditTaskServlet().doPost(request, response);

    writer.flush();
    assertEquals(stringWriter.toString(), "Please enter a valid integer in the range of 0-200\n");
  }

  @Test
  public void emptyInputEdgeCaseTest() throws IOException, EntityNotFoundException {
    // First test the situation where the input task detail is empty
    when(request.getParameter("task-id")).thenReturn(keyString);
    when(request.getParameter("reward-input")).thenReturn("150");
    when(request.getParameter("task-overview-input")).thenReturn("Task Overview");
    when(request.getParameter("task-detail-input")).thenReturn("");
    when(request.getParameter("category-input")).thenReturn("Misc");

    // Try to catch the error message sent by the EditTaskServlet
    System.setErr(new PrintStream(errContent));

    new EditTaskServlet().doPost(request, response);

    // EditTaskServlet should print an error message: "User must be logged in to edit a task"
    assertEquals("The input task detail is empty\n", errContent.toString());

    errContent.reset();
    System.setErr(originalErr);

    // Now verify the error status of the response
    verify(response).setStatus(HttpServletResponse.SC_BAD_REQUEST);

    // Next, test the situation where the input category is empty
    when(request.getParameter("task-id")).thenReturn(keyString);
    when(request.getParameter("reward-input")).thenReturn("150");
    when(request.getParameter("task-detail-input")).thenReturn("Edit Test Task");
    when(request.getParameter("category-input")).thenReturn("");

    // Try to catch the error message sent by the EditTaskServlet
    System.setErr(new PrintStream(errContent));

    new EditTaskServlet().doPost(request, response);

    // EditTaskServlet should print an error message: "User must be logged in to edit a task"
    assertEquals("The task must have a category\n", errContent.toString());

    errContent.reset();
    System.setErr(originalErr);

    // Now verify that the error status has been set to 400 for two times
    verify(response, times(2)).setStatus(HttpServletResponse.SC_BAD_REQUEST);
  }
}
