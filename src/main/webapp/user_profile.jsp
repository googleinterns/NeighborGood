<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>My Account</title>
    <link rel="stylesheet" href="user_profile_style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <script src="user_profile_script.js"></script>
  </head>
  <%@ page import = "com.google.appengine.api.users.UserService" %>
  <%@ page import = "com.google.appengine.api.users.UserServiceFactory" %>
  <% UserService userService = UserServiceFactory.getUserService();
  if (userService.isUserLoggedIn()) { %>
  <body onload="showNeedHelp()">
    <div id="nav-bar">
        <p id="return-link"><a href="index.jsp">BACK TO HOME</a></p>
        <p id="log-out-link">Leonard Zhang |  <a href="logout.html">Logout</a></p>
    </div>
    <div style="clear: both"></div>
    <div id="header">
        <h1 id="title">My Tasks</h1>
        <p id="points">My current points: 347pts</p>
    </div>
    <div style="clear: both"><div/>
    <hr/>
    <div id="button-container-wrap">
        <div id="container">
            <button class="help-button" id="need-help-button" onclick="showNeedHelp()">Need help</button>
            <button class="help-button" id="offer-help-button" onclick="showOfferHelp()">Offer help</button>
            <i class="fa fa-plus-circle fa-3x" id="create" onclick="showModal()"></i>
        </div>
    </div>
    <br/>
    <div id="task-list">
        <table class="task-table" id="need-help">
            <thead>
                <tr>
                    <th>Task Overview</th>
                    <th>Helper</th>
                    <th>Status</th>
                    <th>Edit</th>
                    <th>Delete</th>
                </tr>
            </thead>
            <tbody id="need-help-body"></tbody>
        </table>
        <table class="task-table" id="offer-help">
            <thead>
                <tr>
                    <th>Task Overview</th>
                    <th>Status</th>
                    <th>Neighbor</th>
                    <th>Reward</th>
                    <th>Mark as complete</th>
                    <th>Abandon</th>
                </tr>
            </thead>
            <tbody id="offer-help-body"></tbody>
        </table>
    </div>
    <div class="modalWrapper" id="createTaskModalWrapper">
        <div class="modal" id="createTaskModal">
            <span class="close-button" id="close-button" onclick="closeModal()">&times;</span>
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
    <div class="modalWrapper" id="editTaskModalWrapper">
        <div class="modal" id="editTaskModal">
            <span class="close-button" id="edit-close-button" onclick="closeEditModal()">&times;</span>
            <form id="edit-task-form" action="/tasks/edit" method="POST">
                <h1>EDIT THE CURRENT TASK: </h1>
                <div>
                    <label for="edit-detail-input">Task Detail:</label>
                    <br/>
                </div>
                <textarea name="task-detail-input" id="edit-detail-input"></textarea>
                <br/>
                <label for="edit-point-input">Rewarding Points: </label>
                <input type="number" id="edit-point-input" name="reward-input" min="0" max="200">
                <input type="hidden" name="task-id" id="task-id-input">
                <br/>
                <br/>
                <input type="submit" />
            </form>
        </div>
    </div>
  </body>
  <%
  } else {
      response.sendRedirect(userService.createLoginURL("/"));
  }
  %>
</html>