import React from 'react';
import PropTypes from 'prop-types';

const PubPDFLink = ({publication}) => {
    if (!publication.pdfPath) {
        return null;
    }
    return (
        <a href={`/PDFLoadUp/${publication.pdfPath}`} target='_blank' rel='noopener noreferrer'>
            <i className='far fa-file-pdf'/>
        </a>
    );
};

PubPDFLink.propTypes = {
    publication: PropTypes.object.isRequired,
};

export default PubPDFLink;