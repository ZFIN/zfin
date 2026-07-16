import React from 'react';
import { publicationType } from '../utils/types';

const PublicationCitationLink = ({publication}) => {
    return <a href={'/' + publication.zdbID} dangerouslySetInnerHTML={{__html: publication.shortAuthorList}} />;
};

PublicationCitationLink.propTypes = {
    publication: publicationType
};

export default PublicationCitationLink;
