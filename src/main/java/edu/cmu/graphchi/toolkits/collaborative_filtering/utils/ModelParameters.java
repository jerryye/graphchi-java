package edu.cmu.graphchi.toolkits.collaborative_filtering.utils;

import gov.sandia.cognition.math.matrix.mtj.SparseVector;

import java.io.Serializable;
import java.util.Map;

import org.codehaus.jackson.map.ObjectMapper;

public abstract class ModelParameters implements Serializable{
    /**
	 * 
	 */
	private static final long serialVersionUID = -372468029741472974L;
	protected boolean serialized;
	protected String id;
	protected Map<String, String> paramsMap;
	
	public ModelParameters(String id, Map<String, String> paramsMap) {
		this.serialized = false;
		this.id = id;
		this.paramsMap = paramsMap;
	}
	
	public String getId() {
		return this.id;
	}
	
	public Map<String, String> getParamsMap() {
		return this.paramsMap;
	}
	
	public String toJsonString() throws Exception {
		ObjectMapper mapper = new ObjectMapper();
		return mapper.writeValueAsString(this.paramsMap);
	}
	
	/**
	 * This function serializes the model into the given location.
	 * @param location : Serialize the model into this location (could be local FS, 
	 * HDFS or some other persistent store).
	 */
	abstract public void serialize(String location);

	protected void setSerializedTrue(){
		this.serialized = true;
	}
	
	/**
	 * 
	 * @param userId : The original user id (internally while sharding graphchi might translate it)
	 * @param itemId : The original item id (internally while sharding graphchi might translate it)
	 * @param userFeatures : SparseVector representing features of user (Example: Male, Female, Age 10 to 20, etc)
	 * @param itemFeatures : SparseVector representing features of an item. (Example: Action genre, days since release)
	 * @param edgeFeatures : SparseVector representing features of an edge (Example: timestamp)
	 * @param datasetDesc : The description of the dataset.
	 * @return
	 */
	abstract public double predict(int userId, int itemId, 
			SparseVector userFeatures, SparseVector itemFeatures, SparseVector edgeFeatures,
			DataSetDescription datasetDesc);
	
	abstract public int getEstimatedMemoryUsage(DataSetDescription datasetDesc);
	
	
	@Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((id == null) ? 0 : id.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        ModelParameters other = (ModelParameters) obj;
        if (id == null) {
            if (other.id != null)
                return false;
        } else if (!id.equals(other.id))
            return false;
        return true;
    }
	

}
