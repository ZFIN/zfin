<%@ tag import="org.zfin.framework.presentation.LookupStrings" %>
<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="sequences" type="java.util.Collection" rtexprvalue="true" required="true" %>

<c:if test="${!empty sequences}">

    <div class="summary">
        <div class="summaryTitle">SEQUENCE</div>
        <div class="sequence-view-table-wrapper">
            <c:forEach var="sequence" items="${sequences}">
                <div class="sequence-wrapper">
                    <table class="summary sequence-view">
                        <tr>
                            <td width="65%">
                                ${sequence.dbLink.accessionNumber} <zfin:attribution entity="${sequence.dbLink}" />
                                <a class="show-sequence-button" href="#">
                                    [ <i class="fas fa-caret-right"></i> <span class="show-sequence-button-text">Show</span> ]
                                </a>

                                <span class="sequence-help">
                                    (double-click sequence to select)
                                </span>
                            </td>

                            <td  width="35%">
                                <a href="/action/blast/download-sequence?<%=LookupStrings.ACCESSION%>=${sequence.dbLink.accessionNumber}">
                                    [Download]
                                </a>
                            </td>

                            <td>
                                <zfin2:externalBlastDropDown dbLink="${sequence.dbLink}" instructions="Select Analysis Tool" minWidth="150px"/>
                            </td>
                        </tr>
                    </table>

                    <div class="sequence-data">
                        <pre>${sequence.formattedData}</pre>
                    </div>
                </div>
            </c:forEach>
        </div>
    </div>

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

</c:if>
