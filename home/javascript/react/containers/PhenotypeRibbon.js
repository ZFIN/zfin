import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import {useRibbonState, useTableState} from '../utils/effects';
import {DataRibbon} from '../components/ribbon';
import {DEFAULT_TABLE_STATE} from '../components/data-table';
import {
    PhenotypeAnnotationDetailTable,
    PhenotypeAnnotationSummaryTable,
    PhenotypeFigureGallery,
} from '../components/phenotype';
import Checkbox from '../components/Checkbox';

const PhenotypeRibbon = ({geneId}) => {
    const [summaryTableState, setSummaryTableState] = useTableState();
    const [detailTableState, setDetailTableState] = useTableState();
    const [selectedRibbonTerm, setSelectedRibbonTerm] = useRibbonState();
    const [selectedTablePhenotype, setSelectedTablePhenotype] = useState(null);
    const [selectedTableIDs, setSelectedTableIDs] = useState(null);
    const [excludeEaps, setExcludeEaps] = useState(false);

    const baseUrl = `/action/api/marker/${geneId}/phenotype/ribbon-summary`;
    const params = {};
    if (excludeEaps) {
        params.excludeEaps = true
    }
    const ribbonDataUrl = baseUrl + qs.stringify(params, { addQueryPrefix: true });

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
    };

    const handleNoDataLoad = () => {
        setSelectedRibbonTerm(null, null);
        handleClearTableSelection();
    };

    let selectedTermName = '';
    let headerText = '';
    if (selectedTablePhenotype) {
        selectedTermName = selectedTablePhenotype;
        headerText += 'Phenotype in ' +  selectedTermName
    } else if (selectedRibbonTerm) {
        selectedTermName = selectedRibbonTerm.group.label;
        if (selectedRibbonTerm.group.id.indexOf('ZFS') === -1) {
            headerText += 'Phenotype in ' + selectedTermName

        }
        else {
            headerText += 'Phenotype at ' + selectedTermName + '  stage'
        }
    }


    return (
        <div className='phenotype-ribbon'>
            <div className='mb-2'>
                <Checkbox checked={excludeEaps} id='excludeEapsCheckbox' onChange={e => setExcludeEaps(e.target.checked)}>
                    Exclude altered gene expression phenotypes
                </Checkbox>
            </div>

            <DataRibbon
                dataUrl={ribbonDataUrl}
                onNoDataLoad={handleNoDataLoad}
                onRibbonCellClick={handleRibbonCellClick}
                selected={selectedRibbonTerm}
            />

            {selectedTablePhenotype &&
                <button className=' btn btn-link btn-sm px-0' onClick={handleClearTableSelection}>
                    <i className=' fas fa-chevron-left'/> Back to phenotype in {selectedRibbonTerm.group.label}
                </button>
            }
            {/*{selectedTermName && <h5>Phenotype in <span dangerouslySetInnerHTML={{__html: selectedTermName}} /></h5>}*/}
            {selectedTermName && <h5>{headerText} </h5>}

            {(selectedRibbonTerm || selectedTableIDs) &&
                <PhenotypeFigureGallery
                    excludeEaps={excludeEaps}
                    geneId={geneId}
                    selectedRibbonTerm={selectedRibbonTerm}
                    selectedTableIds={selectedTableIDs}
                />
            }

            {selectedRibbonTerm && !selectedTablePhenotype &&
                <PhenotypeAnnotationSummaryTable
                    excludeEaps={excludeEaps}
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
