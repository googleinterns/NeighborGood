<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>My Account</title>
    <link rel="stylesheet" href="user_profile_style.css">
    <link rel="stylesheet" href="https://cdnjs.cloudflare.com/ajax/libs/font-awesome/4.7.0/css/font-awesome.min.css">
    <script type='text/javascript' src='map_styles.js'></script>
    <script type='text/javascript' src='config.js'></script>
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
  <body>
    <div id="nav-bar">
        <p id="return-link"><a href="index.jsp" id="backtohome">BACK TO HOME</a> |    </p>
        <i class="fa fa-cog fa-2x" id="info-setting" onclick="editInfo()"></i>
        <p id="log-out-link"><%=nickname%> |  <a href="<%=logoutURL%>" id="logout-href">Logout</a></p>
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
                    <th>Address</th>
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
    <div class="modalWrapper" id="editTaskModalWrapper">
        <div class="modal" id="editTaskModal">
            <span class="close-button" id="edit-close-button" onclick="closeEditModal()">&times;</span>
            <form id="edit-task-form" action="/tasks/edit" method="POST" onsubmit="return validateTaskForm('edit-task-form')">
                <h1>EDIT THE CURRENT TASK: </h1>
                <div>
                    <label for="edit-overview-input">Task Overview<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <textarea name="task-overview-input" id="edit-overview-input"></textarea>
                <br/><br/>
                <div>
                    <label for="edit-detail-input">Task Detail<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <textarea name="task-detail-input" id="edit-detail-input"></textarea>
                <br/><br/>
                <label for="edit-point-input">Rewarding Points<span class="req">*</span></label>
                <input type="number" id="edit-point-input" name="reward-input" min="0" max="200">
                <input type="hidden" name="task-id" id="task-id-input">
                <br/><br/>
                <label for="edit-category-input">Task Category<span class="req">*</span></label>
                <select name="category-input" id="edit-category-input" form="edit-task-form">
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
            <form id="update-info-form" action="/account" method="POST" onsubmit="return validateInfoForm('update-info-form')">
                <h1>EDIT YOUR PERSONAL INFORMATION: </h1>
                <div>
                    <label for="edit-nickname-input">Your nickname<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <textarea name="nickname-input" id="edit-nickname-input"></textarea>
                <br/><br/>
                <div>
                    <label for="edit-address-input">Your address<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <textarea name="address-input" id="edit-address-input"></textarea>
                <p id="rest-map">Click to mark your personal address on the map!<span class="req">*</span></p>
                <input id="place-input" class="controls" type="text" placeholder="Search Box">
                <div id="map"></div>
                <br/><br/>
                <div>
                    <label for="edit-zipcode-input">Your Zip Code<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <input type="text" name="zipcode-input" id="edit-zipcode-input">
                <br/><br/>
                <div>
                    <label for="edit-country-input">Your Country<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <input type="text" name="country-input" id="edit-country-input">
                <br/><br/>
                <div>
                    <label for="edit-phone-number-input">Your phone number<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <input type="text" name="phone-input" id="edit-phone-number-input">
                <input type="hidden" name="lat" id="lat-input">
                <input type="hidden" name="lng" id="lng-input">
                <br/>
                <br/>
                <input type="submit"/>
            </form>
        </div>
    </div>
    <div class="modalWrapper" id="taskInfoModalWrapper">
        <div class="modal" id="taskInfoModal">
            <span class="close-button" id="task-info-close-button" onclick="closeTaskInfoModal()">&times;</span>
            <h1>Task Detail: </h1>
            <div id="task-detail-container"></div>
            <hr/>
            <div>
                <h1 id="chat-title">Chat: </h1>
                <button onclick="refresh()" id="refresh-button"><i class="fa fa-refresh fa-2x"></i></button>
            </div>
            <div class="empty"></div>
            <div id="message-container"></div>
            <form id="chat-box" action="/messages" method="POST" onsubmit="return sendMessage()">
                <br/>
                <div>
                    <textarea name="msg" id="msg-input" placeholder="Type message.."></textarea>
                    <input type="hidden" name="task-id" id="chat-id-input">
                    <br/>
                    <br/>
                    <button type="submit" id="send-button"><i class="fa fa-paper-plane fa-2x"></i></button>
                </div>
            </form>
        </div>
    </div>
  </body>
  <%
    }
  }
  %>
</html>
