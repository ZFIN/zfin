import React from 'react';
import PropTypes from 'prop-types';
import {useFetch} from '../utils/effects';
import LoadingSpinner from '../components/LoadingSpinner';
import {GenericRibbon} from '@geneontology/ribbon';

import style from './style.scss';

const GeneOntologyRibbon = ({geneId}) => {
    const data = useFetch(`/action/api/marker/${geneId}/go/ribbon-summary`);

    if (data.rejected) {
        return <span className='text-danger'>Something went wrong fetching data. Try again later or <a href='mailto:@ZFIN_ADMIN@'>contact us</a>.</span>;
    }

    if (data.pending) {
        return <LoadingSpinner />;
    }

    if (!data.value) {
        return null;
    }

    return (
        <div className='text-nowrap'>
            <GenericRibbon
                subjects={data.value.subjects}
                categories={data.value.categories}
                hideFirstSubjectLabel
                colorBy={1} // annotations
                binaryColor
                maxColor={[style.primaryR, style.primaryG, style.primaryB]}
            />
        </div>
    );
};

GeneOntologyRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default GeneOntologyRibbon;
