import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class combinedarticles_generatecitationcount {

	public static void main (String[] args) throws IOException
	{
		FileInputStream fstream = new FileInputStream("combinarticles_articleid_new.txt");
		BufferedReader br1 = new BufferedReader(new InputStreamReader(fstream));
		String strLine;
		int[] articleid = new int[122406];
		
	    int i=0;
		
		while ((strLine = br1.readLine()) != null)   
		{
			articleid[i]= Integer.valueOf(strLine.trim());
			 i++;
		}
		
		System.out.println(i);
		
		br1.close();
		fstream.close();
		
		FileInputStream fstream2 = new FileInputStream("combinedarticles_citations.txt");
		BufferedReader br2 = new BufferedReader(new InputStreamReader(fstream2));
		String strLine2;
		int[] citationid = new int[316503];
		
	    int j=0;
		
		while ((strLine2 = br2.readLine()) != null)   
		{
			String[] data = strLine2.split(",");
			citationid[j]= Integer.valueOf(data[0]);
			 j++;
		}
		
		System.out.println(j);
		
		br2.close();
		fstream2.close();
		String[] finallist = new String[122406];
		
		for(int k=0;k<i;k++)
		{
			int currentid=articleid[k];
			int currentidcount=0;
			for(int m=0;m<j;m++)
			{
				if(currentid-citationid[m] == 0)
				{
					currentidcount=currentidcount+1;
				}
			}
			
			System.out.println(currentid+","+currentidcount);
			finallist[k]=currentid+","+currentidcount;
		}
		
		File file = new File("combinedarticles_citationcountgenerated_new.txt");
		file.createNewFile();
		FileWriter fw = new FileWriter(file.getAbsoluteFile());
		BufferedWriter bw = new BufferedWriter(fw);
		
		for(int n=0;n<i;n++)
		{
			fw.write(finallist[n]);
			fw.write("\n");
			fw.flush();
		}
		
		fw.close();
	}
	
}
