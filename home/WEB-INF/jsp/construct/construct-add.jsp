<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%--<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>--%>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%--<%@ page import="org.zfin.properties.ZfinProperties" %>--%>
<%@ page import="org.zfin.construct.presentation.ConstructAddBean" %>

<jsp:useBean id="formBean" class="org.zfin.construct.presentation.ConstructAddBean" scope="request"/>
<%--<link rel=stylesheet type="text/css" href="/css/jquery-ui-1.8.16.custom.css">--%>
<%--<script type="text/javascript" src="/javascript/jquery-1.7.2.js"></script>--%>
<%--<script type="text/javascript" src="/javascript/jquery-1.10.2.js"></script>--%>
<%--<script type="text/javascript" src="/javascript/jquery-ui.js"></script>--%>


<%--
<script src="/javascript/jquery-ui-1.10.4.custom.js"></script>
<link rel=stylesheet type="text/css" href="/css/jquery-ui-1.10.4.custom.css">

&lt;%&ndash;<script src="/javascript/jquery-ui-1.8.16.custom.min.js"></script>&ndash;%&gt;
<script src="/javascript/jquery-1.11.1.min.js" type="text/javascript"></script>
<script src="/javascript/constructAdd.js" type="text/javascript"></script>

<script type="text/javascript" src="/javascript/jquery-ui.js"></script>
--%>

<link rel=stylesheet type="text/css" href="/css/tabEdit.css">
<link rel="stylesheet" type="text/css" href="/css/jquery-ui-1.10.4.custom.css"/>
<script type="text/javascript" src="/javascript/jquery-ui-1.10.4.custom.js"></script>
<script src="/javascript/constructAdd.js" type="text/javascript"></script>
<link media="all" type="text/css" href="/css/constructAdd.css" rel="stylesheet">

<form:form commandName="formBean" id="constructadd" style="background-color:#EEEEEE;">



<table>
 <form:hidden name="constructPublicationZdbID" path="constructPublicationZdbID" value="${formBean.constructPublicationZdbID}" id = "constructPublicationZdbID"/>
<tr>
<td><b>Construct Type</b></td>
<td>
<select id="chosenType" name="chosenType">
<option value="Tg">Tg</option>
<option value="Et">Et</option>
<option value="Gt">Gt</option>
<option value="Pt">Pt</option>
</select>
<label for="prefix"><b>Prefix:</b></label>
<input id="prefix" size="15" class="prefix" name="prefix">
</td>
</tr>
<tr>
<td><b>Synonym</b>:</td><td><input  id="constructAlias" name="constructAlias" autocomplete="off" value="" type="text" size=50/>

<tr>
<td><b>Public Note</b>:</td><td>
    <textarea id="constructComments" name="constructComments" value="" rows="3" cols="50" ></textarea>
    <td><b>Curator Note</b>:</td><td>
    <textarea id="constructCuratorNote" name="constructCuratorNote" value="" rows="3" cols="50" ></textarea>

</table>

    <%--<div class="error" id="add-construct-error" style="display: none;"></div>--%>
    <span></span>
</div>


<div id="displayName">
</div>
    <input name="cassette1Promoter" type="hidden"/>
    <input name="cassette1Coding" type="hidden"/>
    <input name="constructWrapperString" type="hidden"/>
<%--Area for cassettes--%>
    <div id="cassette1" class="clonable">
    <fieldset>
     <span class="tab">
     <a class="cloneMe" title="Add" onclick="cloneMe(this); return false;" href="#">Add cassette
     </a>
     <a class="deleteMe" title="Delete" onclick="deleteMe(this); return false;" href="#">Remove cassette </a>
     </span>

    <b>
        Promoter
    </b>
    <div id="promoterCassette1" class="1">
      <input id="cassette1Promoter1" class="cassette1Promoter" size="10" />
      <button id="addPromoter1" class="1">+</button>
      <button id="delPromoter1" class="1">-</button>
    </div>

    <b>Coding</b>
    <div id="codingCassette1"  class="1">
        <input id="cassette1Coding1"  class="cassette1Coding" size="10"/>
        <button id="addCoding1" class="1">+</button>
        <button id="delCoding1" class="1">-</button>
    </div>
    </fieldset></div>

    <p>
       &nbsp;<b>Display Name:</b>  &nbsp;
            <form:input id="constructDisplayName" name="constructDisplayName" value="" path="constructDisplayName" size="150" disabled="true"/>
    <p>

            <form:errors path="constructDisplayName"  cssClass="error"/>
            <form:hidden name="constructStoredName" path="constructStoredName"/>
            <form:hidden name="constructName" path="constructName"/>
    <input type="button" value="DONE" id="submitConstruct" onClick=validateAndSubmit(); />
    &nbsp;&nbsp;   <input type="button" value="CANCEL" id="resetConstruct" onClick=resetFields(); />

    <div class="error" id="add-construct-error" style="display: none;"></div>


</form:form>



