package edu.cmu.graphchi.toolkits.collaborative_filtering.utils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.hadoop.mapred.InvalidFileTypeException;
import org.codehaus.jackson.map.ObjectMapper;

import edu.cmu.graphchi.io.MatrixMarketDataReader;

/**
 * This class gives access to recommendation systems data persisted in a file and
 * also in a specific format.
 * All the data should live in at most 3 files
 	i. Ratings and Graph (edge) file: 
	 	This file will be in Matrix market format with slight modification. 
		Matrix Market file contains the first 3 columns. 
		<from>    <to>    <value>   <sparse vector of edge features>". 
		The additional 4th column here, "sparse vector" is a delimited string of format 
		"<Feature Id>:<Feature Value>". For example, an edge feature could be some time related feature 
		like day (Eg: "March 30, 2013" is a boolean feature) or a numerical feature like days after movie release 
		(For example, reviews for the movie rated 5 days after movie release might be different than review 
		after 500 days after movie release)

	ii. User Feature File: 
		This file contains all user features in the following format:
		"<user id> <sparse vector of user features>". These sparse vector is similar to one described above, 
		where the features can be boolean features like "Gender Male", "Age 10-20" or numerical features 
		like "average rating by this user"

	iii. Item Feature File: 
	`	All item feature information in the similar format 
		<user id> <sparse vector of user features>
		 
 * @author mayank
 */

public class FileInputDataReader implements InputDataReader {
	
	public static final String DELIM = "\t| ";
	public static final String FEATURE_DELIM = ":";
	
	String ratingFile;
	String userFile;
	String itemFile;
	DataSetDescription metadata;
	
	private MatrixMarketDataReader ratingsReader;
	
	private String currUserLine;
	private String currItemLine; 
	
	BufferedReader ratingBr = null;
	BufferedReader userBr = null;
	BufferedReader itemBr = null;
	
	public FileInputDataReader(DataSetDescription datasetDesc) {
		this.metadata = datasetDesc;
		
		this.ratingFile = datasetDesc.getRatingsUrl();
		this.userFile = datasetDesc.getUserFeaturesUrl();
		this.itemFile = datasetDesc.getItemFeaturesUrl();
	}
	
	public FileInputDataReader(String dataSetDescFile) {
		DataSetDescription datasetDesc = new DataSetDescription();
		datasetDesc.loadFromJsonFile(dataSetDescFile);
	}
	
	@Override
	public boolean initRatingData() throws IOException, InconsistentDataException {
		File file = new File(this.ratingFile);
		if(!file.exists())
			return false;
		
		this.ratingsReader = new MatrixMarketDataReader(new FileInputStream(file));
		this.ratingsReader.init();
		this.metadata.setNumUsers(ratingsReader.numLeft);
		this.metadata.setNumItems(ratingsReader.numRight);
		this.metadata.setNumRatings(ratingsReader.numRatings);
		
		return true;
	}

	@Override
	public boolean nextRatingData() throws IOException {
		return this.ratingsReader.next();
	}

	@Override
	public int getNextRatingFrom() {
		return this.ratingsReader.getCurrSource();
	}

	@Override
	public int getNextRatingTo() {
		return this.ratingsReader.getCurrDestination();
	}

	@Override
	public float getNextRating() {
		String tok = this.ratingsReader.getCurrEdgeVal().split(DELIM, 2)[0];
		if(tok != null)
			return Float.parseFloat(tok);
		else
			return -1;
	}

	@Override
	public List<Feature> getNextRatingFeatures() {
		String[] tokens = this.ratingsReader.getCurrEdgeVal().split(DELIM, 2);
		if(tokens.length < 2) {
			return null;
		} else {
			return parseFeatures(tokens[1]);
		}
	}

	@Override
	public boolean initUserData()  throws IOException, InconsistentDataException {
		this.userBr = new BufferedReader(new FileReader(new File(this.userFile)));
		return true;
	}

	@Override
	public boolean nextUser() throws IOException{
		String line = progressLine(this.userBr);
		this.currUserLine = line;
		if(line == null) {
			this.userBr = null;
			return false;
		} else {
			return true;
		}
	}

	@Override
	public int getNextUser() {
		if(this.currUserLine == null) {
			return -1;
		} else {
			return Integer.parseInt(this.currUserLine.split(DELIM)[0]);
		}
	}

	@Override
	public List<Feature> getNextUserFeatures() {
		if(this.currUserLine == null) {
			return null;
		} else {
			String[] tokens = this.currUserLine.split(DELIM,2);
			if(tokens.length < 2) {
				return null;
			} else {
				return parseFeatures(tokens[1]);
			}
		}
	}

	@Override
	public boolean initItemData()  throws IOException, InconsistentDataException {
		this.itemBr = new BufferedReader(new FileReader(new File(this.itemFile)));
		return true;
	}

	@Override
	public boolean nextItem() throws IOException {
		String line = progressLine(this.itemBr);
		this.currItemLine = line;
		if(line == null) {
			this.itemBr = null;
			return false;
		} else {
			return true;
		}
	}
	
	@Override
	public int getNextItem() {
		if(this.currItemLine == null) {
			return -1;
		} else {
			return this.metadata.getNumUsers() + 
				Integer.parseInt(this.currItemLine.split(DELIM)[0]);
		}
	}

	@Override
	public List<Feature> getNextItemFeatures() {
		if(this.currItemLine == null) {
			return null;
		} else {
			String[] tokens = this.currItemLine.split(DELIM,2);
			if(tokens.length < 2) {
				return null;
			} else {
				return parseFeatures(tokens[1]);
			}
		}
	}

	@Override
	public DataSetDescription getDataSetDescription() {
		// TODO Auto-generated method stub
		return this.metadata;
	}
	
	private String progressLine(BufferedReader br) throws IOException {
		String line = null;
		if(br != null) {
			line = br.readLine();
		}
		return line;
	}
	
	List<Feature> parseFeatures(String featureStr) {
		String[] tokens = featureStr.split(DELIM);
		List<Feature> features = new ArrayList<Feature>();
		for(String tok : tokens) {
			int featureId = Integer.parseInt(tok.split(FEATURE_DELIM)[0]);
			float featureVal = Float.parseFloat(tok.split(FEATURE_DELIM)[1]);
			features.add(new Feature(featureId, featureVal));
		}
		return features;
	}

}
