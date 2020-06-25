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

package com.google.sps.task;

public final class Task {
    private final String detail;
    private final String keyString;
    private final long timestamp;
    private final String status;
    private final long reward;
    private final String owner;
    private final String helper;
    private final String address;

    public Task(String keyString, String detail, long timestamp, String status, long reward, String owner, String helper, String address) {
        this.keyString = keyString;
        this.detail = detail;
        this.timestamp = timestamp;
        this.status = status;
        this.reward = reward;
        this.owner = owner;
        this.helper = helper;
        this.address = address;
    }
}