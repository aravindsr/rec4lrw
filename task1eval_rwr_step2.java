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
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.TreeMap;
import java.util.Map.Entry;

import edu.uci.ics.jung.algorithms.scoring.PageRank;
import edu.uci.ics.jung.algorithms.scoring.PageRankWithPriors;
import edu.uci.ics.jung.algorithms.scoring.util.ScoringUtils;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.SparseGraph;
import edu.uci.ics.jung.graph.util.EdgeType;

public class task1eval_rwr_step2 {

	public static void main (String[] args) throws NumberFormatException, IOException, SQLException
	{
		        
		
		File folder = new File("task-1_evaluation/");
		File[] listOfFiles = folder.listFiles();
		
		Connection connect1 = null;
		connect1 = DriverManager.getConnection("jdbc:mysql://localhost:3307/acmdl?"+"user=root&password=");
	    Statement statement = connect1.createStatement();

		for (int i = 0; i < listOfFiles.length; i++) 
		{
		  File file = listOfFiles[i];
		  if (file.isFile() && file.getName().endsWith(".txt")) 
		  {
			  FileInputStream fstream = new FileInputStream("task-1_evaluation/"+file.getName());
				BufferedReader br1 = new BufferedReader(new InputStreamReader(fstream));
				String strLine;
				String[] articleid = new String[200];
				String articleidlist="";
				String[] refcitarcticleid = new String[20000];
				
			    int tc=0;
			   int refcount=0;
				
				while ((strLine = br1.readLine()) != null)   
				{
					
					if(tc ==0)
					{
						articleidlist=strLine.trim();
					}
					else
					{
						articleidlist=articleidlist+","+strLine.trim();
					}
					
					tc++;
					
					
				}
				
				String query ="select distinct concat(ntype,'|',article_id,'|',ref_cit_id) refcitations from combinedarticles_refsandcitations where article_id in ("+articleidlist+");";
				 
				 //System.out.println(query);
				 ResultSet resultSet = statement.executeQuery(query);
				 while (resultSet.next()) 
			      {
					 refcitarcticleid[refcount]=resultSet.getString("refcitations");
					 refcount++;
				   
			      }
				System.out.println(refcount);
				
				Graph<Integer,String> g= new SparseGraph<Integer, String>();
				
				int verticecount=0;
				
				for(int j=0;j<refcount;j++)
				{
					
					String currentrecord=refcitarcticleid[j];
					String[] data = currentrecord.split("\\|");
					if(data[0].trim().matches("r"))
					{
						g.addVertex(Integer.valueOf(data[1]));
						verticecount++;
						g.addVertex(Integer.valueOf(data[2]));
						verticecount++;
						g.addEdge(data[1]+"-"+data[2], Integer.valueOf(data[1]), Integer.valueOf(data[2]),EdgeType.DIRECTED);
					}
					else if(data[0].trim().matches("c"))
					{
						g.addVertex(Integer.valueOf(data[2]));
						verticecount++;
						g.addVertex(Integer.valueOf(data[1]));
						verticecount++;
						g.addEdge(data[2]+"-"+data[1], Integer.valueOf(data[2]), Integer.valueOf(data[1]),EdgeType.DIRECTED);
					}
				
					
				}
				System.out.println(verticecount);
								
				PageRank<Integer, String> ranking= new PageRank<Integer, String>(g, 0.15);
				ranking.evaluate();
				
				Collection<Integer> verticeslistc = g.getVertices();
				Integer[] verticeslist = (Integer[])(verticeslistc.toArray(new Integer[verticeslistc.size()]));
				
				HashMap<String,Double> map1 = new HashMap<String,Double>();
				
				for(int i1=0;i1<verticeslist.length;i1++)
				{
					
					map1.put(String.valueOf(verticeslist[i1]),ranking.getVertexScore(verticeslist[i1]));
				}
				
				ValueComparator bvc =  new ValueComparator(map1);
		        TreeMap<String,Double> sorted_map = new TreeMap<String,Double>(bvc);
		        sorted_map.putAll(map1);
		        
		        String[] filen=file.getName().toString().split("\\.");
		        String s = filen[0].toLowerCase();
			    String[] words = s.split("\\s+");
		        File file1 = new File("task-1_evaluation/rwr/"+filen[0]+".txt");
				file1.createNewFile();
				FileWriter fw = new FileWriter(file1.getAbsoluteFile());
				System.out.println(filen[0]);
		        
		        int mapcount=0;
		        String pr_articleid="";
		        for(Entry<String, Double> entry : sorted_map.entrySet()) {
		            if(mapcount < 20)
		            {
		        	String key = entry.getKey();
		            Double value = entry.getValue();
		            //System.out.println(key + " " + value);
		            
		            pr_articleid=key;
		            fw.write(pr_articleid+"|"+value);
					fw.write("\n");
					fw.flush();
		           
		            mapcount++;
		            
		            }
		        }
				
		        
		        fw.close();
				
				fstream.close();
				br1.close();
				}
		        
				 
				
		  }
		}
		        /* ATTENTION!!!! RUN THIS LATERS
		        //read file
				FileInputStream fstream = new FileInputStream("modified_article_refpluscit_cfmatrix.txt");
				BufferedReader br1 = new BufferedReader(new InputStreamReader(fstream));
				String strLine;
				Integer[] storevertices= new Integer[4967106];
				int i=0;
				
				Graph<Integer,String> g= new SparseGraph<Integer, String>();
				//Graph<MyNode,MyLink> g = new DirectedSparseGraph<jung_acm101.MyNode, jung_acm101.MyLink>();
				
					
				while ((strLine = br1.readLine()) != null)
				{
					String[] data = strLine.split(",");
					
					storevertices[i]=Integer.valueOf(data[0]);
					g.addVertex(Integer.valueOf(data[0]));
					i++;
					storevertices[i]=Integer.valueOf(data[1]);
					g.addVertex(Integer.valueOf(data[1]));
					g.addEdge(data[0]+"-"+data[1], Integer.valueOf(data[0]), Integer.valueOf(data[1]),EdgeType.DIRECTED);
					i++;
				}
				
				br1.close();
				fstream.close();
				
				
				//System.out.println(storevertices[173794]);
				System.out.println(i);

				
				Integer[] filtervertices= new Integer[]{1133076,1531928,1451991,1531918,1646448,267445,544246,1571997,1964121,238489,258820,354830,956952,1321530,1386398,1566477,1646254,1871453,1871644,1900117,2016085};
				 List<Integer> intList = new ArrayList<Integer>();
				    for (int index = 0; index < filtervertices.length; index++)
				    {
				        intList.add(filtervertices[index]);
				    }
				//CODE FOR PAGERANK
				PageRank<Integer, String> ranking= new PageRank<Integer, String>(g, 0.15);
				PageRankWithPriors<Integer, String> ranking1 = new PageRankWithPriors<>(g, ScoringUtils.getUniformRootPrior(intList), 0.15);
				ranking.evaluate();
				ranking1.evaluate();
				
				System.out.println(ranking.getVertexScore(1133076));
				System.out.println(ranking1.getVertexScore(1133076));
				*/
	}

