import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import {useFetch, useRibbonState} from '../utils/effects';

import GenericErrorMessage from '../components/GenericErrorMessage';
import LoadingSpinner from '../components/LoadingSpinner';
import NoData from '../components/NoData';
import Ribbon, {getSelectedTermQueryParams} from '../components/Ribbon';
import DataTable, {DEFAULT_TABLE_STATE} from '../components/data-table';
import StageTimelineHeader from '../components/StageTimelineHeader';
import StageTimeline from '../components/StageTimeline';
import AttributionLink from '../components/AttributionLink';
import Stage from '../components/Stage';
import PhenotypeFigureGallery from './PhenotypeFigureGallery';

const PhenotypeRibbon = ({geneId}) => {
    const data = useFetch(`/action/api/marker/${geneId}/phenotype/ribbon-summary`);
    const [tableState, setTableState] = useState(DEFAULT_TABLE_STATE);
    const [detailTableState, setDetailTableState] = useState(DEFAULT_TABLE_STATE);
    const [selectedRibbonTerm, setSelectedRibbonTerm] = useRibbonState();
    const [selectedTablePhenotype, setSelectedTablePhenotype] = useState(null);
    const [selectedTableIDs, setSelectedTableIDs] = useState(null);

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

    const handleEntityNameClick = (event, ids, phenotype) => {
        event.preventDefault();
        setSelectedTableIDs(ids);
        setSelectedTablePhenotype(phenotype);
    };

    const handleRibbonCellClick = (subject, group) => {
        setTableState(DEFAULT_TABLE_STATE);
        setDetailTableState(DEFAULT_TABLE_STATE);
        setSelectedTablePhenotype(null);
        setSelectedTableIDs(null);
        setSelectedRibbonTerm(subject, group);
    };

    const columnsDetail = [
        {
            label: 'Fish',
            content: ({phenotypeSourceGenerated}) => <a href={`/${phenotypeSourceGenerated.fishExperiment.fish.zdbID}`} dangerouslySetInnerHTML={{__html: phenotypeSourceGenerated.fishExperiment.fish.displayName}}/>,
            width: '200px',
        },
        {
            label: 'Experiment',
            content: ({phenotypeSourceGenerated}) => phenotypeSourceGenerated.fishExperiment.experiment.conditions,
            width: '150px',
        },
        {
            label: 'Stage',
            content: row => (
                <Stage
                    startStage={row.phenotypeSourceGenerated.start}
                    endStage={row.phenotypeSourceGenerated.end}
                /> ),
            width: '200px',
        },
        {
            label: 'Figure',
            content: ({phenotypeSourceGenerated}) => <a href={`/${phenotypeSourceGenerated.figure.zdbID}`} dangerouslySetInnerHTML={{__html: phenotypeSourceGenerated.figure.label}}/>,
            width: '100px',
        },
        {
            label: 'Publication',
            content: ({phenotypeSourceGenerated}) => <a href={`/${phenotypeSourceGenerated.figure.publication.zdbID}`} dangerouslySetInnerHTML={{__html: phenotypeSourceGenerated.figure.publication.shortAuthorList}}/>,
            width: '200px',
        },
    ];

    const columns = [
        {
            label: 'Phenotype',
            key: 'locations',
            content: ({phenotype, phenotypeIDs}) =>
                <a
                    href='#'
                    onClick={event => handleEntityNameClick(event, phenotypeIDs, phenotype)}
                    dangerouslySetInnerHTML={{__html: phenotype}}
                />,
            width: '140px',
            filterName: 'termName',
        },
        {
            label: <StageTimelineHeader/>,
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

    let selectedTermName = '';
    let selectedTermId = '';
    let selectedTermIsOther = false;
    if (selectedTablePhenotype) {
        selectedTermName = selectedTablePhenotype;
        //todo: need to handle subterm also
        selectedTermId = selectedTableIDs;
    } else if (selectedRibbonTerm) {
        selectedTermName = selectedRibbonTerm.group.label;
        selectedTermIsOther = selectedRibbonTerm.group.type === 'Other';
        if (selectedRibbonTerm.group.type !== 'GlobalAll') {
            selectedTermId = selectedRibbonTerm.group.id;
        }
    }

    const summaryTableQuery = getSelectedTermQueryParams(selectedRibbonTerm);

    const detailTableQuery = {
        termId: selectedTableIDs,
    };

    return (
        <div>
            <Ribbon
                subjects={data.value.subjects}
                categories={data.value.categories}
                itemClick={handleRibbonCellClick}
                selected={selectedRibbonTerm}
            />

            {selectedTablePhenotype &&
            <button className=' btn btn-link btn-sm px-0' onClick={() => setSelectedTablePhenotype(null)}>
                <i className=' fas fa-chevron-left'/> Back to phenotype in {selectedRibbonTerm.group.label}
            </button>
            }
            {selectedTermName && <h5>Phenotype in <span dangerouslySetInnerHTML={{__html: selectedTermName}} /></h5>}

            {(selectedRibbonTerm || selectedTablePhenotype) &&
            <PhenotypeFigureGallery
                geneId={geneId}
                selectedTermId={selectedTermId}
                selectedTermIsOther={selectedTermIsOther}
            />
            }

            {selectedRibbonTerm && !selectedTablePhenotype &&
            <DataTable
                url={`/action/api/marker/${geneId}/phenotype/summary?${qs.stringify(summaryTableQuery)}`}
                columns={columns}
                rowKey={row => row.phenotype}
                tableState={tableState}
                onTableStateChange={setTableState}
            />
            }

            {selectedTablePhenotype &&
            <DataTable
                url={`/action/api/marker/${geneId}/phenotype/detail?${qs.stringify(detailTableQuery)}`}
                columns={columnsDetail}
                rowKey={row => row.id}
                tableState={detailTableState}
                onTableStateChange={setDetailTableState}
            />
            }
        </div>
    )
};

PhenotypeRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default PhenotypeRibbon;
