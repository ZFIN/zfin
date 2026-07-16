$(function () {
    $('.back-to-top-link')
        .tipsy({gravity: 'n'})
        .on('click', function () {
            this.blur();
        });
    const $body = $('body');
    if ($body.hasClass('data-page')) {
        $body
            .scrollspy({
                target: '.data-page-nav-container',
                offset: 120,
            });
        // event fires on window, not body. see https://github.com/twbs/bootstrap/issues/20086
        $(window)
            .on('activate.bs.scrollspy', function (event, { relatedTarget }) {
                history.replaceState(null, '', relatedTarget);
            });
    }
});
