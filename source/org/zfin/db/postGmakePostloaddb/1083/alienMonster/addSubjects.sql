--liquibase formatted sql
--changeset sierra:addSubjects.sql

insert into pub_correspondence_subject (pcs_subject_text, pcs_subject_type, pcs_handle)
 values ('ZFIN uses short line designations to identify mutant and transgenic fish. Each designation consists of a letter code identifying the institution in which the fish line was generated, followed by a number, so that the combination of the code and number is unique. Lab designations and nomenclature conventions are listed on these web pages. https://wiki.zfin.org/display/general/ZFIN+Zebrafish+Nomenclature+Guidelines
','Explanation of line numbers','nomenclature');


insert into pub_correspondence_subject (pcs_subject_text, pcs_subject_type, pcs_handle)
 values ('In this article you generated a new transgenic zebrafish line. At ZFIN we create records in our database for all mutant and transgenic zebrafish lines that are published. We would like to add this line to the database, but in order to link it to your lab we will need to set up personal and lab records for you at ZFIN.

Request to set up personal and lab records:
If you will provide me with the following information, I will set up the record for you and email your log in information back to you. Then you will be able to update the information and change your password at your convenience.

To set up your personal record, I will need your:
Address
Phone and fax numbers
Email address
URL
For the Lab record, I will need the following information:
Lab mailing address
Names and email addresses of lab members
Their positions in the lab, i.e., Post-Doc, Grad student, etc.
Contact person for the lab
Phone and fax numbers
Optional information such as a statement of research interests and a short biography.  
If you have any questions, please let me know.
','New line with no records in the database','addPerson');


insert into pub_correspondence_subject (pcs_subject_text, pcs_subject_type, pcs_handle)
 values ('Your institution designation is "la". If you would like to see a list of the "la" lines already in use follow the link below and click on the "+" next to the "la" in the left hand column.          
http://zfin.org/action/feature/line-designations

The _Univeristy Name_ Institution designation is "_". This designation is shared between all zebrafish labs at the _University name_ so you will need to coordinate with the __ labs. We recommend that individual labs be assigned blocks of numbers to use to prevent double publishing.
','How to choose line numbers','nomenclature');

