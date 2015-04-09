/*Javascript code used for update constructs. The general assumption made is that a construct will have 3 cassettes.
 The code can be extended in the future to add more cassettes.
 This code takes care of rendering a construct.It also will save updates if any attribute of a construct is changed.

 */


jQuery(document).ready(function () {
//    Hide second and third cassette sections of form at load time

    jQuery("#newCassette1").hide();
    jQuery("#newCassette2").hide();

    var openP = '(';


    //using prefix to build display name (generate name on leaving the prefix input box)
    jQuery("#prefix").on("focusout", function (e) {
        //construct display name changes as soon as a prefix is entered.

        /* jQuery('#constructName').val(jQuery("#chosenType").val() + jQuery("#prefix").val() + openP);
         jQuery('#cName').val(jQuery('#constructName').val());*/
        generateConstructName();

    });


    jQuery("#chosenType")
        .change(function () {
            /* var constructType = "";
             constructType += jQuery("#chosenType").val() + " ";
             jQuery('#constructName').val(constructType + jQuery("#prefix").val() + openP);
             jQuery('#cName').val(jQuery('#constructName').val());*/
            generateConstructName();
        })
        .change();


    /*The following function is to enable all promoter and coding fields be an autocomplete.
     Since all coding and promoter fields have an id starting with "Cassette", the autocomplete works
     for all promoter and coding in all cassettes
     the autocomplete is on markers attribute to the pub. It also contains foreign species*/

    autocompleteSource = '/action/construct/find-constructMarkers';
    var pubId = jQuery('#constructPublicationZdbID').val();

    jQuery(document).on("focus keyup", "input", function (event) {
        jQuery("input[id^='update_cassette']").autocomplete({
            source: function (request, response) {
                jQuery.ajax({
                    url: autocompleteSource,
                    dataType: "json",
                    data: {
                        term: request.term,
                        exclude: pubId

                    },
                    success: function (data) {
                        response(data);
                    }
                });
            },
            minLength: 2,
            autoFocus: true,
            open: function () {
               // $(this).data("autocomplete").menu.element.width(dropdownWidth);
                jQuery(this).data("autocomplete");
            },

            select: function (event, ui) {

                event.preventDefault();

                jQuery(this).data("selected", true);
                jQuery(this).val(ui.item.value);
                jQuery(this).css({ "background-color": 'white'});


            }, change: function (event, ui) {
                //I am not sure how to do this. If a curator overrides the autocomplete, we need to change
                // the background color of the input box
                if (ui.item == null) {
                    jQuery(this).css({ "background-color": '#F0FFFF'});


                }
            }

        }).bind("keydown", function (e) {

        });


        jQuery("input[id^='update_cassette']").on('focusout', function (event) {

            event.preventDefault();
            generateConstructName();

        });


    });
});

/* This function is for adding Promoters to the first cassette.
 when the user clicks on "+", an empty input field with a select list for Promoters is added to the div.
 if there is a value in between, that value has to eb shifted to the right.*/


//jQuery("button[id^='update_addPromoter']").on("click",function (event) {
    jQuery(document).on("click","button[id^='update_addPromoter']",function (event) {
    event.preventDefault();
    alert("add");
    var divToAttachTo = jQuery(this).closest("div").attr("id");
    var buttonClassName = jQuery(this).attr('class');
    addComponent(divToAttachTo, this.id, "Promoter", buttonClassName);


});


//this function is for deleting the fields.
//jQuery("button[id^='update_delPromoter']").live("click", null, function (event) {
jQuery(document).on("click","button[id^='update_delPromoter']",function (event) {

    event.preventDefault();

    var divToAttachTo = jQuery(this).closest("div").attr("id");
    var buttonClassName = jQuery(this).attr('class');
    deleteComponent(divToAttachTo, this.id, "Promoter", buttonClassName);
    generateConstructName();
});

jQuery(document).on("click","button[id^='update_addCoding']",function (event) {
//jQuery("button[id^='update_addCoding']").live("click", null, function (event) {
    event.preventDefault();
    var divToAttachTo = jQuery(this).closest("div").attr("id");
    var buttonClassName = jQuery(this).attr('class');
    addComponent(divToAttachTo, this.id, "Coding", buttonClassName);

});
jQuery(document).on("click","button[id^='update_delCoding']",function (event) {
//jQuery("button[id^='update_delCoding']").live("click", null, function (event) {
    event.preventDefault();
    var divToAttachTo = jQuery(this).closest("div").attr("id");
    var buttonClassName = jQuery(this).attr('class');
    deleteComponent(divToAttachTo, this.id, "Coding", buttonClassName);
    generateConstructName();

});

/*for (var k = nextCoding; k <= countOfCoding + 2; k++) {
 var j = k + 1;
 var a = jQuery("#Cassette2coding" + k).val();
 codingInputArray.push({id: j, value: a});
 }

 jQuery.each(codingInputArray, function () {
 jQuery("#Cassette2coding" + this.id).val(this.value);
 });
 jQuery("#Cassette2coding" + nextCoding).val("");
 return false;*/
