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

package com.google.neighborgood;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;

public final class Notification {
  private final String overview;
  private final String taskId;
  private final int count;

  public Notification(String taskId, int count) {
    this.taskId = taskId;
    this.count = count;

    // Get the latest overview value
    Key taskKey = KeyFactory.stringToKey(taskId);
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();

    Entity taskEntity;
    try {
      taskEntity = datastore.get(taskKey);
    } catch (EntityNotFoundException e) {
      System.err.println("Unable to find the entity based on the input key");
      this.overview = "";
      return;
    }

    this.overview = (String) taskEntity.getProperty("overview");
  }
}
