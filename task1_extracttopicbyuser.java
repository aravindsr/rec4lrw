import java.io.File;
import java.io.FileNotFoundException;
import java.util.Scanner;

public class task1_extracttopicbyuser {

	public static void main (String[] args) throws FileNotFoundException
	{
		String directorylocation="C:\\Aravind files\\OP files dump\\From Experiment";
		File folder = new File(directorylocation);
		File[] listOfFiles = folder.listFiles();
		int expectedfilescount=0;

		    for (int i = 0; i < listOfFiles.length; i++) {
		      if (listOfFiles[i].isFile()) {
		    	if(listOfFiles[i].getName().contains("rar_ind") || listOfFiles[i].getName().contains(".txt") || listOfFiles[i].getName().contains("task2")|| listOfFiles[i].getName().contains("task3"))
		    	{
		    		//do nothing
		    	}
		    	else
		    	{
		    		//System.out.println("File " + listOfFiles[i].getName());
		    		String currentfilename=listOfFiles[i].getName();
		    		
		    		String content = new Scanner(new File(directorylocation+"\\"+currentfilename)).useDelimiter("\\Z").next();
		    		String requiredstring="";
		    		requiredstring = content.substring(content.indexOf("1 \"") + 1);
		    		requiredstring = requiredstring.substring(0, requiredstring.indexOf("\" >"));
		    		requiredstring = requiredstring.replace("\"", "").trim();
		    		//System.out.println(requiredstring);
		    		
		    		//String[] breakstring= requiredstring.split("\\|");
		    		String researchtopic=requiredstring;
		    		
		    		String[] breakfilename = currentfilename.split("\\.");
		    		String timmestamp = breakfilename[0].substring(5, 15);
		    		String participant = breakfilename[0].substring(15);
		    		//System.out.println(breakfilename[0]);
		    		//System.out.println(timmestamp);
		    		//System.out.println(participant);
		    		//System.out.println(participant+","+timmestamp+","+researchtopic);
		    		if(participant.length() > 0)
		    		{
		    		System.out.println(participant+","+timmestamp+","+researchtopic);
		    		expectedfilescount++;
		    		}
		    	}
		    	  
		        
		        
		      } else if (listOfFiles[i].isDirectory()) {
		        System.out.println("Directory " + listOfFiles[i].getName());
		      }
		    }
		    
		    System.out.println("the total number of required files is "+expectedfilescount);
	}
	
}
