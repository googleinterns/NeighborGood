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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.neighborgood.helper.RewardingPoints;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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

    // limits results to the 20 most recent tasks
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(20));

    StringBuilder out = new StringBuilder();

    // Builds and stores HTML for each task
    for (Entity entity : results) {
      out.append("<div class='task' data-key='")
          .append(KeyFactory.keyToString(entity.getKey()))
          .append("'>");
      if (userLoggedIn) {
        out.append("<div class='help-overlay'>");
        out.append("<div class='exit-help'><a>&times</a></div>");
        out.append("<a class='confirm-help'>CONFIRM</a>");
        out.append("</div>");
      }
      out.append("<div class='task-container'>");
      out.append("<div class='task-header'>");
      out.append("<div class='username'>")
          .append((String) entity.getProperty("Owner"))
          .append("</div>");
      if (userLoggedIn) {
        // changes the Help Button div if the current user is the owner of the task
        if (!userId.equals((String) entity.getProperty("userId"))) {
          out.append("<div class='help-out'>HELP OUT</div>");
        } else {
          out.append(
              "<div class='help-out disable-help' title='This is your own task'>HELP OUT</div>");
        }
      }
      out.append("</div>");
      out.append("<div class='task-content'>")
          .append((String) entity.getProperty("detail"))
          .append("</div>");
      out.append("<div class='task-footer'><div class='task-category'>#")
          .append((String) entity.getProperty("category"))
          .append("</div></div>");
      out.append("</div></div>");
    }

    Gson gson = new Gson();
    String json = gson.toJson(out.toString());
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // First check whether the user is logged in
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      response.sendRedirect(userService.createLoginURL("/"));
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

    // Get the task detail from the form input
    String taskDetail = "";
    String input = request.getParameter("task-detail-input");
    // If the input is valid, set the taskDetail value to the input value
    if (input != null) {
      taskDetail = input.trim();
    }

    // If input task detail is empty, reject the request to add a new task and send a 400 error.
    if (taskDetail.equals("")) {
      System.err.println("The input task detail is empty");
      response.sendRedirect("/400.html");
      return;
    }

    long creationTime = System.currentTimeMillis();

    String userId = userService.getCurrentUser().getUserId();

    // Create an Entity that stores the input comment
    Entity taskEntity = new Entity("Task");
    taskEntity.setProperty("userId", userId);
    taskEntity.setProperty("detail", taskDetail);
    taskEntity.setProperty("timestamp", creationTime);
    taskEntity.setProperty("reward", rewardPts);
    taskEntity.setProperty("status", "OPEN");
    taskEntity.setProperty("Owner", userId);
    taskEntity.setProperty("Helper", "N/A");
    taskEntity.setProperty("Address", "4xxx Cxxxxx Avenue, Pittsburgh, PA 15xxx");
    taskEntity.setProperty("zipcode", "98033");
    taskEntity.setProperty("country", "United States");
    taskEntity.setProperty("category", "misc");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
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
