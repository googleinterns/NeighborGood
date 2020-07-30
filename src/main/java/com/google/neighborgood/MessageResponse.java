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

import java.util.List;

public final class MessageResponse {
  private final String cursorString;
  private final List<Message> messages;

  public MessageResponse(String cursorString, List<Message> messages) {
    this.messages = messages;
    this.cursorString = cursorString;
  }
}
