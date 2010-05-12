<%@ page import="org.zfin.properties.ZfinProperties" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.uniquery.presentation.SearchBean" scope="request"/>

<!---
The SearchBean Java Bean should be used in session mode, so user cookies must be
enabled. ZfinSession mode allows the bean to cache search queries and thereby
speed up performance dramatically while browsing categories. The trade-off is
that Tomcat will require significant memory allocation to store these Beans
in memory.

The Tomcat startup script should adequately specify enough heap size for the Java
JVM. As an example, this is how the Tomcat startup script could allocate 256 Megabytes
of memory for the JVM:

CATALINA_OPTS= -XX:NewSize=128m -XX:MaxNewSize=128m -XX:SurvivorRatio=8 -Xms256M -Xmx256M

By default, the JVM is allocated only about 60 Megabytes which is very (too) small.
--->
<style type="text/css">
    .category_header {
        font-size: 100%;
        font-weight: bold;
        font-family: arial, sans-serif;
    }

    .category_label {
        font-size: 100%;
        font-weight: bold;
        font-family: arial, sans-serif;
    }

    .category_box {
        padding-top: 10px;
    }

    .category_table {
        font-size: 90%;
        font-family: arial, sans-serif;
        border: 1px solid #006666;
        padding: 2px;
    }

    .category_item {
        font-size: 90%;
        font-family: arial, sans-serif;
        padding-top: 2px;
        padding-right: 5px;
        padding-bottom: 2px;
        padding-left: 5px;
    }

    .alias_list {
    }

    .alias_list_header {
        Color: #cc0000;
    }

    .ignored_words {
        font-size: 90%;
        font-family: arial, sans-serif;
        padding-top: 10px;
    }

    .related_terms {
        font-size: 90%;
        font-family: arial, sans-serif;
        padding-top: 10px;
    }

    .related_terms_match {
        color: #999999;
    }

    .specific_search {
        font-size: 90%;
        font-family: arial, sans-serif;
        padding-top: 10px;
    }

    .results_header {
        font-size: 100%;
        font-weight: bold;
        font-family: arial, sans-serif;
        padding: 0px;
        margin: 0px;
    }

    .search_tip {
        font-size: 90%;
        font-family: arial, sans-serif;
        padding-top: 10px;
    }

    .best_match {
        font-size: 100%;
        font-family: arial, sans-serif;
        padding: 0px;
        margin: 0px;
    }
</style>

<script type="text/javascript">
    document.getElementById("qsearch").value = "${formBean.queryTerm}";
</script>


<!--- Only display results if there is a query --->
<c:choose>
    <c:when test="${formBean.queryTerm eq ''}">
    <span class="results_header">
      Please enter a search term.
   </span>
    </c:when>
    <c:otherwise>
        <c:choose>
            <c:when test="${formBean.replacementZdbID != null}">
    <span class="results_header">
      ${formBean.queryTerm} has been changed. <p>
        Please check <a href="/action/quicksearch?query=${formBean.replacementZdbID.replacementZdbID}">
            ${formBean.replacementZdbID.replacementZdbID}</a>.
    </span>
            </c:when>
            <c:otherwise>
                <table width=100% cellspacing=0>
                    <tr>
                        <td width=85% align=center>
                            <span class="results_header">
                                ${formBean.categorySearch} results for '${formBean.queryTerm}   '
                            </span>
                            <span class="search_tip">
                                <a href="/zf_info/syntax_help.html"> Tips </a>
                            </span>
                        </td>
                        <td width=15% align=right>
                            <tiles:insert page="/WEB-INF/jsp-include/input_welcome.jsp" flush="false">
                                <tiles:put name="subjectName" value="Site search"/>
                                <tiles:put name="subjectID" value=""/>
                            </tiles:insert>
                        </td>
                    </tr>
                </table>


                <!--- Display Ignored Words List --->
                <c:if test="${ not empty formBean.ignoredWords}">
                    <div class="ignored_words">The following words are very common and were not included in your search:
                        <c:forEach var="ignoredWord" items="${formBean.ignoredWords}">
                        <em> ${ignoredWord}</em> &nbsp;
                            </c:forEach>
                    </div>
                </c:if>
                <table>
                    <tr>
                        <td>
                            <!--- Display Related Words List --->
                                ${formBean.relatedTermsHTML}
                        </td>
                    </tr>
                    <tr>
                        <td>
                            <!---  Display suggestion to use ZFIN specific search forms. --->
                                ${formBean.relatedSearchPageHTML}
                        </td>
                    </tr>
                </table>

                <!--- Display Category List (as a TABLE) --->
                <font size=-1>(Each category displays the first 5000 results)</font>
                <center>
                        ${formBean.categoryListingHTML}
                </center>

                ${formBean.bestMatchHTML}
                <!--- END: Display the best match --->

                ${formBean.searchResult}

                <zfin2:pagination paginationBean="${formBean}" />
            </c:otherwise>
        </c:choose>
    </c:otherwise>
</c:choose>