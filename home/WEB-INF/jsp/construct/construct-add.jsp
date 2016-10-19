<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<jsp:useBean id="formBean" class="org.zfin.construct.presentation.ConstructAddBean" scope="request"/>


<link rel=stylesheet type="text/css" href="/css/tabEdit.css">
<link rel="stylesheet" type="text/css" href="/css/jquery-ui-1.10.4.custom.css"/>
<script type="text/javascript" src="/javascript/jquery-ui-1.10.4.custom.js"></script>
<script src="/javascript/constructAdd.js" type="text/javascript"></script>
<link media="all" type="text/css" href="/css/constructAdd.css" rel="stylesheet">

<form:form commandName="formBean" id="constructadd" style="background-color:#EEEEEE;">


    <table>
        <form:hidden name="constructPublicationZdbID" path="constructPublicationZdbID"
                     value="${formBean.constructPublicationZdbID}" id="constructPublicationZdbID"/>
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
            <td><b>Synonym</b>:</td>
            <td><input id="constructAlias" name="constructAlias" autocomplete="off" value="" type="text" size=50/>

        <tr>
            <td><b>Sequence</b>:</td>
            <td><input id="constructSequence" name="constructSequence" autocomplete="off" value="" type="text" size=50/>

        <tr>
            <td><b>Public Note</b>:</td>
            <td>
                <textarea id="constructComments" name="constructComments" value="" rows="3" cols="50"></textarea>
            <td><b>Curator Note</b>:</td>
            <td>
                <textarea id="constructCuratorNote" name="constructCuratorNote" value="" rows="3" cols="50"></textarea>

    </table>

    <%--<div class="error" id="add-construct-error" style="display: none;"></div>--%>
    <span></span>
    </div>


    <div id="displayName">
    </div>

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
                <input id="cassette1Promoter1" class="cassette1Promoter" size="10"/>
                <button id="addPromoter1" class="1">+</button>
                <button id="delPromoter1" class="1">-</button>
            </div>

            <b>Coding</b>

            <div id="codingCassette1" class="1">
                <input id="cassette1Coding1" class="cassette1Coding" size="10"/>
                <button id="addCoding1" class="1">+</button>
                <button id="delCoding1" class="1">-</button>
            </div>
        </fieldset>
    </div>

    <p>
        &nbsp;<b>Display Name:</b> &nbsp;
            <form:input id="constructDisplayName" name="constructDisplayName" value="" path="constructDisplayName"
                        size="150" disabled="true"/>

    <p>

            <form:errors path="constructDisplayName" cssClass="error"/>
            <form:hidden name="constructStoredName" path="constructStoredName"/>

            <form:hidden name="constructName" path="constructName"/>
        <input type="button" value="DONE" id="submitConstruct" onClick="validateAndSubmit();"/>
        &nbsp;&nbsp; <input type="button" value="CANCEL" id="resetConstruct" onClick="resetFields();"/>

    <div class="error" id="add-construct-error" style="display: none;"></div>


</form:form>



