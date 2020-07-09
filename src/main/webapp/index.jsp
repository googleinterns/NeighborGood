<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>NeighborGood</title>
    <link rel="stylesheet" href="homepage_style.css">
    <script type='text/javascript' src='config.js'></script>
    <script src="homepage_script.js"></script>
    <script src="https://kit.fontawesome.com/71105f4105.js" crossorigin="anonymous"></script> 
  </head>
  <%@ page import = "com.google.appengine.api.users.UserService" %>
  <%@ page import = "com.google.appengine.api.users.UserServiceFactory" %>
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

              <div id="login-logout">
          	      <%
            	  if (userLoggedIn) {
                      String urlToRedirectToAfterUserLogsOut = "/";
                      String logoutUrl = userService.createLogoutURL(urlToRedirectToAfterUserLogsOut);
                  %>
          	      <p class="login-messages"><%=userService.getCurrentUser().getEmail()%> | <a href="<%=logoutUrl%>">Logout</a></p>
                  <%
                  } else {
                      String urlToRedirectToAfterUserLogsIn = "/account.jsp";
                      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
                  %>
                  <p class="login-messages"><a href="<%=loginUrl%>">Login to help out a neighbor!</a></p>
                  <%
                    }
                  %>
                  <p class="login-messages"><a id="topscore-button">Check out your neighborhood topscorers!</a></p>
              </div>
          </nav>
          <h1 id="title">
              NeighborGood
          </h1>
      </header>

      <!--Main Content of Site-->
      <section>

          <!--Control Bar for choosing categories and adding tasks-->
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
                  <i class="fas fa-plus-circle" aria-hidden="true" id="addtaskbutton" title="Add Task"></i>
              </div>
              <% 
              }
              %>
          </div>
          <div id="location-missing-message" class="results-message">
              We could not retrieve your location to display your neighborhood tasks.
          </div>
          <div id="tasks-message" class="results-message">
              These are the 20 (or less) most recent tasks in your neighborhood:
          </div>
          <div id="no-tasks-message" class="results-message">
              Sorry, there are currently no tasks within your neighborhood for you to help with.
          </div>
          <!--Listed Tasks Container-->
          <div id="tasks-list"></div>
      </section>
      <div class="modalWrapper" id="createTaskModalWrapper">
        <div class="modal" id="createTaskModal">
            <span class="close-button" id="close-button">&times;</span>
            <form id="new-task-form" action="/tasks" method="POST">
                <h1>CREATE A NEW TASK: </h1>
                <div>
                    <label for="task-detail-input">Task Detail:</label>
                    <br/>
                </div>
                <textarea name="task-detail-input" id="task-detail-input" placeholder="Describe your task here:"></textarea>
                <br/>
                <label for="rewarding-point-input">Rewarding Points:</label>
                <input type="number" id="rewarding-point-input" name="reward-input" min="0" max="200" value="50">
                <br/><br/>
                <input type="submit" />
            </form>
        </div>
    </div>
    <div class="modalWrapper" id="topScoresModalWrapper">
        <div class="modal" id="topScoresModal">
            <span class="close-button" id="close-button">&times;</span>
            <h1>HIGHSCORES</h1>
            <div id="world-topscores">
              <table>
              <%
              for (int rank = 1; rank <= 10; rank++) {
                String rowId = "world" + rank;
              %> 
                <tr id="<%=rowId%>">
                  <td class="topscore-rank"><%=rank%>.</td>
                  <td class="topscore-nickname"></td>
                  <td class="topscore-score"></td>
                </tr>
              <%
              }
              %>
            </div> 
            <div id="neighborhood-topscore">
              <table>
              <%
              for (int rank = 1; rank <= 10; rank++) {
                String rowId = "neighborhood" + rank;
              %> 
                <tr id="<%=rowId%>">
                  <td class="topscore-rank"><%=rank%>.</td>
                  <td class="topscore-nickname"></td>
                  <td class="topscore-score"></td>
                </tr>
              <%
              }
              %>
            </div>
        </div>
    </div>
  </body>
</html>
