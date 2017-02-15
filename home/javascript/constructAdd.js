/**
 /**
 Javascript file to accompany construct Adding.
 */

//
/*
 anatomy of a construct: Construct TypePrefix(promoter1:coding1,promoter2:coding2,promoter3:coding
 builder is limited to 3 cassettes
 each cassette may have a promoter or a coding (can either have both,or one of either but has to have at least 1 of either promoter or coding)
*/
//using variables names for stuff common betweeen all constructs  , like "(" and ")", also for cassette spearators (",") and promoter, coding separators (":")


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
var constructSeparator=":";
var storedConstructSeparator=":";
var cassetteSeparator=",";

$(document).ready(function () {

    $("#prefix").on("focusout", function (e) {
           makeName();

    });


    $("#chosenType")
        .change(function () {
                      makeName();
        })
        .change();


    /*The following function is to enable all promoter and coding fields be an autocomplete.
     Since all coding and promoter fields have an id starting with "Cassette", the autocomplete works
     for all promoter and coding in all cassettes
     the autocomplete is on markers attributed to the pub. It also contains foreign species and other constrolled vocabularies*/

    autocompleteSource = '/action/construct/find-constructMarkers';
    var pubId = $('#constructPublicationZdbID').val();

    $(document).on("focus keyup", "input", function (event) {

       $("input[id^='cassette']").autocomplete({
            source: function (request, response) {
                $.ajax({
                    url: autocompleteSource,
                    dataType: "json",
                    data: {
                        term: request.term,
                        pub: pubId

                    },
                    success: function (data) {
                        response(data);
                    }
                });
            },
            minLength: 2,
            autoFocus: true,

            select: function (event, ui) {

                event.preventDefault();

                $(this).data("selected", true);
                $(this).val(ui.item.value);
                $(this).css({ "background-color": 'white'});


            }, change: function (event, ui) {
                // If a curator overrides the autocomplete, the background color of the input box chnages ot blue indicating that it was "free text"
                if (ui.item == null) {
                    $(this).css({ "background-color": '#F0FFFF'});


                }
            }

        }).keypress(function(event) {
           if (event.keyCode == 13) {
               event.preventDefault();
               return false;
           }

    }).on('focusout', function (event) {

           event.preventDefault();
//
           makeName();

        });



    })


});

    /* This function is for adding Promoters to the first cassette.
     when the user clicks on "+", an empty input field with a select list for Promoters is added to the div.
     if there is a value in between, that value has to eb shifted to the right.*/


  $(document).on("click","button[id^='addPromoter']",function (event){
        event.preventDefault();
        var divToAttachTo = $(this).closest("div").attr("id");
        var buttonClassName = $(this).attr('class');
        addComponent(divToAttachTo, this.id, "Promoter", buttonClassName);
      makeName();

    });

$(document).on("change","select[id^='selectList']",function (event){

    event.preventDefault();

    makeName();

});


//this function is for deleting the fields.
$(document).on("click","button[id^='delPromoter']",function (event){


        event.preventDefault();
        var divToAttachTo = $(this).closest("div").attr("id");

        var buttonClassName = $(this).attr('class');
        deleteComponent(divToAttachTo, this.id, "Promoter", buttonClassName);
        makeName();
    });

$(document).on("click","button[id^='addCoding']",function (event){


        event.preventDefault();
    var divToAttachTo = $(this).closest("div").attr("id");
    var buttonClassName = $(this).attr('class');
        addComponent(divToAttachTo, this.id, "Coding", buttonClassName);
    makeName();

    });

