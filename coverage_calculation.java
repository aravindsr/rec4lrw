import java.io.BufferedReader;
import java.io.BufferedWriter;
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

public class coverage_calculation {

	public static void main (String[] args) throws NumberFormatException, IOException, SQLException
	{
		FileInputStream fstream = new FileInputStream("combinedarticles_id.txt");
		BufferedReader br1 = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		String[] articleid = new String[115885];
						
		int i=0;
		while ((strLine = br1.readLine()) != null)   
		{
			//System.out.println(strLine);
			articleid[i] = strLine.trim();
		
			i++;
		}
		
		br1.close();
		fstream.close();
		System.out.println(i);
		
		String[] outputcoverage = new String[i];
		
		Connection connect=null;
		
		connect = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
		
		// statements allow to issue SQL queries to the database
	      Statement statement = connect.createStatement();
		
		for(int j=0;j<10000;j++)
		{
			System.out.println(articleid[j]);
			ResultSet resultSet = statement.executeQuery("select count(ref_cit_id) ct from combinedarticles_refsandcitations where article_id ='"+articleid[j]+"' and ref_cit_id in (select distinct article_id from combinedarticles_keywords where keyword_text in (select keyword_text from combinedarticles_keywords where article_id='"+articleid[j]+"') and article_id <> '"+articleid[j]+"')");
			while (resultSet.next()) 
		    {
		    	  String ct = resultSet.getString("ct");
		    	  //System.out.println(aid+ " "+aid);
		    	  outputcoverage[j]=articleid[j]+","+ct;
		    	  	    	
		    }
		}
		
		File file = new File("combinedarticles_coveragegenerated_100000.txt");
		file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
				
		for(int k=0;k<10000;k++)
		{
			fw.write(outputcoverage[k]);
			fw.write("\n");
			fw.flush();
		}
		
		fw.close();
		
	}
}
