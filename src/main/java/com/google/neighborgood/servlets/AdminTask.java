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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.neighborgood.helper.RetrieveUserInfo;
import java.io.IOException;
import java.util.List;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that creates new task entity and fetch saved tasks. */
@WebServlet("/admin-tasks")
public class AdminTask extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    // First check whether the admin is logged in
    UserService userService = UserServiceFactory.getUserService();

    if (!userService.isUserLoggedIn()) {
      response.sendRedirect(userService.createLoginURL("/"));
      return;
    }
    List<String> userInfo = RetrieveUserInfo.getInfo(userService);
    if (userInfo == null) {
      response.sendRedirect("account.jsp");
      return;
    }

    // Get the task detail from the form input
    String owner = request.getParameter("admin-owner-input");
    String date = request.getParameter("admin-date");
    String detail = request.getParameter("admin-detail-input");
    String time = request.getParameter("admin-time");

    long creationTime = System.currentTimeMillis();

    // Create an Entity that stores the input comment
    Entity taskEntity = new Entity("adminTask");
    taskEntity.setProperty("detail", detail);
    taskEntity.setProperty("date", date);
    taskEntity.setProperty("time", time);
    taskEntity.setProperty("owner", owner);

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    datastore.put(taskEntity);

    // Redirect back to the admin page.
    response.sendRedirect(request.getHeader("Referer"));
  }
}
