/**
 Javascript file to accompany construct Adding.
 */

//anatomy of a construct: Construct TypePrefix(promoter:coding)

//Can I use same variable and div names for Add and update?

//variables declared to hold select list for each cassette.








var openP='(';

jQuery(document).ready(function () {

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
        //$("input[id^='cassette']").autocompletify('/action/construct/find-constructMarkers?term=%QUERY');
       jQuery("input[id^='cassette']").autocomplete({
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
                jQuery(this).data("autocomplete").menu.element.width(dropdownWidth);
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

                        jQuery("input[id^='cassette']").on('focusout', function (event) {

            event.preventDefault();
            generateConstructName();

        });


    })


});

    /* This function is for adding Promoters to the first cassette.
     when the user clicks on "+", an empty input field with a select list for Promoters is added to the div.
     if there is a value in between, that value has to eb shifted to the right.*/


//    jQuery("button[id^='addPromoter']").live("click", function (event) {
        jQuery(document).on("click","button[id^='addPromoter']",function (event){
        event.preventDefault();
        var divToAttachTo = jQuery(this).closest("div").attr("id");
        var buttonClassName = jQuery(this).attr('class');
        addComponent(divToAttachTo, this.id, "Promoter", buttonClassName);
            generateConstructName();

    });

jQuery(document).on("change","select[id^='selectList']",function (event){

    event.preventDefault();

    generateConstructName();

});


//this function is for deleting the fields.
jQuery(document).on("click","button[id^='delPromoter']",function (event){


        event.preventDefault();
        var divToAttachTo = jQuery(this).closest("div").attr("id");
        var buttonClassName = jQuery(this).attr('class');
        deleteComponent(divToAttachTo, this.id, "Promoter", buttonClassName);
        generateConstructName();
    });

jQuery(document).on("click","button[id^='addCoding']",function (event){


        event.preventDefault();
    var divToAttachTo = jQuery(this).closest("div").attr("id");
    var buttonClassName = jQuery(this).attr('class');
        addComponent(divToAttachTo, this.id, "Coding", buttonClassName);
    generateConstructName();

    });

jQuery(document).on("click","button[id^='delCoding']",function (event){
        event.preventDefault();
    var divToAttachTo = jQuery(this).closest("div").attr("id");
    var buttonClassName = jQuery(this).attr('class');
        deleteComponent(divToAttachTo, this.id, "Coding", buttonClassName);
        generateConstructName();

    });


    var suffix = 'cassette';
    var counter = 1;

    function cloneMe(a) {
        //I am having a hard time cloning the original form.
        // meaning if the original form has newly created fields, how do I not clone those

        counter++;
       // alert(counter);
        var num = jQuery('.clonable').length;
        //alert(num);
        var duplicate = jQuery('#cassette' + num).clone().attr('id', 'cassette' + counter);
        if (counter > 3) {
            alert("This interface allows for only 3 cassettes");
            return;
        }
        var original = a.parentNode;

        while (original.nodeName.toLowerCase() != 'fieldset') {
            original = original.parentNode;
        }
        var duplicate = original.cloneNode(true);

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
        /*var newSelectList = duplicate.getElementsByTagName('select');
        if (newSelectList.length > 0) {

        newSelectList.remove();
    }*/

        var newInput = duplicate.getElementsByTagName('input');
        for (var i = 0; i < newInput.length; i++) {

            var inputId = newInput[i].id;
            if (inputId) {
                if (inputId.indexOf("1") == -1) {

                }
                oldId = inputId.substring(9, inputId.length);

                newInput[i].id = suffix + counter + oldId;
                newInput[i].value = '';
                newInput[i].style.backgroundColor = "white";

                if (newInput[i].className.substring(0, 10) == suffix + 1 + "P") {
                    newInput[i].className = "Cassette" + counter + "Promoter";
                }
                else {
                    newInput[i].className = "Cassette" + counter + "Coding";
                }
            }


        }

        duplicate.className = 'duplicate';
        jQuery('input', jQuery('.duplicate')).each(function () {
            jQuery(this).css({ "background-color": 'white'});

        });


        jQuery('button', jQuery("#promoterCassette1")).each(function () {
            if (this.id.indexOf("1") == 0) {
                this.remove();
            }
        });
        jQuery('button', jQuery("#codingCassette1")).each(function () {
            if (this.id.indexOf("1") == 0) {
                this.remove();
            }
        });
        jQuery('input', jQuery("#promoterCassette1")).each(function () {
            if (this.id.indexOf("Promoter1") == 0) {
                this.remove();
            }
        });
        jQuery('button', jQuery("#codingCassette1")).each(function () {
            if (this.id.indexOf("Coding1") == 0) {
                this.remove();
            }
        });


        //duplicate.classList.remove('promoterControl');
        insertAfter(duplicate, original,counter);
    }





    function insertAfter(newElement, targetElement,numCassette) {
        var parent = targetElement.parentNode;

        var child = newElement.parentNode;

        if (parent.lastChild == targetElement) {
            parent.appendChild(newElement);

        } else {


            parent.insertBefore(newElement, targetElement.laterSibling);

        }
        //remove extraneous items  from clone.
        //need to accurately pass className of duplicates here

        var classOfDuplicate = jQuery(counter);
        //how do I use this is a jQuery variable?
        if (numCassette < 3) {
        jQuery('select', jQuery('.2')).each(function () {

            jQuery(this).remove();
        });

        jQuery('button', jQuery('.2')).each(function () {

            if (this.id.indexOf("1") == -1) {

                jQuery(this).remove();
            }
        });


        jQuery('input', jQuery('.2')).each(function () {

            if (this.id.indexOf("Promoter1") == -1) {
                if (this.id.indexOf("Coding1") == -1) {

                    jQuery(this).remove();
                }

            }
        });
    }

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
        });
    }

