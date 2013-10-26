package edu.cmu.graphchi.toolkits.collaborative_filtering.utils;

import java.util.List;

import gov.sandia.cognition.math.matrix.mtj.SparseMatrixFactoryMTJ;
import gov.sandia.cognition.math.matrix.mtj.SparseRowMatrix;
import gov.sandia.cognition.math.matrix.mtj.SparseVector;

//Immutable, read-only, in-memory vertex data cache. 
public class VertexDataCache {
	
	//Note that this internally uses an array of int and an array double for each row. 
	//Maybe float instead of double is suitable for our use case. Need to find such impl. or
	//write our own implementation.
	private SparseRowMatrix vertexFeatures;
	
	public VertexDataCache(int numVertices, int maxFeatureId) {
		this.vertexFeatures = (new SparseMatrixFactoryMTJ()).createMatrix(numVertices, maxFeatureId);
	}
	
	public SparseVector getFeatures(int vertexId) {
		return this.vertexFeatures.getRow(vertexId);
	}
	
	public void loadVertexDataCache(InputData data) throws Exception {
		data.initUserData();
		while(data.nextUser()) {
			int userId = data.getNextUser();
			List<Feature> features = data.getNextUserFeatures();
			
			for(Feature f : features) {
				this.vertexFeatures.setElement(userId, f.featureId , f.featureVal);
			}
		}
		
		data.initItemData();
		while(data.nextItem()) {
			int userId = data.getDataSetDescription().getNumUsers() + data.getNextItem();
			List<Feature> features = data.getNextItemFeatures();
			
			for(Feature f : features) {
				this.vertexFeatures.setElement(userId, f.featureId , f.featureVal);
			}
		}
		
	}

}
