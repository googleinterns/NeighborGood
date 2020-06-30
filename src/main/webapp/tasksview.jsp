
<%@ page import = "com.google.appengine.api.users.UserService" %>
<%@ page import = "javax.servlet.http.HttpSession" %>
<%@ page import = "javax.servlet.http.HttpServletRequest" %>
  <%@ page import = "com.google.appengine.api.users.UserServiceFactory" %>
  <%@ page import = "java.util.List" %>
  <%@ page import = "java.util.ArrayList" %>
  <%@ page import = "com.google.sps.task.Task" %>
  <% UserService userService = UserServiceFactory.getUserService(); 
  boolean userLoggedIn = userService.isUserLoggedIn();
  %>
  <% 
  ArrayList<Task> test = (ArrayList<Task>) session.getAttribute("testmafe2");
      System.out.println("inside jsp: " + test);
      for (Task task : test) {
          %>${task.getDetail()}<%}%>
<!--
<% 
List<Task> tasks = (ArrayList<Task>) session.getAttribute("tasks");
    System.out.println("inside jsp: " + tasks);
    for (Task task : tasks){
    %>
<div class="task">
                  <%
                  if (userLoggedIn) {
                  %>
                  <div class="confirm-overlay">
                      <div class="exit-confirm"><a>&times</a></div>
                      <a class="removetask">CONFIRM</a>
                  </div>
                  <%
                  }
                  %>
                  <div class="task-container">
                        <div class="task-header">
                            <div class="username">
                                <%=task.getOwner()%>
                            </div>
                            <%
                            if (userLoggedIn) {
                            %>
                            <div class="help-button">
                                HELP OUT
                            </div>
                            <%
                            }
                            %>
                        </div>
                        <div class="task-content">
                            <%=task.getDetail()%>
                        </div>
                        <div class="task-footer">
                            <div class="task-category">
                                #garden
                            </div>
                            <div class="task-points">
                                50 PTS
                            </div>  
                        </div>
                  </div>
              </div>
<%
    }
    %>
-->