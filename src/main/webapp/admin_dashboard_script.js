/**
 * Copyright 2020 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

var map, infoWindow;
var styledMapType;
var mapKey = config.MAPS_KEY;
var userTasksArray;
var oms;
var infoWindows = [];

load(`https://maps.googleapis.com/maps/api/js?key=${mapKey}`); // Add maps API to html

google.charts.load("current", { packages: ["line"] });

google.charts.setOnLoadCallback(drawChart);
window.addEventListener("load", drawMap);
window.addEventListener("resize", drawChart);

function drawChart() {
	var data = new google.visualization.DataTable();
	var rowData = [
		[1, 37.8, 80.8, 41.8],
		[2, 30.9, 69.5, 32.4],
		[3, 25.4, 57, 25.7],
		[4, 11.7, 18.8, 10.5],
		[5, 11.9, 17.6, 10.4],
		[6, 8.8, 13.6, 7.7],
		[7, 7.6, 12.3, 9.6],
		[8, 12.3, 29.2, 10.6],
		[9, 16.9, 42.9, 14.8],
		[10, 12.8, 30.9, 11.6],
		[11, 5.3, 7.9, 4.7],
		[12, 6.6, 8.4, 5.2],
		[13, 4.8, 6.3, 3.6],
		[14, 4.2, 6.2, 3.4],
	];
	data.addColumn("number", "Days");
	data.addColumn("number", "Admins");
	data.addColumn("number", "Consecutive Login");
	data.addColumn("number", "Tasks");
	data.addRows(rowData);
	var chart = new google.charts.Line(document.getElementById("chart-div"));
	chart.draw(data);
}

function openInfoWindow(map, marker, infoWindow, task) {
    infoWindow.setContent(task.detail);
    infoWindow.open(map, marker);
    infoWindows.push(infoWindow);
}

function displayTaskMarker(task) {
    const marker = new google.maps.Marker({
        position: {lat: task.lat, lng: task.lng},
        map: map});
    const infoWindow = new google.maps.InfoWindow;
    // adds marker click listener to close all other opened infowindows
    // and then open the current marker's infowindow
    marker.addListener("spider_click", () => {
        infoWindows.forEach(infoWindow => {
            infoWindow.close();
        });
        openInfoWindow(map, marker, infoWindow, task);
        });
    oms.addMarker(marker);
}

function drawMap() {
	// Styles a map in night mode.
	const LAT = 40.674;
	const LNG = -73.945;
	map = new google.maps.Map(document.getElementById("map-div"), {
		center: { lat: LAT, lng: LNG },
		zoom: 12,
		styles: [
			{ elementType: "geometry", stylers: [{ color: "#242f3e" }] },
			{ elementType: "labels.text.stroke", stylers: [{ color: "#242f3e" }] },
			{ elementType: "labels.text.fill", stylers: [{ color: "#746855" }] },
			{
				featureType: "administrative.locality",
				elementType: "labels.text.fill",
				stylers: [{ color: "#d59563" }],
			},
			{
				featureType: "poi",
				elementType: "labels.text.fill",
				stylers: [{ color: "#d59563" }],
			},
			{
				featureType: "poi.park",
				elementType: "geometry",
				stylers: [{ color: "#263c3f" }],
			},
			{
				featureType: "poi.park",
				elementType: "labels.text.fill",
				stylers: [{ color: "#6b9a76" }],
			},
			{
				featureType: "road",
				elementType: "geometry",
				stylers: [{ color: "#38414e" }],
			},
			{
				featureType: "road",
				elementType: "geometry.stroke",
				stylers: [{ color: "#212a37" }],
			},
			{
				featureType: "road",
				elementType: "labels.text.fill",
				stylers: [{ color: "#9ca5b3" }],
			},
			{
				featureType: "road.highway",
				elementType: "geometry",
				stylers: [{ color: "#746855" }],
			},
			{
				featureType: "road.highway",
				elementType: "geometry.stroke",
				stylers: [{ color: "#1f2835" }],
			},
			{
				featureType: "road.highway",
				elementType: "labels.text.fill",
				stylers: [{ color: "#f3d19c" }],
			},
			{
				featureType: "transit",
				elementType: "geometry",
				stylers: [{ color: "#2f3948" }],
			},
			{
				featureType: "transit.station",
				elementType: "labels.text.fill",
				stylers: [{ color: "#d59563" }],
			},
			{
				featureType: "water",
				elementType: "geometry",
				stylers: [{ color: "#17263c" }],
			},
			{
				featureType: "water",
				elementType: "labels.text.fill",
				stylers: [{ color: "#515c6d" }],
			},
			{
				featureType: "water",
				elementType: "labels.text.stroke",
				stylers: [{ color: "#17263c" }],
			},
		],
	});
    oms = new OverlappingMarkerSpiderfier(map, {
            markersWontMove: true,
            markersWontHide: false,
            basicFormatEvents: true,
            keepSpiderfied: true
        });
	//Resize Function
	google.maps.event.addDomListener(window, "resize", function () {
		var center = map.getCenter();
		google.maps.event.trigger(map, "resize");
		map.setCenter(center);
	});
    infoWindow = new google.maps.InfoWindow;
	if (navigator.geolocation) {
		navigator.geolocation.getCurrentPosition(function(position) {
			var pos = {
				lat: position.coords.latitude,
				lng: position.coords.longitude
			};

			infoWindow.setPosition(pos);
			infoWindow.open(map);
            infoWindow.close();
			map.setCenter(pos);
		}, function() {
			handleLocationError(true, infoWindow, map.getCenter());
		});
	} else {
		// Browser doesn't support Geolocation
		handleLocationError(false, infoWindow, map.getCenter());
	}
    getUserTasks();
}

function handleLocationError(browserHasGeolocation, infoWindow, pos) {
	infoWindow.setPosition(pos);
	infoWindow.setContent(browserHasGeolocation ?
		'Error: The Geolocation service failed.' :
		'Error: Your browser doesn\'t support geolocation.');
	infoWindow.open(map);
}

function load(file) {
	var src = document.createElement("script");
	src.setAttribute("type", "text/javascript");
	src.setAttribute("src", file);
	document.getElementsByTagName("head")[0].appendChild(src);
}

function getUserTasks() {
	fetch("/admin-user-tasks")
		.then((response) => response.json())
		.then((tasks) => {
			userTasksArray = tasks;
			let taskSection = document.getElementById("user-tasks");
			for (userTask of tasks) {
				taskSection.innerHTML += addTask(userTask);
                displayTaskMarker(userTask);
			}
		});
}

function addTask(task) {
	let string = `<a href="#popup-overlay"><li class="admin-user-task"  onclick="openWithPopup('${task.keyString}')"><span><h3> ${task.category} </h3>`;
	string += `<h4> ${task.owner} </h4></span> <a href="#"><span id="delete-btn" onclick=deleteTask('${task.keyString}')><i class="fas fa-trash fa-2x"></i></span></a></li></a>`;
	return string;
}

function searchTasks(id){
	for(task of userTasksArray){
		if(id == task.keyString){
			return task;
		}
	}
}

async function openWithPopup(id){
	const task = await searchTasks(id);
	document.getElementById("task-detail-input").value = task.detail;
	document.getElementById("reward-input").value = task.reward;
	document.getElementById("task-id-input").value = task.keyString;
	document.getElementById("edit-category-input").value = task.category;
}

async function deleteTask(keyString) {
	if (confirm("Are you sure that you want to delete the task?")) {
		const queryURL = "/tasks?key=" + keyString;
		const request = new Request(queryURL, {method: "DELETE"});
		await fetch(request);
        location.reload();
        return false;
	}
}