import React from 'react';
import {getPosts} from '../api/wiki';
import WikiPostList from '../components/WikiPostList';

function NewsFeed() {
    return <WikiPostList onInit={() => getPosts('news')} showAll='/display/news'/>
}

export default NewsFeed;
