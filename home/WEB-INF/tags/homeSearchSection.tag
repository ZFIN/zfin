<form method="GET" action="/search" name="search" accept-charset="utf-8" id="query-form">
    <label for="search-query-input">Search ZFIN</label>

    <input class="search-form-input form-control"
           placeholder="bmp2a, hindbrain development disrupted, pax morpholino"
           name="q" id="search-query-input" autocomplete="off" type="text"/>

    <button type="submit" class="btn btn-default btn-zfin">Go</button>



    <a href="http://wiki.zfin.org/display/general/ZFIN+Single+Box+Search+Help" target="newWindow">
        <i class="fa fa-question-circle fa-lg"></i>
    </a>
</form>
<script>
    jQuery(document).ready(function() {
        jQuery('#search-query-input').autocompletify('/action/quicksearch/autocomplete?q=%QUERY');
    });
    $('#search-query-input').bind("typeahead:select", function() {
        $('#query-form').submit();
    });
</script>
