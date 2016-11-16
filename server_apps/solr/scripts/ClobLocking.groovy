

/*
#!/bin/bash
//usr/bin/env groovy -cp "$GROOVY_CLASSPATH" "$0" $@; exit $?

*/

import java.sql.Connection
import java.sql.DriverManager
import java.sql.ResultSet

def dbname = System.getenv('DBNAME')
def host = System.getenv('SQLHOSTS_HOST')
def port = System.getenv('INFORMIX_PORT')
def informixServer = System.getenv('INFORMIXSERVER')



args = [driver: 'com.informix.jdbc.IfxDriver',
        url: "jdbc:informix-sqli://$host:$port/$dbname:INFORMIXSERVER=$informixServer;DB_LOCALE=en_US.utf8"
]

Class.forName("com.informix.jdbc.IfxDriver")

Connection conn = null
Properties connectionProps = new Properties()
connectionProps.put("driver", args.driver)

conn = DriverManager.getConnection(args.url, connectionProps)
conn.setTransactionIsolation(Connection.TRANSACTION_READ_UNCOMMITTED);

println("Connected to database")


pubQuery = """
    select zdb_id as id,
    title as name,
    title as full_name,
    zero_pad(pub_mini_ref) as name_sort,
    pub_mini_ref as alias,
    'Publication' as category,
    '/' || zdb_id as url,
    pub_date as date,
    pub_arrival_date as pet_date,
    pub_abstract,
    jtype as publication_type,
    accession_no as related_accession,
    case when pub_is_indexed = 't' then 'Indexed'
      else 'Not Indexed' end as indexing_status,
    case when status = 'active' then 'Active'
      when status = 'inactive' then 'Inactive'
      else status end as publication_status,
    case when pts_status != 'CLOSED'
      then 'Open'
      else 'Closed' end as curation_status,
    keywords as keyword,
    authors as author_string
    from publication, pub_tracking_history, pub_tracking_status
    where pth_pub_zdb_id = zdb_id
    and pth_status_id = pts_pk_id
    and pth_status_is_current = 't'
"""

ResultSet rs = conn.createStatement().executeQuery(pubQuery);
while (rs.next()) {
    def clob = rs.getClob("pub_abstract")
    if (clob) {
        BufferedReader br = new BufferedReader(clob.getCharacterStream())
        print "."
    }

}



