/**
 * 
 */
package com.hortonsoft.foodtruck;

import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.google.gson.Gson;
import com.opencsv.CSVReader;
import com.opencsv.CSVReaderBuilder;

/**
 * @author dhorton
 *
 */
@Path("finder")
public class FoodTrucksFinder {	
	/**
	 * File path from which the food truck data will be retrieved.
	 */
	private static final String CSV_FILE_PATH = "../src/main/resources/Mobile_Food_Facility_Permit.csv";
	
	/**
	 * Boolean which denotes whether or not the food truck data has been read.
	 */
	private boolean mFoodTruckDataRead = false;
	
	/**
	 * The class logger.
	 */
	private Log mLogger = LogFactory.getLog(FoodTrucksFinder.class);


	@GET
    @Produces(MediaType.APPLICATION_JSON)
	public String findNearestFoodTrucks(@QueryParam("latitude") double pLatitude1,
			                            @QueryParam("longitude") double pLongitude1,
			                            @QueryParam("numberOfFoodTrucks") int pNumberOfFoodTrucks) {
		List<String[]> vAllData = null;
		TreeMap<Double, String[]> vTreeMap = null;
		String vResponse = null;
		try {
			// Read in food truck data.
			if (isFoodTruckDataRead() == false) {
				vAllData = readInFoodTruckData(CSV_FILE_PATH);
				setFoodTruckDataRead(true);
			}	
			// Filter the food truck data.
			List<String[]> vFilteredFoodTruckData = filterFoodTruckData(vAllData);
			// Create the TreeMap (i.e. sorted) for the purposes of
			// storing the "distance" key and food truck data.
			vTreeMap = new TreeMap<Double, String[]>();			
			// Calculate the distance between the given
			// latitude/longitude data and the data retrieved
			// from the CSV file for each entry.
			for(String [] vRow : vFilteredFoodTruckData) {
				double vLatitude2 = Double.valueOf(vRow[14]);
				double vLongitude2 = Double.valueOf(vRow[15]);
				// Calculate the distance.
				double vDistance = calculateDistance(pLatitude1, pLongitude1, vLatitude2, vLongitude2, "M");
				// Store the distance and the row within the treemap.
				vTreeMap.put(vDistance, vRow);
			}
			// Retrieve the first N entries within the
			// TreeMap (i.e. which is already sorted).			
			TreeMap<Double, String[]> vFirstNEntriesTreeMap = putFirstNEntries(pNumberOfFoodTrucks, vTreeMap);
			
			List<String[]> vFirstNEntries = vFirstNEntriesTreeMap.values()
                    .stream()
                    .collect(Collectors.toList());
			
			vResponse = new Gson().toJson(vFirstNEntries);

		} catch (Exception e) {			
			// Log an error message.
			mLogger.error(e.getMessage(), e);
			// Return an error response
			// message containing information
			// for the requestor, if possible.
			ResponseMessage vResponseMessage = new ResponseMessage();
			vResponseMessage.setResponseCode("500");
			vResponseMessage.setResponseMessage("ERROR: exception caught, please contact your hortonsoft administrator.");
			vResponse = new Gson().toJson(vResponseMessage);
		}
        return vResponse;
    }
	
	/**
	 * Reads in the food truck data from the given CSV file.
	 * 
	 * @param pFileName
	 * @return
	 * @throws IOException
	 */
	private List<String[]> readInFoodTruckData(String pFileName) throws IOException  {
		List<String[]> vAllData = null;
		// Create a FileReader object with 
		// the CSV filename as a parameter. 
		FileReader vFileReader = new FileReader(pFileName); 

		// Create a CSVReader object and skip the header row.
		CSVReader vCsvReader = new CSVReaderBuilder(vFileReader) 
				.withSkipLines(1) 
				.build(); 
		vAllData = vCsvReader.readAll();  		
		return vAllData;
	}
	
	/**
	 * Filters the food truck data.
	 * 
	 * @param pAllData
	 * @return
	 */
	private List<String[]> filterFoodTruckData(List<String[]> pAllData) {		
		// Status filter
		List<String[]> vFilteredFoodTruckData = pAllData
				                                .stream()
				                                .filter(row -> row[10].equals("APPROVED"))
				                                .collect(Collectors.toList());
		return vFilteredFoodTruckData;
	}
	
	/**
	 * Calculates the distance between two sets of 
	 * latitude/longitude coordinates using the specified units.
	 * 
	 * @param pLatitude1
	 * @param pLongitude1
	 * @param pLatitude2
	 * @param pLongitude2
	 * @param pUnit - 'M' statue miles
	 *                'K' kilometers 
	 *                'N' nautical miles
	 * @return
	 */
	private double calculateDistance(double pLatitude1, double pLongitude1, 
			                         double pLatitude2, double pLongitude2, 
			                         String pUnit) {
		if ((pLatitude1 == pLatitude2) && (pLongitude1 == pLongitude2)) {
			return 0;
		}
		else {
			double theta = pLongitude1 - pLongitude2;
			double dist = Math.sin(Math.toRadians(pLatitude1)) *
					      Math.sin(Math.toRadians(pLatitude2)) + 
					      Math.cos(Math.toRadians(pLatitude1)) * 
					      Math.cos(Math.toRadians(pLatitude2)) * 
					      Math.cos(Math.toRadians(theta));
			dist = Math.acos(dist);
			dist = Math.toDegrees(dist);
			dist = dist * 60 * 1.1515;
			if (pUnit.equals("K")) {
				dist = dist * 1.609344;
			} else if (pUnit.equals("N")) {
				dist = dist * 0.8684;
			}
			return (dist);
		}
	}

	/**
	 * Creates a data structure containing the nearest N number 
	 * of food trucks.
	 * 
	 * @param pNumberOfFoodTrucks
	 * @param vSourceTreeMap
	 * @return
	 */
	private  TreeMap<Double, String[]> putFirstNEntries(int pNumberOfFoodTrucks, TreeMap<Double, String[]> vSourceTreeMap) {
		int vCount = 0;
		TreeMap<Double, String[]> vTargetTreeMap = new TreeMap<Double, String[]>();
		for (Map.Entry<Double, String[]> entry:vSourceTreeMap.entrySet()) {
			if (vCount >= pNumberOfFoodTrucks) {
				break;
			}
			vTargetTreeMap.put(entry.getKey(), entry.getValue());
			vCount++;
		}
		return vTargetTreeMap;
	}

	/**
	 * Returns a boolean value which denotes whether or 
	 * not the food truck data has been read in or not.
	 * 
	 * @return
	 */
	private boolean isFoodTruckDataRead() {
		return mFoodTruckDataRead;
	}

	/**
	 * Allows for the setting of the boolean variable
	 * which denotes whether or not the food truck data
	 * has been read.
	 * 
	 * @param foodTruckDataRead
	 */
	private void setFoodTruckDataRead(boolean foodTruckDataRead) {
		this.mFoodTruckDataRead = foodTruckDataRead;
	}
}
