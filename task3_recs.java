import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.BufferedWriter;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;
import org.apache.commons.lang3.ArrayUtils;

import edu.uci.ics.jung.algorithms.cluster.EdgeBetweennessClusterer;
import edu.uci.ics.jung.algorithms.filters.Filter;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.algorithms.filters.KNeighborhoodFilter;
import edu.uci.ics.jung.algorithms.layout.CircleLayout;
import edu.uci.ics.jung.algorithms.layout.ISOMLayout;
import edu.uci.ics.jung.algorithms.layout.Layout;
import edu.uci.ics.jung.algorithms.scoring.BetweennessCentrality;
import edu.uci.ics.jung.algorithms.scoring.DegreeScorer;
import edu.uci.ics.jung.algorithms.scoring.HITS;
import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.shortestpath.DijkstraShortestPath;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.DirectedSparseMultigraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.Hypergraph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;
import edu.uci.ics.jung.samples.SimpleGraphDraw;
import edu.uci.ics.jung.visualization.BasicVisualizationServer;
import edu.uci.ics.jung.visualization.VisualizationViewer;
import edu.uci.ics.jung.visualization.control.DefaultModalGraphMouse;
import edu.uci.ics.jung.visualization.decorators.ToStringLabeller;
import edu.uci.ics.jung.visualization.renderers.Renderer.Edge;
import edu.uci.ics.jung.visualization.renderers.Renderer.Vertex;
import net.sf.classifier4J.summariser.SimpleSummariser;


public class task3_recs {
	
	 static int edgeCount;
	 static String username,collection_name,at;
	 static int arti_count;
	

