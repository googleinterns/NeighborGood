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

import static com.google.appengine.api.datastore.FetchOptions.Builder.withLimit;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import java.io.*;
import javax.servlet.http.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit test on the MessageServlet file */
@RunWith(JUnit4.class)
public final class MessageServletTest {

  /* Set up the test environment with Datastore and UserService and simulate the situation
   * that the user is logged in as admin, has a specific email and userID.
   */
  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
              new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig())
          .setEnvIsAdmin(true)
          .setEnvIsLoggedIn(true)
          .setEnvEmail("leo@xxx.com")
          .setEnvAuthDomain("1234567890")
          .setEnvAttributes(
              ImmutableMap.of(
                  "com.google.appengine.api.users.UserService.user_id_key", "1234567890"));

  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalErr = System.err;
  private UserService userService;
  private DatastoreService ds;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private Entity userEntity;
  private Entity taskEntity;
  private String keyString;

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
    userEntity.setProperty("email", "leo@xxx.com");
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
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testEnvironmentTest() {
    // Test the UserService feature
    assertTrue(userService.isUserAdmin());
    assertTrue(userService.isUserLoggedIn());

    // Test the DataStore feature
    assertEquals(0, ds.prepare(new Query("dummy")).countEntities(withLimit(10)));
    ds.put(new Entity("dummy"));
    ds.put(new Entity("dummy"));
    assertEquals(2, ds.prepare(new Query("dummy")).countEntities(withLimit(10)));
  }

  @Test
  public void testInsertOneMessage() throws IOException {
    // Check whether the datastore is empty before the test
    assertEquals(0, ds.prepare(new Query("Message")).countEntities(withLimit(10)));

    when(request.getParameter("task-id")).thenReturn(keyString);
    when(request.getParameter("msg")).thenReturn("Testing message");

    new MessageServlet().doPost(request, response);

    // After sending the POST request, there should be one entity in the datastore
    assertEquals(1, ds.prepare(new Query("Message")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("Message"));
    Entity entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored message information
    assertEquals("Testing message", (String) entity.getProperty("message"));
    assertEquals(keyString, (String) entity.getProperty("taskId"));
    assertEquals("1234567890", (String) entity.getProperty("sender"));

    // Also, there should be one corresponding notification entity created
    assertEquals(1, ds.prepare(new Query("Notification")).countEntities(withLimit(10)));
    results = ds.prepare(new Query("Notification"));
    entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored notification information
    assertEquals(keyString, (String) entity.getProperty("taskId"));
    assertEquals("1234567890", (String) entity.getProperty("receiver"));
  }

  @Test
  public void testInsertMultipleMessage() throws IOException {
    // Check whether the datastore is empty before the test
    assertEquals(0, ds.prepare(new Query("Message")).countEntities(withLimit(10)));

    for (int i = 1; i < 11; i++) {
      String index = Integer.toString(i);
      when(request.getParameter("task-id")).thenReturn(keyString);
      when(request.getParameter("msg")).thenReturn("Testing message " + index);

      new MessageServlet().doPost(request, response);

      assertEquals(i, ds.prepare(new Query("Message")).countEntities(withLimit(10)));
      assertEquals(i, ds.prepare(new Query("Notification")).countEntities(withLimit(10)));
    }
  }

  @Test
  public void testEmptyInputEdgeCase() throws IOException {
    // Check whether the datastore is empty before the test
    assertEquals(0, ds.prepare(new Query("Message")).countEntities(withLimit(10)));

    // First test the situation when task-id is not provided
    System.setErr(new PrintStream(errContent));

    new MessageServlet().doPost(request, response);

    // This will lead to the first error handling clause of the doPost function of MessageServlet
    assertEquals("The task id is not included\n", errContent.toString());
    assertEquals(0, ds.prepare(new Query("Message")).countEntities(withLimit(10)));
    assertEquals(0, ds.prepare(new Query("Notification")).countEntities(withLimit(10)));

    errContent.reset();
    System.setErr(originalErr);

    when(request.getParameter("task-id")).thenReturn(keyString);

    // Now test the situation the message is not provided
    System.setErr(new PrintStream(errContent));

    new MessageServlet().doPost(request, response);

    // This will lead to the second error handling clause of doPost() in MessageServlet
    assertEquals("The message is not provided\n", errContent.toString());
    assertEquals(0, ds.prepare(new Query("Message")).countEntities(withLimit(10)));
    assertEquals(0, ds.prepare(new Query("Notification")).countEntities(withLimit(10)));

    errContent.reset();
    System.setErr(originalErr);

    // Now test the situation the message is empty
    when(request.getParameter("msg")).thenReturn("   ");
    System.setErr(new PrintStream(errContent));

    new MessageServlet().doPost(request, response);

    // This will lead to the second error handling clause of doPost() in MessageServlet
    assertEquals("The input message is empty\n", errContent.toString());
    assertEquals(0, ds.prepare(new Query("Message")).countEntities(withLimit(10)));
    assertEquals(0, ds.prepare(new Query("Notification")).countEntities(withLimit(10)));

    errContent.reset();
    System.setErr(originalErr);

    // Now test the normal case
    when(request.getParameter("msg")).thenReturn("Testing message");

    new MessageServlet().doPost(request, response);

    // After sending the POST request, there should be one entity in the datastore
    assertEquals(1, ds.prepare(new Query("Message")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("Message"));
    Entity entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored message information
    assertEquals("Testing message", (String) entity.getProperty("message"));
    assertEquals(keyString, (String) entity.getProperty("taskId"));
    assertEquals("1234567890", (String) entity.getProperty("sender"));

    // Also, there should be one corresponding notification entity created
    assertEquals(1, ds.prepare(new Query("Notification")).countEntities(withLimit(10)));
    results = ds.prepare(new Query("Notification"));
    entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored notification information
    assertEquals(keyString, (String) entity.getProperty("taskId"));
    assertEquals("1234567890", (String) entity.getProperty("receiver"));
  }

  @Test
  public void doGetTest() throws IOException {
    // Check whether the datastore is empty before the test
    assertEquals(0, ds.prepare(new Query("Message")).countEntities(withLimit(10)));

    Entity dummy = new Entity("Message");
    dummy.setProperty("message", "Test 1");
    dummy.setProperty("taskId", "1");
    dummy.setProperty("sender", userService.getCurrentUser().getUserId());
    dummy.setProperty("sentTime", 0);

    Entity dummy2 = new Entity("Message");
    dummy2.setProperty("message", "Test 2");
    dummy2.setProperty("taskId", "1");
    dummy2.setProperty("sender", userService.getCurrentUser().getUserId());
    dummy2.setProperty("sentTime", 5);

    ds.put(dummy);
    ds.put(dummy2);

    when(request.getParameter("key")).thenReturn("1");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new MessageServlet().doGet(request, response);

    // After sending the GET request, the doGet function should output the json string
    writer.flush();
    System.out.println(stringWriter.toString());
    assertTrue(
        stringWriter
            .toString()
            .contains("{\"message\":\"Test 1\",\"className\":\"sentByMe\",\"sentTime\":0}"));
    assertTrue(
        stringWriter
            .toString()
            .contains("{\"message\":\"Test 2\",\"className\":\"sentByMe\",\"sentTime\":5}"));

    // Finally, ensure that the servlet file has set the content type to json
    verify(response).setContentType("application/json;");
  }

  @Test
  public void doGetWithoutTaskIdTest() throws IOException {
    // Check whether the datastore is empty before the test
    assertEquals(0, ds.prepare(new Query("Message")).countEntities(withLimit(10)));

    Entity dummy = new Entity("Message");
    dummy.setProperty("message", "Test 1");
    dummy.setProperty("taskId", "1");
    dummy.setProperty("sender", userService.getCurrentUser().getUserId());
    dummy.setProperty("sentTime", 0);

    Entity dummy2 = new Entity("Message");
    dummy2.setProperty("message", "Test 2");
    dummy2.setProperty("taskId", "1");
    dummy2.setProperty("sender", userService.getCurrentUser().getUserId());
    dummy2.setProperty("sentTime", 5);

    ds.put(dummy);
    ds.put(dummy2);

    // Now test the edge case that task id is not provided
    System.setErr(new PrintStream(errContent));

    new MessageServlet().doGet(request, response);

    // This will lead to the first error handling clause of doGet() in MessageServlet
    assertEquals("No task id provided\n", errContent.toString());
    assertEquals(2, ds.prepare(new Query("Message")).countEntities(withLimit(10)));

    errContent.reset();
    System.setErr(originalErr);
  }
}