var suffix = 'update_cassette';
var counter = 1;




function cloneMe(a) {


    if(jQuery("#constructCassettes").val()== 2) {

        counter=2;
    }
    if(jQuery("#constructCassettes").val()== 3)  {
        counter=3;
    }

    //I am having a hard time cloning the original form.
    // meaning if the original form has newly created fields, how do I not clone those


    if (counter >= 3) {
        alert("This interface allows for only 3 cassettes");
        return;
    }
    counter++;
    var original = a.parentNode;

    while (original.nodeName.toLowerCase() != 'fieldset') {
        original = original.parentNode;
    }
   var duplicate = original.cloneNode(true);
   // var duplicate=$("#clonable").clone();
    var newDiv = duplicate.getElementsByTagName('div');


    for (var i = 0; i < newDiv.length; i++) {
        var divId = newDiv[i].id;
        if (divId) {
            newDiv[i].id = divId + counter;
            newDiv[i].className = counter;
        }
    }

    var newButton = duplicate.getElementsByTagName('button');
    for (var i = 0; i < newButton.length; i++) {
        var buttonId = newButton[i].id;
        newButton[i].id = buttonId;
        newButton[i].className = counter.toString();
    }
    var newSelectList=duplicate.getElementsByTagName('select');
    //newSelectList.remove();
    /*for (var i = 0; i < newSelectList.length; i++) {
     var selectListId = newButton[i].id;
     newButton[i].id = buttonId;
     newButton[i].className = counter.toString();
     }*/
    var newInput = duplicate.getElementsByTagName('input');
    for (var i = 0; i < newInput.length; i++) {

        var inputId = newInput[i].id;
        if (inputId) {
            if (inputId.indexOf("1") == -1) {

            }
            oldId = inputId.substring(16, inputId.length);

            newInput[i].id = suffix + counter + oldId;

            newInput[i].value = '';
            newInput[i].style.backgroundColor="white" ;
             //changing cloned classnames depending on promoter or coding. the substring coems form that.
            if (newInput[i].className.substring(0, 17) == suffix+1+"P") {
                newInput[i].className = "update_Cassette" + counter+"Promoter";
            }
            else {
                newInput[i].className = "update_Cassette" + counter+"Coding";
            }
        }


    }

    duplicate.className = 'duplicate';
    jQuery('input',jQuery('.duplicate')).each(function(){
        jQuery(this).css({ "background-color": 'white'});


    });


    /* jQuery('button',jQuery("#promoterCassette1")).each(function(){
     if (this.id.indexOf("1")==0) {
     this.remove();
     }
     });
     jQuery('button',jQuery("#codingCassette1")).each(function(){
     if (this.id.indexOf("1")==0) {
     this.remove();
     }
     });
     jQuery('input',jQuery("#promoterCassette1")).each(function(){
     if (this.id.indexOf("Promoter1")==0) {
     this.remove();
     }
     });
     jQuery('button',jQuery("#codingCassette1")).each(function(){
     if (this.id.indexOf("Coding1")==0) {
     this.remove();
     }*/




    //duplicate.classList.remove('promoterControl');
    insertAfter(duplicate, original);
}




function insertAfter(newElement, targetElement) {
    var parent = targetElement.parentNode;

    var child = newElement.parentNode;

    if (parent.lastChild == targetElement) {
        parent.appendChild(newElement);

    } else {


        parent.insertBefore(newElement, targetElement.laterSibling);

    }
    //remove extraneous items  from clone.
    //need to accurately pass className of duplicates here

  /*  var classOfDuplicate=$(counter);
    //how do I use this is a jQuery variable?
   *//* jQuery('select',$("#" +"."+classOfDuplicate)).each(function(){
        alert("jquery variable works");
        jQuery(this).remove();
    });

    jQuery('button',$("#" +"."+classOfDuplicate)).each(function(){
        if (this.id.indexOf("1")==-1) {
            jQuery(this).remove();
        }
    });
    jQuery('input',jQuery("#" +"."+classOfDuplicate)).each(function(){
        if (this.id.indexOf("Promoter1") == -1)  {
            if (this.id.indexOf("Coding1") == -1) {

                jQuery(this).remove();
            }

        }
    });*//*

    jQuery('select',jQuery('.2')).each(function(){

        jQuery(this).remove();
    });

    jQuery('button',jQuery('.2')).each(function(){

        if (this.id.indexOf("1")==-1) {

            jQuery(this).remove();
        }
    });
    jQuery('input',jQuery('.2')).each(function() {

        if (this.id.indexOf("Promoter1") == -1)  {
            if (this.id.indexOf("Coding1") == -1) {

                jQuery(this).remove();
            }

        }
    });

    jQuery('select',jQuery('.3')).each(function(){

        jQuery(this).remove();
    });


    jQuery('button',jQuery('.3')).each(function(){

        if (this.id.indexOf("1")==-1) {

            jQuery(this).remove();
        }
    });
    jQuery('input',jQuery('.3')).each(function() {

        if (this.id.indexOf("Promoter1") == -1)  {
            if (this.id.indexOf("Coding1") == -1) {

                jQuery(this).remove();
            }

        }
    });*/
}

