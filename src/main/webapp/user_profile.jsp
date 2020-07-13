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
  <%@ page import = "com.google.neighborgood.helper.RetrieveUserInfo" %>
  <%@ page import = "java.util.List" %>
  <% UserService userService = UserServiceFactory.getUserService();
  if (!userService.isUserLoggedIn()) { 
      response.sendRedirect(userService.createLoginURL("/account.jsp"));
  } else {
    List<String> userInfo = RetrieveUserInfo.getInfo(userService);
    if (userInfo == null) {
        response.sendRedirect("/account.jsp");
        return;
    } else {
        String urlToRedirectAfterUserLogsOut = "/index.jsp";
        String logoutURL = userService.createLogoutURL(urlToRedirectAfterUserLogsOut);
        String nickname = userInfo.get(0);
        String points = userInfo.get(3); %>
  <body onload="showNeedHelp()">
    <div id="nav-bar">
        <p id="return-link"><a href="index.jsp" id="backtohome">BACK TO HOME</a> |    </p>
        <i class="fa fa-cog fa-2x" id="info-setting" onclick="editInfo()"></i>
        <p id="log-out-link"><%=nickname%> |  <a href="<%=logoutURL%>">Logout</a></p>
    </div>
    <div class="empty"></div>
    <div id="header">
        <h1 id="title">My Tasks</h1>
        <p id="points">My current points: <%=points%>pts</p>
    </div>
    <div class="empty"><div/>
    <hr/>
    <div id="button-container-wrap">
        <div id="container">
            <button class="help-button" id="need-help-button" onclick="showNeedHelp()">Need help</button>
            <button class="help-button" id="offer-help-button" onclick="showOfferHelp()">Offer help</button>
            <i class="fa fa-plus-circle fa-3x" id="create-task-button" onclick="showModal()"></i>
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
                    <th>Mark as complete</th>
                    <th>Abandon</th>
                </tr>
            </thead>
            <tbody id="offer-help-body"></tbody>
        </table>
        <br/><br/>
        <table class="task-table" id="await-verif">
            <thead>
                <tr>
                    <th>Task Overview</th>
                    <th>Helper</th>
                    <th>Status</th>
                    <th>Verify</th>
                    <th>Disapprove</th>
                </tr>
            </thead>
            <tbody id="await-verif-body"></tbody>
        </table>
        <table class="task-table" id="complete-task">
            <thead>
                <tr>
                    <th>Task Overview</th>
                    <th>Status</th>
                    <th>Neighbor</th>
                    <th>Reward</th>
                </tr>
            </thead>
            <tbody id="complete-task-body"></tbody>
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
                <textarea name="task-detail-input" id="task-detail-input" required="true" placeholder="Describe your task here:"></textarea>
                <br/>
                <label for="rewarding-point-input">Rewarding Points:</label>
                <input type="number" id="rewarding-point-input" name="reward-input" min="0" max="200" value="50" required="true">
                <br/>
                <label for="category-input">Task Category:</label>
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
    <div class="modalWrapper" id="editTaskModalWrapper">
        <div class="modal" id="editTaskModal">
            <span class="close-button" id="edit-close-button" onclick="closeEditModal()">&times;</span>
            <form id="edit-task-form" action="/tasks/edit" method="POST">
                <h1>EDIT THE CURRENT TASK: </h1>
                <div>
                    <label for="edit-detail-input">Task Detail:</label>
                    <br/>
                </div>
                <textarea name="task-detail-input" id="edit-detail-input" required="true"></textarea>
                <br/>
                <label for="edit-point-input">Rewarding Points: </label>
                <input type="number" id="edit-point-input" name="reward-input" min="0" max="200" required="true">
                <input type="hidden" name="task-id" id="task-id-input">
                <br/>
                <label for="edit-category-input">Task Category:</label>
                <select name="edit-category-input" id="edit-category-input" form="edit-task-form">
                  <option value="garden">Garden</option>
                  <option value="shopping">Shopping</option>
                  <option value="pets">Pets</option>
                  <option value="misc">Misc</option>
                </select>
                <br/>
                <br/>
                <input type="submit" />
            </form>
        </div>
    </div>
    <div class="modalWrapper" id="updateInfoModalWrapper">
        <div class="modal" id="updateInfoModal">
            <span class="close-button" id="info-close-button" onclick="closeInfoModal()">&times;</span>
            <form id="update-info-form" action="/account" method="POST">
                <h1>EDIT YOUR PERSONAL INFORMATION: </h1>
                <div>
                    <label for="edit-nickname-input">New nickname:</label>
                    <br/>
                </div>
                <textarea name="nickname-input" id="edit-nickname-input" required="true"></textarea>
                <br/>
                <div>
                    <label for="edit-address-input">New address:</label>
                    <br/>
                </div>
                <textarea name="address-input" id="edit-address-input" required="true"></textarea>
                <br/>
                <div>
                    <label for="edit-phone-number-input">New phone number:</label>
                    <br/>
                </div>
                <textarea name="phone-input" id="edit-phone-number-input" required="true"></textarea>
                <br/>
                <br/>
                <input type="submit" />
            </form>
        </div>
    </div>
  </body>
  <%
    }
  }
  %>
</html>