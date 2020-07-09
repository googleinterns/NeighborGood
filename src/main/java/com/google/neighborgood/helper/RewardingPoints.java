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

import javax.servlet.http.HttpServletRequest;

public final class RewardingPoints {

  /** Return the input rewarding points by the user, or -1 if the input was invalid */
  public static int get(HttpServletRequest request, String inputName) {
    // Get the input from the form.
    String rewardPtsString = request.getParameter(inputName);

    // Convert the input to an int.
    int rewardPts;
    try {
      rewardPts = Integer.parseInt(rewardPtsString);
    } catch (NumberFormatException e) {
      System.err.println("Could not convert to int: " + rewardPtsString);
      throw new IllegalArgumentException("Could not convert to int: " + rewardPtsString);
    }

    // Check that the input is within the requested range.
    if (rewardPts < 0 || rewardPts > 200) {
      System.err.println("User input is out of range: " + rewardPtsString);
      throw new IllegalArgumentException("User input is out of range: " + rewardPtsString);
    }

    return rewardPts;
  }
}
