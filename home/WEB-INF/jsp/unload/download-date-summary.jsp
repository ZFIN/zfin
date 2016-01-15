<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<jsp:useBean id="formBean" class="org.zfin.util.database.presentation.UnloadBean" scope="request"/>

<table width="100%">
    <tr>
        <td>
            <h2>ZFIN Data Reports from:
                <fmt:formatDate pattern="d MMM yyyy" value="${formBean.archiveDate}"/>
                <authz:authorize access="hasRole('root')">
                    (${formBean.date})
                </authz:authorize>
            </h2></td>
        <td align="right"><a HREF="/warranty.html">Warranty Disclaimer and Copyright Notice</a>&nbsp;&nbsp;</td>
    </tr>
</table>

<table width="75%">
    <tr>
        <td>
            ZFIN data reports are updated every day of the week at 10:10pm PST with the exception of the externally
            hosted
            files.
            The format for each file is described below.
            All files use tab as the field delimiter.
            The 'Download File' link will display the file in your browser. Click the download button to
            save the file onto your local system (includes timestamp and header info). If you choose to link to
            a ZFIN data page from your web site, append the ZFIN ID to the URL
            http://zfin.org/<br/>
            e.g. http://zfin.org/ZDB-GENE-980526-166 <br/>
            for the 'shha' gene.
            Please direct questions or requests for additional data to <a href="mailto:zfinadmn@zfin.org">ZFIN.</a>
        </td>
    </tr>
    <tr>
        <td></td>
    </tr>
    <tr>
        <td style="font-weight: bold">
            <c:choose>
                <c:when test="${fn:contains(formBean.request, 'archive')}">
                    <a href="../">View Archive Files</a>
                </c:when>
                <c:otherwise>
                    <a href="downloads/archive">View Archive Files</a>
                </c:otherwise>
            </c:choose>
        </td>
    </tr>
</table>

<zfin2:createDownloadFileTable downloadFileList="${formBean.officialDownloadInfoFiles}" identifier="official"/>

<h2>Externally Hosted Files:</h2> The 'File Name'-links point to the latest version.
To obtain previous versions you have to check the archive of the external source.

<table class="summary groupstripes">
    <tr>
        <th width="200">Category</th>
        <th width="500">Description</th>
        <th>External Source</th>
        <th width="200">File Name</th>
        <th width="120">Update Frequency</th>
        <th width="120">Format / Header</th>
    </tr>
    <tr>
        <td class="bold">Gene Ontology Data</td>
        <td>
            <b> Gene Ontology (GO) Annotations of Zebrafish Markers</b>
            <br>(<i>see also</i> <a HREF="http://www.geneontology.org/GO.format.gaf-2_0.shtml">GO Annotation File
            format</a> at the Gene Ontology Consortium)
        </td>
        <td>
            <a HREF="http://www.geneontology.org/">geneontology.org</a>
        </td>
        <td><a HREF="ftp://ftp.geneontology.org/pub/go/gene-associations/gene_association.zfin.gz">gene_association.zfin.gz</a>
        </td>
        <td>weekly</td>
        <td>
            <div style="font-size: 14px">
                                            <span style="text-align:right" id="header-info-show-link">
                            <a style="font-size:smaller; margin-right: 1em;" class="clickable showAll"
                               onclick="jQuery('#header-info-show-link').hide();
                                       jQuery('#header-info-hide-detail').show();
                                       jQuery('#header-info').show();">
                                Show </a>
                        </span>
                        <span style="text-align:right; display: none;" id="header-info-hide-detail">
                            <a style="font-size:small; margin-right: 1em;" class="clickable hideAll"
                               onclick="jQuery('#header-info').hide();
                                       jQuery('#header-info-hide-detail').hide();
                                       jQuery('#header-info-show-link').show();">Hide </a>
                        </span>
            </div>
        </td>
    </tr>
    <tr>
        <td colspan="7">
            <div style="font-size: smaller; display: none;" id="header-info" align="right">
                <table class="summary1">
                    <tr>
                        <td style="font-weight: bold; background-color: #91bdbd;">Database Designation</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Marker ID</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Gene Symbol</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Qualifiers</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">GO<br>Term ID</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Refer-<br>ence ID</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">GO<br>Evidence Code</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Inferred From</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Ontology:
      <span style="font-size: smaller">
        <br>P=Biological Process
        <br>F=Molecular Function
        <br>C=Cellular Component
      </span>
                        </td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Marker Name</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Marker Synonyms<br>(if any)</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Marker Type:
      <span style="font-size: smaller">
        <br>gene<br>transcript<br>protein
      </span>
                        </td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Taxon</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Modification Date</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Assigned By</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Annotation Extension</td>
                        <td style="font-weight: bold; background-color: #91bdbd;">Gene Product Form ID</td>
                    </tr>
                </table>
            </div>
        </td>
    </tr>
    <tr>
        <td class="bold">Anatomical Ontologies</td>
        <td>
            Zebrafish Anatomical Ontology in OBO Format
        </td>
        <td>
            <a HREF="http://www.obofoundry.org/">Obofoundry</a>
        </td>
        <td>
            <a HREF="http://purl.obolibrary.org/obo/zfa.obo">zebrafish_anatomy.obo</a>
        </td>
        <td>
            monthly
        </td>
        <td>
            OBO file format
        </td>
    </tr>
</table>


<authz:authorize access="hasRole('root')">

    <h2>Unofficial Download Files:</h2>

    <zfin2:createDownloadFileTable downloadFileList="${formBean.unofficialDownloadInfoFiles}" identifier="unofficial"/>

    <h3>Files not in currently referenced on the download page:</h3>
    <table class="summary">
        <tr>
            <th width="50"></th>
            <th width="200">File Name</th>
            <th width="150" style="text-align: right">File Size</th>
            <th style="text-align: right">Number of Records</th>
        </tr>
        <c:forEach var="fileInfo" items="${formBean.unusedDownloadInfoFiles}" varStatus="loop">
            <zfin:alternating-tr loopName="loop" groupBeanCollection="${formBean.unusedDownloadInfoFiles}">
                <td>${loop.index+1}</td>
                <td>
                    <c:choose>
                        <c:when test="${formBean.currentDate}">
                            <a href="downloads/${fileInfo.name}">${fileInfo.name}</a>
                        </c:when>
                        <c:otherwise>
                            <a href="${formBean.date}/${fileInfo.name}">${fileInfo.name}</a>
                        </c:otherwise>
                    </c:choose>
                </td>
                <td style="text-align: right">
                        ${fileInfo.byteCountDisplay}
                </td>
            </zfin:alternating-tr>
        </c:forEach>
    </table>
</authz:authorize>

<script type="text/javascript">
    function submitForm(fileName) {
        var form = document.getElementById(fileName);
        form.submit();
    }

    function downloadFile(date, fileName) {
        //alert("Hello: " + fileName);
    <c:choose>
    <c:when test="${formBean.currentDate}">
        window.location = "downloads/file/" + fileName;
    </c:when>
    <c:otherwise>
        window.location = date + "/file/" + fileName;
    </c:otherwise>
    </c:choose>
    }

    $(function () {
        $(".header-toggle").click(function (evt) {
            evt.preventDefault();
            var $this = $(this);
            $this.text($this.text() == "Show" ? "Hide" : "Show");
            $($this.data("toggle")).slideToggle(200);
        });
    });
</script>


