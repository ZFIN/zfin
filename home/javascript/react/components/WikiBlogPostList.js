import React from 'react';
import PropTypes from 'prop-types';

const WikiBlogPostList = ({posts}) => {
  return (
      <ul className='list-unstyled'>
        {posts.map(post => (
            <li key={post.id}>
              <small>{new Date(post.history.createdDate).toLocaleString('en-us', {day: 'numeric', month: 'long', hour: 'numeric', minute: '2-digit', year: 'numeric'})}</small>
              <p><a href={`https://@WIKI_HOST@${post._links.webui}`}>{post.title}</a></p>
            </li>
        ))}
      </ul>
  );
};

WikiBlogPostList.propTypes = {
  posts: PropTypes.array,
};

export default WikiBlogPostList;