/// Delete nearest parent fieldset
function deleteMe(a) {

    var duplicate = a.parentNode;
    while (duplicate.nodeName.toLowerCase() != 'fieldset') {
        duplicate = duplicate.parentNode;
    }
    var restore_element=duplicate.parentNode.removeChild(duplicate);

    duplicate.parentNode.removeChild(duplicate);
    counter = counter - 1;
    alert(counter);
    generateConstructName();
}

function addComponent(divToAttachTo, thisId, component,buttonClassName) {

    cassetteNumber = parseInt(buttonClassName);

    componentInputArray = [];

    var ul = jQuery("#" + divToAttachTo);


    /*if (component == "Promoter") {
        var nextComponent = parseInt(thisId.subsring(18)) + 1;
    }
    else {
        var nextComponent = parseInt(thisId.substring(16)) + 1;
    }*/
    //get number of input components in current div
    var inputComponents = parseInt(jQuery("#" + divToAttachTo).find("input").size());
    //get id of next component
    var nextComponent = jQuery("#" + divToAttachTo).find("input").size() + 1;


    //get number of select components
    var selectComponents = parseInt(jQuery("#" + divToAttachTo).find("select").size());
    //get total number of components to iterate through
    var countOfComponents = selectComponents + inputComponents;

    var componentInputId = "update_cassette" + cassetteNumber + component + nextComponent;

    var componentInputClass = "update_cassette"+ cassetteNumber+component ;
    var btnAddId = "update_add" + component + nextComponent;

    var btnDelId = "update_del" + component + nextComponent;
    var selectListID = "update_selectList" + component + nextComponent;

    var selectListComponent = "<select   id='" + selectListID+"' class='" + componentInputClass+"'>";
    selectListComponent += "<option>" + "-" + "</option>";
    selectListComponent += "<option>" + "," + "</option>";
    selectListComponent += "<option>" + "." + "</option>";
    selectListComponent += "<option>" + "" + "</option>";

    selectListComponent += "</select>";
    var li = jQuery(selectListComponent + "<input  size=10  class='"+componentInputClass +"' name='" + componentInputId + "'+ id='" + componentInputId + "' />" + "<button class='"+cassetteNumber+"' id='" + btnAddId + "' >+</button>" + "<button class='"+cassetteNumber+"' id='" + btnDelId + "' >-</button>");
    li.appendTo(ul);
    /*for (var k = nextComponent; k <= countOfComponents + 2; k++) {
        var j = k + 1;  //j is incremented to avoid the select box
        var a = jQuery("#"+"update_cassette"+cassetteNumber+component + k).val();

        componentInputArray.push({id: j, value: a});
    }

    jQuery.each(componentInputArray, function () {
        jQuery("#"+"update_cassette"+cassetteNumber+component + this.id).val(this.value);
    });
    jQuery("#"+"update_cassette"+cassetteNumber+component + nextComponent).val("");*/
    return false;
}

function deleteComponent(divToAttachTo, thisId, component,buttonClassName){

    if (component=="Promoter"){
        var componentNumber = parseInt(thisId.substring(18));
    }
    else{
        var componentNumber = parseInt(thisId.substring(16));
    }
    var i = jQuery("#" + divToAttachTo).find("input").size();

    var ul = jQuery("#" + divToAttachTo);
    jQuery(jQuery("#"+divToAttachTo)).each(function(){
        alert(jQuery(this).id);
        jQuery("#" + "update_add" + component + componentNumber).remove();
        jQuery("#" + "update_del" + component + componentNumber).remove();

        jQuery("#" + "update_cassette" + buttonClassName + component + componentNumber).remove();
        alert("update_selectList" + component + componentNumber);
        jQuery("#" + "update_selectList" + component + componentNumber).remove();
    });
    //after deleting renumber input, select box and button ids.

    componentCount=1; //counter to increment for each input box
    jQuery('input',jQuery("#"+divToAttachTo)).each(function(){

        jQuery(this).attr("id","update_cassette"+buttonClassName+component+componentCount);
        componentCount++;
    });

    addButtonCount=1;
    delButtonCount=1//counter to increment for each add and delete button
    jQuery('button',$("#"+divToAttachTo)).each(function(){

        if(this.id.substring(0,10)=="update_add"){

            jQuery(this).attr("id","update_add"+component+addButtonCount);
            addButtonCount++;
        }
        else{

            jQuery(this).attr("id","update_del"+component+delButtonCount);
            delButtonCount++;
        }

    });

    selectListCount=2; //counter to keep track of select list for Promoter and coding. starts with 2 since the listbox starts only with the second component
    jQuery('select',$("#"+divToAttachTo)).each(function(){
        jQuery(this).attr("id","update_selectList"+component+selectListCount);
        selectListCount++;
    });


}





