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
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Helper class that stores the HTML depiction of tasks in groups of 10 per ArrayList index to
 * assist with pagination
 */
public class TaskPages {
  private static HashMap<String, String>
      usersNicknames; // Stores task owner's user info to prevent querying multiple times in
  // datastore for the same user's info
  private static DatastoreService datastore;
  private final boolean userLoggedIn;
  private final String userId;
  private int pageCount;
  private int taskCount;
  private StringBuilder page; // String-like representation of a page's worth of tasks
  private ArrayList<String> taskPages;

  public TaskPages() {
    this.usersNicknames = new HashMap<String, String>();
    UserService userService = UserServiceFactory.getUserService();
    this.userLoggedIn = userService.isUserLoggedIn();
    this.userId = this.userLoggedIn ? userService.getCurrentUser().getUserId() : "null";
    this.datastore = DatastoreServiceFactory.getDatastoreService();
    this.taskPages = new ArrayList<String>();
    this.page = new StringBuilder();
    this.taskCount = 0;
    this.pageCount = 0;
  }

  /** addTask method adds a single task HTML string to the page variable */
  public void addTask(Entity entity) {

    // adds current page to taskPages and starts new page if task count for current page reached 10
    if (this.taskCount % 10 == 0 && this.taskCount != 0) {
      this.taskPages.add(page.toString());
      this.page = new StringBuilder();
      this.pageCount++;
    }

    page.append("<div class='task' data-key='")
        .append(KeyFactory.keyToString(entity.getKey()))
        .append("'>");
    if (this.userLoggedIn) {
      page.append("<div class='help-overlay'>");
      page.append("<div class='exit-help'><a>&times</a></div>");
      page.append("<a class='confirm-help'>CONFIRM</a>");
      page.append("</div>");
    }
    page.append("<div class='task-container'>");
    page.append("<div class='task-header'>");
    page.append("<div class='user-nickname'>");

    // Checks if tasks's user nickname has already been retrieved,
    // otherwise retrieves it and temporarily stores it
    String taskOwner = (String) entity.getProperty("Owner");
    if (this.usersNicknames.containsKey(taskOwner)) {
      page.append(this.usersNicknames.get(taskOwner));
    } else {
      Key taskOwnerKey = entity.getParent();
      try {
        Entity userEntity = this.datastore.get(taskOwnerKey);
        String userNickname = (String) userEntity.getProperty("nickname");
        this.usersNicknames.put(taskOwner, userNickname);
        page.append(userNickname);
      } catch (EntityNotFoundException e) {
        System.err.println(
            "Unable to find the task's owner info to retrieve the owner's nickname. Setting a default nickname.");
        page.append("Neighbor");
      }
    }
    page.append("</div>");
    if (this.userLoggedIn) {
      // changes the Help Button div if the current user is the owner of the task
      if (!this.userId.equals(taskOwner)) {
        page.append("<div class='help-out'>HELP OUT</div>");
      } else {
        page.append(
            "<div class='help-out disable-help' title='This is your own task'>HELP OUT</div>");
      }
    }
    page.append("</div>");
    page.append("<div class='task-content'>")
        .append((String) entity.getProperty("overview"))
        .append("</div>");
    page.append("<div class='task-footer'><div class='task-category'>#")
        .append((String) entity.getProperty("category"))
        .append("</div>");

    Timestamp timestamp = new Timestamp((Long) entity.getProperty("timestamp"));
    SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm MM-dd-yyyy");

    page.append("<div class='task-date-time'>")
        .append((String) timestampFormat.format(timestamp))
        .append("</div></div></div></div>");

    this.taskCount++;
  }

  /**
   * If there are no more tasks to add, this method can be used to add the current/last page onto
   * the taskPages ArrayList
   */
  public void endPages() {
    this.taskPages.add(page.toString());
    this.pageCount++;
  }
}
