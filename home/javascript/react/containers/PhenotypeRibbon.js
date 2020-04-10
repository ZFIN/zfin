import React from 'react';
import PropTypes from 'prop-types';
import {useFetch} from '../utils/effects';
import GenericErrorMessage from '../components/GenericErrorMessage';
import LoadingSpinner from '../components/LoadingSpinner';
import NoData from '../components/NoData';
import Ribbon from '../components/Ribbon';

const PhenotypeRibbon = ({geneId}) => {
    const data = useFetch(`/action/api/marker/${geneId}/phenotype/ribbon-summary`);

    if (data.rejected) {
        return <GenericErrorMessage/>;
    }

    if (data.pending) {
        return <LoadingSpinner/>;
    }

    if (!data.value) {
        return null;
    }

    if (data.value.subjects[0].nb_annotations === 0) {
        return <NoData/>
    }

    return (
        <div>
            <Ribbon
                subjects={data.value.subjects}
                categories={data.value.categories}
            />
        </div>
    )
};

PhenotypeRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default PhenotypeRibbon;