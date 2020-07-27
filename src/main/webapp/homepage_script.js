// Copyright 2020 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

const MAPSKEY = config.MAPS_KEY
let neighborhood = [null , null];
let userLocation = null;
let userActualLocation = null;
let currentCategory = "all";
let taskGroup = null;

/* Changes navbar background upon resize */
window.addEventListener("resize", function() {
    let navbar = document.getElementsByTagName("nav")[0];
    if (window.innerWidth < 1204) navbar.style.backgroundColor = "white";
    else navbar.style.backgroundColor = "transparent";
});

/* Changes navbar background upon scroll */
window.onscroll = function() {
    if (window.innerWidth >= 1204) {
        let navbar = document.getElementsByTagName("nav")[0];
        OFFSET = 180; // approx distance from top of page to top of control (categories) bar
        if (window.pageYOffset >= OFFSET || document.body.scrollTop >= OFFSET || document.documentElement.scrollTop >= OFFSET) {
            navbar.style.backgroundColor = "white";
        } else navbar.style.backgroundColor = "transparent";
    }
}

/* Adds scroll event listener to load more tasks if the user has reached the bottom of the page */
document.addEventListener("scroll", function() {
    if (getDocumentHeight() == getVerticalScroll() + window.innerHeight) {
        loadMoreTasks();
    }
})

/* Returns document height using different methods of doing so that differ by browser */
function getDocumentHeight() {
    return Math.max(document.body.scrollHeight, document.documentElement.scrollHeight,
        document.body.offsetHeight, document.documentElement.offsetHeight,
        document.body.clientHeight, document.documentElement.clientHeight);
}

/* Returns the vertical scroll the user has gone using different methods of doing so that differ by browser */
function getVerticalScroll() {
    let verticalScroll = 0;
    if( typeof( window.pageYOffset ) == 'number' ) {
        verticalScroll = window.pageYOffset;
    } else if( document.body && document.body.scrollTop) {
        verticalScroll = document.body.scrollTop;
    } else if( document.documentElement && document.documentElement.scrollTop) {
        verticalScroll = document.documentElement.scrollTop;
    }
    return verticalScroll;
}

/* Calls addUIClickHandlers and getTasksForUserLocation once page has loaded */
if (document.readyState === 'loading') {
    // adds on load event listeners if document hasn't yet loaded
    document.addEventListener('DOMContentLoaded', addUIClickHandlers);
    document.addEventListener('DOMContentLoaded', getTasksForUserLocation);
} else {
    // if DOMContentLoaded has already fired, it simply calls the functions
    addUIClickHandlers();
    getTasksForUserLocation();
}

/* Function adds all the necessary UI 'click' event listeners*/
function addUIClickHandlers() {
    // adds showCreateTaskModal and closeCreateTaskModal click events for the add task button
    if (document.body.contains(document.getElementById("addtaskbutton"))) {
        document.getElementById("addtaskbutton").addEventListener("click", showCreateTaskModal);
    	document.getElementById("close-addtask-button").addEventListener("click", closeCreateTaskModal);
    }

    // adds filterTasksBy click event listener to category buttons
    const categoryButtons = document.getElementsByClassName("categories");
    for (let i = 0; i < categoryButtons.length; i++) {
        categoryButtons[i].addEventListener("click", function(e) {
            filterTasksBy(e.target.id);
        });
    }
    // adds showTopScoresModal click event 
    document.getElementById("topscore-button").addEventListener("click", showTopScoresModal);
    document.getElementById("close-topscore-button").addEventListener("click", closeTopScoresModal);

    // adds closeTaskInfoModal click event
    document.getElementById("task-info-close-button").addEventListener("click", closeTaskInfoModal);
}

/* Function loads ten more tasks if there are any more, otherwise it displays a message saying there are no more tasks */
function loadMoreTasks() {
    if (userNeighborhoodIsKnown()) {
        if (!taskGroup.endOfQuery) {
            fetchTasks(currentCategory, "end")
                .then(response => {
                        taskGroup = response;
                        displayTasks(true);
                    });
        } else if (!document.getElementById("no-more-tasks")) {
            let noMoreTasksDiv = document.createElement("div");
            noMoreTasksDiv.setAttribute("id", "no-more-tasks");
            noMoreTasksDiv.classList.add("results-message");
            noMoreTasksDiv.innerText = "There are no more tasks in your neighborhood";
            noMoreTasksDiv.style.display = "block";
            document.getElementById("tasks-list").appendChild(noMoreTasksDiv);
        }                
    } 
}

