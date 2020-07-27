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
import com.google.neighborgood.task.Task;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Helper class that stores the HTML depiction of tasks in groups of 10 or less along with whether
 * or not the end of the query has been reached
 */
public class TaskGroup {
  private static HashMap<String, String>
      usersNicknames; // Stores task owner's user info to prevent querying multiple times in
  // datastore for the same user's info
  private static DatastoreService datastore;
  private final boolean userLoggedIn;
  private int currentTaskCount;
  private boolean endOfQuery;
  private ArrayList<Task> tasks;

  public TaskGroup() {
    this.usersNicknames = new HashMap<String, String>();
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
    if (this.usersNicknames.containsKey(taskOwnerId)) {
      taskOwnerNickname = this.usersNicknames.get(taskOwnerId);
    } else {
      Key taskOwnerKey = entity.getParent();
      try {
        Entity userEntity = this.datastore.get(taskOwnerKey);
        taskOwnerNickname = (String) userEntity.getProperty("nickname");
        this.usersNicknames.put(taskOwnerId, taskOwnerNickname);
      } catch (EntityNotFoundException e) {
        System.err.println(
            "Unable to find the task's owner info to retrieve the owner's nickname. Setting a default nickname.");
        taskOwnerNickname = "Neighbor";
      }
    }
    Task task = new Task(entity, taskOwnerId, taskOwnerNickname);
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
