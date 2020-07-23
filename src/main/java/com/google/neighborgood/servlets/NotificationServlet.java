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
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.neighborgood.Notification;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that loads new notification entities. */
@WebServlet("/notifications")
public class NotificationServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // Make sure that the user has already logged into his account
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect(userService.createLoginURL("/account.jsp"));
      return;
    }

    // Get all the notifications whose receiver is the current user
    Filter filter =
        new FilterPredicate(
            "receiver", FilterOperator.EQUAL, userService.getCurrentUser().getUserId());
    Query query = new Query("Notification").setFilter(filter);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    Map<String, Integer> notifications = new HashMap<>();
    for (Entity entity : results.asIterable()) {
      String taskId = (String) entity.getProperty("taskId");
      notifications.put(taskId, notifications.getOrDefault(taskId, 0) + 1);
    }

    List<Notification> finalResult = new ArrayList<>();
    for (Map.Entry<String, Integer> entry : notifications.entrySet()) {
      finalResult.add(new Notification(entry.getKey(), entry.getValue()));
    }

    Gson gson = new Gson();
    String json = gson.toJson(finalResult);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
