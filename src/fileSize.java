import java.io.*;

	public class fileSize
	{		
		public static long[] getFilesList(String dir)
		{
			System.out.println("in getFilesList");
			long filesData[] = new long[2];//0-total size,1-num of files
			long tempFilesData[] = new long[2];
			File directory = new File(dir);
			File file;
			int j = 0;
			String tempName="";
			File[] filesList = directory.listFiles();
			for(j=0;j<filesList.length;j++)
			{
				tempName = filesList[j].getPath();				
				//System.out.println("full name: "+tempName);
				if(filesList[j].isFile())
				{
					filesData[1] = filesData[1]+1;
					//System.out.println(filesList[j].getName());
					file = filesList[j];
					//System.out.println("len: "+file.length());
					filesData[0]+=file.length();
					
				}
				/*else if(filesList[j].isDirectory())
				{
					System.out.println("in isdir");
					tempFilesData = getFilesList(tempName);
					filesData[0] += tempFilesData[0];
					filesData[1] += tempFilesData[1];
				}*/				
			}
			System.out.println("local totalSize: "+filesData[0]);
			return filesData;	
		}		
		public static void main(String []args)
		{			
			double gbSize = 0.0,mbSize = 0.0,kbSize = 0.0;
			long filesData[] = new long[2];
			String dir = System.getProperty("user.dir");
			filesData = getFilesList(dir);
			//System.out.println("total len: "+filesData[0]);
			System.out.println("num of files : "+filesData[1]);
			kbSize = (double)filesData[0]/1024;
			if(kbSize > 1024)
				mbSize = kbSize/1024;
			if(mbSize > 1024)
				gbSize = mbSize/1024;
			if(gbSize>0)
				System.out.println("gb size: "+gbSize);
			else if(mbSize > 0)
				System.out.println("mb size: "+mbSize);
			else if(kbSize > 0)
				System.out.println("kb size: "+kbSize);
			else
				System.out.println("bytesSize: "+filesData[0]);
		}
	}