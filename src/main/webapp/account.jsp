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
            <form id="new-user-info-form" action="/account" method="POST" onsubmit="return validateInfoForm('new-user-info-form')">
                <div>
                    <label for="nickname-input">Your Preferred Nickname<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <textarea name="nickname-input" id="nickname-input" placeholder="Input your preferred nickname here:"></textarea>
                <br/><br/>
                <div>
                    <label for="address-input">Your Address<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <textarea name="address-input" id="edit-address-input" placeholder="Input your address here:"></textarea>
                <p id="rest-map">Click to mark your personal address on the map!<span class="req">*</span></p>
                <input id="place-input" class="controls" type="text" placeholder="Search Box">
                <div id="map"></div>
                <br/><br/>
                <div>
                    <label for="edit-zipcode-input">Your Zip Code<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <input type="text" name="zipcode-input" id="edit-zipcode-input" placeholder="Input your zipcode here:">
                <br/><br/>
                <div>
                    <label for="edit-country-input">Your Country<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <input type="text" name="country-input" id="edit-country-input" placeholder="Input your country here:">
                <br/><br/>
                <div>
                    <label for="phone-input">Your Phone Number<span class="req">*</span></label>
                    <br/>
                </div>
                <br/>
                <input type="text" name="phone-input" id="phone-input" placeholder="Input your phone number here:">
                <input type="hidden" name="lat" id="lat-input">
                <input type="hidden" name="lng" id="lng-input">
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