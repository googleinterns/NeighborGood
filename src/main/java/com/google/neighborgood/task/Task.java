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

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.KeyFactory;

public final class Task {
  private final String detail;
  private final String keyString;
  private final long creationTime;
  private final String status;
  private final long reward;
  private final String owner;
  private final String helper;
  private final String address;
  private final String userId;
  private final String zipcode;
  private final String country;
  private final String category;

  public Task(
      String keyString,
      String detail,
      long creationTime,
      String status,
      long reward,
      String owner,
      String helper,
      String address,
      String userId,
      String zipcode,
      String country,
      String category) {
    this.keyString = keyString;
    this.detail = detail;
    this.creationTime = creationTime;
    this.status = status;
    this.reward = reward;
    this.owner = owner;
    this.helper = helper;
    this.address = address;
    this.userId = userId;
    this.zipcode = zipcode;
    this.country = country;
    this.category = category;
  }

  public Task(Entity entity) {
    this.keyString = KeyFactory.keyToString(entity.getKey());
    this.detail = (String) entity.getProperty("detail");
    this.creationTime = (long) entity.getProperty("timestamp");
    this.reward = (long) entity.getProperty("reward");
    this.status = (String) entity.getProperty("status");
    this.owner = (String) entity.getProperty("Owner");
    this.helper = (String) entity.getProperty("Helper");
    this.address = (String) entity.getProperty("Address");
    this.userId = (String) entity.getProperty("userId");
    this.zipcode = (String) entity.getProperty("zipcode");
    this.country = (String) entity.getProperty("country");
    this.category = (String) entity.getProperty("category");
  }
}
