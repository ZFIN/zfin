/*
* A friendly wrapper around whatever autocomplete library ZFIN is currently using
*
* Right now this expects to get a JSON array of [{label: 'something', value: 'something'},{label: 'something', value: 'something'},...]
*
* */

(function ($) {
  $.fn.autocompletify = function (url, options) {

    var getSuggestionTemplate = function (withDirectLink) {
      return function (item) {

        var directLink = '';

        //only show a link for ZDB- and OBO formatted IDs
        if (withDirectLink && (item.id && item.id.startsWith('ZDB') || item.id.match('[A-Z]+:[0-9]+'))) {
          directLink = ' <a class="autocomplete-direct-link" ' +
            'title="Go directly to record" ' +
            'href="/' + item.id + '"><i class="fas fa-arrow-circle-right"></i></a>';
        }

        return '<p><span class="autocomplete-suggestion-text">' + item.label + '</span>' + directLink + '</p>';
      };
    };

    var defaults = {
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

    var hound = new Bloodhound({
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
    if (options.storageKey) {
      minLength = 0;
      source = function (q, sync, async) {
        // when there is no query return the suggestions from the store,
        // otherwise do a remote request with the query term
        if (q === '') {
          sync(JSON.parse(localStorage.getItem(options.storageKey)));
        } else {
          hound.search(q, sync, async);
        }
      };
      this.on('typeahead:select', function (evt, suggestion) {
        // get what's already stored or an empty array
        const stored = JSON.parse(localStorage.getItem(options.storageKey)) || [];
        // if the selected suggestion was already in the store, remove the old one
        const deduped = stored.filter(item => item.id !== suggestion.id);
        // put the suggestion at the front of the list
        deduped.unshift(suggestion);
        // if we have more than enough, remove the last
        if (deduped.length > options.limit) {
          deduped.pop();
        }
        // push the list back into the store
        localStorage.setItem(options.storageKey, JSON.stringify(deduped));
      });
    }

    this.typeahead({
      minLength: minLength,
    }, {
      name: 'search',
      displayKey: 'value',
      templates: options.templates,
      source: source,
      limit: options.limit,
    });

    const placeholders = this.data('placeholders');
    if (placeholders) {
      this.animatedPlaceholder(placeholders.split('|'));
    }

    return this;
  };
})(jQuery);

