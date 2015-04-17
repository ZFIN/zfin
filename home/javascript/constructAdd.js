/**
 Javascript file to accompany construct Adding.
 */

//
/*
 anatomy of a construct: Construct TypePrefix(promoter1:coding1,promoter2:coding2,promoter3:coding
 builder is limited to 3 cassettes
 each cassette may have a promoter or a coding (can either have both,or one of either but has to have at least 1 of either promoter or coding)
*/


var openP='(';
var cassette1="";
var cassette2="";
var cassette3="";
var cassette1Stored="";
var cassette2Stored="";
var cassette3Stored="";
var finalCname="";
var finalStoredName="";
var closeP=")";
var constructSeparator=":"
var cassetteSeparator=",";

jQuery(document).ready(function () {

    jQuery("#prefix").on("focusout", function (e) {
           makeName();

    });


    jQuery("#chosenType")
        .change(function () {
                      makeName();
        })
        .change();


    /*The following function is to enable all promoter and coding fields be an autocomplete.
     Since all coding and promoter fields have an id starting with "Cassette", the autocomplete works
     for all promoter and coding in all cassettes
     the autocomplete is on markers attributed to the pub. It also contains foreign species and other constrolled vocabularies*/

    autocompleteSource = '/action/construct/find-constructMarkers';
    var pubId = jQuery('#constructPublicationZdbID').val();

    jQuery(document).on("focus keyup", "input", function (event) {

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
                // If a curator overrides the autocomplete, the background color of the input box chnages ot blue indicating that it was "free text"
                if (ui.item == null) {
                    jQuery(this).css({ "background-color": '#F0FFFF'});


                }
            }

        }).keypress(function(event) {
           if (event.keyCode == 13) {
               event.preventDefault();
               return false;
           }

    });
        jQuery("input[id^='cassette']").on('focusout', function (event) {

            event.preventDefault();
//
            makeName();
        });
        /*jQuery("input[id^='cassette']").keypress(function(event) {
            if (event.keyCode == 13) {
                event.preventDefault();
                return false;
            }
        });*/


    })


});

    /* This function is for adding Promoters to the first cassette.
     when the user clicks on "+", an empty input field with a select list for Promoters is added to the div.
     if there is a value in between, that value has to eb shifted to the right.*/


  jQuery(document).on("click","button[id^='addPromoter']",function (event){
        event.preventDefault();
        var divToAttachTo = jQuery(this).closest("div").attr("id");
        var buttonClassName = jQuery(this).attr('class');
        addComponent(divToAttachTo, this.id, "Promoter", buttonClassName);
      makeName();

    });

jQuery(document).on("change","select[id^='selectList']",function (event){

    event.preventDefault();

    makeName();

});


//this function is for deleting the fields.
jQuery(document).on("click","button[id^='delPromoter']",function (event){


        event.preventDefault();
        var divToAttachTo = jQuery(this).closest("div").attr("id");

        var buttonClassName = jQuery(this).attr('class');
        deleteComponent(divToAttachTo, this.id, "Promoter", buttonClassName);
        makeName();
    });

jQuery(document).on("click","button[id^='addCoding']",function (event){


        event.preventDefault();
    var divToAttachTo = jQuery(this).closest("div").attr("id");
    var buttonClassName = jQuery(this).attr('class');
        addComponent(divToAttachTo, this.id, "Coding", buttonClassName);
    makeName();

    });

