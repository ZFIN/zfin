/*
* A friendly wrapper around whatever autocomplete library ZFIN is currently using
*
* Right now this expects to get a JSON array of [{label: 'something', value: 'something'},{label: 'something', value: 'something'},...]
*
* */

(function ($) {
    $.fn.autocompletify = function (url, options) {

        var defaults = {
            templates: {
                suggestion: function (item) {
                    return "<p>" + item.label + "</p>";
                }
            },
            limit: 5
        };

        options = $.extend({}, defaults, options);

        var hound = new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            rateLimitWait: 50,
            limit: options.limit,
            remote: url
        });

        hound.initialize();

        this.typeahead(null, {
            name: 'search',
            displayKey: 'value',
            templates: options.templates,
            source: hound.ttAdapter()
        });

    };
})(jQuery);