/* Function filters tasks by categories and styles selected categories */
function filterTasksBy(category) {
    currentCategory = category;

    // only fetches tasks if user's location has been retrieved
    if (userNeighborhoodIsKnown()) {
        fetchTasks(currentCategory, "clear")
            .then(response => {
                    taskGroup = response;
                    displayTasks();
                });
    }
	// Unhighlights and resets styling for all category buttons
    const categoryButtons = document.getElementsByClassName("categories");
    for (let i = 0; i < categoryButtons.length; i++){
        let button = categoryButtons[i];
        if (document.getElementById(category) != button) {
            button.style.backgroundColor = "rgb(76, 175, 80)";
        	button.addEventListener("mouseover", function() {
                button.style.backgroundColor = "rgb(62, 142, 65)";
            });
            button.addEventListener("mouseout", function() {
                button.style.backgroundColor = "rgb(76, 175, 80)"
            });
        } else {
            button.style.backgroundColor = "rgb(62, 142, 65)";
            button.addEventListener("mouseover", function() {
                button.style.backgroundColor = "rgb(62, 142, 65)";
            });
            button.addEventListener("mouseout", function() {
                button.style.backgroundColor = "rgb(62, 142, 65)"
            });
        }
    }
}

/* Function that display the help out overlay */
function helpOut(element) {
    const task = element.closest(".task");
    const overlay = task.getElementsByClassName("help-overlay");
    overlay[0].style.display = "block";
}

/* Function sends a fetch request to the edit task servlet when the user
offers to help out, edits the task's status and helper properties, and
then reloads the task list */
function confirmHelp(element) {
    const task = element.closest(".task");
    const url = "/tasks/edit?task-id=" + task.dataset.key + "&action=helpout";
    const request = new Request(url, {method: "POST"});
    fetch(request).then((response) => {
        // checks if another user has already claimed the task
        if (response.status == 409) {
            window.alert
                ("We're sorry, but the task you're trying to help with has already been claimed by another user.");
            window.location.href = '/';
        }
        // hides task from list if it was succesfully claimed
        else {
            element.closest(".task").style.display = "none";
        }
    });
}

/* Function that hides the help out overlay */
function exitHelp(element) {
	element.closest(".help-overlay").style.display = "none";
}

/* Leonard's implementation of the Add Task modal */
function showCreateTaskModal() {
    var modal = document.getElementById("createTaskModalWrapper");
    modal.style.display = "block";
}

function closeCreateTaskModal() {
    var modal = document.getElementById("createTaskModalWrapper");
    modal.style.display = "none";
}

function validateTaskForm(id) {
    var result = true;
    var form = document.getElementById(id);
    var inputName = ["task-overview", "task-detail", "reward", "category"];
    for (var i = 0; i < inputName.length; i++) {
        var name = inputName[i];
        var inputField = form[name.concat("-input")].value.trim();
        if (inputField === "") {
            result = false;
            form[name.concat("-input")].classList.add("highlight");
        } else {
            form[name.concat("-input")].classList.remove("highlight");
        }
    }
    if (!result) {
        alert("All fields are required. Please fill out all fields with non-empty input.");
        return false;
    }
    return true;
}

/* Function that calls the loadTopScorers functions
   and then shows the top scores modal */
function showTopScoresModal() {
    loadTopScorers("world");
    if (userNeighborhoodIsKnown()){
      loadTopScorers("neighborhood");
    }
    document.getElementById("topScoresModalWrapper").style.display = "block";
}

/* Function closes the top scores modal */
function closeTopScoresModal() {
    document.getElementById("topScoresModalWrapper").style.display = "none";
}

/* Function loads the data for the top scorers table */
function loadTopScorers(location) {
    let url = "/account?action=topscorers";
    if (location === "neighborhood") {
      url += "&zipcode=" + neighborhood[0] + "&country=" + neighborhood[1];
    }
    fetch(url)
      .then(response => response.json())
      .then(users => {
        // Inserts Nickname and Points for every top scorer
        for (let i = 0; i < users.length; i++) {
          let points = users[i].points;
          let nickname = users[i].nickname;
          let rowId = location + (i + 1);
          let row = document.getElementById(rowId);
          let rowNickname = row.getElementsByClassName("topscore-nickname")[0];
          let rowScore = row.getElementsByClassName("topscore-score")[0];
          rowNickname.innerText = nickname;
          rowScore.innerText = points;
          // Adds different styling if row includes current user
          if (users[i].isCurrentUser) {
            row.style.fontWeight = "bold";
            row.setAttribute("title", "Congratulations, you made it to the Top Scorers Board!");
          }
        }
    });
}

