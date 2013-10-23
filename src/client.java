import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Array;
import java.net.Socket;
import java.net.UnknownHostException;


public class client
{
	@SuppressWarnings("deprecation")
	public static void main(String[] args) throws Exception, IOException
	{
		int fileSize = 3046;
		String fileName = "A1.jpg"; 
		String output="";
		char[] recvData = new char[fileSize];
		int i = 0;
		Socket sock= new Socket("127.0.0.1",9898);
		BufferedReader in = new BufferedReader(new InputStreamReader(sock.getInputStream()));
		DataOutputStream out = new DataOutputStream(sock.getOutputStream());
		out.writeBytes("Aara\n");
		//byte recvData[] = new byte[1000];
		char charData[] = new char[1000];
		//dis.read(recvData);
		String data;
		data = in.readLine();
		System.out.println("data: "+data);
		System.out.println("before parse int");
		fileSize = Integer.parseInt(data);
		String dir = System.getProperty("user.dir");
		System.out.println("size/......."+fileSize);
		String filePath = dir+"\\"+fileName;
		File download= new File(filePath);
		if (!download.exists()) {
			download.createNewFile();
		}
		
		FileOutputStream fos = new FileOutputStream(filePath);
		byte byteArr[] = new byte[1024];
		byte finalArr[] = new byte[fileSize];
		while(i+1024<fileSize)
		{
			/*output = output+in.readLine();
			output = output+"\n";*/
			System.out.println("reading");
			sock.getInputStream().read(byteArr);
			System.out.println();
			System.out.println("read,writing");
			for(int k = 0; k<1024;k++)
			{
				finalArr[i+k] = byteArr[k];
			}
			//fos.write(byteArr, 0, 1024);
			System.out.println("wrote");
			System.out.println("Downloading "+(i/10)+"%");
			//bw.write("\r\n");
			//System.out.println("o/p: "+output);
			//in.read(charData, i, i+999);
			
			i=i+1024;
		}
		if(i<fileSize)
		{
			sock.getInputStream().read(byteArr);
			for(int k=0;k<fileSize-i;k++)
			{
				finalArr[i+k] = byteArr[k];
			}
		}
		System.out.println("..............."+(int)finalArr[fileSize-1]);
		fos.write(finalArr, 0, fileSize-1);
		fos.flush();
		System.out.println("in client");
		//System.out.println(output);
		out.close();
		fos.close();
	}
}