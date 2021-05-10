<jsp:useBean id="formBean" class="org.zfin.construct.presentation.ConstructAddBean" scope="request"/>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Construct Curation">
  <h1> Construct Tab (ZDB-PUB-120409-8)</h1>

  <div id="show-hide-all-sections"></div>

  <div class="error"> Please use only for developmental purposes as this will make changes to the database!!!
  </div>
  <p/>

  <link rel="stylesheet" href="${zfn:getAssetPath("jquery-ui.css")}">
  <script src="${zfn:getAssetPath("jquery-ui.js")}"></script>

  <script src="${zfn:getAssetPath("curation.js")}"></script>

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
  <a class="cloneMe" title="Add" href="#">Add cassette
  </a>
  <a class="deleteMe" title="Delete" href="#">Remove cassette </a>
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
      <input type="button" value="DONE" id="submitConstruct" />
      &nbsp;&nbsp;   <input type="button" value="CANCEL" id="resetConstruct" />

    <div class="error" id="add-construct-error" style="display: none;"></div>


  </form:form>
</z:devtoolsPage>