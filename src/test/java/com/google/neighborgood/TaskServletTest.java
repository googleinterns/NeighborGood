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

/** Unit test on the TaskServlet file */
@RunWith(JUnit4.class)
public final class TaskServletTest {

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
  public void singleInputDoPostTest() throws IOException {
    // Check whether the datastore is empty before the test
    assertEquals(0, ds.prepare(new Query("Task")).countEntities(withLimit(10)));

    when(request.getParameter("reward-input")).thenReturn("50");
    when(request.getParameter("task-detail-input")).thenReturn("Help me please");
    when(request.getParameter("category-input")).thenReturn("misc");
    when(request.getParameter("task-overview-input")).thenReturn("Task Overview");

    new TaskServlet().doPost(request, response);

    // After sending the POST request, there should be one entity in the datastore
    assertEquals(1, ds.prepare(new Query("Task")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("Task"));
    Entity entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored task information
    assertEquals("1234567890", (String) entity.getProperty("Owner"));
    assertEquals("Help me please", (String) entity.getProperty("detail"));
    assertEquals("Task Overview", (String) entity.getProperty("overview"));
    assertEquals(50, (long) entity.getProperty("reward"));
  }

  @Test
  public void multipleInputDoPostTest() throws IOException {
    // Check whether the datastore is empty before the test
    assertEquals(0, ds.prepare(new Query("Task")).countEntities(withLimit(10)));

    for (int i = 1; i <= 4; i++) {
      // Set the rewarding points of each task to 50i;
      String rewardPts = Integer.toString(50 * i);
      String taskContent = "Testing task " + Integer.toString(i);

      when(request.getParameter("reward-input")).thenReturn(rewardPts);
      when(request.getParameter("task-detail-input")).thenReturn(taskContent);
      when(request.getParameter("category-input")).thenReturn("misc");
      when(request.getParameter("task-overview-input")).thenReturn("Task Overview");

      // Send a POST request to the task servlet
      new TaskServlet().doPost(request, response);

      // After sending the ith POST request, there should be i entities in the datastore
      assertEquals(i, ds.prepare(new Query("Task")).countEntities(withLimit(10)));

      // After sending the first POST request, check the stored task entity content
      if (i == 1) {
        PreparedQuery results = ds.prepare(new Query("Task"));
        Entity entity = results.asSingleEntity();

        // The entity can't be null
        assertNotNull(entity);

        // Test the stored task information
        assertEquals("1234567890", (String) entity.getProperty("Owner"));
        assertEquals("Testing task 1", (String) entity.getProperty("detail"));
        assertEquals("Task Overview", (String) entity.getProperty("overview"));
        assertEquals(50, (long) entity.getProperty("reward"));
      }
    }
  }
}
