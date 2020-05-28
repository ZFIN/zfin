import React, {useState} from 'react';
import PropTypes from 'prop-types';
import FigureGallery from '../FigureGallery';
import FigureGalleryPhenotypeDetails from './FigureGalleryPhenotypeDetails';
import NoData from '../NoData';
import GenericErrorMessage from '../GenericErrorMessage';
import {useAppendingFetch, useFetch} from '../../utils/effects';
import qs from 'qs';

const PhenotypeFigureGallery = ({excludeEaps, geneId, selectedTableIds, selectedRibbonTerm}) => {
    const [page, setPage] = useState(1);
    const [selectedImage, setSelectedImage] = useState(null);

    const baseUrl = `/action/api/marker/${geneId}/phenotype/images`;
    const params = {};
    if (excludeEaps) {
        params.excludeEaps = true;
    }
    if (selectedTableIds) {
        params.phenotypeIds = selectedTableIds;
    } else if (selectedRibbonTerm) {
        if (selectedRibbonTerm.group.type !== 'GlobalAll') {
            params.termId = selectedRibbonTerm.group.id
        }
        if (selectedRibbonTerm.group.type === 'Other') {
            params.isOther = true;
        }
    }
    const url = baseUrl + qs.stringify(params, { addQueryPrefix: true });
    const data = useAppendingFetch(url, page, setPage);

    const figureDetails = useFetch(selectedImage ?
        `/action/api/figure/${selectedImage.figure.zdbID}/summary` :
        undefined
    );

    if (data.rejected) {
        return <GenericErrorMessage />;
    }

    if (!data.value) {
        return null;
    }

    if (data.value.total === 0) {
        return <NoData placeholder='No images available' />;
    }

    return (
        <FigureGallery
            className='figure-gallery-container'
            images={data.value.results}
            loading={data.pending}
            onImageSelect={setSelectedImage}
            onLoadMore={() => setPage(prevPage => prevPage + 1)}
            selectedImageDetails={<FigureGalleryPhenotypeDetails figureDetails={figureDetails} />}
            total={data.value.total}
        />
    );
};

PhenotypeFigureGallery.propTypes = {
    excludeEaps: PropTypes.bool,
    geneId: PropTypes.string,
    selectedRibbonTerm: PropTypes.object,
    selectedTableIds: PropTypes.string,
};

export default PhenotypeFigureGallery;
