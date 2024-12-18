package org.zfin.task;

import org.zfin.framework.HibernateUtil;
import org.zfin.gwt.root.util.StringUtils;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.profile.Person;
import org.zfin.profile.service.ProfileService;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getProfileRepository;

public class AddCountryTask extends AbstractScriptWrapper {

    public static void main(String[] args) throws IOException {
        AddCountryTask addCountryTask = new AddCountryTask();
        addCountryTask.runTask();
    }

    private void runTask() {
        initAll();

        //get all Persons without a country but with an address
        List<Person> persons = getProfileRepository().getAllPeople().stream()
                .filter(person -> StringUtils.isEmpty(person.getCountry()))
                .filter(person -> !StringUtils.isEmpty(person.getAddress()))
                .toList();

        if (System.getenv("COMMIT_CHANGES") != null && System.getenv("COMMIT_CHANGES").equalsIgnoreCase("true")) {
            System.out.println("COMMIT_CHANGES environment variable is set. Changes will be committed to the database.");
        } else {
            System.out.println("DRY RUN: No changes will be committed to the database. Set the COMMIT_CHANGES environment variable to 'true' to commit changes.");
        }
        HibernateUtil.createTransaction();

        List<String> failMessages = new ArrayList<>();
        for(Person person : persons) {
            String address = person.getAddress().trim();
            address = address.replaceAll("\r\n", "\n");//do dos2unix on the address
            List<String> addressLines = Arrays.asList(address.split("\n"));
            String lastLineOfAddress = addressLines.get(addressLines.size() - 1);
            String countryGuess = lastLineOfAddress.trim();
            String countryAbbreviationBasedOnGuess = guessCountry(countryGuess);
            if (countryAbbreviationBasedOnGuess != null) {
                System.out.println("Updating " + person.getZdbID() + "(" + person.getFirstName() + " " + person.getLastName() + ")" + " country to " + countryAbbreviationBasedOnGuess + " based on last line of address field: " + countryGuess);
                currentSession().createNativeQuery("update person set person_country = :country where zdb_id = :zdb_id")
                        .setParameter("country", countryAbbreviationBasedOnGuess)
                        .setParameter("zdb_id", person.getZdbID())
                        .executeUpdate();
            } else {
                failMessages.add("No country found for " + person.getFirstName() + " " + person.getLastName() + " (https://zfin.org/" + person.getZdbID() + ")" + " based on last line of address field: " + countryGuess);
            }
        }

        failMessages.forEach(System.out::println);

        if (System.getenv("COMMIT_CHANGES") != null && System.getenv("COMMIT_CHANGES").equalsIgnoreCase("true")) {
            HibernateUtil.flushAndCommitCurrentSession();
        } else {
            HibernateUtil.rollbackTransaction();
        }

    }

    private String guessCountry(String countryGuess) {
        String country = getReverseCountryMap().get(countryGuess.toUpperCase());
        if (country == null) {
            country = checkOutlierMap(countryGuess);
        }
        return country;
    }

    private String checkOutlierMap(String countryGuess) {
        Map<String, String> outliersMap = getOutliersMap();
        return outliersMap.get(countryGuess.toUpperCase());
    }