/// Delete nearest parent fieldset
    function deleteMe(a) {

        var duplicate = a.parentNode;
        while (duplicate.nodeName.toLowerCase() != 'fieldset') {
            duplicate = duplicate.parentNode;
        }
        duplicate.parentNode.removeChild(duplicate);
        counter = counter - 1;
        generateConstructName();
    }

    function addComponent(divToAttachTo, thisId, component,buttonClassName) {

        cassetteNumber = parseInt(buttonClassName);

        componentInputArray = [];

        var ul = jQuery("#" + divToAttachTo);


        if (component == "Promoter") {
            var nextComponent = parseInt(thisId.substring(11)) + 1;
        }
        else {
            var nextComponent = parseInt(thisId.substring(9)) + 1;
        }
        //get number of input components in current div
        var inputComponents = parseInt(jQuery("#" + divToAttachTo).find("input").size());
        //get id of next component
        var i = jQuery("#" + divToAttachTo).find("input").size() + 1;

        //get number of select components
        var selectComponents = parseInt(jQuery("#" + divToAttachTo).find("select").size());
        //get total number of components to iterate through
        var countOfComponents = selectComponents + inputComponents;

        var componentInputId = "cassette" + cassetteNumber + component + i;
        var componentInputClass = "cassette"+ cassetteNumber+component ;
        var btnAddId = "add" + component + i;
        var btnDelId = "del" + component + i;
        var selectListID = "selectList" + component + i;

        var selectListComponent = "<select   id='" + selectListID+"'+ class='" + componentInputClass+"'>";
        selectListComponent += "<option>" + "-" + "</option>";
        selectListComponent += "<option>" + "," + "</option>";
        selectListComponent += "<option>" + "." + "</option>";
        selectListComponent += "<option>" + "" + "</option>";

        selectListComponent += "</select>";
        var li = jQuery(selectListComponent + "<input   size=10  class='"+componentInputClass +"'+ name='" + componentInputId + "'+ id='" + componentInputId + "' />" + "<button class='"+cassetteNumber+"'+ id='" + btnAddId + "' >+</button>" + "<button class='"+cassetteNumber+"'+ id='" + btnDelId + "' >-</button>");
        li.appendTo(ul);
        for (var k = nextComponent; k <= countOfComponents + 2; k++) {
            var j = k + 1;  //j is incremented to avoid the select box
            var a = jQuery("#"+"cassette"+cassetteNumber+component + k).val();

            componentInputArray.push({id: j, value: a});
        }

        jQuery.each(componentInputArray, function () {
            jQuery("#"+"cassette"+cassetteNumber+component + this.id).val(this.value).focus();
        });
        jQuery("#"+"cassette"+cassetteNumber+component + nextComponent).val("").focus();
        return false;
    }

