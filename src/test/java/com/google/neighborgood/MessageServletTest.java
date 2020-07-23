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

/** Unit test on the UserInfoServlet file */
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
          .setEnvEmail("leonardzhang@google.com")
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

  @Before
  public void setUp() {
    helper.setUp();
    userService = UserServiceFactory.getUserService();
    ds = DatastoreServiceFactory.getDatastoreService();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
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

    when(request.getParameter("task-id")).thenReturn("1234567890");
    when(request.getParameter("msg")).thenReturn("Testing message");

    new MessageServlet().doPost(request, response);

    // After sending the POST request, there should be one entity in the datastore
    assertEquals(1, ds.prepare(new Query("Message")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("Message"));
    Entity entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored personal information
    assertEquals("Testing message", (String) entity.getProperty("message"));
    assertEquals("1234567890", (String) entity.getProperty("taskId"));
    assertEquals("1234567890", (String) entity.getProperty("sender"));
  }

  @Test
  public void testInsertMultipleMessage() throws IOException {
    // Check whether the datastore is empty before the test
    assertEquals(0, ds.prepare(new Query("Message")).countEntities(withLimit(10)));

    for (int i = 1; i < 11; i++) {
      String index = Integer.toString(i);
      when(request.getParameter("task-id")).thenReturn("1234567890" + index);
      when(request.getParameter("msg")).thenReturn("Testing message " + index);

      new MessageServlet().doPost(request, response);

      assertEquals(i, ds.prepare(new Query("Message")).countEntities(withLimit(10)));
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

    errContent.reset();
    System.setErr(originalErr);

    when(request.getParameter("task-id")).thenReturn("1234567890");

    // Now test the situation the message is not provided
    System.setErr(new PrintStream(errContent));

    new MessageServlet().doPost(request, response);

    // This will lead to the second error handling clause of doPost() in MessageServlet
    assertEquals("The message is not provided\n", errContent.toString());
    assertEquals(0, ds.prepare(new Query("Message")).countEntities(withLimit(10)));

    errContent.reset();
    System.setErr(originalErr);

    // Now test the situation the message is empty
    when(request.getParameter("msg")).thenReturn("   ");
    System.setErr(new PrintStream(errContent));

    new MessageServlet().doPost(request, response);

    // This will lead to the second error handling clause of doPost() in MessageServlet
    assertEquals("The input message is empty\n", errContent.toString());
    assertEquals(0, ds.prepare(new Query("Message")).countEntities(withLimit(10)));

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

    // Test the stored personal information
    assertEquals("Testing message", (String) entity.getProperty("message"));
    assertEquals("1234567890", (String) entity.getProperty("taskId"));
    assertEquals("1234567890", (String) entity.getProperty("sender"));
  }
}
