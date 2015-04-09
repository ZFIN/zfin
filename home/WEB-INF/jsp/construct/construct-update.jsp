<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--<%@ page import="org.zfin.properties.ZfinProperties" %>--%>
<%--<%@ page import="ConstructAddBean" %>--%>


<jsp:useBean id="formBean" class="org.zfin.construct.presentation.ConstructUpdateBean" scope="request"/>



<link rel=stylesheet type="text/css" href="/css/jquery-ui-1.8.16.custom.css">

<%--<script type="text/javascript" src="/javascript/jquery-1.10.2.js"></script>--%>


<%--
<script type="text/javascript" src="/javascript/jquery-1.7.2.js"></script>
<script type="text/javascript" src="/javascript/jquery-ui.js"></script>
--%>

<%--<script src="/javascript/jquery-ui-1.8.16.custom.min.js"></script>--%>
<%--<script src="/javascript/jquery-1.11.0.js"></script>
&lt;%&ndash;<script src="/javascript/jquery-ui-1.10.4.custom.js"></script>&ndash;%&gt;
<script type="text/javascript" src="/javascript/jquery-ui.js"></script>--%>
<%--<script src="/javascript/jquery-1.11.0.js"></script>
<script src="/javascript/jquery-ui-1.10.4.custom.js"></script>
<link rel=stylesheet type="text/css" href="/css/jquery-ui-1.10.4.custom.css">--%>
<script src="/javascript/constructUpdate.js" type="text/javascript"></script>



<%--<link rel=stylesheet type="text/css" href="/css/tabEdit.css">--%>








<link media="all" type="text/css" href="/css/constructUpdate.css" rel="stylesheet">

<form:form commandName="formBean" id="thisform" style="background-color:#EEEEEE;">

<%--<label>Add a new Construct</label>--%>
<table>



       <%-- <form:select path="attribution"
                     onchange="changeDefPubFromSelectionBox(this.value)"
                     id="curatorPubs">
            <form:options items="${formBean.defPubList}"/> <option value="">--</option>
        </form:select>--%>

   <tr> <td><b>Construct:</b>
     <td>
         <form:select path="constructEdit" onchange="getConstructDetails(this.value)">
             <%--<form:select path="constructEdit" onchange="getDetails(this.value)">--%>
         <option value="">--</option>
           <form:options items="${formBean.constructsInPub}" itemLabel="name" itemValue="zdbID"/>
       </form:select>


    <form:hidden name="constructPublicationZdbID" path="constructPublicationZdbID" value="${formBean.constructPublicationZdbID}" id = "constructPublicationZdbID"/>
           <div id=construct-detail>
<tr>
<td><b>Construct Type</b></td>
<td>
<select id="chosenType" name="chosenType">
<%--<option value="">Choose Type</option>--%>
<option value="Tg">Tg</option>
<option value="Et">Et</option>
<option value="Gt">Gt</option>
<option value="Pt">Pt</option>
</select>


    &nbsp;&nbsp;
        <label for="prefix"><b>Prefix:</b></label>

    &nbsp;&nbsp;

        <input id="prefix" size="15" class="prefix" name="prefix" autocomplete="off">

</td>

</tr>
<%--<tr>
<td>
<label for="prefix"><b>Prefix:</b></label>
</td>
<td>

<input id="prefix" size="15">
</td>
</tr>--%>
<tr>
<td><b>Synonym</b>:</td>
               <td><div id ="constructSynonyms"></div></td>

               <%--<td><form:input id="constructSynonym" name="constructSynonym" value="" path="constructSynonym" size="50"/>
        <form:errors path="constructSynonym"  cssClass="error"/>--%>
    <tr><td></td>

               <td><input  id="constructAlias" name="constructAlias" autocomplete="off" value="" type="text" size=50/>
               &nbsp;&nbsp;<a onclick="addAlias(); return false;" href="#"><img height= 10 src="/images/plus.png"></a></td><tr>

<tr>
<td><b>Public Note</b>:</td><td>
        <%--<form:textarea path="<%= ConstructAddBean.NEW_CONSTRUCT_COMMENT%>" rows="3" cols="50" />
        <form:errors path="<%= ConstructAddBean.NEW_CONSTRUCT_COMMENT%>" cssClass="error indented-error"/>&ndash;%&gt;--%>
    <textarea id="constructComments" name="constructComments" value="" rows="3" cols="50" ></textarea>&nbsp;&nbsp;<input type="button" value="Save" id="updatePublicNotes"  onClick=updatePublicNotes(); />
</td>
               </tr>
               </tr>

<tr>
               <td>

<b>Curator Note</b>: </td>
               <td>&nbsp;<div id="constructNotes">
           <%--<tr><td></td><td><textarea id="constructPrivateNotes" name="constructPrivateNotes" value="" rows="3" cols="50" ></textarea>--%>
           </tr></div>
               <tr><td></td><td><textarea id="curatorNotes" name="constructComments" value="" rows="3" cols="50" ></textarea> &nbsp;&nbsp;<a onclick="addNotes(); return false;" href="#"><img height= 10 src="/images/plus.png"></a></td><tr>
</tr>
</table>
<div id="constructSynonym"> </div>

