/* Calls addClickHandlers once page has loaded */
if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', addClickHandlers)
} else {
    addClickHandlers();
}

/* Function adds all the necessary 'click' event listeners*/
function addClickHandlers() {
    
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
                helpOutButtons[i].addEventListener("click", function(e) {
                    helpOut(e.target);
                });
            }
        }
}

/* Function that visually mimics the functionality of filtering tasks by category */
function filterTasksBy(category) {
    const categoryButtons = document.getElementsByClassName("categories");
    const tasks = document.getElementsByClassName("task");
    const idName = category;

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
            if (overlay.length > 0) overlay[0].style.display = "none";
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
            if (overlay.length > 0) overlay[0].style.display = "none";
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