// If the user clicks outside of the modals, closes the modals directly
window.onclick = function(event) {
    var createTaskModal = document.getElementById("createTaskModalWrapper");
    if (event.target == createTaskModal) {
        createTaskModal.style.display = "none";
    }
    var topScoresModal = document.getElementById("topScoresModalWrapper");
    if (event.target == topScoresModal) {
        topScoresModal.style.display = "none";
    }

    var infoModal = document.getElementById("taskInfoModalWrapper");
    if (event.target == infoModal) {
        infoModal.style.display = "none";
    }
}

/* Leonard's implementation of showing task details in a pop up window */
async function getTaskInfo(keyString) {
    const queryURL = "/tasks/info?key=" + keyString;
    const request = new Request(queryURL, {method: "GET"});
    const response = await fetch(request);
    const info = await response.json();
    return info;
}

async function showTaskInfo(element) {
    const task = element.closest(".task");
    const info = await getTaskInfo(task.dataset.key);
    var detailContainer = document.getElementById("task-detail-container");
    detailContainer.innerHTML = "";
    detailContainer.appendChild(document.createTextNode(info.detail));
    var modal = document.getElementById("taskInfoModalWrapper");
    modal.style.display = "block";
}

function closeTaskInfoModal() {
    var modal = document.getElementById("taskInfoModalWrapper");
    modal.style.display = "none";
}

/* Function dynamically adds Maps API and
begins the processes of retrieving the user's location*/
function getTasksForUserLocation() {
    const script = document.createElement("script");
    script.type = "text/javascript";
    script.src =  "https://maps.googleapis.com/maps/api/js?key=" + MAPSKEY + "&callback=initialize&libraries=places&language=en";
    script.defer = true;
    script.async = true;
    document.head.appendChild(script);

	window.initialize = function () {
        // Once the Maps API script has dynamically loaded it initializes the place autocomplete searchbox
        let placeAutocomplete = new google.maps.places.Autocomplete(document.getElementById("place-input"));
        // 'geometry' field specifies that returned data will include the place's viewport and lat/lng
        placeAutocomplete.setFields(['geometry']);

        // listener will use the inputted place to retrieve and display tasks
        google.maps.event.addListener(placeAutocomplete, 'place_changed', function() {
                let place = placeAutocomplete.getPlace();
                if (place.geometry != undefined) {
                    userLocation = place.geometry.location.toJSON();
                } else {
                    userLocation = userActualLocation;
                }
                toNeighborhood(userLocation)
                        .then(() => fetchTasks(currentCategory, "clear"))
                        .then(response => {
                                taskGroup = response;
                                displayTasks(response);
                            })
                        .catch(() => {
                            document.getElementById("loading").style.display = "none";
                            document.getElementById("location-missing-message").style.display = "block";
                        });
              });
        // Once the Maps API script has dynamically loaded it initializes it gets the user location,
        // retrieves the neighborhood from the location coordinates, fetches the tasks, and displays them
         getUserLocation().then(location => toNeighborhood(location))
        	.then(() => fetchTasks(currentCategory, "clear"))
            .then(response => {
                    taskGroup = response;
                    displayTasks();
                })
            .catch(() => {
                document.getElementById("loading").style.display = "none";
                document.getElementById("location-missing-message").style.display = "block";
            });
	}
}

/* Function that returns a promise to get and return the user's location */
function getUserLocation() {
    let url = "https://www.googleapis.com/geolocation/v1/geolocate?key=" + MAPSKEY;
    const request = new Request(url, {method: "POST"});
    return new Promise((resolve, reject) => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function(position) {
                userLocation = {lat: position.coords.latitude, lng: position.coords.longitude};
                userActualLocation = userLocation;
                resolve(userLocation);
            }, function() {
                fetch(request).then(response => {
                    if (response.status == 400 || response.status == 403 || response.status == 404) {
                        reject("User location failed");
                    } else {
                        response.json().then(jsonresponse => {
                            userLocation = jsonresponse["location"];
                            userActualLocation = userLocation;
                            resolve(userLocation);
                        });
                    }
                });
            });
        } else {
            fetch(request).then(response => {
                    if (response.status == 400 || response.status == 403 || response.status == 404) {
                        reject("User location failed");
                    } else {
                        response.json().then(jsonresponse => {
                            userLocation = jsonresponse["location"];
                            userActualLocation = userLocation;
                            resolve(userLocation);
                        });
                    }
                });
        }
    });
}