//code to add a cassette
function generateConstructName() {


    jQuery("#submitConstruct").attr("enabled", "enabled");
    jQuery('#relText').text('');
    var partCodingString = "";
    var partPromoterString = "";
    var cass2CodingString = "";
    var cass2PromoterString = "";
    var cass3CodingString = "";
    var cass3PromoterString = "";
    var constructDisplayName = ""
    var constructDisplayName1 = ""
    var constructDisplayName2 = ""
    var consStoreName = ""
    var consStoreName1 = ""
    var storedPromString = "";
    var storedCodingString = "";
    var storedPromString2 = "";
    var storedCodingString2 = "";
    var consStoreName2 = ""
    var storedPromString3 = "";
    var storedCodingString3 = "";

    var prefixStr = "";
    var closeP = ")";


    jQuery.each(jQuery('.update_cassette1Promoter'), function (index) {

       // alert(jQuery(this).val());
        partPromoterString += jQuery(this).val();

        storedPromString = storedPromString + "Prom" + jQuery(this).val() + "#";
    });

    jQuery.each(jQuery('.update_cassette1Coding'), function (index) {
        partCodingString += jQuery(this).val();
        storedCodingString = storedCodingString + "Coding" + jQuery(this).val() + "#";
    });

    if (jQuery("#prefix").val() == "") {
        prefixStr = "";
    }
    else {
        prefixStr = "Prefix" + jQuery("#prefix").val() + "#";
    }

    if (partCodingString != "") {
        if (partPromoterString != "") {
            // jQuery("#constructName").attr('value',jQuery('#chosenType').find(":selected").text() + jQuery("#prefix").val()+"(" +partPromoterString+":"+partCodingString);
            constructDisplayName = jQuery('#chosenType').find(":selected").text() + jQuery("#prefix").val() + "(" + partPromoterString + ":" + partCodingString;
            consStoreName = jQuery('#chosenType').find(":selected").text() + "#" + prefixStr + "(" + "#" + storedPromString + "#" + "Prom:" + "#" + storedCodingString;
            jQuery("#constructStoredName").val(consStoreName);
            jQuery("#constructDisplayName").val(constructDisplayName);
            jQuery("#constructName").val(constructDisplayName);
        } else {
            //      jQuery("#constructName").attr('value',jQuery('#chosenType').find(":selected").text() + jQuery("#prefix").val()+"(" +partCodingString);
            constructDisplayName = jQuery('#chosenType').find(":selected").text() + jQuery("#prefix").val() + "(" + partCodingString;
            consStoreName = jQuery('#chosenType').find(":selected").text() + "#" + prefixStr + "(" + "#" + storedCodingString;
            jQuery("#constructName").val(constructDisplayName);
            jQuery("#constructDisplayName").val(constructDisplayName);
            jQuery("#constructStoredName").val(consStoreName);
        }
    }
    else {
        //jQuery("#constructName").attr('value',jQuery('#chosenType').find(":selected").text() + jQuery("#prefix").val()+"(" +partPromoterString);
        constructDisplayName = jQuery('#chosenType').find(":selected").text() + jQuery("#prefix").val() + "(" + partPromoterString;
        consStoreName = jQuery('#chosenType').find(":selected").text() + "#" + prefixStr + "(" + "#" + storedPromString;
        jQuery("#constructDisplayName").val(constructDisplayName);
        jQuery("#constructName").val(constructDisplayName);
        jQuery("#constructStoredName").val(consStoreName);
    }

    //for cassette 2

    jQuery.each(jQuery('.update_cassette2promoter'), function (index) {

        cass2PromoterString += jQuery(this).val();


            if (jQuery(this).val()!='') {
        storedPromString2 = storedPromString2 + "Prom" + jQuery(this).val() + "#";
           }


    });

    jQuery.each(jQuery('.update_cassette2Coding'), function (index) {
        cass2CodingString += jQuery(this).val();

          if (jQuery(this).val()!='') {
        storedCodingString2 = storedCodingString2 + "Coding" + jQuery(this).val() + "#";
         }

    });

    if (cass2CodingString != "") {
        if (cass2PromoterString != "") {
            // jQuery("#constructName").attr('value',jQuery('#chosenType').find(":selected").text() + jQuery("#prefix").val()+"(" +partPromoterString+":"+partCodingString);
            constructDisplayName1 = cass2PromoterString + ":" + cass2CodingString;


            consStoreName1 = storedPromString2 + "#" + "Prom:" + "#" + storedCodingString2;
            if (constructDisplayName1.length == 0) {

                jQuery("#constructName").val(constructDisplayName);
                jQuery("#constructDisplayName").val(constructDisplayName);

            }
            else {

                jQuery("#constructName").val(constructDisplayName + "," + constructDisplayName1);
                jQuery("#constructDisplayName").val(constructDisplayName + "," + constructDisplayName1);
                consStoreName = consStoreName + "#" + "%" + "#" + "Cassette," + "#" + consStoreName1;
                jQuery("#constructStoredName").val(consStoreName);

            }
        } else {

            //      jQuery("#constructName").attr('value',jQuery('#chosenType').find(":selected").text() + jQuery("#prefix").val()+"(" +partCodingString);
            constructDisplayName1 = cass2CodingString;
            if (constructDisplayName1.length == 0) {

                jQuery("#constructName").val(constructDisplayName);
                jQuery("#constructDisplayName").val(constructDisplayName);

            }
            else {

                jQuery("#constructName").val(constructDisplayName + "," + constructDisplayName1);
                jQuery("#constructdisplayName").val(constructDisplayName + "," + constructDisplayName1);
                consStoreName = consStoreName + "%" + "#" + "Cassette," + "#" + consStoreName1;
                jQuery("#constructStoredName").val(consStoreName);
            }
        }
    }
    else {
        //jQuery("#constructName").attr('value',jQuery('#chosenType').find(":selected").text() + jQuery("#prefix").val()+"(" +partPromoterString);

        constructDisplayName1 = cass2PromoterString;
        consStoreName1 = storedPromString2;

        if (constructDisplayName1.length == 0) {

            jQuery("#constructName").val(constructDisplayName);
            jQuery("#constructDisplayName").val(constructDisplayName);
        }
        else {

            jQuery("#constructName").val(constructDisplayName + "," + constructDisplayName1);
            jQuery("#constructDisplayName").val(constructDisplayName + "," + constructDisplayName1);
            consStoreName = consStoreName + "%" + "Cassette," + "#" + consStoreName1;
            jQuery("#constructStoredName").val(consStoreName);

        }

    }

    //for cassette 2
    jQuery.each(jQuery('.update_cassette3promoter'), function () {

        cass3PromoterString += jQuery(this).val();
        if (jQuery(this).val()!='') {
            storedPromString3 = storedPromString3 + "Prom" + jQuery(this).val() + "#";

        }
    });
    jQuery.each(jQuery('.update_cassette3coding'), function () {
        cass3CodingString += jQuery(this).val();
        if (jQuery(this).val()!='') {
            storedCodingString3 = storedCodingString3 + "Coding" + jQuery(this).val() + "#";
        }
    });
    if (cass3CodingString != "") {

        if (cass3PromoterString != "") {
            // jQuery("#constructName").attr('value',jQuery('#chosenType').find(":selected").text() + jQuery("#prefix").val()+"(" +partPromoterString+":"+partCodingString);
            constructDisplayName2 = cass3PromoterString + ":" + cass3CodingString;
            consStoreName2 = storedPromString3 + "#" + "Prom:" + "#" + storedCodingString3;
            jQuery("#constructName").val(constructDisplayName + "," + constructDisplayName1 + "," + constructDisplayName2);
            jQuery("#constructDisplayName").val(constructDisplayName + "," + constructDisplayName1 + "," + constructDisplayName2);
        } else {
            constructDisplayName2 = cass3CodingString;
            consStoreName = consStoreName + "#" + "%" + "#" + "Cassette," + "#" + consStoreName2;
            jQuery("#constructName").val(constructDisplayName + "," + constructDisplayName1 + "," + constructDisplayName2);
            jQuery("#constructDisplayName").val(constructDisplayName + "," + constructDisplayName1 + "," + constructDisplayName2);
            jQuery("#constructStoredName").val(consStoreName);
        }
    }
    else {

        constructDisplayName2 = cass3PromoterString;
        jQuery("#constructName").val(constructDisplayName + "," + constructDisplayName1 + "," + constructDisplayName2);
        jQuery("#constructDisplayName").val(constructDisplayName + "," + constructDisplayName1 + "," + constructDisplayName2);
        consStoreName = consStoreName + "%" + "#" + "Cassette," + "#" + consStoreName2;
        jQuery("#constructStoredName").val(consStoreName);
    }
    if (constructDisplayName2.length == 0) {
        if (constructDisplayName1.length == 0) {
            jQuery("#constructName").val(constructDisplayName);
        }
        else {
            jQuery("#constructName").val(constructDisplayName + "," + constructDisplayName1);
            consStoreName = consStoreName + "%" + "Cassette," + "#" + consStoreName1;

            jQuery("#constructStoredName").val(consStoreName);
        }
    }
    else {
        consStoreName = consStoreName + "%" + "Cassette," + "#" + consStoreName2;

        jQuery("#constructStoredName").val(consStoreName);
    }
    jQuery("#constructName").css({ 'color': 'red'});
    finalConName = jQuery("#constructName").val();
    finalStoredName = jQuery("#constructStoredName").val();

    constructLength = finalConName.length;
    if (finalConName.indexOf(closeP) != constructLength) {
        jQuery('#constructDisplayName').val(finalConName + closeP);
    }
    else {
        jQuery('#constructDisplayName').val(finalConName);
    }
    finalDisplayName = jQuery("#constructDisplayName").val();
    jQuery('#constructDisplayName').val(finalDisplayName);
    jQuery('#constructName').val(finalDisplayName);
    if (finalStoredName.indexOf(closeP) == -1) {
        jQuery('#constructStoredName').val(finalStoredName + "#" + closeP);

    }
    else {
        jQuery('#constructStoredName').val(finalStoredName);

    }

}

