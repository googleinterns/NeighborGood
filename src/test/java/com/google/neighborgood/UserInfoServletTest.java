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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
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

/** */
@RunWith(JUnit4.class)
public final class UserInfoServletTest {

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

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void setUpTest() {
    // Test the UserService feature
    UserService userService = UserServiceFactory.getUserService();
    assertTrue(userService.isUserAdmin());
    assertTrue(userService.isUserLoggedIn());

    // Test the DataStore feature
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("dummy")).countEntities(withLimit(10)));
    ds.put(new Entity("dummy"));
    ds.put(new Entity("dummy"));
    assertEquals(2, ds.prepare(new Query("dummy")).countEntities(withLimit(10)));
  }

  @Test
  public void normalSingleInputTest() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // Check whether the datastore is empty before the test
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    when(request.getParameter("nickname-input")).thenReturn("Leonard");
    when(request.getParameter("address-input")).thenReturn("4xxx Centre Avenue");
    when(request.getParameter("phone-input")).thenReturn("4xxxxxxxxx");

    try {
      new UserInfoServlet().doPost(request, response);
    } catch (IOException e) {
      assertTrue(false);
    }

    // After sending the POST request, there should be one entity in the datastore
    assertEquals(1, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("UserInfo"));
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      // The entity can't be null
      assertTrue(false);
    }

    // Test the stored personal information
    assertEquals("Leonard", (String) entity.getProperty("nickname"));
    assertEquals("4xxx Centre Avenue", (String) entity.getProperty("address"));
    assertEquals("4xxxxxxxxx", (String) entity.getProperty("phone"));
    assertEquals("leonardzhang@google.com", (String) entity.getProperty("email"));
    assertEquals("1234567890", (String) entity.getProperty("userId"));
    assertEquals(0, (long) entity.getProperty("points"));

    when(request.getParameter("nickname-input")).thenReturn("Leo");

    try {
      new UserInfoServlet().doPost(request, response);
    } catch (IOException e) {
      assertTrue(false);
    }

    // After sending the second POST request, there should be still one entity in the datastore
    assertEquals(1, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));
    results = ds.prepare(new Query("UserInfo"));
    entity = results.asSingleEntity();
    if (entity == null) {
      // The entity can't be null
      assertTrue(false);
    }

    // Test the stored personal information
    assertEquals("Leo", (String) entity.getProperty("nickname"));
    assertEquals("4xxx Centre Avenue", (String) entity.getProperty("address"));
    assertEquals("4xxxxxxxxx", (String) entity.getProperty("phone"));
    assertEquals("leonardzhang@google.com", (String) entity.getProperty("email"));
    assertEquals("1234567890", (String) entity.getProperty("userId"));
    assertEquals(0, (long) entity.getProperty("points"));
  }

  @Test
  public void normalMultipleInputTest() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // Put two hard-coded entities into datastore in advance
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity dummy = new Entity("UserInfo");
    dummy.setProperty("nickname", "Leonardo");
    dummy.setProperty("address", "xxx");
    dummy.setProperty("phone", "xxx");
    dummy.setProperty("email", "test@example.com");
    dummy.setProperty("userId", "1234567");
    dummy.setProperty("points", 0);
    ds.put(dummy);
    Entity dummy_2 = new Entity("UserInfo");
    dummy_2.setProperty("nickname", "Leonar");
    dummy_2.setProperty("address", "xxx");
    dummy_2.setProperty("phone", "xxx");
    dummy_2.setProperty("email", "test2@example.com");
    dummy_2.setProperty("userId", "12345");
    dummy_2.setProperty("points", 50);
    ds.put(dummy_2);

    when(request.getParameter("nickname-input")).thenReturn("Leonard");
    when(request.getParameter("address-input")).thenReturn("4xxx Centre Avenue");
    when(request.getParameter("phone-input")).thenReturn("4xxxxxxxxx");

    try {
      new UserInfoServlet().doPost(request, response);
    } catch (IOException e) {
      assertTrue(false);
    }

    // After sending the POST request, there should be three entities in the datastore
    assertEquals(3, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    // Filter out the entity that has the userId of 1234567890
    PreparedQuery results =
        ds.prepare(
            new Query("UserInfo")
                .setFilter(new FilterPredicate("userId", FilterOperator.EQUAL, "1234567890")));
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      // The entity can't be null
      assertTrue(false);
    }

    // Test the stored personal information
    assertEquals("Leonard", (String) entity.getProperty("nickname"));
    assertEquals("4xxx Centre Avenue", (String) entity.getProperty("address"));
    assertEquals("4xxxxxxxxx", (String) entity.getProperty("phone"));
    assertEquals("leonardzhang@google.com", (String) entity.getProperty("email"));
    assertEquals("1234567890", (String) entity.getProperty("userId"));
    assertEquals(0, (long) entity.getProperty("points"));

    when(request.getParameter("nickname-input")).thenReturn("Leo");

    try {
      new UserInfoServlet().doPost(request, response);
    } catch (IOException e) {
      assertTrue(false);
    }

    // After sending the second POST request, there should be still three entities in the datastore
    assertEquals(3, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));
    results =
        ds.prepare(
            new Query("UserInfo")
                .setFilter(new FilterPredicate("userId", FilterOperator.EQUAL, "1234567890")));
    entity = results.asSingleEntity();
    if (entity == null) {
      // The entity can't be null
      assertTrue(false);
    }

    // Test the stored personal information
    assertEquals("Leo", (String) entity.getProperty("nickname"));
    assertEquals("4xxx Centre Avenue", (String) entity.getProperty("address"));
    assertEquals("4xxxxxxxxx", (String) entity.getProperty("phone"));
    assertEquals("leonardzhang@google.com", (String) entity.getProperty("email"));
    assertEquals("1234567890", (String) entity.getProperty("userId"));
    assertEquals(0, (long) entity.getProperty("points"));
  }

  @Test
  public void edgeCaseTest() {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // Check whether the datastore is empty before the test
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    // Set the nickname input to be empty
    when(request.getParameter("nickname-input")).thenReturn("");
    when(request.getParameter("address-input")).thenReturn("4xxx Centre Avenue");
    when(request.getParameter("phone-input")).thenReturn("4xxxxxxxxx");

    try {
      new UserInfoServlet().doPost(request, response);
    } catch (IOException e) {
      assertTrue(false);
    }

    // The POST request should lead to an error handling clause, there should be no entity in the
    // datastore
    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    // Set the nickname input to empty spaces
    when(request.getParameter("nickname-input")).thenReturn("   ");

    try {
      new UserInfoServlet().doPost(request, response);
    } catch (IOException e) {
      assertTrue(false);
    }

    // Spaces will also lead to error handling clause, there should be still no entity in the
    // datastore
    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    // Now we give the servlet a valid nickname input, but an invalid address input
    when(request.getParameter("nickname-input")).thenReturn("Leonard");
    when(request.getParameter("address-input")).thenReturn(" ");

    try {
      new UserInfoServlet().doPost(request, response);
    } catch (IOException e) {
      assertTrue(false);
    }

    // The POST request should lead to an error handling clause, there should be no entity in the
    // datastore
    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    // Now we give the servlet an invalid phone number input
    when(request.getParameter("address-input")).thenReturn("4xxx Centre Avenue");
    when(request.getParameter("phone-input")).thenReturn("");

    try {
      new UserInfoServlet().doPost(request, response);
    } catch (IOException e) {
      assertTrue(false);
    }
    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    // Finally we give the servlet a valid phone number input
    when(request.getParameter("phone-input")).thenReturn("4xxxxxxxxx");

    try {
      new UserInfoServlet().doPost(request, response);
    } catch (IOException e) {
      assertTrue(false);
    }

    assertEquals(1, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("UserInfo"));
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      // The entity can't be null
      assertTrue(false);
    }

    // Test the stored personal information
    assertEquals("Leonard", (String) entity.getProperty("nickname"));
    assertEquals("4xxx Centre Avenue", (String) entity.getProperty("address"));
    assertEquals("4xxxxxxxxx", (String) entity.getProperty("phone"));
    assertEquals("leonardzhang@google.com", (String) entity.getProperty("email"));
    assertEquals("1234567890", (String) entity.getProperty("userId"));
    assertEquals(0, (long) entity.getProperty("points"));
  }
}
