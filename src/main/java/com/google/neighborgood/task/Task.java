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

package com.google.neighborgood.task;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;

public final class Task {
  private final String detail;
  private final String overview;
  private final String keyString;
  private final long creationTime;
  private final String status;
  private final long reward;
  private final String owner;
  private final String helper;
  private final String address;
  private final String zipcode;
  private final String country;
  private final String category;

  public Task(
      String keyString,
      String detail,
      String overview,
      long creationTime,
      String status,
      long reward,
      String owner,
      String helper,
      String address,
      String zipcode,
      String country,
      String category) {
    this.keyString = keyString;
    this.detail = detail;
    this.overview = overview;
    this.creationTime = creationTime;
    this.status = status;
    this.reward = reward;
    this.owner = owner;
    this.helper = helper;
    this.address = address;
    this.zipcode = zipcode;
    this.country = country;
    this.category = category;
  }

  public Task(Entity entity) {
    this.keyString = KeyFactory.keyToString(entity.getKey());
    this.detail = (String) entity.getProperty("detail");
    this.overview = (String) entity.getProperty("overview");
    this.creationTime = (long) entity.getProperty("timestamp");
    this.reward = (long) entity.getProperty("reward");
    this.status = (String) entity.getProperty("status");
    this.address = (String) entity.getProperty("Address");
    this.zipcode = (String) entity.getProperty("zipcode");
    this.country = (String) entity.getProperty("country");
    this.category = (String) entity.getProperty("category");

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String ownerId = (String) entity.getProperty("Owner");
    String helperId = (String) entity.getProperty("Helper");
    Query ownerQuery =
        new Query("UserInfo")
            .setFilter(new FilterPredicate("userId", FilterOperator.EQUAL, ownerId));
    PreparedQuery results = datastore.prepare(ownerQuery);
    Entity ownerEntity = results.asSingleEntity();
    if (ownerEntity == null) {
      System.err.println("Unable to find the owner of the task in the database");
      this.owner = ownerId;
      this.helper = helperId;
      return;
    }
    this.owner = (String) ownerEntity.getProperty("nickname");
    // If the task status is still "OPEN", the input helper should be "N/A".
    // Otherwise, we will show the nickname of the helper.
    if (!helperId.equals("N/A")) {
      Query helperQuery =
          new Query("UserInfo")
              .setFilter(new FilterPredicate("userId", FilterOperator.EQUAL, helperId));
      results = datastore.prepare(helperQuery);
      Entity helperEntity = results.asSingleEntity();
      if (helperEntity == null) {
        System.err.println("Unable to find the owner of the task in the database");
        this.helper = helperId;
        return;
      }
      this.helper = (String) helperEntity.getProperty("nickname");
    } else {
      this.helper = "N/A";
    }
  }
}
