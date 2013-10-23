import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.Map.Entry;

import javax.management.modelmbean.RequiredModelMBean;
import javax.sql.XAConnection;

public class fileIndex
{
	private static FileInputStream fis;

	public static HashMap<Integer,String[]> getFilesList(String dir)
	{
		int counter = 0;
		String exactFileName,exactFileName1;
		System.out.println("directory: "+dir);
		File directory = new File(dir);
		File[] filesList = directory.listFiles();



		HashMap<Integer, String[]> filesIndex = new HashMap<Integer,String[]>();
		for(int j=0;j<filesList.length;j++)
		{
			if(filesList[j].isFile())
			{
				String[] details = new String[2];
				counter++;
				exactFileName1 =filesList[j].getName();
				exactFileName = exactFileName1.replace(dir,"");
				System.out.println("......"+exactFileName);
				details[0] = exactFileName;
				details[1] = Integer.toString((int) filesList[j].length());

				filesIndex.put(counter, details);
			}	
		}
		Set<Entry<Integer, String[]>> set = filesIndex.entrySet();
		Iterator<Entry<Integer, String[]>> i = set.iterator();
		//Prints key,value pairs
		while(i.hasNext())
		{
			Entry<Integer, String[]> me = i.next();
			System.out.println(",,,,"+me.getKey()+"  "+me.getValue()[0]);	
		}
		return filesIndex;
	}

	public static void downloadFile(String filePath,ServerSocket serv ,Socket sock,DataOutputStream out) throws Exception
	{
		int len = 0;
		System.out.println("filepath: "+filePath);
		FileInputStream fis = new FileInputStream(filePath);
		SimpleDateFormat dateFormat = new SimpleDateFormat("EE, dd MMM YYYY HH:mm:ss");
		Date date = new Date();
		File fileData = new File(filePath);
		int  fileSize = (int) fileData.length();
		System.out.println("jvgkvsduvgsdiugvdskvgsdvgsvgs"+fileSize);
		long fs = fileData.length();
		System.out.println("int size: "+fileSize);
		System.out.println("long size: "+fs);
		byte[] byteData = new byte[fileSize];
		byte[] sendData = new byte[1000];
		System.out.println("arr size: "+byteData.length);
		fis.read(byteData);
		
		int t1=0;
		int z = 0;
		try{
			System.out.println("fileSize: "+fileSize);
			String sendSize = ""+fileSize+"\n";
			out.writeBytes(sendSize);
		while(t1+1024 < fileSize)
		{
			//System.out.println("in while"+t1);
			//if(t1+1024 < fileSize)
			//{
				System.out.println("aaaaaaaaaaaaaaaaaaaaaaaaaaaaaa"+"  "+t1+"  "+byteData.length);
			out.write(byteData, t1,1024);
			out.writeBytes("\n");
			out.flush();
			//}
			//temp=temp-1024;
			t1+=1024;
		}
		//else
		//{
			System.out.println("bbbbbbbbbbbbbbbbbbbbbbbbbbbbb"+"  "+t1+"  "+(fileSize-t1));
			out.write(byteData, t1,fileSize-t1);
			out.writeBytes("\n");
		//}
	
		}
		catch(IOException e)
		{
			e.printStackTrace();
		}
		//out.write(byteData,0,fileSize);
		out.writeBytes("\n");
		System.out.println("done");
		//fis.close();
		out.close();
	}

	public static HashMap<Integer, String[]> getRequiredFilesList(HashMap<Integer, String[]> filesIndex,String requestedFileName)
	{
		int counter = 0;
		HashMap<Integer, String[]> matchedFiles = new HashMap<Integer, String[]>();
		Set<Entry<Integer, String[]>> set = filesIndex.entrySet();
		Iterator<Entry<Integer, String[]>> i = set.iterator();
		//Prints key,value pairs
		System.out.println("requested file name: "+requestedFileName);
		while(i.hasNext())
		{
			Entry<Integer, String[]> me = i.next();
			if(me.getValue()[0].toUpperCase().contains(requestedFileName.toUpperCase()))
			{
				counter++;
				System.out.println("matched: "+me.getValue()[0]);
				System.out.println("file size: "+Integer.parseInt(me.getValue()[1]));
				matchedFiles.put(counter, me.getValue());
			}	
		}
		System.out.println("counter: "+counter);
		return matchedFiles; 
	}

	public static void main(String []args) throws Exception
	{
		ServerSocket serv = new ServerSocket(9898);
		Socket sock = serv.accept();
		System.out.println("started");
		//need to ` logic to get file index
		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		DataOutputStream out = new DataOutputStream(sock.getOutputStream());
		String requestMessage = in.readLine();
		System.out.println("req mag: "+requestMessage);
		//StringTokenizer st = new StringTokenizer(requestMessage);
		//String requestedFile = st.nextToken();
		//requestedFile = st.nextToken();
		String requestedFile = requestMessage;
		//String[] strArray = requestedFile.split("/");
		//int len = strArray.length;
		//requestedFile = strArray[len-1];
		
		String dir = System.getProperty("user.dir");
		HashMap<Integer, String[]> filesIndex = getFilesList(dir);
		HashMap<Integer, String[]> matchedFiles = getRequiredFilesList(filesIndex,requestedFile);
		
		
		String fileName = new String();
		//assuming dowmload file index is 3
		int downloadIndex = 1;
		int numOfFiles = matchedFiles.size();
		System.out.println("num of files: "+numOfFiles);
		if(downloadIndex > numOfFiles || downloadIndex<1)
		{
			System.out.println("wrong index");
			System.exit(0);
		}
		Set<Entry<Integer, String[]>> set = matchedFiles.entrySet();
		Iterator<Entry<Integer, String[]>> i = set.iterator();
		//Prints key,value pairs
		while(i.hasNext())
		{
			Entry<Integer, String[]> me = i.next();
			if(downloadIndex == me.getKey())
			{
				fileName = me.getValue()[0];
			}	
		}

		String downloadPath = dir+"\\"+fileName;
		System.out.println("download path: "+downloadPath);
		
		downloadFile(downloadPath,serv,sock,out);
	}
}
