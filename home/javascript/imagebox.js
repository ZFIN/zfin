/** FILE: imageBox.js

 Defines an imageBox class that will manage display of image thumbnails
 built from an array of image objects defined with:

 { "imgThumb":"$1",
 "imgZdbId":"$2",
 "figLabel":"$3",
 "figZdbId":"$4",
 "pubMiniRef":"$5" }

 Because of the way events are handled, the instantiated class has to be named
 imageBox.

 A good solution might be to refactor this into a singleton starting with
 something like:

 var imageBox = new function() { ...  


 **/


function ImageBox() {

    this.FIRST = 0;
    this.MAX_VISIBLE = 10;

    this.IMG_URL = "/imageLoadUp/";
    this.POPUP_URL = "/action/publication/image-popup?image.zdbID=";
    this.FIG_URL = "/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-fxfigureview.apg&OID=";
    this.IMG_PAGE_URL = "/<!--|WEBDRIVER_PATH_FROM_ROOT|-->?MIval=aa-imageview.apg&image_table=image&OID=";

    this.max_images = 100; //a default that should get set by the page

    this.images = new Array();

    this.controlDiv = "";
    this.imageDiv = "";
    this.hiddenInput = "";

    this.firstVisibleImage = this.FIRST;
	
	//"private" methods

    this.generateImageAnchor = function(image) {
        anchor = document.createElement('a');
        anchor.href = this.POPUP_URL + image.imgZdbId + "&imgpop_displayed_width=670";
        //anchor.href = "http://iguana.zfin.org";
        //anchor.href = this.FIG_URL + image.figZdbId;
        //anchor.href = this.POPUP_URL + image.imgZdbId;
        anchor.title = image.figLabel;
        anchor.id = image.imgZdbId;

        img = document.createElement('img');
        img.src = this.IMG_URL + image.imgThumb;
        img.className = "xpresimg_img";
        anchor.appendChild(img);

        return anchor;
    }

    this.render = function() {

        //render the controls section
        this.renderControls();

        //render the image section
        this.renderImages();

    }

    this.renderControls = function() {
        this.controlDiv.innerHTML = "";

        //only show controls if there are more images than
        //will fit in a single frame
        if (this.images.length > this.MAX_VISIBLE) {

            //previous arrow
            if (this.firstVisibleImage == 0 ) {
                //on the first set, don't make a link
                backArrow = document.createElement('img');
                backArrow.src = "/images/arrow_back_disabled.png";
                backArrow.title = "This is the first set";
            } else {
                backArrow = document.createElement('a');
                backArrowImg = document.createElement('img');
                backArrowImg.src = "/images/arrow_back.png";
                backArrow.href = "javascript:;";
                //see the commment below about the onclick method, notice that
                //it's imageBox rather than ImageBox - referring to the insance
                //rather than the class - so the object *MUST* be named imageBox
                backArrow.onclick = function() { imageBox.displayPrev(); }
                backArrow.appendChild(backArrowImg);
            }
            this.controlDiv.appendChild(backArrow);

            countField = document.createElement('input');
            countField.size = 3 ;
        

            countField.value = (this.firstVisibleImage + (this.MAX_VISIBLE))/this.MAX_VISIBLE;
        
            if (this.getHiddenCountInput() != null) {
                this.getHiddenCountInput().value = countField.value;
            }


            countField.onchange = function() {
                if (countField.value < 1)
                    countField.value = 1;
                if (countField.value > imageBox.getLastPageIndex()) {
                    countField.value = imageBox.getLastPageIndex();
                }
            
                document.getElementById('xpatsel_thumbnail_page_hidden_field').value = countField.value;
                imageBox.jumpToPage(countField.value);
            }

            this.controlDiv.appendChild(countField) ;

        maxboxes = document.createElement('span');
        maxboxes.innerHTML = " / "  + this.getLastPageIndex()  + "";
        this.controlDiv.appendChild(maxboxes);


        //next arrow

            if (this.getLastVisibleImageIndex() ==  this.getLastImageIndex() ) {
                //on the last set, don't make a link
                nextArrow = document.createElement('img');
                nextArrow.src = "/images/arrow_next_disabled.png";
                nextArrow.title = "This is the last set";
            } else {
                nextArrow = document.createElement('a');
                nextArrowImg = document.createElement('img');
                nextArrowImg.src = "/images/arrow_next.png";
                nextArrow.href = "javascript:;";
                //this is unfortunate - imageBox *has* to be the name of the
                //instance of this class - it should probably be refactored
                //to be a singleton where the object is created by name when
                //the .js file is included.
                nextArrow.onclick = function() { imageBox.displayNext(); }
                nextArrow.appendChild(nextArrowImg);
            }
            this.controlDiv.appendChild(nextArrow);

            if (this.images.length == this.maxImages) {
                maxnote = document.getElementById("imagebox_maxnote");
                maxnote.innerHTML = "[Note: preview display truncated to " + this.maxImages + " image maximum.]";
                maxnote.style.display = "block";
            }
        }
    }


    this.renderImages = function() {
        this.imageDiv.innerHTML = "";
        document.getElementById("xpresimg_imagePreload").innerHTML = "";

        //j is a position counter within the 10 being displayed
        var j = 0;
        for(var i = this.firstVisibleImage ; i <= this.getLastVisibleImageIndex() ; i++) {
            anchor = this.generateImageAnchor(this.images[i]);
            this.imageDiv.appendChild(anchor);
            var popup_width = 550;

            //for images on the right, we have to push the popup to the left
            //so that it doesn't end up displaying off the page
            //this bit of math pushes the box left more and more as you
            //from right to left.  The final image should be totally to
            //the left of the cursor.  Applying this to the first few
            //caused problems with very narrow images, so that's why it's
            // j > 3
            if (j > 3) {
                var myOffsetLeft = -1 * j/this.MAX_VISIBLE * popup_width;
            } else { var myOffsetLeft = 0; }

            myOffsetTop = 0;
            if (Prototype.Browser.IE) {
                myOffsetTop = 45;
                popup_width = popup_width + 15;
            }
            new Control.Modal(anchor, {opacity: 0.7, hover: true, position: 'relative', offsetTop: myOffsetTop, offsetLeft: myOffsetLeft , width: popup_width, height: 600});

            //this is sneaky - after Control.modal uses the initial href value to generate the hover popup,
            //once the Control.modal object exists, I can set it to where I want a click action to go
            anchor.href=this.IMG_PAGE_URL + this.images[i].imgZdbId;

            j++;
        }

        
    }

    this.getLastVisibleImageIndex = function() {
        var lastVisibleImage = this.firstVisibleImage + this.MAX_VISIBLE - 1;
        if (lastVisibleImage > this.getLastImageIndex()) { lastVisibleImage = this.getLastImageIndex(); }
        return lastVisibleImage;
    }

    this.getLastImageIndex = function() {
        return this.images.length - 1;
    }

    this.getLastPageIndex = function() {
        return Math.ceil(this.getLastImageIndex() / this.MAX_VISIBLE)
    }

    this.getHiddenCountInput = function() {
	return document.getElementById(this.hiddenInput);
    }
  
    this.setHiddenCountFieldById = function(hiddenInputId) {
	this.hiddenInput = hiddenInputId;
    }

    //public methods

    this.preloadImages = function() {
        //todo: get & set this via a method once preloading is actually in use
        var div = document.getElementById('xpresimg_imagePreload');
                
        div.innerHTML = "";
        for(var i = this.firstVisibleImage ; i <= this.getLastVisibleImageIndex() ; i++) {
            var img = document.createElement('img');
            img.src = "/cgi-bin/image_resize.cgi?maxheight=500&maxwidth=550&image=" + this.images[i].filename;
            div.appendChild(img);
        }
    }

    this.setMaxImages = function(max) {
        this.maxImages = max;
    }

    this.setControlDivById = function(div) {
        this.controlDiv = document.getElementById(div);
    }

    this.setImageDivById = function(div) {
        this.imageDiv = document.getElementById(div);
    }

    this.displayFirstSet = function() {

        if (this.imageDiv != null) {
            //this.imageDiv.style.display = "block";
            this.jumpToImage(this.FIRST);
        }

    }

    this.displayNext = function () {
        var newIndex = this.firstVisibleImage + this.MAX_VISIBLE;
        if (newIndex < this.getLastImageIndex()) {
            this.jumpToImage(this.firstVisibleImage + this.MAX_VISIBLE);
            countField.value = this.firstVisibleImage  / this.MAX_VISIBLE + 1 ; 
        }
    }


    this.displayPrev = function () {
        if (this.firstVisibleImage > this.MAX_VISIBLE-1) {
            this.jumpToImage(this.firstVisibleImage - this.MAX_VISIBLE)
            countField.value = this.firstVisibleImage  / this.MAX_VISIBLE + 1 ; 
        }
    }

    this.jumpToImage = function(index) {
        this.firstVisibleImage = index;
        this.render();
    }

    this.jumpToPage = function(index) {
        this.firstVisibleImage = (index - 1) * this.MAX_VISIBLE;
        this.render();
    }

}




