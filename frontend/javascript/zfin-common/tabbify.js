/* Simple replacement for jQueryTOOLS tabs.
 * Just the basics, nothing fancy. Use it like this:
 *
 *   jQuery("tab link selector").tabbify("tab pane selector");
 *
 * Make sure that the tab links point to the pane id in href.
 */
(function ($) {
    $.fn.tabbify = function (paneSelector) {

        var tabs = this,
            panes = $(paneSelector);

        panes.hide();

        // set up the click handler
        this.click(function (event) {
            event.preventDefault();
            var $tab = $(this);
            tabs.removeClass("current");
            panes.hide();
            $tab.addClass("current");
            $($tab.attr("href")).show();
        });

        // on load select the first tab
        $(function () {
            tabs.first().addClass("current");
            panes.first().show();
        });

    };
})(jQuery);