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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.GeoPt;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.neighborgood.helper.RetrieveUserInfo;
import com.google.neighborgood.helper.RewardingPoints;
import com.google.neighborgood.helper.UnitConversion;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
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
    Float lat = null;
    Float lng = null;
    double mile_radius = UnitConversion.milesToMeters(5);

    if (request.getParameterMap().containsKey("lat")
        && request.getParameterMap().containsKey("lng")) {
      try {
        lat = Float.parseFloat(request.getParameter("lat"));
        lng = Float.parseFloat(request.getParameter("lng"));
      } catch (NumberFormatException e) {
        System.err.println("Invalid location coordinates");
        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid location coordinates");
      }
    } else {
      System.err.println("Location coordinates are missing");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Location coordinates are missing");
    }

    if (request.getParameterMap().containsKey("miles")) {
      double miles = 5;
      try {
        miles = Double.parseDouble(request.getParameter("miles"));
      } catch (NumberFormatException e) {
        System.err.println("Invalid miles input. Using 5 miles as default.");
      }
      mile_radius = UnitConversion.milesToMeters(miles);
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    GeoPt userLocation = new GeoPt(lat, lng);

    Query query = new Query("Task").addSort("timestamp", SortDirection.DESCENDING);

    // Creates list of filters
    List<Query.Filter> filters = new ArrayList<Query.Filter>();
    filters.add(
        new Query.StContainsFilter(
            "location", new Query.GeoRegion.Circle(userLocation, mile_radius)));
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

    // Stores task owner's user info to prevent querying multiple
    // times in datastore for the same user's info
    HashMap<String, String> usersNicknames = new HashMap<String, String>();

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
      out.append("<div class='user-nickname'>");

      // Checks if tasks's user nickname has already been retrieved,
      // otherwise retrieves it and temporarily stores it
      String taskOwner = (String) entity.getProperty("Owner");
      if (usersNicknames.containsKey(taskOwner)) {
        out.append(usersNicknames.get(taskOwner));
      } else {
        Key taskOwnerKey = entity.getParent();
        try {
          Entity userEntity = datastore.get(taskOwnerKey);
          String userNickname = (String) userEntity.getProperty("nickname");
          usersNicknames.put(taskOwner, userNickname);
          out.append(userNickname);
        } catch (EntityNotFoundException e) {
          System.err.println(
              "Unable to find the task's owner info to retrieve the owner's nickname. Setting a default nickname.");
          out.append("Neighbor");
        }
      }
      out.append("</div>");
      if (userLoggedIn) {
        // changes the Help Button div if the current user is the owner of the task
        if (!userId.equals(taskOwner)) {
          out.append("<div class='help-out'>HELP OUT</div>");
        } else {
          out.append(
              "<div class='help-out disable-help' title='This is your own task'>HELP OUT</div>");
        }
      }
      out.append("</div>");
      out.append(
              "<div class='task-content' onclick='showTaskInfo(\""
                  + KeyFactory.keyToString(entity.getKey())
                  + "\")'>")
          .append((String) entity.getProperty("overview"))
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

    // I will work on a different implementation for this after the MVP since
    // this implementation forces users to re-input their task details
    // if they haven't ever inputted their user info and are automatically
    // logged in when opening the page.
    List<String> userInfo = RetrieveUserInfo.getInfo(userService);
    if (userInfo == null) {
      response.sendRedirect("account.jsp");
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
    GeoPt location = (GeoPt) userEntity.getProperty("location");

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
    taskEntity.setProperty("location", location);
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
