
<div>
    <form id="pub-advanced-form">

        <div class="form-group">
            <label class="control-label" for="pub-advanced-abstract">Title</label>
            <div class="controls">
                <input id="pub-advanced-title" class="input advanced-text-input" type="text"/>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label" for="pub-advanced-author">Author</label>
            <div class="controls">
                <input id="pub-advanced-author" class="input advanced-text-input" type="text"/>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label" for="pub-advanced-abstract">Abstract</label>
            <div class="controls">
                <input id="pub-advanced-abstract" class="input advanced-text-input" type="text"/>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label">PET Date from</label>
            <div class="controls">
                <input id="pub-advanced-start-date" name="start-date" placeholder="YYYY-MM-DD" class="input datepicker"
                       style="width: 120px;" type="text" data-provide="datepicker" data-date-autoclose="true" data-date-format="yyyy-mm-dd" />
            </div>
            <label class="control-label" for="pub-advanced-end-date">To</label>
            <div class="controls">
                <input id="pub-advanced-end-date" name="end-date" placeholder="YYYY-MM-DD" class="input datepicker"
                       style="width: 120px;" type="text" data-provide="datepicker" data-date-autoclose="true" data-date-format="yyyy-mm-dd"/>
            </div>
        </div>

        <div class="form-group">
            <label class="control-label">&nbsp;</label>
            <div class="controls">
                <button id="pub-advanced-button" type="submit" class="btn btn-primary" value="Go">Search</button>
                <button type="button" class="btn" onClick="jQuery('#advanced-container').slideToggle(200);">Close</button>
            </div>
        </div>


    </form>
    <script>
        jQuery('#pub-advanced-button').click(function(event) {

            event.preventDefault();

            var fields = [
                {
                    id: 'pub-advanced-title',
                    field: 'name_t',
                    type: 'string'
                },
                {
                    id: 'pub-advanced-author',
                    field: 'author_string',
                    type: 'string'
                },
                {
                    id: 'pub-advanced-abstract',
                    field: 'abstract',
                    type: 'string'
                },
                {
                    startId: 'pub-advanced-start-date',
                    endId: 'pub-advanced-end-date',
                    field: 'pet_date',
                    type: 'date'
                }
            ]

            submitAdvancedQuery(fields);

        });
    </script>

</div>

<%--
<script>
    jQuery(document).ready(function() {
       jQuery('')
    });

</script>--%>
