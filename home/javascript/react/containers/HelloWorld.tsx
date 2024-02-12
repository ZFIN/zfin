import React from 'react';

interface HelloWorldProps {
    name: string;
}

export default function HelloWorld(props: HelloWorldProps) {
    return <h1>Hello, {props.name}</h1>;
}