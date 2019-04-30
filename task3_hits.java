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
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.TreeMap;

import javax.swing.JFrame;

import org.apache.commons.collections15.Transformer;

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


public class task3_hits {
	
	 static int edgeCount;
	

	public static void main (String[] args) throws IOException, SQLException
	{
		
		Connection connect=null;
		
		connect = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
			
	      Statement statement = connect.createStatement();
	    
	      String ipvalue = args[0];
	      String[] ipvalues=ipvalue.split("\\|");
	      String username=ipvalues[0];
	      String collection_name=ipvalues[1];
	      String at = ipvalues[2];
	      
	      int  at_reccount=0;
	      
	      //retrieve threshold values for article type
	    ResultSet resultSet_at = statement.executeQuery("select rec_count from rec4lrw_articletype_thresholds where at_abbr='"+at+"'");
	    while (resultSet_at.next()) 
	    {
	    	  at_reccount = resultSet_at.getInt("rec_count");
	    }
	    //System.out.println(at_reccount);
	    	     
		ResultSet resultSet = statement.executeQuery("select article_id,ref_cit_id from combinedarticles_refsandcitations where article_id in (select article_id from rec4lrw_user_readinglist where collection_name='"+collection_name+"' and username ='"+username+"')  limit 10000");
		String q1a = "select article_id,ref_cit_id from combinedarticles_refsandcitations where article_id in (select article_id from rec4lrw_user_readinglist where collection_name='"+collection_name+"' and username ='"+username+"')  limit 10000";
		System.out.println(q1a);
		Integer[] storevertices= new Integer[14280];
		int i=0;
		
		
		//Graph<Integer,String> g= new SparseGraph<Integer, String>();
		DirectedSparseGraph<Integer, String> g = new DirectedSparseGraph<Integer, String>();
		
		 while (resultSet.next()) 
		    {
		    	  int aid = resultSet.getInt("article_id");
		    	  int refid =resultSet.getInt("ref_cit_id");
		    	  storevertices[i]=aid;
				   g.addVertex(aid);
					i++;
					storevertices[i]=refid;
					g.addVertex(refid);
					g.addEdge(aid+"-"+refid, aid, refid,EdgeType.DIRECTED);
					i++;
		    	
		    }
		
		
		//System.out.println(i);
				
		Collection<Integer> vertices = g.getVertices();
		Integer[] verticesarray=vertices.toArray (new Integer[vertices.size()]);
		
		
		HITS<Integer, String> hits1 = new HITS<Integer,String>(g);
		hits1.evaluate();
		//System.out.println();
		
		Double[] hitshubvalue= new Double[14280];
		Double[] hitsauthorityvalue= new Double[14280];
		
		HashMap<String,Double> map = new HashMap<String,Double>();
		
		for(int i1=0;i1<verticesarray.length;i1++)
		{
			hitshubvalue[i1]=hits1.getVertexScore(verticesarray[i1]).hub;
			hitsauthorityvalue[i1]=hits1.getVertexScore(verticesarray[i1]).authority;
			map.put(String.valueOf(verticesarray[i1]),hitshubvalue[i1]);
		}
		
		ValueComparator bvc =  new ValueComparator(map);
        TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
        
		//System.out.println(hitshubvalue[173795]);
		//System.out.println(hitsauthorityvalue[173795]);
		//System.out.println(hits1.getVertexScore(2));
		//System.out.println(hits1.getVertexScore(3));
		
		//System.out.println();
		//System.out.println(verticesarray[(int) getMaxValue(hitshubvalue)]);
		//System.out.println(verticesarray[(int) getMaxValue(hitsauthorityvalue)]);
		//System.out.println();
		for(int j1=0;j1<verticesarray.length;j1++)
		{
			//System.out.println(verticesarray[j1]+" "+hitshubvalue[j1]);
		}
		
		sorted_map.putAll(map);

        //System.out.println("results: "+sorted_map);
        int mapcount=0;
        String hits_articleid="";
        for(Entry<String, Double> entry : sorted_map.entrySet()) {
            if(mapcount < at_reccount)
            {
        	String key = entry.getKey();
            Double value = entry.getValue();
            //System.out.println(key + " " + value);
            if(mapcount == 0)
            {
            	hits_articleid=key;
            }
            else
            {
            	hits_articleid=hits_articleid+","+key;
            }
            mapcount++;
            
            }
        }
        //System.out.println(hits_articleid);
        mysqlretrievetitleabs(hits_articleid);
		
	}
	
	private static void mysqlretrievetitleabs(String uniids) throws SQLException
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
		String query ="select a.article_id,a.cit_ct,a.ref_ct,a.cov_ct,(((cit_ct-mincit_ct)/(maxcit_ct-mincit_ct))*0.2) + (((ref_ct-minref_ct)/(maxref_ct-minref_ct))*0.2) + (((cov_ct-mincov_ct)/(maxcov_ct-mincov_ct))*0.6) composite_rank, a.article_title, a.article_abstract, a.article_pubdate, a.article_kw, a.article_authors,a.article_doi,convert(a.citations_count,UNSIGNED INTEGER) citations_count from (select article_id,convert(citations_count,UNSIGNED INT) cit_ct,convert(references_count,UNSIGNED INT) ref_ct,convert(coverage,UNSIGNED INT) cov_ct, replace(IF(article_subtitle = '',article_title,concat(article_title,': ',article_subtitle)),',','') article_title, replace(replace(article_abstract,'<p>',''),'</p>','') article_abstract, article_pubdate, article_kw, article_authors,article_doi,convert(citations_count,UNSIGNED INTEGER) citations_count from combinedarticles_basic where article_id in ("+uniids+")) a,(select max(convert(citations_count,UNSIGNED integer)) maxcit_ct,min(convert(citations_count, UNSIGNED INTEGER)) mincit_ct,max(convert(references_count,UNSIGNED integer)) maxref_ct,min(convert(references_count, UNSIGNED INTEGER)) minref_ct,max(convert(coverage,UNSIGNED integer)) maxcov_ct,min(convert(coverage, UNSIGNED INTEGER)) mincov_ct from combinedarticles_basic where article_id in ("+uniids+")) b order by composite_rank desc limit 20";
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
	class ValueComparator implements Comparator<String> {

	    Map<String, Double> base;
	    public ValueComparator(Map<String, Double> base) {
	        this.base = base;
	    }

	    // Note: this comparator imposes orderings that are inconsistent with equals.    
	    public int compare(String a, String b) {
	        if (base.get(a) >= base.get(b)) {
	            return -1;
	        } else {
	            return 1;
	        } // returning 0 would merge keys
	    }
	}
		 