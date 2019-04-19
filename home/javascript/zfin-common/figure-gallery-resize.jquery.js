;(function ($) {

    var pluginName = 'figureGalleryResize';

    function modalIsShown($modal) {
        return ($modal.data('bs.modal') || {}).isShown;
    }

    function doResize($modal) {
        if (!modalIsShown($modal)) {
            return;
        }
        var totalHeight = $(window).height();
        var padding = 90;
        var navigationArrowSize = 150;
        var headerHeight = $modal.find('.modal-header').outerHeight();
        var modalBody = $modal.find('.modal-body');
        var modalBackdrop = $modal.find('.modal-backdrop');
        var modalDialog = $modal.find('.modal-dialog');
        var modalImage = $modal.find('.figure-gallery-modal-image')[0];
        var availableHeight = totalHeight - headerHeight - padding;
        var availableWidth = $(window).width() - padding - navigationArrowSize;
        var newHeight;
        if (modalImage.naturalWidth / modalImage.naturalHeight > availableWidth / availableHeight) {
            newHeight = availableWidth * modalImage.naturalHeight / modalImage.naturalWidth;
        } else {
            newHeight = availableHeight;
        }
        newHeight = Math.min(newHeight, modalImage.naturalHeight);
        modalBody.height(newHeight);
        modalDialog.height(newHeight + headerHeight + 30);
        modalBackdrop.height(totalHeight);
    }

    $.fn[pluginName] = function () {

        return this.each(function () {
            doResize($(this));
        });

    };

})(jQuery);

