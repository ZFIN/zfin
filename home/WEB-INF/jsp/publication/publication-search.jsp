<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.publication.presentation.PublicationSearchBean" scope="request"/>

<c:if test="${!formBean.isEmpty()}">
    <div class="pub-export-controls">
        <input type="button" value="Format into a printable listing" id="pub-printable-results">
        <input type="button" value="Output as REFER format file" id="pub-refer-results">
        <a href="/ZFIN/misc_html/refer_info.html" class="popup-link help-popup-link"></a>
    </div>
    <table class="pub-search-results">
        <caption>
            Publication Search Results<br>
            (<b>${formBean.totalRecords}</b> records found)
        </caption>
        <tbody>
        <c:forEach items="${formBean.results}" var="pub">
            <tr>
                <td>
                    <div class="show_pubs">
                        <a href="/${pub.zdbID}"
                           id="${pub.zdbID}">${pub.citation}</a>
                        <authz:authorize access="hasRole('root')"><c:if
                                test="${pub.open}">OPEN</c:if><c:if
                                test="${!pub.open}">CLOSED</c:if><c:if
                                test="${pub.indexed}">, INDEXED</c:if>
                        </authz:authorize>
                    </div>
                </td>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <zfin2:pagination paginationBean="${formBean}" />
</c:if>

<div class="titlebar">
    <h1>${formBean.isEmpty() ? "Search for Publications" : "Modify your search"}</h1>
    <span class="yourinputwelcome">
        <tiles:insertTemplate template="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
            <tiles:putAttribute name="subjectName" value="Publication search"/>
        </tiles:insertTemplate>
    </span>
</div>

<form:form method="get" modelAttribute="formBean" id="pub-search-form">
    <table width="100%">
        <tr valign="top">
            <td width="50%">
                <table class="primary-entity-attributes">
                    <tr>
                        <th><form:label path="author" cssClass="namesearchLabel">Author</form:label></th>
                        <td><form:input type="text" path="author"/></td>
                    </tr>
                    <tr>
                        <th><form:label path="title">Title</form:label></th>
                        <td><form:input type="text" path="title" /></td>
                    </tr>
                    <tr>
                        <th><form:label path="journal" cssClass="namesearchLabel">Journal</form:label></th>
                        <td><form:input type="text" path="journal"/></td>
                    </tr>
                    <tr>
                        <th><form:label path="keywords" cssClass="namesearchLabel">Keywords</form:label></th>
                        <td><form:input type="text" path="keywords"/></td>
                    </tr>
                    <tr>
                        <th><form:label path="zdbID" cssClass="namesearchLabel">ZFIN ID</form:label></th>
                        <td><form:input type="text" path="zdbID"/></td>
                    </tr>
                </table>
            </td>
            <td width="50%">
                <table class="primary-entity-attributes">
                    <tr>
                        <th><form:label path="twoDigitYear" cssClass="namesearchLabel">Year</form:label></th>
                        <td>
                            <form:select path="yearType">
                                <form:options itmes="${yearTypes}" itemLabel="display" />
                            </form:select>
                            <form:select path="century">
                                <form:options items="${centuries}" itemLabel="display" />
                            </form:select>
                            <form:input type="text" path="twoDigitYear" size="2" maxlength="2" />
                        </td>
                    </tr>
                    <tr>
                        <th><form:label path="pubType" cssClass="namesearchLabel">Type</form:label></th>
                        <td>
                            <form:select path="pubType">
                                <form:option value="" label="ALL" />
                                <form:options items="${pubTypes}" itemLabel="display" />
                            </form:select>
                        </td>
                    </tr>
                    <tr>
                        <th><form:label path="sort" cssClass="namesearchLabel">Order by</form:label></th>
                        <td>
                            <form:select path="sort">
                                <form:options items="${sortOrders}" itemLabel="display" />
                            </form:select>
                        </td>
                    </tr>
                </table>
            </td>
        </tr>
        <tr>
            <td class="submitbar" bgcolor="#cccccc" colspan="2">
                <input value="Search" type="submit">
                <input value="Reset" type="reset">
                <input value="List all publications" type="button" id="list-all-pubs">
            </td>
        </tr>
    </table>
</form:form>

<script>
    $(function () {
        function resetForm($form) {
            $form.find('input:text').val('');
            $form.find('select option:first-child').attr('selected', true);
        }

        var $form = $("#pub-search-form");
        $form.find(":reset").click(function (evt) {
            evt.preventDefault();
            resetForm($form);
        });
        $('#list-all-pubs').click(function (evt) {
            evt.preventDefault();
            resetForm($form);
            $form.submit();
        });
        $('#pub-printable-results').click(function (evt) {
            evt.preventDefault();
            window.location.href = '/action/publication/search/printable?' + $form.serialize();
        });
        $('#pub-refer-results').click(function (evt) {
            evt.preventDefault();
            window.location.href = '/action/publication/search/refer?' + $form.serialize();
        });
    });
</script>
