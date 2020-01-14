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
 *  Options can also be provided by using data attributes on the selected element. For
 *  example, label can be specified by data-label="genes".
 */

;(function ($) {

    const PLUGIN_NAME = 'tableCollapse';
    const DEFAULTS = {
        headerRows: 1,
        rowsToShow: 5,
        label: '',
    };

    class TableCollapse {
        constructor(element, options) {
            this.element = element.is('table') ? element : element.find('table');

            this.options = $.extend({}, DEFAULTS, options, element.data());

            this._init();
        }

        _init() {
            const table = this.element;
            const rows = table.find("tr").slice(this.options.headerRows);
            const toggleContainer = table.parent().find(this.options.toggleContainer);
            let numVisibleRows = rows.length;
            const numRowsText = $(`<span>1 - ${numVisibleRows} of ${rows.length}</span>`).appendTo(toggleContainer);
            const showText = toggleContainer.length ?
                ' Show all' :
                ' Show all ' + rows.length + ' ' + this.options.label;
            const hideText = toggleContainer.length ?
                ' Show fewer' :
                ' Show first ' + this.options.rowsToShow + ' ' + this.options.label;

            if (rows.length > this.options.rowsToShow) {
                let removed = rows.slice(this.options.rowsToShow).detach();
                numRowsText.text(`1 - ${this.options.rowsToShow} of ${rows.length}`);

                const linkClass = 'table-collapse-link' + toggleContainer.length ? ' text-dark' : '';
                // add the show/hide controls and hook up events
                const showLink = $(`<a href="#" class="${linkClass}"></a>`);
                if (toggleContainer.length) {
                    showLink.appendTo(toggleContainer);
                } else {
                    showLink.insertAfter(table);
                }
                const linkIcon = $('<span class="fa-animation-container"><i class="fas fa-angle-down"></i></span>')
                    .appendTo(showLink);
                const linkText = $('<span></span>')
                    .text(showText)
                    .appendTo(showLink);
                showLink.on('click', (evt) => {
                    evt.preventDefault();
                    if (removed) {
                        removed.appendTo(rows.first().parent());
                        numRowsText.text(`1 - ${numVisibleRows} of ${rows.length}`);
                        removed = null;
                    } else {
                        removed = rows.slice(this.options.rowsToShow).detach();
                        numRowsText.text(`1 - ${this.options.rowsToShow} of ${rows.length}`);
                    }
                    linkIcon.toggleClass('fa-rotate-180');
                    linkText.text(removed ? showText : hideText);
                });
            }

            table.css({display: 'table'});
        }
    }

    $.fn[PLUGIN_NAME] = function (options) {

        return this.each(function () {
            // prevent multiple instantiations
            if (!$.data(this, 'plugin_' + PLUGIN_NAME)) {
                $.data(this, 'plugin_' + PLUGIN_NAME, new TableCollapse($(this), options));
            }
        });

    };

    $(function () {
        $("[data-table='collapse']").tableCollapse();
    });

})(jQuery);
