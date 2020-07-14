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
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
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
  private String keyString;
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
}
