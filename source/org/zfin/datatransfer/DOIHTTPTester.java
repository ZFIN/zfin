package org.zfin.datatransfer ; 

import java.util.Iterator ; 
import java.util.List ; 
import java.net.URL; 
import java.net.HttpURLConnection ; 
import java.io.BufferedReader ; 
import java.io.InputStreamReader; 

import org.apache.log4j.Logger;

import org.zfin.properties.ZfinProperties;
import org.zfin.publication.Publication ;
import org.zfin.datatransfer.webservice.Citexplore ; 

/**
 *  Class DOIHTTPTester tests 
 */
public class DOIHTTPTester {


    private Logger fullLogger = Logger.getLogger( ZfinProperties.FULL_UPDATE_DOI) ; 

    public List<Publication> testDOIList(List<Publication> publications){

//        System.err.println("dois to test: " + publications.size()) ; 
        fullLogger.info("dois to test: " + publications.size()) ; 
        Iterator<Publication> iter = publications.iterator() ; 
        Publication pub ; 
        while(iter.hasNext()){
            pub = iter.next() ; 
            if(  pub.getDoi()==null || checkPubExists(pub)==false){
                 fullLogger.info("INVALID doi["+ pub.getDoi()+"]"); 
                 iter.remove() ; 
            }
            else{
                 fullLogger.info("VALID doi["+ pub.getDoi()+"]"); 
            }
        }
        fullLogger.info("dois still valid: " + publications.size()) ; 
        return publications ; 
    }

    public boolean checkPubExists(Publication publication){
        HttpURLConnection connection  = null ; 
        BufferedReader reader  = null ; 
        try{
            String connectionString =  Citexplore.DOI_URL+ "/" + publication.getDoi() ; 
            HttpURLConnection.setFollowRedirects(true) ; 
            URL url = new URL(connectionString) ; 
            connection = (HttpURLConnection) url.openConnection() ; 
            if(connection.getResponseCode() != connection.HTTP_OK){
                fullLogger.warn("BAD Connection: " + connection.getResponseCode() ) ; 
                return false ; 
            }
            // read stream
            // if title is 'Not Found', return false, else return true
          
            int i = 0 ; 

            reader = new BufferedReader( new InputStreamReader( connection.getInputStream())) ;  
            String line = reader.readLine() ; 
            while(line!=null){
                if(line.indexOf("<title>")>=0 || line.indexOf("<TITLE>")>=0){
                    if(line.indexOf("DOI Not Found")>0){
                        fullLogger.info("bad DOI connection: " +  publication.getDoi()) ; 
                        return false ; 
                    }
                    else{
                        return true; 
                    }
                }
                line = reader.readLine() ; 
            }
            fullLogger.warn("error in html:" + connectionString ) ; 
            return false ; 
        }
        catch(Exception e){
            fullLogger.error(e) ; 
            return false ; 
        }
        finally{
            try{
                if(connection!=null){
                    connection.disconnect() ; 
                }
                if(reader!=null){
                    reader.close() ; 
                }
            }
            catch(Exception e){
                fullLogger.error(e) ; 
            }
        }

    }

} 


