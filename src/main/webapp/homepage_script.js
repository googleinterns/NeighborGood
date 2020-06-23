function filterBy(category) {
    const categoryButtons = document.getElementsByClassName("categories");
    console.log(categoryButtons);
    const tasks = document.getElementsByClassName("task");

    for (let i = 0; i < categoryButtons.length; i++){
        console.log(categoryButtons[i]);
        categoryButtons[i].style.backgroundColor = "transparent";
    }

	if (category == "all") {
        for (let i = 0; i < tasks.length; i++) {
            tasks[i].style.display = "block";
        }
        document.getElementById("category-all").style.backgroundColor = "lightgray";
    }
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