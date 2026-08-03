import PropTypes from 'prop-types';
import React from 'react';

const SequenceInformationTable = ({markerLinks, editClicked, deleteClicked}) => {

    function mapLinksToLinkDisplays(links) {
        if (!links || links.length === 0) {
            return [];
        }
        let tempLinkDisplays = links.map(mapLinkToLinkDisplay);
        
        let previousDataType = tempLinkDisplays[0].dataTypeColumn;
        for (let i = 0; i < tempLinkDisplays.length; i++) {
            tempLinkDisplays.dataTypeColumn = tempLinkDisplays[i].dataTypeColumn;
            if (i > 0 && tempLinkDisplays[i].dataTypeColumn === previousDataType) {
                tempLinkDisplays[i].dataTypeColumn = '';
            } else {
                previousDataType = tempLinkDisplays[i].dataTypeColumn;
            }
        }
        return tempLinkDisplays;
    }

    function mapLinkToLinkDisplay(link) {
        return {
            ...link,
            referenceDatabaseDisplay: link.referenceDatabaseName + ' - ' + link.dataType,
            refLink: link.attributionLink,
            dataTypeColumn: link.dataType,
            modalLink: link.urlPrefix + link.accession,
            modalTitle: link.referenceDatabaseName + ':' + link.accession
        };
    }

    return (
        <table className='summary rowstripes'>
            <tbody>
                <tr>
                    <th width='30%'>Type</th>
                    <th width='50%'> Accession #</th>
                    <th width='20%'> Length (nt/aa)</th>
                </tr>
                {markerLinks && mapLinksToLinkDisplays(markerLinks).map((link, index) => {
                    return <tr key={link.displayName} className={index % 2 === 0 ? 'odd' : 'even'}>
                        <td>{link.dataTypeColumn}</td>
                        <td>
                            <a href={link.modalLink}>{link.modalTitle}</a>{' '}
                            <span dangerouslySetInnerHTML={{__html: link.refLink}}/>{' '}
                            <span style={{cursor: 'pointer'}} onClick={() => editClicked(link)}>
                                <i
                                    className='far fa-edit red'
                                    aria-hidden='true'
                                    title='Update the sequence information'
                                />
                            </span>{' '}
                            <span style={{cursor: 'pointer'}} onClick={() => deleteClicked(link)}>
                                <i
                                    className='fas fa-trash red'
                                    aria-hidden='true'
                                    title='Delete the sequence information'
                                />
                            </span>{' '}
                        </td>
                        <td>{link.length}</td>
                    </tr>
                })}
            </tbody>
        </table>
    );

}

SequenceInformationTable.propTypes = {
    markerLinks: PropTypes.array,
    editClicked: PropTypes.func,
    deleteClicked: PropTypes.func
}

export default SequenceInformationTable;