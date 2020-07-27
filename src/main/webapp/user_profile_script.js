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

var markers = [];
var map;
var cursorString;
const GOOGLE_KIRKLAND_LAT = 47.669846;
const GOOGLE_KIRKLAND_LNG = -122.1996099;
const MAPSKEY = config.MAPS_KEY;

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

function validateInfoForm(id) {
    var result = true;
    var form = document.getElementById(id);
    var inputName = ["nickname", "address", "zipcode", "country", "phone"];
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

function sendMessage() {
    var msgField = document.getElementById("msg-input").value.trim();
    if (msgField === "") {
        document.getElementById("msg-input").classList.add("highlight");
        alert("You cannot send an empty message.");
    } else {
        document.getElementById("msg-input").classList.remove("highlight");
        var form = document.getElementById("chat-box");
        var messageContent = form["msg"].value;
        const queryURL = "/messages?task-id=" + form["task-id"].value 
                        + "&msg=" + encodeURIComponent(messageContent);
        const request = new Request(queryURL, {method: "POST"});
        fetch(request).then((response) => {
            const msgContainer = document.getElementById("message-container");
            var newMessage = document.createElement("div");
            newMessage.className = "sentByMe";
            newMessage.appendChild(document.createTextNode(messageContent)); 
            msgContainer.appendChild(newMessage);
            msgContainer.scrollTop = msgContainer.scrollHeight;
        });
        form["msg"].value = "";
    }
    return false;
}

async function refresh() {
    loadMessages(document.getElementById("chat-id-input").value);
}

// To address the scaling issue and avoid loading too many messages at a time,
// the chat box would first load at most 10 most recent messages.
async function loadMessages(keyString) {
    const queryURL = "/messages?key=" + keyString;
    const request = new Request(queryURL, {method: "GET"});
    const response = await fetch(request);
    const result = await response.json();
    const msgResponse = result.messages;
    cursorString = result.cursorString;

    const msgContainer = document.getElementById("message-container");
    msgContainer.innerHTML = "";
    if (msgResponse.length === 10) {
        var msg = msgResponse[9];
        var newMessage = document.createElement("div");
        newMessage.className = msg.className;
        newMessage.appendChild(document.createTextNode(msg.message)); 
        var moreMessageBtn = document.createElement("button");
        moreMessageBtn.setAttribute("class", "more-msg-button");
        moreMessageBtn.textContent = "Load 10 more messages";
        moreMessageBtn.addEventListener("click", function() { 
            loadMoreMessages(keyString, newMessage)
        });
        msgContainer.appendChild(moreMessageBtn);
        msgContainer.appendChild(newMessage);
        for (var index = 8; index >= 0; index--) {
            var message = msgResponse[index];
            var newMessageDiv = document.createElement("div");
            newMessageDiv.className = message.className;
            newMessageDiv.appendChild(document.createTextNode(message.message)); 
            msgContainer.appendChild(newMessageDiv);
        }
    } else {
        for (var index = msgResponse.length - 1; index >= 0; index--) {
            var msg = msgResponse[index];
            var newMessage = document.createElement("div");
            newMessage.className = msg.className;
            newMessage.appendChild(document.createTextNode(msg.message)); 
            msgContainer.appendChild(newMessage);
        }
    }

    // Keep message container scrolled to bottom at the beginning
    msgContainer.scrollTop = msgContainer.scrollHeight;
}

// loadMoreMessages(key, messageDiv) will load 10 more messages of
// task with keyString equals to key. After loading, scroll to element messageDiv
async function loadMoreMessages(key, messageDiv) {
    const queryURL = "/messages?key=" + key + "&cursor=" + cursorString;
    const request = new Request(queryURL, {method: "GET"});
    const response = await fetch(request);
    const result = await response.json();
    const msgResponse = result.messages;
    cursorString = result.cursorString;

    const msgContainer = document.getElementById("message-container");
    msgContainer.removeChild(msgContainer.childNodes[0]);
    if (msgResponse.length === 10) {
        var newMessageDiv;
        for (var index = 0; index < 10; index++) {
            var message = msgResponse[index];
            newMessageDiv = document.createElement("div");
            newMessageDiv.className = message.className;
            newMessageDiv.appendChild(document.createTextNode(message.message)); 
            msgContainer.insertBefore(newMessageDiv, msgContainer.childNodes[0]);
        }
        var moreMessageBtn = document.createElement("button");
        moreMessageBtn.setAttribute("class", "more-msg-button");
        moreMessageBtn.textContent = "Load 10 more messages";
        moreMessageBtn.addEventListener("click", function() { 
            loadMoreMessages(key, newMessageDiv);
        });
        msgContainer.insertBefore(moreMessageBtn, msgContainer.childNodes[0]);
    } else {
        for (var index = 0; index < msgResponse.length; index++) {
            var message = msgResponse[index];
            newMessageDiv = document.createElement("div");
            newMessageDiv.className = message.className;
            newMessageDiv.appendChild(document.createTextNode(message.message)); 
            msgContainer.insertBefore(newMessageDiv, msgContainer.childNodes[0]);
        } 
    }
    messageDiv.scrollIntoView();
}

// deleteMessages(taskId) will delete all the messages stored in datastore related
// with the task corresponds to the given taskId
async function deleteMessages(taskId) {
    const queryURL = "messages?task-id=" + taskId;
    const request = new Request(queryURL, {method: "DELETE"});
    const response = await fetch(request);
    return;
}

async function getTaskInfo(keyString) {
    const queryURL = "/tasks/info?key=" + keyString;
    const request = new Request(queryURL, {method: "GET"});
    const response = await fetch(request);
    const info = await response.json();
    return info;
}

async function deleteTask(keyString) {
    const info = await getTaskInfo(keyString);
    if (info.status !== "OPEN") {
        window.alert("You can only delete an 'OPEN' task.")
    } else {
        if (confirm("Are you sure that you want to delete the task?")) {
            const queryURL = "/tasks?key=" + keyString;
            const request = new Request(queryURL, {method: "DELETE"});
            const response = await fetch(request);
            deleteMessages(keyString);
            showNeedHelp();
        }
    }
}

async function editTask(keyString) {
    const info = await getTaskInfo(keyString);
    if (info.status !== "OPEN") {
        window.alert("You can only edit an 'OPEN' task.");
    } else {
        document.getElementById("edit-detail-input").value = info.detail;
        document.getElementById("edit-overview-input").value = info.overview;
        document.getElementById("edit-category-input").value = info.category;
        document.getElementById("edit-point-input").value = info.reward.toString();
        const id_input = document.getElementById("task-id-input");
        id_input.value = info.keyString;
        document.getElementById("editTaskModalWrapper").style.display = "block";
        showNeedHelp();
    }
}

async function editInfo() {
    const request = new Request("/account", {method: "GET"});
    const response = await fetch(request);
    const userInfo = await response.json();
    document.getElementById("edit-nickname-input").value = userInfo[0];
    document.getElementById("edit-address-input").value = userInfo[1];
    document.getElementById("edit-phone-number-input").value = userInfo[2];
    document.getElementById("edit-zipcode-input").value = userInfo[3];
    document.getElementById("edit-country-input").value = userInfo[4];
    showInfoModal();
}

async function completeTask(keyString) {
    const info = await getTaskInfo(keyString);
    if (info.status !== "IN PROGRESS") {
        window.alert("You have already marked the task as complete.");
    } else if (info.owner === info.helper) {
        window.alert("You cannot complete a task published by yourself!");
    } else {
        if (confirm("Are you sure that you have already completed the task?")) {
            const queryURL = "/tasks/info?key=" + keyString + "&status=" + "COMPLETE: AWAIT VERIFICATION";
            const request = new Request(queryURL, {method: "POST"});
            const response = await fetch(request);
            showOfferHelp();
        }
    }
}

async function abandonTask(keyString) {
    const info = await getTaskInfo(keyString);
    if (info.status !== "IN PROGRESS") {
        window.alert("You have already marked the task as complete.");
    } else {
        if (confirm("Are you sure that you want to abandon the task?")) {
            const queryURL = "/tasks/info?key=" + keyString + "&status=" + "OPEN";
            const request = new Request(queryURL, {method: "POST"});
            const response = await fetch(request);
            deleteMessages(keyString);
            showOfferHelp();
        }
    }
}

async function verifyTask(keyString) {
    const info = await getTaskInfo(keyString);
    if (info.status === "COMPLETE") {
        window.alert("You have already verified the task.");
    } else {
        if (confirm("Are you sure that you want to verify the task?")) {
            const queryURL = "/tasks/info?key=" + keyString + "&status=" + "COMPLETE";
            const request = new Request(queryURL, {method: "POST"});
            const response = await fetch(request);
            deleteMessages(keyString);
            showNeedHelp();
        }
    }
}

async function disapproveTask(keyString) {
    const info = await getTaskInfo(keyString);
    if (info.status === "COMPLETE") {
        window.alert("You have already verified the task.");
    } else {
        if (confirm("Are you sure that you want to disapprove the task?")) {
            const queryURL = "/tasks/info?key=" + keyString + "&status=" + "IN PROGRESS";
            const request = new Request(queryURL, {method: "POST"});
            const response = await fetch(request);
            showNeedHelp();
        }
    }
}

async function showTaskInfo(keyString) {
    const info = await getTaskInfo(keyString);
    var detailContainer = document.getElementById("task-detail-container");
    detailContainer.innerHTML = "";
    detailContainer.appendChild(document.createTextNode(info.detail));
    document.getElementById("chat-id-input").value = keyString;
    var modal = document.getElementById("taskInfoModalWrapper");
    modal.style.display = "block";
    loadMessages(keyString);
}

function showNeedHelp() {
    if (document.getElementById("need-help") == null) return;
    document.getElementById("need-help").style.display = "table";
    document.getElementById("create").style.display = "block";
    document.getElementById("offer-help").style.display = "none";
    document.getElementById("await-verif").style.display = "table";
    document.getElementById("complete-task").style.display = "none";
    document.getElementById("need-help-button").style.backgroundColor = "#3e8e41";
    document.getElementById("offer-help-button").style.backgroundColor = "#4CAF50";
    displayNeedHelpTasks();
    displayNeedHelpCompleteTasks();
}

function showOfferHelp() {
    document.getElementById("need-help").style.display = "none";
    document.getElementById("create").style.display = "none";
    document.getElementById("offer-help").style.display = "table";
    document.getElementById("await-verif").style.display = "none";
    document.getElementById("complete-task").style.display = "table";
    document.getElementById("need-help-button").style.backgroundColor = "#4CAF50";
    document.getElementById("offer-help-button").style.backgroundColor = "#3e8e41";
    displayOfferHelpTasks();
    displayOfferHelpCompleteTasks();
}

function showModal() {
    var modal = document.getElementById("createTaskModalWrapper");
    modal.style.display = "block";
}

function showInfoModal() {
    var modal = document.getElementById("updateInfoModalWrapper");
    modal.style.display = "block";
}

function closeModal() {
    var modal = document.getElementById("createTaskModalWrapper");
    modal.style.display = "none";
}

function closeEditModal() {
    var modal = document.getElementById("editTaskModalWrapper");
    modal.style.display = "none";
}

function closeInfoModal() {
    var modal = document.getElementById("updateInfoModalWrapper");
    modal.style.display = "none";
}

function closeTaskInfoModal() {
    var modal = document.getElementById("taskInfoModalWrapper");
    modal.style.display = "none";
}

// If the user clicks outside of the modal, closes the modal directly
window.onclick = function(event) {
    var wrapperId = ["createTaskModalWrapper", "editTaskModalWrapper", "updateInfoModalWrapper", "taskInfoModalWrapper"];
    for (var i = 0; i < wrapperId.length; i++) {
        var modal = document.getElementById(wrapperId[i]);
        if (event.target == modal) {
            modal.style.display = "none";
        }
    }
}

async function displayNeedHelpTasks() {
    const queryURL = "/mytasks?keyword=Owner&complete=False";
    const request = new Request(queryURL, {method: "GET"});
    const response = await fetch(request);
    const taskResponse = await response.json();
    const needHelpBody = document.getElementById("need-help-body");
    needHelpBody.innerHTML = "";
    for (var index = 0; index < taskResponse.length; index++) {
        var tr = document.createElement("tr");
        var task = taskResponse[index];
        var data = [task.overview, task.helper, task.status];
        const keyStringCopy = task.keyString.slice();
        for (var i = 0; i < data.length; i++) {
            var td = document.createElement("td");
            if (i === 0) {
                td.addEventListener("click", function () { showTaskInfo(keyStringCopy) });
                td.className = "task-overview";
            }
            td.appendChild(document.createTextNode(data[i]));
            tr.appendChild(td);
        }
        var editTd = document.createElement("td");
        var editBtn = document.createElement("button");
        editBtn.className = "edit-task";
        editBtn.addEventListener("click", function () { editTask(keyStringCopy) });
        editBtn.innerHTML = (task.status === "OPEN") ? '<i class="fa fa-edit"></i>':'<i class="fa fa-ban"></i>';
        editTd.appendChild(editBtn);
        var deleteTd = document.createElement("td");
        var deleteBtn = document.createElement("button");
        deleteBtn.className = "delete-task";
        deleteBtn.addEventListener("click", function () { deleteTask(keyStringCopy) });
        deleteBtn.innerHTML = (task.status === "OPEN") ? '<i class="fa fa-trash-o"></i>':'<i class="fa fa-ban"></i>';
        deleteTd.appendChild(deleteBtn);
        tr.appendChild(editTd);
        tr.appendChild(deleteTd);
        needHelpBody.appendChild(tr);
    }
}

async function displayNeedHelpCompleteTasks() {
    const queryURL = "/mytasks?keyword=Owner&complete=True";
    const request = new Request(queryURL, {method: "GET"});
    const response = await fetch(request);
    const taskResponse = await response.json();
    const completeTaskBody = document.getElementById("await-verif-body");
    completeTaskBody.innerHTML = "";
    for (var index = 0; index < taskResponse.length; index++) {
        var tr = document.createElement("tr");
        var task = taskResponse[index];
        var data = [task.overview, task.helper, task.status];
        const keyStringCopy = task.keyString.slice();
        for (var i = 0; i < data.length; i++) {
            var td = document.createElement("td");
            if (i === 0) {
                td.addEventListener("click", function () { showTaskInfo(keyStringCopy) });
                td.className = "task-overview";
            }
            td.appendChild(document.createTextNode(data[i]));
            tr.appendChild(td);
        }
        var verifyTd = document.createElement("td");
        var verifyBtn = document.createElement("button");
        verifyBtn.className = "verify-task";
        verifyBtn.addEventListener("click", function () { verifyTask(keyStringCopy) });
        verifyBtn.innerHTML = (task.status !== "COMPLETE") ? '<i class="fa fa-check"></i>':'<i class="fa fa-ban"></i>';
        verifyTd.appendChild(verifyBtn);
        var disapproveTd = document.createElement("td");
        var disapproveBtn = document.createElement("button");
        disapproveBtn.className = "disapprove-task";
        disapproveBtn.addEventListener("click", function () { disapproveTask(keyStringCopy) });
        disapproveBtn.innerHTML = (task.status !== "COMPLETE") ? '<i class="fa fa-times"></i>':'<i class="fa fa-ban"></i>';
        disapproveTd.appendChild(disapproveBtn);
        tr.appendChild(verifyTd);
        tr.appendChild(disapproveTd);
        completeTaskBody.appendChild(tr);
    }
}

async function displayOfferHelpTasks() {
    const queryURL = "/mytasks?keyword=Helper&complete=False";
    const request = new Request(queryURL, {method: "GET"});
    const response = await fetch(request);
    const taskResponse = await response.json();
    const offerHelpBody = document.getElementById("offer-help-body");
    offerHelpBody.innerHTML = "";
    for (var index = 0; index < taskResponse.length; index++) {
        var tr = document.createElement("tr");
        var task = taskResponse[index];
        var data = [task.overview, task.status, task.owner, task.address];
        const keyStringCopy = task.keyString.slice();
        for (var i = 0; i < data.length; i++) {
            var td = document.createElement("td");
            if (i === 0) {
                td.addEventListener("click", function () { showTaskInfo(keyStringCopy) });
                td.className = "task-overview";
            }
            td.appendChild(document.createTextNode(data[i]));
            tr.appendChild(td);
        }
        var completeTd = document.createElement("td");
        var completeBtn = document.createElement("button");
        completeBtn.className = "complete-task";
        completeBtn.addEventListener("click", function () { completeTask(keyStringCopy) });
        completeBtn.innerHTML = (task.status === "IN PROGRESS") ? '<i class="fa fa-check"></i>':'<i class="fa fa-ban"></i>';
        completeTd.appendChild(completeBtn);
        var abandonTd = document.createElement("td");
        var abandonBtn = document.createElement("button");
        abandonBtn.className = "abandon-task";
        abandonBtn.addEventListener("click", function () { abandonTask(keyStringCopy) });
        abandonBtn.innerHTML = (task.status === "IN PROGRESS") ? '<i class="fa fa-times"></i>':'<i class="fa fa-ban"></i>';;
        abandonTd.appendChild(abandonBtn);
        tr.appendChild(completeTd);
        tr.appendChild(abandonTd);
        offerHelpBody.appendChild(tr);
    }
}

async function displayOfferHelpCompleteTasks() {
    const queryURL = "/mytasks?keyword=Helper&complete=True";
    const request = new Request(queryURL, {method: "GET"});
    const response = await fetch(request);
    const taskResponse = await response.json();
    const completeTaskBody = document.getElementById("complete-task-body");
    completeTaskBody.innerHTML = "";
    for (var index = 0; index < taskResponse.length; index++) {
        var tr = document.createElement("tr");
        var task = taskResponse[index];
        var data = [task.overview, task.status, task.owner, task.reward.toString()];
        const keyStringCopy = task.keyString.slice();
        for (var i = 0; i < data.length; i++) {
            var td = document.createElement("td");
            if (i === 0) {
                td.addEventListener("click", function () { showTaskInfo(keyStringCopy) });
                td.className = "task-overview";
            }
            td.appendChild(document.createTextNode(data[i]));
            tr.appendChild(td);
        }
        completeTaskBody.appendChild(tr);
    }
}

/* Calls showNeedHelp and initMap once page has loaded */
if (document.readyState === 'loading') {
    // adds on load event listeners if document hasn't yet loaded
    document.addEventListener('DOMContentLoaded', initMap);
    document.addEventListener('DOMContentLoaded', showNeedHelp)
} else {
    // if DOMContentLoaded has already fired, it simply calls the functions
    initMap();
    showNeedHelp();
}

/**
 * Initialize a map on the page
 */
async function initMap() {
    const script = document.createElement("script");
    script.type = "text/javascript";
    script.src = "https://maps.googleapis.com/maps/api/js?key=" + 
                    MAPSKEY + "&callback=initialize&libraries=places&language=en";
    document.head.appendChild(script);

    // After the Google Maps API script has been loaded, start initializing the map
    window.initialize = async function() {
        markers = [];
        map = new google.maps.Map(document.getElementById("map"), {
            center: {lat: GOOGLE_KIRKLAND_LAT, lng: GOOGLE_KIRKLAND_LNG},
            zoom: 18,
            styles: [
                {
                    "elementType": "geometry",
                    "stylers": [{"color": "#ebe3cd"}]
                },
                {
                    "elementType": "labels.text.fill",
                    "stylers": [{"color": "#523735"}]
                },
                {
                    "elementType": "labels.text.stroke",
                    "stylers": [{"color": "#f5f1e6"}]
                },
                {
                    "featureType": "administrative",
                    "elementType": "geometry.stroke",
                    "stylers": [{"color": "#c9b2a6"}]
                },
                {
                    "featureType": "administrative.land_parcel",
                    "elementType": "geometry.stroke",
                    "stylers": [{"color": "#dcd2be"}]
                },
                {
                    "featureType": "administrative.land_parcel",
                    "elementType": "labels.text.fill",
                    "stylers": [{"color": "#ae9e90"}]
                },
                {
                    "featureType": "landscape.man_made",
                    "elementType": "geometry.stroke",
                    "stylers": [{"color": "#36aff9"}]
                },
                {
                    "featureType": "landscape.natural",
                    "elementType": "geometry",
                    "stylers": [{"color": "#dfd2ae"}]
                },
                {
                    "featureType": "poi",
                    "elementType": "geometry",
                    "stylers": [{"color": "#dfd2ae"}]
                },
                {
                    "featureType": "poi",
                    "elementType": "labels.text.fill",
                    "stylers": [{"color": "#93817c"}]
                },
                {
                    "featureType": "poi.park",
                    "elementType": "geometry.fill",
                    "stylers": [{"color": "#a5b076"}]
                },
                {
                    "featureType": "poi.park",
                    "elementType": "labels.text.fill",
                    "stylers": [{"color": "#28c4fa"}, {"lightness": -5}, {"weight": 2}]
                },
                {
                    "featureType": "poi.park",
                    "elementType": "labels.text.stroke",
                    "stylers": [{"color": "#f9fcc7"}]
                },
                {
                    "featureType": "road",
                    "elementType": "geometry",
                    "stylers": [{"color": "#f5f1e6"}]
                },
                {
                    "featureType": "road.arterial",
                    "elementType": "geometry",
                    "stylers": [{"color": "#fdfcf8"}]
                },
                {
                    "featureType": "road.highway",
                    "elementType": "geometry",
                    "stylers": [{"color": "#f8c967"}]
                },
                {
                    "featureType": "road.highway",
                    "elementType": "geometry.stroke",
                    "stylers": [{"color": "#f2756a"}]
                },
                {
                    "featureType": "road.highway",
                    "elementType": "labels.text.fill",
                    "stylers": [{"color": "#f98357"}]
                },
                {
                    "featureType": "road.highway.controlled_access",
                    "elementType": "geometry",
                    "stylers": [{"color": "#e98d58"}]
                },
                {
                    "featureType": "road.highway.controlled_access",
                    "elementType": "geometry.stroke",
                    "stylers": [{"color": "#db8555"}]
                },
                {
                    "featureType": "road.local",
                    "elementType": "labels.text.fill",
                    "stylers": [{"color": "#806b63"}]
                },
                {
                    "featureType": "transit.line",
                    "elementType": "geometry",
                    "stylers": [{"color": "#dfd2ae"}]
                },
                {
                    "featureType": "transit.line",
                    "elementType": "labels.text.fill",
                    "stylers": [{"color": "#8f7d77"}]
                },
                {
                    "featureType": "transit.line",
                    "elementType": "labels.text.stroke",
                    "stylers": [{"color": "#ebe3cd"}]
                },
                {
                    "featureType": "transit.station",
                    "elementType": "geometry",
                    "stylers": [{"color": "#dfd2ae"}]
                },
                {
                    "featureType": "water",
                    "elementType": "geometry.fill",
                    "stylers": [{"color": "#65d3f9"}, {"saturation": -10},  {"lightness": 10}]
                },
                {
                    "featureType": "water",
                    "elementType": "labels.text.fill",
                    "stylers": [{"color": "#92998d"}]
                }
            ],
        });
        map.setTilt(45);

        var geocoder = new google.maps.Geocoder();
        var infowindow = new google.maps.InfoWindow();

        // When the map is clicked, display a marker and fill out the address info
        map.addListener("click", function(event) {
            var marker = displayMarker(event.latLng);
            geocodeLatLng(geocoder, map, infowindow, event.latLng, marker);
        });

        var input = document.getElementById("place-input");
        var searchBox = new google.maps.places.SearchBox(input);
        map.controls[google.maps.ControlPosition.TOP_LEFT].push(input);

        // Restrict the search result near to the current viewport
        map.addListener("bounds_changed", function() {
            searchBox.setBounds(map.getBounds());
        });

        // Place a new marker on the place that the user searches
        searchBox.addListener("places_changed", function() {
            var places = searchBox.getPlaces();
            if (places.length === 0) {
                return;
            }

            var bounds = new google.maps.LatLngBounds();
            places.forEach(function(place) {
                if (!place.geometry) {
                    return;
                }
                if (place.geometry.viewport) {
                    bounds.union(place.geometry.viewport);
                } else {
                    bounds.extend(place.geometry.location);
                }
            });
            map.fitBounds(bounds);
        });

        function onError() {
            console.log("Unable to resolve the user's current location");
        }

        function onSuccess(geo) {
            const position = {
                lat: geo.coords.latitude,
                lng: geo.coords.longitude
            };

            // Set the center to the user's current location
            map.setCenter(position);
        }

        if (!navigator.geolocation) {
            onError();
        } else {
            await navigator.geolocation.getCurrentPosition(onSuccess, onError);
        }
    }
}

function displayMarker(position) {
    var lat = position.lat();
    var lng = position.lng();
    let marker = new google.maps.Marker({
        position: {lat: lat, lng: lng},
        map: map
    });

    google.maps.event.addListener(marker, "dblclick", function(event) {
        deleteMarker(lat, lng);

        // Clear the address, zipcode and country input
        document.getElementById("edit-address-input").value = "";
        document.getElementById("edit-zipcode-input").value = "";
        document.getElementById("edit-country-input").value = "";

        // Add a placeholder to remind the user to fill out the blank
        document.getElementById("edit-address-input").placeholder = "Don't forget to input your address";
        document.getElementById("edit-zipcode-input").placeholder = "Don't forget to input your zipcode";
        document.getElementById("edit-country-input").placeholder = "Don't forget to input your country";
    })
    
    // There is at most one marker displayed on the map
    if (markers.length > 0) {
        markers[0].setMap(null);
    }
    markers = [];
    markers.push(marker);
    return marker;
}

function deleteMarker(latitude, longitude) {
    markers = markers.filter(function(marker) { 
        if (marker.getPosition().lat() !== latitude 
         || marker.getPosition().lng() !== longitude) {
             return true;
        } else {
            marker.setMap(null);
            return false;
        }
    });
}

function geocodeLatLng(geocoder, map, infowindow, position, marker) {
    geocoder.geocode({ location: position }, function(results, status) {
        if (status === "OK") {
            if (results[0]) {
                map.setZoom(19);
                infowindow.setContent(results[0].formatted_address);
                infowindow.open(map, marker);

                // Set the input address field to the formatted address
                document.getElementById("edit-address-input").value = results[0].formatted_address;
                
                // Get the zipcode and country from the Geocoding response
                // Since the postal code and country are often at the end of address_components, we loop from back to front
                for (var i = results[0].address_components.length - 1; i >= 0; i--) {
                    if (results[0].address_components[i].types[0] === "country") {
                        document.getElementById("edit-country-input").value = results[0].address_components[i].long_name;
                    }
                    if (results[0].address_components[i].types[0] === "postal_code") {
                        document.getElementById("edit-zipcode-input").value = results[0].address_components[i].long_name;
                    }
                }
            } else {
                window.alert("No results found");
            }
        } else {
            window.alert("Geocoder failed due to: " + status);
        }
    });
}
