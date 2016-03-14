<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="doid" rtexprvalue="true" required="true" %>

<script src="/javascript/phenogrid/phenogrid-bundle.js"></script>
<script src="/javascript/phenogrid/phenogrid_config.js"></script>
<link rel="stylesheet" type="text/css" href="/javascript/phenogrid/phenogrid-bundle.css"/>

<style>
    body {margin-top: 80px;}

</style>

<script>


    jQuery(document).ready(function() {

        $.ajax({
            dataType: "json",
            url: "https://beta.monarchinitiative.org/disease/${doid}/phenotype_list.json",
            success: function(data) {

                $('#disease-phenotype-spinner').hide();

                if (data.phenotype_list.length == 0) {
                    $('#phen_vis').hide();
                    $('#disease-phenotype-no-data-tag').show();

                } else {
                    var phenotypes = data.phenotype_list.map(function (p) { return p.id; });

                    Phenogrid.createPhenogridForElement(document.getElementById('phen_vis'), {
                        serverURL : "https://beta.monarchinitiative.org",
                        phenotypeData: phenotypes,
                        targetGroupList: [
                            {name: "Danio rerio", taxon: "7955", crossComparisonView: true, active: true},
                            {name: "Homo sapiens", taxon: "9606", crossComparisonView: true, active: true},
                            {name: "Mus musculus", taxon: "10090", crossComparisonView: true, active: true}
                        ]
                    });
                }



            }
        });

    });



</script>


<div class="summary">

    <span class="summaryTitle" id="phenogrid-title">DISEASE PHENOTYPE</span>
    <img id="disease-phenotype-spinner" src="/images/ajax-loader.gif" alt="loading...">
    <span  class="no-data-tag" id="disease-phenotype-no-data-tag" style="display:none">No data available</span>
    <div id="phen_vis" class="clearfix"></div>
</div>
