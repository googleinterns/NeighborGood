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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
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
import javax.servlet.ServletException;
import javax.servlet.http.*;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit test on the UserInfoServlet file */
@RunWith(JUnit4.class)
public final class UserInfoServletTest {

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

  @Before
  public void setUp() {
    helper.setUp();
  }

  @After
  public void tearDown() {
    helper.tearDown();
  }

  @Test
  public void testEnvironmentTest() {
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
  public void normalSingleInputTest() throws IOException, ServletException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // Check whether the datastore is empty before the test
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    when(request.getParameter("nickname-input")).thenReturn("Leonard");
    when(request.getParameter("address-input")).thenReturn("4xxx Centre Avenue");
    when(request.getParameter("phone-input")).thenReturn("4xxxxxxxxx");
    when(request.getParameter("zipcode-input")).thenReturn("xxxxx");
    when(request.getParameter("country-input")).thenReturn("United States");
    when(request.getParameter("lat")).thenReturn("47.6912892");
    when(request.getParameter("lng")).thenReturn("-122.2406845");

    new UserInfoServlet().doPost(request, response);

    // After sending the POST request, there should be one entity in the datastore
    assertEquals(1, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("UserInfo"));
    Entity entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored personal information
    assertEquals("Leonard", (String) entity.getProperty("nickname"));
    assertEquals("4xxx Centre Avenue", (String) entity.getProperty("address"));
    assertEquals("4xxxxxxxxx", (String) entity.getProperty("phone"));
    assertEquals("xxxxx", (String) entity.getProperty("zipcode"));
    assertEquals("United States", (String) entity.getProperty("country"));
    assertEquals("leonardzhang@google.com", (String) entity.getProperty("email"));
    assertEquals("1234567890", entity.getKey().getName());
    assertEquals(0, (long) entity.getProperty("points"));

    when(request.getParameter("nickname-input")).thenReturn("Leo");

    new UserInfoServlet().doPost(request, response);

    // After sending the second POST request, there should be still one entity in the datastore
    assertEquals(1, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));
    results = ds.prepare(new Query("UserInfo"));
    entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored personal information
    assertEquals("Leo", (String) entity.getProperty("nickname"));
    assertEquals("4xxx Centre Avenue", (String) entity.getProperty("address"));
    assertEquals("4xxxxxxxxx", (String) entity.getProperty("phone"));
    assertEquals("xxxxx", (String) entity.getProperty("zipcode"));
    assertEquals("United States", (String) entity.getProperty("country"));
    assertEquals("leonardzhang@google.com", (String) entity.getProperty("email"));
    assertEquals("1234567890", entity.getKey().getName());
    assertEquals(0, (long) entity.getProperty("points"));
  }

  @Test
  public void normalMultipleInputTest() throws IOException, ServletException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // Put two hard-coded entities into datastore in advance
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    Entity dummy = new Entity("UserInfo", "1234567");
    dummy.setProperty("nickname", "Leonardo");
    dummy.setProperty("address", "xxx");
    dummy.setProperty("phone", "xxx");
    dummy.setProperty("email", "test@example.com");
    dummy.setProperty("points", 0);

    ds.put(dummy);
    Entity dummy_2 = new Entity("UserInfo", "12345");
    dummy_2.setProperty("nickname", "Leonar");
    dummy_2.setProperty("address", "xxx");
    dummy_2.setProperty("phone", "xxx");
    dummy_2.setProperty("email", "test2@example.com");
    dummy_2.setProperty("points", 50);
    ds.put(dummy_2);

    when(request.getParameter("nickname-input")).thenReturn("Leonard");
    when(request.getParameter("address-input")).thenReturn("4xxx Centre Avenue");
    when(request.getParameter("phone-input")).thenReturn("4xxxxxxxxx");
    when(request.getParameter("zipcode-input")).thenReturn("xxxxx");
    when(request.getParameter("country-input")).thenReturn("United States");
    when(request.getParameter("lat")).thenReturn("47.6912892");
    when(request.getParameter("lng")).thenReturn("-122.2406845");

    new UserInfoServlet().doPost(request, response);

    // After sending the POST request, there should be three entities in the datastore
    assertEquals(3, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    // Filter out the entity that has the userId of 1234567890
    Key userEntityKey = KeyFactory.createKey("UserInfo", "1234567890");
    Entity entity = null;
    try {
      entity = ds.get(userEntityKey);
    } catch (EntityNotFoundException e) {
      System.err.println("The entity cannot be null");
    }
    // The entity can't be null
    assertNotNull(entity);

    // Test the stored personal information
    assertEquals("Leonard", (String) entity.getProperty("nickname"));
    assertEquals("4xxx Centre Avenue", (String) entity.getProperty("address"));
    assertEquals("4xxxxxxxxx", (String) entity.getProperty("phone"));
    assertEquals("xxxxx", (String) entity.getProperty("zipcode"));
    assertEquals("United States", (String) entity.getProperty("country"));
    assertEquals("leonardzhang@google.com", (String) entity.getProperty("email"));
    assertEquals("1234567890", entity.getKey().getName());
    assertEquals("47.6912892", String.valueOf(entity.getProperty("lat")));
    assertEquals("-122.2406845", String.valueOf(entity.getProperty("lng")));
    assertEquals(0, (long) entity.getProperty("points"));

    when(request.getParameter("nickname-input")).thenReturn("Leo");

    new UserInfoServlet().doPost(request, response);

    // After sending the second POST request, there should be still three entities in the datastore
    assertEquals(3, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    try {
      entity = ds.get(userEntityKey);
    } catch (EntityNotFoundException e) {
      System.err.println("The entity cannot be null");
    }
    // The entity can't be null
    assertNotNull(entity);

    // Test the stored personal information
    assertEquals("Leo", (String) entity.getProperty("nickname"));
    assertEquals("4xxx Centre Avenue", (String) entity.getProperty("address"));
    assertEquals("4xxxxxxxxx", (String) entity.getProperty("phone"));
    assertEquals("xxxxx", (String) entity.getProperty("zipcode"));
    assertEquals("United States", (String) entity.getProperty("country"));
    assertEquals("leonardzhang@google.com", (String) entity.getProperty("email"));
    assertEquals("1234567890", entity.getKey().getName());
    assertEquals("47.6912892", String.valueOf(entity.getProperty("lat")));
    assertEquals("-122.2406845", String.valueOf(entity.getProperty("lng")));
    assertEquals(0, (long) entity.getProperty("points"));
  }

  /** Test the edge case where at least one of the three input fields are empty or spaces. */
  @Test
  public void emptyInputTest() throws IOException, ServletException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // Check whether the datastore is empty before the test
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    // Set the nickname input to be empty
    when(request.getParameter("nickname-input")).thenReturn("");
    when(request.getParameter("address-input")).thenReturn("4xxx Centre Avenue");
    when(request.getParameter("phone-input")).thenReturn("4xxxxxxxxx");
    when(request.getParameter("zipcode-input")).thenReturn("xxxxx");
    when(request.getParameter("country-input")).thenReturn("United States");
    when(request.getParameter("lat")).thenReturn("47.6912892");
    when(request.getParameter("lng")).thenReturn("-122.2406845");

    // Try to catch the error message sent by the UserInfoServlet
    System.setErr(new PrintStream(errContent));

    new UserInfoServlet().doPost(request, response);

    // The POST request should lead to an error handling clause, there should be no entity in the
    // datastore
    assertEquals("At least one input field is empty\n", errContent.toString());
    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    errContent.reset();
    System.setErr(originalErr);

    // Set the nickname input to empty spaces
    when(request.getParameter("nickname-input")).thenReturn("   ");

    // Try to catch the error message sent by the UserInfoServlet
    System.setErr(new PrintStream(errContent));

    new UserInfoServlet().doPost(request, response);

    // Spaces will also lead to error handling clause, there should be still no entity in the
    // datastore
    assertEquals("At least one input field is empty\n", errContent.toString());
    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    errContent.reset();
    System.setErr(originalErr);

    // Now we give the servlet a valid nickname input, but an invalid address input
    when(request.getParameter("nickname-input")).thenReturn("Leonard");
    when(request.getParameter("address-input")).thenReturn(" ");

    // Try to catch the error message sent by the UserInfoServlet
    System.setErr(new PrintStream(errContent));

    new UserInfoServlet().doPost(request, response);

    // The POST request should lead to an error handling clause, there should be no entity in the
    // datastore
    assertEquals("At least one input field is empty\n", errContent.toString());
    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    errContent.reset();
    System.setErr(originalErr);

    // Now we give the servlet an invalid phone number input
    when(request.getParameter("address-input")).thenReturn("4xxx Centre Avenue");
    when(request.getParameter("phone-input")).thenReturn("");

    new UserInfoServlet().doPost(request, response);

    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    // Finally we give the servlet a valid phone number input
    when(request.getParameter("phone-input")).thenReturn("4xxxxxxxxx");

    new UserInfoServlet().doPost(request, response);

    assertEquals(1, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));
    PreparedQuery results = ds.prepare(new Query("UserInfo"));
    Entity entity = results.asSingleEntity();

    // The entity can't be null
    assertNotNull(entity);

    // Test the stored personal information
    assertEquals("Leonard", (String) entity.getProperty("nickname"));
    assertEquals("4xxx Centre Avenue", (String) entity.getProperty("address"));
    assertEquals("4xxxxxxxxx", (String) entity.getProperty("phone"));
    assertEquals("xxxxx", (String) entity.getProperty("zipcode"));
    assertEquals("United States", (String) entity.getProperty("country"));
    assertEquals("leonardzhang@google.com", (String) entity.getProperty("email"));
    assertEquals("1234567890", entity.getKey().getName());
    assertEquals("47.6912892", String.valueOf(entity.getProperty("lat")));
    assertEquals("-122.2406845", String.valueOf(entity.getProperty("lng")));
    assertEquals(0, (long) entity.getProperty("points"));
  }

  @Test
  public void doGetTest() throws IOException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // Check whether the datastore is empty before the test
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("UserInfo")).countEntities(withLimit(10)));

    Entity dummy = new Entity("UserInfo", "1234567890");
    dummy.setProperty("nickname", "Leonard");
    dummy.setProperty("address", "xxx");
    dummy.setProperty("phone", "xxx");
    dummy.setProperty("email", "test@example.com");
    dummy.setProperty("points", 0);
    dummy.setProperty("zipcode", "xxxxx");
    dummy.setProperty("country", "US");
    ds.put(dummy);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new UserInfoServlet().doGet(request, response);

    // After sending the GET request, the doGet function should output the json string
    writer.flush();
    assertTrue(stringWriter.toString().contains("[\"Leonard\",\"xxx\",\"xxx\",\"xxxxx\",\"US\"]"));

    // Finally, ensure that the servlet file has set the content type to json
    verify(response).setContentType("application/json;");
  }
}
