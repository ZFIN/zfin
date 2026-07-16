$(function () {
    $(document).on('click.popupLink', '.popup-link', function (event) {
        event.preventDefault();
        $(this).modal({
            modalClass: 'jq-modal',
            fadeDuration: 100,
            spinnerHtml: '<i class="fas fa-spinner fa-spin"></i> Loading',
        });
    })
});
