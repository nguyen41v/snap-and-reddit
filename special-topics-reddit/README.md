## Disclaimer!
This is just a project! Don't sue me friends.

[Link to demo video](https://drive.google.com/open?id=17u2P_LEzlr45iOuYdfPg53WGvJGHJO9r)  
[Link to GitHub repo](https://github.com/nguyen41v/snap-and-reddit/tree/master/special-topics-reddit)  

## Running the app
1. You can build and run the android studio project to run the app or you can install the apk file on your android phone to run the app.

## Setting up your own instance of the server

### System requirements
* MySQL 5.6.43 or a compatible distribution (I didn't install 5.7 on my server initially . . .)
* Java 1.8

### Setting up the mySQL server
1. Go to your server and open up MySQL and create a new database, note down the name of it  
2. Copy and paste createdb.sql into mySQL to create all the tables  
3. Edit the fields in MyReddit\src\main\java\project\UserController.java
    You need to change:  
    * DATABASE to the name of the database on your server  
    * USER to your MySQL username  
    * PASSWORD to your MySQL password (or null if not using a password)

### Setting up the Spring Boot server
1. First, make sure the server you're using takes in connections to port 80 from any IP (unless you plan on IP blocking or limiting access)  
   Go look up on how to do it if your server can't (depends on your setup)
2. In the Final Project directory, run  
    `
    mvn package
    `  
3. Run the jar file  
    `
    cd target && java -jar myreddit-0.1.0.jar
    `  
    OR if you're going to set up the server on a remote server from where Final Project was saved, 
    copy the file over and then run it on the server  
    For example, to store the file in the user directory and then run it:  
    `
    scp -i myPem.pem myserver-0.1.0.jar applications.properties myserver.com:~/
    `  
    `
    ssh -i myPem.pem user@myserver.com
    `  
    `
    java -jar myreddit-0.1.0.jar
    `
4. You need to update the reddit\app\src\main\res\xml\network_security_config domain to your server's and reddit\app\src\main\java\com\example\reddit\MainActivity.java's URL to your server's domain
