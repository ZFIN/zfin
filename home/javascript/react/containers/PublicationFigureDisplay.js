import React, {useEffect} from 'react';
import PropTypes from 'prop-types';

const PublicationFigureDisplay = ({title, imagesJson, publicationId, navigationCounter}) => {
    const images = JSON.parse(imagesJson);
    let storedpage = null;

    useEffect(() => {
        var imageBox = new ImageBox();
        imageBox.setImageDivById("xpresimg_box");
        imageBox.setControlDivById("xpresimg_controls");
        imageBox.setHiddenCountFieldById("xpatsel_thumbnail_page_hidden_field");
        imageBox.setMaxImages(5000);
        imageBox.images = images.map(image => ({imgThumb: image.imageThumbnail, imgZdbId: image.imageZdbId}));
        document.getElementById('xpresimg_thumbs_title').innerHTML = `Figure Gallery (${images.length} images)`;

        function loadImages() {
            storedpage = imageBox.getHiddenCountInput().value;
            if ((storedpage != null) && (storedpage != "") && Number.isInteger(storedpage)) {
                imageBox.jumpToPage(storedpage);
            } else {
                imageBox.displayFirstSet();
            }
        }

        document.hasImages = true;
        loadImages();
        navigationCounter.setCounts(title, images.length);
    });

    return (
        <>
            <div id="xpresimg_all">
                <input type="hidden" name="xpatsel_thumbnail_page" id="xpatsel_thumbnail_page_hidden_field" value="1"/>
                <div id="xpresimg_control_box">
                    <span id="xpresimg_thumbs_title" className="summary"/>
                    <span id="xpresimg_controls"></span>
                </div>
                <div id="xpresimg_box"></div>
                <div id="imagebox_maxnote" style={{display: 'none'}}></div>
                <div id="xpresimg_imagePreload"></div>
            </div>
            <div><a href={`/action/figure/all-figure-view/${publicationId}`}>Show all Figures</a></div>
        </>
    );
};

PublicationFigureDisplay.propTypes = {
    title: PropTypes.string,
};

export default PublicationFigureDisplay;
