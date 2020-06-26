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

/** Function that visually mimics the functionality of filtering tasks by category */
function filterBy(category) {
    const categoryButtons = document.getElementsByClassName("categories");
    const tasks = document.getElementsByClassName("task");
    const idName = "category-" + category;

	// Unhighlights and resets styling for all category buttons
    for (let i = 0; i < categoryButtons.length; i++){
        let button = categoryButtons[i];
        if (document.getElementById(idName) != categoryButtons[i]) {
            button.style.backgroundColor = "transparent";
    		button.style.color = "black";
        	button.style.fontWeight = "normal";
        	button.addEventListener("mouseover", function() {
                button.style.backgroundColor = "lightgray";
            });
            button.addEventListener("mouseout", function() {
                button.style.backgroundColor = "transparent"
            });
        } else {
            button.style.backgroundColor = "gray";
        	button.style.color = "white";
        	button.style.fontWeight = "bold";
            button.addEventListener("mouseover", function() {
                button.style.backgroundColor = "gray";
            });
            button.addEventListener("mouseout", function() {
                button.style.backgroundColor = "gray"
            });
        }
    }

    // Shows all tasks and highlights the 'ALL' button
	if (category == "all") {
        for (let i = 0; i < tasks.length; i++) {
            tasks[i].style.display = "block";

            // removes any help with task overlays
            let overlay = tasks[i].getElementsByClassName("confirm-overlay");
            overlay[0].style.display = "none";
        }
    }
    
    // Hides all tasks that don't match the category, shows all
    // of those that do and highlights appropriate category button
	else {
        for (let i = 0; i < tasks.length; i++) {
            if (tasks[i].classList.contains(category)) {
                tasks[i].style.display = "block";
            } else {
                tasks[i].style.display = "none";
            }
            // removes any help with task overlays
            let overlay = tasks[i].getElementsByClassName("confirm-overlay");
            overlay[0].style.display = "none";
        }
    }
}

/** Function that display the help out confirmation overlay */
function helpOut(element) {
    const overlay = element.parentNode.parentNode.parentNode.getElementsByClassName("confirm-overlay");
    overlay[0].style.display = "block";
}

/** Function that hides the task after user confirms they want to help out */
function removeTask(element) {
    element.parentNode.parentNode.style.display = "none";
}

/** Function that hides the help out confirmation overlay */
function cancelHelpOut(element) {
	element.parentNode.style.display = "none";
}


/** Leonard's implementation of the Add Task modal */
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

function adjustControlBar(userLogged){
	if (userLogged === false) {
        document.getElementById("categories").style.width = "100%";
    } else {
        document.getElementById("categories").style.width = "90%";
    }
}

/* Event listener to get user's neighborhood*/
if (document.readyState === 'loading') {
    // adds on load event listener if document hasn't yet loaded
	document.addEventListener('DOMContentLoaded', getUserNeighborhood);
} else {
    // if DOMContentLoaded has already fired, it simply calls the function
    getUserNeighborhood();
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
        	.then(neighborhood => {
                // For now this just prints the neighborhood to the console
                // but the neighborhood will be used when implementing
                // the list tasks feature
            	console.log(neighborhood);
        	}).catch(() => {
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
                    resolve([zipCode, country]);
                } else reject("Couldn't get neighborhood");
            } else reject("Couldn't get neighborhood");
        });
    });
}


