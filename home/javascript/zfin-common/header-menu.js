import Popper from 'popper.js';

$(() => {
    document.querySelectorAll('header > .menu > li').forEach(menu => {
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
});

