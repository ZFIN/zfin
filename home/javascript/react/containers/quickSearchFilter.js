// Pure-JS filter used by QuickSearchDialog. Lives outside the JSX file so it
// can be unit-tested via node:test (which doesn't transpile JSX in .js files).
export const findMatches = (input, query) => {
    let split_on = ['-', ':', ',', '(', ')', '.', ';']; //in addition to spaces
    let output = [];
    if (input === undefined) {
        return;
    }
    if (query !== undefined && query !== '') {
        let query_value_string = query.toLowerCase();
        //todo: if we run into performance trouble, do this once when the list is loaded rather than for each keystroke
        split_on.forEach(function(split_char) {
            query_value_string = query_value_string.replaceAll(split_char,' ');
        });
        let query_list = query_value_string.split(' ');

        input.forEach(function(facetValue) {
            let value_string = facetValue.value.toLowerCase();
            //split on commas and colons
            split_on.forEach(function(split_char) {
                value_string = value_string.replaceAll(split_char,' ');
            });
            let value_list = value_string.split(' ');
            let query_hit_map = [];
            query_list.forEach(function(query_component) {
                query_hit_map[query_component] = false;
            });

            for (let j = 0 ; j < value_list.length ; j++) {
                for (let i = 0 ; i < query_list.length ; i++) {
                    if (value_list[j].indexOf(query_list[i]) === 0) {
                        query_hit_map[query_list[i]] = true;
                    }
                }
            }

            let addToOutput = true;
            query_list.forEach(function(query_component) {
                if (query_hit_map[query_component] === false) {
                    addToOutput = false;
                }
            });

            if (addToOutput) {
                output.push(facetValue);
            }
        });
    } else { //if there was no query, return everything...
        return input;
    }
    return output;
};
