<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:page>
    <h1>ZFIN Data Contents</h1>
    <table style="border-collapse: collapse;">
        <tr>
            <td style="border: 1px solid black; width:280px;">&nbsp;</td>
            <c:forEach var="y" items="${years}">
                <td style="border: 1px solid black;  width:60px; text-align: center;"><strong>${y}</strong></td>
            </c:forEach>
        </tr>

            <tr style="background-color: #b4b4b4; border: 1px solid black;">
                <td><strong>Genes</strong></td>
                <c:forEach var="y" items="${years}">
                    <td>&nbsp;</td>
                </c:forEach>
            </tr>

            <c:forEach var="stat" items="${genesStats}" varStatus="loop">
                <c:if test="${(loop.count - 1) % numberOfYears == 0}">
                    <tr style="border: 1px solid black;">
                    <td style="border: 1px solid black;">${stat.category}</td>
                </c:if>
                <td style="border: 1px solid black; text-align: right;">${stat.count}</td>
                <c:if test="${loop.count % numberOfYears == 0}">
                    </tr>
                </c:if>
            </c:forEach>

            <tr style="background-color: #b4b4b4; border: 1px solid black;">
                <td><strong>Genetics</strong></td>
                <c:forEach var="y" items="${years}">
                    <td>&nbsp;</td>
                </c:forEach>
            </tr>

            <c:forEach var="stat" items="${geneticsStats}" varStatus="loop">
                <c:if test="${(loop.count - 1) % numberOfYears == 0}">
                    <tr style="border: 1px solid black;">
                    <td style="border: 1px solid black;">${stat.category}</td>
                </c:if>
                <td style="border: 1px solid black; text-align: right;">${stat.count}</td>
                <c:if test="${loop.count % numberOfYears == 0}">
                    </tr>
                </c:if>
            </c:forEach>

            <tr style="background-color: #b4b4b4; border: 1px solid black;">
                <td><strong>Functional Annotation</strong></td>
                <c:forEach var="y" items="${years}">
                    <td>&nbsp;</td>
                </c:forEach>
            </tr>

            <c:forEach var="stat" items="${faStats}" varStatus="loop">
                <c:if test="${(loop.count - 1) % numberOfYears == 0}">
                    <tr style="border: 1px solid black;">
                    <td style="border: 1px solid black;">${stat.category}</td>
                </c:if>
                <td style="border: 1px solid black; text-align: right;">${stat.count}</td>
                <c:if test="${loop.count % numberOfYears == 0}">
                    </tr>
                </c:if>
            </c:forEach>

            <tr style="background-color: #b4b4b4; border: 1px solid black;">
                <td><strong>Reagents</strong></td>
                <c:forEach var="y" items="${years}">
                    <td>&nbsp;</td>
                </c:forEach>
            </tr>

            <c:forEach var="stat" items="${reagentsStats}" varStatus="loop">
                <c:if test="${(loop.count - 1) % numberOfYears == 0}">
                    <tr style="border: 1px solid black;">
                    <td style="border: 1px solid black;">${stat.category}</td>
                </c:if>
                <td style="border: 1px solid black; text-align: right;">${stat.count}</td>
                <c:if test="${loop.count % numberOfYears == 0}">
                    </tr>
                </c:if>
            </c:forEach>

            <tr style="background-color: #b4b4b4; border: 1px solid black;">
                <td><strong>Expression & Phenotype</strong></td>
                <c:forEach var="y" items="${years}">
                    <td>&nbsp;</td>
                </c:forEach>
            </tr>

            <c:forEach var="stat" items="${xpPhenoStats}" varStatus="loop">
                <c:if test="${(loop.count - 1) % numberOfYears == 0}">
                    <tr style="border: 1px solid black;">
                    <td style="border: 1px solid black;">${stat.category}</td>
                </c:if>
                <td style="border: 1px solid black; text-align: right;">${stat.count}</td>
                <c:if test="${loop.count % numberOfYears == 0}">
                    </tr>
                </c:if>
            </c:forEach>

            <tr style="background-color: #b4b4b4; border: 1px solid black;">
                <td><strong>Genomics</strong></td>
                <c:forEach var="y" items="${years}">
                    <td>&nbsp;</td>
                </c:forEach>
            </tr>

            <c:forEach var="stat" items="${genomicsStats}" varStatus="loop">
                <c:if test="${(loop.count - 1) % numberOfYears == 0}">
                    <tr style="border: 1px solid black;">
                    <td style="border: 1px solid black;">${stat.category}</td>
                </c:if>
                <td style="border: 1px solid black; text-align: right;">${stat.count}</td>
                <c:if test="${loop.count % numberOfYears == 0}">
                    </tr>
                </c:if>
            </c:forEach>

            <tr style="background-color: #b4b4b4; border: 1px solid black;">
                <td><strong>Community Information</strong></td>
                <c:forEach var="y" items="${years}">
                    <td>&nbsp;</td>
                </c:forEach>
            </tr>

            <c:forEach var="stat" items="${communityStats}" varStatus="loop">
                <c:if test="${(loop.count - 1) % numberOfYears == 0}">
                    <tr style="border: 1px solid black;">
                    <td style="border: 1px solid black;">${stat.category}</td>
                </c:if>
                <td style="border: 1px solid black; text-align: right;">${stat.count}</td>
                <c:if test="${loop.count % numberOfYears == 0}">
                    </tr>
                </c:if>
            </c:forEach>

            <tr style="background-color: #b4b4b4; border: 1px solid black;">
                <td><strong>Orthology</strong></td>
                <c:forEach var="y" items="${years}">
                    <td>&nbsp;</td>
                </c:forEach>
            </tr>

            <c:forEach var="stat" items="${orthStats}" varStatus="loop">
                <c:if test="${(loop.count - 1) % numberOfYears == 0}">
                    <tr style="border: 1px solid black;">
                    <td style="border: 1px solid black;">${stat.category}</td>
                </c:if>
                <td style="border: 1px solid black; text-align: right;">${stat.count}</td>
                <c:if test="${loop.count % numberOfYears == 0}">
                    </tr>
                </c:if>
            </c:forEach>
    </table>
</z:page>
