<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>

<z:dataTable collapse="true" hasData="${!empty formBean.nucleotideSequences}">



        <thead>
        <tr>
            <th>
                Accession #
            </th>
            <th>
                Sequence
            </th>
            <th>
                Analyze
            </th>
        </tr>
        </thead>
        <tbody>
        <c:forEach var="sequence" items="${formBean.nucleotideSequences}">
            <tr>
                <td>
                    ${sequence.dbLink.accessionNumber} <zfin:attribution entity="${sequence.dbLink}" />
                <a class="show-sequence-button" href="#">
                    [ <i class="fas fa-caret-right"></i> <span class="show-sequence-button-text">Show</span> ]
                </a>

                <span class="sequence-help">
                                    (double-click sequence to select)
                                </span>
                </td>
                <td>
                    <a href="/action/blast/download-sequence?accession=${sequence.dbLink.accessionNumber}">
                        [Download]
                    </a>
                </td>
                <td>
                    <zfin2:blastDropDown dbLink="${sequence.dbLink}" instructions="Select Analysis Tool" minWidth="150px"/>
                </td>
            </tr>
    </c:forEach>
    </tbody>
</z:dataTable>

