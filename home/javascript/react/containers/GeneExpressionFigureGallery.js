import React, {useEffect, useState} from 'react';
import PropTypes from 'prop-types';
import FigureGallery from '../components/FigureGallery';
import http from '../utils/http';
import GenericErrorMessage from '../components/GenericErrorMessage';

const GeneExpressionFigureGallery = ({geneId}) => {
    const [page, setPage] = useState(1);
    const [images, setImages] = useState([]);
    const [total, setTotal] = useState(0);
    const [error, setError] = useState(null);
    const [pending, setPending] = useState(false);

    // not using useFetch here because the image arrays need to be concatenated, not replaced
    useEffect(() => {
        setPending(true);
        http.get(`/action/api/marker/${geneId}/expression/images?page=${page}`)
            .then(response => {
                setImages(images.concat(response.results));
                setTotal(response.total);
                setError(null);
            })
            .fail(setError)
            .always(() => setPending(false));
    }, [geneId, page]);

    if (error) {
        return <GenericErrorMessage />;
    }

    return (
        <FigureGallery
            className='figure-gallery-container'
            images={images}
            loading={pending}
            onLoadMore={() => setPage(prevPage => prevPage + 1)}
            total={total}
        />
    );
};

GeneExpressionFigureGallery.propTypes = {
    geneId: PropTypes.string,
};

export default GeneExpressionFigureGallery;
