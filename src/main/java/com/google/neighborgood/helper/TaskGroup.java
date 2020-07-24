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

package com.google.neighborgood.helper;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.HashMap;

/**
 * Helper class that stores the HTML depiction of tasks in groups of 10 or less along with storing
 * the current position of the query's cursor and whether or not the end of the query has been
 * reached
 */
public class TaskGroup {
  private static HashMap<String, String>
      usersNicknames; // Stores task owner's user info to prevent querying multiple times in
  // datastore for the same user's info
  private static DatastoreService datastore;
  private final boolean userLoggedIn;
  private final String userId;
  private int currentTaskCount;
  private StringBuilder tasks; // String-like representation of 10 tasks
  private String endCursor;
  private String startCursor;
  private boolean endOfQuery;

  public TaskGroup() {
    this.usersNicknames = new HashMap<String, String>();
    UserService userService = UserServiceFactory.getUserService();
    this.userLoggedIn = userService.isUserLoggedIn();
    this.userId = this.userLoggedIn ? userService.getCurrentUser().getUserId() : "null";
    this.datastore = DatastoreServiceFactory.getDatastoreService();
    this.tasks = new StringBuilder();
    this.currentTaskCount = 0;
    this.endOfQuery = false;
  }

  /** addTask method adds a single task HTML string to the tasks variable */
  public void addTask(Entity entity) {

    tasks
        .append("<div class='task' data-key='")
        .append(KeyFactory.keyToString(entity.getKey()))
        .append("'>");
    if (this.userLoggedIn) {
      tasks.append("<div class='help-overlay'>");
      tasks.append("<div class='exit-help'><a>&times</a></div>");
      tasks.append("<a class='confirm-help'>CONFIRM</a>");
      tasks.append("</div>");
    }
    tasks.append("<div class='task-container'>");
    tasks.append("<div class='task-header'>");
    tasks.append("<div class='user-nickname'>");

    // Checks if tasks's user nickname has already been retrieved,
    // otherwise retrieves it and temporarily stores it
    String taskOwner = (String) entity.getProperty("Owner");
    if (this.usersNicknames.containsKey(taskOwner)) {
      tasks.append(this.usersNicknames.get(taskOwner));
    } else {
      Key taskOwnerKey = entity.getParent();
      try {
        Entity userEntity = this.datastore.get(taskOwnerKey);
        String userNickname = (String) userEntity.getProperty("nickname");
        this.usersNicknames.put(taskOwner, userNickname);
        tasks.append(userNickname);
      } catch (EntityNotFoundException e) {
        System.err.println(
            "Unable to find the task's owner info to retrieve the owner's nickname. Setting a default nickname.");
        tasks.append("Neighbor");
      }
    }
    tasks.append("</div>");
    if (this.userLoggedIn) {
      // changes the Help Button div if the current user is the owner of the task
      if (!this.userId.equals(taskOwner)) {
        tasks.append("<div class='help-out'>HELP OUT</div>");
      } else {
        tasks.append(
            "<div class='help-out disable-help' title='This is your own task'>HELP OUT</div>");
      }
    }
    tasks.append("</div>");
    tasks
        .append("<div class='task-content'>")
        .append((String) entity.getProperty("overview"))
        .append("</div>");
    tasks
        .append("<div class='task-footer'><div class='task-category'>#")
        .append((String) entity.getProperty("category"))
        .append("</div>");

    Timestamp timestamp = new Timestamp((Long) entity.getProperty("timestamp"));
    SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm MM-dd-yyyy");

    tasks
        .append("<div class='task-date-time'>")
        .append((String) timestampFormat.format(timestamp))
        .append("</div></div></div></div>");

    this.currentTaskCount++;
  }

  /** Adds end cursor position to TaskGroup instance */
  public void addEndCursor(String cursor) {
    this.endCursor = cursor;
  }

  /** Adds start cursor position to TaskGroup instance */
  public void addStartCursor(String cursor) {
    this.startCursor = cursor;
  }

  public void checkIfEnd() {
    if (this.currentTaskCount < 10) {
      this.endOfQuery = true;
    }
  }
}
