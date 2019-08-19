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
                zIndex: (parseInt($input.css('z-index'), 10) || 1) + 1,
                height: $input.height(),
                padding: $input.css('padding'),
                fontSize: $input.css('font-size'),
                color: '#999999',
                borderWidth: $input.css('border-width'),
                borderStyle: 'solid',
                borderColor: 'transparent',
                pointerEvents: 'none',
                transition: 'transform 400ms',
                transform: 'perspective(300px) rotateX(0deg)',
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
