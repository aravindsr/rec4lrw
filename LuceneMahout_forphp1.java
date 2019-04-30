import net.sf.classifier4J.summariser.SimpleSummariser;

import org.apache.commons.lang3.ArrayUtils;
import org.apache.lucene.analysis.en.EnglishAnalyzer;
import org.apache.lucene.analysis.standard.StandardAnalyzer;
import org.apache.lucene.analysis.standard.std31.*;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.StringField;
import org.apache.lucene.document.TextField;
import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexWriterConfig;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.classic.MultiFieldQueryParser;
import org.apache.lucene.queryparser.classic.ParseException;
import org.apache.lucene.queryparser.classic.QueryParser;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.FuzzyQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.MultiPhraseQuery;
import org.apache.lucene.search.PhraseQuery;
import org.apache.lucene.search.PrefixQuery;
import org.apache.lucene.search.Query;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TopScoreDocCollector;
import org.apache.lucene.search.WildcardQuery;
import org.apache.lucene.search.BooleanClause.Occur;
import org.apache.lucene.search.similarities.BM25Similarity;
import org.apache.lucene.search.similarities.DFRSimilarity;
import org.apache.lucene.search.similarities.DefaultSimilarity;
import org.apache.lucene.search.similarities.LMDirichletSimilarity;
import org.apache.lucene.search.similarities.MultiSimilarity;
import org.apache.lucene.search.similarities.TFIDFSimilarity;
import org.apache.lucene.store.Directory;
import org.apache.lucene.store.FSDirectory;
import org.apache.lucene.store.RAMDirectory;
import org.apache.lucene.util.ArrayUtil;
import org.apache.lucene.util.Version;
import org.apache.mahout.cf.taste.common.TasteException;
import org.apache.mahout.cf.taste.impl.model.file.FileDataModel;
import org.apache.mahout.cf.taste.impl.neighborhood.NearestNUserNeighborhood;
import org.apache.mahout.cf.taste.impl.neighborhood.ThresholdUserNeighborhood;
import org.apache.mahout.cf.taste.impl.recommender.CachingRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericItemBasedRecommender;
import org.apache.mahout.cf.taste.impl.recommender.GenericUserBasedRecommender;
import org.apache.mahout.cf.taste.impl.similarity.CachingItemSimilarity;
import org.apache.mahout.cf.taste.model.DataModel;
import org.apache.mahout.cf.taste.neighborhood.UserNeighborhood;
import org.apache.mahout.cf.taste.recommender.ItemBasedRecommender;
import org.apache.mahout.cf.taste.recommender.RecommendedItem;
import org.apache.mahout.cf.taste.recommender.UserBasedRecommender;
import org.apache.mahout.cf.taste.similarity.ItemSimilarity;
import org.apache.mahout.cf.taste.similarity.UserSimilarity;

import org.tartarus.snowball.ext.PorterStemmer;

