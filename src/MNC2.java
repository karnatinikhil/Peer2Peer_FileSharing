//to get the list of matching files, and indexing
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class MNC2
{
	public static void main(String args[]) throws Exception
	{
		int i = 0,j=0,count =0;
		long filesListLen = 0;
		String requestedFile;
		String requestMessage;
		String exactFileName1="",exactFileName=""; 
		//File reqFile;
		System.out.println("started");
		ServerSocket serv = new ServerSocket(3334);
		Socket sock = serv.accept();
		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		DataOutputStream out = new DataOutputStream(sock.getOutputStream());
		new PrintWriter(out);
		requestMessage = in.readLine();
		StringTokenizer st = new StringTokenizer(requestMessage);
		//      if(st.nextToken().equals("GET"))
		//      {
		//if(requestedFile.startsWith("/"))
		requestedFile = st.nextToken();
		requestedFile = st.nextToken();
		//      }
		String dir = System.getProperty("user.dir");
		System.out.println(dir);
		String[] strArray;
		strArray = requestedFile.split("/");
		int len = strArray.length;
		String fileName = strArray[len-1];
		for(i = 0; i < strArray.length -1; i++)
		{
			//dir.append("\");
			// incomplete: need to append relative path to dir...dir = dir + "\" + strArray[i];
		}
		System.out.println("dir:"+dir);
		System.out.println("fileName:"+fileName);
		String nameArray[] = fileName.split(" ");
		File directory = new File(dir);
		File[] filesList = directory.listFiles();
		String[] matchedFiles = new String[filesList.length]; 

		for(i = 0; i < nameArray.length; i++)
		{
			for(j=0;j<filesList.length;j++)
			{
				System.out.println("nameArray,filesList: "+nameArray[i]+"  "+filesList[j]);
				if(filesList[j].isFile())
				{

					//StringTokenizer stkn = new StringTokenizer(filesList[j].getName(),"\");
					exactFileName1 =filesList[j].getName();
					exactFileName = exactFileName1.replace(dir,""); 
					System.out.println("exact: "+exactFileName);
					if(exactFileName.startsWith(nameArray[i]))
					{
						System.out.println("matched....");
						filesList[j] = null;
						matchedFiles[count] = exactFileName;
						count++;
						filesListLen += nameArray[i].length();
					}
				}
			}
		}
		SimpleDateFormat dateFormat = new SimpleDateFormat("EE, dd MMM YYYY HH:mm:ss");
		Date date = new Date();
		System.out.println("1");
		out.writeBytes("HTTP/1.1 200 OK\n");
		out.writeBytes("Date: "+dateFormat.format(date)+"\n");     	
		out.writeBytes("Server: localhost\n");
		out.writeBytes("Last-Modified: "+dateFormat.format(date)+"\n");
		//  out.writeBytes("Content-Length: "+filesListLen+"\n");
		// out.writeBytes("Content-Type: plain/text\n");
		out.writeBytes("\n\n");
		if(count>0)
		{
			System.out.println("count > 0");
			//String writeString;
			//out.writeBytes("Match \n");
			//out.flush();
			for(i=0;i<count;i++)
			{

				//writeString = writeString + nameArray[i]+"\n";
				out.writeBytes(matchedFiles[i]+"\n");
				out.flush();

			}
		}
		else
		{

		}
		System.out.println("done");
		out.close();
		sock.close();
		serv.close();
	}
}