    private Map<String, String> getReverseCountryMap() {
        Map<String, String> reversedMap = getCountryMap().entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getValue, Map.Entry::getKey));

        //Add some extras we know are in our data:
        reversedMap.put("UNITED STATES", "US");
        reversedMap.put("USA", "US");
        reversedMap.put("KOREA", "KR");

        return reversedMap;
    }

    private Map<String, String> getCountryMap() {
        //get all Countries
        Map<String, String> countries = (new ProfileService()).getCountries();

        //make it upper case
        HashMap<String, String> upperCaseMap = new HashMap<String, String>();
        for (Map.Entry<String, String> entry : countries.entrySet()) {
            upperCaseMap.put(entry.getKey().toUpperCase(), entry.getValue().toUpperCase());
        }

        return upperCaseMap;
    }

    private Map<String, String> getOutliersMap() {
        String outliers = """
Provo, UT 84602|USA
PR|Puerto Rico
Helsinki, Finland|finland
Germany.|germany
Shaanxi province, China|china
Bethesda, MD 20892|usa
Edmonton, Alberta, Canada|canada
Nuevo León, Mx|
Dunedin, New Zealand|New Zealand
33 4 67 14 38 15|
13125 Berlin|germany
Madison, WI 53706|usa
1-1-1, Yayoi, Bunkyo-ku, Tokyo 113-0032, Japan|japan
10901 N. Torrey Pines Rd, LaJolla, CA 92037|usa
Brisbane, QLD, Australia|Australia
Cambridge University LMB and Wellcome Trust Sanger Institute|
Louisville, KY  40292|usa
Faculty of Agriculture, Kyushu University, Fukuoka, Japan|japan
170 Villarroel Street, 08036 Barcelona, Spain|spain
Birmingham, Alabama 35294|usa
Boise, Idaho, 83725|usa
Address: No.7 Nanhai Road, Qingdao,Shandong,China.|china
Qingdao 266003|china
Rochester, MN 55905|usa
Illkirch, France|france
Stirling FK9 4LA Scotland, United Kingdom|United Kingdom
Dr. Reddy's Institute of Life Sciences, University of Hyderabad|
INRA, UR1037 Laboratory of Fish Physiology and Genomics. Campus de Beaulieu 35000 Rennes - FRANCE|FRANCE
Oslo, NO-0033, Norway|Norway
Pittsburgh PA 15213|usa
(B1876BXD) Bernal, Buenos Aires, Argentina|Argentina
Corvallis, OR 97331-4804|usa
Kowloon , Hong Kong|china
Kenosha, WI  53140|usa
Spain including Canary Islands, Ceuta and Melilla|spain
Brasil|brazil
Department of Biology and Ecology, Faculty of Sciences, University of Novi Sad,  Trg Dositeja Obradovica 2, 21000 Novi Sad.|
Calgary, Alberta, Canada T2N 4N1|canada
35 042 Rennes cedex, France|france
email|
Victor Chang Cardiac Research Institute, 405 Liverpool St, Darlinghurst 2010|
1-1 Idaigaoka, Yufu City, Oita, 879-5593|
1111 Yata, Mishima, Japan 411-8540|japan
WC1N1EH.|
New Haven, CT  06520-8024|usa
Hangøvej 2, 8200 Aarhus N, Denmark|Denmark
BioE building, University of California Santa Barbara. CA 93106|usa
76344 Eggenstein-Leopoldshafen, Germany|germany
109 Carrigan Dr, 120A Marsh Life Science, Burlington VT 05405|usa
Leiden, Netherlands|Netherlands
DE|germany
UK|united kingdom
Korea, Republic of|korea
Eugene, OR 97403-1254|usa
291 Daehak-ro, Yuseong-gu, Daejeon, 305-701, Korea|korea
76131 Karlsruhe, Germany|germany
Padua, Italy|italy
Taichung 40704, Taiwan|taiwan
Houston, Texas, 77030|usa
Zebrafish Developmental Genomics Laboratory, International Institute of Molecular and Cell Biology, Warsaw, Poland|poland
rut.19.588.714-4|
Columbia, MO 65201-8250|usa
The Affiliated Kangning Hospital of Wenzhou Medical University, No. 1 Shengjin Road, Lucheng District, Wenzhou City, Zhejiang Province, China.|china
Universidade Federal do Rio Grande do Norte|
Russian Federation|russia
Anonymous|
Palackeho 1-3, 612 42 Brno|
1165 Light Hall 2215 Garland Av. Nashville, TN 37232-0275 USA|usa
Tyler, TX 75799|usa
Seattle, WA 98109-4714|usa
Alegre, Brazil|Brazil
21205, USA|usa
20009 San Sebastian (Spain)|spain
London W12 0NN-United Kingdom|United Kingdom
250100|
Bldg. 66, 3rd floor, room 003|
P.O. Box: 16635-148, Tehran, Iran|iran
Korea|Korea
Instituto de Química y Fisicoquímica Biológicas (IQUIFIB)-CONICET|
Valparaiso, Chile|Chile
Quai de la Darse, 06234 Villefranche-sur-Mer Cedex, France|france
Wuhan 430072, China|china
Tecumseh, Ontario, Canada|Canada
825 N 300 W Suite N139, Salt Lake City, Utah 84103|usa
Via Vivaldi 43 - 81100 - Caserta -  Italy|italy
NY 13244, USA|usa
London, United Kingdom|United Kingdom
E-mail :  	nadia.soussi&#64;inserm.fr|
59 Nanyang Drive Singapore 636921|singapore
Santiago, Chile|Chile
P. R. China|china
RILD Building | Barrack Road, Exeter, EX2 5DW, UK‌|
Springfield, OH  45501|usa
Nankang, Taipei, Taiwan|taiwan
Unit 516, 5/F., Biotech Centre 2 (Bldg 12), No. 11 Science Park West Avenue, Hong Kong Science Park, Shatin, Hong Kong|china
St Louis, MO 63110|usa
Galveston, TX 77555-0645|usa
Stockholm, Sweden|sweden
Galway, Ireland|Ireland
Ithaca, NY|usa
Innsbruck, Austria|austria
Wageningen, Netherlands|Netherlands
Jiangning Road 495, Room 1602, Shanghai|china
IT|Italy
02-109 Warsaw Poland|poland
The University of Sheffield|
Ronkonkoma, NY 11779-7329|usa
4700 Keele St., Toronto, Ontario M3J 1P3, Canada|canada
Mailbox 437, Harbin Institute of Technology , Harbin 150001,China|china
17 000 La Rochelle, FRANCE|france
Singapore 138673|singapore
Boston, MA 02125|usa
B-3000 Leuven, Belgium|belgium
Hong Kong, Hong Kong|china
Portland, OR 97239 USA|usa
Institute of Molecular Biology &amp; Biotechnology,  Faculty of Biology, Adam Mickiewicz University,  ul. Uniwersytetu Poznańskiego 6, 61-614 Poznań, Poland|poland
00936-5067|
Stanford, CA 94305|usa
Jalan Lagoon Selatan, 47500 Bandar Sunway, Selangor Darul Ehsan, Malaysia|malaysia
266 Fangzheng Avenue, Hi-Tech District of Shuitu Town, Beibei District, Chongqing, China|china
101 Reykjavik, Iceland|iceland
University of Pennsylvania|usa
Ann Arbor, MI  48109-5646|usa
Raleigh, NC 27695-7617|usa
Blizard Institute, Barts and The London School of Medicine and Dentistry, Queen Mary University of London, 4 Newark Street, London E1 2AT, United Kingdom|United Kingdom
13115 Saint Paul lez Durance Cedex|
Singapore, SG|Singapore
Institute of Hydrobiology, Chinese Academy of Sciences, Wuhan, 430072, China|china
Suffolk, United Kingdom|United Kingdom
100050|
Centenary Institute, Locked Bag 6, Newtown, NSW 2042|Australia
Center of Genetics and Developmental Biology, College of Life Sciences, Peking University. The address is 5, Summer Palace Road, Beijing, P.R.China, 100871|china
Jiaoxi Township, Taiwan|taiwan
Kannapolis, NC 28081|usa
ZDB-LAB-141110-4|
Baltimore, MD 21205|usa
636921|
08034 Barcelona, Spain|spain
Philadelphia, PA 19107|usa
Institute of Biological and Chemical Systems - Biological Information Processing Karlsruhe Institute of Technology Postfach 3640 Karlsruhe, D-76021|
80336 Munich, Germany|Germany
Heraklion, Crete, Greece|greece
Holliston, MA 01746|usa
The Netherlands|Netherlands
C.U. de Strasbourg, France|France
Johns Hopkins University|usa
HONG KONG, China|china
Northwestern University|usa
19380|
#08-03, Singapore 138673|Singapore
Argentina.|argentina
University of Pittsburgh|usa
Checheng, Pingtung, Taiwan|taiwan
Illkirch, France 67400|france
Kowloon, Hong Kong|china
69120 Heidelberg, Germany|germany
Bridgewater, MA 02324|usa
353 Nanaline Duke Bldg, Research Drive, Durham, NC 27710|usa
Umeå University, SE-90187 Umeå, Sweden|sweden
Davis, CA 95616|usa
PA, 15213|usa
Ashburn, VA 20147|usa
Madurai, Tamilnadu-625 021|
Davis, CA 95616  USA|usa
76344 Eggenstein-Leopoldshafen|
Dept. of Cellular &amp; Molecular Neurophysiology, Instituto de Investigaciones Biologicas Clemente Estable. Av Italia 3318. 11600 Montevideo.|uruguay
Taipei, Taiwan|taiwan
Boston, MA 02115|usa
Taipei, Nankang, Taiwan|taiwan
Chaoyang district, Beijing, 100101, P.R. China|china
Alberta, Canada|canada
D-37075 Göttingen|
St Andrews, Fife, United Kingdom|United Kingdom
Aliso Viejo, CA 92656|usa
D-89081 Ulm|
97403-5291|
Durham, NC 27710|usa
(SPAIN)|spain
1026 Riverwood Dr, Longview, TX 75604|usa
97403-5291|
Singapore, 637551|Singapore
The King's University College|
EC1V 9EL. UK|united kingdom
University of Hong Kong|china
92126 USA|usa
London, WC1E 6DE|united kingdom
FIN-00014|finland
The University of Edinburgh|united kingdom
Santa Cruz, CA 95064|usa
1-5-45, Yushima, Bunkyo-ku, Tokyo, 113-8510, Japan|japan
The Norwegian School of Veterinary Science|norway
Irvine, CA 92606|usa
La Jolla, CA 92037|usa
Chicago, IL 60637|usa
Chicago, IL 60637|usa
Knoxville, TN, 37996|usa
Raleigh, NC 27603|usa
SOUTH PARKS ROAD, OXFORD  OX1 3PS, UK|united kingdom
Sichuan University, Chengdu 610041, China|china
500 University Drive, Hershey, PA 17033 USA|usa
                """;

        String[] lines = outliers.trim().split("\n");
        Map<String, String> outliersMap = new HashMap<>();
        for (String line : lines) {
            String[] parts = line.split("\\|");
            if (parts.length == 2 && !StringUtils.isEmpty(parts[0]) && !StringUtils.isEmpty(parts[1])) {
                outliersMap.put(parts[0].toUpperCase(), parts[1].toUpperCase());
            }
        }

        //transform to get country codes
        for(Map.Entry<String, String> entry : outliersMap.entrySet()) {
            String country = entry.getValue();
            String countryCode = getReverseCountryMap().get(country);
            if (countryCode != null) {
                entry.setValue(countryCode);
            }
        }

        return outliersMap;
    }

}
