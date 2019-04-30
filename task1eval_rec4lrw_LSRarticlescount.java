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

public class task1eval_rec4lrw_LSRarticlescount {

	public static void main (String[] args) throws IOException, SQLException
	{

		Connection connect1 = null;
		connect1 = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
	    Statement statement = connect1.createStatement();
		
	    File file1 = new File("rec4lrwhits_newm_topics-LSRarticlesvalues.txt");
		file1.createNewFile();
		FileWriter fw = new FileWriter(file1.getAbsoluteFile());
	    
		File folder = new File("rec4lrwhits_newm/");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
		  File file = listOfFiles[i];
		  if (file.isFile() && file.getName().endsWith(".txt")) 
		  {
			  FileInputStream fstream = new FileInputStream("rec4lrwhits_newm/"+file.getName());
				BufferedReader br1 = new BufferedReader(new InputStreamReader(fstream));
				String strLine;
				String[] articleid = new String[20];
				
				
			    int tc=0;
			    int lsrarticle=0;
				
				while ((strLine = br1.readLine()) != null)   
				{
					String[] data=strLine.split("\\|");
					articleid[tc]=data[1];
					
					 String query ="select distinct if((lower(concat(article_title, ' ',article_subtitle,' ',article_kw)) like '%survey%') OR (lower(concat(article_title, ' ',article_subtitle,' ',article_kw)) like '%review%') ,'y','n') ls_flag from combinedarticles_basic where article_id='"+articleid[tc]+"';";
					 
					 //System.out.println(query);
					 ResultSet resultSet = statement.executeQuery(query);
					 while (resultSet.next()) 
				      {
					   String lsflag=resultSet.getString("ls_flag");
					   if(lsflag.contains("y"))
					   {
						   lsrarticle++;
					   }
				      }
					tc++;
							
				}
		
				String[] filen=file.getName().toString().split("\\.");
			      System.out.println(filen[0]);
			      
				fw.write(filen[0]+","+lsrarticle);
				fw.write("\n");
				fw.flush();
				
				fstream.close();
				br1.close();
				
		  }
		}
		fw.close();
		
	}
	
}
