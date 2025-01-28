<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <h1>ZFIN Data Contents</h1>
    <div class="stats-controls">
        <form method="GET">
            <p>Showing data from
                <select name="beginYear">
                    <c:forEach var="y" items="${allYears}">
                        <c:choose>
                        <c:when test="${y == beginYear}">
                            <option selected>${y}</option>
                        </c:when>
                        <c:otherwise>
                            <option>${y}</option>
                        </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </select>
                to
                <select name="endYear">
                    <c:forEach var="y" items="${allYears}">
                        <c:choose>
                        <c:when test="${y == endYear}">
                            <option selected>${y}</option>
                        </c:when>
                        <c:otherwise>
                            <option>${y}</option>
                        </c:otherwise>
                        </c:choose>
                    </c:forEach>
                </select>
                <button>Update</button>
            </p>
        </form>
    </div>

    <a href="annual-stats-view-download">Download CSV</a>
    <table style="border-collapse: collapse;">
        <tr>
            <td style="border: 1px solid black; width:280px;">&nbsp;</td>
            <c:forEach var="y" items="${years}" >
                <td style="border: 1px solid black;  width:60px; text-align: center;"><strong>${y}</strong></td>
            </c:forEach>
        </tr>

        <c:forEach var="stat" items="${statsMap}">
            <tr style="background-color: #b4b4b4; border: 1px solid black;">
                <td colspan="${fn:length(years)+1}"><strong>${stat.key}</strong>
                </td>
            </tr>
            <c:forEach var="statCategory" items="${stat.value}">
                <tr style="border: 1px solid black;width:280px;">
                    <td style="border: 1px solid black;">${statCategory.key}</td>
                    <c:forEach var="annualStat" items="${statCategory.value}">
                        <td style="border: 1px solid black; text-align: right">
                            <c:if test="${annualStat.count != 0}">
                            <fmt:formatNumber type = "number"
                                              maxFractionDigits = "3" value = "${annualStat.count}" />
                            </c:if>
                        </td>
                    </c:forEach>
                </tr>
            </c:forEach>
        </c:forEach>
    </table>
</z:page>
