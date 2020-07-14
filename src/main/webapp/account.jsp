<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>My Personal Info</title>
    <link rel="stylesheet" href="account_style.css">
    <script type='text/javascript' src='config.js'></script>
    <script src="user_profile_script.js"></script>
  </head>
  <%@ page import = "com.google.appengine.api.users.UserService" %>
  <%@ page import = "com.google.appengine.api.users.UserServiceFactory" %>
  <%@ page import = "com.google.neighborgood.helper.RetrieveUserInfo" %>
  <% UserService userService = UserServiceFactory.getUserService();
  if (!userService.isUserLoggedIn()) {
       response.sendRedirect(userService.createLoginURL("/account.jsp"));
  } else if (RetrieveUserInfo.getInfo(userService) == null) { %>
  <body>
    <div id="container">
        <div id="header">
            <h1 id="title">Please input your personal information</h1>
        </div>
        <div class="empty" style="clear: both"><div/>
        <hr/>
        <div id="form-container">
            <form id="new-user-info-form" action="/account" method="POST">
                <div>
                    <label for="nickname-input">Your Preferred Nickname:</label>
                    <br/>
                </div>
                <br/>
                <textarea name="nickname-input" id="nickname-input" required="true" placeholder="Input your preferred nickname here:"></textarea>
                <br/><br/>
                <div>
                    <label for="address-input">Your Address:</label>
                    <br/>
                </div>
                <br/>
                <textarea name="address-input" id="edit-address-input" required="true" placeholder="Input your address here:"></textarea>
                <p id="rest-map">Click to mark your personal address on the map!</p>
                <input id="place-input" class="controls" type="text" placeholder="Search Box">
                <div id="map"></div>
                <br/><br/>
                <div>
                    <label for="edit-zipcode-input">Your Zip Code:</label>
                    <br/>
                </div>
                <br/>
                <textarea name="zipcode-input" id="edit-zipcode-input" required="true" placeholder="Input your zipcode here:"></textarea>
                <br/><br/>
                <div>
                    <label for="edit-country-input">Your Country:</label>
                    <br/>
                </div>
                <br/>
                <textarea name="country-input" id="edit-country-input" required="true" placeholder="Input your country here:"></textarea>
                <br/><br/>
                <div>
                    <label for="phone-input">Your Phone Number:</label>
                    <br/>
                </div>
                <br/>
                <textarea name="phone-input" id="phone-input" required="true" placeholder="Input your phone number here:"></textarea>
                <br/><br/>
                <button type="submit" id="submit-button"/>GET STARTED</button>
                <br/><br/>
            </form>
        </div>
    </div>
  </body>
  <%
  } else {
      response.sendRedirect("/user_profile.jsp");
  }
  %>
</html>