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

import com.google.appengine.api.datastore.*;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.neighborgood.helper.RetrieveUserInfo;
import com.google.neighborgood.helper.RewardingPoints;
import com.google.neighborgood.helper.TaskGroup;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/** Servlet that creates new task entity and fetch saved tasks. */
@WebServlet("/tasks")
public class TaskServlet extends HttpServlet {
  @Override
  // doGet method retrieves tasks from datastore and responds with the HTML for each task fetched
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    boolean userLoggedIn = userService.isUserLoggedIn();
    String userId = userLoggedIn ? userService.getCurrentUser().getUserId() : "null";
    String zipcode = "";
    String country = "";

    if (request.getParameterMap().containsKey("zipcode")
        && request.getParameterMap().containsKey("country")) {
      zipcode = request.getParameter("zipcode");
      country = request.getParameter("country");
    } else {
      System.err.println("Zipcode and Country details are missing");
      response.sendError(
          HttpServletResponse.SC_BAD_REQUEST, "Zipcode and Country details are missing");
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Query query = new Query("Task").addSort("timestamp", SortDirection.DESCENDING);

    // Creates list of filters
    List<Query.Filter> filters = new ArrayList<Query.Filter>();
    filters.add(new Query.FilterPredicate("zipcode", Query.FilterOperator.EQUAL, zipcode));
    filters.add(new Query.FilterPredicate("country", Query.FilterOperator.EQUAL, country));
    filters.add(new Query.FilterPredicate("status", Query.FilterOperator.EQUAL, "OPEN"));

    // Applies a category filter, if any
    if (request.getParameterMap().containsKey("category")) {
      String category = request.getParameter("category");
      filters.add(new Query.FilterPredicate("category", Query.FilterOperator.EQUAL, category));
    }

    // Applies filters to query
    query.setFilter(new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filters));

    FetchOptions fetchOptions = FetchOptions.Builder.withLimit(10);

    // Helper class instance that will store 10 tasks and keep track of some query metadata
    TaskGroup taskGroup = new TaskGroup();

    // Gets session and passed cursor action
    HttpSession session = request.getSession();
    String cursorAction = request.getParameter("cursor");

    // Clears cursors if no cursor action is passed or a clear action is passed
    if (cursorAction == null || cursorAction.equals("clear")) {
      session.removeAttribute("startCursor");
      session.removeAttribute("endCursor");
    }

    // initializes startCursor to the appropriate cursor location
    String startCursor = null;
    if (cursorAction.equals("start")) {
      startCursor = (String) session.getAttribute("startCursor");
    } else if (cursorAction.equals("end")) {
      startCursor = (String) session.getAttribute("endCursor");
    }

    // sets cursor in fetchOptions and stores the new startCursor in the session
    if (startCursor != null) {
      fetchOptions.startCursor(Cursor.fromWebSafeString(startCursor));
      session.setAttribute("startCursor", startCursor);
    }

    QueryResultList<Entity> results;
    try {
      results = datastore.prepare(query).asQueryResultList(fetchOptions);
    } catch (IllegalArgumentException e) {
      response.sendRedirect("/index.jsp");
      return;
    }

    for (Entity entity : results) {
      taskGroup.addTask(entity);
    }

    // Stores end cursor and checks if the end of the query has been reached
    session.setAttribute("endCursor", results.getCursor().toWebSafeString());
    taskGroup.checkIfEnd();

    Gson gson = new Gson();
    String json = gson.toJson(taskGroup);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    // First check whether the user is logged in
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      response.sendRedirect(userService.createLoginURL("/"));
      return;
    }

    // If the user still hasn't save their info, it forwards the request to create an account
    List<String> userInfo = RetrieveUserInfo.getInfo(userService);
    if (userInfo == null) {
      RequestDispatcher rd = request.getRequestDispatcher("/account.jsp");
      rd.forward(request, response);
      return;
    }

    // Get the rewarding points from the form
    int rewardPts;
    try {
      rewardPts = RewardingPoints.get(request, "reward-input");
    } catch (IllegalArgumentException e) {
      response.setContentType("text/html");
      response.getWriter().println("Please enter a valid integer in the range of 0-200");
      return;
    }

    // Get task category from the form input
    String taskCategory = request.getParameter("category-input");
    if (taskCategory == null || taskCategory.isEmpty()) {
      System.err.println("The task must have a category");
      return;
    }

    // Get the task detail from the form input
    String taskDetail = "";
    String input = request.getParameter("task-detail-input");
    // If the input is valid, set the taskDetail value to the input value
    if (input != null) {
      taskDetail = input.trim();
    }

    // If input task detail is empty, reject the request to add a new task.
    if (taskDetail.equals("")) {
      System.err.println("The input task detail is empty");
      return;
    }

    // Get the task overview from the form input
    String taskOverview = "";
    input = request.getParameter("task-overview-input");
    // If the input is valid, set the taskOverview value to the input value
    if (input != null) {
      taskOverview = input.trim();
    }

    // If input task overview is empty, reject the request to add a new task.
    if (taskOverview.equals("")) {
      System.err.println("The input task overview is empty");
      return;
    }

    long creationTime = System.currentTimeMillis();

    String userId = userService.getCurrentUser().getUserId();

    // Creates current user entity key to include as the task's parent
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key userEntityKey = KeyFactory.createKey("UserInfo", userId);

    Entity userEntity;
    try {
      userEntity = datastore.get(userEntityKey);
    } catch (EntityNotFoundException e) {
      System.err.println("Unable to find the UserInfo entity based on the current user id");
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND, "The requested user info could not be found");
      return;
    }

    String formattedAddress = (String) userEntity.getProperty("address");
    String country = (String) userEntity.getProperty("country");
    String zipcode = (String) userEntity.getProperty("zipcode");

    // Create an Entity that stores the input comment
    Entity taskEntity = new Entity("Task", userEntity.getKey());
    taskEntity.setProperty("detail", taskDetail);
    taskEntity.setProperty("overview", taskOverview);
    taskEntity.setProperty("timestamp", creationTime);
    taskEntity.setProperty("reward", rewardPts);
    taskEntity.setProperty("status", "OPEN");
    taskEntity.setProperty("Owner", userId);
    taskEntity.setProperty("Helper", "N/A");
    taskEntity.setProperty("Address", formattedAddress);
    taskEntity.setProperty("zipcode", zipcode);
    taskEntity.setProperty("country", country);
    taskEntity.setProperty("category", taskCategory);

    datastore.put(taskEntity);

    // Redirect back to the user page.
    response.sendRedirect("/user_profile.jsp");
  }

  @Override
  public void doDelete(HttpServletRequest request, HttpServletResponse response)
      throws IOException {
    String keyString = request.getParameter("key");

    Key taskKey = KeyFactory.stringToKey(keyString);

    // TODO: Handle the exceptional case where the user attempts to delete a non-existent task.
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.delete(taskKey);

    // Redirect to the user profile page
    response.sendRedirect("/user_profile.jsp");
  }
}
