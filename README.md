# foodTrucksFinder

To compile and run the application, follow these steps:

Open a command line window or terminal.
Navigate to the root directory of the project, where the pom.xml resides.
Compile the project: mvn clean compile.
Package the application: mvn package.
Look in the target directory. You should see a file with the following or a similar name: tomcat-embedded-food-trucks-finder0.0.1-SNAPSHOT.jar.
Change into the target directory.
Execute the JAR: java -jar tomcat-embedded-food-trucks-finder0.0.1-SNAPSHOT.jar.
The application should be available at http://localhost:8080/api/finder.
Example GET URL: http://localhost:8080/api/finder?latitude=37.7875398934674&longitude=-122.397726709151&numberOfFoodTrucks=5

Assumptions:
- There were only about six entries within the "days/hours" column. The other cells within the that column were empty so I treeted them as if though they would always be available.

Rationale behind technical choices:
My intial thought process as it relates to what needs to be done to solve the specified problem:
- Read in the data from the .csv file. This should only be done once and a flag (e.g. csvFileRead) should be set and checked upon subsequent calls. A hash of the CSV file or checking the last modified timestamp could be used to determine if the file has changed.
- See if I can come up with a way to determine whether each existing latitude/longitude exists within a given region (i.e. SW, NW, SE, NE of San Francisco or zip code) and subsequently set the region/zip code. This information could be used to reduce the overall number of locations for which the distance has to be calculated.
- Filter the following: Filter out any data that doesn’t have a status of APPROVED; Filter out the “days/hours” (i.e. I assume based on the current time in San Francisco).
- Calculate the distance between the specified latitude/longitude and each latitude/longitude from the .csv data set. Subsequently, set the distance on the FoodTrucksData data structure. 
- Sort the data structure holding the FoodTrucksData based on the distance. Maybe I could use a Treemap as this would allow me to store the data from the CSV file, use the distance as a key, and it is always automatically sorted.
- Return a list of the five nearest food trucks.
    
Tradeoffs:
- I traded not further investigating the aforemtioned zip code/region based aproach for a somewhat less optionmal approach to actually get the problem completed within the alloted time.

Things I would do or do differently if I were able to spend more time on the project or do it again:
- I would have investigated the utilization of zip code lat/long data in order to break up San Francisco into sections/regions as this would further reduce the data set and subsequent calculations.
- I would have used TSL/SSL (i.e. https) for encryption.
- I would have used something like Oauth for REST API authorization.
- I was able to test the REST service using Postman, however, I was not able to thoroughly unit test the service as that requires the additional setting up of REST related unit test infrastructure and mocking (i.e. and would have required more time). I really wanted to test more (e.g. more edge/corner cases), however, that would have required more time. 
- I would have created a Swagger file to document the REST API if time would have allowed as well.

Context: Although I am familiar with the deployment of applications via the Tomcat servlet container I thought this was a good opportunity to become more familiar with its embedded version.
