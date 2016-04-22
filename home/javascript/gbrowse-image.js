/* == SUMMARY ==
 *
 * A jQuery plugin for loading and displaying GBrowse images. It handles creating the link
 * and image tag structure as well as hiding the hosting element until the image has loaded.
 * This means that a broken image will not be displayed if the GBrowse server is down for
 * whatever reason or if the client makes a request that GBrowse cannot handle (resulting in
 * GBrowse returning 500 status code.
 *
 *
 * == API ==
 *
 *   .gbrowseImage( options )
 *     Adds a GBrowse image to each of the matched elements. The matched element will be hidden,
 *     a link and image element will be added, and the matched element will be shown again (if it
 *     was visible initially) once the GBrowse image successfully loads.
 *
 *     Options:
 *       width          desired width of the GBrowse image in pixels (default: 400)
 *       imageUrl       URL of the GBrowse image to display.
 *       linkUrl        URL to use as the href of the link which wraps the GBrowse image
 *       imageTarget    by default, the GBrowse image will be appended to the matched element. If
 *                      this options is specified, the image will be appended to a child of the
 *                      matched element which matches this selector instead.
 *       success        a function that will be called when the gbrowse image successfully loads
 *
 *     Options can also be specified as a JSON string stored in the matched element's
 *     'data-gbrowse-image' attribute. This can be useful if many GBrowse images with different
 *     options appear on a single page.
 *
 * == EXAMPLES ==
 *
 *  Add GBrowse image to the .gbrowse-image element. The entire #gbrowse-container is shown when
 *  the image loads:
 *
 *    <div id="gbrowse-container">
 *      <h2>Cool GBrowse Image!</h2>
 *      <div class="gbrowse-image"/>
 *    </div>
 *    <script>
 *      $('#gbrowse-container').gbrowseImage({
 *        width: 600,
 *        imageUrl: '...',
 *        linkUrl: '...',
 *        imageTarget: '.gbrowse-image'
 *      });
 *    </script>
 *
 *  Using data attribute to pass options:
 *
 *    <div class='gbrowse-image'
 *         data-gbrowse-image='{"imageUrl": "/image1", "linkUrl": "/link1"}'/>
 *    <div class='gbrowse-image'
 *         data-gbrowse-image='{"imageUrl": "/image2", "linkUrl": "/link2"}'/>
 *    <script>
 *      $('.gbrowse-image').gbrowseImage();
 *    </script>
 *
 */

;(function ($, undefined) {

    var pluginName = 'gbrowseImage',
        defaults = {
            width: 400
        };

    function GbrowseImage(element, options) {
        this.element = element;

        this.options = $.extend({}, defaults, options, this.element.data('gbrowse-image'));

        this.init();
    }

    GbrowseImage.prototype = {

        init: function() {
            var host = this.element,
                target = (this.options.imageTarget === undefined) ? host : host.find(this.options.imageTarget),
                wasDisplayed = host.css('display') !== 'none', // using this instead of :visible because we don't want to consider parent visibility
                sep = (this.options.imageUrl.indexOf('?') > -1) ? '&' : '?',
                imgSrc = this.options.imageUrl + sep + 'width=' + this.options.width,
                success = this.options.success,
                link, img;

            host.hide();

            if (!this.options.imageUrl) {
                return;
            }

            if (this.options.build) {
                $('<span>')
                    .addClass('gbrowse-source-label')
                    .text('Genome Build: ' + this.options.build)
                    .appendTo(target);
            }

            link = $('<a>')
                .attr('href', this.options.linkUrl)
                .appendTo(target);

            img = $('<img>')
                .attr('src', imgSrc)
                .appendTo(link);

            // when the image loads, show the hosting element if it was previously displayed
            img.on('load', function () {
                if (wasDisplayed) {
                    host.show();
                }
                if (typeof success === 'function') {
                    success();
                }
            });

            // if the image was already loaded (e.g. due to caching), manually trigger the load event
            if (img[0].complete) {
                img.trigger('load');
            }
        }

    };

    $.fn[pluginName] = function (options) {

        return this.each(function () {
            // prevent multiple instantiations
            if (!$.data(this, 'plugin_' + pluginName)) {
                $.data(this, 'plugin_' + pluginName, new GbrowseImage($(this), options));
            }
        });

    };

})(jQuery);