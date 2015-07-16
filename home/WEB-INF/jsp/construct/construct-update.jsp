<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--<%@ page import="org.zfin.properties.ZfinProperties" %>--%>
<%--<%@ page import="ConstructAddBean" %>--%>


<jsp:useBean id="formBean" class="org.zfin.construct.presentation.ConstructUpdateBean" scope="request"/>









<%--<link rel=stylesheet type="text/css" href="/css/tabEdit.css">--%>
<link rel=stylesheet type="text/css" href="/css/tabEdit.css">
<link rel="stylesheet" type="text/css" href="/css/jquery-ui-1.10.4.custom.css"/>
<script type="text/javascript" src="/javascript/jquery-ui-1.10.4.custom.js"></script>
<script src="/javascript/constructUpdate.js" type="text/javascript"></script>
<link media="all" type="text/css" href="/css/constructUpdate.css" rel="stylesheet">









<form:form commandName="formBean" id="thisform" style="background-color:#EEEEEE;">


<table>





   <tr> <td><b>Construct:</b>
     <td>
         <form:select path="constructEdit" onchange="getConstructDetails(this.value)">

         <option value="">--</option>
           <form:options items="${formBean.constructsInPub}" itemLabel="name" itemValue="zdbID"/>
       </form:select>


    <form:hidden name="constructPublicationZdbID" path="constructPublicationZdbID" value="${formBean.constructPublicationZdbID}" id = "constructPublicationZdbID"/>

           <div id=construct-detail>

<tr>
<td><b>Synonym</b>:</td>
               <td><div id ="constructEditSynonyms"></div></td>

    <tr><td></td>

               <td><input  id="constructEditAlias" name="constructEditAlias" autocomplete="off" value="" type="text" size=50/>
               &nbsp;&nbsp;<input type="button" value="Add" id="addNewAlias"  onClick=addAlias(); /></td><tr>

<tr>
               <tr>
                   <td><b>Sequence</b>:</td>
                   <td><div id ="constructEditSequences"></div></td>

               <tr>

                   <td><input  id="constructEditSequence" name="constructEditSequence" autocomplete="off" value="" type="text" size=50/>
                       &nbsp;&nbsp; <input type="button" value="Add" id="addNewSequence"  onClick=addSequence(); /></td></tr>

               <tr>
<td><b>Public Note</b>:</td><td>

    <textarea id="constructEditComments" name="constructEditComments" value="" rows="3" cols="50" ></textarea>&nbsp;&nbsp;

            <input type="button" value="Save" id="updatePublicNotes"  onClick=updatePNotes(); />
</td>
               </tr>


<tr>
               <td>

<b>Curator Note</b>: </td>
               <td>&nbsp;<div id="constructEditNotes">

           </tr></div>
               <tr><td></td><td><textarea id="curatorEditNotes" name="curatorEditNotes" value="" rows="3" cols="50" ></textarea> &nbsp;&nbsp; <input type="button" value="Add" id="addConstructEditNotes"  onClick=addNotes(); /></td><tr>
</tr>
</table>
<div id="constructSynonym"> </div>

    <div class="error" id="update-construct-error" style="display: none;"></div>



</form:form>
<script type="text/javascript">
    function updatePNotes() {

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


    }

    </script>