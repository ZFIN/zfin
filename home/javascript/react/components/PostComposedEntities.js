import React from 'react';
import PropTypes from 'prop-types';

const PostComposedEntities = ({entities}) => {
    // get the indices of the highlighted stages in the all stages array, remove
    // non-existent ones and sort numerically.
    return (
        <div>
            {
                entities.map((postComposedEntity) => {
                    return (
                        <div key={postComposedEntity.superterm.zdbID}>{postComposedEntity.superterm.termName}</div>
                    );
                })
            }
        </div>
    );
};

PostComposedEntities.propTypes = {
    entities: PropTypes.array,
};

export default PostComposedEntities;
