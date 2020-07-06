<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <title>My Personal Info</title>
    <link rel="stylesheet" href="account_style.css">
  </head>
  <%@ page import = "com.google.appengine.api.users.UserService" %>
  <%@ page import = "com.google.appengine.api.users.UserServiceFactory" %>
  <%@ page import = "com.google.sps.helper.RetrieveUserInfo" %>
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
                <textarea name="address-input" id="address-input" required="true" placeholder="Input your address here:"></textarea>
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