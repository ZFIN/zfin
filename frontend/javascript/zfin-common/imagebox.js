/** FILE: imageBox.js

 Defines an imageBox class that will manage display of image thumbnails
 built from an array of image objects defined with:

 {
 "imgThumb":"$1",
 "imgZdbId":"$2"
 }

 Because of the way events are handled, the instantiated class has to be named
 imageBox.

 A good solution might be to refactor this into a singleton starting with
 something like:

 var imageBox = new function() { ...  


 **/


function ImageBox() {

    var instance = this;

    this.FIRST = 0;
    this.MAX_VISIBLE = 10;

    this.IMG_URL = "/imageLoadUp/";
    this.POPUP_URL = "/action/image/publication/image-popup/";
    this.IMG_PAGE_URL = "/";

    this.images = [];

    this.firstVisibleImage = this.FIRST;

    // "private" methods

    this.generateImageAnchor = function (image) {
        var anchor = document.createElement('a');
        anchor.hoverHref = this.POPUP_URL + image.imgZdbId + "?imgpop_displayed_width=670";
        anchor.id = image.imgZdbId;
        anchor.href = this.IMG_PAGE_URL + image.imgZdbId;
        var img = document.createElement('img');
        img.src = this.IMG_URL + image.imgThumb;
        img.className = "xpresimg_img";
        anchor.appendChild(img);
        return anchor;
    };

    this.render = function () {
        //render the controls section
        this.renderControls();

        //render the image section
        this.renderImages();
    };

    this.renderControls = function () {
        var backArrow;
        var backArrowImg;
        var nextArrow;
        var nextArrowImg;
        var countField;
        var maxboxes;
        var maxnote;

        this.controlDiv.innerHTML = "";

        //only show controls if there are more images than
        //will fit in a single frame
        if (this.images.length > this.MAX_VISIBLE) {
            //previous arrow
            if (this.firstVisibleImage === 0) {
                //on the first set, don't make a link
                backArrow = document.createElement('img');
                backArrow.src = "/images/arrow_back_disabled.png";
                backArrow.title = "This is the first set";
            } else {
                backArrow = document.createElement('a');
                backArrowImg = document.createElement('img');
                backArrowImg.src = "/images/arrow_back.png";
                backArrow.href = "javascript:;";
                backArrow.onclick = function () {
                    instance.displayPrev();
                };
                backArrow.appendChild(backArrowImg);
            }
            this.controlDiv.appendChild(backArrow);

            countField = document.createElement('input');
            countField.size = 3;

            countField.value = (this.firstVisibleImage + (this.MAX_VISIBLE)) / this.MAX_VISIBLE;

            if (this.getHiddenCountInput() != null) {
                this.getHiddenCountInput().value = countField.value;
            }

            countField.onchange = function () {
                if (countField.value < 1) {
                    countField.value = 1;
                }

                if (countField.value > instance.getLastPageIndex()) {
                    countField.value = instance.getLastPageIndex();
                }

                document.getElementById('xpatsel_thumbnail_page_hidden_field').value = countField.value;
                instance.jumpToPage(countField.value);
            };

            this.controlDiv.appendChild(countField);

            maxboxes = document.createElement('span');
            maxboxes.innerHTML = " / " + this.getLastPageIndex() + "";
            this.controlDiv.appendChild(maxboxes);

            //next arrow
            if (this.getLastVisibleImageIndex() == this.getLastImageIndex()) {
                //on the last set, don't make a link
                nextArrow = document.createElement('img');
                nextArrow.src = "/images/arrow_next_disabled.png";
                nextArrow.title = "This is the last set";
            } else {
                nextArrow = document.createElement('a');
                nextArrowImg = document.createElement('img');
                nextArrowImg.src = "/images/arrow_next.png";
                nextArrow.href = "javascript:;";
                nextArrow.onclick = function () {
                    instance.displayNext();
                };
                nextArrow.appendChild(nextArrowImg);
            }
            this.controlDiv.appendChild(nextArrow);

            if (this.images.length == this.maxImages) {
                maxnote = document.getElementById("imagebox_maxnote");
                maxnote.innerHTML = "[Note: preview display truncated to " + this.maxImages + " image maximum.]";
                maxnote.style.display = "block";
            }
        }
    };

    this.renderImages = function () {
        var anchor;
        var popupDiv;
        var wrapper;

        this.imageDiv.innerHTML = "";
        document.getElementById("xpresimg_imagePreload").innerHTML = "";

        for (var i = this.firstVisibleImage; i <= this.getLastVisibleImageIndex(); i++) {
            wrapper = document.createElement('span');
            wrapper.className = "imagebox-image-wrapper";

            anchor = this.generateImageAnchor(this.images[i]);
            wrapper.appendChild(anchor);

            popupDiv = document.createElement('div');
            popupDiv.className = "imagebox-popup";
            wrapper.appendChild(popupDiv);

            this.imageDiv.appendChild(wrapper);

            $(anchor).hover(
                function () {
                    $('.imagebox-popup').hide();
                    clearTimeout(instance.popupTimeout);
                    $($(this).siblings('.imagebox-popup'))
                        .load(this.hoverHref)
                        .fadeIn(50);
                },
                function () {
                    instance.popupTimeout = setTimeout(function () {
                        $('.imagebox-popup').fadeOut(100);
                    }, 500);
                }
            );

            $(popupDiv).hover(
                function () {
                    clearTimeout(instance.popupTimeout);
                    $(this).show();
                },
                function () {
                    $(this).fadeOut(100);
                }
            );
        }

    };

    this.getLastVisibleImageIndex = function () {
        var lastVisibleImage = this.firstVisibleImage + this.MAX_VISIBLE - 1;
        if (lastVisibleImage > this.getLastImageIndex()) {
            lastVisibleImage = this.getLastImageIndex();
        }
        return lastVisibleImage;
    };

    this.getLastImageIndex = function () {
        return this.images.length - 1;
    };

    this.getLastPageIndex = function () {
        return Math.ceil((this.getLastImageIndex() + 1) / this.MAX_VISIBLE);
    };

    this.getHiddenCountInput = function () {
        return document.getElementById(this.hiddenInput);
    };

    this.setHiddenCountFieldById = function (hiddenInputId) {
        this.hiddenInput = hiddenInputId;
    };

    //public methods

    this.preloadImages = function () {
        //todo: get & set this via a method once preloading is actually in use
        var div = document.getElementById('xpresimg_imagePreload');

        div.innerHTML = "";
        for (var i = this.firstVisibleImage; i <= this.getLastVisibleImageIndex(); i++) {
            var img = document.createElement('img');
            img.src = "/cgi-bin/image_resize.cgi?maxheight=500&maxwidth=550&image=" + this.images[i].filename;
            div.appendChild(img);
        }
    };

    this.setMaxImages = function (max) {
        this.maxImages = max;
    };

    this.setControlDivById = function (div) {
        this.controlDiv = document.getElementById(div);
    };

    this.setImageDivById = function (div) {
        this.imageDiv = document.getElementById(div);
    };

    this.displayFirstSet = function () {
        if (this.imageDiv != null) {
            this.jumpToImage(this.FIRST);
        }
    };

    this.displayNext = function () {
        var newIndex = this.firstVisibleImage + this.MAX_VISIBLE;
        if (newIndex <= this.getLastImageIndex()) {
            var pageString =  (newIndex / this.MAX_VISIBLE + 1) + ' / ' + this.getLastPageIndex();
            this.jumpToImage(newIndex);
        }
    };

    this.displayPrev = function () {
        if (this.firstVisibleImage > this.MAX_VISIBLE - 1) {
            var newIndex = this.firstVisibleImage - this.MAX_VISIBLE;
            var pageString =  (newIndex / this.MAX_VISIBLE + 1) + ' / ' + this.getLastPageIndex();
            this.jumpToImage(newIndex);
        }
    };

    this.jumpToImage = function (index) {
        this.firstVisibleImage = index;
        this.render();
    };

    this.jumpToPage = function (index) {
        this.firstVisibleImage = (index - 1) * this.MAX_VISIBLE;
        this.render();
    };

}
