<%@ tag import="org.zfin.gwt.root.ui.LookupComposite" %>
<%@ tag import="org.zfin.ontology.Ontology" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@attribute name="title" type="java.lang.String" required="true" %>
<%@attribute name="criteria" type="org.zfin.expression.presentation.ExpressionSearchCriteria" required="true" %>

<style>
    /* overriding, see case 9050 */
    input#searchTerm.error {
        color: black;
    }

    .form-group {
        margin-bottom: 0.5em;
    }
</style>
<link rel="stylesheet" type="text/css" href="/css/Lookup.css" />

<div class="titlebar">
    <h1>${title}</h1>
    <a href="/ZFIN/misc_html/xpatselect_search_tips.html" class="popup-link help-popup-link"
       id="xpatsel_expression_tips" rel="#searchtips"></a>

    <span class="yourinputwelcome" nowrap="nowrap">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Gene expression search"/>
        </tiles:insertTemplate>
    </span>
</div>

<form:form action="/action/expression/results" id="expression-search-form" method="get" modelAttribute="criteria">
    <form:hidden path="rows" />
    <table width="100%">
        <tr valign="top">
            <td width="50%">
                <table class="primary-entity-attributes">
                    <tr>
                        <th><form:label path="geneField" cssClass="namesearchLabel">Gene/EST</form:label></th>
                        <td><form:input type="text" path="geneField" /></td>
                    </tr>
                    <tr>
                        <th><form:label path="fish">Fish</form:label></th>
                        <td><form:input type="text" path="fish" /></td>
                    </tr>
                    <tr>
                        <th><form:label path="targetGeneField" cssClass="namesearchLabel">Target Gene</form:label></th>
                        <td><form:input type="text" path="targetGeneField" /></td>
                    </tr>
                    <tr>
                        <th><form:label path="authorField" cssClass="namesearchLabel">Author</form:label></th>
                        <td><form:input type="text" path="authorField" /></td>
                    </tr>

                </table>
                <table border="0" bgcolor="#EEEEEE">
                    <tr>
                        <td><form:label path="anatomy" cssClass="namesearchLabel">Anatomy Terms</form:label></td>
                    </tr>
                    <tr>
                        <td>
                            <form:hidden path="anatomyTermIDs" />
                            <form:hidden path="anatomyTermNames" />
                            <div id="searchTermList"></div>
                            <div id="anatomyTermInput"></div>
                        </td>
                    </tr>
                </table>
                <script type="text/javascript">
                  var LookupProperties = {
                    inputDiv: "anatomyTermInput",
                    termListDiv: "searchTermList",
                    inputName: "searchTerm",
                    showError: true,
                    <c:if test='${criteria.anatomyTermNames != null}' >
                    previousTableValues: "${criteria.anatomyTermNames}",
                    </c:if>
                    hiddenNames: "anatomyTermNames",
                    hiddenIds: "anatomyTermIDs",
                    type: "<%= LookupComposite.GDAG_TERM_LOOKUP %>",
                    ontologyName: "<%= Ontology.ANATOMY %>",
                    action: "<%= LookupComposite.ACTION_ANATOMY_SEARCH %>",
                    width: 40,
                    wildcard: false,
                    termsWithDataOnly: true,
                    useTermTable: true
                  };
                  $('#anatomyTermInput').on('keyup keypress', function (e) {
                    if (e.which === 13) {
                      e.preventDefault();
                      return false;
                    }
                  });
                </script>
                <script language="javascript"
                        src="/gwt/org.zfin.gwt.lookup.Lookup/org.zfin.gwt.lookup.Lookup.nocache.js" type=""></script>
            </td>
            <td width="50%">
                <div class="form-group">
                    <span class="namesearchLabel">Between stages</span>
                    <div>
                        <form:select path="startStageId" items="${stages}" />
                        &nbsp;<span class="namesearchLabel">&</span>
                    </div>
                    <div>
                        <form:select path="endStageId" items="${stages}" />
                    </div>
                    <div>
                        <a href="/zf_info/zfbook/stages/index.html">Developmental Staging Series</a>
                    </div>
                </div>
                <div class="form-group">
                    <form:label path="assayName" cssClass="namesearchLabel">Assay</form:label>
                    <form:select path="assayName">
                        <form:option value="">Any</form:option>
                        <form:options items="${assays}" itemLabel="name" itemValue="name" />
                    </form:select>
                </div>
                <div class="form-group">
                    <div><form:checkbox path="onlyWildtype" id="onlyWildtype" /> <label for="onlyWildtype">Show only WT
                        expression</label></div>
                    <div><form:checkbox path="onlyReporter" id="onlyReporter" /> <label for="onlyReporter">Show only
                        reporter genes in transgenic fish</label></div>
                    <div><form:checkbox path="onlyFiguresWithImages" id="onlyFiguresWithImages" /> <label
                            for="onlyFiguresWithImages">Show only figures with images</label></div>
                </div>
                <div class="form-group">
                    <c:forEach items="${journalTypeOptions}" var="type">
                        <div><form:radiobutton path="journalType" value="${type}" label="${type.label}" /></div>
                    </c:forEach>
                </div>
            </td>
        </tr>
    </table>

    <div class="submitbar">
        <button type="submit">Search</button>
        <button type="reset">Reset</button>
    </div>
</form:form>

<script>
  $(function () {
    function disableInputsOnCheckboxEvent (checkbox, message, fields) {
      checkbox
        .change(function (evt) {
          evt.preventDefault();
          var checked = $(this).is(':checked');
          var labelColor = checked ? '#777' : '';
          var labelTitle = checked ? message : '';
          $.each(fields, function (idx, field) {
            $('#' + field).prop('disabled', checked);
            $('[for=' + field + ']')
              .css('color', labelColor)
              .prop('title', labelTitle);
          });
        })
        .trigger('change');
    }

    var $form = $('#expression-search-form');
    $form.find(':reset').click(function (evt) {
      evt.preventDefault();
      $form.resetForm({
        endStageId: function () { $(this).find('option:last').prop('selected', true); },
        journalType3: function () { $(this).prop('checked', true); }
      });
      clearTable();
    });

    disableInputsOnCheckboxEvent(
      $('#onlyWildtype'),
      'This option is not compatible with WT only searches',
      ['fish', 'targetGeneField', 'onlyReporter']
    );
    disableInputsOnCheckboxEvent(
      $('#onlyReporter'),
      'This option is not compatible with reporter gene searches',
      ['geneField', 'onlyWildtype']
    );

    $('input[name=anatomyTermIDs]').change(function () {
      var termCount = $('#searchTermList .gwt-Hyperlink').size();
      if (termCount === 0) {
        $('#searchTermList').hide();
        $('#term-list-remove-all-link').hide();
      } else if (termCount === 1) {
        $('#searchTermList').show();
        $('#term-list-remove-all-link').hide();
      } else {
        $('#searchTermList').show();
        $('#term-list-remove-all-link').show();
      }
    });
  });
</script>
