import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import FigureGallery from '../FigureGallery';
import FigureGalleryExpressionDetails from './FigureGalleryExpressionDetails';
import NoData from '../NoData';
import GenericErrorMessage from '../GenericErrorMessage';
import useAppendingFetch from '../../hooks/useAppendingFetch';
import useFetch from '../../hooks/useFetch';

const GeneExpressionFigureGallery = (
    {
        geneId,
        includeReporters,
        onlyInSitu,
        selectedRibbonTerm,
        selectedTableEntity,
    }
) => {
    const [page, setPage] = useState(1);
    const [selectedImage, setSelectedImage] = useState(null);

    const baseUrl = `/action/api/marker/${geneId}/expression/images`;
    const params = { };
    if (includeReporters) {
        params.includeReporters = includeReporters;
    }
    if (onlyInSitu) {
        params.onlyInSitu = onlyInSitu;
    }
    if (selectedTableEntity) {
        params.supertermId = selectedTableEntity.superterm.oboID;
        if (selectedTableEntity.subterm) {
            params.subtermId = selectedTableEntity.subterm.oboID;
        }
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
            selectedImageDetails={<FigureGalleryExpressionDetails figureDetails={figureDetails} />}
            total={data.value.total}
        />
    );
};

GeneExpressionFigureGallery.propTypes = {
    geneId: PropTypes.string,
    includeReporters: PropTypes.bool,
    onlyInSitu: PropTypes.bool,
    selectedRibbonTerm: PropTypes.object,
    selectedTableEntity: PropTypes.object,
};

export default GeneExpressionFigureGallery;
