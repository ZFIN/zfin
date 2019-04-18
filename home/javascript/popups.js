// TODO: this should be refactored to be a jquery plugin so the processPopupLinks function doesn't need to be exported

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
        if ($(this).hasClass('info-popup-link')) {
          $(this).append('<i class="fas fa-info-circle"></i>');
        } else if ($(this).hasClass('help-popup-link')) {
          $(this).append('<i class="fas fa-question-circle"></i>');
        } else if ($(this).hasClass('data-popup-link')) {
          $(this).append('<i class="far fa-window-maximize fa-sm"></i>');
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