	public static void main (String[] args) throws IOException, SQLException
	{
		
		Connection connect=null;
		
		connect = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
			
	      Statement statement = connect.createStatement();
	    
	      String ipvalue = args[0];
	      String[] ipvalues=ipvalue.split("\\|");
	      username=ipvalues[0];
	      collection_name=ipvalues[1];
	      at = ipvalues[2];
	      
	      int  at_reccount=0;
	      
	      //retrieve threshold values for article type
	    ResultSet resultSet_at = statement.executeQuery("select rec_count from rec4lrw_articletype_thresholds where at_abbr='"+at+"'");
	    while (resultSet_at.next()) 
	    {
	    	  at_reccount = resultSet_at.getInt("rec_count");
	    }
	   //System.out.println(at_reccount);
	   
	    //retrieve total number of articles in the user's reading list
	    ResultSet resultSet_arti_ct = statement.executeQuery("select count(distinct article_id) arti_ct from rec4lrw_user_readinglist where username ='"+username+"' and collection_name='"+collection_name+"'");
	    while (resultSet_arti_ct.next()) 
	    {
	    	  arti_count = resultSet_arti_ct.getInt("arti_ct");
	    }
	   //System.out.println(arti_count);
	   
	   
	   
		ResultSet resultSet = statement.executeQuery("select distinct article_id,ref_cit_id,ntype from combinedarticles_refsandcitations where article_id in (select article_id from rec4lrw_user_readinglist where collection_name='"+collection_name+"' and username ='"+username+"')  limit 10000");
		String q1a = "select distinct article_id,ref_cit_id,ntype from combinedarticles_refsandcitations where article_id in (select article_id from rec4lrw_user_readinglist where collection_name='"+collection_name+"' and username ='"+username+"')  limit 10000";
		//System.out.println(q1a);
		Integer[] storevertices= new Integer[14280];
		int i=0;
		
		
		//Graph<Integer,String> g= new SparseGraph<Integer, String>();
		DirectedSparseGraph<Integer, String> g = new DirectedSparseGraph<Integer, String>();
		
		 while (resultSet.next()) 
		    {
		    	  int aid = resultSet.getInt("article_id");
		    	  int refid =resultSet.getInt("ref_cit_id");
		    	  String ntype = resultSet.getString("ntype");
		    	  storevertices[i]=aid;
				   g.addVertex(aid);
				   if(ntype.trim().contains("r"))
					{
					g.addEdge(aid+"-"+refid, aid, refid,EdgeType.DIRECTED);
					}
					else
					{
						g.addEdge(refid+"-"+aid, refid, aid,EdgeType.DIRECTED);
					}
					i++;
					storevertices[i]=refid;
					g.addVertex(refid);
					g.addEdge(aid+"-"+refid, aid, refid,EdgeType.DIRECTED);
					i++;
		    	
		    }
		
		
		//System.out.println(i);
		//New code comes here
		
        //System.out.println(g.getVertexCount());
		
		EdgeBetweennessClusterer<Integer, String> ec1 = new EdgeBetweennessClusterer<Integer, String>(1);
		Set<Set<Integer>> sets = ec1.transform(g);
        Iterator<Set<Integer>> keys = sets.iterator();
        
        int clustcount=0;
        
        while(keys.hasNext())
        {
        	
        	//System.out.println(keys.next());
        	keys.next();//dont have both statements together
        	clustcount++;
        
        }
		
        int caseflag=9;
        int clustrecdiff=0;
        
       //System.out.println("Total number of clusters is "+clustcount);
        if(clustcount > at_reccount)
        {
        	clustrecdiff = clustcount - at_reccount;
        	caseflag = 1;
        }
        else
        {
        	clustrecdiff = at_reccount - clustcount;
        	caseflag = 0;
        }
        
        
        
        keys = sets.iterator();
        
        int[][] clusterarticles = new int[clustcount][10000];
        
        int clustindex=0;
        int[] clustersize = new int[clustcount];
        
        
        while(keys.hasNext())
        {
        	//System.out.println(keys.next());
        	Set<Integer> currentset = keys.next();
        	//int[] testarray = currentset.toArray(new int[currentset.size()]);
        	//testarray.toString();
        	//int[] arr = new int[currentset.size()];
        	int index = 0;

        	for( Integer y : currentset ) {
        	  clusterarticles[clustindex][index++] = y; //note the autounboxing here
        	}
        	
        	//System.out.println(index);
        	clustersize[clustindex]=index;
        	clustindex++;
        }
        
       //System.out.println(clustindex);
        for(int u=0;u<clustindex;u++)
        {
        	int[] currentarray = clusterarticles[u];
        	for(int v=0;v<clustersize[u];v++)
        	{
        		//System.out.print(currentarray[v]+", ");
        	}
        	//System.out.println();
        }
        
        //hardcoding for testing
        //caseflag = 1;
        //at_reccount=5;
        
        String finalarticleslist ="";
        String[] finalarticles = new String[at_reccount];
        
        int finalarticles_counter=0;
        
        
        
        //the rare case
        if(caseflag == 1)
        {
        	//System.out.println("It came here caseflag 1");
        	//Here the number of clusters is more than the number of recommendations. Scenario is possible for article-types such as posters
        	for(int e=0;e<clustindex;e++)
        	{
        		 if((finalarticles_counter+1) > at_reccount)
        		 {
        			 break;
        		 }
        		 int[] clusartilces = clusterarticles[e];
        		 String queryarticles =Arrays.toString(clusartilces);
        		 queryarticles = queryarticles.replace("[", "");
        		 queryarticles = queryarticles.replace("]", "");
        		// System.out.println("lala "+queryarticles);
        		 String retrievedtoparticle=retrievetoparticle(queryarticles,0);
        		 finalarticles[finalarticles_counter]=retrievedtoparticle;
        		//System.out.println("Top article in cluster "+e+": "+retrievedtoparticle);
        		 finalarticles_counter++;
        	}
        	
        }
        else if(caseflag == 0)
        {
        	//System.out.println("It came here caseflag 0");
        	int limitsql=0;
        	//Here the number of recommendations is more than the number of clusters. Scenario is possible for most article-types
        	//the logic is to have equal number of top articles from each cluster
        	
        	
        	outerloop:
        	while (finalarticles_counter < at_reccount)
        	{
        		
        		for(int e=0;e<clustindex;e++)
            	{
            		 if((finalarticles_counter+1) > at_reccount)
            		 {
            			 break;
            		 }
            		 int[] clusartilces = clusterarticles[e];
            		 String queryarticles =Arrays.toString(clusartilces);
            		 queryarticles = queryarticles.replace("[", "");
            		 queryarticles = queryarticles.replace("]", "");
            		 queryarticles = queryarticles.replaceAll(", 0", "");
            		//System.out.println("lala "+limitsql+" "+queryarticles);
            		 String retrievedtoparticle=retrievetoparticle(queryarticles,limitsql);
            		// System.out.println(retrievedtoparticle);
            		 if(retrievedtoparticle.length() > 2)
            		 { 
            		 finalarticles[finalarticles_counter]=retrievedtoparticle;
            		//System.out.println("Top article in cluster "+e+": "+retrievedtoparticle);
            		 finalarticles_counter++;
            		 }
            		 
            		 if(finalarticles_counter == arti_count)
            		 {
            			 break outerloop;
            		 }
            		 
            		 /*
            		 else if(( retrievedtoparticle.length() == 0))
            		 {
            			 System.out.println("Break happened");
            			 break outerloop;
            			 
            		 }
            		 */
            		 if((clustindex-e) == 1)
            		 {
            			 e=-1;
            			 limitsql++;
            		 }
            	}
        		
        		
        	}
        	
        }
        
        finalarticleslist = Arrays.toString(finalarticles);
        finalarticleslist = finalarticleslist.replace("[", "");
        finalarticleslist = finalarticleslist.replace("]", "");
		//System.out.println(finalarticleslist);
		//final step
        mysqlretrievetitleabs(finalarticleslist,clusterarticles,clustindex);
		
	}
	
