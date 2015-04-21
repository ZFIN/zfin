// this attaches onMouseOver handlers to each of the items in the
// auto suggest box (popup panel). changes in the selected item should
// trigger an update of the term info box.
// ony body
var showTermInfo = function () {
    var selectedTerm = $(".item-selected").text();
    if (selectedTerm.length > 0) {
        var div = $(".termInfoUsed").attr("class");
        if ($(".termInfoUsed").length > 0) {
            var classArray = div.split(" ");
            var entityName = classArray[2];
            var selectionBox = $("select." + entityName);
            var selectedOption = selectionBox.val();
            if (selectedOption == null) {
                var element = "." + entityName + "_single";
                selectedOption = $(element).text();
            }
            updateTermInfo(selectedTerm, selectedOption);
        }
    }
};

jQuery("body").on("mouseover", ".item-selected", showTermInfo)
    .on("keydown", "input", showTermInfo);

