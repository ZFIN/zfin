import React, {useEffect} from 'react';
import PropTypes from 'prop-types';
let ImageBox = window.ImageBox || (() => {console.error('no ImageBox found')});

const PublicationFigureDisplay = ({title, imagesJson, navigationCounter}) => {
    const images = JSON.parse(imagesJson);
    let storedpage = null;

    useEffect(() => {
        if (images.length === 0) {
            return;
        }
        let imageBox = new ImageBox();
        imageBox.setImageDivById('xpresimg_box');
        imageBox.setControlDivById('xpresimg_controls');
        imageBox.setHiddenCountFieldById('xpatsel_thumbnail_page_hidden_field');
        imageBox.setMaxImages(5000);
        imageBox.images = images.map(image => ({imgThumb: image.imageThumbnail, imgZdbId: image.imageZdbId}));
        document.getElementById('xpresimg_thumbs_title').innerHTML = `Figure Gallery (${images.length} images)`;

        function loadImages() {
            storedpage = imageBox.getHiddenCountInput().value;
            if ((storedpage !== null) && (storedpage !== '') && Number.isInteger(storedpage)) {
                imageBox.jumpToPage(storedpage);
            } else {
                imageBox.displayFirstSet();
            }
        }

        document.hasImages = true;
        loadImages();
        navigationCounter.setCounts(title, images.length);
    });

    if (images.length && images.length > 0) {
        return (
            <>
                <div id='xpresimg_all'>
                    <input
                        type='hidden'
                        name='xpatsel_thumbnail_page'
                        id='xpatsel_thumbnail_page_hidden_field'
                        value='1'
                    />
                    <div id='xpresimg_control_box'>
                        <span id='xpresimg_thumbs_title' className='summary'/>
                        <span id='xpresimg_controls'/>
                    </div>
                    <div id='xpresimg_box'/>
                    <div id='imagebox_maxnote' style={{display: 'none'}}/>
                    <div id='xpresimg_imagePreload'/>
                </div>
            </>
        );
    } else {
        return <i className='text-muted'>No images available</i>
    }
};

PublicationFigureDisplay.propTypes = {
    title: PropTypes.string,
    imagesJson: PropTypes.string,
    publicationId: PropTypes.string,
    navigationCounter: PropTypes.shape({
        setCounts: PropTypes.func
    }),
};

export default PublicationFigureDisplay;
