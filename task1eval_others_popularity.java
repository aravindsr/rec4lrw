import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class task1eval_others_popularity {

	public static void main (String[] args) throws IOException, SQLException
	{

		Connection connect1 = null;
		connect1 = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
	    Statement statement = connect1.createStatement();
		
	    File file1 = new File("rwr_topics-popularityvalues.txt");
		file1.createNewFile();
		FileWriter fw = new FileWriter(file1.getAbsoluteFile());
	    
		File folder = new File("rwr/");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
		  File file = listOfFiles[i];
		  if (file.isFile() && file.getName().endsWith(".txt")) 
		  {
			  FileInputStream fstream = new FileInputStream("rwr/"+file.getName());
				BufferedReader br1 = new BufferedReader(new InputStreamReader(fstream));
				String strLine;
				String[] articleid = new String[20];
				
				
			    int tc=0;
			    int populararticle=0;
				
				while ((strLine = br1.readLine()) != null)   
				{
					String[] data=strLine.split("\\|");
					articleid[tc]=data[0];//change this to 1 for all methods except rwr
					
					 String query ="select distinct citations_count from combinedarticles_basic where article_id='"+articleid[tc]+"';";
					 
					 //System.out.println(query);
					 ResultSet resultSet = statement.executeQuery(query);
					 while (resultSet.next()) 
				      {
					   int cc=resultSet.getInt("citations_count");
					   if(cc >= 50)
					   {
						   populararticle++;
					   }
				      }
					tc++;
							
				}
		
				String[] filen=file.getName().toString().split("\\.");
			      System.out.println(filen[0]);
			      
				fw.write(filen[0]+","+populararticle);
				fw.write("\n");
				fw.flush();
		  }
		}
		fw.close();
		
	}
	
}
