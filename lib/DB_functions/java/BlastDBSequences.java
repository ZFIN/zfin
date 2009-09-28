import java.sql.* ;
import java.math.* ;
import java.util.List;
import java.util.ArrayList;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public class BlastDBSequences {

    /**
     * Get blastdb from dblink and use accession:
     * 1 - type (from dblink)
     * 2/3 - path / abbreviation (from dblink)
     * 4 - sequence accession
     */
    public static String getSequencesForDBLink( String dblink )
            throws SQLException {
        Connection conn = null ;
        try{
            Class.forName("com.informix.jdbc.IfxDriver") ;
            conn = DriverManager.getConnection("jdbc:informix-direct") ;

            PreparedStatement preparedStatement = conn.prepareStatement("select bdb.blastdb_type[1] as type, " +
                    " bdb.blastdb_path || bdb.blastdb_abbrev as path, dbl.dblink_acc_num as accession from db_link dbl\n" +
                    "join blast_database bdb on dbl.dblink_primary_blastdb_zdb_id=bdb.blastdb_zdb_id " +
            " where dbl.dblink_zdb_id=?") ;
            preparedStatement.setString(1,dblink);
            ResultSet resultSet = preparedStatement.executeQuery() ;

            String type = "" ;
            String path = "" ;
            String accession = "" ;
            // we only should get one record for one dblink
            if(resultSet.next()){
                type = resultSet.getString("type") ;
                path = resultSet.getString("path") ;
                accession = resultSet.getString("accession") ;
//                String executeString = "/private/apps/wublast/xdget -d -" + type + " " + path + " "+ accession ;
                List commandList = new ArrayList();
                commandList.add("/private/apps/wublast/xdget");
                commandList.add("-d");
                commandList.add("-"+type);
                commandList.add(path);
                commandList.add(accession);
                String[] commands = new String[commandList.size()] ;
                String executeString = "" ;
                for (int i = 0 ; i < commands.length ; i++) {
                    commands[i] = commandList.get(i).toString() ;
                    executeString += commands[i].toString() + " " ;
                }

                Process process = Runtime.getRuntime().exec(commands);

                BufferedReader stdout = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line ;
                String standardOutput = "";
                while ((line = stdout.readLine()) != null) {
                    standardOutput += line + "\n";
                }

                return standardOutput ;
            }
            else{
                return "no records found for: " + dblink ;
            }

        }
        catch(Exception e){
            //        throw new SQLException(e.toString()) ;
            return "exception: "  + e.toString();
        }
    }

    /**
     * 1 - type (from dblink)
     * 2/3 - path / abbreviation (from dblink)
     * 4 - sequence to write to fasta file and reread
     */
    public static void addSequence( String blastDBZdbID,String sequence){


//    "/private/apps/wublast/xdformat -%s -a %s/%s /tmp/%s 2>/dev/null",



    }


}
