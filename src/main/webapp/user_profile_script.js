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

async function getInfo(keyString) {
    const queryURL = "/tasks/info?key=" + keyString;
    const request = new Request(queryURL, {method: "GET"});
    const response = await fetch(request);
    const info = await response.json();
    console.log(info);
    return info;
}

async function deleteTask(keyString) {
    const info = await getInfo(keyString);
    if (info.status !== "OPEN") {
        window.alert("You can only delete an 'OPEN' task.")
    } else {
        if (confirm("Are you sure that you want to delete the task?")) {
            const queryURL = "/tasks?key=" + keyString;
            const request = new Request(queryURL, {method: "DELETE"});
            const response = await fetch(request);
        }
    }
}

async function editTask(keyString) {
    const info = await getInfo(keyString);
    console.log(info);
    if (info.status !== "OPEN") {
        window.alert("You can only edit an 'OPEN' task.")
    } else {
        document.getElementById("edit-content-input").value = info.detail;
        document.getElementById("edit-point-input").value = info.reward.toString();
        const id_input = document.getElementById("task-id-input");
        id_input.value = info.keyString;
        id_input.readOnly = true;
        id_input.style.display = "none"
        document.getElementById("editTaskModal").style.display = "block";
    }
}

async function completeTask(keyString) {
    const info = await getInfo(keyString);
    if (info.status !== "IN PROGRESS") {
        window.alert("You have already marked the task as complete.");
    } else {
        if (confirm("Are you sure that you have already completed the task?")) {
            const queryURL = "/tasks/info?key=" + keyString + "&status=" + "COMPLETE: AWAIT VERIFICATION";
            const request = new Request(queryURL, {method: "POST"});
            const response = await fetch(request);
        }
    }
}

async function abandonTask(keyString) {
    const info = await getInfo(keyString);
    if (info.status !== "IN PROGRESS") {
        window.alert("You have already marked the task as complete.");
    } else {
        if (confirm("Are you sure that you want to abandon the task?")) {
            const queryURL = "/tasks/info?key=" + keyString + "&status=" + "OPEN";
            const request = new Request(queryURL, {method: "POST"});
            const response = await fetch(request);
        }
    }
}

function showNeedHelp() {
    document.getElementById("need-help").style.display = "table";
    document.getElementById("create").style.display = "block";
    document.getElementById("offer-help").style.display = "none";
    document.getElementById("need-help-button").style.backgroundColor = "#3e8e41";
    document.getElementById("offer-help-button").style.backgroundColor = "#4CAF50";
    displayNeedHelpTasks();
}

function showOfferHelp() {
    document.getElementById("need-help").style.display = "none";
    document.getElementById("create").style.display = "none";
    document.getElementById("offer-help").style.display = "table";
    document.getElementById("need-help-button").style.backgroundColor = "#4CAF50";
    document.getElementById("offer-help-button").style.backgroundColor = "#3e8e41";
    displayOfferHelpTasks();
}

function showModal() {
    var modal = document.getElementById("createTaskModal");
    modal.style.display = "block";
}

function closeModal() {
    var modal = document.getElementById("createTaskModal");
    modal.style.display = "none";
}

function closeEditModal() {
    var modal = document.getElementById("editTaskModal");
    modal.style.display = "none";
}

// If the user clicks outside of the modal, closes the modal directly
window.onclick = function(event) {
    var modal = document.getElementById("createTaskModal");
    if (event.target == modal) {
        modal.style.display = "none";
    }
    var editModal = document.getElementById("editTaskModal");
    if (event.target == editModal) {
        editModal.style.display = "none";
    }
}

async function displayNeedHelpTasks() {
    const queryURL = "mytasks?keyword=Owner";
    const request = new Request(queryURL, {method: "GET"});
    const response = await fetch(request);
    const taskResponse = await response.json();
    const needHelpTable = document.getElementById("need-help");
    needHelpTable.innerHTML = "";
    var headerRow = document.createElement("tr");
    var headers = ["Task Overview", "Helper", "Status", "Edit", "Delete"];
    for (var index = 0; index < headers.length; index++) {
        var th = document.createElement("th");
        th.appendChild(document.createTextNode(headers[index]));
        headerRow.appendChild(th);
    }
    needHelpTable.appendChild(headerRow);
    for (var index = 0; index < taskResponse.length; index++) {
        var tr = document.createElement("tr");
        var task = taskResponse[index];
        var data = [task.detail, task.helper, task.status];
        for (var i = 0; i < data.length; i++) {
            var td = document.createElement("td");
            td.appendChild(document.createTextNode(data[i]));
            tr.appendChild(td);
        }
        const keyStringCopy = task.keyString.slice();
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
        needHelpTable.appendChild(tr);
    }
}

async function displayOfferHelpTasks() {
    const queryURL = "mytasks?keyword=Helper";
    const request = new Request(queryURL, {method: "GET"});
    const response = await fetch(request);
    const taskResponse = await response.json();
    const offerHelpTable = document.getElementById("offer-help");
    offerHelpTable.innerHTML = "";
    var headerRow = document.createElement("tr");
    var headers = ["Task Overview", "Status", "Neighbor", "Reward", "Mark as complete", "Abandon"];
    for (var index = 0; index < headers.length; index++) {
        var th = document.createElement("th");
        th.appendChild(document.createTextNode(headers[index]));
        headerRow.appendChild(th);
    }
    offerHelpTable.appendChild(headerRow);
    for (var index = 0; index < taskResponse.length; index++) {
        var tr = document.createElement("tr");
        var task = taskResponse[index];
        var data = [task.detail, task.status, task.owner, task.reward.toString()];
        for (var i = 0; i < data.length; i++) {
            var td = document.createElement("td");
            td.appendChild(document.createTextNode(data[i]));
            tr.appendChild(td);
        }
        const keyStringCopy = task.keyString.slice();
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
        offerHelpTable.appendChild(tr);
    }
}