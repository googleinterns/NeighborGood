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
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.neighborgood.User;
import com.google.neighborgood.task.Task;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Helper class that stores the HTML depiction of tasks in groups of 10 or less along with whether
 * or not the end of the query has been reached
 */
public class TaskGroup {
  private static Map<String, User>
      usersInfo; // Stores task owner's user info to prevent querying multiple times in
  // datastore for the same user's info
  private static DatastoreService datastore;
  private final boolean userLoggedIn;
  private int currentTaskCount;
  private boolean endOfQuery;
  private List<Task> tasks;

  public TaskGroup() {
    this.usersInfo = new HashMap<String, User>();
    UserService userService = UserServiceFactory.getUserService();
    this.userLoggedIn = userService.isUserLoggedIn();
    this.datastore = DatastoreServiceFactory.getDatastoreService();
    this.currentTaskCount = 0;
    this.endOfQuery = false;
    this.tasks = new ArrayList<>();
  }

  /** addTask method adds a single task HTML string to the tasks variable */
  public void addTask(Entity entity) {

    String taskOwnerId = (String) entity.getProperty("Owner");
    String taskOwnerNickname = null;
    Double taskLat = null;
    Double taskLng = null;
    if (this.usersInfo.containsKey(taskOwnerId)) {
      taskOwnerNickname = this.usersInfo.get(taskOwnerId).getUserNickname();
      taskLat = this.usersInfo.get(taskOwnerId).getUserLat();
      taskLng = this.usersInfo.get(taskOwnerId).getUserLng();
    } else {
      Key taskOwnerKey = entity.getParent();
      try {
        Entity userEntity = this.datastore.get(taskOwnerKey);
        User taskUser = new User(userEntity);
        taskOwnerNickname = taskUser.getUserNickname();
        taskLat = taskUser.getUserLat();
        taskLng = taskUser.getUserLng();
        this.usersInfo.put(taskOwnerId, taskUser);
      } catch (EntityNotFoundException e) {
        System.err.println(
            "Unable to find the task's owner info to retrieve the owner's nickname. Setting a default nickname.");
        taskOwnerNickname = "Your Friendly Neighbor";
      }
    }
    Task task = new Task(entity, taskOwnerId, taskOwnerNickname, taskLat, taskLng);
    tasks.add(task);

    this.currentTaskCount++;
  }

  /** Checks if its the end of the query */
  public void checkIfEnd() {
    if (this.currentTaskCount < 10) {
      this.endOfQuery = true;
    }
  }
}
