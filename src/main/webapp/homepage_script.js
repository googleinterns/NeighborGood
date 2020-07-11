// Copyright 2019 Google LLC
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
let currentCategory = "all";

window.onscroll = stickyControlBar;

/* Scroll function so that the control bar sticks to the top of the page */
function stickyControlBar() {
    let controlBarWrapper = document.getElementById("control-bar-message-wrapper");
    let taskListDiv = document.getElementById("tasks-list");
    const OFFSET = 165;
    if (window.pageYOffset >= OFFSET || document.body.scrollTop >= OFFSET || document.documentElement.scrollTop >= OFFSET) {
        controlBarWrapper.style.position = "fixed";
        taskListDiv.style.marginTop = "165px";
    } else {
        controlBarWrapper.style.position = "static";
        taskListDiv.style.marginTop = "auto";
    }
}

/* Calls addUIClickHandlers and getUserNeighborhood once page has loaded */
if (document.readyState === 'loading') {
    // adds on load event listeners if document hasn't yet loaded
    document.addEventListener('DOMContentLoaded', addUIClickHandlers)
    document.addEventListener('DOMContentLoaded', getUserNeighborhood);
} else {
    // if DOMContentLoaded has already fired, it simply calls the functions
    addUIClickHandlers();
    getUserNeighborhood();
}

/* Function adds all the necessary UI 'click' event listeners*/
function addUIClickHandlers() {
    // adds showModal and closeModal click events for the add task button
    if ((document.getElementById("addtaskbutton") !== null)) {
        document.getElementById("addtaskbutton").addEventListener("click", showModal);
    	document.getElementById("close-button").addEventListener("click", closeModal);
    }

	// adds filterTasksBy click event listener to category buttons
	const categoryButtons = document.getElementsByClassName("categories");
    for (let i = 0; i < categoryButtons.length; i++) {
        categoryButtons[i].addEventListener("click", function(e) {
            filterTasksBy(e.target.id);
        });
    }
}