function deleteComponent(divToAttachTo, thisId, component,buttonClassName){
    if (component=="Promoter"){
        var componentNumber = parseInt(thisId.substring(11));
    }
    else{
        var componentNumber = parseInt(thisId.substring(9));
    }
    var ul = jQuery("#" + divToAttachTo);
    var i = jQuery("#" + divToAttachTo).find("input").size() ;

    if (i==1){
        alert("Construct must have at least one component");
        return;
    }
    jQuery(jQuery("#"+divToAttachTo)).each(function(){

        jQuery("#" + "add" + component + componentNumber).remove();
        jQuery("#" + "del" + component + componentNumber).remove();
        jQuery("#" + "cassette" + buttonClassName + component + componentNumber).remove();

        jQuery("#" + "selectList" + component + componentNumber).remove();
    });
    //after deleting renumber input, select box and button ids.

    componentCount=1; //counter to increment for each input box
    jQuery('input',jQuery("#"+divToAttachTo)).each(function(){

        jQuery(this).attr("id","cassette"+buttonClassName+component+componentCount);
        componentCount++;
    });

    addButtonCount=1;
    delButtonCount=1//counter to increment for each add and delete button
    jQuery('button',jQuery("#"+divToAttachTo)).each(function(){

       if(this.id.substring(0,3)=="add"){

        jQuery(this).attr("id","add"+component+addButtonCount);
            addButtonCount++;
        }
        else{

            jQuery(this).attr("id","del"+component+delButtonCount);
            delButtonCount++;
        }

    });

    selectListCount=2; //counter to keep track of select list for Promoter and coding. starts with 2 since the listbox starts only with the second component
    jQuery('select',jQuery("#"+divToAttachTo)).each(function(){
        jQuery(this).attr("id","selectList"+component+selectListCount);
        selectListCount++;
    });


 }
