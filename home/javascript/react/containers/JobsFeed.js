import React from 'react';
import {getPosts} from '../api/wiki';
import WikiPostList from '../components/WikiPostList';

function JobsFeed() {
    return <WikiPostList onInit={() => getPosts('jobs')} showAll='/display/jobs'/>
}

export default JobsFeed;
