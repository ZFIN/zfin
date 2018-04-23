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
 *      show: Number of items to show. Default 5
 *
 *  Options can also be provided by using a 'data-list-collapse' attribute on the
 *  selected element. The attribute should be set to a JSON-formatted string.
 */

;(function ($) {

    var pluginName = 'listCollapse',
        defaults = {
            show: 5
        };

    function ListCollapse(element, options) {
        this.element = element.is('ul') ? element : element.find('ul');

        this.options = $.extend({}, defaults, options, element.data());

        this.init();
    }

    ListCollapse.prototype = {

        init: function() {
            var opts = this.options;
            var list = this.element;
            var visible = false;
            var ajaxLoaded = false;
            var endItem = opts.show;
            var items = list.find("li");
            var showText = '(all ' + (opts.count || items.length) + ') ';
            var hideText = '';
            var iconClass = 'fa-caret-right';
            var ajaxContainer, showLink, linkIcon, linkText;

            if (endItem && items.length > endItem) {
                //hide the list items above endItem
                $(items[endItem - 1]).addClass('no-comma');
                $.each(items.slice(endItem), function() {
                    $(this).hide();}
                );

                if (opts.url) {
                    ajaxContainer = $('<div style="display: none"></div>').insertAfter(list);
                }

                // add the show/hide controls and hook up events
                showLink = $('<a class="table-collapse-link"></a>')
                    .attr('href', '#')
                    .insertAfter(list);
                linkText = $('<span>' + showText + '</span>');
                linkIcon = $('<span class="fa-animation-container"><i class="fas ' + iconClass + '"></i></span>');
                showLink
                    .append(linkText)
                    .append(linkIcon)
                    .on('click', function (evt) {
                        evt.preventDefault();
                        linkText.text(visible ? showText : hideText);
                        visible = !visible;
                        linkIcon.toggleClass('fa-rotate-180');
                        if (opts.url && !ajaxLoaded) {
                            ajaxContainer.load(opts.url);
                        }
                        if (opts.url) {
                            list.toggle();
                            ajaxContainer.toggle();
                        } else {
                            $(items[endItem - 1]).toggleClass('no-comma');
                            $.each(items.slice(endItem), function () {
                                $(this).toggle();
                            });
                        }
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

    $(function () {
      $("[data-toggle='collapse']").listCollapse();
    });

})(jQuery);
