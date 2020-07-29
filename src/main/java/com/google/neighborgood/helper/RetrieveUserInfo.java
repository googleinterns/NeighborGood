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
import java.util.ArrayList;
import java.util.List;

public final class RetrieveUserInfo {
  public static List<String> getInfoFromId(String userId) {
    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Key userEntityKey = KeyFactory.createKey("UserInfo", userId);
    Entity entity;
    try {
      entity = datastore.get(userEntityKey);
    } catch (EntityNotFoundException e) {
      return null;
    }

    long points = (long) entity.getProperty("points");
    List<String> result = new ArrayList<>();
    result.add((String) entity.getProperty("nickname"));
    result.add((String) entity.getProperty("address"));
    result.add(Long.toString(points));
    return result;
  }

  public static List<String> getInfo(UserService userService) {
    return getInfoFromId(userService.getCurrentUser().getUserId());
  }
}
