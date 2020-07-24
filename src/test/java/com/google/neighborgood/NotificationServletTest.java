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
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
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

/** Unit test on the NotificationServlet file */
@RunWith(JUnit4.class)
public final class NotificationServletTest {

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

  @Before
  public void setUp() {
    helper.setUp();
    userService = UserServiceFactory.getUserService();
    ds = DatastoreServiceFactory.getDatastoreService();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);

    // Add 3 dummy notification entities with the current user as receiver and taskId equals to 1
    for (int i = 0; i < 3; i++) {
      Entity entity = new Entity("Notification");
      entity.setProperty("receiver", "1234567890");
      entity.setProperty("taskId", "1");
      ds.put(entity);
    }

    // Add 4 dummy notification entities with taskId equals to 1 but not the current user as
    // receiver
    for (int i = 0; i < 4; i++) {
      Entity entity = new Entity("Notification");
      entity.setProperty("receiver", "123456789");
      entity.setProperty("taskId", "1");
      ds.put(entity);
    }

    // Add 1 dummy notification entity with the current user as receiver and taskId equals to 2
    Entity entity = new Entity("Notification");
    entity.setProperty("receiver", "1234567890");
    entity.setProperty("taskId", "2");
    ds.put(entity);
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
    assertEquals(8, ds.prepare(new Query("Notification")).countEntities(withLimit(10)));
  }

  @Test
  public void doPostTest() throws IOException {
    // Ensure that there are 8 notificationi entity at the beginning
    assertEquals(8, ds.prepare(new Query("Notification")).countEntities(withLimit(10)));

    // Now let's try to delete all the notification entity with task id 1 and the current user as
    // receiver
    when(request.getParameter("task-id")).thenReturn("1");

    new NotificationServlet().doPost(request, response);

    // After handling the POST request, 3 entities should be removed
    assertEquals(5, ds.prepare(new Query("Notification")).countEntities(withLimit(10)));

    // Ensure that no notification with task id 1 and receiver 1234567890 is left
    Filter idFilter = new FilterPredicate("taskId", FilterOperator.EQUAL, "1");
    Filter receiverFilter = new FilterPredicate("receiver", FilterOperator.EQUAL, "1234567890");
    CompositeFilter filter = CompositeFilterOperator.and(idFilter, receiverFilter);
    assertEquals(
        0, ds.prepare(new Query("Notification").setFilter(filter)).countEntities(withLimit(10)));

    // Now let's try to delete all the notification entity with task id 2 and the current user as
    // receiver
    when(request.getParameter("task-id")).thenReturn("2");

    new NotificationServlet().doPost(request, response);

    // After handling the POST request, 1 entity should be removed
    assertEquals(4, ds.prepare(new Query("Notification")).countEntities(withLimit(10)));

    // Ensure that no notification with task id 2 and receiver 1234567890 is left
    idFilter = new FilterPredicate("taskId", FilterOperator.EQUAL, "2");
    receiverFilter = new FilterPredicate("receiver", FilterOperator.EQUAL, "1234567890");
    filter = CompositeFilterOperator.and(idFilter, receiverFilter);
    assertEquals(
        0, ds.prepare(new Query("Notification").setFilter(filter)).countEntities(withLimit(10)));
  }
}
