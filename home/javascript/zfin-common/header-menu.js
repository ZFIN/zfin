import Popper from 'popper.js';

$(() => {
    document.querySelectorAll('header .reference').forEach(menu => {
        const dropdown = menu.querySelector('.dropdown');
        if (!dropdown) {
            return;
        }
        new Popper(menu, dropdown, {
            placement: 'bottom-start',
            modifiers: {
                offset: {
                    offset: 1,
                }
            }
        });
        menu.addEventListener('mouseover', () => dropdown.style.visibility = 'unset');
        menu.addEventListener('mouseout', () => dropdown.style.visibility = 'hidden');
    });

    $('header input[type="text"]')
        .autocompletify('/action/quicksearch/autocomplete?q=%QUERY')
        .on('typeahead:select', function () {
            $(this).closest('form').submit();
        });
});

