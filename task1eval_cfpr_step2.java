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
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;

public class task1eval_cfpr_step2 {

	public static void main (String[] args) throws IOException, TasteException, SQLException
	{
		
        DataModel model = new FileDataModel(new File("modified_article_refpluscit_cfmatrix-prnormalized-corrected.txt"));	
		
		ItemSimilarity similarity = new UncenteredCosineSimilarity(model);
		//CachingItemSimilarity sim1 = new CachingItemSimilarity(similarity, model);
		
		ItemBasedRecommender recommender = new GenericItemBasedRecommender(model, similarity);
		
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
				String[] recarcticleid = new String[1000];
				
			    int tc=0;
			    int reccount=0;
				
				while ((strLine = br1.readLine()) != null)   
				{
					articleid[tc]=strLine.trim();
					List<RecommendedItem> recommendations = recommender.recommend(Integer.valueOf(articleid[tc]), 5);
					
					for (RecommendedItem recommendation: recommendations)
					{
						recarcticleid[reccount]=String.valueOf(recommendation.getItemID());
						reccount++;
					
					}
				tc++;	
				}
				
				System.out.println(reccount);
								
				//String[] mergeseednrecs = ArrayUtils.addAll(articleid, recarcticleid);
				//String[] mergeseednrecs_unique = new HashSet<String>(Arrays.asList(mergeseednrecs)).toArray(new String[0]);
				//System.out.println(mergeseednrecs.length);
				
				//mysql part
				String mysqlids="";
				
				for(int k=0;k<200;k++)
				{
				if(k == 0)
					{
						mysqlids=articleid[k];
					}
					else{
						mysqlids=mysqlids+","+articleid[k];
					}
				}
				
				for(int b=0;b<reccount;b++)
				{
				
					mysqlids=mysqlids+","+recarcticleid[b];
				}
												
				System.out.println(file.getName());
			      String[] filen=file.getName().toString().split("\\.");
			      System.out.println(filen[0]);
			      
			     String s = filen[0].toLowerCase();
			     String[] words = s.split("\\s+");
			     int wordlength = words.length;
			     String wordsearch="";
			     for(int h=0;h< wordlength;h++)
			     {
			    	 if(h==0)
			    	 {
			    	 wordsearch=" lower(concat(a.article_title, ' ',a.article_subtitle,' ',a.article_kw)) like  '%"+words[h]+"%'";
			    	 }
			    	 else{
			    		 
			    		 wordsearch=wordsearch+" and lower(concat(a.article_title, ' ',a.article_subtitle,' ',a.article_kw)) like  '%"+words[h]+"%'";
			    	 }
			     }
				
				Connection connect1 = null;
				connect1 = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
			    Statement statement = connect1.createStatement();
			    String query ="select distinct a.article_id,a.citations_count,a.references_count, cast(b.prvaluenorm as DECIMAL(9,5)) pr from combinedarticles_basic a, temp_purp_pr b where a.article_id=b.article_id and a.article_id in ("+mysqlids+") and "+wordsearch+" order by pr desc limit 20";
				 int z=0;
				 System.out.println(query);
				      ResultSet resultSet = statement.executeQuery(query);
				      
				      
				      
				      File file1 = new File("task-1_evaluation/cfpr/"+filen[0]+".txt");
						file1.createNewFile();
						FileWriter fw = new FileWriter(file1.getAbsoluteFile());
				      
				      while (resultSet.next()) 
				      {
					    z++;  
				  	    int article_id=resultSet.getInt("article_id");
				  	    String article_citationscount = resultSet.getString("citations_count");
					  	String article_refscount = resultSet.getString("references_count");
					  	String article_rank = resultSet.getString("pr");
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
