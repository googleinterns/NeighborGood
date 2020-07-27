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

import com.google.appengine.api.datastore.Entity;

public final class User {
  private final String nickname;
  private final String address;
  private final String zipcode;
  private final String country;
  private final String phone;
  private final String email;
  private final String userId;
  private final long points;
  private final Double lat;
  private final Double lng;
  private boolean isCurrentUser;

  public User(Entity entity) {

    this.nickname = (String) entity.getProperty("nickname");
    this.address = (String) entity.getProperty("address");
    this.zipcode = (String) entity.getProperty("zipcode");
    this.country = (String) entity.getProperty("country");
    this.phone = (String) entity.getProperty("phone");
    this.email = (String) entity.getProperty("email");
    this.userId = entity.getKey().getName();
    this.points = (Long) entity.getProperty("points");
    this.lat = (Double) entity.getProperty("lat");
    this.lng = (Double) entity.getProperty("lng");
    this.isCurrentUser = false;
  }

  public void setCurrentUser() {
    this.isCurrentUser = true;
  }

  public String getUserId() {
    return this.userId;
  }

  public String getUserNickname() {
    return this.nickname;
  }

  public Double getUserLat() {
    return this.lat;
  }

  public Double getUserLng() {
    return this.lng;
  }
}