function updateConstruct() {
//   document.getElementById("thisform").submit();

        var param = jQuery("#thisform").serialize();


        jQuery.ajax({
            url: '/action/construct/construct-run-update/',
            type: 'POST',
            data: param,
            success: function (response) {
                jQuery("#update-construct-error").html(response);
                jQuery("#update-construct-error").show();


            },
            error: function (data) {
                alert('There was a problem with your request: ' + data);
                jQuery("#update-construct-error").html(response);
                jQuery("#update-construct-error").show();
            }

        });
}
function addAlias() {
    var alias = jQuery("#constructAlias").val();
    var constructID = jQuery('#constructEdit').val();
    var pubID = jQuery('#constructPublicationZdbID').val();


    jQuery.ajax({
        url: "/action/construct/add-alias/" + constructID
            + "/alias/" + alias + "/publication/" + pubID,
        type: 'POST',
        //data: param,
        success: function (response) {
            getConstructDetails(constructID);
            jQuery("#constructAlias").val("");


        },
        error: function (data) {
            alert('There was a problem with your request: ' + data);
        }

    });


}

function updatePublicNotes() {
    var constructComments = jQuery("#constructComments").val();
    var constructID = jQuery('#constructEdit').val();
    


    jQuery.ajax({
        url: "/action/construct/update-comments/" + constructID
            + "/constructComments/" + constructComments,
        type: 'POST',
        //data: param,
        success: function (response) {
          
            getConstructDetails(constructID);


        },
        error: function (data) {
            alert('There was a problem with your request: ' + data);
        }

    });


}
function addNotes() {
    var notes = jQuery("#curatorNotes").val();
    var constructID = jQuery('#constructEdit').val();
    var pubID = jQuery('#constructPublicationZdbID').val();


    jQuery.ajax({
        url: "/action/construct/add-notes/" + constructID
            + "/notes/" + notes + "/publication/" + pubID,
        type: 'POST',
        //data: param,
        success: function (response) {
            getConstructDetails(constructID);
            jQuery("#curatorNotes").val("");


        },
        error: function (data) {
            alert('There was a problem with your request: ' + data);
        }

    });


}
function deleteAlias(aliasID) {

    var constructID = jQuery('#constructEdit').val();

    jQuery.ajax(
        {
            url:  "/action/construct/delete-alias/" + constructID
                + "/aliasID/" + aliasID ,
            type: "DELETE",
            success: function (data) {
                getConstructDetails(constructID);
//                                            jQuery('#member-delete-button-'+personZdbID+"'").html('') ;

            },
            error: function (data) {
                alert('There was a problem with your request: ' + data);
            }
        }
    );
}

