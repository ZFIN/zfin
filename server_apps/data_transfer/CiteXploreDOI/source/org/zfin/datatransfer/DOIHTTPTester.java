package org.zfin.datatransfer ; 

import java.util.Iterator ; 
import java.util.List ; 
import java.net.URL; 
//import java.net.URLConnection ; 
import java.net.HttpURLConnection ; 
import java.io.BufferedReader ; 
import java.io.InputStreamReader; 
/**
 *  Class DOIHTTPTester tests 
 */
public class DOIHTTPTester {

    public List<Publication> testDOIList(List<Publication> publications){

        System.err.println("dois to test: " + publications.size()) ; 
        Iterator<Publication> iter = publications.iterator() ; 
        Publication pub ; 
        while(iter.hasNext()){
            pub = iter.next() ; 
            if(  pub.getPubDOI()==null || checkPubExists(pub)==false){
                 System.err.println("INVALID doi["+ pub.getPubDOI()+"]"); 
                 iter.remove() ; 
            }
            else{
                 System.err.println("VALID doi["+ pub.getPubDOI()+"]"); 
            }
        }
        System.err.println("dois still valid: " + publications.size()) ; 
        return publications ; 
    }

    public boolean checkPubExists(Publication publication){
        HttpURLConnection connection  = null ; 
        BufferedReader reader  = null ; 
        try{
            String connectionString =  CitexploreWSDLConnect.DOI_URL+ "/" + publication.getPubDOI() ; 
//            System.err.println("connectionString: " + connectionString) ; 
            HttpURLConnection.setFollowRedirects(true) ; 
            URL url = new URL(connectionString) ; 
            connection = (HttpURLConnection) url.openConnection() ; 
//            System.err.println("responde code: " + connection.getResponseCode()) ; 
            if(connection.getResponseCode() != connection.HTTP_OK){
                System.err.println("BAD Connection: " + connection.getResponseCode() ) ; 
                return false ; 
            }
            // read stream
            // if title is 'Not Found', return false, else return true
          
            int i = 0 ; 

            reader = new BufferedReader( new InputStreamReader( connection.getInputStream())) ;  
            String line = reader.readLine() ; 
            while(line!=null){
//                System.err.println(line) ; 
                if(line.indexOf("<title>")>=0 || line.indexOf("<TITLE>")>=0){
                    if(line.indexOf("DOI Not Found")>0){
                        System.err.println("bad DOI connection: " +  publication.getPubDOI()) ; 
                        return false ; 
                    }
                    else{
                        return true; 
                    }
                }
                line = reader.readLine() ; 
            }
            System.err.println("error in html:" + connectionString ) ; 
            return false ; 
        }
        catch(Exception e){
            e.printStackTrace() ; 
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
                System.err.println(e) ; 
            }
        }

    }

} 


