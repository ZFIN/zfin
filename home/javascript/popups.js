function randomUniqueID(prefix) {
    var id = prefix + Math.floor(Math.random() * 99999);
    // if the id exists, try again
    if (document.getElementById(id) !== null) {
        return randomUniqueID(prefix);
    } else {
        return id;
    }
}

function processPopupLinks(parent) {
    var selector = parent + ' .popup-link ';

    $(selector).each(function() {
        var div_id = randomUniqueID("popup-");
        $(this).attr('rel', '#' + div_id);
        var div_html = '' +
            '<div class="jq-modal" id="' + div_id + '">' +
            '  <div class="popup-content">' +
            '    Loading... <img src="/images/ajax-loader.gif"/>' +
            '  </div>' +
            '</div>';
        //append to the body so that we don't get unwanted css rules
        $('body').append(div_html);
        if ($(this).modal !== undefined) {
            this.style.display = 'inline';
        }
    });

    $(selector).click(function (event) {
        event.preventDefault();
        var overlay = $($(this).attr('rel'));
        $.ajax({
            url: $(this).attr('href'),
            success: function(data) {
                $('.popup-content', overlay).html(data);
                overlay.modal({
                    fadeDuration: 100
                });
            }
        });
    });
}