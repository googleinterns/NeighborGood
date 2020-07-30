// Copyright 2020 Google LLC
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
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.ServletException;
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
  private HttpSession session;
  private HttpServletResponse response;
  private Entity userEntity;
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalErr = System.err;

  @Before
  public void setUp() {
    helper.setUp();
    userService = UserServiceFactory.getUserService();
    ds = DatastoreServiceFactory.getDatastoreService();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    session = mock(HttpSession.class);
    userEntity = new Entity("UserInfo", "1234567890");
    userEntity.setProperty("nickname", "Leonard");
    userEntity.setProperty("address", "xxx");
    userEntity.setProperty("phone", "xxx");
    userEntity.setProperty("email", "leonardzhang@google.com");
    userEntity.setProperty("userId", "1234567890");
    userEntity.setProperty("country", "US");
    userEntity.setProperty("zipcode", "15213");
    userEntity.setProperty("points", 0);
    userEntity.setProperty("lat", 47.674400);
    userEntity.setProperty("lng", -122.175798);
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
  public void singleInputDoPostTest() throws IOException, ServletException {
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
  public void multipleInputDoPostTest() throws IOException, ServletException {
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

  @Test
  public void lessThanTenTasksDoGetTest() throws IOException, ServletException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // Check whether there are no entities before test
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("Task")).countEntities(withLimit(10)));

    // Adds 5 task entities
    for (int i = 0; i < 5; i++) {
      Entity taskEntity = new Entity("Task", userEntity.getKey());
      taskEntity.setProperty("detail", "Test task detail");
      taskEntity.setProperty("overview", "Test task overview" + i);
      taskEntity.setProperty("timestamp", System.currentTimeMillis());
      taskEntity.setProperty("reward", (long) 50);
      taskEntity.setProperty("status", "OPEN");
      taskEntity.setProperty("Owner", "1234567890");
      taskEntity.setProperty("Helper", "N/A");
      taskEntity.setProperty("Address", "xxx");
      taskEntity.setProperty("zipcode", "98033");
      taskEntity.setProperty("country", "United States");
      taskEntity.setProperty("category", "Garden");
      ds.put(taskEntity);
    }

    when(request.getParameter("zipcode")).thenReturn("98033");
    when(request.getParameter("country")).thenReturn("United States");
    when(request.getParameter("cursor")).thenReturn("clear");
    Map<String, String[]> dummyReturn = new HashMap<>();
    dummyReturn.put("zipcode", new String[] {"dummy1"});
    dummyReturn.put("country", new String[] {"dummy1"});
    when(request.getParameterMap()).thenReturn(dummyReturn);
    when(request.getSession()).thenReturn(session);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new TaskServlet().doGet(request, response);

    writer.flush();

    // parses response as a json object
    JsonObject jsonObject = new JsonParser().parse(stringWriter.toString()).getAsJsonObject();

    // Response should have 5 tasks and should be the end of the query (that is there are no more
    // tasks left
    // on that query to return
    int taskCount = jsonObject.get("currentTaskCount").getAsInt();
    boolean endOfQuery = jsonObject.get("endOfQuery").getAsBoolean();
    assertEquals(5, taskCount);
    assertEquals(true, endOfQuery);

    verify(response).setContentType("application/json;");
  }

  @Test
  public void moreThanTenTasksDoGetTest() throws IOException, ServletException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // Check whether there are no entities before test
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("Task")).countEntities(withLimit(10)));

    // Adds 15 task entities
    for (int i = 0; i < 15; i++) {
      Entity taskEntity = new Entity("Task", userEntity.getKey());
      taskEntity.setProperty("detail", "Test task detail");
      taskEntity.setProperty("overview", "Test task overview" + i);
      taskEntity.setProperty("timestamp", System.currentTimeMillis());
      taskEntity.setProperty("reward", (long) 50);
      taskEntity.setProperty("status", "OPEN");
      taskEntity.setProperty("Owner", "1234567890");
      taskEntity.setProperty("Helper", "N/A");
      taskEntity.setProperty("Address", "xxx");
      taskEntity.setProperty("zipcode", "98033");
      taskEntity.setProperty("country", "United States");
      taskEntity.setProperty("category", "Garden");
      ds.put(taskEntity);
    }

    when(request.getParameter("zipcode")).thenReturn("98033");
    when(request.getParameter("country")).thenReturn("United States");
    when(request.getParameter("cursor")).thenReturn("clear");
    Map<String, String[]> dummyReturn = new HashMap<>();
    dummyReturn.put("zipcode", new String[] {"dummy1"});
    dummyReturn.put("country", new String[] {"dummy1"});
    when(request.getParameterMap()).thenReturn(dummyReturn);
    when(request.getSession()).thenReturn(session);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new TaskServlet().doGet(request, response);

    writer.flush();

    // parses response as a json object
    JsonObject jsonObject = new JsonParser().parse(stringWriter.toString()).getAsJsonObject();

    // Response should have 10 tasks and should not be the end of the query
    int taskCount = jsonObject.get("currentTaskCount").getAsInt();
    boolean endOfQuery = jsonObject.get("endOfQuery").getAsBoolean();
    assertEquals(10, taskCount);
    assertEquals(false, endOfQuery);

    // I generated this query just as like the previous servlet doGet would do for the sole purpose
    // of retrieving the endCursor for the next test
    Query query = new Query("Task").addSort("timestamp", SortDirection.DESCENDING);
    List<Query.Filter> filters = new ArrayList<Query.Filter>();
    filters.add(new Query.FilterPredicate("zipcode", Query.FilterOperator.EQUAL, "98033"));
    filters.add(new Query.FilterPredicate("country", Query.FilterOperator.EQUAL, "United States"));
    filters.add(new Query.FilterPredicate("status", Query.FilterOperator.EQUAL, "OPEN"));
    query.setFilter(new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filters));
    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);
    QueryResultList<Entity> results;
    try {
      results = ds.prepare(query).asQueryResultList(fetchOptions);
    } catch (IllegalArgumentException e) {
      System.err.println(e);
      return;
    }
    String endCursor = results.getCursor().toWebSafeString();

    // passes the endCursor to the mocked session instance as it were stored in the real session
    when(request.getSession().getAttribute("endCursor")).thenReturn(endCursor);

    verify(response).setContentType("application/json;");

    stringWriter = new StringWriter();
    writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    when(request.getParameter("cursor")).thenReturn("end");
    new TaskServlet().doGet(request, response);

    writer.flush();

    // parses response as a json object
    jsonObject = new JsonParser().parse(stringWriter.toString()).getAsJsonObject();

    // Response should have 5 tasks and should be the end of the query
    taskCount = jsonObject.get("currentTaskCount").getAsInt();
    endOfQuery = jsonObject.get("endOfQuery").getAsBoolean();
    assertEquals(5, taskCount);
    assertEquals(true, endOfQuery);
  }

  @Test
  public void missingNeighborhoodDoGetTest() throws IOException, ServletException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // Check whether there are no entities before test
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("Task")).countEntities(withLimit(10)));

    when(request.getParameter("cursor")).thenReturn("clear");
    when(request.getSession()).thenReturn(session);

    System.setErr(new PrintStream(errContent));

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new TaskServlet().doGet(request, response);

    writer.flush();

    // Captures System error printed out when zipcode and country parameters are missing
    assertEquals("Zipcode and Country details are missing\n", errContent.toString());

    errContent.reset();
    System.setErr(originalErr);

    // Verifies correct error code and message got sent out
    verify(response).sendError(400, "Zipcode and Country details are missing");
  }

  @Test
  public void categoryFilterDoGetTest() throws IOException, ServletException {
    HttpServletRequest request = mock(HttpServletRequest.class);
    HttpServletResponse response = mock(HttpServletResponse.class);

    // Check whether there are no entities before test
    DatastoreService ds = DatastoreServiceFactory.getDatastoreService();
    assertEquals(0, ds.prepare(new Query("Task")).countEntities(withLimit(10)));

    String[] categories = {"Garden", "Pets", "Shopping", "Misc"};

    // Adds 20 task entities, 5 of each category
    for (int i = 0; i < 20; i++) {
      Entity taskEntity = new Entity("Task", userEntity.getKey());
      taskEntity.setProperty("detail", "Test task detail");
      taskEntity.setProperty("overview", "Test task overview" + i);
      taskEntity.setProperty("timestamp", System.currentTimeMillis());
      taskEntity.setProperty("reward", (long) 50);
      taskEntity.setProperty("status", "OPEN");
      taskEntity.setProperty("Owner", "1234567890");
      taskEntity.setProperty("Helper", "N/A");
      taskEntity.setProperty("Address", "xxx");
      taskEntity.setProperty("zipcode", "98033");
      taskEntity.setProperty("country", "United States");
      taskEntity.setProperty("category", categories[(i % 4)]);
      ds.put(taskEntity);
    }

    // Adds 2 more task entities of Shopping category
    for (int i = 0; i < 2; i++) {
      Entity taskEntity = new Entity("Task", userEntity.getKey());
      taskEntity.setProperty("detail", "Test task detail");
      taskEntity.setProperty("overview", "Test task overview" + i);
      taskEntity.setProperty("timestamp", System.currentTimeMillis());
      taskEntity.setProperty("reward", (long) 50);
      taskEntity.setProperty("status", "OPEN");
      taskEntity.setProperty("Owner", "1234567890");
      taskEntity.setProperty("Helper", "N/A");
      taskEntity.setProperty("Address", "xxx");
      taskEntity.setProperty("zipcode", "98033");
      taskEntity.setProperty("country", "United States");
      taskEntity.setProperty("category", "Shopping");
      ds.put(taskEntity);
    }

    when(request.getParameter("zipcode")).thenReturn("98033");
    when(request.getParameter("country")).thenReturn("United States");
    when(request.getParameter("cursor")).thenReturn("clear");
    when(request.getParameter("category")).thenReturn("Shopping");
    Map<String, String[]> dummyReturn = new HashMap<>();
    dummyReturn.put("zipcode", new String[] {"dummy1"});
    dummyReturn.put("country", new String[] {"dummy1"});
    dummyReturn.put("category", new String[] {"dummy1"});
    when(request.getParameterMap()).thenReturn(dummyReturn);
    when(request.getSession()).thenReturn(session);

    StringWriter stringWriter = new StringWriter();
    PrintWriter writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new TaskServlet().doGet(request, response);

    writer.flush();

    // parses response as a json object
    JsonObject jsonObject = new JsonParser().parse(stringWriter.toString()).getAsJsonObject();

    // Response should have 7 shopping tasks and should be end of query
    int taskCount = jsonObject.get("currentTaskCount").getAsInt();
    boolean endOfQuery = jsonObject.get("endOfQuery").getAsBoolean();
    assertEquals(7, taskCount);
    assertEquals(true, endOfQuery);

    when(request.getParameter("category")).thenReturn("Misc");

    stringWriter = new StringWriter();
    writer = new PrintWriter(stringWriter);
    when(response.getWriter()).thenReturn(writer);

    new TaskServlet().doGet(request, response);

    writer.flush();

    // parses response as a json object
    jsonObject = new JsonParser().parse(stringWriter.toString()).getAsJsonObject();

    // Response should have 5 misc tasks and should be end of query
    taskCount = jsonObject.get("currentTaskCount").getAsInt();
    endOfQuery = jsonObject.get("endOfQuery").getAsBoolean();
    assertEquals(5, taskCount);
    assertEquals(true, endOfQuery);
  }
}
