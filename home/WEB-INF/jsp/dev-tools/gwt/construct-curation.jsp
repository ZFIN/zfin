<%@ taglib prefix="form" uri="http://www.springframework.org/tags/form" %>
<%@ page import="org.zfin.properties.ZfinPropertiesEnum" %>
<%@ page contentType="text/html;charset=UTF-8" language="java" %>
<%@ page import="org.zfin.construct.presentation.ConstructAddBean" %>

<jsp:useBean id="formBean" class="org.zfin.construct.presentation.ConstructAddBean" scope="request"/>

<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<html>
<head>
  <meta http-equiv="Content-Type" content="text/html; charset=UTF-8"/>
  <meta http-equiv="Pragma" content="no-cache"/>
  <meta http-equiv="Cache-Control" content="no-cache"/>
  <meta http-equiv="Expires" content="0"/>
  <title>
    GWT Modules</title>

  <link href="/css/font-awesome.min.css" rel="stylesheet">

  <link rel="stylesheet" type="text/css" href="/css/zfin.css">
  <link rel="stylesheet" type="text/css" href="/css/header.css">
  <link rel="stylesheet" type="text/css" href="/css/footer.css">
  <link rel=stylesheet type="text/css" href="/css/searchresults.css">
  <link rel=stylesheet type="text/css" href="/css/summary.css">
  <link rel=stylesheet type="text/css" href="/css/Lookup.css">
  <link rel=stylesheet type="text/css" href="/css/datapage.css">
  <link rel=stylesheet type="text/css" href="/css/popup.css">
  <link rel=stylesheet type="text/css" href="/css/tipsy.css">
  <link rel=stylesheet type="text/css" href="/css/jquery.modal.css">
  <link rel=stylesheet type="text/css" href="/css/typeahead.css">


  <script src="/javascript/jquery-1.11.1.min.js" type="text/javascript"></script>
  <script src="/javascript/header.js" type="text/javascript"></script>
  <script type="text/javascript" src="/javascript/jquery.modal.min.js"></script>
  <script type="text/javascript" src="/javascript/jquery.tipsy.js"></script>
  <script type="text/javascript" src="/javascript/sorttable.js"></script>

  <script src="/javascript/autocompletify.js"></script>
  <script type="text/javascript" src="/javascript/typeahead.bundle.min.js"></script>

</head>
<%--<script type="text/javascript" language="javascript"
        src="/gwt/org.zfin.gwt.curation.Curation/org.zfin.gwt.curation.Curation.nocache.js"></script>--%>
<h1> Construct Tab (ZDB-PUB-120409-8)</h1>

<div id="show-hide-all-sections"></div>

<div class="error"> Please use only for developmental purposes as this will make changes to the database!!!
</div>
<p/>

<link rel=stylesheet type="text/css" href="/css/tabEdit.css">
<link rel="stylesheet" type="text/css" href="/css/jquery-ui-1.10.4.custom.css"/>
<script type="text/javascript" src="/javascript/jquery-ui-1.10.4.custom.js"></script>
<script src="/javascript/constructAdd.js" type="text/javascript"></script>
<link media="all" type="text/css" href="/css/constructAdd.css" rel="stylesheet">

<form:form commandName="formBean" id="constructadd" style="background-color:#EEEEEE;">



  <table>
  <form:hidden name="constructPublicationZdbID" path="constructPublicationZdbID" value="ZDB-PUB-120409-8" id = "constructPublicationZdbID"/>
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
  <td><b>Sequence</b>:</td><td><input  id="constructSequence" name="constructSequence" autocomplete="off" value="" type="text" size=50/>

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