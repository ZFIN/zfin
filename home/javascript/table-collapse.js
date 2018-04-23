/*
 * This plugin allows a data table to be collapsed with a "show all" link at the bottom.
 * To use, call .tableCollapse() on the table to be collapsed. If the selection is not a
 * table element, it will search for tables within the selected element. Therefore, given
 * this...
 *
 *     <div id="foobar">
 *         <table>
 *             ...
 *         </table>
 *     </div>
 *
 *  The following are equivalent:
 *
 *      jQuery('#foobar').tableCollapse();
 *      jQuery('#foobar table').tableCollapse();
 *      jQuery('#foobar).find('table').tableCollapse();
 *
 *  Options can be passed to tableCollapse() as an object. The following properties are
 *  recognized:
 *
 *      headerRows: Number of rows to skip before finding rows to collapse. Default: 1
 *      rowsToShow: Number of rows (not including header rows) to show when the table
 *                  is collapsed. Default 5
 *      label: Text to append to the 'show all' link. For example if this parameter is
 *             set to 'targeted genes', the link under the table would read 'Show all N
 *             targeted genes' when the table is collapsed, and 'Show first N targeted
 *             genes' when the table is expanded.
 *
 *  Options can also be provided by using a 'data-table-collapse' attribute on the
 *  selected element. The attribute should be set to a JSON-formatted string.
 */

;(function ($) {

    var pluginName = 'tableCollapse',
        defaults = {
            headerRows: 1,
            rowsToShow: 5,
            label: ''
        };

    function TableCollapse(element, options) {
        this.element = element.is('table') ? element : element.find('table');

        this.options = $.extend({}, defaults, options, element.data('table-collapse'));

        this.init();
    }

    TableCollapse.prototype = {

        init: function() {
            var table = this.element,
                startRow = this.options.headerRows,
                endRow = startRow + this.options.rowsToShow,
                rows = table.find("tr"),
                showText = ' Show all ' + (rows.length - startRow) + ' ' + this.options.label,
                hideText = ' Show first ' + this.options.rowsToShow + ' ' + this.options.label,
                tbody, showLink, linkIcon, linkText;

            if (rows.length > endRow) {
                // create a new tbody element and add the collapsible rows to it
                tbody = $('<tbody></tbody>').appendTo(table);
                rows.slice(endRow).appendTo(tbody);
                tbody.hide();

                // add the show/hide controls and hook up events
                showLink = $('<a class="table-collapse-link"></a>')
                    .attr('href', '#')
                    .insertAfter(table);
                linkIcon = $('<span class="fa-animation-container"><i class="fas fa-caret-down"></i></span>')
                    .appendTo(showLink);
                linkText = $('<span></span>')
                    .text(showText)
                    .appendTo(showLink);
                showLink.on('click', function (evt) {
                    evt.preventDefault();
                    linkText.text(tbody.is(':visible') ? showText : hideText);
                    linkIcon.toggleClass('fa-rotate-180');
                    tbody.toggle();
                });
            }
        }

    };

    $.fn[pluginName] = function (options) {

        return this.each(function () {
            // prevent multiple instantiations
            if (!$.data(this, 'plugin_' + pluginName)) {
                $.data(this, 'plugin_' + pluginName, new TableCollapse($(this), options));
            }
        });

    };

})(jQuery);
