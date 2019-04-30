import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import net.sf.classifier4J.summariser.SimpleSummariser;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.util.Version;
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


public class task2_fetcharticleids_3waymerge {
	
	static int[] articleidmysql = new int[50];
	static String articletitlemysql = new String();
	static String articleidconcatmysql = new String();
	static String[] mergedrecs = new String[1000];
	static int a1=0;
	static int recid=0;
	static String username="";
	static String collection_name="";
	static String t1topic="";

	public static void main (String[] args) throws SQLException, IOException, TasteException, ParseException
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
	      username=values[0];
	      collection_name=values[1];
	      t1topic=values[2];
	      
	     
	      
	      //System.out.println(collection_name);
			//System.out.println(i+" "+articletitles[i]);
	      //retrieve both articleids and article title
			ResultSet resultSet = statement.executeQuery("select distinct a.article_id article_id,concat(b.article_title,b.article_subtitle) article_title from rec4lrw_user_task2list a,combinedarticles_basic b where a.username='"+username+"' and a.collection_name='"+collection_name+"'and a.article_id=b.article_id");
		     // ResultSet resultSet = statement.executeQuery("insert into dummy1(a) values(2)");
			//System.out.println("select article_id from proceedings_articles_final where lower(if(article_subtitle='',article_title,concat(article_title,': ',article_subtitle))) like lower('%"+articletitles[i]+"%')");
		    readResultSet(resultSet);
		
		
		cfrecommender(articleidmysql,a1);
		cbrecommender(articleidmysql,articletitlemysql,a1);
		customrecommender(articleidconcatmysql,a1);
		//System.out.println(recid+" "+mergedrecs.length);
		
		
		List asList = Arrays.asList(mergedrecs);
		Set<String> mySet = new HashSet<String>(asList);
		HashMap<String,Integer> filteredlist = new HashMap<String,Integer>();
		HashMap<String,Integer> firstpreflist = new HashMap<String,Integer>();
		
		String secondprefarticleids="";
		int lala=0;
		
		String finalids[] = new String[200];
		int finalidslistcounter=0;
		for(String s: mySet){
		//seedset.contains(s);
			if(s != null)
			{
				//System.out.println(s + " " +Collections.frequency(asList,s)+" "+ArrayUtils.contains(articleidmysql, Integer.valueOf(s)));
				if(!ArrayUtils.contains(articleidmysql, Integer.valueOf(s)))
				{
				
				if(Collections.frequency(asList,s) > 1)
				{
					firstpreflist.put(s,Collections.frequency(asList,s));
					//System.out.println(finalidslistcounter+" "+s);
					finalids[finalidslistcounter]=s;
					finalidslistcounter++;
				}
				else
				{
					filteredlist.put(s,Collections.frequency(asList,s));
					if(lala == 0)
					{
						secondprefarticleids=s;
					}
					else
					{
						secondprefarticleids=secondprefarticleids+","+s;
					}
					lala++;
				}
				
				}
			}
		 

		}
		//System.out.println(filteredlist.size());
		//System.out.println(firstpreflist.size());
		//System.out.println(lala);
		
		int remainingtobefilled=0;
		if(finalidslistcounter >= 30)
		{
			remainingtobefilled=0;
		}
		else
		{
			remainingtobefilled= 30-firstpreflist.size();
		}
		//rank the secondprefarticles based on citation count and get the remainingarticles
		Statement statement2 = connect.createStatement();
		//System.out.println("select distinct article_id, convert(citations_count,UNSIGNED integer) citations_count from combinedarticles_basic where article_id in ("+secondprefarticleids+") order by citations_count desc limit "+String.valueOf(remainingtobefilled));
		ResultSet resultSet2 = statement2.executeQuery("select distinct article_id, convert(citations_count,UNSIGNED integer) citations_count from combinedarticles_basic where article_id in ("+secondprefarticleids+") order by citations_count desc limit "+String.valueOf(remainingtobefilled));
		while (resultSet2.next()) 
	    {
	    	  //System.out.println(resultSet2.getString("article_id")+" "+resultSet2.getString("citations_count"));
	    	  firstpreflist.put(resultSet2.getString("article_id"),1);
	    }
		
