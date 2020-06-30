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
let neighborhood = null;

/* Calls addOnClicks and getUserNeighborhood once page has loaded */
if (document.readyState === 'loading') {
    // adds on load event listeners if document hasn't yet loaded
    document.addEventListener('DOMContentLoaded', addUIClickHandlers)
    document.addEventListener('DOMContentLoaded', getUserNeighborhood);
} else {
    // if DOMContentLoaded has already fired, it simply calls the functions
    addUIClickHandlers();
    getUserNeighborhood();
}

function addUIClickHandlers() {
    // adds showModal and closeModal click events for the add task button
    if (document.body.contains(document.getElementById("addtaskbutton"))) {
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

/* Function adds all the necessary 'click' event listeners*/
function addTasksClickHandlers() {

    // adds removeTask click event listener to remove task buttons
    const removeTaskButtons = document.getElementsByClassName("removetask");
    for (let i = 0; i < removeTaskButtons.length; i++){
        if (document.body.contains(removeTaskButtons[i])){
            removeTaskButtons[i].addEventListener("click", function(e) {
            	removeTask(e.target);
        	});
        }
    }
    // adds cancelHelpOut click event listener to exit confirm buttons
    const exitConfirmButtons = document.getElementsByClassName("exit-confirm");
    for (let i = 0; i < exitConfirmButtons.length; i++) {
        if (document.body.contains(exitConfirmButtons[i])){
            exitConfirmButtons[i].addEventListener("click", function(e) {
                cancelHelpOut(e.target);
            });
        }
    }
    // adds helpOut click event listener to help out buttons
    const helpOutButtons = document.getElementsByClassName("help-button");
        for (let i = 0; i < helpOutButtons.length; i++) {
            if (document.body.contains(helpOutButtons[i])){
                if (!helpOutButtons[i].classList.contains("disable-help")) {
                    helpOutButtons[i].addEventListener("click", function(e) {
                        helpOut(e.target);
                    });
                }
            }
        }
}

/* Function filters tasks by categories and styles selected categories */
function filterTasksBy(category) {
    fetchTasks(category).then(response => displayTasks(response));

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

/* Function that display the help out confirmation overlay */
function helpOut(element) {
    const task = element.closest(".task");
    const overlay = task.getElementsByClassName("confirm-overlay");
    overlay[0].style.display = "block";
}

/* Function that hides the task after user confirms they want to help out */
function removeTask(element) {
    element.closest(".task").style.display = "none";
}

/* Function that hides the help out confirmation overlay */
function cancelHelpOut(element) {
	element.closest(".confirm-overlay").style.display = "none";
}

/* Leonard's implementation of the Add Task modal */
function showModal() {
    var modal = document.getElementById("createTaskModal");
    modal.style.display = "block";
}
function closeModal() {
    var modal = document.getElementById("createTaskModal");
    modal.style.display = "none";
}
// If the user clicks outside of the modal, closes the modal directly
window.onclick = function(event) {
    var modal = document.getElementById("createTaskModal");
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
    // as an argument. toNeighborhood consequently returns the user's neighborhood
	window.initialize = function () {
        getUserLocation().then(location => toNeighborhood(location))
        	.then(() => fetchTasks())
            .then((response) => displayTasks(response))
            .catch(() => {
                console.error("User location and/or neighborhood could not be retrieved");
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
            }, function() {
                reject("User location failed");
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
            document.getElementById("tasks-list").innerHTML = html;
            document.getElementById("tasks-list").style.display = "block";
            addTasksClickHandlers();
        } else {
            document.getElementById("no-tasks-message").style.display = "block";
            document.getElementById("tasks-list").style.display = "none";
        }
    });
}
