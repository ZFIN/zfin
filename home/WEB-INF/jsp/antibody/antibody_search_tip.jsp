<%@ page import="org.zfin.properties.ZfinProperties" %>
<script type="text/javascript" src="/javascript/control.modal.js"></script>
<a id="search-tips" href="#searchTips">Search Tips</a>

<div id="searchTips">

    <style>
        a.close_link {
            color: #333333;
            float: right;
            font-family: sans-serif;
            font-size: Large;
            font-weight: bold;
            text-decoration: none;
        }
    </style>

    <a class="close_link" href="javascript:;" onclick="parent.Control.Modal.close();" target="_top">x</a>
    <br>

    <h2 style="text-align:center;">Antibody Search Tips</h2>
    <ol>
        <li>
            To find immuno-labeling described in older publications, or in mutant fish, try a
            <a href="/<%= ZfinProperties.getWebDriver()%>?MIval=aa-xpatselect.apg" target=new>
                Gene Expression Search</a>
            with Assay set to immunohistochemistry (IHC) or Western blot (WB).
        </li>
        <li>
            Need an antibody to label a favorite cellular substructure? Try Site Search (in the upper right corner of
            every ZFIN page). Antibody search does not yet
            have an entry field for cellular components (growth cone, plasma membrane, etc.). This feature will be part
            of a future release. For now, you can often find what you need using Site Search. Enter a desired cellular
            component in Site Search, then look in the Antibodies section of the Site Search results page. For more
            information on Cellular Components, check out
            <a href="http://amigo.geneontology.org/cgi-bin/amigo/browse.cgi?open_1=all&ont=cellular_component&speciesdb=all&taxid=all&tree_view=full&session_id=8092amigo1222215981&action=filter">
                AmiGO
            </a>
            </li>
        <li>
            We are very interested in peoples' experiences with antibodies! If
            you try out an antibody and find that it works on zebrafish, let us
            know. We can share this information with the community and everyone
            will benefit. Thanks!
        </li>
    </ol>
    <p>If you have questions or suggestions, please <a href="mailto:zfinadmn@zfin.org">contact us</a>.
    </p>
</div>
<script type="text/javascript">
    // used as part of control.modal.js.
    // Needs to be out here because functions weird if within MIVAR apg calls.
    new Control.Modal('search-tips', {opacity: 0.3, width: 400, height: 600, iframe: true});
</script>
