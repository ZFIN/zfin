import React, { useEffect } from 'react';
import PropTypes from 'prop-types';
import GenericErrorMessage from '../GenericErrorMessage';
import LoadingSpinner from '../LoadingSpinner';
import NoData from '../NoData';
import Ribbon from './Ribbon';
import useFetch from '../../hooks/useFetch';
import {ribbonGroupHasData} from './utils';

const DataRibbon = ({dataUrl, onRibbonCellClick, onNoDataLoad, selected}) => {
    const data = useFetch(dataUrl);
    useEffect(() => {
        if (typeof onNoDataLoad !== 'function' || !selected) {
            return;
        }
        if (!ribbonGroupHasData(data.value, selected.group)) {
            onNoDataLoad()
        }
    }, [data.value]);

    if (data.rejected) {
        return <GenericErrorMessage />;
    }

    return (
        <div className='data-ribbon-container'>
            { data.pending && <div className='position-absolute'><LoadingSpinner/></div> }
            { data.value && (data.value.subjects[0].nb_annotations === 0 ?
                (!data.pending && <NoData />) :
                <Ribbon
                    subjects={data.value.subjects}
                    categories={data.value.categories}
                    itemClick={onRibbonCellClick}
                    selected={selected}
                />
            )}
        </div>
    );
};

DataRibbon.propTypes = {
    dataUrl: PropTypes.string,
    onRibbonCellClick: PropTypes.func,
    onNoDataLoad: PropTypes.func,
    selected: PropTypes.object,
};

export default DataRibbon;