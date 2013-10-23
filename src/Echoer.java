


import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
//@SuppressWarnings("all")
class udpThread extends Thread{
	Thread t1;
	public static DatagramSocket udpServerSocket;
	public static byte[] receiveData;
	ArrayList<Byte> arr;
	ByteArrayOutputStream bStream;

	//oStream.writeObject ( obj );
	//byte[] byteVal = bStream. toByteArray()
	udpThread(DatagramSocket udpServSocket){
		udpServerSocket=udpServSocket;
		start();
	}

	public void run() {


		receiveData= new byte[10000];
		arr= new ArrayList<Byte>();
		String message="";
		while(true){
			DatagramPacket receivePacket= new DatagramPacket(receiveData,receiveData.length);
			//DatagramPacket receivePacket= new DatagramPacket((byte [])arr.toArray(),arr.toArray().length);

			try {
				udpServerSocket.receive(receivePacket);
				message= new String(receivePacket.getData());

				//Echoing message to: IP=ipaddress type=udp
				System.out.println("\nechoing "+message.trim()+"\n\t from: IP="+receivePacket.getAddress().getHostAddress()+"\n\ttype=UDP");
				System.out.println("echoer:>");
			} catch (IOException e) {
				e.printStackTrace();
			}

		}

	}

}
class InputThread extends Thread{
	Thread t;
	String input[];
	public static ServerSocket servSocket;	
	public static InetAddress hostDetail;
	public static DatagramSocket udpServSocket;
	public static List<Socket> clientSockets;

