/*
 * This plugin allows a data list to be collapsed with a "show all" link at the bottom.
 * To use, call .listCollapse() on the list to be collapsed. If the selection is not a
 * list element, it will search for lists within the selected element. Therefore, given
 * this...
 *     <div id="foobar">
 *         <ul>
 *             <li> blah blah </li>
 *             <li> blah blah </li>
 *             ...
 *             <li> blah blah </li>
 *         </ul>
 *     </div>
 *
 *  The following are equivalent:
 *
 *      jQuery('#foobar').listCollapse();
 *      jQuery('#foobar ul').listCollapse();
 *      jQuery('#foobar).find('ul').listCollapse();
 *
 *  Options can be passed to listCollapse() as an object. The following properties are
 *  recognized:
 *
 *      itemsToShow: Number of items to show. Default 5
 *      label: Text to append to the 'show all' link. For example if this parameter is
 *             set to 'targeted genes', the link under the list would read 'Show all N
 *             targeted genes' when the list is collapsed, and 'Show first N targeted
 *             genes' when the list is expanded.
 *
 *  Options can also be provided by using a 'data-list-collapse' attribute on the
 *  selected element. The attribute should be set to a JSON-formatted string.
 */

;(function ($) {

    var pluginName = 'listCollapse',
        defaults = {
            itemsToShow: 5,
            label: ''
        };

    function ListCollapse(element, options) {
        this.element = element.is('ul') ? element : element.find('ul');

        this.options = $.extend({}, defaults, options, element.data('list-collapse'));

        this.init();
    }

    ListCollapse.prototype = {

        init: function() {
            var list = this.element,
                visible = false,
                startItem = 0,
                endItem = startItem + this.options.itemsToShow,
                items = list.find("li"),
                showText = ' Show all ' + (items.length - startItem) + ' ' + this.options.label,
                hideText = ' Show first ' + this.options.itemsToShow + ' ' + this.options.label,
                showLink, linkIcon, linkText;

            if (endItem && items.length > endItem) {
                //hide the list items above endItem
/*
                items.slice(endItem).forEach(function(item) {
                    item.hide();
                });
*/

                $.each(items.slice(endItem), function() {
                    $(this).hide();}
                );

                // add the show/hide controls and hook up events
                showLink = $('<a class="table-collapse-link"></a>')
                    .attr('href', '#')
                    .insertAfter(list);
                linkIcon = $('<i class="fa fa-caret-down"></i>')
                    .appendTo(showLink);
                linkText = $('<span></span>')
                    .text(showText)
                    .appendTo(showLink);
                showLink.on('click', function (evt) {
                    evt.preventDefault();
                    linkText.text(visible ? showText : hideText);
                    visible = !visible;
                    linkIcon.toggleClass('fa-rotate-180');
                    $.each(items.slice(endItem), function() {
                        $(this).toggle();
                    });
                });
            }
        }

    };

    $.fn[pluginName] = function (options) {

        return this.each(function () {
            // prevent multiple instantiations
            if (!$.data(this, 'plugin_' + pluginName)) {
                $.data(this, 'plugin_' + pluginName, new ListCollapse($(this), options));
            }
        });

    };

})(jQuery);
