;(function ($) {

    const pluginName = 'animatedPlaceholder';

    function addPlaceholder($input, values) {
        let idx = 0;
        let intervalId = null;

        const startAnimation = () => {
            pauseAnimation();
            intervalId = setInterval(function () {
                idx = (idx + 1) % values.length;
                $placeholder.on('transitionend', function () {
                    $placeholder.text(values[idx]);
                    $placeholder.off('transitionend');
                    $placeholder.css('transform', 'perspective(300px) rotateX(0deg)');
                });
                $placeholder.css('transform', 'perspective(300px) rotateX(90deg)');
            }, 5000);
        };

        const pauseAnimation = () => {
            clearInterval(intervalId);
        };

        const $placeholder = $('<span>')
            .css({
                position: 'absolute',
                left: 0,
                top: 0,
                bottom: 0,
                right: 'auto',
                zIndex: (parseInt($input.css('z-index'), 10) || 1) + 1,
                paddingTop: $input.css('padding-top'),
                paddingRight: $input.css('padding-right'),
                paddingBottom: $input.css('padding-bottom'),
                paddingLeft: $input.css('padding-left'),
                fontSize: $input.css('font-size'),
                lineHeight: $input.css('line-height'),
                color: '#999999',
                borderTopWidth: $input.css('border-top-width'),
                borderRightWidth: $input.css('border-right-width'),
                borderBottomWidth: $input.css('border-bottom-width'),
                borderLeftWidth: $input.css('border-left-width'),
                borderStyle: 'solid',
                borderColor: 'transparent',
                pointerEvents: 'none',
                transition: 'transform 400ms',
                transform: 'perspective(300px) rotateX(0deg)',
                transformOrigin: 'center ' + $input.css('padding-top'),
            })
            .text(values[idx]);
        $placeholder.insertAfter($input);
        $input
            .on('input', function () { $placeholder.css('visibility', $input.val() === '' ? 'visible' : 'hidden'); })
            .on('focus', function () { pauseAnimation(); })
            .on('blur', function () { startAnimation(); });
        startAnimation();
    }

    $.fn[pluginName] = function (placeholders) {
        return this.each(function () {
            addPlaceholder($(this), placeholders);
        });

    };

})(jQuery);
