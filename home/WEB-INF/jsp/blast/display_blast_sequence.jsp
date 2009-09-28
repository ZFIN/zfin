<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ page contentType="text/html;charset=UTF-8" language="java" %>

<%--<zfin2:sequenceDisplay/>--%>

<%--todo: this should use an attribute, not a formBean, which is a nucleotideSequence --%>

<c:choose>
    <c:when test="${empty formBean.sequences}">
        <table width="100%">
            <tr>
                <td align="center">
                    <span class="error">Sequence not found.</span>
                </td>
            </tr>
        </table>
    </c:when>
    <c:otherwise>
        <c:set var="sequence" value="${formBean.sequence}"/>
        <table>
            <c:if test="${not empty formBean.transcript}">
            <tr>
                <td><b>Transcript:</b></td><td> <zfin:link entity="${formBean.transcript}"/></td>
            </tr>
            </c:if>
            <c:if test="${not empty formBean.gene}">
            <tr>
                <td><b>Gene:</b> </td><td><zfin:link entity="${formBean.gene}"/></td>
            </tr>
            </c:if>
            <tr>
                <td> <b>Accession:</b> </td><td>${sequence.dbLink.accessionNumber} </td>
                <td><zfin2:externalBlastDropDown dbLink="${sequence.dbLink}"/></td>
            </tr>
        </table>

        <table>
            <tr>
                <td>
                    <pre>${sequence.defLine}</pre>
                    <pre>${sequence.formattedSequence}</pre>
                </td>
            </tr>
        </table>
    </c:otherwise>
</c:choose>



