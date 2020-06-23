
/** Function that visually mimics the functionality of filtering tasks by category */
function filterBy(category) {
    const categoryButtons = document.getElementsByClassName("categories");
    const tasks = document.getElementsByClassName("task");

	// Unhighlights all category buttons
    for (let i = 0; i < categoryButtons.length; i++){
        categoryButtons[i].style.backgroundColor = "transparent";
    }
	
    // Shows all tasks and highlights the 'ALL' button
	if (category == "all") {
        for (let i = 0; i < tasks.length; i++) {
            tasks[i].style.display = "block";
        }
        document.getElementById("category-all").style.backgroundColor = "lightgray";
    }
    
    // Hides all tasks that don't match the category, shows all
    // of those that do and highlights appropriate category button
	else {
        let idName = "category-" + category;
        document.getElementById(idName).style.backgroundColor = "lightgray";
        for (let i = 0; i < tasks.length; i++) {
            if (tasks[i].classList.contains(category)) {
                tasks[i].style.display = "block";
            } else {
                tasks[i].style.display = "none";
            }
        }
    }
}