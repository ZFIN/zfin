/* Duplicates form elements
 */
(function ($) {
    $.fn.multirowTable = function (rowSelector, buttonLabel, callback) {

        var parent = this;

        function replaceNumber(newNum) {
            return function(idx, val) {
                return val.replace('0', newNum);
            };
        }

        function addRow(evt) {
            evt.preventDefault();
            var rows = parent.find(rowSelector);
            var newRow = rows.first().clone();
            newRow.find(':input')
                .val('')
                .attr('name', replaceNumber(rows.length))
                .attr('id', replaceNumber(rows.length));
            var separator = $('<hr>');
            rows.last().after(separator);
            separator.after(newRow);
            if (typeof callback === 'function') {
                // evaluate callback with new row as `this`
                callback.call(newRow);
            }
        }

        $(function() {
            parent.append(
                $("<a href class=\"add-row\">")
                    .click(addRow)
                    .append("<i class=\"fa fa-plus-circle fa-lg\"></i> " + buttonLabel)
            );
        });

        return this;
    };
})(jQuery);