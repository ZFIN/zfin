<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<z:attributeList>

    <z:attributeListItem label="Totals">
        <z:dataTable>
            <thead>
                <tr>
                    <c:forEach var="column" items="${statistic.columns}">
                        <th>${column.key}</th>
                    </c:forEach>
                </tr>
            </thead>
            <tbody>
                <tr>
                    <c:forEach var="column" items="${statistic.columns}">
                        <td>
                            <table>
                                <tr>
                                    <td>T</td>
                                    <td><fmt:formatNumber value="${column.value.columnStat.totalNumber}"
                                                          pattern="##,###"/></td>
                                </tr>
                                <c:if test="${!column.value.columnDefinition.superEntity}">
                                    <tr>
                                        <td>TD</td>
                                        <td><fmt:formatNumber value="${column.value.columnStat.totalDistinctNumber}"
                                                              pattern="##,###"/></td>
                                    </tr>
                                    <tr>
                                        <td>C</td>
                                        <td>${column.value.columnStat.cardinality}</td>
                                    </tr>
                                    <tr>
                                        <td>H</td>
                                        <td>
                                            <table>
                                                <c:forEach var="histogram" items="${column.value.columnStat.histogram}">
                                                    <tr>
                                                        <td>${histogram.key}</td>
                                                        <td><fmt:formatNumber value="${histogram.value}"
                                                                              pattern="##,###"/></td>
                                                    </tr>
                                                </c:forEach>
                                            </table>
                                        </td>
                                    </tr>
                                </c:if>
                            </table>
                        </td>
                    </c:forEach>
                </tr>
            </tbody>
        </z:dataTable>
    </z:attributeListItem>


</z:attributeList>