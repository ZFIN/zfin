<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>
<jsp:useBean id="formBean" class="org.zfin.marker.presentation.TranscriptBean" scope="request"/>

<div>
    <c:forEach var="sequence" items="${formBean.nucleotideSequences}">
    <div class="sequence-wrapper">
        <table>
            <tr>
                <td width="65%">
                        ${sequence.dbLink.accessionNumber} <zfin:attribution entity="${sequence.dbLink}"/>
                    <a class="show-sequence-button" href="#">
                        [ <i class="fas fa-caret-right"></i> <span class="show-sequence-button-text">Show</span> ]
                    </a>

                    <span class="sequence-help">
                                    (double-click sequence to select)
                                </span>
                </td>

                <td width="35%">
                    <a href="/action/blast/download-sequence?accession=${sequence.dbLink.accessionNumber}">
                        [Download]
                    </a>
                </td>

                <td>
                    <zfin2:blastDropDown dbLink="${sequence.dbLink}" instructions="Select Analysis Tool"
                                         minWidth="150px"/>
                </td>
            </tr>
        </table>

        <div class="sequence-data">
            <pre>${sequence.formattedData}</pre>
        </div>
    </div>
    </c:forEach>


        <script>
        function selectSequence (element) {
            var range, selection;
            if (document.body.createTextRange) {
                range = document.body.createTextRange();
                range.moveToElementText(element);
                range.select();
            } else if (window.getSelection) {
                selection = window.getSelection();
                range = document.createRange();
                range.selectNodeContents(element);
                selection.removeAllRanges();
                selection.addRange(range);
            }
        }

        jQuery(".sequence-wrapper").each(function () {
            var parent = jQuery(this),
                button = parent.find(".show-sequence-button"),
                buttonText = parent.find(".show-sequence-button-text"),
                icon = parent.find(".fa"),
                help = parent.find(".sequence-help"),
                data = parent.find(".sequence-data");
            help.hide();
            data.hide();

            button.on('click', function (evt) {
                buttonText.text(function (idx, current) {
                    return current === 'Show' ? 'Hide' : 'Show';
                });
                icon.toggleClass('fa-rotate-90');
                help.toggle();
                data.slideToggle(200);
                evt.preventDefault();
            });

            data.on('dblclick', function () {
                selectSequence(this);
            });
        });
    </script>

</div>
<%--

<z:dataTable hasData="${!empty formBean.nucleotideSequences}">

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
        <div class="sequence-wrapper">
            <tr>
                <td>
                        ${sequence.dbLink.accessionNumber} <zfin:attribution entity="${sequence.dbLink}"/>
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
                    <zfin2:blastDropDown dbLink="${sequence.dbLink}" instructions="Select Analysis Tool"
                                         minWidth="150px"/>
                </td>
            </tr>

            <div class="sequence-data">
                <pre>${sequence.formattedData}</pre>
            </div>
        </div>
    </c:forEach>
    </tbody>
</z:dataTable>
--%>






