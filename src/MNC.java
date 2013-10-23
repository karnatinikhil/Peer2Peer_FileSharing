//to get the requested file content(download)
import java.io.*;
import java.net.*;
import java.util.*;
import java.text.SimpleDateFormat;

public class MNC
{
        public static void main(String args[]) throws Exception
        {
                String requestedFile;
                String requestMessage;
                File reqFile;
                System.out.println("started");
                ServerSocket serv = new ServerSocket(3445);
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

                final String dir = System.getProperty("user.dir");
                requestedFile = dir+requestedFile;
                System.out.println("fgyj:     "+requestedFile);
                SimpleDateFormat dateFormat = new SimpleDateFormat("EE, dd MMM YYYY HH:mm:ss");
                reqFile = new File(requestedFile);
                int fileSize = (int)reqFile.length();
                //int packetsize=1024;
                //double nosofpackets=Math.ceil(((int) reqFile.length())/packetsize);
                FileInputStream fis = new FileInputStream(requestedFile);
                
                byte[] fileData = new byte[fileSize];
                fis.read(fileData);
                Date date = new Date();
                System.out.println("1");
                out.writeBytes("HTTP/1.1 200 OK\n");
                out.writeBytes("Date: "+dateFormat.format(date)+"\n");
                out.writeBytes("Server: localhost\n");
                out.writeBytes("Last-Modified: "+dateFormat.format(reqFile.lastModified())+"\n");
                out.writeBytes("Content-Length: "+fileSize+"\n");
                out.writeBytes("Content-Type: plain/text\n");
                //out.writeBytes("Content-Type: audio/mpeg\n");
                out.writeBytes("\n\n");                
                System.out.println("2,fileSize:"+fileSize);
                out.write(fileData,0,fileSize);
                
                System.out.println("after");
                fis.close();
                out.close();
                sock.close();
                serv.close();
        }
}