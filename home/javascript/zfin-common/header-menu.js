import Popper from 'popper.js';

$(() => {
    document.querySelectorAll('header .reference').forEach(menu => {
        const dropdown = menu.querySelector('.dropdown');
        if (!dropdown) {
            return;
        }
        new Popper(menu, dropdown, {
            placement: 'bottom-start',
            positionFixed: true,
            modifiers: {
                offset: {
                    offset: 1,
                }
            }
        });
        menu.addEventListener('mouseover', () => dropdown.style.visibility = 'unset');
        menu.addEventListener('mouseout', () => dropdown.style.visibility = 'hidden');
    });

    $('header .jump-to-pub').on('submit', function (e) {
        e.preventDefault();
        let zdbId = $(this).find('input[type="text"]').val();
        if (zdbId.indexOf('ZDB-PUB-') !== 0) {
            zdbId = 'ZDB-PUB-' + zdbId;
        }
        window.location.href = '/action/curation/' + zdbId;
    });

    $('.fs-autocomplete')
        .find('input[type="text"]')
            .autocompletify('/action/quicksearch/autocomplete?q=%QUERY', {
                templates: {
                    suggestion: function (item) {
                        return (`
                          <a href="${item.url}">
                            <span>${item.label}</span>
                            <span class="category">${item.category}</span>
                          </a>
                        `);
                    },
                    footer: function ({query}) {
                        return (`
                          <a href="/search?q=${query}" class="tt-search-link">
                            All results for ${query}
                            <span>&rarr;</span>
                          </a>
                        `);
                    },
                },
            });
});

