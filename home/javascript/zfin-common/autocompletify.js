/*
* A friendly wrapper around whatever autocomplete library ZFIN is currently using
*
* Right now this expects to get a JSON array of [{label: 'something', value: 'something'},{label: 'something', value: 'something'},...]
*
* */

import Bloodhound from 'corejs-typeahead/dist/bloodhound';

(function ($) {
    $.fn.autocompletify = function (url, options) {

        const getSuggestionTemplate = function (withDirectLink) {
            return function (item) {

                let directLink = '';

                //only show a link for ZDB- and OBO formatted IDs
                if (withDirectLink && (item.id && item.id.startsWith('ZDB') || item.id.match('[A-Z]+:[0-9]+'))) {
                    directLink = ' <a class="autocomplete-direct-link" ' +
                        'title="Go directly to record" ' +
                        'href="/' + item.id + '"><i class="fas fa-arrow-circle-right"></i></a>';
                }

                return '<div><span class="autocomplete-suggestion-text">' + item.label + '</span>' + directLink + '</div>';
            };
        };

        const defaults = {
            displayKey: 'value',
            templates: {
                suggestion: getSuggestionTemplate(this.directLink)
            },
            limit: 5,
            directLink: false,
        };

        if (options && (!options.templates || !options.templates.suggestion) && options.directLink) {
            options.templates = $.extend({}, options.templates, {
                suggestion: getSuggestionTemplate(options.directLink)
            });
        }
        options = $.extend({}, defaults, options);

        const hound = new Bloodhound({
            datumTokenizer: Bloodhound.tokenizers.obj.whitespace('value'),
            queryTokenizer: Bloodhound.tokenizers.whitespace,
            rateLimitWait: 50,
            remote: {
                url: url,
                wildcard: '%QUERY',
                prepare: options.prepare,
            },
        });

        hound.initialize();

        let source = hound;
        let minLength = 1;
        if (options.defaultSuggestions) {
            minLength = 0;
            source = function (q, sync, async) {
                // when there is no query return the suggestions from the store,
                // otherwise do a remote request with the query term
                if (q === '') {
                    sync(options.defaultSuggestions);
                } else {
                    hound.search(q, sync, async);
                }
            };
        }

        this.typeahead({
            minLength: minLength,
        }, {
            name: 'search',
            displayKey: options.displayKey,
            templates: options.templates,
            source: source,
            limit: options.limit,
        });

        const placeholders = this.data('placeholders');
        if (placeholders) {
            this.animatedPlaceholder({ values: placeholders.split('|') });
        }

        return this;
    };
})(jQuery);