jQuery(document).on("click","button[id^='delCoding']",function (event){
        event.preventDefault();
    var divToAttachTo = jQuery(this).closest("div").attr("id");
    var buttonClassName = jQuery(this).attr('class');
        deleteComponent(divToAttachTo, this.id, "Coding", buttonClassName);
        makeName();

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

     /*   jQuery('input', jQuery('.duplicate')).each(function () {
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
        });*/



        insertAfter(duplicate, original,counter);
    }





    function insertAfter(newElement, targetElement,numCassette) {
        var parent = targetElement.parentNode;



        if (parent.lastChild == targetElement) {
            parent.appendChild(newElement);

        } else {


            parent.insertBefore(newElement, targetElement.laterSibling);

        }

        //remove extraneous items  from clone.
        //need to accurately pass className of duplicates here


        //only remove extra elements form cassette2 if there is no cassette3
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
        //add cassette but remove extra elements

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
        makeName();
    }

    function addComponent(divToAttachTo, thisId, component,buttonClassName) {
//the add does not only add fields, (input box, select box and + and - buttons. It also has to remember the vale of the input and select box before it.
// Curator might choose to insert fields between exisitng fileds an dnot lose values already entered

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

        //This work is done to "remember" the input values, if a box gets added in between 2 alreday filled boxes.
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

function deleteComponent(divToAttachTo, thisId, component,buttonClassName) {
    if (component == "Promoter") {
        var componentNumber = parseInt(thisId.substring(11));
    }
    else {
        var componentNumber = parseInt(thisId.substring(9));
    }

    var i = jQuery("#" + divToAttachTo).find("input").size();
    if (buttonClassName == 1) {

        if (i == 1) {
            alert("Construct must have at least one component");
            return;
        }
    }

    jQuery(jQuery("#"+divToAttachTo)).each(function(){

//TODO for code reviewer, this seems to remove buttons always from the first cassette, i dont know why. (meaning if I am in second cassette and the div points to second (or third)cassette, first cassette buttons get deleted).

        jQuery("#" + "add" + component + componentNumber).remove();
        jQuery("#" + "del" + component + componentNumber).remove();
       jQuery("#" + "cassette" + buttonClassName + component + componentNumber).remove();

        jQuery("#" + "selectList" + component + componentNumber).remove();


    });


    //after deleting renumber input, select box and button ids and do not lose values of  alreday entered inputs.

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

    var param = jQuery("form").serialize();
var pubID=jQuery('#constructPublicationZdbID').val();
    console.log(param);


    jQuery.ajax({
        url: '/action/construct/construct-add-component/',
        type: 'POST',
        data: jQuery("form").serialize(),
        success: function (response) {

            //the 2 functions are for teh GWT to recognixe that new constructs have been added for them to reflect right away in the "Attribute marker" section
            //as well as the relationships section
            refreshAttribution(pubID);
            refreshRelationship(pubID);

            if (response != "") {
                jQuery("#add-construct-error").html(response);

                jQuery("#add-construct-error").show();
                //need to reset fields upon successful insert.
                if (response.indexOf("successfully") > 1) {

                    jQuery("#add-construct-error").html(response);
                    jQuery("#add-construct-error").show();
                    resetFields();

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
    jQuery("#storedCass1").val("");
    jQuery("#constructAlias").val("");
    jQuery("#constructComments").val("");
    jQuery("#constructCuratorNote").val("");
    jQuery(".duplicate").remove();
    jQuery("#cName").attr("disabled", "disabled");
    //jQuery('#add-construct-error').hide();

    //The resetting has to remove all extra fields and cassettes, if there is easier way in js to do it will be great

    jQuery('.2').each(function(){

        jQuery(this).remove();
    });
    jQuery('.3').each(function(){

        jQuery(this).remove();
    });

       jQuery('button',jQuery('.1')).each(function(){

        if (this.id.indexOf("1")==-1) {

            jQuery(this).remove();
        }
    });
    jQuery('select',jQuery('.1')).each(function(){

        jQuery(this).remove();
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


        counter=1;//reset cassette counter to 1


}
function makeName(){
    jQuery('#add-construct-error').hide();
    var cassette1PromoterString="";
    var cassette1CodingString="";

    var cassette2PromoterString="";
    var cassette2CodingString="";

    var cassette3PromoterString="";
    var cassette3CodingString="";

    var componentDelim="#"

    finalCname=jQuery("#chosenType").val();
    finalCname+=jQuery("#prefix").val()+openP;

    jQuery.each(jQuery('.cassette1Promoter'), function (index) {
        cassette1PromoterString +=jQuery(this).val()+componentDelim;

    });
    jQuery.each(jQuery('.cassette1Coding'), function (index) {
        cassette1CodingString +=jQuery(this).val()+componentDelim;

    });
    cassette1=((cassette1CodingString!=componentDelim)?cassette1PromoterString+constructSeparator+cassette1CodingString:cassette1PromoterString);
    cassette1Stored=((cassette1CodingString!="")?cassette1PromoterString+constructSeparator+cassette1CodingString:cassette1PromoterString);

    jQuery.each(jQuery('.cassette2Promoter'), function (index) {
        cassette2PromoterString +=jQuery(this).val()+componentDelim;
    });
    jQuery.each(jQuery('.cassette2Coding'), function (index) {
        cassette2CodingString +=jQuery(this).val()+componentDelim;
    });

    cassette2=(((cassette2CodingString!="")&&(cassette2CodingString!=componentDelim))?cassette2PromoterString+constructSeparator+cassette2CodingString:cassette2PromoterString);
    cassette2Stored=((cassette2CodingString!="")?cassette2PromoterString+constructSeparator+cassette2CodingString:cassette2PromoterString);

    jQuery.each(jQuery('.cassette3Promoter'), function (index) {
        cassette3PromoterString +=jQuery(this).val()+componentDelim;
    });
    jQuery.each(jQuery('.cassette3Coding'), function (index) {
        cassette3CodingString +=jQuery(this).val()+componentDelim;
    });
    cassette3=(((cassette3CodingString!="")&&(cassette3CodingString!=componentDelim))?cassette3PromoterString+constructSeparator+cassette3CodingString:cassette3PromoterString);
    cassette3Stored=((cassette3CodingString!="")?cassette3PromoterString+constructSeparator+cassette3CodingString:cassette3PromoterString);

    if (cassette2!=""){
        finalCname+=cassette1+cassetteSeparator+cassette2;
        finalStoredName=cassette1Stored+"Cassette"+componentDelim+cassetteSeparator+componentDelim+cassette2Stored;
    }
    else{
        finalCname+=cassette1;
        finalStoredName=cassette1Stored;


    }
    if (cassette3!=""){
        finalCname+=cassetteSeparator+cassette3;
        finalStoredName=cassette1Stored+"Cassette"+componentDelim+cassetteSeparator+componentDelim+cassette2Stored+"Cassette"+componentDelim+cassetteSeparator+componentDelim+cassette3Stored;

    }
  finalCname+=closeP;
  var finalName=finalCname.split("#").join("");
    var storedName=finalStoredName.replace("##","#");


    jQuery('#constructDisplayName').val(finalName);
    jQuery('#constructName').val(finalName);
    jQuery('#constructStoredName').val(storedName);








}







