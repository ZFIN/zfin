<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<div style="text-align: center;">
    <span style="font-size: larger">Listing of selected Publication records:</span>
    (as of <fmt:formatDate value="${today}" pattern="MM/dd/yyyy" />)
</div>

<c:if test="${!empty formBean.title ||
              !empty formBean.author ||
              !empty formBean.journal ||
              !empty formBean.keywords ||
              !empty formBean.zdbID ||
              !empty formBean.twoDigitYear}">
    <p style="font-size: larger">Criteria used to generate this listing:</p>
    <ul>
        <c:if test="${!empty formBean.title}"><li><b>TITLE</b> contains: ${formBean.title}</li></c:if>
        <c:if test="${!empty formBean.author}"><li><b>AUTHORS</b> contain: ${formBean.author}</li></c:if>
        <c:if test="${!empty formBean.journal}"><li><b>JOURNAL</b> contains: ${formBean.journal}</li></c:if>
        <c:if test="${!empty formBean.keywords}"><li><b>KEYWORDS</b> contain: ${formBean.keywords}</li></c:if>
        <c:if test="${!empty formBean.zdbID}"><li><b>ZFIN ID</b> contains: ${formBean.zdbID}</li></c:if>
        <c:if test="${!empty formBean.twoDigitYear}"><li><b>YEAR</b> ${formBean.yearType.display}: ${formBean.century.display}${formBean.twoDigitYear}</li></c:if>
    </ul>
</c:if>

<p>
    <b>Total of <span style="font-size: larger">${fn:length(resultBeans)}</span> matching record(s) to display.</b>
</p>

<c:forEach items="${resultBeans}" var="result">
<div style="padding: 3px;">
    ${result.authors}${' '}
    <c:if test="${!empty result.year}">(${result.year}) </c:if>
    ${result.title}.
    ${result.journal}${' '}
    <c:if test="${!empty result.volume}">${result.volume}: </c:if>
    <c:if test="${!empty result.pages}">${result.pages}. </c:if>
    <c:if test="${result.status == 'Epub ahead of print' or result.status == 'in press'}">${result.status}. </c:if>
    <c:if test="${result.journal == 'ZFIN Direct Data Submission'}">(http://zfin.org). </c:if>
</div>
</c:forEach>