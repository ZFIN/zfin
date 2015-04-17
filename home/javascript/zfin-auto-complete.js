// this attaches onMouseOver handlers to each of the items in the
// auto suggest box (popup panel). changes in the selected item should
// trigger an update of the term info box.
// ony body
jQuery("body").on("mouseover", ".item-selected", function () {
    var selectedTerm = $(".item-selected").text();
    if (selectedTerm.length < 1)
        return;
    var div = $(".termInfoUsed").attr("class");
    //console.log(selectedTerm);
    if ($(".termInfoUsed").length > 0) {
//        console.log(classArray[2]);
        var classArray = div.split(" ");
        var entityName = classArray[2];
        var selectionBox = $("select." + entityName);
        var selectedOption = selectionBox.val();
        //console.log(selectedOption);
        if (selectedOption == null) {
            var element = "." + entityName+"_single";
            selectedOption = $(element).text();
        }
        updateTermInfo(selectedTerm, selectedOption);
    }
});

jQuery("body").on("keydown", "input", function () {
    var selectedTerm = $(".item-selected").text();
    if (selectedTerm.length < 1)
        return;
    var div = $(".termInfoUsed").attr("class");
    //console.log(selectedTerm);
    if ($(".termInfoUsed").length > 0) {
//        console.log(classArray[2]);
        var classArray = div.split(" ");
        var entityName = classArray[2];
        var selectionBox = $("select." + entityName);
        var selectedOption = selectionBox.val();
        //console.log(selectedOption);
        if (selectedOption == null) {
            var element = "." + entityName+"_single";
            selectedOption = $(element).text();
        }
        //console.log(selectedOption);
        updateTermInfo(selectedTerm, selectedOption);
    }
});