	private static void mysqlretrievetitleabs(String uniids,int[][] clusterarticles,int clustindex) throws SQLException
	{
		
        Connection connect=null;
		
		connect = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
		Statement statement = connect.createStatement();
		 
		
		int z=0;
		
		
			//old query
			//String query ="select a.article_id article_id,replace(IF(a.article_subtitle = '',a.article_title,concat(a.article_title,': ',a.article_subtitle)),',','') article_title, b.article_abstract article_abstract "+
			  //      "from proceedings_articles_final a,"+
			    //    "(select article_id, article_abstract from proceedings_articles where article_id="+uniids[i]+
			      //  " union select article_id, article_abstract from proceedings_articles_temp where article_id="+uniids[i]+") b where a.article_id = b.article_id;";
			
			
		//String query ="select article_id, replace(IF(article_subtitle = '',article_title,concat(article_title,': ',article_subtitle)),',','') article_title, replace(replace(article_abstract,'<p>',''),'</p>','') article_abstract, article_pubdate, article_kw, article_authors,article_doi,convert(citations_count,UNSIGNED INTEGER) citations_count from combinedarticles_basic where article_id in ("+uniids+") order by citations_count desc limit 20";
		String query ="select distinct a.article_id,a.cit_ct,a.ref_ct,a.cov_ct,(((cit_ct-mincit_ct)/(maxcit_ct-mincit_ct))*0.25) + (((ref_ct-minref_ct)/(maxref_ct-minref_ct))*0.25) + (((cov_ct-mincov_ct)/(maxcov_ct-mincov_ct))*0.5) composite_rank, a.article_title, a.article_abstract, a.article_pubdate, a.article_kw, a.article_authors,a.article_doi,convert(a.citations_count,UNSIGNED INTEGER) citations_count,a.collectible_title collectible_title,a.collectible_type collectible_type,convert(a.ref_ct,UNSIGNED INTEGER) references_count from (select distinct article_id,convert(citations_count,UNSIGNED INT) cit_ct,convert(references_count,UNSIGNED INT) ref_ct,convert(coverage,UNSIGNED INT) cov_ct, replace(IF(article_subtitle = '',article_title,concat(article_title,': ',article_subtitle)),',','') article_title, replace(replace(article_abstract,'<p>',''),'</p>','') article_abstract, article_pubdate, article_kw, article_authors,article_doi,convert(citations_count,UNSIGNED INTEGER) citations_count, collectible_title,collectible_type from combinedarticles_basic where article_id in ("+uniids+")) a,(select distinct max(convert(citations_count,UNSIGNED integer)) maxcit_ct,min(convert(citations_count, UNSIGNED INTEGER)) mincit_ct,max(convert(references_count,UNSIGNED integer)) maxref_ct,min(convert(references_count, UNSIGNED INTEGER)) minref_ct,max(convert(coverage,UNSIGNED integer)) maxcov_ct,min(convert(coverage, UNSIGNED INTEGER)) mincov_ct from combinedarticles_basic where article_id in ("+uniids+")) b order by composite_rank desc";
		//From task 1 - String query ="select a.article_id,a.cit_ct,a.ref_ct,a.cov_ct,(((cit_ct-mincit_ct)/(maxcit_ct-mincit_ct))*0.25) + (((ref_ct-minref_ct)/(maxref_ct-minref_ct))*0.25) + (((cov_ct-mincov_ct)/(maxcov_ct-mincov_ct))*0.5) composite_rank, a.article_title, a.article_abstract, a.article_pubdate, a.article_kw, a.article_authors,a.article_doi,convert(a.citations_count,UNSIGNED INTEGER) citations_count,a.collectible_title collectible_title,a.collectible_type collectible_type from (select article_id,convert(citations_count,UNSIGNED INT) cit_ct,convert(references_count,UNSIGNED INT) ref_ct,convert(coverage,UNSIGNED INT) cov_ct, article_title, article_abstract, article_pubdate, article_kw, article_authors,article_doi,convert(citations_count,UNSIGNED INTEGER) citations_count, collectible_title,collectible_type from (select distinct a1.article_id article_id,citations_count cit_ct, citations_count, references_count,coverage*b1.hubvaluenorm coverage, replace(IF(article_subtitle = '',article_title,concat(article_title,': ',article_subtitle)),',','') article_title, replace(replace(article_abstract,'<p>',''),'</p>','') article_abstract, right(article_pubdate,4) article_pubdate, article_kw, article_authors,article_doi,collectible_title,collectible_type from  combinedarticles_basic a1, temp_purp b1 where a1.article_id=b1.article_id and a1.article_id in ("+articleids+")) d) a, (select max(convert(citations_count,UNSIGNED integer)) maxcit_ct,min(convert(citations_count, UNSIGNED INTEGER)) mincit_ct,max(convert(references_count,UNSIGNED integer)) maxref_ct,min(convert(references_count, UNSIGNED INTEGER)) minref_ct,max(convert(coverage,UNSIGNED integer)) maxcov_ct,min(convert(coverage, UNSIGNED INTEGER)) mincov_ct from (select distinct a1.article_id article_id,citations_count cit_ct, citations_count, references_count,coverage*b1.hubvaluenorm coverage from  combinedarticles_basic a1, temp_purp b1 where a1.article_id=b1.article_id and a1.article_id in ("+articleids+")) e) b  order by composite_rank desc limit 20";
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
			  	
			  	//Send the cluster entries information along with the data. Easiest way but data heavy!
			  	//Code for finding the cluster
			  	String article_cluster_entries ="";
			  	String article_cluster_entries_final = "";
			  	for(int h=0;h<clustindex;h++)
			  	{
			  		int[] curcl = clusterarticles[h];
			  		if(ArrayUtils.contains(curcl, Integer.valueOf(article_id)))
			  		{
			  			article_cluster_entries = Arrays.toString(curcl);
			  			article_cluster_entries = article_cluster_entries.replace("[", "");
			  			article_cluster_entries = article_cluster_entries.replace("]", "");
			  			article_cluster_entries = article_cluster_entries.replaceAll(", 0", "");
			  			//if( string.substring(string.length() - 1)) =="" )
			  			
			  			Statement statement1 = connect.createStatement();
			  			//String query1 ="select group_concat(distinct article_id) articleids from combinedarticles_basic where article_id in ("+article_cluster_entries+") and article_id in (select article_id from rec4lrw_user_readinglist where collection_name='"+collection_name+"' and username ='"+username+"')";
			  			String query1 ="select group_concat(article_id) articleids from (select distinct article_id from combinedarticles_basic where article_id in ("+article_cluster_entries+") order by cast(citations_count as unsigned) desc limit 100) a";
			  			//System.out.println(query1);
			  			ResultSet resultSet1 = statement1.executeQuery(query1);
			  			while (resultSet1.next()) 
			  			{
			  				article_cluster_entries_final=resultSet1.getString("articleids");
			  			}
			  		}
			  	}
			  	
			  	
			  	System.out.println(lala+"|cf|"+article_id+"|"+article_title+"|"+article_abstract+"|"+article_authors+"|"+article_pubdate+"|"+article_kw+"|"+article_doi+"|"+article_citationscount+"|"+article_cluster_entries_final+"|"+article_c_title+"|"+article_c_type+"|"+article_referencescount);
		  	  }
		      }
			
			
		    
		
		
		
	}
	
	//newly added
	private static String retrievetoparticle(String currentclustarticleslist,int rowcount) throws SQLException
	{
		String toparticle="";
        Connection connect=null;
		
		connect = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
		Statement statement = connect.createStatement();
		 
		
		int z=0;
		
		String query ="select distinct article_id,concat(article_title,' ',article_subtitle) at,citations_count,references_count,article_pubdate,coverage from combinedarticles_basic where article_id in ("+currentclustarticleslist+") and article_id in (select article_id from rec4lrw_user_readinglist where collection_name='"+collection_name+"' and username ='"+username+"') order by cast(citations_count as unsigned) desc limit "+String.valueOf(rowcount)+",1";
			//System.out.println(query);
			
			ResultSet resultSet = statement.executeQuery(query);
		    
			while (resultSet.next()) 
		      {
			    z++;  
		  	    toparticle=resultSet.getString("article_id");
		  	 	
		      }
			
			
		    
		
	connect.close();
	return toparticle;	
	}
	
	
	public static double getMaxValue(Double[] prvalue){  
	      double maxValue = prvalue[0]; 
	      double id=0;
	      for(int i=1;i < prvalue.length;i++){  
	      if(prvalue[i] > maxValue){  
	      maxValue = prvalue[i];  
	      id=i;
	         }  
	     }  
	             return id;  
	}
	
	class MyNode {
		 int id; // good coding practice would have this as private
		 public MyNode(int id) {
		 this.id = id;
		 }
		 public String toString() { // Always a good idea for debuging
		 return "V"+id; // JUNG2 makes good use of these.
		 } 
		 }
	
	class MyLink {
		 double capacity; // should be private 
		 double weight; // should be private for good practice
		 int id;
		 
		 public MyLink(double weight, double capacity) {
		
		this.id = edgeCount++; // This is defined in the outer class.
		 this.weight = weight;
		 this.capacity = capacity;
		 } 
		 public String toString() { // Always good for debugging
		 return "E"+id;
		 }
		 
		 }
	
}
	
		 