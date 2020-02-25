import React, { useState } from 'react';
import PropTypes from 'prop-types';
import {useFetch} from '../utils/effects';
import LoadingSpinner from '../components/LoadingSpinner';
import {GenericRibbon} from '@geneontology/ribbon';

import style from './style.scss';
import DataTable, {DEFAULT_TABLE_STATE} from '../components/DataTable';

const GeneOntologyRibbon = ({geneId}) => {
    const [selected, setSelected] = useState(null);
    const [tableState, setTableState] = useState(DEFAULT_TABLE_STATE);

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

    const columns = [
        {
            label: 'Ontology',
            content: ({ontology}) => ontology,
            width: '60px',
            hidden: selected && selected.group.type !== 'GlobalAll',
        },
        {
            label: 'Qualifier',
            content: ({qualifier}) => qualifier,
            width: '60px',
        },
        {
            label: 'Term',
            content: ({term}) => <a href={`/${term.oboID}`}>{term.termName}</a>,
            width: '200px',
        },
        {
            label: 'Annotation Extension',
            content: ({annotationExtensions}) => annotationExtensions.map(ax => <div key={ax} dangerouslySetInnerHTML={{__html: ax}} />),
            width: '100px',
        },
        {
            label: 'Evidence',
            content: ({evidenceCode}) => (
                <a href={`http://www.geneontology.org/GO.evidence.shtml#${evidenceCode.code.toLowerCase()}`}>
                    {evidenceCode.code}
                </a>
            ),
            width: '50px',
        },
        {
            label: 'With/From',
            content: ({inferenceLinks}) => inferenceLinks.map(il => <div key={il} dangerouslySetInnerHTML={{__html: il}} />),
            width: '120px',
        },
        {
            label: 'Publications',
            content: ({publications}) => publications.map(pub => (
                <div key={pub.zdbID}><a href={'/' + pub.zdbID} dangerouslySetInnerHTML={{__html: pub.shortAuthorList}} /></div>
            )),
            width: '120px',
        },
    ];

    const handleItemClick = (subject, group) => {
        if (selected && selected.group.id === group.id && selected.group.type === group.type) {
            setSelected(null);
        } else {
            setTableState(DEFAULT_TABLE_STATE);
            setSelected({subject, group});
        }
    };

    let termQuery = '';
    if (selected) {
        if (selected.group.type !== 'GlobalAll') {
            termQuery += `?termId=${selected.group.id}`;
        }
        if (selected.group.type === 'Other') {
            termQuery += '&isOther=true';
        }
    }

    return (
        <div>
            <div className='ontology-ribbon-container'>
                <GenericRibbon
                    subjects={data.value.subjects}
                    categories={data.value.categories}
                    hideFirstSubjectLabel
                    colorBy={1} // annotations
                    binaryColor
                    maxColor={[style.primaryR, style.primaryG, style.primaryB]}
                    itemClick={handleItemClick}
                    selected={selected}
                />
            </div>

            {selected &&
                <DataTable
                    url={`/action/api/marker/${geneId}/go${termQuery}`}
                    columns={columns}
                    rowKey='id'
                    tableState={tableState}
                    onTableStateChange={setTableState}
                />
            }
        </div>
    );
};

GeneOntologyRibbon.propTypes = {
    geneId: PropTypes.string,
};

export default GeneOntologyRibbon;
