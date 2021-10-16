<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:devtoolsPage title="Fluorescent">
    <table class="data-table sortable">
        <thead>
            <tr>
                <th style="width: 300px;">Protein</th>
                <th>EFG</th>
            </tr>
        </thead>
        <tbody>
            <c:forEach var="protein" items="${proteins}" varStatus="loop">
                <zfin:alternating-tr loopName="loop">
                    <td>
                        <a href='https://www.fpbase.org/protein/${protein.ID}'>${protein.name}</a>

                    </td>
                    <td>
                        <c:if test="${not empty protein.efgs}">
                            <zfin:link entity="${protein.efgs[0]}"/>
                        </c:if>
                    </td>
                </zfin:alternating-tr>
            </c:forEach>
        </tbody>
    </table>
</z:devtoolsPage>