/* Function that returns a promise to return a neighborhood
array that includes the postal code and country */
function toNeighborhood(latlng) {
	return new Promise((resolve, reject) => {
        const geocoder = new google.maps.Geocoder;
        geocoder.geocode({"location": latlng}, function(results, status) {
            if (status == "OK") {
                if (results[0]) {
                    const result = results[0]
                    let zipCode = "";
                    let country ="";
                    for (let i = result.address_components.length - 1; i >= 0; i--) {
                        let component = result.address_components[i];
                        if ((zipCode == "") && (component.types.indexOf("postal_code") >= 0 )) {
                            zipCode = component.long_name;
                        }
                        if ((country == "") && (component.types.indexOf("country") >= 0 )) {
                            country = component.long_name;
                        }
                        if (zipCode != "" && country != "") break;
                    }
                    neighborhood = [zipCode, country];
                    resolve([zipCode, country]);
                } else reject("Couldn't get neighborhood");
            } else reject("Couldn't get neighborhood");
        });
    });
}

/* Fetches tasks from servlet by category and cursor action.
   Cursor can pick up from the last start, the endpoint, or clear
   the cursor and start from beginning of query */
function fetchTasks(category, cursorAction) {
    let url = "/tasks?zipcode=" + neighborhood[0]+ "&country=" + neighborhood[1] +"&cursor=" + cursorAction;
    if (category !== undefined && category != "all") {
        url += "&category=" + category;
    }
    return fetch(url).then(response => response.json());
}

/* Displays the tasks received from the server response */
function displayTasks(append) {
    if (taskGroup !== null && taskGroup.currentTaskCount > 0) {
        document.getElementById("no-tasks-message").style.display = "none";
        document.getElementById("tasks-message").style.display = "block";
        if (append) document.getElementById("tasks-list").innerHTML += taskGroup.tasks;
        else document.getElementById("tasks-list").innerHTML = taskGroup.tasks;
        document.getElementById("tasks-list").style.display = "block";
        addTasksClickHandlers();
    } else {
        document.getElementById("no-tasks-message").style.display = "block";
        document.getElementById("tasks-message").style.display = "none";
        document.getElementById("tasks-list").style.display = "none";
    }
    document.getElementById("loading").style.display = "none";
    document.getElementById("search-box").style.visibility = "visible";
}

/* Function adds all the necessary tasks 'click' event listeners*/
function addTasksClickHandlers() {

    // adds confirmHelp click event listener to confirm help buttons
    const confirmHelpButtons = document.getElementsByClassName("confirm-help");
    for (let i = 0; i < confirmHelpButtons.length; i++){
        confirmHelpButtons[i].addEventListener("click", function(e) {
            confirmHelp(e.target);
            e.stopPropagation();
        });
        
    }
    // adds exitHelp click event listener to exit help buttons
    const exitHelpButtons = document.getElementsByClassName("exit-help");
    for (let i = 0; i < exitHelpButtons.length; i++) {
        exitHelpButtons[i].addEventListener("click", function(e) {
            exitHelp(e.target);
            e.stopPropagation();
        });
    }

    // adds helpOut click event listener to help out buttons
    const helpOutButtons = document.getElementsByClassName("help-out");
    for (let i = 0; i < helpOutButtons.length; i++) {
        if (!helpOutButtons[i].classList.contains("disable-help")) {
            helpOutButtons[i].addEventListener("click", function(e) {
                helpOut(e.target);
                e.stopPropagation();
            });
        }
    }

    // adds stopPropagation on help overlay to prevent opening task details when clicking on it
    const helpOverlays = document.getElementsByClassName("help-overlay");
    for (let i = 0; i < helpOverlays.length; i++) {
        helpOverlays[i].addEventListener("click", function(e) {
            e.stopPropagation();
        });
    }
    
    // adds task click event listener to open up task details
    const tasks = document.getElementsByClassName("task");
    for (let i = 0; i < tasks.length; i++) {
        tasks[i].addEventListener("click", function(e) {
            showTaskInfo(e.target);
        });
    }
}

/* Helper function that determines if the current user's neighborhood is known */
function userNeighborhoodIsKnown() {
  return (neighborhood[0] !== null && neighborhood[1] !== null);
}