		//Output all the firsprefarticles
		Set<String> finartiset = firstpreflist.keySet();
		int finfinal=1;
		for(Object finarti: finartiset)
		{
			//System.out.println(finfinal+" "+finarti.toString());
			finfinal++;
		}
		//Job done, just need to send the important details START HERE
		String[] finartiarray = finartiset.toArray(new String[finartiset.size()]);
		mysqlretrievetitleabs(finartiarray);
		
	}
	
	private static void readResultSet(ResultSet resultSet) throws SQLException 
	{
	    // resultSet is initialised before the first data set
	    while (resultSet.next()) 
	    {
	    	  int aid = resultSet.getInt("article_id");
	    	  String atitle= resultSet.getString("article_title");
	    	  //System.out.println(aid+ " "+aid);
	    	  articleidmysql[a1]=aid;
	    	  articletitlemysql=articletitlemysql+" "+atitle;
	    	  if(a1 == 0)
	    	  {
	    		  articleidconcatmysql=String.valueOf(aid);
	    	  }
	    	  else
	    	  {
	    		  //articleidconcatmysql=articleidconcatmysql+","+articleidconcatmysql;
	    		  articleidconcatmysql=articleidconcatmysql+","+String.valueOf(aid);
	    	  }
	    
	    	  a1++;
	    	
	    }
	
	}
	
	private static void cfrecommender(int[] articleid1level,int arti_count) throws IOException, TasteException, SQLException
	{
		
		 DataModel model = new FileDataModel(new File("modified_article_refpluscit_cfmatrix.txt"));//"Articlerefmatrix_formahout_new.csv"));
			//UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity(model);
			//UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity(model,Weighting.WEIGHTED);
			ItemSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity(model,Weighting.WEIGHTED);
			//UserNeighborhood neighborhood = new ThresholdUserNeighborhood(1.0, similarity, model);
			//UserNeighborhood neighborhood = new NearestNUserNeighborhood(5, similarity, model);
			
			
			//UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
			ItemBasedRecommender recommender = new GenericItemBasedRecommender(model, similarity);
			CachingRecommender rec1 = new CachingRecommender(recommender);
					
			String[] rec_articleid = new String[10000];
			//int recid=0;
		
		for(int b=0;b<arti_count;b++)
		{
			
			
			//System.out.println(articleid1level[b]);
			
			
	    	
	    	long aid = Long.valueOf(articleid1level[b]).longValue();
	    	
	    	//Code for recommending similar users based on UBCF
	    	
	    	//long[] similarusers = recommender.mostSimilarUserIDs(aid, 20);
	    	List<RecommendedItem> recommendations = rec1.recommend(aid, 5);
			
			for (RecommendedItem recommendation: recommendations)
			{
			//userrecs[q]=exisusers[p]+","+recommendation.getItemID();
			//System.out.println(recommendation.getItemID());
			rec_articleid[recid]=String.valueOf(recommendation.getItemID());
			mergedrecs[recid]=String.valueOf(recommendation.getItemID());
			recid++;
			}
	    	  			
			
		}
		
		//System.out.println(recid);
		String[] unique = new HashSet<String>(Arrays.asList(mergedrecs)).toArray(new String[0]);
    	mergedrecs=new String[1000];
    	recid=0;
    	for(int x=0;x<unique.length;x++)
    	{
    		mergedrecs[x]=unique[x];
    		//System.out.println("cf "+mergedrecs[x]);
    		recid++;
    	}
    	//System.out.println(recid);
	
		
	
	}
	
	private static void cbrecommender(int[] articleid1level,String articletitle1level,int arti_count) throws IOException, ParseException
	{
		////////////////////
		
	    StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
		 
	    Directory index = FSDirectory.open(new File("C:/Aravind files/lucene/combinedarticles_titleabskw_test"));

	    // 2. query
	    articletitle1level=articletitle1level.replaceAll("  ", "");
	    String querystr =articletitle1level;
	    querystr=querystr.toLowerCase();

	   // System.out.println(querystr);
	   // PorterStemmer stem = new PorterStemmer();
	   // stem.setCurrent(querystr);
	    //stem.stem();
	    //querystr = stem.getCurrent();
	    
	    
	    //String special = "article_abstract:" + querystr + "article_title:" + querystr; //Add more columns if necessary
	   //String special = "article_title:" + querystr + " article_abstract:" + querystr+ " article_kw:" + querystr; //Add more columns if necessary
	   String special = "article_title:" + querystr + " article_kw:" + querystr; //Add more columns if necessary
	    
	    Query qt = new QueryParser(Version.LUCENE_40, "article_title", analyzer).parse(QueryParser.escape(querystr));
	    
	  
	    // 3. search
	    int hitsPerPage = 200;
	    IndexReader reader = DirectoryReader.open(index);
	    
	    /* For CHECKING PURPOSES
	    System.out.println("total number of docs in index"+reader.numDocs());
	    int maxDoc = reader.maxDoc();
	    int j=0;
	    for (j=0; j<maxDoc; j++)
	    {
	         Document d = reader.document(j);
	        //System.out.println(d.getField("article_id").stringValue());
	         if(d.getField("article_id").stringValue().contains("1921593"))
	         {
	        	 System.out.println("yes "+d.getField("article_id").stringValue());
	        	 
	         }
	    }
	    */
	    
	    
	    IndexSearcher searcher = new IndexSearcher(reader);
	   searcher.setSimilarity(new BM25Similarity(1000,0));
	   //searcher.setSimilarity(new BM25Similarity());
	   // searcher.setSimilarity(new DefaultSimilarity());
	    //searcher.setSimilarity(new MultiSimilarity(Similarity )); TRY THIS OUT SOMETIME
	    
	    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
	   // System.out.println(qt.toString());
	    searcher.search(qt, collector);
	    //searcher.search(queryParser.parse(querystr));
	    ScoreDoc[] hits = collector.topDocs().scoreDocs;
	    
	    
	    String[] lucarticle_id = new String[hits.length] ;
	    // 4. display results
	    
	    
	    
	    
	   // System.out.println("Found " + hits.length + " hits.");
	    for(int i=0;i<hits.length;++i) {
	      int docId = hits[i].doc;
	      Document d = searcher.doc(docId);
	     //System.out.println((i + 1) + ". " + d.get("article_id") + " "+ d.get("article_title") + " "+ d.get("article_kw") + " "+hits[i].score);
	      lucarticle_id[i]=d.get("article_id")+"@cb";;
	      mergedrecs[recid]=d.get("article_id");
	      recid++;
	    }

	    // reader can only be closed when there
	    // is no need to access the documents any more.
	    reader.close();
		
		////////////////////
	}
	
	private static void customrecommender(String articleid1levelconcat,int arti_count) throws SQLException
	{
		
        Connection connect=null;
		connect = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
		Statement statement = connect.createStatement();
		
		//System.out.println(a1);
		String query ="select distinct a.ref_cit_id refid,((ts-mints)/(maxts-mints))+((spec-minspec)/(maxspec-minspec)) normtsspec from (select distinct ref_cit_id,cast(lps_value as DECIMAL(9,5)) ts,convert(specificity,UNSIGNED integer) spec from combinedarticles_refsandcitations where article_id in ("+articleid1levelconcat+")) a, (select distinct ref_cit_id,max(cast(lps_value as DECIMAL(9,5))) maxts,min(cast(lps_value as DECIMAL(9,5))) mints,max(convert(specificity,UNSIGNED integer)) maxspec,min(convert(specificity,UNSIGNED integer)) minspec from combinedarticles_refsandcitations where article_id in ("+articleid1levelconcat+")) b order by normtsspec desc limit 200";
		//System.out.println(query);
		
		ResultSet resultSet = statement.executeQuery(query);
		while (resultSet.next()) 
	      {
		     
	  	    //int article_id=resultSet.getInt("article_id");
			//System.out.println(resultSet.getInt("refid"));
			mergedrecs[recid]=resultSet.getString("refid");
			recid++;
	      }
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
			
		//adding references_count newly ON November 16th 2015
			String query ="select article_id, replace(IF(article_subtitle = '',article_title,concat(article_title,': ',article_subtitle)),',','') article_title, replace(replace(article_abstract,'<p>',''),'</p>','') article_abstract, substring(article_pubdate,-4) article_pubdate, article_kw, article_authors,article_doi,convert(citations_count,UNSIGNED INTEGER) citations_count,collectible_title,collectible_type,convert(references_count,UNSIGNED INTEGER) references_count from combinedarticles_basic where article_id in ("+articleids+") order by citations_count desc limit 30";
			//System.out.println(query);
			
			ResultSet resultSet = statement.executeQuery(query);
		    
			int[] retarticleids = new int[100];
			
			while (resultSet.next()) 
		      {
			    
		  	    int article_id=resultSet.getInt("article_id");
		  	  if(ArrayUtils.contains(retarticleids, article_id))
		  	  {
		  		//do nothing  
		  	  }
		  	else
		  	  {
		  	  z++;
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
			  	//If the word length is above 200, do summarization, otherwise display it as such
			  		int article_abstract_length  = article_abstract.trim().split("\\s+").length;
			  	    if(article_abstract_length > 200)
			  	    {
			  		SimpleSummariser summariser = new SimpleSummariser();
			  		article_abstract = summariser.summarise(article_abstract, 2).trim().replace("\n", "");
			  	    }
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
			  	String article_referencescount = resultSet.getString("references_count");
			  	
			  	if(article_doi.length() < 1)
			  	{
			  		article_doi="https://scholar.google.com/scholar?hl=en&q="+article_title;
			  	}
			  	else
			  	{
			  		article_doi="http://dx.doi.org/"+article_doi;
			  	}
			
			  	String article_c_title = resultSet.getString("collectible_title");
			  	String article_c_type = resultSet.getString("collectible_type");
			  	
			  	
			  	
			  	//New code for counting number of shared co-references and shared co-citations for each paper
			  	Statement statement1 = connect.createStatement();
			  	String query1 ="select distinct a.article_id,if(b.article_subtitle='',b.article_title,concat(b.article_title,': ',b.article_subtitle)) article_title from rec4lrw_user_task2list a,combinedarticles_basic b where a.username ='"+username+"' and a.collection_name ='"+collection_name+"' and a.article_id=b.article_id";
				//System.out.println(query1);
				
				ResultSet resultSet1 = statement1.executeQuery(query1);
				
				int firstlevelcounter=0;
				String[] articleid_task2sl = new String[200];
				String[] articletitle_task2sl = new String[200];
				
				while (resultSet1.next()) 
			      {
					articleid_task2sl[firstlevelcounter] = resultSet1.getString("article_id");
					articletitle_task2sl[firstlevelcounter] = resultSet1.getString("article_title");
					firstlevelcounter++;
			      }
				
				//System.out.println(firstlevelcounter);
				
				int article_sharecoref_count=0;
				int article_sharecocit_count=0;
				
				for(int u=0;u<firstlevelcounter;u++)
				{
					
					Statement statement2 = connect.createStatement();
				  	String query2 ="select distinct ref_cit_id,ref_cit_title from combinedarticles_refsandcitations where article_id in ('"+article_id+"','"+articleid_task2sl[u]+"') and ntype='r' group by ref_cit_id having count(ref_cit_id) > 1 order by count(ref_cit_id) desc";
					//System.out.println(query2);
					
				  	ResultSet resultSet2 = statement2.executeQuery(query2);
				  	
				  	while (resultSet2.next()) 
				      {
				  		article_sharecoref_count++;
				      }
				  	
				  	Statement statement3 = connect.createStatement();
				  	String query3 ="select distinct ref_cit_id from combinedarticles_refsandcitations where article_id in ('"+article_id+"','"+articleid_task2sl[u]+"') and ntype='c' group by ref_cit_id having count(ref_cit_id) > 1 order by count(ref_cit_id) desc";
					//System.out.println(query3);
					
				  	ResultSet resultSet3 = statement3.executeQuery(query3);
				  	
				  	while (resultSet3.next()) 
				      {
				  		article_sharecocit_count++;
				      }
				  	
				  				  	
				}	
					
			      
			  	
			  	System.out.println(lala+"|cf|"+article_id+"|"+article_title+"|"+article_abstract+"|"+article_authors+"|"+article_pubdate+"|"+article_kw+"|"+article_doi+"|"+article_citationscount+"|"+article_sharecoref_count+"|"+article_sharecocit_count+"|"+article_c_title+"|"+article_c_type+"|"+article_referencescount);
			  	
		  	  }
		  	  }
			
			
		    
		
		
		
	}
	
	
	
}
