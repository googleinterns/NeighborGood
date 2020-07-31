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
import com.google.appengine.api.datastore.Query.CompositeFilter;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.neighborgood.data.Task;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that fetch all saved tasks whose owner is the current user. */
@WebServlet("/mytasks")
public class MyTaskServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String keyword = request.getParameter("keyword");
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect(userService.createLoginURL("/account.jsp"));
      return;
    }
    // keyword is either "Owner" or "Helper". Depending on the keyword, the filter will filter
    // on different fields accordingly.
    Filter filter =
        new FilterPredicate(
            keyword, FilterOperator.EQUAL, userService.getCurrentUser().getUserId());

    // Depending on the input complete parameter, the status filter will be different.
    String complete = request.getParameter("complete");
    if (complete.equals("True")) {
      Filter openFilter = new FilterPredicate("status", FilterOperator.EQUAL, "COMPLETE");
      Filter inProgressFilter =
          new FilterPredicate("status", FilterOperator.EQUAL, "COMPLETE: AWAIT VERIFICATION");
      CompositeFilter statusFilter = CompositeFilterOperator.or(openFilter, inProgressFilter);
      filter = CompositeFilterOperator.and(filter, statusFilter);
    } else {
      Filter openFilter = new FilterPredicate("status", FilterOperator.EQUAL, "OPEN");
      Filter inProgressFilter = new FilterPredicate("status", FilterOperator.EQUAL, "IN PROGRESS");
      CompositeFilter statusFilter = CompositeFilterOperator.or(openFilter, inProgressFilter);
      filter = CompositeFilterOperator.and(filter, statusFilter);
    }

    Query query =
        new Query("Task").addSort("timestamp", SortDirection.DESCENDING).setFilter(filter);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    PreparedQuery results = datastore.prepare(query);

    List<Task> myTasks = new ArrayList<>();
    for (Entity entity : results.asIterable()) {
      myTasks.add(new Task(entity));
    }

    Gson gson = new Gson();
    String json = gson.toJson(myTasks);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
