#!/bin/bash -e
#
# Download SwissProt and TrEMBL zebrafish, mouse, and human FASTA files
# from UniProt's REST API, using cursor-paginated /search rather than
# /stream so a mid-download connection drop only forfeits the current
# page rather than the whole file.
#
# See: https://www.uniprot.org/help/pagination
#      https://www.uniprot.org/help/api_queries

source "../config.sh"

SEARCH_URL="https://rest.uniprot.org/uniprotkb/search"
PAGE_SIZE=500     # UniProt-recommended page size for paginated downloads

taxonomy_id_for() {
    case "$1" in
        zebrafish) echo 7955  ;;
        mouse)     echo 10090 ;;
        human)     echo 9606  ;;
        *) error_exit "unknown organism: $1" ;;
    esac
}

download_organism_fasta() {
    local organism="$1"
    local taxonomy_id; taxonomy_id=$(taxonomy_id_for "${organism}")
    local output="${organism}.fasta"
    local headers; headers=$(mktemp)

    log_message "== Downloading ${organism} (organism_id:${taxonomy_id}) via paginated /search =="
    : > "${output}"

    local url="${SEARCH_URL}?query=organism_id:${taxonomy_id}&format=fasta&size=${PAGE_SIZE}"
    local page=0
    while [[ -n "${url}" ]]; do
        page=$((page + 1))
        curl --silent --show-error --fail --compressed \
             --max-time 300 --retry 3 --retry-delay 5 \
             -D "${headers}" \
             "${url}" >> "${output}"

        local total
        total=$(awk 'BEGIN{IGNORECASE=1} /^x-total-results:/ {print $2}' "${headers}" | tr -d '\r')
        local link
        link=$(awk 'BEGIN{IGNORECASE=1} /^link:/ {sub(/^[^:]*:[ \t]*/,""); print}' "${headers}" | tr -d '\r')

        if [[ "${link}" =~ \<([^\>]+)\>\;\ *rel=\"next\" ]]; then
            url="${BASH_REMATCH[1]}"
        else
            url=""
        fi

        if (( page == 1 || page % 20 == 0 )) || [[ -z "${url}" ]]; then
            log_message "  ${organism} page ${page}: $(grep -c '^>' "${output}")/${total:-?} sequences ($(du -h "${output}" | cut -f1))"
        fi
    done

    rm -f "${headers}"
    log_message "Rewriting >tr to >sp in ${organism}.fasta"
    sed -i 's/>tr/>sp/g' "${output}"
    log_message "Done with ${organism}: $(grep -c '^>' "${output}") sequences"
}

for organism in zebrafish mouse human; do
    download_organism_fasta "${organism}"
done

log_message "==| done downloading SPTrEMBL |=="
