<%@ include file="/WEB-INF/jsp-include/tag-import.jsp" %>

<%@ attribute name="doid" rtexprvalue="true" required="true" %>

<script src="/javascript/phenogrid/js/jquery-ui-1.10.3.custom.min.js"></script>
<script src="/javascript/phenogrid/js/d3.min.js"></script>
<script src="/javascript/phenogrid/js/jshashtable.js"></script>
<script src="/javascript/phenogrid/js/stickytooltip.js"></script>
<script src="/javascript/phenogrid/js/phenogrid_config.js"></script>
<script src="/javascript/phenogrid/js/phenogrid.js"></script>
<script src="/javascript/phenogrid/js/render.js"></script>
<link rel="stylesheet" type="text/css" href="/javascript/phenogrid/css/jquery-ui.css"/>
<link rel="stylesheet" type="text/css" href="/javascript/phenogrid/css/instructions.css"/>
<link rel="stylesheet" type="text/css" href="/javascript/phenogrid/css/phenogrid.css"/>
<link rel="stylesheet" type="text/css" href="/javascript/phenogrid/css/stickytooltip.css"/>

<script>

    jQuery(document).ready(function() {
        $.ajax({
            dataType: "json",
            url: "http://tartini.crbs.ucsd.edu/disease/${doid}.json",
            success: function(data) {
                var phenotypes = [];

                $.each(data.phenotype_list, function() {
                    phenotypes.push(this.id);
                });


                $("#phen_vis").phenogrid({serverURL :
                        "http://tartini.crbs.ucsd.edu", phenotypeData: phenotypes,targetSpeciesName: "Danio rerio" });


            }
        });

    });


</script>

<div id="viscontent" style="height: 650px;">
    <div id="phen_vis"></div>
</div>
<div class="text"></div>
