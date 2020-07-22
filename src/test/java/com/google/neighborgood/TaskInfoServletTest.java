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

/** Unit test on the TaskInfoServlet file */
@RunWith(JUnit4.class)
public final class TaskInfoServletTest {

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
    assertEquals(1, ds.prepare(new Query("Task")).countEntities(withLimit(10)));
    assertEquals(1, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));
  }

  @Test
  public void abandonTaskTest() throws IOException {
    // Check whether the datastore has the input dummy task entity at first
    assertEquals(1, ds.prepare(new Query("Task")).countEntities(withLimit(10)));

    // Simulate the situation where the user abandon a task from the offer help page
    when(request.getParameter("key")).thenReturn(keyString);
    when(request.getParameter("status")).thenReturn("OPEN");

    new TaskInfoServlet().doPost(request, response);

    // After sending the POST request, the dummy task entity should be set to OPEN status
    assertEquals(1, ds.prepare(new Query("Task")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("Task"));
    Entity entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored task information
    assertEquals("1234567890", (String) entity.getProperty("Owner"));
    assertEquals("Test task", (String) entity.getProperty("detail"));
    assertEquals(50, (long) entity.getProperty("reward"));
    assertEquals("N/A", (String) entity.getProperty("Helper"));
    assertEquals("OPEN", (String) entity.getProperty("status"));
  }

  @Test
  public void completeTaskTest() throws IOException {
    // Check whether the datastore has the input dummy task entity at first
    assertEquals(1, ds.prepare(new Query("Task")).countEntities(withLimit(10)));

    // Simulate the situation where the user verify a task from the need help page
    when(request.getParameter("key")).thenReturn(keyString);
    when(request.getParameter("status")).thenReturn("COMPLETE: AWAIT VERIFICATION");

    new TaskInfoServlet().doPost(request, response);

    // After sending the POST request, the dummy task entity should be set to AWAIT VERIFICATION
    // status
    assertEquals(1, ds.prepare(new Query("Task")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("Task"));
    Entity entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored task information
    assertEquals("1234567890", (String) entity.getProperty("Owner"));
    assertEquals("Test task", (String) entity.getProperty("detail"));
    assertEquals(50, (long) entity.getProperty("reward"));
    assertEquals("1234567890", (String) entity.getProperty("Helper"));
    assertEquals("COMPLETE: AWAIT VERIFICATION", (String) entity.getProperty("status"));
  }

  @Test
  public void verifyTaskTest() throws IOException {
    // Check whether the datastore has the input dummy task entity at first
    assertEquals(1, ds.prepare(new Query("Task")).countEntities(withLimit(10)));

    // Change the status of the task to COMPLETE: AWAIT VERIFICATION
    taskEntity.setProperty("status", "COMPLETE: AWAIT VERIFICATION");
    ds.put(taskEntity);

    // Simulate the situation where the user complete a task from the offer help page
    when(request.getParameter("key")).thenReturn(keyString);
    when(request.getParameter("status")).thenReturn("COMPLETE");

    new TaskInfoServlet().doPost(request, response);

    // After sending the POST request, the dummy task entity should be set to COMPLETE status
    assertEquals(1, ds.prepare(new Query("Task")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("Task"));
    Entity entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored task information
    assertEquals("1234567890", (String) entity.getProperty("Owner"));
    assertEquals("Test task", (String) entity.getProperty("detail"));
    assertEquals(50, (long) entity.getProperty("reward"));
    assertEquals("1234567890", (String) entity.getProperty("Helper"));
    assertEquals("COMPLETE", (String) entity.getProperty("status"));

    // After the user verified task, the helper's helping point should also be updated
    results = ds.prepare(new Query("UserInfo"));
    entity = results.asSingleEntity();
    assertNotNull(entity);

    // Test whether the stored personal information has been updated
    assertEquals("Leonard", (String) entity.getProperty("nickname"));
    assertEquals("xxx", (String) entity.getProperty("address"));
    assertEquals("xxx", (String) entity.getProperty("phone"));
    assertEquals("15213", (String) entity.getProperty("zipcode"));
    assertEquals("US", (String) entity.getProperty("country"));
    assertEquals("leonardzhang@google.com", (String) entity.getProperty("email"));
    assertEquals("1234567890", (String) entity.getProperty("userId"));
    assertEquals(50, (long) entity.getProperty("points"));
  }

  @Test
  public void disapproveTaskTest() throws IOException {
    // Check whether the datastore has the input dummy task entity at first
    assertEquals(1, ds.prepare(new Query("Task")).countEntities(withLimit(10)));

    // Change the status of the task to COMPLETE: AWAIT VERIFICATION
    taskEntity.setProperty("status", "COMPLETE: AWAIT VERIFICATION");
    ds.put(taskEntity);

    // Simulate the situation where the user disapprove a task from the offer help page
    when(request.getParameter("key")).thenReturn(keyString);
    when(request.getParameter("status")).thenReturn("IN PROGRESS");

    new TaskInfoServlet().doPost(request, response);

    // After sending the POST request, the dummy task entity should be set to IN PROGRESS status
    assertEquals(1, ds.prepare(new Query("Task")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("Task"));
    Entity entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored task information
    assertEquals("1234567890", (String) entity.getProperty("Owner"));
    assertEquals("Test task", (String) entity.getProperty("detail"));
    assertEquals(50, (long) entity.getProperty("reward"));
    assertEquals("1234567890", (String) entity.getProperty("Helper"));
    assertEquals("IN PROGRESS", (String) entity.getProperty("status"));

    // After the user disapproved task, the helper's helping point should still be 0
    results = ds.prepare(new Query("UserInfo"));
    entity = results.asSingleEntity();
    assertNotNull(entity);

    // Test whether the stored personal information has been updated
    assertEquals("1234567890", (String) entity.getProperty("userId"));
    assertEquals(0, (long) entity.getProperty("points"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void invalidKeyStringTest() throws IOException {
    // Simulate the situation where the input key string is invalid
    when(request.getParameter("key")).thenReturn("SOME INVALID KEY STRING");
    when(request.getParameter("status")).thenReturn("IN PROGRESS");

    new TaskInfoServlet().doPost(request, response);
  }
}
