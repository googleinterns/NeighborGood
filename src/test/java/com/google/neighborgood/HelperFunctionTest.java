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

import static org.hamcrest.CoreMatchers.*;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.appengine.tools.development.testing.LocalDatastoreServiceTestConfig;
import com.google.appengine.tools.development.testing.LocalServiceTestHelper;
import com.google.appengine.tools.development.testing.LocalUserServiceTestConfig;
import com.google.common.collect.ImmutableMap;
import java.io.*;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.*;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

/** Unit test on the helper functions */
@RunWith(JUnit4.class)
public final class HelperFunctionTest {
  /* Set up the test environment with Datastore and UserService and simulate the situation
   * that the user is logged in as admin, has a specific email and userID.
   */
  private LocalServiceTestHelper helper =
      new LocalServiceTestHelper(
              new LocalDatastoreServiceTestConfig(), new LocalUserServiceTestConfig())
          .setEnvIsAdmin(true)
          .setEnvIsLoggedIn(true)
          .setEnvEmail("leonardzhang@google.com")
          .setEnvAuthDomain("1234567890")
          .setEnvAttributes(
              ImmutableMap.of(
                  "com.google.appengine.api.users.UserService.user_id_key", "1234567890"));

  private UserService userService;
  private DatastoreService ds;
  private HttpServletRequest request;
  private HttpServletResponse response;
  private Entity userEntity;
  private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
  private final PrintStream originalErr = System.err;

  @Before
  public void setUp() {
    helper.setUp();
    userService = UserServiceFactory.getUserService();
    ds = DatastoreServiceFactory.getDatastoreService();
    request = mock(HttpServletRequest.class);
    response = mock(HttpServletResponse.class);
    userEntity = new Entity("UserInfo", "1234567890");
    userEntity.setProperty("nickname", "Leonard");
    userEntity.setProperty("address", "4xxx Cxxxxx Avenue");
    userEntity.setProperty("phone", "xxx2282760");
    userEntity.setProperty("email", "leonardzhang@google.com");
    userEntity.setProperty("userId", "1234567890");
    userEntity.setProperty("country", "US");
    userEntity.setProperty("zipcode", "15213");
    userEntity.setProperty("points", 0);
    ds.put(userEntity);
  }

  @Test
  public void retrieveUserInfoTest() {
    // Given the userId 1234567890, the result should be a list of correct nickname,
    // address, phone number and rewarding points
    List<String> expected = new ArrayList<>();
    expected.add("Leonard");
    expected.add("4xxx Cxxxxx Avenue");
    expected.add("xxx2282760");
    expected.add("0");

    List<String> actual = new RetrieveUserInfo().getInfoFromId("1234567890");
    assertThat(actual, is(expected));

    actual = new RetrieveUserInfo().getInfo(userService);
    assertThat(actual, is(expected));
  }

  @Test
  public void retrieveUserInfoEdgeCaseTest() {
    // Given an invalid userId 123456789, RetrieveUserInfo should return null
    List<String> actual = new RetrieveUserInfo().getInfoFromId("123456789");
    assertNull(actual);
  }

  @Test
  public void RewardingPointsTest() {
    when(request.getParameter("reward-input")).thenReturn("150");
    // Given request and inputName "reward-input", RewardingPoints.get() should return 150
    assertEquals(150, new RewardingPoints().get(request, "reward-input"));

    when(request.getParameter("reward-input")).thenReturn("50");
    // RewardingPoints.get() should now return 50
    assertEquals(50, new RewardingPoints().get(request, "reward-input"));

    when(request.getParameter("reward-input")).thenReturn("200");
    // RewardingPoints.get() should now return 200
    assertEquals(200, new RewardingPoints().get(request, "reward-input"));
  }

  @Test(expected = IllegalArgumentException.class)
  public void notNumericInputTest() {
    when(request.getParameter("reward-input")).thenReturn("abc123");
    // If the input is not in numeric format, error message will be sent and the get function
    // will throw an IllegalArgumentException
    System.setErr(new PrintStream(errContent));

    new RewardingPoints().get(request, "reward-input");
    assertEquals("Could not convert to int: abc123\n", errContent.toString());
    errContent.reset();
    System.setErr(originalErr);
  }

  @Test(expected = IllegalArgumentException.class)
  public void outOfRangeTest() {
    when(request.getParameter("reward-input")).thenReturn("201");
    // If the input is out of range, error message will be sent and the get function
    // will throw an IllegalArgumentException
    System.setErr(new PrintStream(errContent));

    new RewardingPoints().get(request, "reward-input");
    assertEquals("User input is out of range: 201\n", errContent.toString());
    errContent.reset();
    System.setErr(originalErr);

    when(request.getParameter("reward-input")).thenReturn("-1");
    // If the input is smaller than 0, response should be the same
    System.setErr(new PrintStream(errContent));

    new RewardingPoints().get(request, "reward-input");
    assertEquals("User input is out of range: -1\n", errContent.toString());
    errContent.reset();
    System.setErr(originalErr);
  }

  @Test
  public void TaskGroupClassTest() {}
}