function deleteNote(noteID) {

    var constructID = jQuery('#constructEdit').val();

    jQuery.ajax(
        {
            url:  "/action/construct/delete-note/" + constructID
                + "/noteID/" + noteID ,
            type: "DELETE",
            success: function (data) {
                getConstructDetails(constructID);
//                                            jQuery('#member-delete-button-'+personZdbID+"'").html('') ;

            },
            error: function (data) {
                alert('There was a problem with your request: ' + data);
            }
        }
    );
}

/*function getDetails(construct){
    jQuery.ajax({
        url: '/action/construct/construct-do-update/' + construct,
=======
    $('a[href^="http://"]').attr('target', '_blank');

    jQuery.ajax({
        url: '/action/construct/construct-do-update/' + test,
>>>>>>> .r32325
//               data: ({constructID:test}),
<<<<<<< .mine
        type: "GET",
        dataType: "JSON",
        success: function(data){
            jQuery.each(data, function(key, value) {
                alert(value);
            });
        }
    });
}*/
function resetFields(){



    jQuery("#prefix").val("");
    jQuery("#constructName").val("");
    jQuery("#cName").val("");
    jQuery("#constructDisplayName").val("");
    jQuery("#constructStoredName").val("");
    jQuery("#constructAlias").val("");
    jQuery("#constructComments").val("");
    jQuery("#constructCuratorNote").val("");
    jQuery(".duplicate").remove();
    jQuery("#cName").attr("disabled", "disabled");
    jQuery("#constructSynonyms").empty();
    jQuery("#constructNotes").empty();
    jQuery("#update-construct-error").hide();

  /*  jQuery('select',$("#update_promoterCassette1")).each(function(){
        this.remove();
    });
    jQuery('select',$("#update_codingCassette1")).each(function(){
        this.remove();
    });
    jQuery('button',$("#update_promoterCassette1")).each(function(){
        if (this.id.indexOf("1")==-1) {
            this.remove();
        }
    });
    jQuery('button',$("#update_codingCassette1")).each(function(){
        if (this.id.indexOf("1")==-1) {
            this.remove();
        }
    });
    jQuery('input',$("#update_promoterCassette1")).each(function(){
        alert(this.id);
        if (this.id.indexOf("Promoter1")==-1) {
            this.remove();
        }
        else {

            $(this).attr("value","");
            $(this).css({"background-color": 'white'});
        }

    });
    jQuery('input',$("#update_codingCassette1")).each(function(){
        if (this.id.indexOf("Coding1")==-1) {
            this.remove();
        }
        else {

            $(this).attr("value","");
            $(this).css({"background-color": 'white'});
        }
        counter=1;//reset cassette counter to 1
    });*/

}
function getConstructDetails(constructID) {

    //this.form.reset();
    //var child = document.getElementById("promoterTest1");
    /*  jQuery("#promoterTest1").empty();
     jQuery("#promoterTest1").html('');
     jQuery("#codingTest1").empty();
     jQuery("#codingTest1").html('');*/
    if (constructID == "") {
        alert("Please select a construct");
        return;
    }

    /*jQuery("#newCassette1").hide();

    jQuery("#newCassette2").hide();*/

    jQuery("#constructSynonyms").empty();
    jQuery("#constructNotes").empty();
    jQuery("#update-construct-error").hide();



    jQuery.ajax({
        url: '/action/construct/construct-do-update/' + constructID,
//               data: ({constructID:test}),
        type: "GET",
        success: function (data) {
            var promoterCount = 1;
            var codingCount = 1;
            var cvs = ["-", ",", "."];
            var codingItems = "";
            for (l = 0; l < 4; l++) {
                window['items' + l] = "";
                window['codingItems' + l] = "";
            }

            jQuery.each(data, function (i, constructComponentPresentation) {

                if (constructComponentPresentation.constructComponentName != "") {
                    if (constructComponentPresentation.constructComponentName in {Tg: 1, Pt: 1, Et: 1, Gt: 1}) {
                        jQuery("#chosenType").val(constructComponentPresentation.constructComponentName);
                    }
                    if (constructComponentPresentation.constructComponentCategory == "prefix component") {

                        jQuery("#prefix").val(constructComponentPresentation.constructComponentName);
                    }
                    jQuery("#constructCassettes").val(constructComponentPresentation.maxCassettes);
                    if (constructComponentPresentation.maxCassettes == 2) {

                        jQuery("#newCassette1").show();
                    }
                    else {
                        alert("number of casettes" + constructComponentPresentation.maxCassettes);
                        jQuery("#newCassette1").hide();

                        /*Query("#newCassette1").val('');
                         jQuery("#newCassette1").html('');
                         jQuery("#newCassette1").empty();*/
                        jQuery("#newCassette2").hide();
                    }
                    if (constructComponentPresentation.maxCassettes == 3) {
                        jQuery("#newCassette1").show();
                        jQuery("#newCassette2").show();
                    }
                    else {

                        jQuery("#newCassette2").hide();
                        jQuery("#newCassette1").show();
                        /*                            jQuery("#newCassette2").val('');
                         jQuery("#newCassette2").html('');
                         jQuery("#newCassette2").empty();*/
                    }
                    if (constructComponentPresentation.constructComponentCategory != "construct wrapper component") {


                        var cassetteNumber = constructComponentPresentation.constructCassetteNumber;


                        var j = constructComponentPresentation.constructComponentOrder
                        if (constructComponentPresentation.constructComponentName != ":") {
                            if (constructComponentPresentation.constructComponentCategory == "promoter component") {
                               // window['items' + cassetteNumber] += "<span class='promoterControl1'>";

                                //window['items'+cassetteNumber] += "<input class='"+"promoterCassette"+cassetteNumber+ " name=promoter" + promoterCount + " id=promoter" + j + " size=10" + " value=" + constructComponentPresentation.constructComponentName + ">";
                                window['items' + cassetteNumber] += "<input  name=promoter" + promoterCount + " class=update_cassette" + cassetteNumber+"Promoter" + "  id=update_cassette" + cassetteNumber + "Promoter" + promoterCount + " size=10" + " value=" + constructComponentPresentation.constructComponentName + ">";
                                window['items' + cassetteNumber] += "<button   class=" + cassetteNumber + " id='" + "update_addPromoter" + promoterCount + "' >+</button>" + "<button class=" + cassetteNumber + " id='" + "update_delPromoter" + promoterCount + "' >-</button>";
                                window['items' + cassetteNumber] += "</span>";
                              /*  if (cvs.indexOf(constructComponentPresentation.constructComponentName) == -1) {





                                }*/
                               /* else {
                                    var selectListPromoter = "<select class=promoter" + cassetteNumber + " id=selectListPromoter" + promoterCount + ">";


                                    selectListPromoter += "<option>" + "-" + "</option>";
                                    selectListPromoter += "<option>" + "," + "</option>";
                                    selectListPromoter += "<option>" + "." + "</option>";
                                    selectListPromoter += "<option>" + "" + "</option>";

                                    selectListPromoter += "</select>";
                                    window['items' + cassetteNumber] += selectListPromoter;
                                    var selOption = constructComponentPresentation.constructComponentName;
                                    //jQuery("#selectListPromoter").find( 'option[value="'+selOption+'"]' ).prop( "selected", true );
                                }*/
                                promoterCount++;
                            }
                        }
                      /*  jQuery('#promoterTest' + cassetteNumber).empty();
                        jQuery('#promoterTest' + cassetteNumber).html('');*/
                        //jQuery('#update_promoterCassette' + cassetteNumber).html(window['items' + cassetteNumber]);
                        jQuery('#update_promoterCassette' + cassetteNumber).html(window['items' + cassetteNumber]);


                     //   jQuery("#selectListPromoter").find('option[value="' + selOption + '"]').prop("selected", true);


                        if (constructComponentPresentation.constructComponentCategory == "coding sequence component") {

                          //  if (cvs.indexOf(constructComponentPresentation.constructComponentName) == -1) {
                              //  window['codingItems' + cassetteNumber] += "<span class='codingControl1'>";
                                window['codingItems' + cassetteNumber] += "<input class=update_cassette" + cassetteNumber+"Coding" + " name=coding" + codingCount + " id=update_cassette" + cassetteNumber + "Coding" +codingCount + " size=10" + " value=" + constructComponentPresentation.constructComponentName + ">";
                                window['codingItems' + cassetteNumber] += "<button class=" + cassetteNumber + " id='" + "update_addCoding" + codingCount + "' >+</button>" + "<button class=" + cassetteNumber + " id='" + "update_delCoding" + codingCount + "' >-</button>";


                           // }
                           /* else {
                                var selectListCoding = "<select class=coding" + cassetteNumber + "  id=selectListCoding name=coding" + codingCount + ">";


                                selectListCoding += "<option>" + "-" + "</option>";
                                selectListCoding += "<option>" + "," + "</option>";
                                selectListCoding += "<option>" + "." + "</option>";
                                selectListCoding += "<option>" + "" + "</option>";

                                selectListCoding += "</select>";
                                window['codingItems' + cassetteNumber] += selectListCoding;
                                jQuery('#selectListCoding').val(constructComponentPresentation.constructComponentName);
                            }*/
                            codingCount++;
                        }
            /*            jQuery('#codingTest' + cassetteNumber).empty();
                        jQuery('#codingTest' + cassetteNumber).html('');*/
                        //actually attachhing and rendering the html to the div for the cassette.
                        jQuery('#update_codingCassette' + cassetteNumber).html(window['codingItems' + cassetteNumber]);
                        //jQuery('#constructDisplayName').val(constructComponentPresentation.constructDisplayName);
                      jQuery('#constructDisplayName').val(constructComponentPresentation.constructDisplayName);
                        jQuery('#constructName').val(constructComponentPresentation.constructDisplayName);
                        jQuery('#constructComments').val(constructComponentPresentation.constructComments);

                        if (jQuery("#synonym").length==0) {
                            jQuery.each(constructComponentPresentation.constructAliases, function () {

                                jQuery('#constructSynonyms').append("<div id='synonym'>"
                                    + "<img  src='/images/delete-button.png' "
                                    + "  title='Delete Alias.' onclick=deleteAlias('" + this.aliasZdbID + "');> "
                                    + this.alias
                                    + "</div>");

                            });
                        }

                        if (jQuery("#privateNote").length==0) {
                            jQuery.each(constructComponentPresentation.constructCuratorNotes, function () {

                                jQuery('#constructNotes').append("<div id='privateNote'>"
                                    + "<img  src='/images/delete-button.png' "
                                    + "  title='Delete Note.' onclick=deleteNote('" + this.zdbID + "');> "
                                    + this.noteData
                                                                                        + "</div>");

                            });
                        }
                        /*for (galias in constructComponentPresentation.constructAliases) {
                            alert(galias.name);
                            jQuery('#constructSynonyms').append(
                                + "<img class='clickable' src='/images/delete-button.png' "
                                + "  title='Remove person from lab.' onclick=deleteAlias('" + data[constructAliases].aliaszdbID + "');> "
                                +  data[constructAliases].alias + "</a>"
                                + " "
                                + "");
                        }*/
                      //  jQuery('#constructSynonyms').html(constructComponentPresentation.constructAlias);
                       // jQuery('#constructNotes').html(constructComponentPresentation.curatorNotes);


                    }
                }


            });
        }

    });


}

