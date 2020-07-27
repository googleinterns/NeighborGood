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
import com.google.appengine.api.datastore.EntityNotFoundException;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

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
  private boolean isOwnerCurrentUser;
  private String dateTime;
  private Double lat;
  private Double lng;

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
    Entity ownerEntity = null;
    try {
      ownerEntity = datastore.get(KeyFactory.createKey("UserInfo", ownerId));
    } catch (EntityNotFoundException e) {
      System.err.println("Unable to find the owner of the task in the database");
      this.owner = ownerId;
      this.helper = helperId;
      return;
    }
    this.owner = (String) ownerEntity.getProperty("nickname");
    this.lat = (Double) ownerEntity.getProperty("lat");
    this.lng = (Double) ownerEntity.getProperty("lng");

    // If the task status is still "OPEN", the input helper should be "N/A".
    // Otherwise, we will show the nickname of the helper.
    if (!helperId.equals("N/A")) {
      Entity helperEntity = null;
      try {
        helperEntity = datastore.get(KeyFactory.createKey("UserInfo", helperId));
      } catch (EntityNotFoundException e) {
        System.err.println("Unable to find the helper of the task in the database");
        this.helper = helperId;
        return;
      }
      this.helper = (String) helperEntity.getProperty("nickname");
    } else {
      this.helper = "N/A";
    }

    setIsOwnerCurrentUser(ownerId);
    setDateTime();
  }

  public Task(
      Entity entity, String ownerId, String ownerNickname, Double ownerLat, Double ownerLng) {
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
    this.owner = ownerNickname;
    this.lat = ownerLat;
    this.lng = ownerLng;

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    String helperId = (String) entity.getProperty("Helper");

    // If the task status is still "OPEN", the input helper should be "N/A".
    // Otherwise, we will show the nickname of the helper.
    if (!helperId.equals("N/A")) {
      Entity helperEntity = null;
      try {
        helperEntity = datastore.get(KeyFactory.createKey("UserInfo", helperId));
      } catch (EntityNotFoundException e) {
        System.err.println("Unable to find the helper of the task in the database");
        this.helper = helperId;
        return;
      }
      this.helper = (String) helperEntity.getProperty("nickname");
    } else {
      this.helper = "N/A";
    }

    setIsOwnerCurrentUser(ownerId);
    setDateTime();
  }

  private void setDateTime() {
    Timestamp timestamp = new Timestamp(this.creationTime);
    SimpleDateFormat timestampFormat = new SimpleDateFormat("HH:mm MM-dd-yyyy");
    this.dateTime = timestampFormat.format(timestamp);
  }

  private void setIsOwnerCurrentUser(String ownerId) {
    UserService userService = UserServiceFactory.getUserService();
    boolean userLoggedIn = userService.isUserLoggedIn();
    String userId = userLoggedIn ? userService.getCurrentUser().getUserId() : "null";
    if (userId.equals(ownerId)) this.isOwnerCurrentUser = true;
    else this.isOwnerCurrentUser = false;
  }
}