import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class LuceneMahout_forphp1 {
  public static void main(String[] args) throws IOException, ParseException, TasteException, SQLException{
    // 0. Specify the analyzer for tokenizing text.
    //    The same analyzer should be used for indexing and searching
    StandardAnalyzer analyzer = new StandardAnalyzer(Version.LUCENE_40);
	 // EnglishAnalyzer analyzer = new EnglishAnalyzer(Version.LUCENE_40);
    // 1. create the index
    
    //Directory index = FSDirectory.open(new File("C:/Aravind files/lucene/proc_pre_abs_ft"));
    //Directory index = FSDirectory.open(new File("C:/Aravind files/lucene/combinedarticles_titleabskw_test"));
    Directory index = FSDirectory.open(new File("C:/Aravind files/lucene/combinedarticles_titleabskw_test_new"));

    // 2. query
    String querystr = args[0];
    String[] queryterms = querystr.split("\\s+");
    //System.out.println(queryterms.length);

   // System.out.println(querystr);
   // PorterStemmer stem = new PorterStemmer();
   // stem.setCurrent(querystr);
    //stem.stem();
    //querystr = stem.getCurrent();
    
    
    //String special = "article_abstract:" + querystr + "article_title:" + querystr; //Add more columns if necessary
   String special = "article_title:" + querystr + " article_abstract:" + querystr; //Add more columns if necessary
    //String special = "article_title:algorithm"; //Add more columns if necessary
    
   
   
   
    // the "title" arg specifies the default field to use
    // when no field is explicitly specified in the query.
    //Query q1 = new QueryParser(Version.LUCENE_40, "article_title", analyzer).parse(querystr);
   Query q1 = new QueryParser(Version.LUCENE_40, "article_combtext", analyzer).parse(querystr);
    Query q2 = new QueryParser(Version.LUCENE_40, "article_abstract", analyzer).parse(querystr);
    Query q3 = new QueryParser(Version.LUCENE_40, "article_kw", analyzer).parse(querystr);
    //Query q3 = new QueryParser(Version.LUCENE_40, "article_kw", analyzer).parse(querystr);
   // System.out.println(q.toString());
    
    //Query prefixquery = new PrefixQuery(new Term("article_kw",querystr));
    Query wcquery1 = new WildcardQuery(new Term("article_title","*"+querystr+"*"));
    Query wcquery2 = new WildcardQuery(new Term("article_abstract","*"+querystr+"*"));
    Query wcquery3 = new WildcardQuery(new Term("article_kw","*"+querystr+"*"));
    
    //PhraseQuery query = new PhraseQuery();
    MultiPhraseQuery query = new MultiPhraseQuery();
    String[] words = querystr.split(" ");
    for (String word : words) {
    	word=word.toLowerCase();
        query.add(new Term("article_title", word));
        
    }
    //System.out.println(query.toString());
   
    //PhraseQuery query1 = new PhraseQuery();
    MultiPhraseQuery query1 = new MultiPhraseQuery();
    String[] words1 = querystr.split(" ");
    for (String word1 : words1) {
    	word1=word1.toLowerCase();
        query1.add(new Term("article_abstract", word1));
        
    }
    
    MultiPhraseQuery query2 = new MultiPhraseQuery();
    String[] words2 = querystr.split(" ");
    for (String word2 : words2) {
    	word2=word2.toLowerCase();
        query2.add(new Term("article_kw", word2));
        
    }
    
   // Term term1 = new Term("article_title", querystr);
    //Term term2 = new Term("article_abstract", querystr);
    //Query fquery1 = new FuzzyQuery(term1);
   // Query fquery2 = new FuzzyQuery(term2);
    
    //Query mfquery = MultiFieldQueryParser.parse(Version.LUCENE_40,querystr,new String[]{"article_title", "article_abstract"},Occur.SHOULD, new StandardAnalyzer(null));
    
  BooleanQuery b = new BooleanQuery();
  
  /*Old settings
  b.add(wcquery3, Occur.SHOULD);
  //b.add(wcquery2, Occur.SHOULD);
  b.add(q3, Occur.SHOULD);
  b.add(query, Occur.MUST);
  end of Old setting*/
 
  //the below three queries were used for offline evaluation
  b.add(q1, Occur.MUST);
  //b.add(wcquery3, Occur.SHOULD);
  //b.add(wcquery2, Occur.SHOULD);
  
  
  // System.out.println(b.toString());
    
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
    searcher.setSimilarity(new BM25Similarity());
   // searcher.setSimilarity(new DefaultSimilarity());
    //searcher.setSimilarity(new MultiSimilarity(Similarity )); TRY THIS OUT SOMETIME
    
    TopScoreDocCollector collector = TopScoreDocCollector.create(hitsPerPage, true);
    searcher.search(b, collector);
    //searcher.search(queryParser.parse(querystr));
    ScoreDoc[] hits = collector.topDocs().scoreDocs;
    
    
    String[] lucarticle_id = new String[hits.length] ;
    // 4. display results
    
    
    String[] rec_articleid = new String[1000];
	int recid=0;
    
   // System.out.println("Found " + hits.length + " hits.");
    for(int i=0;i<hits.length;++i) {
      int docId = hits[i].doc;
      Document d = searcher.doc(docId);
      
   //System.out.println((i + 1) + ". " + d.get("article_id") + " "+ d.get("article_title") + " "+hits[i].score);
      
      
      lucarticle_id[i]=d.get("article_id")+"@cb";
    }

    // reader can only be closed when there
    // is no need to access the documents any more.
    reader.close();
    
    
    //System.out.println("The size of the article_id arrary is "+lucarticle_id.length);
    
    // Start the commenting here****
    /*
    //Mahout Part
    
    DataModel model = new FileDataModel(new File("article_refpluscit_cfmatrix.txt"));	
	//UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.PearsonCorrelationSimilarity(model);
	//UserSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity(model);
	//UserNeighborhood neighborhood = new ThresholdUserNeighborhood(0.01, similarity, model);
	
	//UserNeighborhood neighborhood = new NearestNUserNeighborhood(5, similarity, model);
	//UserBasedRecommender recommender = new GenericUserBasedRecommender(model, neighborhood, similarity);
    
	ItemSimilarity similarity = new org.apache.mahout.cf.taste.impl.similarity.UncenteredCosineSimilarity(model);
	//CachingItemSimilarity similarity = new CachingItemSimilarity(similarity1, model);
	ItemBasedRecommender recommender = new GenericItemBasedRecommender(model, similarity);
	CachingRecommender rec1 = new CachingRecommender(recommender);
	
	
	
    for (int p=0;p<lucarticle_id.length;p++)
    {
    	//System.out.println(lucarticle_id[p]);
    	String artar1[] =lucarticle_id[p].split("@");
    	
    	long aid = Long.valueOf(artar1[0]).longValue();
    	
    	//Code for recommending similar users based on UBCF
    	
    	//long[] similarusers = recommender.mostSimilarUserIDs(aid, 5);
    	//System.out.println(similarusers.length);
    	//for(int m=0;m < similarusers.length;m++)
    	//{
    		//System.out.println("For article "+aid+", similar article: "+similarusers[m]);
    		//rec_articleid[recid]=String.valueOf(similarusers[m])+"@cf";
    		//recid++;
    	//}
    	List<RecommendedItem> recommendations = rec1.recommend(aid, 2);
		
		for (RecommendedItem recommendation: recommendations)
		{
		//userrecs[q]=exisusers[p]+","+recommendation.getItemID();
		//System.out.println(recommendation.getItemID());
		rec_articleid[recid]=String.valueOf(recommendation.getItemID())+"@cf";
		recid++;
		}
    	
    	
    }   	 
    	//List<RecommendedItem> recommendations = recommender.recommend(aid, 5);
    	//for (RecommendedItem recommendation: recommendations)
		//{
		//	System.out.println("UBCF msi for "+aid+": "+recommendation.getItemID());
			
	//	}
    	
			
    //}
  //End the commenting here
    */
  
    
    //System.out.println("Total number of recommended articles are: "+recid);
	for(int v=0;v<recid;v++)
	{
		String artar2[] =rec_articleid[v].split("@");
		//System.out.println("Recommended article_id: "+artar2[0]);
	}
    
	String[] mergedarticles = ArrayUtils.addAll(lucarticle_id, rec_articleid);
	
		
	ArrayList<String> list = new ArrayList<String>();
	for (String s : mergedarticles)
	    if (s != null)
	        list.add(s);
	
	mergedarticles = list.toArray(new String[list.size()]);
	
	//System.out.println(mergedarticles.length);
	for(int w=0;w<mergedarticles.length;w++)
	{
		String artar3[] =mergedarticles[w].split("@");
		//System.out.println("article_id: "+w+" "+artar3[0]);
	}
	
	//function for retrieving mysql details
	
	retrievearticledetails(mergedarticles);
	
	
    
  }
  
  public static void retrievearticledetails(String[] mergedarticles) throws SQLException
  {
	  	Connection connect1 = null;
		connect1 = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
	    Statement statement = connect1.createStatement();
	    
	    int z=0;
	    
	    String articleids="";
	    Map<String, String> map = new HashMap<String, String>();
	    
	  for (int y=0;y<mergedarticles.length;y++)
	  {
		  String artar4[] =mergedarticles[y].split("@");
		  map.put(artar4[0].trim(), artar4[1].trim());
		  //System.out.println(artar4[0].trim()+ " "+artar4[1].trim());
		  if(y ==0)
		  {
			  articleids=artar4[0];
		  }
		  else
		  {
		  articleids=articleids+","+artar4[0];
		  }
	  }
	  
	 // System.out.println(map.size());
		
	    //String query ="select a.article_id article_id,replace(IF(a.article_subtitle = '',a.article_title,concat(a.article_title,': ',a.article_subtitle)),',','') article_title, b.article_abstract article_abstract "+
        //"from proceedings_articles_final a,"+
        //"(select article_id, article_abstract from proceedings_articles where article_id="+artar4[0]+
        //" union select article_id, article_abstract from proceedings_articles_temp where article_id="+artar4[0]+") b where a.article_id = b.article_id;";
	    
		  //new query
	  	//System.out.println(articleids);
		  //String query ="select article_id, replace(IF(article_subtitle = '',article_title,concat(article_title,': ',article_subtitle)),',','') article_title, replace(replace(article_abstract,'<p>',''),'</p>','') article_abstract, article_pubdate, article_kw, article_authors,article_doi,convert(citations_count,UNSIGNED INTEGER) citations_count from combinedarticles_basic where article_id in ("+articleids+") order by citations_count desc limit 20";
//	       String query ="select a.article_id,a.cit_ct,a.ref_ct,a.cov_ct,(((cit_ct-mincit_ct)/(maxcit_ct-mincit_ct))*0.25) + (((ref_ct-minref_ct)/(maxref_ct-minref_ct))*0.25) + (((cov_ct-mincov_ct)/(maxcov_ct-mincov_ct))*0.5) composite_rank, a.article_title, a.article_abstract, a.article_pubdate, a.article_kw, a.article_authors,a.article_doi,convert(a.citations_count,UNSIGNED INTEGER) citations_count from (select article_id,convert(citations_count,UNSIGNED INT) cit_ct,convert(references_count,UNSIGNED INT) ref_ct,convert(coverage,UNSIGNED INT) cov_ct, replace(IF(article_subtitle = '',article_title,concat(article_title,': ',article_subtitle)),',','') article_title, replace(replace(article_abstract,'<p>',''),'</p>','') article_abstract, right(article_pubdate,4) article_pubdate, article_kw, article_authors,article_doi,convert(citations_count,UNSIGNED INTEGER) citations_count from combinedarticles_basic where article_id in ("+articleids+")) a,(select max(convert(citations_count,UNSIGNED integer)) maxcit_ct,min(convert(citations_count, UNSIGNED INTEGER)) mincit_ct,max(convert(references_count,UNSIGNED integer)) maxref_ct,min(convert(references_count, UNSIGNED INTEGER)) minref_ct,max(convert(coverage,UNSIGNED integer)) maxcov_ct,min(convert(coverage, UNSIGNED INTEGER)) mincov_ct from combinedarticles_basic where article_id in ("+articleids+")) b order by composite_rank desc limit 20";
	        //new query for HITS Values TO BE ADDED
		    String query ="select a.article_id,a.cit_ct,a.ref_ct,a.cov_ct,(((cit_ct-mincit_ct)/(maxcit_ct-mincit_ct))*0.25) + (((ref_ct-minref_ct)/(maxref_ct-minref_ct))*0.25) + (((cov_ct-mincov_ct)/(maxcov_ct-mincov_ct))*0.5) composite_rank, a.article_title, a.article_abstract, a.article_pubdate, a.article_kw, a.article_authors,a.article_doi,convert(a.citations_count,UNSIGNED INTEGER) citations_count,a.collectible_title collectible_title,a.collectible_type collectible_type from (select article_id,convert(citations_count,UNSIGNED INT) cit_ct,convert(references_count,UNSIGNED INT) ref_ct,convert(coverage,UNSIGNED INT) cov_ct, article_title, article_abstract, article_pubdate, article_kw, article_authors,article_doi,convert(citations_count,UNSIGNED INTEGER) citations_count, collectible_title,collectible_type from (select distinct a1.article_id article_id,citations_count cit_ct, citations_count, references_count,coverage*b1.hubvaluenorm coverage, replace(IF(article_subtitle = '',article_title,concat(article_title,': ',article_subtitle)),',','') article_title, replace(replace(article_abstract,'<p>',''),'</p>','') article_abstract, right(article_pubdate,4) article_pubdate, article_kw, article_authors,article_doi,collectible_title,collectible_type from  combinedarticles_basic a1, temp_purp b1 where a1.article_id=b1.article_id and a1.article_id in ("+articleids+")) d) a, (select max(convert(citations_count,UNSIGNED integer)) maxcit_ct,min(convert(citations_count, UNSIGNED INTEGER)) mincit_ct,max(convert(references_count,UNSIGNED integer)) maxref_ct,min(convert(references_count, UNSIGNED INTEGER)) minref_ct,max(convert(coverage,UNSIGNED integer)) maxcov_ct,min(convert(coverage, UNSIGNED INTEGER)) mincov_ct from (select distinct a1.article_id article_id,citations_count cit_ct, citations_count, references_count,coverage*b1.hubvaluenorm coverage from  combinedarticles_basic a1, temp_purp b1 where a1.article_id=b1.article_id and a1.article_id in ("+articleids+")) e) b  order by composite_rank desc limit 20";
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
	  	  retarticleids[z] = article_id;
	  	 	String article_title = resultSet.getString("article_title");
		  	String article_abstract = resultSet.getString("article_abstract");
		  	article_abstract= article_abstract.replace("<p>","");
		  	article_abstract= article_abstract.replace("</p>","");
		  	article_abstract=article_abstract.trim();
		  	//System.out.println(article_id);
		  	
		  	if(article_abstract.length() < 1)
		  	{
		  		article_abstract="No abstract data";
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
		  	String article_refscount = resultSet.getString("ref_ct");
		  	String article_rank = resultSet.getString("composite_rank");
		  	
		  	if(article_doi.length() < 1)
		  	{
		  		article_doi="https://scholar.google.com/scholar?hl=en&q="+article_title;
		  	}
		  	else
		  	{
		  		article_doi="http://dx.doi.org/"+article_doi;
		  	}
		  	
		  	
		  	String rectype=(String) map.get(String.valueOf(article_id));
		  	
		  	
		  	String article_c_title = resultSet.getString("collectible_title");
		  	String article_c_type = resultSet.getString("collectible_type");
		  	
		  	System.out.println(z+"|"+rectype+"|"+article_id+"|"+article_title+"|"+article_abstract+"|"+article_authors+"|"+article_pubdate+"|"+article_kw+"|"+article_doi+"|"+article_citationscount+"|"+article_refscount+"|"+article_rank+"|"+article_c_title+"|"+article_c_type);
		  	//System.out.println(article_kw);
		  	
	  	  }
	      }
	      
	  
	  
	  //System.out.println("Total rows retrieved: "+z);
	      
	     
	  
  }

  
}
    		