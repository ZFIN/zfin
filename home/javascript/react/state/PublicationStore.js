import { configureStore } from '@reduxjs/toolkit'

const createReducer = () => {

    const initialState = {
        navigationCounts: {},
    };

    const navigationCountsReducer = (state = initialState, action) => {
        // Check to see if the reducer cares about this action
        if (action.type === 'count/set') {
            const {title, count} = action.payload;
            const updatedCounts = {...state.navigationCounts, [title]: count};
            const updatedState = {...state, "navigationCounts": updatedCounts};
            return updatedState;
        }
        // otherwise return the existing state unchanged
        return state
    };

    return navigationCountsReducer;
}

let reducerSingleton;
const getReducer = () => {
    if (reducerSingleton) {
        return reducerSingleton;
    }
    return createReducer();
}
reducerSingleton = getReducer();

let storeSingleton;
const getStore = () => {
    if (storeSingleton) {
        console.log("store already exists");
        return storeSingleton;
    }
    console.log("create store");
    return configureStore({
        reducer: getReducer(),
    });
}
storeSingleton = getStore();

export default storeSingleton;


// // Still pass action objects to `dispatch`, but they're created for us
// store.dispatch(incremented())
// // {value: 1}
// store.dispatch(incremented())
// // {value: 2}
// store.dispatch(decremented())
// // {value: 1}
