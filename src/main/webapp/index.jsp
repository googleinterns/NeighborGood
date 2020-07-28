<!DOCTYPE html>
<!--
 Copyright 2020 Google LLC

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

      http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
-->

<html>
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1, minimum-scale=1">
    <title>NeighborGood</title>
    <link rel="stylesheet" href="homepage_style.css">
    <script type='text/javascript' src='config.js'></script>
    <script src="homepage_script.js"></script>
    <script src="https://kit.fontawesome.com/71105f4105.js" crossorigin="anonymous"></script>
  </head>
  <%@ page import = "com.google.appengine.api.users.UserService" %>
  <%@ page import = "com.google.appengine.api.users.UserServiceFactory" %>
  <%@ page import = "com.google.neighborgood.helper.RetrieveUserInfo" %>
  <%@ page import = "java.util.List" %>
  <% UserService userService = UserServiceFactory.getUserService();
  boolean userLoggedIn = userService.isUserLoggedIn();
  String categoriesClass = userLoggedIn ? "notFullWidth" : "fullWidth";
  %>
  <body>
      <!--Site Header-->
      <header>
          <nav>
              <div id="dashboard-icon-container">
              <%
              if (userLoggedIn){
              %>
                  <a href="user_profile.jsp" class="dashboard-icon">
                      <i class="fas fa-user-circle fa-3x" title="Go to User Page"></i>
                  </a>
              <%
                if (userService.isUserAdmin()) {
              %>
                  <a href="admin_dashboard.html" class="dashboard-icon">
                      <i class="fas fa-user-cog fa-3x" title="Go to Admin Dashboard"></i>
                  </a>
              <%
                }
              }
              %>
              </div>

              <div id="UI-messages">
          	    <%
            	  if (userLoggedIn) {
                  List<String> userInfo = RetrieveUserInfo.getInfo(userService);
                  String urlToRedirectToAfterUserLogsOut = "/index.jsp";
                  String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
                  String nickname = (userInfo == null) ? userService.getCurrentUser().getEmail() : userInfo.get(0);
                %>
          	      <p class="login-messages" id="login-logout"> <%=nickname%> | <a href="<%=logoutUrl%>" id="loginLogoutMessage">Logout</a></p>
                <%
                } else {
                      String urlToRedirectToAfterUserLogsIn = "/account.jsp";
                      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
                %>
                  <p class="login-messages"><a href="<%=loginUrl%>" id="loginLogoutMessage">Login to help out a neighbor!</a></p>
                  <%
                    }
                  %>
                  <p class="login-messages"><a id="topscore-button">NeighborGood's Top Scorers</a></p>
              </div>
          </nav>
          <h1 id="title">
              NeighborGood
          </h1>
      </header>

      <!--Main Content of Site-->
      <section>

          <!--Control Bar for choosing categories and adding tasks-->
          <div id="control-bar-message-wrapper">
              <div id="control-bar">
                  <div id="categories" class="<%=categoriesClass%>">
                      <div class="categories" id="all">ALL</div>
                      <div class="categories" id="garden">GARDEN</div>
                      <div class="categories" id="shopping">SHOPPING</div>
                      <div class="categories" id="pets">PETS</div>
                      <div class="categories" id="misc">MISC</div>
                  </div>
                  <%
                  if (userLoggedIn) {
                  %>
                  <div id="add-task">
                      <i class="fas fa-plus-circle" aria-hidden="true" id="create-task-button" title="Add Task"></i>
                  </div>
                  <%
                  }
                  %>
              </div>

              <!--Results Messages-->
              <div id="loading" class="results-message">
                  <img id="loading-gif" src="images/loading.gif" alt="Loading..."/>
              </div>
              <div id="location-missing-message" class="results-message">
                  We could not retrieve your location to display your neighborhood tasks.
              </div>
              <div id="tasks-message" class="results-message">
                  These are the most recent tasks in your neighborhood:
              </div>
              <div id="no-tasks-message" class="results-message">
                  Sorry, there are currently no tasks within your neighborhood for you to help with.
              </div>
          </div>
          <!--Listed Tasks Container-->
          <div id="tasks-list"></div>
      </section>
      <!--Create Tasks Modal-->
      <div class="modalWrapper" id="createTaskModalWrapper">
        <div class="modal" id="createTaskModal">
            <span class="close-button" id="close-addtask-button">&times;</span>
            <form id="new-task-form" action="/tasks" method="POST" onsubmit="return validateTaskForm('new-task-form')">
                <h1>CREATE A NEW TASK: </h1>
                <div>
                    <label for="task-overview-input">Task Overview<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <textarea name="task-overview-input" id="task-overview-input" placeholder="Briefly describe your task here:"></textarea>
                <br/><br/>
                <div>
                    <label for="task-detail-input">Task Detail<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <textarea name="task-detail-input" id="task-detail-input" placeholder="Describe your task here:"></textarea>
                <br/><br/>
                <label for="rewarding-point-input">Rewarding Points<span class="req">*</span></label>
                <input type="number" id="rewarding-point-input" name="reward-input" min="0" max="200" value="50">
                <br/><br/>
                <label for="category-input">Task Category<span class="req">*</span></label>
                <select name="category-input" id="category-input" form="new-task-form">
                  <option value="garden">Garden</option>
                  <option value="shopping">Shopping</option>
                  <option value="pets">Pets</option>
                  <option value="misc">Misc</option>
                </select>
                <br/><br/>
                <input type="submit" id="submit-create-task"/>
            </form>
        </div>
      </div>
      <!--Top Scorers Modal-->
      <div class="modalWrapper" id="topScoresModalWrapper">
        <div class="modal" id="topScoresModal">
            <span class="close-button" id="close-topscore-button">&times;</span>
            <h1 id="topScoresTitle">Top Scorers</h1>
            <div id="topScoresTitlesWrapper">
            </div>
            <div id="topScoresTablesWrapper">
              <!--World Top Scorers-->
              <div id="world-topscores" class="topScoresDiv">
                <h2>World Wide</h2>
                <table class="topScoresTable">
                <%
                for (int rank = 1; rank <= 10; rank++) {
                  String rowId = "world" + rank;
                %>
                  <tr id="<%=rowId%>">
                    <td class="topscore-rank topscores"><%=rank%>.</td>
                    <td class="topscore-nickname topscores">-</td>
                    <td class="topscore-score topscores">-</td>
                  </tr>
                <%
                }
                %>
                </table>
              </div>
              <!--Neighborhood Top Scorers-->
              <div id="neighborhood-topscore" class="topScoresDiv">
                <h2>Neighborhood</h2>
                <table class="topScoresTable">
                <%
                for (int rank = 1; rank <= 10; rank++) {
                  String rowId = "neighborhood" + rank;
                %>
                  <tr id="<%=rowId%>">
                    <td class="topscore-rank topscores"><%=rank%>.</td>
                    <td class="topscore-nickname topscores">-</td>
                    <td class="topscore-score topscores">-</td>
                  </tr>
                <%
                }
                %>
                </table>
              </div>
            </div>
        </div>
    </div>
    <div class="modalWrapper" id="taskInfoModalWrapper">
        <div class="modal" id="taskInfoModal">
            <span class="close-button" id="task-info-close-button"">&times;</span>
            <h1>Task Detail: </h1>
            <div id="task-detail-container"></div>
        </div>
    </div>
  </body>
</html>
