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

package com.google.sps.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.SortDirection;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.gson.Gson;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/** Servlet that handles the request for editing the details and rewards for a certain task. */
@WebServlet("/tasks/edit")
public class EditTaskServlet extends HttpServlet {
    @Override
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        System.out.println("Modify");
        String keyString = request.getParameter("task-id");
        int rewardPts = getRewardingPoints(request, "edit-input");
        if (rewardPts == -1) {
            response.setContentType("text/html");
            response.getWriter().println("Please enter a valid integer in the range of 0-200");
            return;
        }

        // Get the task detail from the form input
        String taskDetail = "";
        String input = request.getParameter("edit-content-input");
        if (input != null) {
            taskDetail = input;
        }

        // If the input is nonempty and valid, set the taskDetail value to the input value
        if (!taskDetail.equals("")) {
            Key taskKey = KeyFactory.stringToKey(keyString);
            DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
            Entity task;
            try {
                task = datastore.get(taskKey);
            } catch (EntityNotFoundException e) {
                System.err.println("Unable to find the entity based on the input key");
                response.sendError(HttpServletResponse.SC_NOT_FOUND, "The entity is not found in the database");
                return;
            }

            // Set the details and rewards to the newly input value
            task.setProperty("detail", taskDetail);
            task.setProperty("reward", rewardPts);
            datastore.put(task);
        }
        response.sendRedirect("/user_profile.html");
    }

    // Both TaskServlet and EditTaskServlet use this method. I will fix this by putting the function in a separate
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