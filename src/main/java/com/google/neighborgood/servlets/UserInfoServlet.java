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

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.FetchOptions;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.memcache.ErrorHandlers;
import com.google.appengine.api.memcache.MemcacheService;
import com.google.appengine.api.memcache.MemcacheServiceFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;
import com.google.neighborgood.data.User;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import javax.servlet.RequestDispatcher;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/account")
public class UserInfoServlet extends HttpServlet {
  @Override
  public void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    UserService userService = UserServiceFactory.getUserService();

    // Retrieves user accounts for the topscorers board
    if (request.getParameterMap().containsKey("action")
        && request.getParameter("action").equals("topscorers")) {
      List<User> users = retrieveTopTenUsers(request, userService, datastore);
      Gson gson = new Gson();
      response.setContentType("application/json;");
      response.getWriter().println(gson.toJson(users));
      return;
    }

    if (!userService.isUserLoggedIn()) {
      response.sendRedirect(userService.createLoginURL("/account.jsp"));
      return;
    }

    Key userEntityKey = KeyFactory.createKey("UserInfo", userService.getCurrentUser().getUserId());
    Entity entity;
    try {
      entity = datastore.get(userEntityKey);
    } catch (EntityNotFoundException e) {
      System.err.println("Unable to find the UserInfo entity based on the current user id");
      response.sendError(
          HttpServletResponse.SC_NOT_FOUND, "The requested user info could not be found");
      return;
    }

    List<String> result = new ArrayList<>();
    result.add((String) entity.getProperty("nickname"));
    result.add((String) entity.getProperty("address"));
    result.add((String) entity.getProperty("zipcode"));
    result.add((String) entity.getProperty("country"));

    Gson gson = new Gson();
    String json = gson.toJson(result);
    response.setContentType("application/json;");
    response.getWriter().println(json);
  }

  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response)
      throws IOException, ServletException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect(userService.createLoginURL("/account.jsp"));
      return;
    }

    String nickname = "";
    String address = "";
    String zipcode = "";
    String country = "";
    Double lat = null;
    Double lng = null;
    String nicknameInput = request.getParameter("nickname-input");
    String addressInput = request.getParameter("address-input");
    String zipcodeInput = request.getParameter("zipcode-input");
    String countryInput = request.getParameter("country-input");
    String email = userService.getCurrentUser().getEmail();
    String userId = userService.getCurrentUser().getUserId();

    if (nicknameInput != null) nickname = nicknameInput.trim();
    if (addressInput != null) address = addressInput.trim();
    if (zipcodeInput != null) zipcode = zipcodeInput.trim();
    if (countryInput != null) country = countryInput.trim();

    try {
      lat = Double.parseDouble(request.getParameter("lat"));
      lng = Double.parseDouble(request.getParameter("lng"));
    } catch (NumberFormatException e) {
      System.err.println("Invalid location coordinates");
      response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid location coordinates");
    }

    if (nickname.equals("") || address.equals("") || country.equals("") || zipcode.equals("")) {
      System.err.println("At least one input field is empty");
      return;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key userEntityKey = KeyFactory.createKey("UserInfo", userId);
    Entity entity = null;
    try {
      entity = datastore.get(userEntityKey);
    } catch (EntityNotFoundException e) {
      System.out.println("UserInfo entity does not exist. Creating a new one...");
    }

    if (entity == null) {
      entity = new Entity("UserInfo", userId);
      entity.setProperty("nickname", nickname);
      entity.setProperty("address", address);
      entity.setProperty("email", email);
      entity.setProperty("country", country);
      entity.setProperty("zipcode", zipcode);
      entity.setProperty("lat", lat);
      entity.setProperty("lng", lng);
      entity.setProperty("points", 0);
    } else {
      entity.setProperty("nickname", nickname);
      entity.setProperty("address", address);
      entity.setProperty("country", country);
      entity.setProperty("zipcode", zipcode);
      entity.setProperty("lat", lat);
      entity.setProperty("lng", lng);

      MemcacheService syncCache = MemcacheServiceFactory.getMemcacheService();
      syncCache.setErrorHandler(ErrorHandlers.getConsistentLogAndContinue(Level.INFO));
      syncCache.put(userId, nickname);
    }
    datastore.put(entity);

    // If task details were forwarded, then forward this request back to /tasks
    if (request.getParameterMap().containsKey("task-overview-input")) {
      RequestDispatcher rd = request.getRequestDispatcher("/tasks");
      rd.forward(request, response);
      return;
    }
    response.sendRedirect("/user_profile.jsp");
  }

  private List<User> retrieveTopTenUsers(
      HttpServletRequest request, UserService userService, DatastoreService datastore) {

    Query query = new Query("UserInfo").addSort("points", SortDirection.DESCENDING);

    // Adds additional filters for the nearby neighbors board
    if (request.getParameterMap().containsKey("zipcode")
        && request.getParameterMap().containsKey("country")) {
      String zipcode = request.getParameter("zipcode");
      String country = request.getParameter("country");
      List<Query.Filter> filters = new ArrayList<Query.Filter>();
      filters.add(new Query.FilterPredicate("zipcode", Query.FilterOperator.EQUAL, zipcode));
      filters.add(new Query.FilterPredicate("country", Query.FilterOperator.EQUAL, country));
      query.setFilter(new Query.CompositeFilter(Query.CompositeFilterOperator.AND, filters));
    }

    // Gathers the top 10 results
    List<Entity> results = datastore.prepare(query).asList(FetchOptions.Builder.withLimit(10));

    List<User> users = new ArrayList<>();

    for (Entity entity : results) {
      User user = new User(entity);
      if (userService.isUserLoggedIn()
          && user.getUserId().equals(userService.getCurrentUser().getUserId())) {
        user.setCurrentUser();
      }
      users.add(user);
    }
    return users;
  }
}
