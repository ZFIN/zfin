
let navigationCounts = {};
let listeners = {};

const getCounts = () => {
    return {...navigationCounts};
};

const setCounts = (keyName, count) => {
    console.log("counts", keyName, count, navigationCounts);
    navigationCounts[keyName] = count;
    const currentListeners = listeners[keyName] || [];
    currentListeners.forEach(listener => listener(count));
};

const subscribe = (keyName, callback) => {
    const currentListeners = listeners[keyName] || [];
    console.log("listeners", keyName, currentListeners);
    listeners[keyName] = [...currentListeners, callback];
};

export { getCounts, subscribe, setCounts };


