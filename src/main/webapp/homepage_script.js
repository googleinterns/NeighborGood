
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

/** Temporary logout function for prototype */
function logOut() {
    alert("You have been logged out");
    let loginMessage = document.getElementById("login-message");
    loginMessage.innerHTML = "<a onclick='logIn()'>Login to help a neighbor!</a>";
    document.getElementById("user-link").style.display = "none";
    document.getElementById("categories").style.width = "100%";
    document.getElementById("add-task").style.display = "none";
    const helpButtons = document.getElementsByClassName("help-button");
    for (let i = 0; i < helpButtons.length; i++) {
        helpButtons[i].style.display = "none";
    }
}

/** Temporary login function for prototype */
function logIn(){
    alert("You are now logged in");
    let loginMessage = document.getElementById("login-message");
    loginMessage.innerHTML = "User | <a onclick='logOut()'>Logout</a>";
    document.getElementById("user-link").style.display = "block";
    document.getElementById("categories").style.width = "90%";
    document.getElementById("add-task").style.display = "block";
    const helpButtons = document.getElementsByClassName("help-button");
    for (let i = 0; i < helpButtons.length; i++) {
        helpButtons[i].style.display = "block";
    }
}

/** Leonard's implementation of the Add Task modal */
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