/* Function filters tasks by categories and styles selected categories */
function filterTasksBy(category) {
    currentCategory = category;

    // only fetches tasks if user's neighborhood has been retrieved
    if (userNeighborhoodIsKnown()) {
        fetchTasks(category)
            .then(response => displayTasks(response));
    }
	// Unhighlights and resets styling for all category buttons
    const categoryButtons = document.getElementsByClassName("categories");
    for (let i = 0; i < categoryButtons.length; i++){
        let button = categoryButtons[i];
        if (document.getElementById(category) != button) {
            button.style.backgroundColor = "transparent";
    		button.style.color = "#222";
        	button.style.fontWeight = "normal";
        	button.addEventListener("mouseover", function() {
                button.style.backgroundColor = "lightgray";
            });
            button.addEventListener("mouseout", function() {
                button.style.backgroundColor = "transparent"
            });
        } else {
            button.style.backgroundColor = "#222";
        	button.style.color = "white";
        	button.style.fontWeight = "bold";
            button.addEventListener("mouseover", function() {
                button.style.backgroundColor = "#222";
            });
            button.addEventListener("mouseout", function() {
                button.style.backgroundColor = "#222"
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
        // fetches tasks again if user's current neighborhood was successfully retrieved and stored
        else if (userNeighborhoodIsKnown()) {
            fetchTasks(currentCategory).then(response => displayTasks(response));
        }
    });
}

/* Function that hides the help out overlay */
function exitHelp(element) {
	element.closest(".help-overlay").style.display = "none";
}

/* Leonard's implementation of the Add Task modal */
function showModal() {
    var modal = document.getElementById("createTaskModalWrapper");
    modal.style.display = "block";
}
function closeModal() {
    var modal = document.getElementById("createTaskModalWrapper");
    modal.style.display = "none";
}
// If the user clicks outside of the modal, closes the modal directly
window.onclick = function(event) {
    var modal = document.getElementById("createTaskModalWrapper");
    if (event.target == modal) {
        modal.style.display = "none";
    }
}

/* Function dynamically adds Maps API and
begins the processes of retrieving the user's neighborhood*/
function getUserNeighborhood() {
    const script = document.createElement("script");
    script.type = "text/javascript";
    script.src =  "https://maps.googleapis.com/maps/api/js?key=" + MAPSKEY + "&callback=initialize";
    script.defer = true;
    script.async = true;
    document.head.appendChild(script);
	
    // Once the Maps API script has dynamically loaded it gets the user location,
    // waits until it gets an answer and then calls toNeighborhood passing the location
    // as an argument, updates the global neighborhood variable and then calls
    // fetchTasks and displayTasks
	window.initialize = function () {
        getUserLocation().then(location => toNeighborhood(location))
        	.then(() => fetchTasks())
            .then((response) => displayTasks(response))
            .catch(() => {
                console.error("User location and/or neighborhood could not be retrieved");
                document.getElementById("location-missing-message").style.display = "block";
            });
	}
}

/* Function that returns a promise to get and return the user's location */
function getUserLocation() {
    return new Promise((resolve, reject) => {
        if (navigator.geolocation) {
            navigator.geolocation.getCurrentPosition(function(position) {
                var location = {lat: position.coords.latitude, lng: position.coords.longitude};
                resolve(location);
            }, function(err) {
                // Check to see if this failed because we're in an insecure
                // context, such as a local dev environment that isn't
                // http://localhost (https://developer.mozilla.org/en-US/docs/Web/Security/Secure_Contexts).
                if (err.code === 1 && !window.isSecureContext) {
                    if (config.LOCAL_DEV_LAT_LNG) {
                        resolve(config.LOCAL_DEV_LAT_LNG);
                    } else {
                        reject("User location failed");
                    }
                } else {
                    reject("User location failed");
                }
            });
        } else {
            reject("User location is not supported by this browser");
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

/* Fetches tasks from servlet by neighborhood and category */
function fetchTasks(category) {
    let url = "/tasks?zipcode=" + neighborhood[0]+ "&country=" + neighborhood[1];
    if (category !== undefined && category != "all") {
        url += "&category=" + category;
    }
    return fetch(url);
}

/* Displays the tasks received from the server response */
function displayTasks(response) {
    response.json().then(html => {
        if (html){
            document.getElementById("no-tasks-message").style.display = "none";
            document.getElementById("tasks-message").style.display = "block";
            document.getElementById("tasks-list").innerHTML = html;
            document.getElementById("tasks-list").style.display = "block";
            addTasksClickHandlers();
        } else {
            document.getElementById("no-tasks-message").style.display = "block";
            document.getElementById("tasks-message").style.display = "none";
            document.getElementById("tasks-list").style.display = "none";
        }
    });
}

/* Function adds all the necessary tasks 'click' event listeners*/
function addTasksClickHandlers() {

    // adds confirmHelp click event listener to confirm help buttons
    const confirmHelpButtons = document.getElementsByClassName("confirm-help");
    for (let i = 0; i < confirmHelpButtons.length; i++){
        confirmHelpButtons[i].addEventListener("click", function(e) {
            confirmHelp(e.target);
        });
        
    }
    // adds exitHelp click event listener to exit help buttons
    const exitHelpButtons = document.getElementsByClassName("exit-help");
    for (let i = 0; i < exitHelpButtons.length; i++) {
        exitHelpButtons[i].addEventListener("click", function(e) {
            exitHelp(e.target);
        });
    }
    // adds helpOut click event listener to help out buttons
    const helpOutButtons = document.getElementsByClassName("help-out");
        for (let i = 0; i < helpOutButtons.length; i++) {
            if (!helpOutButtons[i].classList.contains("disable-help")) {
                helpOutButtons[i].addEventListener("click", function(e) {
                    helpOut(e.target);
                });
            }
        }
}

function userNeighborhoodIsKnown() {
  return (neighborhood[0] !== null && neighborhood[1] !== null);
}
