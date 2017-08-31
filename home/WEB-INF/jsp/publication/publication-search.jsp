<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.publication.presentation.PublicationSearchBean" scope="request"/>

<c:set var="newestPubEntryYear"><fmt:formatDate value="${newestPubEntryDate}" pattern="yyyy"/></c:set>
<c:set var="newestPubEntryMonth"><fmt:formatDate value="${newestPubEntryDate}" pattern="M"/></c:set>
<c:set var="newestPubEntryDay"><fmt:formatDate value="${newestPubEntryDate}" pattern="d"/></c:set>
<c:set var="oldestPubEntryYear"><fmt:formatDate value="${oldestPubEntryDate}" pattern="yyyy"/></c:set>
<c:set var="oldestPubEntryMonth"><fmt:formatDate value="${oldestPubEntryDate}" pattern="M"/></c:set>
<c:set var="oldestPubEntryDay"><fmt:formatDate value="${oldestPubEntryDate}" pattern="d"/></c:set>

<c:if test="${formBean.results != null}">
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
                <authz:authorize access="hasRole('root')">
                    <td>
                        <div><a href="/action/publication/${pub.zdbID}/edit">EDIT</a></div>
                        <div><a href="/action/publication/${pub.zdbID}/link">LINK</a></div>
                        <div><a href="/action/publication/${pub.zdbID}/track">TRACK</a></div>
                        <c:if test="${pub.type.curationAllowed}">
                            <div><a href="/action/curation/${pub.zdbID}">CURATE</a></div>
                        </c:if>
                    </td>
                </authz:authorize>
                <td>
                    <div class="show_pubs">
                        <a href="/${pub.zdbID}" id="${pub.zdbID}">${pub.citation}</a>
                    </div>
                </td>
                <authz:authorize access="hasRole('root')">
                    <td>
                        <c:if test="${pub.open}">OPEN</c:if>
                        <c:if test="${!pub.open}">CLOSED</c:if>
                        <c:if test="${pub.indexed}">, INDEXED</c:if>
                    </td>
                </authz:authorize>
            </tr>
        </c:forEach>
        </tbody>
    </table>
    <zfin2:pagination paginationBean="${formBean}" />
</c:if>

<div class="titlebar">
    <h1>${formBean.results == null ? "Search for Publications" : "Modify your search"}</h1>
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
                    <authz:authorize access="hasRole('root')">
                    <tr>
                        <th><form:label path="curator" cssClass="namesearchLabel">Curator</form:label></th>
                        <td>
                            <form:select path="curator">
                                <form:option value="" label="Anyone"/>
                                <form:options items="${curators}" itemLabel="fullName" itemValue="zdbID"/>
                            </form:select>
                        </td>
                    </tr>
                    <tr>
                        <th><form:label path="curationStatus" cssClass="namesearchLabel">Curation Status</form:label></th>
                        <td>
                            <form:select path="curationStatus">
                                <form:option value="" label="Any"/>
                                <form:option value="Closed"/>
                                <form:option value="Open"/>
                                <form:option value="Indexed"/>
                            </form:select>
                        </td>
                    </tr>
                    <tr>
                        <th><form:label path="pubStatus" cssClass="namesearchLabel">Publication Status</form:label></th>
                        <td>
                            <form:select path="pubStatus">
                                <form:option value="" label="Any"/>
                                <form:option value="Active" />
                                <form:option value="Inactive" />
                                <form:option value="Epub ahead of print" />
                                <form:option value="in press" />
                            </form:select>
                        </td>
                    </tr>
                    </authz:authorize>
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
                                <form:option value="20"/>
                                <form:option value="19"/>
                                <form:option value="18"/>
                            </form:select>
                            <form:input type="text" path="twoDigitYear" size="2" maxlength="2" />
                        </td>
                    </tr>
                    <tr>
                        <th><form:label path="pubType" cssClass="namesearchLabel">Type</form:label></th>
                        <td>
                            <form:select path="pubType">
                                <form:option value="" label="Any" />
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
                    <authz:authorize access="hasRole('root')">
                    <tr>
                        <th><label class="namesearchLabel">PET Date</label></th>
                        <td>
                            <table>
                                <tr>
                                    <td>From:</td>
                                    <td>
                                        <form:select path="petFromMonth">
                                            <c:forEach begin="1" end="12" var="month"><form:option value="${month}"/></c:forEach>
                                        </form:select>
                                        /
                                        <form:select path="petFromDay">
                                            <c:forEach begin="1" end="31" var="day"><form:option value="${day}"/></c:forEach>
                                        </form:select>
                                        /
                                        <form:select path="petFromYear">
                                            <c:forEach begin="${oldestPubEntryYear}" end="${newestPubEntryYear}" var="year"><form:option value="${year}"/></c:forEach>
                                        </form:select>
                                    </td>
                                </tr>
                                <tr>
                                    <td>To:</td>
                                    <td>
                                        <form:select path="petToMonth">
                                            <c:forEach begin="1" end="12" var="month"><form:option value="${month}"/></c:forEach>
                                        </form:select>
                                        /
                                        <form:select path="petToDay">
                                            <c:forEach begin="1" end="31" var="day"><form:option value="${day}"/></c:forEach>
                                        </form:select>
                                        /
                                        <form:select path="petToYear">
                                            <c:forEach begin="${oldestPubEntryYear}" end="${newestPubEntryYear}" var="year"><form:option value="${year}"/></c:forEach>
                                        </form:select>
                                    </td>
                                </tr>
                            </table>
                        </td>
                    </tr>
                    </authz:authorize>
                    <tr>
                        <td colspan="2">
                            <form:label path="count">Display results in groups of </form:label>
                            <form:input type="text" path="count" size="3"/>
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
            $form.resetForm({
                petFromMonth: ${oldestPubEntryMonth},
                petFromDay: ${oldestPubEntryDay},
                petFromYear: ${oldestPubEntryYear},
                petToMonth: ${newestPubEntryMonth},
                petToDay: ${newestPubEntryDay},
                petToYear: ${newestPubEntryYear},
                count: '10'
            });
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
