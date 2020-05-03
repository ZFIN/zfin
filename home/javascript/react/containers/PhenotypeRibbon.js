import React, {useState} from 'react';
import PropTypes from 'prop-types';
import {useFetch, useRibbonState} from '../utils/effects';
import GenericErrorMessage from '../components/GenericErrorMessage';
import LoadingSpinner from '../components/LoadingSpinner';
import NoData from '../components/NoData';
import Ribbon, {getSelectedTermQueryParams} from '../components/Ribbon';
import DataTable, {DEFAULT_TABLE_STATE} from 'components/DataTable';
import StageTimelineHeader from '../components/StageTimelineHeader';
import StageTimeline from '../components/StageTimeline';
import AttributionLink from '../components/AttributionLink';

const PhenotypeRibbon = ({geneId}) => {
    const data = useFetch(`/action/api/marker/${geneId}/phenotype/ribbon-summary`);
    const [tableState, setTableState] = useState(DEFAULT_TABLE_STATE);
    const [selectedRibbonTerm, setSelectedRibbonTerm] = useRibbonState(() => setTableState(DEFAULT_TABLE_STATE));
    const [selectedTableEntity, setSelectedTableEntity] = useState(null);
    const [filteredTerm, setFilteredTerm] = useState('');

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

    const handleFilterChange = (event) => {
        event.preventDefault();
        setFilteredTerm(event.target.value);
    };

    const handleFilter = (event, term) => {
        event.preventDefault();
        setFilteredTerm(term);
    };

    const handleRibbonCellClick = (subject, group) => {
        setSelectedTableEntity(null);
        setSelectedRibbonTerm(subject, group);
        setFilteredTerm('');
    };

    const columns = [
        {
            label: (
                <div>
                    <div>Phenotype</div>
                    <form className='form-inline'>
                        <label className='sr-only' htmlFor='filterInputField'>Name</label>
                        <input
                            type='text'
                            className='form-control mb-2 mr-sm-2'
                            id='filterTermName'
                            size='15'
                            value={filteredTerm}
                            onChange={event => handleFilterChange(event)}
                        />
                        <button
                            type='button'
                            className='btn btn-secondary btn-sm mb-2'
                            onClick={event => handleFilter(event, '')}
                        >
                            Clear
                        </button>
                    </form>
                </div>
            ),
            key: 'locations',
            content: ({phenotype}) =>
                <a
                    href='#'
                    dangerouslySetInnerHTML={{__html: phenotype}}
                />,
            width: '140px',
        },
        {
            label: <StageTimelineHeader />,
            key: 'stages',
            content: ({stages}, supplementalData) => (
                <StageTimeline highlightedStages={stages} allStages={supplementalData.stages}/>
            ),
            width: '300px',
        },
        {
            label: 'Citations',
            content: row => (
                <AttributionLink
                    url={`/action/marker/${geneId}`}
                    publicationCount={row.numberOfPublications}
                    publication={row.publication}
                    multiPubAccessionID={geneId}
                    multiPubs={row.ribbonPubs}
                />
            ),
            width: '120px',
        },
    ];


    return (
        <div>
            <Ribbon
                subjects={data.value.subjects}
                categories={data.value.categories}
                itemClick={handleRibbonCellClick}

            />

            {selectedRibbonTerm && !selectedTableEntity &&
            <DataTable
                url={`/action/api/marker/${geneId}/phenotype/summary${getSelectedTermQueryParams(selectedRibbonTerm)}&filter.termName=${filteredTerm}`}
                columns={columns}
                rowKey={row => row.phenotype}
                showEmptyTable={filteredTerm}
                tableState={tableState}
                onTableStateChange={setTableState}
            />
            }

        </div>
    )
};

PhenotypeRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default PhenotypeRibbon;