// limitations under the License.

package com.google.neighborgood.servlets;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.PreparedQuery;
import com.google.appengine.api.datastore.Query;
import com.google.appengine.api.datastore.Query.FilterOperator;
import com.google.appengine.api.datastore.Query.FilterPredicate;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import java.io.IOException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

@WebServlet("/account")
public class UserInfoServlet extends HttpServlet {
  @Override
  public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
    UserService userService = UserServiceFactory.getUserService();
    if (!userService.isUserLoggedIn()) {
      response.sendRedirect(userService.createLoginURL("/account.jsp"));
      return;
    }

    String nickname = "";
    String address = "";
    String phone = "";
    String nicknameInput = request.getParameter("nickname-input");
    String addressInput = request.getParameter("address-input");
    String phoneInput = request.getParameter("phone-input");
    String email = userService.getCurrentUser().getEmail();
    String userId = userService.getCurrentUser().getUserId();

    if (nicknameInput != null) nickname = nicknameInput.trim();
    if (addressInput != null) address = addressInput.trim();
    if (phoneInput != null) phone = phoneInput.trim();

    if (nickname.equals("") || address.equals("") || phone.equals("")) {
      System.err.println("At least one input field is empty");
      response.sendRedirect("/400.html");
      return;
    }

    DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
    Query query =
        new Query("UserInfo")
            .setFilter(new FilterPredicate("userId", FilterOperator.EQUAL, userId));
    PreparedQuery results = datastore.prepare(query);
    Entity entity = results.asSingleEntity();
    if (entity == null) {
      entity = new Entity("UserInfo");
      entity.setProperty("nickname", nickname);
      entity.setProperty("address", address);
      entity.setProperty("phone", phone);
      entity.setProperty("email", email);
      entity.setProperty("userId", userId);
    } else {
      entity.setProperty("nickname", nickname);
      entity.setProperty("address", address);
      entity.setProperty("phone", phone);
    }
    datastore.put(entity);

    response.sendRedirect("/user_profile.jsp");
  }
}
