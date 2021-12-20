import React from 'react';
import PropTypes from 'prop-types';

const PostComposedEntity = ({postComposedEntity}) => {
    if (postComposedEntity.subterm == null) {
        return (
            <div
                key={postComposedEntity.superterm.zdbID}
            >
                <a
                    key={postComposedEntity.superterm.zdbID}
                    href={'/action/ontology/term-detail/' + postComposedEntity.superterm.oboID}
                    dangerouslySetInnerHTML={{__html: postComposedEntity.superterm.termName}}
                />
                <a
                    className='popup-link data-popup-link'
                    href={`/action/ontology/term-detail-popup?termID=${postComposedEntity.superterm.zdbID}`}
                />
            </div>
        )
    } else {
        return (
            <div key={postComposedEntity.superterm.zdbID + postComposedEntity.subterm.zdbID}>
                <a
                    key={postComposedEntity.superterm.zdbID + postComposedEntity.subterm.zdbID}
                    href={'/action/ontology/post-composed-term-detail?superTermID=' + postComposedEntity.superterm.oboID + '&subTermID=' + postComposedEntity.superterm.oboID}
                    dangerouslySetInnerHTML={{__html: postComposedEntity.superterm.termName + ' ' + postComposedEntity.subterm.termName}}
                />
                <a
                    className='popup-link data-popup-link'
                    href={`/action/ontology/post-composed-term-detail-popup?superTermID=${postComposedEntity.superterm.oboID}+'&subTermID='+${postComposedEntity.subterm.oboID}`}
                />
            </div>
        )
    }
};

PostComposedEntity.propTypes = {
    postComposedEntity: PropTypes.shape({
        superterm: PropTypes.object,
        subterm: PropTypes.object,
    })
};

export default PostComposedEntity;
