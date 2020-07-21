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
import java.util.HashMap;

public final class TaskList {

  private static HashMap<String, String> usersNicknames;
  private StringBuilder taskListString;
  private boolean endOfResults;
  private final boolean userLoggedIn;
  private final String userId;
  private static DatastoreService datastore;
  private int pageCount;

  public TaskList() {
    this.usersNicknames = new HashMap<String, String>();
    this.taskListString = new StringBuilder();
    UserService userService = UserServiceFactory.getUserService();
    this.userLoggedIn = userService.isUserLoggedIn();
    this.userId = this.userLoggedIn ? userService.getCurrentUser().getUserId() : "null";
    this.datastore = DatastoreServiceFactory.getDatastoreService();
    this.endOfResults = false;
    this.pageCount = 1;
  }

  public void addTask(Entity entity) {
    taskListString
        .append("<div class='task' data-key='")
        .append(KeyFactory.keyToString(entity.getKey()))
        .append("'>");
    if (this.userLoggedIn) {
      taskListString.append("<div class='help-overlay'>");
      taskListString.append("<div class='exit-help'><a>&times</a></div>");
      taskListString.append("<a class='confirm-help'>CONFIRM</a>");
      taskListString.append("</div>");
    }
    taskListString.append("<div class='task-container'>");
    taskListString.append("<div class='task-header'>");
    taskListString.append("<div class='user-nickname'>");

    // Checks if tasks's user nickname has already been retrieved,
    // otherwise retrieves it and temporarily stores it
    String taskOwner = (String) entity.getProperty("Owner");
    if (this.usersNicknames.containsKey(taskOwner)) {
      taskListString.append(this.usersNicknames.get(taskOwner));
    } else {
      Key taskOwnerKey = entity.getParent();
      try {
        Entity userEntity = datastore.get(taskOwnerKey);
        String userNickname = (String) userEntity.getProperty("nickname");
        this.usersNicknames.put(taskOwner, userNickname);
        taskListString.append(userNickname);
      } catch (EntityNotFoundException e) {
        System.err.println(
            "Unable to find the task's owner info to retrieve the owner's nickname. Setting a default nickname.");
        taskListString.append("Neighbor");
      }
    }
    taskListString.append("</div>");
    if (this.userLoggedIn) {
      // changes the Help Button div if the current user is the owner of the task
      if (!this.userId.equals(taskOwner)) {
        taskListString.append("<div class='help-out'>HELP OUT</div>");
      } else {
        taskListString.append(
            "<div class='help-out disable-help' title='This is your own task'>HELP OUT</div>");
      }
    }
    taskListString.append("</div>");
    taskListString
        .append(
            "<div class='task-content' onclick='showTaskInfo(\""
                + KeyFactory.keyToString(entity.getKey())
                + "\")'>")
        .append((String) entity.getProperty("overview"))
        .append("</div>");
    taskListString
        .append("<div class='task-footer'><div class='task-category'>#")
        .append((String) entity.getProperty("category"))
        .append("</div></div>");
    taskListString.append("</div></div>");
  }

  public void setEndOfResults() {
    this.endOfResults = true;
  }

  public void setPageCount(int pagecount) {
    this.pageCount = pagecount;
  }
}
