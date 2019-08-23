;(function ($) {

    const PLUGIN_NAME = 'animatedPlaceholder';
    const DATA_KEY = 'zfin.' + PLUGIN_NAME;

    class AnimatedPlaceholder {
        constructor(input, config) {
            this._input = input;
            this._config = config;
            this._showing = 0;
            this._intervalId = null;
            this._placeholder = $();

            this._init();
            this._input.data(DATA_KEY, this);
        }

        play() {
            this.pause();
            this._placeholder.css('visibility', 'visible');
            this._intervalId = setInterval(this._next.bind(this), 5000);
        }

        pause() {
            this._placeholder.css('visibility', 'hidden');
            clearInterval(this._intervalId);
        }

        _next() {
            this._showing = (this._showing + 1) % this._config.values.length;
            this._placeholder.on('transitionend', () => {
                this._placeholder.text(this._config.values[this._showing]);
                this._placeholder.off('transitionend');
                this._placeholder.css('transform', 'perspective(300px) rotateX(0deg)');
            });
            this._placeholder.css('transform', 'perspective(300px) rotateX(90deg)');
        }

        _init() {
            this._placeholder = $('<span>')
                .css({
                    position: 'absolute',
                    left: 0,
                    top: 0,
                    bottom: 0,
                    right: 'auto',
                    zIndex: (parseInt(this._input.css('z-index'), 10) || 1) + 1,
                    paddingTop: this._input.css('padding-top'),
                    paddingRight: this._input.css('padding-right'),
                    paddingBottom: this._input.css('padding-bottom'),
                    paddingLeft: this._input.css('padding-left'),
                    fontSize: this._input.css('font-size'),
                    lineHeight: this._input.css('line-height'),
                    color: '#999999',
                    borderTopWidth: this._input.css('border-top-width'),
                    borderRightWidth: this._input.css('border-right-width'),
                    borderBottomWidth: this._input.css('border-bottom-width'),
                    borderLeftWidth: this._input.css('border-left-width'),
                    borderStyle: 'solid',
                    borderColor: 'transparent',
                    pointerEvents: 'none',
                    transition: 'transform 400ms',
                    transform: 'perspective(300px) rotateX(0deg)',
                    transformOrigin: 'center ' + this._input.css('padding-top'),
                })
                .text(this._config.values[this._showing])
                .insertAfter(this._input);
            this._input
                .on('input', () => this.pause())
                .on('focus', () => this.pause())
                .on('blur', () => {
                    if (!this._input.val()) {
                        this.play();
                    }
                });
            this.play();
        }
    }

    $.fn[PLUGIN_NAME] = function (config) {
        return this.each(function () {
            const data = $(this).data(DATA_KEY) || new AnimatedPlaceholder($(this), config);

            if (typeof config === 'string') {
                data[config]();
            }
        });
    };

})(jQuery);
