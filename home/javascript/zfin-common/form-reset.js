/*
 * Reset forms The ZFIN Way (tm). Clears text inputs, selects first option from selects. Pass an
 * object mapping element ids to values for custom reset values. Passing a function will execute
 * the function with the element passed as `this`. The return value of the function is not utilized.
 */

(function ($) {
    $.fn.resetForm = function (customValues) {
        this.each(function () {
            var $form = $(this);
            $form.find('input:text').val('');
            $form.find('input:checkbox').removeAttr('checked');
            $form.find('input:radio').prop('checked', false);
            $form.find('select').prop("selectedIndex", 0);
            $.each(customValues, function (id, value) {
                var $el = $('#' + id);
                if (typeof value === 'function') {
                    value.call($el[0]);
                } else {
                    $el.val(value);
                }
            });
        });
        return this;
    };
}(jQuery));