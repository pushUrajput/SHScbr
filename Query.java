package examples.universityerp;

import java.io.Serializable;
//A class to pass and receive query to and from a file
public class Query implements Serializable {
	private String fileName;
	private String sapId;
	Query(String fName, String id){
		fileName = fName;
		sapId = id;
	}
	public String getFileName(){
		return fileName;
	}
	public String getSapId(){
		return sapId;
	}
}
 