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
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles the request for editing the details and rewards for a certain task. */
@WebServlet("/tasks/edit")
public class EditTaskServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    String keyString = request.getParameter("task-id");
    int rewardPts = getRewardingPoints(request, "reward-input");
    if (rewardPts == -1) {
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

    // If input task detail is empty, reject the request to edit and send a 400 error.
    if (taskDetail.equals("")) {
      System.err.println("The input task detail is empty");
      response.sendRedirect("/400.html");
      return;
    }

    Key taskKey = KeyFactory.stringToKey(keyString);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Entity task;
    try {
      task = datastore.get(taskKey);
    } catch (EntityNotFoundException e) {
      System.err.println("Unable to find the entity based on the input key");
      response.sendError(HttpServletResponse.SC_NOT_FOUND, "The requested task could not be found");
      return;
    }

    // Set the details and rewards to the newly input value
    task.setProperty("detail", taskDetail);
    task.setProperty("reward", rewardPts);
    datastore.put(task);

    response.sendRedirect("/user_profile.html");
  }

  // Both TaskServlet and EditTaskServlet use this method. I will fix this by putting the function
  // in a separate
  // class in the next PR.
  /** Return the input rewarding points by the user, or -1 if the input was invalid */
  private int getRewardingPoints(HttpServletRequest request, String inputName) {
    // Get the input from the form.
    String rewardPtsString = request.getParameter(inputName);

    // Convert the input to an int.
    int rewardPts;
    try {
      rewardPts = Integer.parseInt(rewardPtsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + rewardPtsString);
      return -1;
    }

    // Check that the input is within the requested range.
    if (rewardPts < 0 || rewardPts > 200) {
      System.err.println("User input is out of range: " + rewardPtsString);
      return -1;
    }

    return rewardPts;
  }
}
