import Popper from 'popper.js';

$(() => {
    const poppers = [];
    document.querySelectorAll('header .reference').forEach(menu => {
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
        menu.addEventListener('mouseover', () => dropdown.style.visibility = 'unset');
        menu.addEventListener('mouseout', () => dropdown.style.visibility = 'hidden');
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
                storageKey: 'fs-autocomplete-defaults',
            })
            .on('typeahead:select', function (evt, suggestion) {
                ga('send', 'event', 'FS autocomplete', 'Go to page', `${suggestion.value} [${suggestion.category}]`);
            });
    });
});

