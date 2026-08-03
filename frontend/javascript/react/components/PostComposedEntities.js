import React from 'react';
import PropTypes from 'prop-types';

const PostComposedEntities = ({entities}) => {
    return (
        <div>
            {
                entities.map((postComposedEntity) => {
                    return (
                        <div key={postComposedEntity.superterm.zdbID}>
                            <span key={postComposedEntity.superterm.zdbID}>
                                {postComposedEntity.superterm.termName}
                            </span>
                            {
                                postComposedEntity.subterm &&
                                <span key={postComposedEntity.subterm.zdbID}>
                                    &nbsp; {postComposedEntity.subterm.termName}
                                </span>
                            }
                        </div>
                    )
                })
            }
        </div>
    );
};

PostComposedEntities.propTypes = {
    entities: PropTypes.array,
};

export default PostComposedEntities;
