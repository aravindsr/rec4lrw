import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import net.sf.classifier4J.summariser.SimpleSummariser;

import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.common.Weighting;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.Recommender;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;


public class task2_fetcharticleids_new {
	
	static int[] articleidmysql = new int[50];
	static int a1=0;
	
	 

	public static void main (String[] args) throws SQLException, IOException, TasteException
	{
		Connection connect=null;
		
		connect = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
		
		// statements allow to issue SQL queries to the database
	      Statement statement = connect.createStatement();
	      // resultSet gets the result of the SQL query
		
		//receive string from php file
		//String[] articletitles = args[0].split("\\r?\\n");
	      String value = args[0];
	      String[] values=value.split("\\|");
	      String username=values[0];
	      String collection_name=values[1];
	      
	      //System.out.println(collection_name);
			//System.out.println(i+" "+articletitles[i]);
			ResultSet resultSet = statement.executeQuery("select article_id from rec4lrw_user_task2list where username='"+username+"' and collection_name='"+collection_name+"'");
		     // ResultSet resultSet = statement.executeQuery("insert into dummy1(a) values(2)");
			//System.out.println("select article_id from proceedings_articles_final where lower(if(article_subtitle='',article_title,concat(article_title,': ',article_subtitle))) like lower('%"+articletitles[i]+"%')");
		    readResultSet(resultSet);
		
		
		cfrecommender(articleidmysql,a1);
		
	}
	
	private static void readResultSet(ResultSet resultSet) throws SQLException 
	{
	    // resultSet is initialised before the first data set
	    while (resultSet.next()) 
	    {
	    	  int aid = resultSet.getInt("article_id");
	    	  //System.out.println(aid+ " "+aid);
	    	  articleidmysql[a1]=aid;
	    	  a1++;
	    	
	    }
	
	}
	
	private static void cfrecommender(int[] articleid1level,int arti_count) throws IOException, TasteException, SQLException
	{
		
		 DataModel model = new FileDataModel(new File("article_refpluscit_cfmatrix.txt"));//"Articlerefmatrix_formahout_new.csv"));
			//UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity(model);
			//UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity(model,Weighting.WEIGHTED);
			ItemSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity(model,Weighting.WEIGHTED);
			//UserNeighborhood neighborhood = new ThresholdUserNeighborhood(1.0, similarity, model);
			//UserNeighborhood neighborhood = new NearestNUserNeighborhood(5, similarity, model);
			
			
			//UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
			ItemBasedRecommender recommender = new GenericItemBasedRecommender(model, similarity);
			CachingRecommender rec1 = new CachingRecommender(recommender);
					
			String[] rec_articleid = new String[10000];
			int recid=0;
		
		for(int b=0;b<arti_count;b++)
		{
			
			
			//System.out.println(articleid1level[b]);
			
			
	    	
	    	long aid = Long.valueOf(articleid1level[b]).longValue();
	    	
	    	//Code for recommending similar users based on UBCF
	    	
	    	//long[] similarusers = recommender.mostSimilarUserIDs(aid, 20);
	    	List<RecommendedItem> recommendations = rec1.recommend(aid, 10);
			
			for (RecommendedItem recommendation: recommendations)
			{
			//userrecs[q]=exisusers[p]+","+recommendation.getItemID();
			//System.out.println(recommendation.getItemID());
			rec_articleid[recid]=String.valueOf(recommendation.getItemID());
			recid++;
			}
	    	
	    	
	    	//System.out.println(similarusers.length);
	    	//for(int m=0;m < similarusers.length;m++)
	    	//{
	    		//System.out.println("For article "+aid+", similar article: "+similarusers[m]);
	    	//	rec_articleid[recid]=String.valueOf(similarusers[m]);
	    		//recid++;
	    	//}
			
			
		}
	
		//System.out.println(recid);
		
		//for getting unique values
		String[] uniqueids = new HashSet<String>(Arrays.asList(rec_articleid)).toArray(new String[0]);
		
		//removing nulls
		List<String> list = new ArrayList<String>(Arrays.asList(uniqueids));
	    list.removeAll(Collections.singleton(null));
	    uniqueids= list.toArray(new String[list.size()]);
		
		
		//System.out.println(uniqueids.length);
		
		for(int c=0;c<uniqueids.length;c++)
		{
			if(uniqueids[c] != null)
			{
			//System.out.println(c+ " "+uniqueids[c]);
			}
		}
	
		//call mysql again
		mysqlretrievetitleabs(uniqueids);
	
	}
	
