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

function deleteRow(row) {
    if (confirm("Are you sure that you want to delete this already published task?")) {
        var rowIndex = row.parentNode.parentNode.rowIndex;
        document.getElementById("need-help").deleteRow(rowIndex);
    }
}

function completeTask(row) {
    if (confirm("Are you sure that you have already completed the task?")) {
        var rowIndex = row.parentNode.parentNode.rowIndex;
        document.getElementById("offer-help").deleteRow(rowIndex);
    }
}

function showNeedHelp() {
    document.getElementById("need-help").style.display = "table";
    document.getElementById("create").style.display = "block";
    document.getElementById("offer-help").style.display = "none";
    document.getElementById("need-help-button").style.backgroundColor = "#3e8e41";
    document.getElementById("offer-help-button").style.backgroundColor = "#4CAF50";
}

function showOfferHelp() {
    document.getElementById("need-help").style.display = "none";
    document.getElementById("create").style.display = "none";
    document.getElementById("offer-help").style.display = "table";
    document.getElementById("need-help-button").style.backgroundColor = "#4CAF50";
    document.getElementById("offer-help-button").style.backgroundColor = "#3e8e41";
}

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