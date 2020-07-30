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
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
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

@RunWith(JUnit4.class)
public final class AdminPageTest {

  /* Set up the test environment with Datastore and UserService and simulate the situation
   * that the user is logged in as admin, has a specific email and userID.
   */
  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
              new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig())
          .setEnvIsAdmin(true)
          .setEnvIsLoggedIn(true)
          .setEnvEmail("denzilbilson@google.com")
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
    System.setErr(new PrintStream(errContent));
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    userEntity = new Entity("UserInfo", "1234567890");
    userEntity.setProperty("nickname", "Denzil");
    userEntity.setProperty("address", "xxxxxx");
    userEntity.setProperty("phone", "xxxxxxxx");
    userEntity.setProperty("email", "denzilbilson@google.com");
    userEntity.setProperty("userId", "1234567890");
    userEntity.setProperty("country", "US");
    userEntity.setProperty("zipcode", "80017");
    userEntity.setProperty("points", 0);
    ds.put(userEntity);

    // create Entity to be edited
    Entity taskEntity = new Entity("Task", userEntity.getKey());
    taskEntity.setProperty("detail", "This is the original task details");
    taskEntity.setProperty("overview", "This is the task overview");
    taskEntity.setProperty("timestamp", 12346567);
    taskEntity.setProperty("reward", 50);
    taskEntity.setProperty("status", "IN PROGRESS");
    taskEntity.setProperty("Owner", "1234567890");
    taskEntity.setProperty("Helper", "1234567890");
    taskEntity.setProperty("Address", "xxxxxx");
    taskEntity.setProperty("zipcode", "80017");
    taskEntity.setProperty("country", "US");
    taskEntity.setProperty("category", "misc");
    ds.put(taskEntity);

    keyString = (String) KeyFactory.keyToString(taskEntity.getKey());
  }

  @After
  public void tearDown() {
    System.setErr(originalErr);
    helper.tearDown();
  }

  // Test Datastore and Entities
  @Test
  public void TestDatastore() {
    // Check if Datastore is empty before test
    assertEquals(0, ds.prepare(new Query("TestQuery")).countEntities());

    // Check if Datastore adds new Entities
    ds.put(new Entity("TestQuery"));
    ds.put(new Entity("TestQuery"));
    ds.put(new Entity("TestQuery"));
    ds.put(new Entity("TestQuery"));
    ds.put(new Entity("TestQuery"));
    assertEquals(5, ds.prepare(new Query("TestQuery")).countEntities());

    // Check if Limits work
    assertEquals(3, ds.prepare(new Query("TestQuery")).countEntities(withLimit(3)));

    // Query random non-existent entity
    assertEquals(0, ds.prepare(new Query("random")).countEntities());
  }

  @Test
  public void testSubmitPost() throws IOException {
    // Check whether the datastore contains the single entity
    assertEquals(1, ds.prepare(new Query("Task")).countEntities(withLimit(10)));

    // set responses
    when(request.getParameter("task-id")).thenReturn(keyString);
    when(request.getParameter("task-detail-input")).thenReturn("Can someone buy me groceries?");
    when(request.getParameter("edit-category-input")).thenReturn("shopping");
    when(request.getParameter("reward-input")).thenReturn("200");

    new AdminPage().doPost(request, response);

    // After sending the POST request, there should still be one entity in the datastore
    assertEquals(1, ds.prepare(new Query("Task")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("Task"));
    Entity entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored task information
    assertEquals(keyString, (String) KeyFactory.keyToString(entity.getKey()));
    assertEquals("Can someone buy me groceries?", (String) entity.getProperty("detail"));
    assertEquals("shopping", (String) entity.getProperty("category"));
    assertEquals(200, (long) entity.getProperty("reward"));
  }

  @Test
  public void testSubmitPostDetails() throws IOException, EntityNotFoundException {
    // test when tasks detail input is empty
    when(request.getParameter("task-id")).thenReturn(keyString);
    when(request.getParameter("reward-input")).thenReturn("150");
    when(request.getParameter("task-detail-input")).thenReturn("");
    when(request.getParameter("edit-category-input")).thenReturn("Misc");

    new AdminPage().doPost(request, response);
    assertEquals("The input task detail is empty\n", errContent.toString());
  }

  @Test
  public void testSubmitPostCategory() throws IOException, EntityNotFoundException {
    // test when tasks detail input is empty
    when(request.getParameter("task-id")).thenReturn(keyString);
    when(request.getParameter("reward-input")).thenReturn("150");
    when(request.getParameter("task-detail-input")).thenReturn("Random text over here");
    when(request.getParameter("edit-category-input")).thenReturn("");

    new AdminPage().doPost(request, response);
    assertEquals("The task must have a category\n", errContent.toString());
  }

  @Test
  public void testSubmitPostReward() throws IOException, EntityNotFoundException {
    // test when reward input is empty
    when(request.getParameter("task-id")).thenReturn(keyString);
    when(request.getParameter("reward-input")).thenReturn("");
    when(request.getParameter("task-detail-input")).thenReturn("Random text over here");
    when(request.getParameter("category-input")).thenReturn("shopping");

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new AdminPage().doPost(request, response);
    writer.flush();
    assertEquals(stringWriter.toString(), "Please enter a valid integer in the range of 0-200\n");

    // test when reward input is not a numerical value
    when(request.getParameter("reward-input")).thenReturn("Random text over here");
    stringWriter.getBuffer().setLength(0);
    new AdminPage().doPost(request, response);
    writer.flush();
    assertEquals(stringWriter.toString(), "Please enter a valid integer in the range of 0-200\n");

    // test bounds of reward
    when(request.getParameter("reward-input")).thenReturn("201");
    stringWriter.getBuffer().setLength(0);
    new AdminPage().doPost(request, response);
    writer.flush();
    assertEquals(stringWriter.toString(), "Please enter a valid integer in the range of 0-200\n");

    when(request.getParameter("reward-input")).thenReturn("-1");
    stringWriter.getBuffer().setLength(0);

    new AdminPage().doPost(request, response);
    writer.flush();
    assertEquals(stringWriter.toString(), "Please enter a valid integer in the range of 0-200\n");
  }
}
