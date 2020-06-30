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
              <div id="userpage-icon">
              <%
              if (userLoggedIn && !userService.isUserAdmin()) { 
              %>
                  <a href="user_profile.html">
                      <i class="fas fa-user-circle fa-3x" title="Go to User Page"></i>
                  </a>
              <%
              }
              %>
              <%
              if (userLoggedIn && userService.isUserAdmin()) { 
              %>
                  <a href="admin_dashboard.html">
                      <i class="fas fa-user-cog fa-3x" title="Go to Admin Dashboard"></i>
                  </a>
              <%
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
                      String urlToRedirectToAfterUserLogsIn = "/";
                      String loginUrl = userService.createLoginURL(urlToRedirectToAfterUserLogsIn);
                  %>
                  <p class="login-messages"><a href="<%=loginUrl%>">Login to help out a neighbor!</a></p>
                  <%
                    }
                  %>
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
          <div id="no-tasks-message">
              Sorry, there are currently no tasks within your neighborhood for you to help with.
          </div>
          <!--Listed Tasks Container-->
          <div id="tasks-list"></div>
      </section>
      <div id="createTaskModal">
        <div id="modal">
            <span id="close-button">&times;</span>
            <form id="new-task-form" action="/tasks" method="POST">
                <h1>CREATE A NEW TASK: </h1>
                <div>
                    <label for="task-content-input">Task Detail:</label>
                    <br/>
                </div>
                <textarea name="task-content-input" id="task-content-input" placeholder="Describe your task here:"></textarea>
                <br/>
                <label for="rewarding-point-input">Rewarding Points:</label>
                <input type="number" id="rewarding-point-input" name="reward-input" min="0" max="200" value="50">
                <br/><br/>
                <input type="submit" />
            </form>
        </div>
    </div>
  </body>
</html>
