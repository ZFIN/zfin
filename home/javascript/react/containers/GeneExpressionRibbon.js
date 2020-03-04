import React from 'react';
import PropTypes from 'prop-types';
import {useFetch} from '../utils/effects';
import LoadingSpinner from '../components/LoadingSpinner';

import NoData from '../components/NoData';
import Ribbon from '../components/Ribbon';

const GeneExpressionRibbon = ({geneId}) => {

    const data = useFetch(`/action/api/marker/${geneId}/expression/ribbon-summary`);

    if (data.rejected) {
        return <span className='text-danger'>Something went wrong fetching data. Try again later or <a href='mailto:@ZFIN_ADMIN@'>contact us</a>.</span>;
    }

    if (data.pending) {
        return <LoadingSpinner />;
    }

    if (!data.value) {
        return null;
    }

    if (data.value.subjects[0].nb_annotations === 0) {
        return <NoData />
    }



    return (
        <div>
            <Ribbon
                subjects={data.value.subjects}
                categories={data.value.categories}
            />
        </div>
    );
};

GeneExpressionRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default GeneExpressionRibbon;