function validateAndSubmit(){
//    the display name always defaults to "Tg"
//  and if curator clicks on done, the puts the "Tg()" so that is why I am checking for length.



    /*if (document.getElementById("cName").value.length <=5 ){
        //jQuery("#add-construct-error").append("<font color=red>Construct Name cannot be blank");
    }
    else {
        jQuery("#cName").removeAttr("disabled", "disabled");
    }*/
    var param = jQuery("form").serialize();
var pubID=jQuery('#constructPublicationZdbID').val();
    console.log(param);


    jQuery.ajax({
        url: '/action/construct/construct-add-component/',
        type: 'POST',
        data: jQuery("form").serialize(),
        success: function (response) {
            //alert(response);
            refreshAttribution(pubID);
            refreshRelationship(pubID);

            if (response != "") {
                jQuery("#add-construct-error").html(response);

                jQuery("#add-construct-error").show();
                if (response.indexOf("successfully") > 1) {

                    resetFields();
                    jQuery("#add-construct-error").html(response);

                    jQuery("#add-construct-error").show();
                }
                //resetFields();
            }
            else{
                jQuery("#add-construct-error").html(response);
                jQuery("#add-construct-error").show();
               // jQuery('#add-construct-error').hide();
                resetFields();
            }


        },
        error: function (response) {
            alert('There was a problem with your request: ' + response);
            resetFields();
        }

    });


}
function resetFields(){

    jQuery("#prefix").val("");
    jQuery("#constructDisplayName").val("");
    jQuery("#cName").val("");
    jQuery("#constructStoredName").val("");
    jQuery("#constructAlias").val("");
    jQuery("#constructComments").val("");
    jQuery("#constructCuratorNote").val("");
    jQuery(".duplicate").remove();
    jQuery("#cName").attr("disabled", "disabled");
    jQuery('#add-construct-error').hide();
    jQuery('select',jQuery('.2')).each(function(){

        jQuery(this).remove();
    });
    jQuery('select',jQuery('.1')).each(function(){

        jQuery(this).remove();
    });
    jQuery('button',jQuery('.1')).each(function(){

        if (this.id.indexOf("1")==-1) {

            jQuery(this).remove();
        }
    });
    jQuery('input',jQuery('.1')).each(function() {

        if (this.id.indexOf("Promoter1") == -1)  {
            if (this.id.indexOf("Coding1") == -1) {

                jQuery(this).remove();
            }
                else{
                    jQuery(this).val("");
                    jQuery(this).css({"background-color": 'white'});

            }

        }
        else{
            jQuery(this).val("");
            jQuery(this).css({"background-color": 'white'});
        }
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
    });


   /* jQuery('select',$("#promoterCassette1")).each(function(){
        alert("removing");
        this.remove();
    });
    jQuery('select',$("#codingCassette1")).each(function(){
        this.remove();
    });
    jQuery('input',$("#promoterCassette1")).each(function(){
        if (this.id.indexOf("Promoter1")==-1) {
            this.remove();
        }
        else {
            alert("blank out romoters should come here");
            jQuery(this).attr("value","");
            jQuery(this).css({"background-color": 'white'});
        }

    });
    jQuery('input',$("#codingCassette1")).each(function(){
        if (this.id.indexOf("Coding1")==-1) {
            this.remove();
        }
        else {
            alert("should come here");
            jQuery(this).attr("value","");
            jQuery(this).css({"background-color": 'white'});
        }
    });
    jQuery('button',$("#promoterCassette1")).each(function(){
        alert(this.id.indexOf("1"));
        if (this.id.indexOf("1")==-1) {
            alert(this.id.indexOf("1"))
            this.remove();
        }
        });
        jQuery('button',$("#codingCassette1")).each(function(){
            if (this.id.indexOf("1")==-1) {
                this.remove();
            }
            });
*/
        counter=1;//reset cassette counter to 1


}

    function generateConstructName() {
        //This is the function used to generate the display name
        //this function also generates a "stored" name to pass to the controller in order to parse
        //the construct into "components"
        //In the stored string, the cassettes are separated by a "%" delimiter
        //Promoters are preceeded by the text "Prom" and coding are preceeded by "Coding"
        //each element is delimited by a "#"
        if (jQuery("#prefix").val() != "") {
            var constructGeneratedName = jQuery("#chosenType").val() + jQuery("#prefix").val() + openP;
            var constructWrapperString="wrapper"+jQuery("#chosenType").val()+"#"+jQuery("#prefix").val()+"#"+"wrapper"+openP;
        }
        else {
            var constructGeneratedName = jQuery("#chosenType").val() + openP;
            var constructWrapperString=jQuery("#chosenType").val()+"#"+"wrapper"+openP ;

        }


        var cassetteDelimiter = ",";
        var promCodingSeparator = ":";
        var cassette1String = "";
        var cassette2String = "";
        var cassette3String = "";
        var numberOfCassettes=1;
        var cassette1Promoter="";
        var cassette1Coding="";
        var cassette2Promoter="";
        var cassette2Coding="";
        var cassette3Promoter="";
        var cassette3Coding="";
        var promPrefix="Prom";
        var codingPrefix="Coding"

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
        var consStoreName2 = ""
        var storedPromString = "";
        var storedCodingString = "";
        var storedPromString2 = "";
        var storedCodingString2 = "";
        var storedPromString3 = "";
        var storedCodingString3 = "";
        var prefixStr = "";
        var closeP = ")";

        //first deal with default  cassette, cassette1
        jQuery.each(jQuery('.cassette1Promoter'), function (index) {
            cassette1String += jQuery(this).attr('value');
            cassette1Promoter+= promPrefix+jQuery(this).attr('value')+"#";
        });
        jQuery.each(jQuery('.cassette1Coding'), function (index) {
            if (jQuery(this).attr('value') != '') {
                if (cassette1String != "") {
                    if (cassette1String.indexOf(promCodingSeparator)==-1) {
                        cassette1String += promCodingSeparator;
                        cassette1Promoter+=promPrefix+promCodingSeparator+"#";
                    }
                }

            }
        });

        jQuery.each(jQuery('.cassette1Coding'), function (index) {
            if (jQuery(this).attr('value') != '')
                cassette1String += jQuery(this).attr('value');
                cassette1Coding+= codingPrefix+jQuery(this).attr('value')+"#";
        });

        //second cassette Strings
        jQuery.each(jQuery('.cassette2Promoter'), function (index) {
            cassette2String += jQuery(this).attr('value');
            cassette2Promoter+= promPrefix+jQuery(this).attr('value')+"#";
        });
        jQuery.each(jQuery('.cassette2Coding'), function (index) {
            if (jQuery(this).attr('value') != '') {
                if (cassette2String != "") {
                    if (cassette2String.indexOf(promCodingSeparator)==-1) {
                        cassette2String += promCodingSeparator;
                        cassette2Promoter+=promPrefix+promCodingSeparator+"#";
                    }
                }

            }
        });
        jQuery.each(jQuery('.cassette2Coding'), function (index) {
            if (jQuery(this).attr('value') != '')
                cassette2String += jQuery(this).attr('value');
                 cassette2Coding+= codingPrefix+jQuery(this).attr('value')+"#";
        });

        //third cassette Strings
        jQuery.each(jQuery('.cassette3Promoter'), function (index) {
            cassette3String += jQuery(this).attr('value');
            cassette3Promoter+= promPrefix+jQuery(this).attr('value')+"#";
        });
        jQuery.each(jQuery('.cassette3Coding'), function (index) {
            if (jQuery(this).attr('value') != '') {
                if (cassette3String != "") {
                    if (cassette3String.indexOf(promCodingSeparator)==-1) {
                        cassette3String += promCodingSeparator;
                        cassette3Promoter+=promPrefix+promCodingSeparator+"#";
                    }

                }

            }
        });
        jQuery.each(jQuery('.cassette3Coding'), function (index) {
            if (jQuery(this).attr('value') != '')
                cassette3String += jQuery(this).attr('value');
                cassette3Coding+= codingPrefix+jQuery(this).attr('value')+"#";
        });
        if (cassette2String !== "") {
            numberOfCassettes++;
        }
        cassette2String != "" ? constructGeneratedName += cassette1String + cassetteDelimiter + cassette2String : constructGeneratedName += cassette1String;
        if (cassette3String !== "") {
            constructGeneratedName += cassetteDelimiter + cassette3String;
            numberOfCassettes++;
        }
        // cassette3String!=""? constructGeneratedName+=cassetteDelimiter+cassette3String : constructGeneratedName+=cassette1String+cassetteDelimiter+ cassette2String ;
        constructGeneratedName += closeP;
        jQuery('#constructName').val(constructGeneratedName);
        jQuery('#cName').val(constructGeneratedName);
        jQuery("#constructWrapperString").attr("value","testdd");

        jQuery("#cassette1Promoter").val(cassette1Promoter);
        jQuery("#cassette2Promoter").val(cassette2Promoter);
        jQuery("#cassette3Promoter").val(cassette2Promoter);
        jQuery("#cassette1Coding").val(cassette1Coding);
        jQuery("#cassette2Coding").val(cassette2Coding);
        jQuery("#cassette3Coding").val(cassette3Coding);
        jQuery("#numberOfCassettes").val(numberOfCassettes);
        jQuery.each(jQuery('.cassette1Promoter'), function (index) {

            // alert(jQuery(this).val());
            partPromoterString += jQuery(this).val();

            storedPromString = storedPromString + "Prom" + jQuery(this).val() + "#";
        });

        jQuery.each(jQuery('.cassette1Coding'), function (index) {
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

        jQuery.each(jQuery('.cassette2promoter'), function (index) {
            cass2PromoterString += jQuery(this).val();
            if (jQuery(this).val()!='') {
                storedPromString2 = storedPromString2 + "Prom" + jQuery(this).val() + "#";
            }
        });

        jQuery.each(jQuery('.cassette2Coding'), function (index) {
            cass2CodingString += jQuery(this).val();
            if (jQuery(this).val()!='') {
                storedCodingString2 = storedCodingString2 + "Coding" + jQuery(this).val() + "#";
            }

        });

        if (cass2CodingString != "") {
            if (cass2PromoterString != "") {
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
        jQuery.each(jQuery('.cassette3promoter'), function () {
            cass3PromoterString += jQuery(this).val();
            if (jQuery(this).val()!='') {
                storedPromString3 = storedPromString3 + "Prom" + jQuery(this).val() + "#";
            }
        });
        jQuery.each(jQuery('.cassette3coding'), function () {
            cass3CodingString += jQuery(this).val();
            if (jQuery(this).val()!='') {
                storedCodingString3 = storedCodingString3 + "Coding" + jQuery(this).val() + "#";
            }
        });
        if (cass3CodingString != "") {
            if (cass3PromoterString != "") {
                constructDisplayName2 = cass3PromoterString + ":" + cass3CodingString;
                consStoreName2 = storedPromString3 + "#" + "Prom:" + "#" + storedCodingString3;
                if (constructDisplayName2.length == 0) {

                jQuery("#constructName").val(constructDisplayName);
                jQuery("#constructDisplayName").val(constructDisplayName);
            }
                else
                {
                    jQuery("#constructName").val(constructDisplayName + "," + constructDisplayName1 + "," + constructDisplayName2);
                    jQuery("#constructDisplayName").val(constructDisplayName + "," + constructDisplayName1 + "," + constructDisplayName2);
                    //consStoreName = consStoreName + "#" + "%" + "#" + "Cassette," + "#" + consStoreName1+ "#" + "%" + "#" + "Cassette," + "#" + consStoreName2;
                    consStoreName = consStoreName + "#" + "%" + "#" + "Cassette," + "#" + consStoreName2;
                    jQuery("#constructStoredName").val(consStoreName);
                }
            } else {
                constructDisplayName2 = cass3CodingString;
                if (constructDisplayName2.length == 0) {


                        jQuery("#constructName").val(constructDisplayName);
                        jQuery("#constructDisplayName").val(constructDisplayName);
                    }
                else {
                    jQuery("#constructName").val(constructDisplayName + "," + constructDisplayName1 + "," + constructDisplayName2);
                    jQuery("#constructDisplayName").val(constructDisplayName + "," + constructDisplayName1 + "," + constructDisplayName2);
                    consStoreName = consStoreName + "%" + "#" + "Cassette," + "#" + consStoreName1+ "%" + "#" + "Cassette," + "#" + consStoreName2;
                    jQuery("#constructStoredName").val(consStoreName);

                }
            }
        }
        else {

            constructDisplayName2 = cass3PromoterString;
            consStoreName2=storedPromString3;
            if (constructDisplayName2.length == 0) {
                jQuery("#constructName").val(constructDisplayName);
                jQuery("#constructDisplayName").val(constructDisplayName);
            }
            else {
                jQuery("#constructName").val(constructDisplayName + "," + constructDisplayName1 + "," + constructDisplayName2);
                jQuery("#constructDisplayName").val(constructDisplayName + "," + constructDisplayName1 + "," + constructDisplayName2);
                consStoreName = consStoreName + "%" + "Cassette," + "#" + consStoreName1 + "%" + "Cassette," + "#" + consStoreName2;
                jQuery("#constructStoredName").val(consStoreName);
            }
        }
         if (constructDisplayName2.length == 0) {
            if (constructDisplayName1.length == 0)
                jQuery("#constructName").val(constructDisplayName);
            else {
                jQuery("#constructName").val(constructDisplayName + "," + constructDisplayName1);
            }
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
        /*if (finalDisplayName.indexOf(".-") > -1){
            alert("construct name has a dot folowed by a hyphen. Please correct")
        }*/
        jQuery('#constructDisplayName').val(finalDisplayName);
        jQuery('#constructName').val(finalDisplayName);
        if (finalStoredName.indexOf(closeP) == -1) {
            jQuery('#constructStoredName').val(finalStoredName + "#" + closeP);

        }
        else {
            jQuery('#constructStoredName').val(finalStoredName);

        }



    }






