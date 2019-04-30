import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import edu.uci.ics.jung.algorithms.filters.FilterUtils;
import edu.uci.ics.jung.graph.DirectedSparseGraph;
import edu.uci.ics.jung.graph.Graph;
import edu.uci.ics.jung.graph.util.EdgeType;



public class task1eval_others_diversity {
	
	 static int edgeCount;
	

	public static void main (String[] args) throws IOException
	{
		
		//read file
		//FileInputStream fstream = new FileInputStream("Art_ref_5000_jung.csv");
		FileInputStream fstream = new FileInputStream("modified_article_refpluscit_cfmatrix.txt");
		BufferedReader br1 = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		//Integer[] storevertices= new Integer[173798];
		Integer[] storevertices= new Integer[4967104];
		int i=0;
		
		//Graph<Integer,String> g= new SparseGraph<Integer, String>();
		DirectedSparseGraph<Integer, String> g = new DirectedSparseGraph<Integer, String>();
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
		System.out.println(i);
		
		File file1 = new File("oneoffcfpr_topics-diversityvalues.txt");
		file1.createNewFile();
		FileWriter fw = new FileWriter(file1.getAbsoluteFile());
		
		File folder = new File("dummy/");
		File[] listOfFiles = folder.listFiles();
		System.out.println(listOfFiles.length);

		for (int k = 0; k < listOfFiles.length; k++) 
		{
		  File file = listOfFiles[k];
		  if (file.isFile() && file.getName().endsWith(".txt")) 
		  {
			  FileInputStream fstream2 = new FileInputStream("dummy/"+file.getName());
				BufferedReader br2 = new BufferedReader(new InputStreamReader(fstream2));
				String strLine2;
				Integer[] articleids= new Integer[17];
				int p=0;
					
				while ((strLine2 = br2.readLine()) != null)
				{
					//System.out.println(strLine2);
					String[] data = strLine2.split("\\|");
					articleids[p]=Integer.valueOf(data[1]);//CHANGE THIS TO 1 for ALL METHODS EXCEPT RWR
					p++;
				}
				
				String[] filen=file.getName().toString().split("\\.");
			      System.out.println(filen[0]);
				
				//Integer[] filtervertices= new Integer[]{1531928,1451991,1531918,1646448,267445,544246,1571997,1964121,238489,258820,354830,956952,1321530,1386398,1566477,1646254,1871453,1871644,1900117,2016085};
				Graph<Integer, String> currentGraph = (Graph<Integer, String>) FilterUtils.createInducedSubgraph(Arrays.asList(articleids), g);
				System.out.println(currentGraph.getEdgeCount());
				fw.write(filen[0]+","+currentGraph.getEdgeCount());
				fw.write("\n");
				fw.flush();
				
				br2.close();
				fstream2.close();
				
		  }
		  
		 }
		
		fw.close();
		
		
		
		
		
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