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

import com.google.appengine.api.datastore.Cursor;
import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.CompositeFilterOperator;
import com.google.appengine.api.datastore.Query.Filter;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.QueryResultList;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.neighborgood.TaskResponse;
import com.google.neighborgood.task.Task;
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
  private static final int PAGE_SIZE = 5;

  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String keyword = request.getParameter("keyword");
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect(userService.createLoginURL("/account.jsp"));
      return;
    }

    // Initiate a fetchOption with a fetch limit equals to 5
    FetchOptions fetchOption = FetchOptions.Builder.withLimit(PAGE_SIZE);

    // If the client requires for a cursor, get the cursor string given. The user should provide
    // two cursor strings, one for the first query, and another one for the second query.
    String firstStartCursor = request.getParameter("firstcursor");
    String secondStartCursor = request.getParameter("secondcursor");
    if (firstStartCursor != null) {
      fetchOption.startCursor(Cursor.fromWebSafeString(firstStartCursor));
    }

    String complete = request.getParameter("complete");
    String[] trueStatus = new String[] {"COMPLETE: AWAIT VERIFICATION", "COMPLETE"};
    String[] falseStatus = new String[] {"OPEN", "IN PROGRESS"};
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    List<Task> myTasks = new ArrayList<>();
    List<String> cursorStrings = new ArrayList<>();

    for (int i = 0; i < 2; i++) {
      // keyword is either "Owner" or "Helper". Depending on the keyword, the filter will filter
      // on different fields accordingly.
      Filter filter =
          new FilterPredicate(
              keyword, FilterOperator.EQUAL, userService.getCurrentUser().getUserId());
      // Depending on the input complete parameter, the status filter will be different.
      if (complete.equals("True")) {
        Filter statusFilter = new FilterPredicate("status", FilterOperator.EQUAL, trueStatus[i]);
        filter = CompositeFilterOperator.and(filter, statusFilter);
      } else {
        Filter statusFilter = new FilterPredicate("status", FilterOperator.EQUAL, falseStatus[i]);
        filter = CompositeFilterOperator.and(filter, statusFilter);
      }

      Query query =
          new Query("Task").setFilter(filter).addSort("timestamp", SortDirection.DESCENDING);

      PreparedQuery pq = datastore.prepare(query);

      QueryResultList<Entity> results;
      try {
        results = pq.asQueryResultList(fetchOption);
      } catch (IllegalArgumentException e) {
        System.err.println("Invalid cursor is provided");
        return;
      }

      for (Entity entity : results) {
        myTasks.add(new Task(entity));
      }

      String cursorString = results.getCursor().toWebSafeString();
      cursorStrings.add(cursorString);

      // If there are less then 5 tasks fetched in the first loop, fetch more tasks in the second
      // loop. Also, set the cursor to the second cursor string provided for the second query.
      fetchOption = FetchOptions.Builder.withLimit(PAGE_SIZE - myTasks.size());
      if (secondStartCursor != null) {
        fetchOption.startCursor(Cursor.fromWebSafeString(secondStartCursor));
      }
    }

    Gson gson = new Gson();
    String json = gson.toJson(new TaskResponse(cursorStrings, myTasks));
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }
}