	BufferedReader br= new BufferedReader(new InputStreamReader(System.in));
	InputThread(){
		super("input thread");
	}
	InputThread(ServerSocket servSock,DatagramSocket dgramSocket,InetAddress hostDetails,List<Socket> clientListSockets){
		udpServSocket=dgramSocket;
		servSocket=servSock;
		hostDetail=hostDetails;
		clientSockets=clientListSockets;

		start();
	}
	public static void helpScreen(){
		System.out.println("Please enter the following commands:");
		System.out.println("\n");
		System.out.println("info:                                     \tTo print out host's IP address, name, TCP (listening) port, UDP port. ");
		System.out.println("connect <IPAddress> <TCPPortNo>: 	      \tTo establish a TCP connection to <IPAddress> at port <TCPPortNo>.");
		System.out.println("show:                            		To show the existing TCP connections");
		System.out.println("send <conn-id> <message>:                 \tTo send message to connection ID");
		System.out.println("sendto <ip-address> <udp-port> <message>: \tTo send message as UDP datagram to ip-address");
		System.out.println("disconnect <conn-id>:                     \tTo disconnect the TCP connection whose id is <conn-id>");
		System.out.println("help:                                     \tTo display help screen");


	}
	@Override
	public void run() {
		try {
			System.out.println("=========================================================================================");
			System.out.println("                                   Welcome To Echoer");
			System.out.println("=========================================================================================");
			helpScreen();

			while(true){
				System.out.print("echoer:>");
				input=br.readLine().trim().split("\\ ");

				processInput(input,servSocket,udpServSocket,hostDetail,clientSockets);
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
	public static boolean checkForDuplicates(List<Socket> clientsList,String hostName,int portNo) throws UnknownHostException, Exception{
		//Iterator<Socket> itr= clientsList.iterator();
		int index=0;
		boolean flag=false;
		while(index!=clientsList.size()){
			if(clientsList.get(index).getInetAddress().getHostAddress().equalsIgnoreCase("127.0.0.1")){
				Socket h= new Socket("8.8.8.8",53);
				InetAddress addr=h.getLocalAddress();
				if((addr.getHostAddress().equalsIgnoreCase(hostName)) && (clientsList.get(index).getPort()==portNo)){
					return true;
				}
			}




			flag=((clientsList.get(index).getInetAddress().getHostAddress().equalsIgnoreCase(hostName)||(("127.0.0.1").equalsIgnoreCase(hostName))||(clientsList.get(index).getInetAddress().getHostName().equalsIgnoreCase(hostName))) &&( (clientsList.get(index).getPort()==portNo)));
			if(flag){
				return true;
			}
			else{
				index++;
			}

		}
		return false;
	}
	public void processInput(String input[],ServerSocket servSocket,DatagramSocket udpServSocket,InetAddress hostDetails,List<Socket> clientsSockets) throws Exception{
		Socket clientSockets;
		DatagramSocket udpClientSockets;
		DatagramPacket sendPacket;
		byte entireMsg[];
		if(input[0].trim().length()==0);

		else if((input.length==1) && (input[0].equalsIgnoreCase("info"))){
			info(servSocket,udpServSocket,hostDetails);
		}

		else if((input.length==1) && (input[0].equalsIgnoreCase("help"))){
			helpScreen();
		}
		else if((input.length==1) && input[0].equalsIgnoreCase("show")){
			show(clientsSockets);
		}
		else if((input.length==3) && input[0].equalsIgnoreCase("connect")){
			if((input[1].equalsIgnoreCase("127.0.0.1")||(input[1].equalsIgnoreCase(hostDetails.getHostAddress().toString())))&&(Integer.parseInt(input[2])==servSocket.getLocalPort())){
				System.out.println("Self connection not allowed");
			}
			else{
				try{
					String hName=input[1];
					int pNumber=Integer.parseInt(input[2]);
					if(clientsSockets.size()!=0){
						if(checkForDuplicates(clientsSockets,hName,pNumber)){
							System.out.println("Duplicate connections not allowed");
						}
						else{
							try{
								clientSockets= new Socket(hName,pNumber);//("127.0.0.1",8080);
								if(clientSockets.isConnected())
									System.out.println("Successfully connected to Server");
								clientsSockets.add(clientSockets);
							}catch(java.net.ConnectException e){
								System.out.println("Server is not listening on the "+pNumber+" Port Number");
							}
							catch(java.net.NoRouteToHostException e){
								System.out.println("No Route to Host: "+hName);
							}
						}

					}
					else{
						try{
							clientSockets= new Socket(hName,pNumber);//("127.0.0.1",8080);
							if(clientSockets.isConnected())
								System.out.println("Successfully connected to Server");
							clientsSockets.add(clientSockets);
						}catch(java.net.ConnectException e){
							System.out.println("Server is not listening on the "+pNumber+" Port Number");
						}
						catch(java.net.NoRouteToHostException e){
							System.out.println("No Route to Host: "+hName);
						}
					}
				}
				catch(NumberFormatException e){
					System.out.println("Please enter valid Port Number");
				}
			}
		}
		else if((input.length>=4) && input[0].equalsIgnoreCase("sendto")){
			try{
				udpClientSockets= new DatagramSocket();
				String entireMessage="";
				for(int i=3;i<input.length;i++){

					entireMessage+=input[i]+" ";
				}
				entireMsg=entireMessage.getBytes();
				int pNumber=Integer.parseInt(input[2]);
				if(pNumber>60000 || pNumber<0){
					System.out.println("Please enter valid Port Number");
				}
				//sendTo(input[3].getBytes(),input[3].length(),Integer.parseInt(input[1]),Integer.parseInt	(input[2]));
				else{
					sendPacket= new DatagramPacket(entireMsg,entireMsg.length,InetAddress.getByName(input[1]),pNumber);

					//System.out.println("echoing "+entireMessage+"\n\t to: IP="+sendPacket.getAddress().getHostAddress()+"\t\ntype=UDP");
					udpClientSockets.send(sendPacket);
				}

			}catch(UnknownHostException e){
				System.out.println("Please enter proper IP");
				System.exit(0);
			}
			catch(Exception e){

			}
		}
		else if((input.length>=3) && input[0].equalsIgnoreCase("send")){
			try{
				int connNumber=Integer.parseInt(input[1]);
				if(connNumber<=0 || connNumber>clientsSockets.size()){
					System.out.println("Given Connection ID doesn't exists");
				}
				else{
					String entireMessage="";
					for(int i=2;i<input.length;i++){
						entireMessage+=input[i]+" ";
					}
					entireMessage+="\n";
					send(connNumber,clientsSockets,entireMessage);
				}
			}catch(NumberFormatException e){
				System.out.println("Please enter valid Connection ID");
			}
		}
		else if((input.length==2) && input[0].equalsIgnoreCase("disconnect")){
			try{
				int connID=Integer.parseInt(input[1]);
				if(connID<=0 || connID>clientsSockets.size()){
					System.out.println("Given Connection ID doesn't exists");

				}
				else
					disconnect(clientsSockets,connID);
			}
			catch(NumberFormatException e){
				System.out.println("Please enter valid Connection ID");
			}
		}
		else{
			System.out.println("Unknown Command");
		}


	}
	public void disconnect(List<Socket> connectedClients,int connID) throws Exception{
		Iterator<Socket> itr = connectedClients.iterator();
		Socket cSocket;

		int count=0;
		while(itr.hasNext()){
			count++;
			if(count==connID){
				cSocket=connectedClients.get(connID-1);
				cSocket.close();
				connectedClients.remove(connID-1);
				break;
			}
			else{
				continue;
			}
		}
	}
	public void send(int connID,List<Socket> connectedClients,String message) throws Exception{
		Iterator<Socket> itr = connectedClients.iterator();
		Socket cSocket;
		DataOutputStream outToClient;
		int count=0;
		while(itr.hasNext()){
			count++;
			if(count==connID){
				cSocket=connectedClients.get(connID-1);
				outToClient = new DataOutputStream(cSocket.getOutputStream());
				outToClient.writeBytes(message);
				outToClient.flush();
				//System.out.println("echoing "+message+"to IP: "+cSocket.getInetAddress().getHostAddress()+"\ntype=TCP");

				break;
			}
			else{
				continue;
			}
		}

	}
	public void show(List<Socket> lstOfClientSockets){
		Socket cSock;
		if(lstOfClientSockets.size()==0){
			System.out.println("No Outgoing Connections yet!!");

		}
		else{
			System.out.println("===========================================================================");
			System.out.println("|Connection ID\t|IPAddress\t|HostName\t|LocalPort\t|RemotePort");
			System.out.println("===========================================================================");
			Iterator<Socket> itr= lstOfClientSockets.iterator();
			int count=0;
			while(itr.hasNext()){
				cSock=itr.next();
				count++;
				System.out.println(count+"\t"+cSock.getInetAddress().getHostAddress()+"\t"+cSock.getInetAddress().getHostName()+"\t"+cSock.getLocalPort()+" "+cSock.getPort());

			}
		}
	}
	public  void info(ServerSocket s,DatagramSocket udpServSocket,InetAddress hostDetails) throws UnknownHostException{
		System.out.println("IP Address\tHostName\t\t    UDP Port   TCP Port");
		InetAddress sy= InetAddress.getLocalHost();
		System.out.println(hostDetails.getHostAddress().toString()+"\t"+sy.getHostName()/*hostDetails.getHostName()*/+"\t"+udpServSocket.getLocalPort()+"\t   "+s.getLocalPort());
	}
}
class clientThread extends Thread{
	Socket cSocket;
	BufferedReader dFromClient;
	ObjectInputStream input;
	public clientThread() {
		super("clientsThread");

	}
	clientThread(Socket clientSocket){
		cSocket=clientSocket;
		start();
	}

	@Override
	public void run() {
		try {
			String inf="";
			while(true){
				dFromClient= new BufferedReader(new InputStreamReader(cSocket.getInputStream()));

				inf=dFromClient.readLine();
				if(inf.equalsIgnoreCase(null))
					break;
				else
					System.out.println("echoing "+inf+"\n\t\tFrom:IP= "+cSocket.getInetAddress().getHostAddress()+"\n\t\ttype=TCP");
				System.out.println("echoer:>");


			}

		} catch (Exception e) {
			System.out.println("Connection from "+cSocket.getInetAddress().getHostAddress()+" is lost");
			System.out.println("echoer:>");
			//e.printStackTrace();
		}

	}
}
public class Echoer {
	public static List<Socket> clientListSockets=new ArrayList<Socket>();

	public static void acceptIncomingRequests(ServerSocket servSocket) throws IOException{

		while(true){
			Socket clientSock=servSocket.accept();
			String xyz=clientSock.getInetAddress().getHostAddress();
			if(xyz.equalsIgnoreCase("127.0.0.1")){
				Socket hS= new Socket("8.8.8.8",53);
				InetAddress iA= hS.getLocalAddress();
				System.out.println("got connection request from "+iA.getHostAddress());
			}
			else
			System.out.println("got connection request from "+clientSock.getInetAddress().getHostAddress());
			if(clientSock.isConnected()){
				System.out.println("Connection Successful");
				System.out.println("echoer:>");
			}
			//clientListSockets.add(clientSock); 
			new clientThread(clientSock);
		}
	}
	public static ServerSocket servSocket;
	public static InetAddress hostDetails;
	public static DatagramSocket servUDPSocket;


	public static void main(String[] args) throws Exception{
		try{
			if(args.length!=2){
				System.out.println("Please enter  two runtime arguments i.e TCP and UDP port numbers only");
				System.exit(0);
			}
			//tcport=udport write for that 
			int tcpPort=Integer.parseInt(args[0]);
			int udpPort= Integer.parseInt(args[1]);
			if(tcpPort>60000 || udpPort>60000 ||tcpPort<0 || udpPort<0){
				System.out.println("Please Enter Valid Port Numbers which are greater than 0 and lesser than 60000");
				System.exit(0);
			}
//			byte ip[]={8,8,8,8};
//			InetAddress ir= InetAddress.getByAddress(ip);
//			
//			DatagramSocket dSock= new DatagramSocket(53,ir);
//			System.out.println(dSock.getInetAddress().getHostName());
//			
			
			Socket hSocket= new Socket("8.8.8.8", 53);
			InetAddress addr=null;
			if(hSocket != null) {
				addr = hSocket.getLocalAddress();
			}
			try{
				servSocket= new ServerSocket(tcpPort);
			}
			catch(java.net.BindException e){
				System.out.println("Binding to "+tcpPort+" Port is failed because port is already in use or not allowed to bind i.e. permission denied");
				System.exit(0);
			}
			try{
				servUDPSocket= new DatagramSocket(udpPort);
			}
			catch(java.net.BindException e){
				System.out.println("Binding to "+udpPort+" Port is failed because port is already in use or not allowed to bind i.e. permission denied");
				System.exit(0);
			}

			//hostDetails= InetAddress.getLocalHost();
			hostDetails=addr;
			new InputThread(servSocket,servUDPSocket,hostDetails,clientListSockets);
			new udpThread(servUDPSocket);
			acceptIncomingRequests(servSocket);
		}

		catch(NumberFormatException e){
			System.out.println("Please Enter Valid Port Numbers");
		}

	}
}

