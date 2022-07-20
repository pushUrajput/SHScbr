package examples.universityerp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;	
public class FileRead 
{
	private String fileName;
	private String sapId;
	private String record;
	FileRead(String fName, String id) throws NoRecordFoundException,IOException{
		fileName = fName;
		sapId =  id;
        System.out.println("Reading File....: "+ fileName+ " for ID: "+id);
		File file = new File("F:\\Papers\\Second_Paper\\JADE\\jade\\classes\\"+fileName); //Creation of File Descriptor for input file
		String[] fields= null;  //Intialize the fields Array
		FileReader fr = new FileReader(file);  //Creation of File Reader object
		BufferedReader br = new BufferedReader(fr); //Creation of BufferedReader object
		String s="";    
		while((s = br.readLine())!=null)   //Reading Content from the file
		{
			//System.out.println("Current Record: "+s);
			fields = s.split(" ");  //Split the record using space
        	if (fields[0].equals(sapId))   //Search for the given sap id 
        	{
           		record = s;
        	}
        }
        if(record == null)  //Check for count not equal to zero
		{
			System.out.println("Fualt Detected: Exception in File Read");
			throw new NoRecordFoundException(fileName);
		}
		br.close();
   	}
   public String getData()
   {
	   return record;
   }
}