import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class task1eval_rec4lrw_step2 {

	public static void main(String[] args) throws IOException, SQLException
	{
		File folder = new File("task-1_evaluation/");
		File[] listOfFiles = folder.listFiles();

		for (int i = 0; i < listOfFiles.length; i++) {
		  File file = listOfFiles[i];
		  if (file.isFile() && file.getName().endsWith(".txt")) 
		  {
			  FileInputStream fstream = new FileInputStream("task-1_evaluation/"+file.getName());
				BufferedReader br1 = new BufferedReader(new InputStreamReader(fstream));
				String strLine;
				String[] articleid = new String[200];
				String articleidlist="";
				
			    int tc=0;
				
				while ((strLine = br1.readLine()) != null)   
				{
					String[] data = strLine.trim().split(",");
					articleid[tc]= data[0].toLowerCase();
					if(tc == 0)
					{
						articleidlist=articleid[tc];
					}
					else{
					articleidlist=articleidlist+","+articleid[tc];
					}
					 tc++;
				}
				
				System.out.println(tc);
				
				br1.close();
				fstream.close();
				
				Connection connect1 = null;
				connect1 = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
			    Statement statement = connect1.createStatement();
			    //String query ="select distinct a.article_id,a.cit_ct,a.ref_ct,a.cov_ct,(((cit_ct-mincit_ct)/(maxcit_ct-mincit_ct))*0.25) + (((ref_ct-minref_ct)/(maxref_ct-minref_ct))*0.25) + (((cov_ct-mincov_ct)/(maxcov_ct-mincov_ct))*0.5) composite_rank,convert(a.citations_count,UNSIGNED INTEGER) citations_count from (select article_id,convert(citations_count,UNSIGNED INT) cit_ct,convert(references_count,UNSIGNED INT) ref_ct,convert(coverage,UNSIGNED INT) cov_ct,convert(citations_count,UNSIGNED INTEGER) citations_count from combinedarticles_basic where article_id in ("+articleidlist+")) a,(select max(convert(citations_count,UNSIGNED integer)) maxcit_ct,min(convert(citations_count, UNSIGNED INTEGER)) mincit_ct,max(convert(references_count,UNSIGNED integer)) maxref_ct,min(convert(references_count, UNSIGNED INTEGER)) minref_ct,max(convert(coverage,UNSIGNED integer)) maxcov_ct,min(convert(coverage, UNSIGNED INTEGER)) mincov_ct from combinedarticles_basic where article_id in ("+articleidlist+")) b order by composite_rank desc limit 20";
			    //String query ="select distinct a.article_id,a.cit_ct,a.ref_ct,a.cov_ct,(((cit_ct-mincit_ct)/(maxcit_ct-mincit_ct))*0.1) + (((ref_ct-minref_ct)/(maxref_ct-minref_ct))*0.1) + (((cov_ct-mincov_ct)/(maxcov_ct-mincov_ct))*0.8) composite_rank,convert(a.citations_count,UNSIGNED INTEGER) citations_count from (select article_id,convert(citations_count,UNSIGNED INT) cit_ct,convert(references_count,UNSIGNED INT) ref_ct,convert(coverage,UNSIGNED INT) cov_ct,convert(citations_count,UNSIGNED INTEGER) citations_count from combinedarticles_basic where article_id in ("+articleidlist+")) a,(select max(convert(citations_count,UNSIGNED integer)) maxcit_ct,min(convert(citations_count, UNSIGNED INTEGER)) mincit_ct,max(convert(references_count,UNSIGNED integer)) maxref_ct,min(convert(references_count, UNSIGNED INTEGER)) minref_ct,max(convert(coverage,UNSIGNED integer)) maxcov_ct,min(convert(coverage, UNSIGNED INTEGER)) mincov_ct from combinedarticles_basic where article_id in ("+articleidlist+")) b order by composite_rank desc limit 20";
			   //Coverage + Normalized hits values
			    String query ="select distinct a.article_id,a.cit_ct,a.ref_ct,a.cov_ct,(((cit_ct-mincit_ct)/(maxcit_ct-mincit_ct))*0.1) + (((ref_ct-minref_ct)/(maxref_ct-minref_ct))*0.1) + (((cov_ct-mincov_ct)/(maxcov_ct-mincov_ct))*0.8) composite_rank,convert(a.citations_count,UNSIGNED INTEGER) citations_count from (select article_id,convert(citations_count,UNSIGNED INT) cit_ct,convert(references_count,UNSIGNED INT) ref_ct,convert(coverage,UNSIGNED INT) cov_ct,convert(citations_count,UNSIGNED INTEGER) citations_count from (select distinct a1.article_id article_id,citations_count cit_ct, citations_count, references_count,coverage*b1.hubvaluenorm coverage from  combinedarticles_basic a1, temp_purp b1 where a1.article_id=b1.article_id and a1.article_id in ("+articleidlist+")) d) a,(select max(convert(citations_count,UNSIGNED integer)) maxcit_ct,min(convert(citations_count, UNSIGNED INTEGER)) mincit_ct,max(convert(references_count,UNSIGNED integer)) maxref_ct,min(convert(references_count, UNSIGNED INTEGER)) minref_ct,max(convert(coverage,UNSIGNED integer)) maxcov_ct,min(convert(coverage, UNSIGNED INTEGER)) mincov_ct from (select distinct a1.article_id article_id,citations_count cit_ct, citations_count, references_count,coverage*b1.hubvaluenorm coverage from  combinedarticles_basic a1, temp_purp b1 where a1.article_id=b1.article_id and a1.article_id in ("+articleidlist+")) e) b order by composite_rank desc limit 20";
			   
				 int z=0;
				 System.out.println(query);
				      ResultSet resultSet = statement.executeQuery(query);
				      
				      System.out.println(file.getName());
				      String[] filen=file.getName().toString().split("\\.");
				      System.out.println(filen[0]);
				      
				      File file1 = new File("rec4lrwhits_newm/"+filen[0]+".txt");
						file1.createNewFile();
						FileWriter fw = new FileWriter(file1.getAbsoluteFile());
				      
				      while (resultSet.next()) 
				      {
					    z++;  
				  	    int article_id=resultSet.getInt("article_id");
				  	    String article_citationscount = resultSet.getString("citations_count");
					  	String article_refscount = resultSet.getString("ref_ct");
					  	String article_rank = resultSet.getString("composite_rank");
					  	//System.out.println(z+"|"+rectype+"|"+article_id+"|"+
					  	fw.write(z+"|"+article_id+"|"+article_citationscount+"|"+article_refscount+"|"+article_rank);
						fw.write("\n");
						fw.flush();
				      }
				      fw.close();
		  } 
		}
	}
	
}
