import React, {useState} from 'react';
import PropTypes from 'prop-types';
import qs from 'qs';
import DataTable from '../components/data-table';
import DataTableSummaryToggle from '../components/DataTableSummaryToggle';
import PublicationSummary from '../components/PublicationSummary';
import {EntityLink} from '../components/entity';
import CommaSeparatedList from '../components/CommaSeparatedList';

const ChebiTermZebrafishModelTable = ({termId, directAnnotationOnly }) => {

    const [directAnnotation, setDirectAnnotation] = useState(directAnnotationOnly === 'true');
    const [count, setCount] = useState({'countDirect': 0, 'countIncludingChildren': 0});

    const columns = [
        {
            label: 'Disease',
            content: (row) => <EntityLink entity={row.fishModelDisplay.disease}/>,
            filterName: 'diseaseName',
            width: '180px',
        },
        {
            label: 'Fish',
            content: (row) => <a
                href={'/' + row.fishModelDisplay.fish.zdbID}
                dangerouslySetInnerHTML={{__html: row.fishModelDisplay.fish.name}}
            />,
            filterName: 'fishName',
            width: '300px',
        },
        {
            label: 'Conditions',
            content: (row) => <span className='text-break'>
                <a
                    className='text-break'
                    href={`/${row.fishModelDisplay.experiment.zdbID}`}
                    dangerouslySetInnerHTML={{__html: row.fishModelDisplay.experiment.conditions}}
                />
                <a
                    className='popup-link data-popup-link'
                    href={`/action/experiment/popup/${row.fishModelDisplay.experiment.zdbID}`}
                />
            </span>,
            filterName: 'conditionName',
            width: '200px',
        },
        {
            label: 'Evidence',
            content: (row) => <CommaSeparatedList>
                {row.fishModelDisplay.evidenceCodes.map(entity => {
                    return <>{entity.abbreviation}</>
                })}
            </CommaSeparatedList>,
            filterName: 'evidenceCode',
            width: '180px',
        },
        {
            label: 'Chebi Term',
            content: (row) => <EntityLink entity={row.chebi}/>,
            filterName: 'chebiName',
            width: '180px',
        },
        {
            label: 'Citation',
            content: row => (
                <PublicationSummary
                    numberOfPublications={row.fishModelDisplay.numberOfPublications}
                    firstPublication={row.fishModelDisplay.singlePublication}
                    fishID={row.fishModelDisplay.fish.zdbID}
                    experimentID={row.fishModelDisplay.experiment.zdbID}
                    termID={row.fishModelDisplay.disease.zdbID}
                    allPublicationUrl={`/action/ontology/phenotype-summary/${row.fishModelDisplay.fish.zdbID}`}
                />
            ),
            filterName: 'citation',
            width: '230px',
        },

    ];

    const params = {};
    if (directAnnotation) {
        params.directAnnotation = true;
    }

    return (
        <>
            {directAnnotationOnly && count.countIncludingChildren > 0 && (
                <DataTableSummaryToggle
                    showPopup={directAnnotation}
                    directCount={count.countDirect}
                    childrenCount={count.countIncludingChildren}
                    onChange={setDirectAnnotation}
                />
            )}
            <DataTable
                columns={columns}
                dataUrl={`/action/api/ontology/${termId}/chebi-zebrafish-models?${qs.stringify(params)}`}
                onDataLoadedCount={(count) => setCount(count)}
                rowKey={row => row.zdbID}
            />
        </>
    );
};

ChebiTermZebrafishModelTable.propTypes = {
    termId: PropTypes.string,
    directAnnotationOnly: PropTypes.string,
};

export default ChebiTermZebrafishModelTable;