$(document).on("click","button[id^='delCoding']",function (event){
        event.preventDefault();
    var divToAttachTo = $(this).closest("div").attr("id");
    var buttonClassName = $(this).attr('class');
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
        var num = $('.clonable').length;
        //alert(num);
        var duplicate = $('#cassette' + num).clone().attr('id', 'cassette' + counter);
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
                    newInput[i].className = "cassette" + counter + "Promoter";
                }
                else {
                    newInput[i].className = "cassette" + counter + "Coding";
                }
            }


        }

        duplicate.className = 'duplicate';





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

        $('select', $('.2')).each(function () {

            $(this).remove();
        });

        $('button', $('.2')).each(function () {

            if (this.id.indexOf("1") == -1) {

                $(this).remove();
            }
        });


        $('input', $('.2')).each(function () {

            if (this.id.indexOf("Promoter1") == -1) {
                if (this.id.indexOf("Coding1") == -1) {

                    $(this).remove();
                }

            }
        });
    }
        //add cassette but remove extra elements

        $('select',$('.3')).each(function(){

            $(this).remove();
        });


        $('button',$('.3')).each(function(){

            if (this.id.indexOf("1")==-1) {

                $(this).remove();
            }
        });
        $('input',$('.3')).each(function() {

            if (this.id.indexOf("Promoter1") == -1)  {
                if (this.id.indexOf("Coding1") == -1) {

                    $(this).remove();
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
//the add does not only add fields, (input box, select box and + and - buttons. It also has to remember the value of the input and select box before it.
// Curator might choose to insert fields between exisitng fileds an dnot lose values already entered

        cassetteNumber = parseInt(buttonClassName);

        componentInputArray = [];

        var ul = $("#" + divToAttachTo);


        if (component == "Promoter") {
            var nextComponent = parseInt(thisId.substring(11)) + 1;
        }
        else {
            var nextComponent = parseInt(thisId.substring(9)) + 1;
        }
        //get number of input components in current div
        //var inputComponents = parseInt($("#" + divToAttachTo).find("input").size());
        var inputComponents = $("#" + divToAttachTo).find("input").length;
        //get id of next component
        var i = $("#" + divToAttachTo).find("input").size() + 1;

        //get number of select components
        //var selectComponents = parseInt($("#" + divToAttachTo).find("select").size());
        var selectComponents = $("#" + divToAttachTo).find("select").length;
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
        var li = $(selectListComponent + "<input   size=10  class='"+componentInputClass +"'+ name='" + componentInputId + "'+ id='" + componentInputId + "' />" + "<button class='"+cassetteNumber+"'+ id='" + btnAddId + "' >+</button>" + "<button class='"+cassetteNumber+"'+ id='" + btnDelId + "' >-</button>");
        li.appendTo(ul);

        //This work is done to "remember" the input values, if a box gets added in between 2 alreday filled boxes.
        for (var k = nextComponent; k <= countOfComponents + 2; k++) {
            var j = k + 1;  //j is incremented to avoid the select box
            var a = $("#"+"cassette"+cassetteNumber+component + k).val();

            componentInputArray.push({id: j, value: a});
        }

        $.each(componentInputArray, function () {
            $("#"+"cassette"+cassetteNumber+component + this.id).val(this.value).focus();
        });
        $("#"+"cassette"+cassetteNumber+component + nextComponent).val("").focus();
        return false;
    }

function deleteComponent(divToAttachTo, thisId, component,buttonClassName) {

    if (component == "Promoter") {
        var componentNumber = parseInt(thisId.substring(11));
    }
    else {
        var componentNumber = parseInt(thisId.substring(9));
    }

    var i = $("#" + divToAttachTo).find("input").length;
    if (buttonClassName == 1) {

        if (i == 1) {
            alert("Construct must have at least one component");
            return;
        }
    }

    $($("#"+divToAttachTo)).each(function(){

//TODO for code reviewer, this seems to remove buttons always from the first cassette, i dont know why. (meaning if I am in second cassette and the div points to second (or third)cassette, first cassette buttons get deleted).

        $("#" + "add" + component + componentNumber).remove();
        $("#" + "del" + component + componentNumber).remove();
       $("#" + "cassette" + buttonClassName + component + componentNumber).remove();
        $("#" + "selectList" + component + componentNumber).remove();


    });


    //after deleting renumber input, select box and button ids and do not lose values of  alreday entered inputs.

    var componentCount=1; //counter to increment for each input box
    $('input',$("#"+divToAttachTo)).each(function(){

        $(this).attr("id","cassette"+buttonClassName+component+componentCount);
        componentCount++;
    });

    var addButtonCount=1;
    var delButtonCount=1//counter to increment for each add and delete button
    $('button',$("#"+divToAttachTo)).each(function(){

       if(this.id.substring(0,3)=="add"){

        $(this).attr("id","add"+component+addButtonCount);
            addButtonCount++;
        }
        else{

            $(this).attr("id","del"+component+delButtonCount);
            delButtonCount++;
        }

    });

    var selectListCount=2; //counter to keep track of select list for Promoter and coding. starts with 2 since the listbox starts only with the second component
    $('select',$("#"+divToAttachTo)).each(function(){
        $(this).attr("id","selectList"+component+selectListCount);
        selectListCount++;
    });


 }
function validateAndSubmit(){


var pubID=$('#constructPublicationZdbID').val();



    $.ajax({
        url: '/action/construct/construct-add-component/',
        type: 'POST',
        data: $("form").serialize(),
        success: function (response) {

            //the 2 functions are for GWT to recognize that new constructs have been added for them to reflect right away in the "Attribute marker" section
            //as well as the relationship section
            refreshAttribution(pubID);
            refreshRelationship(pubID);

            if (response != "") {
                $("#add-construct-error").html(response);

                $("#add-construct-error").show();
                //need to reset fields upon successful insert.
                if (response.indexOf("successfully") > 1) {

                    $("#add-construct-error").html(response);
                    $("#add-construct-error").show();
                    resetFields();

                }

            }
            else{
                $("#add-construct-error").html(response);
                $("#add-construct-error").show();
               // $('#add-construct-error').hide();
                resetFields();
            }
            jQuery('#constructedit').load('/action/construct/construct-update?constructPublicationZdbID='+pubID);


        },
        error: function (response) {
            alert('There was a problem with your request: ' + response);
            resetFields();
        }

    });


}
function resetFields(){

    $("#prefix").val("");
    $("#constructDisplayName").val("");
    $("#cName").val("");
    $("#constructStoredName").val("");
    $("#storedCass1").val("");
    $("#constructAlias").val("");
    $("#constructComments").val("");
    $("#constructCuratorNote").val("");
    $(".duplicate").remove();
    $("#cName").attr("disabled", "disabled");
    //$('#add-construct-error').hide();

    //The resetting has to remove all extra fields and cassettes, if there is easier way in js to do it will be great

    $('.2').each(function(){

        $(this).remove();
    });
    $('.3').each(function(){

        $(this).remove();
    });

       $('button',$('.1')).each(function(){

        if (this.id.indexOf("1")==-1) {

            $(this).remove();
        }
    });
    $('select',$('.1')).each(function(){

        $(this).remove();
    });
    $('input',$('.1')).each(function() {

        if (this.id.indexOf("Promoter1") == -1)  {
            if (this.id.indexOf("Coding1") == -1) {

                $(this).remove();
            }
                else{
                    $(this).val("");
                    $(this).css({"background-color": 'white'});

            }

        }
        else{
            $(this).val("");
            $(this).css({"background-color": 'white'});
        }
    });
        counter=1;//reset cassette counter to 1
}

function makeName(){
    //main function to generate construct name on the fly
    //it also creates a "stored string" to pass on to the controller to store construct components in order
    //the stored string contains special characters to delimit promoters and coding as wlel as to delimit cassettes.

    $('#add-construct-error').hide();
    var cassette1PromoterString="";
    var cassette1CodingString="";

    var cassette2PromoterString="";
    var cassette2CodingString="";

    var cassette3PromoterString="";
    var cassette3CodingString="";

    var componentDelim="#"

    finalCname=$("#chosenType").val();
    finalCname+=$("#prefix").val()+openP;

    $.each($('.cassette1Promoter'), function (index) {
        cassette1PromoterString +=$(this).val()+componentDelim;

    });

    $.each($('.cassette1Coding'), function (index) {
        cassette1CodingString +=$(this).val()+componentDelim;

    });
    if (cassette1PromoterString==componentDelim){
        constructSeparator=""
            }
    else{
        constructSeparator=":"
    }

    cassette1=((cassette1CodingString!=componentDelim)?cassette1PromoterString+constructSeparator+cassette1CodingString:cassette1PromoterString);
    cassette1Stored=((cassette1CodingString!="")?cassette1PromoterString+storedConstructSeparator+cassette1CodingString:cassette1PromoterString);

    $.each($('.cassette2Promoter'), function (index) {

        cassette2PromoterString +=$(this).val()+componentDelim;
    });
    $.each($('.cassette2Coding'), function (index) {
        cassette2CodingString +=$(this).val()+componentDelim;
    });
    if (cassette2PromoterString==componentDelim){
        constructSeparator=""
    }
    else{
        constructSeparator=":"
    }
    cassette2=(((cassette2CodingString!="")&&(cassette2CodingString!=componentDelim))?cassette2PromoterString+constructSeparator+cassette2CodingString:cassette2PromoterString);
    cassette2Stored=((cassette2CodingString!="")?cassette2PromoterString+storedConstructSeparator+cassette2CodingString:cassette2PromoterString);

    $.each($('.cassette3Promoter'), function (index) {
        cassette3PromoterString +=$(this).val()+componentDelim;
    });
    $.each($('.cassette3Coding'), function (index) {
        cassette3CodingString +=$(this).val()+componentDelim;
    });
    if (cassette3PromoterString==componentDelim){
        constructSeparator=""
    }
    else{
        constructSeparator=":"
    }
    cassette3=(((cassette3CodingString!="")&&(cassette3CodingString!=componentDelim))?cassette3PromoterString+constructSeparator+cassette3CodingString:cassette3PromoterString);
    cassette3Stored=((cassette3CodingString!="")?cassette3PromoterString+storedConstructSeparator+cassette3CodingString:cassette3PromoterString);

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


    $('#constructDisplayName').val(finalName);
    $('#constructName').val(finalName);
    $('#constructStoredName').val(storedName);

}







