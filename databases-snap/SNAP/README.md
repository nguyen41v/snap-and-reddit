## Disclaimer!
The app is not approved by the USDA, FNS, or SNAP. 
Some information may be incorrect or not updated as the app does not have any functionality to automatically pull data from the SNAP website. 
The app is also not maintained by any of the said organizations, so use it keeping this in mind.

## System requirements
* MySQL 5.6.43 or a compatible distribution
* Java 1.8

## Setting up the mySQL server
1. Go to your server and open up MySQL and create a new database, note down the name of it  
2. Copy and paste createdb.sql into mySQL to create all the tables  
3. Edit the fields in FinalInsert.java  
    You need to change:  
    * DATABASE to the name of the database on your server  
    * USER to your MySQL username  
    * PASSWORD to your MySQL password (or null if not using a password)  
4. Run FinalInsert in the Final Project directory with the MySQL connecter jar as follows:  
    `
    javac -cp mysql-connector-java-8.0.15.jar FinalInsert.java
    `  
    `
    java -cp .: mysql-connector-java-8.0.15.jar FinalInsert States States.csv Benefits Benefits.csv State_specific state_only_hotlines.csv
    `  

## Setting up the Spring Boot server
1. First, make sure the server you're using takes in connections to port 80 from any IP (unless you plan on IP blocking or limiting access)  
   Go look up on how to do it if your server can't (depends on your setup)
2. In the Final Project directory, run  
    `
    mvn package
    `  
3. Run the jar file  
    `
    cd target && java -jar myserver-0.1.0.jar
    `  
    OR if you're going to set up the server on a remote server from where Final Project was saved, 
    copy the file over and then run it on the server  
    For example, to store the file in the user directory and then run it:  
    `
    scp -i myPem.pem myserver-0.1.0.jar myserver.com:~/
    `  
    `
    ssh -i myPem.pem user@myserver.com
    `  
    `
    java -jar myserver-0.1.0.jar
    `