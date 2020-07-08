var map;
var styledMapType;
var map_key = keys.MAP_KEY;

load(`https://maps.googleapis.com/maps/api/js?key=${map_key}`); // Add maps API to html

google.charts.load("current", { packages: ["line"] });

google.charts.setOnLoadCallback(drawChart);
window.addEventListener("load", drawMap);
window.addEventListener("load", initUserTasks());
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

function drawMap() {
  // Styles a map in night mode.
  const LAT = 40.674;
  const LNG = -73.945;
  var map = new google.maps.Map(document.getElementById("map-div"), {
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
  //Resize Function
  google.maps.event.addDomListener(window, "resize", function () {
    var center = map.getCenter();
    google.maps.event.trigger(map, "resize");
    map.setCenter(center);
  });
}

function load(file) {
  var src = document.createElement("script");
  src.setAttribute("type", "text/javascript");
  src.setAttribute("src", file);
  document.getElementsByTagName("head")[0].appendChild(src);
}

function initUserTasks() {
  fetch("/user-tasks")
    .then((response) => response.json())
    .then((tasks) => {
      let taskSection = document.getElementById("tasks-container");
      console.log(tasks);
      for (userTask of tasks) {
        taskSection.innerHTML += addTask(userTask.propertyMap);
      }
    });
}

function addTask(task) {
  let string = `<li class="admin-task"><h3> ${task.category} </h3>`;
  string += `<h4> ${task.Owner} </h4></li>`;
  return string;
}