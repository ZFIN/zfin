var listMembers = function(labZdbID) {
    jQuery.ajax(
        {
            url: "/action/profile/list-members/"+labZdbID,
            type: "GET",
            success: function(data) {
                var contactPerson = jQuery('#contact-person option:selected').val();
                jQuery('#contact-person').empty();
                jQuery('#contact-person').append(jQuery('<option></option>').attr("selected","true").attr("value", "none").text("-- Select Contact Person --"));
                jQuery('#change-position-members').empty();
                jQuery('#change-position-members').append(jQuery('<option></option>').attr("selected","true").attr("value", "none").text("-- Select Member --"));
                jQuery('#change-position-members').val('none').attr('selected',true);
                $.each(data, function (idx, person) {
                    var deleteIcon = $("<img id='member-delete-button-" + person.zdbID + "' class='clickable' src='/images/delete-button.png' title='Remove person from lab.'>")
                        .on('click', function () {
                            removeMember(person.zdbID, labZdbID);
                        });
                    $("<div>")
                        .append(deleteIcon)
                        .append(' ')
                        .append("<a href='/action/profile/view/" + person.zdbID + "'>" + person.name + "</a> " +
                            person.positionString +
                            "</div>")
                        .appendTo(jQuery('#memberList'));
                    var option = jQuery('<option></option>').attr("value", person.zdbID).text(person.name);
                    jQuery('#contact-person').append(jQuery('<option></option>').attr("value", person.zdbID).text(person.name));
                    jQuery('#change-position-members').append(jQuery('<option></option>').attr("value", person.zdbID).text(person.name));

                });
                jQuery('#contact-person').val(contactPerson).attr('selected',true);
            },
            error: function(data) {
                alert('There was a problem with your request: ' + data);
            }
        }
    );
};

var changePosition = function(personZdbID, organizationZdbID, position) {
  jQuery.ajax( {
          url: "/action/profile/change-position/" + personZdbID
              + "/organization/" + organizationZdbID
              + "/position/" + position,
          type:"POST",
          success:function(data) {
              jQuery('#memberList').html('');
              listMembers(organizationZdbID);
          },
          error: function(data) {
              alert('There was a problem with your request: ' + data);
          }
      }
  );
};


var removeMember = function(personZdbID, organizationZdbID) {
    jQuery.ajax(
        {
            url: "/action/profile/delete-member/" + personZdbID + "/organization/" + organizationZdbID,
            type: "DELETE",
            success: function(data) {
//                                            jQuery('#member-delete-button-'+personZdbID+"'").html('') ;
                jQuery('#memberList').html('');
                listMembers(organizationZdbID);
            },
            error: function(data) {
                alert('There was a problem with your request: ' + data);
            }
        }
    );
};

var addMember = function(personZdbID, organizationZdbID, position, name) {
    if (position === null || position === 'none') {
        alert('Please select a position.');
        return true;
    }
    jQuery.ajax(
        {
            url: "/action/profile/add-member/" + personZdbID + "/organization/" + organizationZdbID + "/position/" + position + "/name/" + name,
            type: "POST",
            success: function(data) {
                if (data != "") {
                    jQuery('#add-member-error').html(data);
                    jQuery('#add-member-error').show();
                } else {
                    jQuery('#add-member-error').hide();
                    jQuery('#memberList').html('');
                    listMembers(organizationZdbID);
                    jQuery('#addMemberBox').val('');
                    jQuery('.no-member-error').hide();
                    jQuery('#members-tab').css("color","black");


                }
/*
                if(data===false){
                    alert('There was a problem adding this person to this organization.');
                    return false ;
                }
*/
            },
            error: function(data) {
                alert('There was a problem with your request: ' + data);
            }
        }
    );
};

var generatePassword = function(destinationClass) {
    jQuery.ajax(
        {
            url:'https://www.random.org/passwords/?num=1&len=8&format=plain&rnd=new',
            dataType:'text',
            type:"GET",
            success: function(password) {
              jQuery('input.' + destinationClass).val(password);
              jQuery('span.' + destinationClass).text(password);
            },
            error: function(response) {
                alert("The password generating website didn't respond properly: " + response);
            }
        }
    );

};

var personToAddZdbID;
var personToAddPosition = null;


$(document).ready(function () {
    var orgId = $('#orgZdbID').val();
    $('#addMemberBox')
        .autocompletify('/action/profile/find-member?term=%QUERY')
        .bind('typeahead:select', function(obj, datum, name) {
            personToAddZdbID = datum.id;
        });
    if ($('#memberList').length !== 0) {
        listMembers(orgId);
    }
    $('#addMemberButton').on('click', function () {
        addMember( personToAddZdbID, orgId , $('#addMemberPosition').val(), $('#addMemberBox').val());
    });
    $('#change-position-button').on('click', function () {
        changePosition($('#change-position-members option:selected').val(),
            orgId,
            $('#change-position-positions option:selected').val());
    });
    $('#generate-password-button').click( function() { generatePassword('fill-with-generated-password'); });
});