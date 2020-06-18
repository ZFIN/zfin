$(function () {
    $('.back-to-top-link')
        .tipsy({gravity: 'n'})
        .on('click', function () {
            this.blur();
        });
});