<div id="displayName">

    <%--Construct Display Name: <input id="name" name="name" value="" type="text" size=150 disabled font color="red">--%>
    <%--&nbsp;<b>Display Name:</b>  &nbsp;<form:input id="constructName" name="constructName" value="" path="constructName" size="150"/>--%>
       <%-- &nbsp;<b>Display Name:</b>  &nbsp;
        <form:input id="constructDisplayName" name="constructDisplayName" value="" path="constructDisplayName" size="150" disabled="true" style="color:red;"/>
        &lt;%&ndash;//<input id="constructName" name="constructName" value="" size="150" disabled style="color:red;"/>&ndash;%&gt;
        <form:errors path="constructDisplayName"  cssClass="error"/>
        <form:hidden name="constructStoredName" path="constructStoredName"/>
        <form:hidden name="constructName" path="constructName"/>--%>
            <%--<form:errors path="constructName"  cssClass="error"/>--%>



    <br>
    <%--<input type="button" value="PREVIEW" id="previewConstruct" disabled/>--%>
    <%--<input type="submit" value="DONE" id="submitConstruct" onClick=formBean.submit(); />--%>
        <%--<input type="button" value="DONE" id="submitConstruct"  onClick=updateConstruct(); />--%>


</div>
    <div class="error" id="update-construct-error" style="display: none;"></div>

    <div id="constructTest"></div>
    <input name="constructCassettes" id="constructCassettes" type="hidden">


&nbsp;<b>Display Name:</b>  &nbsp;
<form:input id="constructDisplayName" name="constructDisplayName" value="" path="constructDisplayName" size="150" disabled="true"/>
<p>
        <%--//<input id="constructName" name="constructName" value="" size="150" disabled style="color:red;"/>--%>
        <form:errors path="constructDisplayName"  cssClass="error"/>
        <form:hidden name="constructStoredName" path="constructStoredName"/>
        <form:hidden name="constructName" path="constructName"/>
    <input type="button" value="DONE" id="submitConstruct"  onClick=updateConstruct(); />
<!-- /Embeded sheepIt Form -->




  <%--//  <input type="button" value="No Promoter/Coding" id="noRel" />--%>


<fieldset>
<span class="tab">

<a class="cloneMe" title="Add" onclick="cloneMe(this); return false;" href="#">Add cassette

</a>
<a class="deleteMe" title="Delete" onclick="deleteMe(this); return false;" href="#">Remove cassette </a>

</span>



    <b>
        Promoter
    </b>
    <div id="update_promoterCassette1" class="1">
        <input id="update_cassette1Promoter1" class="update_cassette1Promoter" size="10" />
        <button id="update_addPromoter1" class="1">+</button>
        <button id="update_delPromoter1" class="1">-</button>
    </div>

    <b>Coding</b>
    <div id="update_codingCassette1"  class="1">
        <input id="update_cassette1Coding1"  class="update_cassette1Coding" size="10"/>
        <button id="update_addCoding1" class="1">+</button>
        <button id="update_delCoding1" class="1">-</button>
    </div>

</fieldset>
</div>

   <fieldset  class="duplicate" id="newCassette1">
<span class="tab">

<a class="cloneMe" title="Add" onclick="cloneMe(this); return false;" href="#">Add cassette&ndash;%&gt;

</a>
<a class="deleteMe" title="Delete" onclick="deleteMe(this); return false;" href="#">Remove cassette </a>
    <a class="cloneMe" title="Delete" onclick="cloneMe(this); return false;" href="#">Add cassette </a>

</span>
        <b>
            Promoter
        </b>

        <div id="update_promoterCassette2" class="2" >


        </div>

        <b>Coding</b>
        <div id="update_codingCassette2" class="2" >



        </div>

    </fieldset>
    <fieldset  class="duplicate" id="newCassette2">
<span class="tab">

<a class="cloneMe" title="Add" onclick="cloneMe(this); return false;" href="#">Add cassette&ndash;%&gt;

</a>
<a class="deleteMe" title="Delete" onclick="deleteMe(this); return false;" href="#">Remove cassette </a>

</span>

        <b>
            Promoter
        </b>

        <div id="update_promoterCassette3" class="3">

        </div>

        <b>Coding</b>
        <div id="update_codingCassette3" class="3">

        </div>


    <%--   <b>
            Promoter
        </b>

        <div id="promoterTest2" class="promoter-group">
        <span class="promoterControl">
        &lt;%&ndash;<input id="promoter2" name="promoter2" class="promoter1" size="10"/>
    <button class = "2" id="addPromoter2">+</button>
    <button id="delPromoter3">-</button>&ndash;%&gt;
         </span>
        </div>

        <b>Coding</b>
        <div id="codingTest2"  class="coding-group">
     <span class="codingControl">
&lt;%&ndash;    <input id="coding2" name="coding2" class="coding1" size="10"/>
    <button class = "2" id="addCoding2">+</button>
    <button class = "2" id="delCoding2">-</button>&ndash;%&gt;
         </span>
        </div>

    </fieldset>
<fieldset  class="duplicate">
<span class="tab">

<a class="cloneMe" title="Add" onclick="cloneMe(this); return false;" href="#">Add cassette

</a>
<a class="deleteMe" title="Delete" onclick="deleteMe(this); return false;" href="#">Remove cassette </a>

</span>

    <b>
        Promoter
    </b>

    <div id="promoterTest3" class="promoter-group">
     <span class="promoterControl">
         </span>
    </div>

    <b>Coding</b>
    <div id="codingTest3" class="coding-group">
     <span class="codingControl">

         </span>
    </div>
--%>
</fieldset>

<%--</div>--%>
<%--&nbsp;<b>Display Name:</b>  &nbsp;
<form:input id="constructDisplayName" name="constructDisplayName" value="" path="constructDisplayName" size="150" disabled="true"/>
<p>
&lt;%&ndash;//<input id="constructName" name="constructName" value="" size="150" disabled style="color:red;"/>&ndash;%&gt;
<form:errors path="constructDisplayName"  cssClass="error"/>
<form:hidden name="constructStoredName" path="constructStoredName"/>
<form:hidden name="constructName" path="constructName"/>
<input type="button" value="DONE" id="submitConstruct"  onClick=updateConstruct(); />--%>





</form:form>
