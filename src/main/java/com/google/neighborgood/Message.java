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

package com.google.neighborgood;

import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;

public final class Message {
  private final String message;
  private final String className;
  private final long sentTime;

  public Message(String message, String className, long sentTime) {
    this.message = message;
    this.className = className;
    this.sentTime = sentTime;
  }

  public Message(Entity entity) {
    this.message = (String) entity.getProperty("message");
    this.sentTime = (long) entity.getProperty("sentTime");

    String sender = (String) entity.getProperty("sender");
    UserService userService = UserServiceFactory.getUserService();
    if (sender.equals(userService.getCurrentUser().getUserId())) {
      this.className = "sentByMe";
    } else {
      this.className = "sentByOthers";
    }
  }
}
