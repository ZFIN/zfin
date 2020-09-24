import React from 'react';
import PropTypes from 'prop-types';
import LoadingSpinner from './LoadingSpinner';

const WikiList = ({
    posts,
    showDate = true,
    loading,
    hasMore,
    onLoadMore,
    showAllUrl,
}) => {

    const handleLoadMore = (event) => {
        event.preventDefault();
        onLoadMore();
    };

    return (
        <div>
            <ul className='list-unstyled'>
                {posts.map(post => (
                    <li key={post.id}>
                        {showDate && <small>{new Date(post.history.createdDate).toLocaleString('en-us', {
                            day: 'numeric',
                            month: 'long',
                            hour: 'numeric',
                            minute: '2-digit',
                            year: 'numeric'
                        })}</small>}
                        <p><a href={`https://${process.env.WIKI_HOST}${post._links.webui}`}>{post.title}</a></p>
                    </li>
                ))}
            </ul>
            <LoadingSpinner loading={loading} />
            <div className='wiki-list-controls'>
                <span>{ !loading && hasMore && <a href='#' onClick={handleLoadMore}>Load More</a> }</span>
                <span>{ showAllUrl && <a href={`https://${process.env.WIKI_HOST}${showAllUrl}`}>See All <i className='fas fa-chevron-right' /></a>}</span>
            </div>
        </div>
    );
};

WikiList.propTypes = {
    posts: PropTypes.array,
    showDate: PropTypes.bool,
    loading: PropTypes.bool,
    hasMore: PropTypes.bool,
    onLoadMore: PropTypes.func,
    showAllUrl: PropTypes.string,
};

export default WikiList;
