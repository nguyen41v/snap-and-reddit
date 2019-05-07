## Disclaimer!
The app is not approved by the USDA, FNS, or SNAP. 
Some information may be incorrect or not updated as the app does not have any functionality to automatically pull data from the SNAP website. 
The app is also not maintained by any of the said organizations, so use it keeping this in mind.

[Link to full, but low quality demo video](https://drive.google.com/open?id=1IBIatuOsmcm0sowH1DUVrtjkd3jGW-j2)  
[Link to part 1 of demo video](https://drive.google.com/open?id=1ySv0Jf5Fl0kTwhScDI7U1XIxGYnmD1bF)  
[Link to part 2 of demo video](https://drive.google.com/open?id=1QTqeITwQvkhqmGtu0JDo0hkspv7ekTGI)  
[Link to GitHub repo](https://github.com/nguyen41v/snap-and-reddit/tree/master/databases-snap)  

## Running the app
1. You can build and run the android studio project to run the app or you can install the apk file on your android phone to run the app

## Setting up your own instance of the server

### System requirements
* MySQL 5.6.43 or a compatible distribution (I didn't install 5.7 on my server initially . . .)
* Java 1.8

### Setting up the mySQL server
1. Go to your server and open up MySQL and create a new database, note down the name of it  
2. Copy and paste createdb.sql into mySQL to create all the tables  
3. Edit the fields in SNAP\FinalInsert.java and SNAP\src\main\java\snap\UserController.java
    You need to change:  
    * DATABASE to the name of the database on your server  
    * USER to your MySQL username  
    * PASSWORD to your MySQL password (or null if not using a password)  
4. Run FinalInsert in the Final Project directory with the MySQL connecter jar as follows:  
    `
    javac -cp mysql-connector-java-8.0.15.jar FinalInsert.java
    `  
    `
    java -cp .: mysql-connector-java-8.0.15.jar FinalInsert States States.csv Benefits Benefits.csv State_specific state_only_hotlines.csv Stores Stores.csv Stores Stores1.csv
    `  
    You might potentially run out of memory or something, so you might want to insert data into Stores separately. If you want, you can split the csv for Stores into small csv files. There are stores_locations0*.csv files that you can use (they're smaller). They have some duplicate values in them though. If you get a random error, use this command:
    `
    LANG=C sed -i 's/[\d128-\d255]//g' stores* 
    `

### Setting up the Spring Boot server
1. First, make sure the server you're using takes in connections to port 80 from any IP (unless you plan on IP blocking or limiting access)  
   Go look up on how to do it if your server can't (depends on your setup)
2. In the Final Project directory, run  
    `
    mvn package
    `  
3. Run the jar file  
    `
    cd target && java -jar snap-app-1.0-SNAPSHOT.jar
    `  
    OR if you're going to set up the server on a remote server from where Final Project was saved, 
    copy the file over and then run it on the server  
    For example, to store the file in the user directory and then run it:  
    `
    scp -i myPem.pem snap-app-1.0-SNAPSHOT.jar applications.properties myserver.com:~/
    `  
    `
    ssh -i myPem.pem user@myserver.com
    `  
    `
    java -jar snap-app-1.0-SNAPSHOT.jar
    `
4. You need to update the reddit\app\src\main\res\xml\network_security_config domain to your server's and reddit\app\src\main\java\com\example\reddit\MainActivity.java's URL to your server's domain
