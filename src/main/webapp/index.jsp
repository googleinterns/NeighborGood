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
      <header>
          <nav>
              <div id="userpage-icon">
              	<i class="fas fa-user-circle fa-3x"></i>
              </div>
          </nav>
          <div id="title">
              NeighborHelp
          </div>
      </header>
      <section>
          <div id="control-bar">
              <div id="categories">
                  <div class="categories" id="category-all" onclick="filterBy('all')">ALL</div>
                  <div class="categories" id="category-garden" onclick="filterBy('garden')">GARDEN</div>
                  <div class="categories" id="category-shopping" onclick="filterBy('shopping')">SHOPPING</div>
                  <div class="categories" id="category-pets" onclick="filterBy('pets')">PETS</div>
                  <div class="categories" id="category-misc" onclick="filterBy('misc')">MISC</div>
              </div>
              <div id="add-task">
                  ADD TASK
              </div>
          </div>
          <div id="tasks-list">
              <!--Task-->
              <div class="task garden">
                  <div class="task-header">
                      <div class="username">
                          JOHN SMITH
                      </div>
                      <div class="help-button">
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
              <!--Task-->
              <div class="task shopping">
                  <div class="task-header">
                      <div class="username">
                          CARMEN ROSA
                      </div>
                      <div class="help-button">
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
              <!--Task-->
              <div class="task misc">
                  <div class="task-header">
                      <div class="username">
                          FRIENDLY NEIGHBOR
                      </div>
                      <div class="help-button">
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
              <!--Task-->
              <div class="task shopping">
                  <div class="task-header">
                      <div class="username">
                          SPONGE BOB
                      </div>
                      <div class="help-button">
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
              <!--Task-->
              <div class="task pets">
                  <div class="task-header">
                      <div class="username">
                          BOB ROGERS
                      </div>
                      <div class="help-button">
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
              <!--Task-->
              <div class="task garden">
                  <div class="task-header">
                      <div class="username">
                          GARDEN ENTHUSIAST
                      </div>
                      <div class="help-button">
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
      </section>
  </body>
</html>
