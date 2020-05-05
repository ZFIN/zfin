import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import FigureGallery from '../components/FigureGallery';
import FigureGalleryPhenotypeDetails from '../components/FigureGalleryPhenotypeDetails';
import http from '../utils/http';
import NoData from '../components/NoData';
import GenericErrorMessage from '../components/GenericErrorMessage';
import {useFetch} from '../utils/effects';

const PhenotypeFigureGallery = ({geneId,  selectedTermId}) => {
    const [page, setPage] = useState(1);
    const [images, setImages] = useState([]);
    const [total, setTotal] = useState(0);
    const [error, setError] = useState(null);
    const [pending, setPending] = useState(false);
    const [selectedImage, setSelectedImage] = useState(null);

    // not using useFetch here because the image arrays sometimes need to be concatenated, not replaced
    const fetchImages = (page, resultHandler) => {
        setPending(true);
        let url = `/action/api/marker/${geneId}/phenotype/images?page=${page}`;

        if (selectedTermId) {
            url += '&termId=' + selectedTermId;
        }
        http.get(url)
            .then(response => {
                resultHandler(response.results);
                setTotal(response.total);
                setError(null);

            })
            .fail(setError)
            .always(() => setPending(false));
    };

    // if the gene or selected ribbon block changes, start at the beginning and replace the existing images
    useEffect(() => {
        setPage(1);
        setImages([]);
        fetchImages(1, results => setImages(results));
    }, [geneId, selectedTermId]);

    // if the page changes, concatenate the new images to the old ones
    useEffect(() => {
        if (page === 1) {
            return;
        }
        fetchImages(page, results => setImages(images.concat(results)))
    }, [page]);

    const figureDetails = useFetch(selectedImage ?
        `/action/api/figure/${selectedImage.figure.zdbID}/summary` :
        undefined
    );

    if (error) {
        return <GenericErrorMessage />;
    }

    if(total === 0){
        return <NoData placeholder={'No images available'}/>;
    }



    return (
        <FigureGallery
            className='figure-gallery-container'
            images={images}
            loading={pending}
            onImageSelect={setSelectedImage}
            onLoadMore={() => setPage(prevPage => prevPage + 1)}
            selectedImageDetails={<FigureGalleryPhenotypeDetails figureDetails={figureDetails} />}
            total={total}
        />
    );
};

PhenotypeFigureGallery.propTypes = {
    geneId: PropTypes.string,
    selectedTermId: PropTypes.string,
    selectedTermIsOther: PropTypes.bool,
};

export default PhenotypeFigureGallery;
