import Popper from 'popper.js';

$(() => {
    const poppers = [];
    $('header .reference').each(function () {
        const menu = this;
        const dropdown = menu.querySelector('.dropdown');
        if (!dropdown) {
            return;
        }
        poppers.push(new Popper(menu, dropdown, {
            placement: dropdown.classList.contains('left') ? 'bottom-end' : 'bottom-start',
            positionFixed: true,
            modifiers: {
                offset: {
                    offset: 1,
                },
                preventOverflow: {
                    padding: 0,
                },
                flip: {
                    enabled: false,
                },
            }
        }));
        $(menu)
            .on('mouseover', () => dropdown.style.visibility = 'visible')
            .on('mouseout', () => dropdown.style.visibility = 'hidden');
    });

    $('.mobile-menu').on('click', function (e) {
        e.preventDefault();
        $('header > .menu').slideToggle({
            start: function () { $(this).css({display: 'flex'}) },
            done: function () { poppers.forEach(popper => popper.scheduleUpdate()) },
        });
    });

    $('header .jump-to-pub').on('submit', function (e) {
        e.preventDefault();
        let zdbId = $(this).find('input[type="text"]').val();
        if (zdbId.indexOf('ZDB-PUB-') !== 0) {
            zdbId = 'ZDB-PUB-' + zdbId;
        }
        window.location.href = '/action/curation/' + zdbId;
    });

    $('.fs-autocomplete').each(function () {
        const autocomplete = $(this);
        const $input = autocomplete.find('input[type="text"]');
        const limit = 5;
        const storageKey = 'fs-autocomplete-defaults';

        autocomplete.find('.category-dropdown a').on('click', function (e) {
            e.preventDefault();
            const category = $(this).text();
            autocomplete.find('.category-label').text(category);
            autocomplete.find('input[name="category"]').val(category === 'Any' ? '' : category);
            $input.animatedPlaceholder('pause');
        });
        const allResultsLink = ({query}) => {
            if (!query) {
                return '';
            }
            return (`
              <a href="/search?q=${query}" class="tt-search-link">
                All results for ${query}
                <span>&rarr;</span>
              </a>
            `);
        };
        $input
            .autocompletify('/action/quicksearch/autocomplete?q=%QUERY', {
                limit: limit,
                templates: {
                    suggestion: item => (`
                        <a href="${item.url}">
                            <span>${item.label}</span>
                            <span class="details">${item.category}</span>
                        </a>
                    `),
                    footer: allResultsLink,
                    empty: allResultsLink,
                },
                prepare: function (query, settings) {
                    settings.url = settings.url.replace('%QUERY', query);
                    const category = autocomplete.find('input[name="category"]').val();
                    if (category) {
                        settings.url += '&category=' + category;
                    }
                    return settings;
                },
                defaultSuggestions: JSON.parse(localStorage.getItem(storageKey)),
            })
            .on('typeahead:select', function (evt, suggestion) {
                // send a google analytics event
                ga('send', 'event', 'FS autocomplete', 'Go to page', `${suggestion.value} [${suggestion.category}]`);

                // get what's already stored or an empty array
                const stored = JSON.parse(localStorage.getItem(storageKey)) || [];
                // if the selected suggestion was already in the store, remove the old one
                const deduped = stored.filter(item => item.id !== suggestion.id);
                // put the suggestion at the front of the list
                deduped.unshift(suggestion);
                // if we have more than enough, remove the last
                if (deduped.length > limit) {
                    deduped.pop();
                }
                // push the list back into the store
                localStorage.setItem(storageKey, JSON.stringify(deduped));
            });
    });
});