	private static void mysqlretrievetitleabs(String[] uniids) throws SQLException
	{
		
Connection connect=null;
		
		connect = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
		Statement statement = connect.createStatement();
		 
		String articleids="";
		int z=0;
		
		for(int i=0;i<uniids.length;i++)
		{
			 
			  //System.out.println(artar4[0].trim()+ " "+artar4[1].trim());
			  if(i ==0)
			  {
				  articleids=uniids[i];
			  }
			  else
			  {
			  articleids=articleids+","+uniids[i];
			  }
		  }
			//old query
			//String query ="select a.article_id article_id,replace(IF(a.article_subtitle = '',a.article_title,concat(a.article_title,': ',a.article_subtitle)),',','') article_title, b.article_abstract article_abstract "+
			  //      "from proceedings_articles_final a,"+
			    //    "(select article_id, article_abstract from proceedings_articles where article_id="+uniids[i]+
			      //  " union select article_id, article_abstract from proceedings_articles_temp where article_id="+uniids[i]+") b where a.article_id = b.article_id;";
			
			String query ="select article_id, replace(IF(article_subtitle = '',article_title,concat(article_title,': ',article_subtitle)),',','') article_title, replace(replace(article_abstract,'<p>',''),'</p>','') article_abstract, article_pubdate, article_kw, article_authors,article_doi,convert(citations_count,UNSIGNED INTEGER) citations_count from combinedarticles_basic where article_id in ("+articleids+") order by citations_count desc limit 20";
			//System.out.println(query);
			
			ResultSet resultSet = statement.executeQuery(query);
		    
			while (resultSet.next()) 
		      {
			    z++;  
		  	    int article_id=resultSet.getInt("article_id");
		  	 	String article_title = resultSet.getString("article_title");
			  	String article_abstract = resultSet.getString("article_abstract");
			  	article_abstract= article_abstract.replace("<p>","");
			  	article_abstract= article_abstract.replace("</p>","");
			  	int lala=z;
			  	if(article_abstract.length() < 5)
			  	{
			  		article_abstract="No data";
			  	}
			  	else
			  	{
			  		SimpleSummariser summariser = new SimpleSummariser();
			  		article_abstract = summariser.summarise(article_abstract, 2).trim().replace("\n", "");
			  	}
			  	
			  	String article_authors = resultSet.getString("article_authors");
			  	String article_kw = resultSet.getString("article_kw");
			  	
			  	if(article_authors.length() < 1)
			  	{
			  		article_authors="No authors data";
			  	}
			  	
			  	if(article_kw.length() < 1)
			  	{
			  		article_kw="No keywords data";
			  	}
			  	
			  	
			  	String article_pubdate = resultSet.getString("article_pubdate");
			  	String article_doi = resultSet.getString("article_doi");
			  	String article_citationscount = resultSet.getString("citations_count");
			  	
			  	if(article_doi.length() < 1)
			  	{
			  		article_doi="https://scholar.google.com/scholar?hl=en&q="+article_title;
			  	}
			  	else
			  	{
			  		article_doi="http://dx.doi.org/"+article_doi;
			  	}
			  	
			  	
			  	System.out.println(lala+"|cf|"+article_id+"|"+article_title+"|"+article_abstract+"|"+article_authors+"|"+article_pubdate+"|"+article_kw+"|"+article_doi+"|"+article_citationscount);
			  	
		      }
			
			
		    
		
		
		
	}
	
	
	
}
