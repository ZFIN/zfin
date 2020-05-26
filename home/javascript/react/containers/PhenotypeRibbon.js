import React, {useState} from 'react';
import PropTypes from 'prop-types';
import {useFetch, useRibbonState, useTableState} from '../utils/effects';

import GenericErrorMessage from '../components/GenericErrorMessage';
import LoadingSpinner from '../components/LoadingSpinner';
import NoData from '../components/NoData';
import Ribbon from '../components/Ribbon';
import {DEFAULT_TABLE_STATE} from '../components/data-table';
import {
    PhenotypeAnnotationDetailTable,
    PhenotypeAnnotationSummaryTable,
    PhenotypeFigureGallery,
} from '../components/phenotype';

const PhenotypeRibbon = ({geneId}) => {
    const data = useFetch(`/action/api/marker/${geneId}/phenotype/ribbon-summary`);
    const [summaryTableState, setSummaryTableState] = useTableState();
    const [detailTableState, setDetailTableState] = useTableState();
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
        setSummaryTableState(DEFAULT_TABLE_STATE);
        setDetailTableState(DEFAULT_TABLE_STATE);
        setSelectedTablePhenotype(null);
        setSelectedTableIDs(null);
        setSelectedRibbonTerm(subject, group);
    };

    const handleClearTableSelection = () => {
        setSelectedTablePhenotype(null);
        setSelectedTableIDs(null);
    }

    let selectedTermName = '';
    if (selectedTablePhenotype) {
        selectedTermName = selectedTablePhenotype;
    } else if (selectedRibbonTerm) {
        selectedTermName = selectedRibbonTerm.group.label;
    }

    return (
        <div>
            <Ribbon
                subjects={data.value.subjects}
                categories={data.value.categories}
                itemClick={handleRibbonCellClick}
                selected={selectedRibbonTerm}
            />

            {selectedTablePhenotype &&
                <button className=' btn btn-link btn-sm px-0' onClick={handleClearTableSelection}>
                    <i className=' fas fa-chevron-left'/> Back to phenotype in {selectedRibbonTerm.group.label}
                </button>
            }
            {selectedTermName && <h5>Phenotype in <span dangerouslySetInnerHTML={{__html: selectedTermName}} /></h5>}

            {(selectedRibbonTerm || selectedTableIDs) &&
                <PhenotypeFigureGallery
                    geneId={geneId}
                    selectedRibbonTerm={selectedRibbonTerm}
                    selectedTableIds={selectedTableIDs}
                />
            }

            {selectedRibbonTerm && !selectedTablePhenotype &&
                <PhenotypeAnnotationSummaryTable
                    geneId={geneId}
                    onEntityClick={handleEntityNameClick}
                    selectedRibbonTerm={selectedRibbonTerm}
                    setTableState={setSummaryTableState}
                    tableState={summaryTableState}
                />
            }

            {selectedTablePhenotype &&
                <PhenotypeAnnotationDetailTable
                    geneId={geneId}
                    selectedPhenotypeIds={selectedTableIDs}
                    setTableState={setDetailTableState}
                    tableState={detailTableState}
                />
            }
        </div>
    )
};

PhenotypeRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default PhenotypeRibbon;
