<!DOCTYPE html>
<html>
  <head>
    <meta charset="UTF-8">
    <meta name="viewport" content="width=device-width, initial-scale=1, maximum-scale=1">
    <title>NeighborHelp</title>
    <link rel="stylesheet" href="homepage_style.css">
    <script src="homepage_script.js"></script>
    <script src="https://kit.fontawesome.com/71105f4105.js" crossorigin="anonymous"></script> 
  </head>
  <body>
      <!--Site Header-->
      <header>
          <nav>
              <div id="userpage-icon">
                  <a href="user_profile.html">
                      <i class="fas fa-user-circle fa-3x" title="Go to User Page"></i>
                  </a>
              </div>
          </nav>
          <div id="title">
              NeighborHelp
          </div>
      </header>

      <!--Main Content of Site-->
      <section>

          <!--Control Bar for choosing categories and adding tasks-->
          <div id="control-bar">
              <div id="categories">
                  <div class="categories" id="category-all" onclick="filterBy('all')">ALL</div>
                  <div class="categories" id="category-garden" onclick="filterBy('garden')">GARDEN</div>
                  <div class="categories" id="category-shopping" onclick="filterBy('shopping')">SHOPPING</div>
                  <div class="categories" id="category-pets" onclick="filterBy('pets')">PETS</div>
                  <div class="categories" id="category-misc" onclick="filterBy('misc')">MISC</div>
              </div>
              <div id="add-task">
                  <i class="fas fa-plus-circle" aria-hidden="true" id="addtaskbutton" title="Add Task" onclick="showModal()"></i>
              </div>
          </div>

          <!--Hard Coded List of Tasks-->
          <div id="tasks-list">
              <!-- New Task Begins-->
              <div class="task garden">
                  <div class="confirm-overlay" id="overlay-task-1">
                      <div class="exit-confirm" onclick="cancelHelpOut(this)"><a>&times</a></div>
                      <a onclick="removeTask(this)">CONFIRM</a>
                  </div>
                  <div class="task-container">
                        <div class="task-header">
                            <div class="username">
                                JOHN SMITH
                            </div>
                            <div class="help-button" onclick="helpOut(this)">
                                HELP OUT
                            </div>
                        </div>
                        <div class="task-content">
                            I need someone to help me mow my lawn.
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
              <!-- New Task Begins-->
              <div class="task shopping">
                  <div class="confirm-overlay"  id="overlay-task-2">
                      <div class="exit-confirm" onclick="cancelHelpOut(this)"><a>&times</a></div>
                      <a onclick="removeTask(this)">CONFIRM</a>
                  </div>
                  <div class="task-container">
                        <div class="task-header">
                            <div class="username">
                                CARMEN ROSA
                            </div>
                            <div class="help-button" onclick="helpOut(this)">
                                HELP OUT
                            </div>
                        </div>
                        <div class="task-content">
                            Could someone pick up my pharmacy prescription?
                        </div>
                        <div class="task-footer">
                            <div class="task-category">
                                #shopping
                            </div>
                            <div class="task-points">
                                25 PTS
                            </div>  
                        </div>
                  </div>
              </div>
              <!-- New Task Begins-->
              <div class="task misc">
                  <div class="confirm-overlay" id="overlay-task-3">
                      <div class="exit-confirm" onclick="cancelHelpOut(this)"><a>&times</a></div>
                      <a onclick="removeTask(this)">CONFIRM</a>
                  </div>
                  <div class="task-container">
                        <div class="task-header">
                            <div class="username">
                                FRIENDLY NEIGHBOR
                            </div>
                            <div class="help-button" onclick="helpOut(this)">
                                HELP OUT
                            </div>
                        </div>
                        <div class="task-content">
                            Can someone help me unload some furniture?
                        </div>
                        <div class="task-footer">
                            <div class="task-category">
                                #misc
                            </div>
                            <div class="task-points">
                                75 PTS
                            </div>  
                        </div>
                  </div>
              </div>
              <!-- New Task Begins-->
              <div class="task shopping">
                  <div class="confirm-overlay" id="overlay-task-4">
                      <div class="exit-confirm" onclick="cancelHelpOut(this)"><a>&times</a></div>
                      <a onclick="removeTask(this)">CONFIRM</a>
                  </div>
                  <div class="task-container">
                        <div class="task-header">
                            <div class="username">
                                SPONGE BOB
                            </div>
                            <div class="help-button" onclick="helpOut(this)">
                                HELP OUT
                            </div>
                        </div>
                        <div class="task-content">
                            I need someone to go to the grocery store for me.
                        </div>
                        <div class="task-footer">
                            <div class="task-category">
                                #shopping
                            </div>
                            <div class="task-points">
                                50 PTS
                            </div>  
                        </div>
                  </div>
              </div>
              <!-- New Task Begins-->
              <div class="task pets">
                  <div class="confirm-overlay" id="overlay-task-5">
                      <div class="exit-confirm" onclick="cancelHelpOut(this)"><a>&times</a></div>
                      <a onclick="removeTask(this)">CONFIRM</a>
                  </div>
                  <div class="task-container">
                        <div class="task-header">
                            <div class="username">
                                BOB ROGERS
                            </div>
                            <div class="help-button" onclick="helpOut(this)">
                                HELP OUT
                            </div>
                        </div>
                        <div class="task-content">
                            Could someone walk Fluffy for me? I am in bedrest for a week.
                        </div>
                        <div class="task-footer">
                            <div class="task-category">
                                #pets
                            </div>
                            <div class="task-points">
                                75 PTS
                            </div>  
                        </div>
                  </div>
              </div>
              <!-- New Task Begins-->
              <div class="task garden">
                  <div class="confirm-overlay" id="overlay-task-6">
                      <div class="exit-confirm" onclick="cancelHelpOut(this)"><a>&times</a></div>
                      <a onclick="removeTask(this)">CONFIRM</a>
                  </div>
                  <div class="task-container">
                        <div class="task-header">
                            <div class="username">
                                GARDEN ENTHUSIAST
                            </div>
                            <div class="help-button" onclick="helpOut(this)">
                                HELP OUT
                            </div>
                        </div>
                        <div class="task-content">
                            Can someone water my garden next week? I'll be out of town.
                        </div>
                        <div class="task-footer">
                            <div class="task-category">
                                #garden
                            </div>
                            <div class="task-points">
                                150 PTS
                            </div>  
                        </div>
                  </div>
              </div>
          </div>
      </section>
      <!-- Add Task Modal Section Implemented by Leonard -->
      <div id="createTaskModal">
        <div id="modal">
            <span id="close-button" onclick="closeModal()">&times;</span>
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
