/*Javascript code used for update constructs. The general assumption made is that a construct will have 3 cassettes.
 The code can be extended in the future to add more cassettes.
 This code takes care of rendering a construct.It also will save updates if any attribute of a construct is changed.

 */


//    Hide second and third cassette sections of form at load time

$(function () {
    $('#addNewAlias').on('click', function addAlias() {
        var param = jQuery("form").serialize();
        var pubID = jQuery('#constructPublicationZdbID').val();
        var constructID = jQuery('#constructEdit').val();
        console.log(param);


        jQuery.ajax({
            url: '/action/construct/construct-add-alias/',
            type: 'POST',
            data: jQuery("form").serialize(),

            success: function (response) {

                getConstructDetails(constructID);

                jQuery("#constructEditAlias").val("");


            },
            error: function (data) {
                alert('There was a problem with your request: ' + data);
            }

        });
    });

    $('#addNewSequence').on('click', function addSequence() {
        var param = jQuery("form").serialize();
        var pubID = jQuery('#constructPublicationZdbID').val();
        var constructID = jQuery('#constructEdit').val();
        console.log(param);


        jQuery.ajax({
            url: '/action/construct/construct-add-sequence/',
            type: 'POST',
            data: jQuery("form").serialize(),

            success: function (response) {

                getConstructDetails(constructID);

                jQuery("#constructEditSequence").val("");


            },
            error: function (data) {
                alert('There was a problem with your request: ' + data);
            }

        });
    });

    $('#updatePublicNotes').on('click', function updatePNotes() {

        var constructUpdateComments = jQuery("#constructEditComments").val();

        var constructID = jQuery('#constructEdit').val();
        if (constructUpdateComments==""){
            constructUpdateComments="null"
        }



        jQuery.ajax({
            url: "/action/construct/update-comments/" + constructID
                + "/constructEditComments/" + constructUpdateComments,
            type: 'POST',
            //data: param,
            success: function (response) {

                getConstructDetails(constructID);


            },
            error: function (data) {
                alert('There was a problem with your request: ' + data);
            }

        });
    });

    $('#addConstructEditNotes').on('click', function addNotes() {
        var notes = jQuery("#curatorEditNotes").val();
        var constructID = jQuery('#constructEdit').val();
        var pubID = jQuery('#constructPublicationZdbID').val();


        jQuery.ajax({
            url: "/action/construct/add-notes/" + constructID
                + "/notes/" + notes + "/publication/" + pubID,
            type: 'POST',
            //data: param,
            success: function (response) {
                getConstructDetails(constructID);

                jQuery("#curatorEditNotes").val("");


            },
            error: function (data) {
                alert('There was a problem with your request: ' + data);
            }

        });
    });

    $('#constructEdit').on('change', function () {
        getConstructDetails(this.value);
    });
});


function deleteAlias(aliasID) {

    var constructID = jQuery('#constructEdit').val();

    jQuery.ajax(
        {
            url: "/action/construct/delete-alias/" + constructID
                + "/aliasID/" + aliasID,
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

function deleteSequence(sequenceID) {

    var constructID = jQuery('#constructEdit').val();

    jQuery.ajax(
        {
            url: "/action/construct/delete-sequence/" + constructID
                + "/sequenceID/" + sequenceID,
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
            url: "/action/construct/delete-note/" + constructID
                + "/noteID/" + noteID,
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

    jQuery("#constructEditSynonyms").empty();
    jQuery("#constructEditNotes").empty();
    jQuery("#constructEditSequences").empty();
    jQuery("#update-construct-error").hide();


    jQuery.ajax({
        url: '/action/construct/construct-do-update/' + constructID,
//               data: ({constructID:test}),
        type: "GET",
        success: function (data) {


            jQuery.each(data, function (i, constructComponentPresentation) {

                jQuery('#constructEditComments').val(constructComponentPresentation.constructComments);

                if (jQuery(".synonym").length == 0) {
                    jQuery.each(constructComponentPresentation.constructAliases, function () {
                        var zdbId = this.aliasZdbID;
                        $("<div class='synonym'>")
                            .append(
                                $("<img src='/images/delete-button.png' title='Delete Alias.'>")
                                    .on('click', function () { deleteAlias(zdbId); }))
                            .append(' ')
                            .append(this.alias)
                            .appendTo(jQuery('#constructEditSynonyms'));
                    });
                }
                if (jQuery(".privateNote").length == 0) {
                    jQuery.each(constructComponentPresentation.constructCuratorNotes, function () {
                        var zdbId = this.zdbID;
                        $("<div class='privateNote'>")
                            .append(
                                $("<img src='/images/delete-button.png' title='Delete Note.'>")
                                    .on('click', function () { deleteNote(zdbId); }))
                            .append(' ')
                            .append(this.noteData)
                            .appendTo(jQuery('#constructEditNotes'));
                    });
                }
                if (jQuery(".sequence").length == 0) {
                    jQuery.each(constructComponentPresentation.constructSequences, function () {
                        var zdbId = this.zdbID;
                        $("<div class='sequence'>")
                            .append(
                                $("<img src='/images/delete-button.png' title='Delete Sequence.'>")
                                    .on('click', function () { deleteSequence(zdbId); }))
                            .append(' ')
                            .append(this.link)
                            .appendTo(jQuery('#constructEditSequences'));

                    });
                }
            });
        }

    });
}



