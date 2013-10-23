package com.code;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.UUID;
import java.util.Map.Entry;


class PingTable {
	String messageID;
	Socket sock;
	PingTable(String messageID, Socket sock){
	this.messageID=messageID;
	this.sock=sock;
	}

}
class PongTable{
	String ip;
	int port;
	int fileCount;
	int fileSize;
	Socket sock;
	PongTable(String ip,int port,int fileCount,int fileSize,Socket sock){
	this.ip=ip;
	this.port=port;
	this.fileCount=fileCount;
	this.fileSize=fileSize;
	this.sock=sock;
	}

}

class queryHitTable{
	String ip;
	int httpPort;
	int fileSize;
	int index;
	String fileName;
	String messageID;
	queryHitTable(String ip, int httpPort, int index,String fileName,int fileSize,String messageID){
	this.ip=ip;
	this.httpPort=httpPort;
	this.fileSize=fileSize;
	this.index=index;
	this.fileName=fileName;
	this.messageID=messageID;

	}
}

class queryTable{
	String messageID;
	Socket sock;
	queryTable(String messageID, Socket sock){
	this.messageID=messageID;
	this.sock=sock;
	}
}

class connectedHostDetails{
	Socket sock;
	int packsSent;
	int packsReceived;
	int bytesSent;
	int bytesReceived;
	int hostType;//0 for client 1 for server
	//String clientRealIP;
	connectedHostDetails(Socket sock, int packSent,int packsReceived,int bytesSent, int bytesReceived,int hostType/*,String clientRealIP*/){
	this.sock=sock;
	this.bytesReceived=bytesReceived;
	this.bytesSent=bytesSent;
	this.packsReceived=packsReceived;
	this.packsSent=packSent;
	this.hostType=hostType;
	//this.clientRealIP=clientRealIP;
	}
}

class downloadList{
	String fName;
	Socket sock;
	int cSize;
	int fSize;
	downloadList(Socket sock,String fName,int cSize,int fSize){
	this.sock=sock;
	this.fName=fName;
	this.cSize=cSize;
	this.fSize=fSize;
	}
}



class GlobalVars{
	static int tcpPort;
	static int httpPort;
	static byte ipAddr[]= new byte[4];
	static String realIP;
	static String filePath;
	static String wFilePath;//path in which code is executing
	static byte[] serventID= new byte[16];
	static List<PingTable> pingList= new ArrayList<PingTable>();
	static List<downloadList> dList= new ArrayList<downloadList>();
	static List<connectedHostDetails> listOfHostsConnected= new ArrayList<connectedHostDetails>();
	//static List<connectedHostDetails> listOfServerConnected= new ArrayList<connectedHostDetails>();
	//static List<connectedHostDetails> clientListSockets=new ArrayList<connectedHostDetails>();
	static List<queryTable> queryList= new ArrayList<queryTable>();
	static List<PongTable> pongList= new ArrayList<PongTable>();
	static List<queryHitTable> queryHitList= new ArrayList<queryHitTable>();
	static List<queryHitTable> tempQueryHitList= new ArrayList<queryHitTable>();
	static int fileCount;
	static HashMap<Integer, String[]> filesIndex = new HashMap<Integer,String[]>();
	static int fileSize;
	static int bFlag=0;
	static int responseCounter=0;
	static int mFlag=0;//monitor flag
	static int incomingQueryCounter=0;
	static int iQueryHitListSize=0;
	static int qlimitExceedFlag=0;
	static int fEmptyStringFindFlag=0;//find flag(empty space)
	static int 	 inConnCOunter=0;
	static int outConnCounter=0;
	//static List
}

class HostThread extends Thread{

	public  Socket sock;
	HostThread(){
	super("Host Thread");
	}
	HostThread(Socket cSocket){
	sock=cSocket;
	start();
	}

	public static void forwardQueryMessage(String messageID, Socket sock, byte message[],int qPayLoadLength) throws IOException{
	//forwarding to servers
	for(int i=0;i<GlobalVars.listOfHostsConnected.size();i++){
	if(GlobalVars.listOfHostsConnected.get(i).sock!= sock){
	System.out.println("Writing Query to Server");
	OutputStream oStream= GlobalVars.listOfHostsConnected.get(i).sock.getOutputStream();
	GlobalVars.listOfHostsConnected.get(i).bytesSent+=qPayLoadLength+23;
	GlobalVars.listOfHostsConnected.get(i).packsSent+=1;

	oStream.write(message);
	oStream.flush();
	}
	}

	//forwarding to clients
	//	for(int i=0;i<GlobalVars.clientListSockets.size();i++){
	//	if(GlobalVars.clientListSockets.get(i).sock!= sock){
	//	System.out.println("Writing Query to Client");
	//	OutputStream oStream= GlobalVars.clientListSockets.get(i).sock.getOutputStream();
	//	GlobalVars.clientListSockets.get(i).bytesSent+=23+qPayLoadLength;
	//	GlobalVars.clientListSockets.get(i).packsSent+=1;
	//	oStream.write(message);
	//	oStream.flush();
	//	}
	//	}


	}


	public static byte[] buildQueryHitHeader(HashMap<Integer, String[]> matchedFiles,byte message[] ){
	byte b[]= new byte[4096];

	int payLoadLength=0;
	//copying first initial 15 bytes to pong 
	for(int i=0;i<16;i++)
	b[i]=message[i];

	b[16]=(byte)0x81;
	b[17]=(byte) (message[18] +2);
	b[18]=0;

	b[23]=(byte)matchedFiles.size();

	byte t[]= new byte[4];
	t = ByteBuffer.allocate(4).putInt(GlobalVars.httpPort).array();
	b[24]=t[2];//port
	b[25]=t[3];//port

	t=GlobalVars.ipAddr;

	b[26]=t[0];//ip
	b[27]=t[1];//ip
	b[28]=t[2];//ip
	b[29]=t[3];//ip

	//speed
	t=ByteBuffer.allocate(4).putInt(10000).array();
	b[30]=t[0];
	b[31]=t[1];
	b[32]=t[2];
	b[33]=t[3];
	//from 11th byte

	payLoadLength=11+matchedFiles.size()*8;
	int temp=0,temp1=0;

	Iterator<Entry<Integer,String[]>> itr= matchedFiles.entrySet().iterator();
	int tempLen=0;//for calculating filename bytes length for payload calculation
	//temp1=0;
	while(itr.hasNext()){
	temp1=0;
	Entry<Integer, String[]> me = itr.next();
	t = ByteBuffer.allocate(4).putInt(Integer.parseInt(me.getValue()[2])).array();//me.getKey()/*.intValue()*/).array();
	b[34+temp]=t[0];
	temp1++;
	b[35+temp]=t[1];
	temp1++;
	b[36+temp]=t[2];
	temp1++;
	b[37+temp]=t[3];
	temp1++;
	t=ByteBuffer.allocate(4).putInt(Integer.parseInt(me.getValue()[1])).array();
	b[38+temp]=t[0];
	temp1++;
	b[39+temp]=t[1];
	temp1++;
	b[40+temp]=t[2];
	temp1++;
	b[41+temp]=t[3];
	temp1++;
	int len= me.getValue()[0].concat("\0").getBytes().length;
	//tempLen+=len;
	temp1+=len;
	byte xyz[]=new byte[len];
	xyz=me.getValue()[0].concat("\0").getBytes();
	for(int i=0;i<xyz.length;i++){
	b[42+temp+i]=xyz[i];
	}
	//temp1+=
	temp+=temp1;

	}
	//payloadlength before adding 16 byte servent ID
	payLoadLength+=tempLen;

	//adding serventID to query hit message
	//	for(int k=0;k<GlobalVars.serventID.length;k++){
	//	b[22+payLoadLength+k]=GlobalVars.serventID[k];
	//
	//	}
	//	payLoadLength+=16;

	t = ByteBuffer.allocate(4).putInt(payLoadLength).array();
	//payload length

	b[19]=t[0];
	b[20]=t[1];
	b[21]=t[2];
	b[22]=t[3];


	return b;

	}




	public static void forwardPingMessage(String messageID,Socket sock,byte message[]) throws IOException{

	//forwarding to servers
	for(int i=0;i<GlobalVars.listOfHostsConnected.size();i++){
	if(GlobalVars.listOfHostsConnected.get(i).sock!= sock){
	System.out.println("Writing Ping to Server");
	OutputStream oStream= GlobalVars.listOfHostsConnected.get(i).sock.getOutputStream();
	GlobalVars.listOfHostsConnected.get(i).packsSent++;
	GlobalVars.listOfHostsConnected.get(i).bytesSent+=23;
	oStream.write(message);
	oStream.flush();
	}
	}

	//forwarding to clients
	//	for(int i=0;i<GlobalVars.clientListSockets.size();i++){
	//	if(GlobalVars.clientListSockets.get(i).sock!= sock){
	//	System.out.println("Writing Ping to Client");
	//	OutputStream oStream= GlobalVars.clientListSockets.get(i).sock.getOutputStream();
	//	GlobalVars.clientListSockets.get(i).packsSent++;
	//	GlobalVars.clientListSockets.get(i).bytesSent+=23;
	//	oStream.write(message);
	//	oStream.flush();
	//	}
	//	}
	}



	public static int[] getFilesLists(String dir)
	{
	try{
	int filesData[] = new int[2];//0-total size,1-num of files
	int tempFilesData[] = new int[2];
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

	return filesData;	
	}catch(NullPointerException e){

	}
	return null;
	}	











	public static void sendPongMessage(byte message[],String messageID,Socket sock) throws IOException{


	int filesData[] = new int[2];
	if(GlobalVars.filePath!=null){
	filesData = getFilesLists(GlobalVars.filePath);
	//System.out.println("Scanned "+filesData[1]+ "files and "+filesData[0]+" bytes");
	GlobalVars.fileSize=filesData[0]/1024;
	GlobalVars.fileCount=filesData[1];
	}
	else{
	GlobalVars.fileSize=filesData[0]/1024;
	GlobalVars.fileCount=filesData[1];
	}

	byte pong[]= new byte[4096];
	//copying first initial 15 bytes to pong 
	for(int i=0;i<16;i++)
	pong[i]=message[i];

	pong[16]=(byte) 0x01;
	pong[17]=(byte) (message[18]);//update TTL=number of hops in the PING message
	pong[18]=(byte) 0x00;//update Hops
	pong[19]=(byte)0x00;//PL
	pong[20]=(byte)0x00;//PL
	pong[21]=(byte)0x00;//PL
	pong[22]=(byte)0x0e;//PL
	byte t[]=new byte[4];
	t = ByteBuffer.allocate(4).putInt(GlobalVars.tcpPort).array();
	pong[23]=t[2];//port
	pong[24]=t[3];//port

	t=GlobalVars.ipAddr;

	pong[25]=t[0];//ip
	pong[26]=t[1];//ip
	pong[27]=t[2];//ip
	pong[28]=t[3];//ip
	t=ByteBuffer.allocate(4).putInt(GlobalVars.fileCount).array();
	pong[29]=t[0];//number of files shared
	pong[30]=t[1];//number of files shared
	pong[31]=t[2];//number of files shared
	pong[32]=t[3];//number of files shared
	t=ByteBuffer.allocate(4).putInt(GlobalVars.fileSize).array();
	pong[33]=t[0];//size of files shared
	pong[34]=t[1];//size of files shared
	pong[35]=t[2];//size of files shared
	pong[36]=t[3];//size of files shared

	//System.out.println("In Send Pong PingList size="+GlobalVars.pingList.size());
	//	for(int i=0;i<GlobalVars.pingList.size();i++){
	//	if(messageID.equalsIgnoreCase(GlobalVars.pingList.get(i).messageID) && sock==GlobalVars.pingList.get(i).sock){
	//	System.out.println("I am matched");
	//	OutputStream oStream= GlobalVars.pingList.get(i).sock.getOutputStream();
	//	oStream.write(pong);
	//	}
	//	}



	OutputStream oStream= sock.getOutputStream();
	oStream.write(pong);

	}


	public static void searchInShare(String fName,HashMap<Integer,String[]> matchedFiles){


	int counter = 0;
	//HashMap<Integer, String[]> matchedFiles = new HashMap<Integer, String[]>();
	Set<Entry<Integer, String[]>> set = GlobalVars.filesIndex.entrySet();
	Iterator<Entry<Integer, String[]>> i = set.iterator();
	//Prints key,value pairs
	//System.out.println("requested file name: "+fName);
	while(i.hasNext())
	{
	Entry<Integer, String[]> me = i.next();
	if(me.getValue()[0].toUpperCase().contains(fName.toUpperCase()) && !(matchedFiles.containsValue(me.getValue())))
	{
	counter++;
	//System.out.println("matched: "+me.getValue()[0]);
	matchedFiles.put(counter, me.getValue());
	}	
	}
	//System.out.println("counter: "+counter);
	//return matchedFiles; 
	}



	public static void handleMessage(byte[] message,Socket sock,int index) throws IOException, InterruptedException{
	String messageID="";
	message[17]--;
	message[18]++;
	for(int i=0;i<16;i++)
	messageID+= message[i];

	if(message[16]==0x00){
	//	if(sFlag==1){
	//	GlobalVars.listOfServerConnected.get(index).bytesReceived+=23;
	//	}
	//	else{
	//	GlobalVars.clientListSockets.get(index).bytesReceived+=23;
	//	}
	System.out.println("Received data is ping message");
	int alreadyThere=0;
	//checking ping messageID already exists
	System.out.println("");

	for(int i=0;i<GlobalVars.pingList.size();i++){
	if(messageID.equalsIgnoreCase(GlobalVars.pingList.get(i).messageID)){
	alreadyThere=1;
	}
	}
	if(alreadyThere==0){
	//removing first entry from pingtable if entries are more than/equal to 160
	if(GlobalVars.pingList.size()>=160){
	GlobalVars.pingList.remove(0);
	}

	GlobalVars.pingList.add(new PingTable(messageID,sock));


	}
	if(alreadyThere!=1){
	sendPongMessage(message,messageID,sock);

	//if(sFlag==1){
	GlobalVars.listOfHostsConnected.get(index).bytesSent+=37;
	GlobalVars.listOfHostsConnected.get(index).packsSent+=1;
	}
	//}
	//	else{
	//	GlobalVars.clientListSockets.get(index).bytesSent+=37;
	//	GlobalVars.clientListSockets.get(index).packsSent+=1;
	//	}


	System.out.println("Printing out the Ping Table Message IDS");
	System.out.println("PingList Size: "+GlobalVars.pingList.size());
	for(int i=0;i<GlobalVars.pingList.size();i++){
	System.out.println(GlobalVars.pingList.get(i).messageID);
	}

	//	dont forward the ping packet if TTL=0
	if(message[17]!=0 && alreadyThere==0){
	forwardPingMessage(messageID,sock,message);
	}

	}
	else if(message[16]==0x01){
	//	if(sFlag==1){
	GlobalVars.listOfHostsConnected.get(index).bytesReceived+=37;
	//	}
	//	else{
	//	GlobalVars.clientListSockets.get(index).bytesReceived+=37;
	//	}
	System.out.println("Received data is pong message");
	for(int i=0;i<GlobalVars.pingList.size();i++){
	//if(messageID.equalsIgnoreCase(GlobalVars.pingList.get(i).messageID) && sock==GlobalVars.pingList.get(i).sock){
	if(messageID.equalsIgnoreCase(GlobalVars.pingList.get(i).messageID) && GlobalVars.pingList.get(i).sock==null){
	String ip="";
	int port, fileSize,fileCount;
	ip=(message[25]& 0xff) +"."+(message[26] & 0xff)+"."+(message[27]& 0xff)+"."+(message[28]& 0xff);
	port= (message[23] & 0xff)<<8 | message[24] & 0xff;
	fileCount=(message[29] & 0xff)<<24 | (message[30] & 0xff)<<16 | (message[31] & 0xff)<<8 |(message[32] & 0xff); 
	fileSize=(message[33] & 0xff)<<24 | (message[34] & 0xff)<<16 | (message[35] & 0xff)<<8 |(message[36] & 0xff);
	System.out.println(ip);
	System.out.println(port);


	int flag=0;
	for(int j=0;j<GlobalVars.pongList.size();j++){
	if(GlobalVars.pongList.get(j).port==port && GlobalVars.pongList.get(j).ip.equalsIgnoreCase(ip)){
	GlobalVars.pongList.remove(j);
	GlobalVars.pongList.add(new PongTable(ip, port, fileCount,fileSize,sock));
	flag=1;
	}
	}
	if(flag==0){
	GlobalVars.pongList.add(new PongTable(ip, port, fileCount,fileSize,sock));
	}
	System.out.println("Printing out the pong table");
	for(int j=0;j<GlobalVars.pongList.size();j++){
	//	System.out.println("IP= "+GlobalVars.pongList.get(j).ip +" Port= "+GlobalVars.pongList.get(j).port+" Files Shared= "+GlobalVars.fileCount +" Memory Shared= "+GlobalVars.fileSize +"KB");
	System.out.println("IP= "+GlobalVars.pongList.get(j).ip +" Port= "+GlobalVars.pongList.get(j).port+" Files Shared= "+GlobalVars.pongList.get(j).fileCount +" Memory Shared= "+GlobalVars.pongList.get(j).fileSize +"KB");
	}

	}
	else if(messageID.equalsIgnoreCase(GlobalVars.pingList.get(i).messageID) ){//&& sock/*!*/==GlobalVars.pingList.get(i).sock){
	OutputStream oStream=GlobalVars.pingList.get(i).sock.getOutputStream();
	//	if(sFlag==1){
	GlobalVars.listOfHostsConnected.get(index).bytesSent+=23;
	GlobalVars.listOfHostsConnected.get(index).packsSent+=1;
	//	}
	//	else{
	//	GlobalVars.clientListSockets.get(index).bytesSent+=23;
	//	GlobalVars.clientListSockets.get(index).packsSent+=1;
	//	}
	oStream.write(message);

	}
	}
	}
	else if(message[16]==(byte)0x80){
	System.out.println("Received a query message");
	GlobalVars.incomingQueryCounter++;
	HashMap<Integer, String[]> matchedFiles = new HashMap<Integer, String[]>();


	int alreadyThere=0;
	String searchString;
	int qPayLoadLength;
	//checking query messageID already exists
	System.out.println("");
	//	int flag=0;
	//	for(int i=0;i<GlobalVars.queryList.size();i++){
	//	if(messageID.equalsIgnoreCase(GlobalVars.queryList.get(i).messageID) && GlobalVars.queryList.get(i).sock==null){
	//	flag=1;
	//	}
	//	}
	//	if(flag==0){
	for(int i=0;i<GlobalVars.queryList.size();i++){
	if(messageID.equalsIgnoreCase(GlobalVars.queryList.get(i).messageID)){
	alreadyThere=1;
	}
	}

	if(alreadyThere==0){
	GlobalVars.queryList.add(new queryTable(messageID,sock));
	}
	qPayLoadLength=(message[19] & 0xff)<<24 |(message[20] & 0xff)<<16 |(message[21] & 0xff)<<8 |(message[22] & 0xff) ;
	//sendPongMessage(message,messageID,sock);
	//	dont forward the query  packet if TTL=0
	if(message[17]!=0 && alreadyThere==0){
	forwardQueryMessage(messageID,sock,message,qPayLoadLength);
	}

	//	}


	//	if(sFlag==1){
	GlobalVars.listOfHostsConnected.get(index).bytesReceived+=23+qPayLoadLength;
	//	}
	//	else{
	//	GlobalVars.clientListSockets.get(index).bytesReceived+=23+qPayLoadLength;
	//	}
	byte searchStr[]= new byte[qPayLoadLength-2];
	//System.out.println("Payload Length:"+qPayLoadLength);

	for(int i=25;i<25+qPayLoadLength-2;i++){
	searchStr[i-25]=message[i];
	}

	searchString= new String(searchStr);
	if(GlobalVars.mFlag==1){
	System.out.println("Search: '"+searchString+"'");
	}
	if(searchString.length()!=0){
	String tempSearch[]= searchString.split(" ");
	for(int i=0;i<tempSearch.length;i++){

	//matchedFiles.putAll(searchInShare(tempSearch[i]));
	searchInShare(tempSearch[i],matchedFiles);


	}

	}
	else{

	matchedFiles=GlobalVars.filesIndex;


	}

	if(matchedFiles.size()>0){
	GlobalVars.responseCounter++;
	//System.out.println("Sending the list");
	//buildQueryHitHeader(matchedFiles,message);
	OutputStream oStream= sock.getOutputStream();
	//	if(sFlag==1){
	GlobalVars.listOfHostsConnected.get(index).bytesSent+=23+qPayLoadLength;
	GlobalVars.listOfHostsConnected.get(index).packsSent+=1;
	//	}
	//	else{
	//	GlobalVars.clientListSockets.get(index).bytesSent+=23+qPayLoadLength;
	//	GlobalVars.clientListSockets.get(index).packsSent+=1;
	//	}
	oStream.write(buildQueryHitHeader(matchedFiles,message));
	}
	}
	else if(message[16]==(byte)0x81){
	System.out.println("Received data is Query Hit message");
	int qPayLoadLength=-1;
	for(int i=0;i<GlobalVars.queryList.size();i++){
	if(messageID.equalsIgnoreCase(GlobalVars.queryList.get(i).messageID) && GlobalVars.queryList.get(i).sock==null){

	String ip="",fileName="";
	int httpPort=0,fIndex=0,fileSize=0;
	int hits;
	int hitIndex=0;
	qPayLoadLength=(message[19] & 0xff)<<24 |(message[20] & 0xff)<<16 |(message[21] & 0xff)<<8 |(message[22] & 0xff) ;
	//	if(sFlag==1){
	GlobalVars.listOfHostsConnected.get(index).bytesReceived+=23+qPayLoadLength;
	//	}
	//	else{
	//	GlobalVars.clientListSockets.get(index).bytesReceived+=23+qPayLoadLength;
	//	}

	hits=message[23] &0xff;
	ip=(message[26]& 0xff) +"."+(message[27] & 0xff)+"."+(message[28]& 0xff)+"."+(message[29]& 0xff);
	httpPort= (message[24] & 0xff)<<8 | message[25] & 0xff;

	// code while waiting for se class
	int temp=0,temp1=0;
	System.out.println("hits="+hits);
	while(hitIndex<hits){

	fIndex=(message[34+temp] &0xff)<<24 | (message[35+temp] &0xff)<<16 | (message[36+temp] &0xff)<<8 |(message[37+temp] &0xff);
	temp1=temp1+4;
	fileSize=(message[38+temp] &0xff)<<24 | (message[39+temp] &0xff)<<16 | (message[40+temp] &0xff)<<8 |(message[41+temp] &0xff);
	temp1=temp1+4;

	int tempIndex= 42+temp;
	while(message[tempIndex]!=(byte)'\0'){
	tempIndex++;
	}

	byte f[]= new byte[tempIndex-42-temp];
	for(int j=42+temp;j<tempIndex;j++){
	f[j-(42+temp)]=message[j];

	}


	temp1+=f.length;
	fileName=new String(f);
	int flag=0;
	if(GlobalVars.queryHitList.size()>160){
	flag=1;
	}
	if(flag==1){
	GlobalVars.queryHitList.remove(0);
	}
	GlobalVars.tempQueryHitList.add(new queryHitTable(ip, httpPort, fIndex, fileName, fileSize,messageID));
	GlobalVars.queryHitList.add(new queryHitTable(ip, httpPort, fIndex, fileName, fileSize,messageID));
	//GlobalVars.tempQueryHitList.add(new queryHitTable(ip, httpPort, fIndex, fileName, fileSize,messageID));
	//GlobalVars.iQueryHitListSize=GlobalVars.queryHitList.size();


	hitIndex++;
	//temp=temp1+1;//in doubt;
	temp=temp1+hitIndex;
	}

	//	System.out.println("Printing out the Query Hit table");
	//	for(int j=0;j<GlobalVars.queryHitList.size();j++){
	//	System.out.println("IP= "+GlobalVars.queryHitList.get(j).ip +" HTTP Port= "+GlobalVars.queryHitList.get(j).httpPort+" File Index= "+GlobalVars.queryHitList.get(j).index +" FileName= "+GlobalVars.queryHitList.get(j).fileName +" FileSize= "+GlobalVars.queryHitList.get(j).fileSize);
	//	//	System.out.println("IP= "+GlobalVars.queryHitList.get(j).ip +" Port= "+GlobalVars.pongList.get(j).port+" Files Shared= "+GlobalVars.pongList.get(j).fileCount +" Memory Shared= "+GlobalVars.pongList.get(j).fileSize +"KB");
	//	}

	}
	else if(messageID.equalsIgnoreCase(GlobalVars.queryList.get(i).messageID) ){//&& sock/*!*/==GlobalVars.pingList.get(i).sock){
	OutputStream oStream=GlobalVars.queryList.get(i).sock.getOutputStream();
	//	if(sFlag==1){
	GlobalVars.listOfHostsConnected.get(index).bytesSent+=23+qPayLoadLength;
	GlobalVars.listOfHostsConnected.get(index).packsSent+=1;
	//	}
	//	else{
	//	GlobalVars.clientListSockets.get(index).bytesSent+=23+qPayLoadLength;
	//	GlobalVars.clientListSockets.get(index).packsSent+=1;
	//	}
	oStream.write(message);

	}
	}

	}
	}



	public void run(){
	try {
	int foundOut=0,index=-1,cFlag=0,sSize=-1,cSize=-1;
	//sSize=GlobalVars.listOfServerConnected.size();
	//cSize=GlobalVars.clientListSockets.size();
	while(true){

	InputStream inpFromHost= sock.getInputStream();
	byte b[]=new byte[4096];
	if(inpFromHost.read(b)!=-1){
	//inpFromHost.read(b);
	//	if(foundOut==0 || sSize!=GlobalVars.listOfServerConnected.size() || cSize!=GlobalVars.clientListSockets.size()){
	for(int i=0;i<GlobalVars.listOfHostsConnected.size();i++){
	if(GlobalVars.listOfHostsConnected.get(i).sock==sock){
	index=i;

	foundOut=1;
	sSize=GlobalVars.listOfHostsConnected.size();
	break;
	}
	}
	//	for(int i=0;i<GlobalVars.clientListSockets.size();i++){
	//	if(GlobalVars.clientListSockets.get(i).sock==sock){
	//	index=i;
	//	cFlag=1;
	//	foundOut=1;
	//	cSize=GlobalVars.clientListSockets.size();
	//	break;
	//	}
	//	}
	//	}
	if(foundOut==1){
	GlobalVars.listOfHostsConnected.get(index).packsReceived++;
	}
	//	else if((foundOut==1) && (cFlag==1)){
	//	GlobalVars.clientListSockets.get(index).packsReceived++;
	//
	//	}

	handleMessage(b,sock,index);

	}
	else
	break;
	}
	String xyz=sock.getInetAddress().getHostAddress();
	if(xyz.equalsIgnoreCase("127.0.0.1")){
	Socket hS = null;
	try {
	hS = new Socket("8.8.8.8",53);
	} catch (UnknownHostException e1) {

	e1.printStackTrace();
	} catch (IOException e1) {

	e1.printStackTrace();
	}
	InetAddress iA= hS.getLocalAddress();
	System.out.println("Connection from "+iA.getHostAddress()+" is lost");
	}
	else
	System.out.println("Connection from "+sock.getInetAddress().getHostAddress()+" is lost");

	//update the list

	for(int i=0;i<GlobalVars.pongList.size();i++){
	if(GlobalVars.pongList.get(i).sock==sock){
	GlobalVars.pongList.remove(i);
	}
	}
	for(int i=0;i<GlobalVars.listOfHostsConnected.size();i++){
	if(GlobalVars.listOfHostsConnected.get(i).sock==sock){
	GlobalVars.listOfHostsConnected.remove(i);
	}

	}


	}


	catch(java.net.SocketException e){
	String xyz=sock.getInetAddress().getHostAddress();
	if(xyz.equalsIgnoreCase("127.0.0.1")){
	Socket hS = null;
	try {
	hS = new Socket("8.8.8.8",53);
	} catch (UnknownHostException e1) {

	e1.printStackTrace();
	} catch (IOException e1) {

	e1.printStackTrace();
	}
	InetAddress iA= hS.getLocalAddress();
	System.out.println("Connection from "+iA.getHostAddress()+" is lost");
	}
	else
	System.out.println("Connection from "+sock.getInetAddress().getHostAddress()+" is lost");

	//update the list

	for(int i=0;i<GlobalVars.pongList.size();i++){
	if(GlobalVars.pongList.get(i).sock==sock){
	GlobalVars.pongList.remove(i);
	}
	}

	for(int i=0;i<GlobalVars.listOfHostsConnected.size();i++){
	if(GlobalVars.listOfHostsConnected.get(i).sock==sock){
	if(GlobalVars.listOfHostsConnected.get(i).hostType==0){
	GlobalVars.inConnCOunter--;
	}
	else{
	GlobalVars.outConnCounter--;
	}
	GlobalVars.listOfHostsConnected.remove(i);
	}


	}


	}
	//	for(int i=0;i<GlobalVars.clientListSockets.size();i++){
	//	if(GlobalVars.clientListSockets.get(i).sock==sock){
	//	GlobalVars.clientListSockets.remove(i);
	//	}
	//
	//	}
 catch (Exception e) {
	// TODO Auto-generated catch block
	//	e.printStackTrace();
	}


	System.out.println("Simpella:>");	
	}

}



class qHitResultThread extends Thread{

	public qHitResultThread() {

	// TODO Auto-generated constructor stub
	start();
	}


	//	public void run(){
	//	//int flag=0;
	//	int temp1=GlobalVars.tempQueryHitList.size();
	//
	//	while(GlobalVars.bFlag!=1){
	//	int temp=GlobalVars.tempQueryHitList.size();	
	//	if(temp1!=temp){
	//	System.out.println(GlobalVars.tempQueryHitList.size() +" responses received");
	//	temp1=temp;//=GlobalVars.tempQueryHitList.size();
	//	}
	//	}
	//	}

	public void run(){
	//int flag=0;
	//int temp1=0;//GlobalVars.tempQueryHitList.size();
	int temp1=GlobalVars.tempQueryHitList.size();
	while(GlobalVars.bFlag!=1){
	int temp=GlobalVars.tempQueryHitList.size();	
	if(temp1!=temp){
	System.out.println(GlobalVars.tempQueryHitList.size() +" responses received");
	System.out.flush();
	temp1=temp;//=GlobalVars.tempQueryHitList.size();
	}
	}
	}

}



class HTTPThread extends Thread{
	Thread t;

	String IP;
	int httpPort;
	String fName;
	int fIndex;
	int fSize;
	HTTPThread(){
	super("http thread");
	}
	HTTPThread(String IP,int httpPort,String fName,int fIndex,int fSize) throws Exception{
	this.IP=IP;
	this.httpPort=httpPort;
	this.fName=fName;
	this.fIndex=fIndex;
	this.fSize=fSize;
	start();

	}
	public void run(){
	try {



	Socket httpSocket= new Socket(IP,httpPort);
	//BufferedReader inFServer= new BufferedReader(new InputStreamReader(httpSocket.getInputStream()));
	int cSize=0;
	GlobalVars.dList.add(new downloadList(httpSocket,fName, cSize, fSize));

	int actIndex=-1;
	for(int index=0;index<GlobalVars.dList.size();index++){
	if(GlobalVars.dList.get(index).fName==fName){
	actIndex=index;
	break;
	}

	}
	DataOutputStream outTServer= new DataOutputStream(httpSocket.getOutputStream());
	String fLine= "GET /get/"+fIndex+"/"+fName+" HTTP/1.1\r\n";
	String sLine="User-Agent: Simpella\r\n";
	String tLine="Host: "+IP+":"+httpPort+"\r\n";
	String foLine= "Connection: Keep-Alive\r\n";
	String fiLine="Range: bytes=0-\r\n";
	String siLine="\r\n";
	System.out.println(fLine+siLine+tLine+foLine+fiLine+siLine);
	outTServer.writeBytes(fLine+siLine+tLine+foLine+fiLine+siLine);
	byte data[]= new byte[1024];
	BufferedReader inFServer= new BufferedReader(new InputStreamReader(httpSocket.getInputStream()));
	String fResponse= inFServer.readLine();
	String fRep[]=fResponse.split(" ");
	System.out.println(fResponse);

	if(fRep[1].equalsIgnoreCase("200")){
	inFServer.readLine();
	inFServer.readLine();
	inFServer.readLine();
	inFServer.readLine();


	//String filePath= GlobalVars.filePath+"\\"+fName;
	String filePath="";
	//	if(GlobalVars.filePath!=null){
	//	filePath= GlobalVars.filePath+"/"+fName;
	//	}
	//	else{
	filePath= GlobalVars.wFilePath+"/~"+fName;	
	//	}
	File f1= new File(filePath);
	System.out.println("Temporary File= "+filePath);
	//	File download= new File(filePath);
	//	BufferedReader inFServer= new BufferedReader(new InputStreamReader(httpSocket.getInputStream()));
	//	String fResponse= inFServer.readLine();
	//	String fRep[]=fResponse.split(" ");
	//	if(fRep[1].equalsIgnoreCase("200")){
	//	inFServer.readLine();
	//	inFServer.readLine();
	//	inFServer.readLine();
	//	inFServer.readLine();


	FileOutputStream fos= new FileOutputStream(filePath);
	InputStream is= httpSocket.getInputStream();

	int count,cntr=0;
	while ((count = is.read(data)) >=0) {
	//	System.out.println(cntr);
	//	cntr++;
	GlobalVars.dList.get(actIndex).cSize+=count;
	fos.write(data, 0, count);
	fos.flush();
	}
	//System.out.println("hai");
	fos.close();
	System.out.flush();
	System.out.println("Downloaded Successfully");
	System.out.flush();
	
	
	
	
	File f2 = new File(GlobalVars.wFilePath+"/"+fName);
	if(f2.exists()){
	f2.delete();	
	}
	f1.renameTo(f1);
	if(GlobalVars.filePath!=null)
	{
	File fshared = new File (GlobalVars.filePath+"/"+fName);
	if (fshared.exists())
	{
	fshared.delete();
	}
	fshared.createNewFile();
	FileInputStream finsh= new FileInputStream(f2);
	FileOutputStream fsh=new FileOutputStream(fshared);
	byte [] mybytearraycopy = new byte [1024]; 
	int bytes;
	while((bytes=finsh.read(mybytearraycopy)) >= 0)
	{
	fsh.write(mybytearraycopy, 0, bytes);
	}
	finsh.close();
	//fsh.close();
	}
	
	
	
//	File f1= new File(filePath);
//	File f2= new File(GlobalVars.wFilePath+"/"+fName);
//	f1.renameTo(f2);
//
//	if(GlobalVars.filePath!=null){
//	File f3= new File(GlobalVars.filePath+"/"+fName);
//	FileChannel src = new FileInputStream(f2).getChannel();
//	FileChannel dest = new FileOutputStream(f3).getChannel();
//	dest.transferFrom(src, 0, src.size());
//
//	}
	}
	else{
	System.out.println("FILE NOT FOUND");
	}
	httpSocket.close();
	//System.out.println("I am at the end of http thread");
	System.out.flush();
	GlobalVars.dList.remove(actIndex);

	} catch (UnknownHostException e) {

	//e.printStackTrace();
	} catch (IOException e) {

	//e.printStackTrace();
	}
	}
}


class InputThread extends Thread{
	Thread t;
	String input[];
	public static ServerSocket servSocket;	
	public static InetAddress hostDetail;
	BufferedReader br= new BufferedReader(new InputStreamReader(System.in));
	InputThread(){
	super("input thread");
	}
	InputThread(ServerSocket servSock,InetAddress hostDetails){

	servSocket=servSock;
	hostDetail=hostDetails;
	start();
	}
	@Override
	public void run() {
	try {
	while(true){
	System.out.print("simpella:>");
	input=br.readLine().trim().split("\\ ");

	processInput(input,servSocket,hostDetail);//,clientSockets);
	}
	} catch (Exception e) {
	e.printStackTrace();
	}
	}



	public static byte[] buildPingHeader(){
	String messageID="";
	byte header[]= new byte[4096];
	UUID uuid= UUID.randomUUID();
	String id= uuid.toString().replaceAll("-","");
	char idArray[]=new char[32];
	idArray=id.toCharArray();
	//copying first 8 bytes of headers with uuid first 8 characters
	for(int len=0;len<8;len++){
	header[len]=(byte)idArray[len];
	}
	//setting 8th byte to all 1's
	header[8]=(byte)0xff;

	//copying 9 to 14 bytes with uuid next 6 characters 
	for(int len=8;len<14;len++){
	header[len+1]= (byte)idArray[len];
	}
	//setting 15th byte to 0
	header[15]=0x00;
	header[16]=0x00;
	int ttl=7; 
	int hops=0;
	header[17]=(byte)ttl;
	header[18]=(byte)hops;
	header[19]=0;
	header[20]=0;
	header[21]=0;
	header[22]=0;


	for(int i=0;i<16;i++){
	messageID+=header[i];
	}
	//System.out.println("Ping Value= " +messageID);
	//	GlobalVars.pingList.add(new PingTable(messageID, sock));

	return header;

	}

	public static void sendPings() throws  Exception{
	//sleep(2000);
	OutputStream outToServer,outToClient;

	String messageID="";
	byte b[]= new byte[4096]; 
	b= buildPingHeader();
	for(int i=0;i<16;i++)
	messageID+=b[i];

	GlobalVars.pingList.add(new PingTable(messageID, null));
	//sending pings to all connected servers
	for(int len=0;len<GlobalVars.listOfHostsConnected.size();len++){

	outToServer= GlobalVars.listOfHostsConnected.get(len).sock.getOutputStream();
	GlobalVars.listOfHostsConnected.get(len).packsSent+=1;
	GlobalVars.listOfHostsConnected.get(len).bytesSent+=23;
	outToServer.write(b);
	outToServer.flush();

	}

	//sending pings to all connected clients

	//	for(int i=0;i<GlobalVars.clientListSockets.size();i++){
	//	outToClient= GlobalVars.clientListSockets.get(i).sock.getOutputStream();
	//	GlobalVars.clientListSockets.get(i).bytesSent+=23;
	//	GlobalVars.clientListSockets.get(i).packsSent+=1;
	//	outToClient.write(b);//buildPingHeader());
	//	outToClient.flush();
	//
	//	}
	}

	public static byte [] buildQueryHeader(String query){

	byte temp[]=query.getBytes();

	byte t[]= new byte[4];
	int qpayloadLength=2+temp.length; 
	byte header[]= new byte[4096];
	UUID uuid= UUID.randomUUID();
	String id= uuid.toString().replaceAll("-","");
	char idArray[]=new char[32];
	idArray=id.toCharArray();
	//copying first 8 bytes of headers with uuid first 8 characters
	for(int len=0;len<8;len++){
	header[len]=(byte)idArray[len];
	}
	//setting 8th byte to all 1's
	header[8]=(byte)0xff;

	//copying 9 to 14 bytes with uuid next 6 characters 
	for(int len=8;len<14;len++){
	header[len+1]= (byte)idArray[len];
	}
	//setting 15th byte to 0
	header[15]=0x00;
	header[16]=(byte)0x80;
	int ttl=7; 
	int hops=0;
	if(GlobalVars.fEmptyStringFindFlag==0){
	header[17]=(byte)ttl;
	}
	else{
	header[17]=(byte)1;
	}
	header[18]=(byte)hops;
	t=ByteBuffer.allocate(4).putInt(qpayloadLength).array();
	header[19]=t[0];
	header[20]=t[1];
	header[21]=t[2];
	header[22]=t[3];
	header[23]=0;
	header[24]=0;
	for(int i=0;i<temp.length;i++){
	header[25+i]=temp[i];
	}
	if(temp.length>256){
	System.out.println("Query Message Length is greater than 256 bytes. Hence dropping the query message");
	GlobalVars.qlimitExceedFlag=1;

	}
	return header;
	}


	public static void sendQueries(String query) throws IOException{

	OutputStream outToServer,outToClient;
	//sending pings to all connected servers
	String messageID="";
	byte b[]= new byte[4096]; 
	b= buildQueryHeader(query);



	for(int i=0;i<16;i++)
	messageID+=b[i];
	if(GlobalVars.qlimitExceedFlag==0){
	GlobalVars.queryList.add(new queryTable(messageID, null));

	for(int len=0;len<GlobalVars.listOfHostsConnected.size();len++){

	outToServer= GlobalVars.listOfHostsConnected.get(len).sock.getOutputStream();
	GlobalVars.listOfHostsConnected.get(len).packsSent+=1;
	GlobalVars.listOfHostsConnected.get(len).bytesSent+=23+2+query.getBytes().length;
	outToServer.write(b);//buildPingHeader());
	outToServer.flush();
	//outToServer.close();
	}

	//sending queries to all connected clients

	//	for(int i=0;i<GlobalVars.clientListSockets.size();i++){
	//
	//	outToClient= GlobalVars.clientListSockets.get(i).sock.getOutputStream();
	//	GlobalVars.clientListSockets.get(i).packsSent+=1;
	//	GlobalVars.clientListSockets.get(i).bytesSent+=23+2+query.getBytes().length;
	//	outToClient.write(b);
	//	outToClient.flush();
	//
	//	}
	}
	}



	public static boolean checkForDuplicates(List<connectedHostDetails> serversList,String hostName,int portNo) throws UnknownHostException, Exception{

	int index=0;
	boolean flag=false;
	while(index!=serversList.size()){
	if(serversList.get(index).sock.getInetAddress().getHostAddress().equalsIgnoreCase("127.0.0.1")){
	Socket h= new Socket("8.8.8.8",53);
	InetAddress addr=h.getLocalAddress();
	if((addr.getHostAddress().equalsIgnoreCase(hostName)) && (serversList.get(index).sock.getPort()==portNo)){
	return true;
	}
	}
	flag=((serversList.get(index).sock.getInetAddress().getHostAddress().equalsIgnoreCase(hostName)||(("127.0.0.1").equalsIgnoreCase(hostName))||(serversList.get(index).sock.getInetAddress().getHostName().equalsIgnoreCase(hostName))) &&( (serversList.get(index).sock.getPort()==portNo)));
	if(flag){
	return true;
	}
	else{
	index++;
	}

	}
	return false;
	}

	public void open(String ipAddr, int port) throws Exception
	{ 
	try{
	if(GlobalVars.outConnCounter<=2){

	//InetAddress servIAddr= InetAddress.getByName(ipAddr);
	//Socket cSocket= new Socket(servIAddr,port);
	Socket cSocket= new Socket(ipAddr, port);

	DataOutputStream outToServer= new DataOutputStream(cSocket.getOutputStream());
	BufferedReader inFromServer= new BufferedReader(new InputStreamReader(cSocket.getInputStream()));
	outToServer.flush();
	outToServer.writeBytes("SIMPELLA CONNECT/0.6\r\n");
	String responseFServer[]= inFromServer.readLine().split("\\ ");

	String responseMessage="";
	for(int len=2;len<responseFServer.length;len++)
	responseMessage+=responseFServer[len]+" ";
	int responseCode=Integer.parseInt(responseFServer[1]);
	if(responseCode==200){
	System.out.println("Response From Server: " + responseMessage);
	String msg="THANKS FOR ACCEPTING ME";
	outToServer.writeBytes("SIMPELLA/0.6 200 "+msg +"\r\n");
	GlobalVars.outConnCounter++;
	outToServer.flush();
	//outToServer.close();

	GlobalVars.listOfHostsConnected.add(new connectedHostDetails(cSocket,0,0,0,0,1));

	//	sendPings();
	sleep(1000);
	OutputStream outToServers,outToClient;

	String messageID="";
	byte b[]= new byte[23]; 
	b= buildPingHeader();
	for(int i=0;i<16;i++)
	messageID+=b[i];

	GlobalVars.pingList.add(new PingTable(messageID, null));
	//sending pings to all connected servers
	for(int len=0;len<GlobalVars.listOfHostsConnected.size();len++){

	outToServers= GlobalVars.listOfHostsConnected.get(len).sock.getOutputStream();
	GlobalVars.listOfHostsConnected.get(len).bytesSent+=23;
	GlobalVars.listOfHostsConnected.get(len).packsSent+=1;

	outToServers.write(b);
	outToServers.flush();

	}

	//sending pings to all connected clients

	//	for(int i=0;i<GlobalVars.clientListSockets.size();i++){
	//	outToClient= GlobalVars.clientListSockets.get(i).sock.getOutputStream();
	//	GlobalVars.clientListSockets.get(i).packsSent+=1;
	//	GlobalVars.clientListSockets.get(i).bytesSent+=23;
	//	outToClient.write(b);//buildPingHeader());
	//	outToClient.flush();
	//
	//	}

	new HostThread(cSocket);
	}
	else if(responseCode==503){
	System.out.println("Response From Server: "+responseFServer[2]);
	}
	}
	else{
	System.out.println("Sorry Maximum Number of Outgoing Connections Limit reached\n");
	}
	}catch(java.net.UnknownHostException e){
	System.out.println("Please Enter Proper Host Name");
	System.exit(1);

	}
	catch(java.net.ConnectException e){
	if(ipAddr.equalsIgnoreCase("127.0.0.1")){
	System.out.println("No server is listening on this "+GlobalVars.realIP+":"+port);	
	}
	else{
	System.out.println("No server is listening on this "+ipAddr+":"+port);
	}

	System.exit(1);

	}
	}
	public static int[] getFilesLists(String dir){

	try{
	int filesData[] = new int[2];//0-total size,1-num of files
	int tempFilesData[] = new int[2];
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

	return filesData;	
	}catch(NullPointerException e){
	System.out.println("Please Enter Valid Directory");
	System.exit(1);

	}
	return null;
	}



	public static HashMap<Integer,String[]> getFilesList(String dir)
	{
	try{
	Random r= new Random();

	GlobalVars.filesIndex.clear();
	int counter = 0;
	String exactFileName,exactFileName1;
	System.out.println("directory: "+dir);
	File directory = new File(dir);
	File[] filesList = directory.listFiles();
	//HashMap<Integer, String> filesIndex = new HashMap<Integer,String>();
	for(int j=0;j<filesList.length;j++)
	{
	if(filesList[j].isFile())
	{
	String details[] = new String[3];
	counter++;
	exactFileName1 =filesList[j].getName();
	exactFileName = exactFileName1.replace(dir,"");
	details[0] = exactFileName;
	details[1] = Integer.toString((int)filesList[j].length());
	details[2] = Integer.toString(r.nextInt(45000));//Integer.toString((int)counter);
	GlobalVars.filesIndex.put(counter, details);
	}	
	}
	Set<Entry<Integer, String[]>> set = GlobalVars.filesIndex.entrySet();
	Iterator<Entry<Integer, String[]>> i = set.iterator();
	//Prints key,value pairs
	while(i.hasNext())
	{
	Entry<Integer, String[]> me = i.next();
	System.out.println(me.getKey()+"  "+me.getValue()[0]+" "+me.getValue()[2]);	
	}
	return GlobalVars.filesIndex;
	}catch(NullPointerException e){
	System.out.println("Please Enter Valid Directory");
	System.exit(1);
	}
	return null;

	}




	//	public static void download(int fIndex) throws Exception{
	//	for(int i=0;i<GlobalVars.queryHitList.size();i++){
	//	if(GlobalVars.queryHitList.get(i).index==fIndex){
	//	System.out.println("FileName= "+GlobalVars.queryHitList.get(i).fileName);
	//	new HTTPThread(GlobalVars.queryHitList.get(i).ip,GlobalVars.queryHitList.get(i).httpPort,GlobalVars.queryHitList.get(i).fileName,fIndex,GlobalVars.queryHitList.get(i).fileSize);
	//	}
	//	}
	//
	//	}

	public static void download(int fIndex) throws Exception{
	for(int i=0;i<GlobalVars.tempQueryHitList.size();i++){
	if(i+1 ==fIndex){

	System.out.println("FileName= "+GlobalVars.tempQueryHitList.get(i).fileName);
	new HTTPThread(GlobalVars.tempQueryHitList.get(i).ip,GlobalVars.tempQueryHitList.get(i).httpPort,GlobalVars.tempQueryHitList.get(i).fileName,GlobalVars.tempQueryHitList.get(i).index,GlobalVars.tempQueryHitList.get(i).fileSize);
	}
	}

	}





	String dir="";
	//public static HashMap<Integer, String> filesIndex = new HashMap<Integer,String>();
	public void processInput(String input[],ServerSocket servSocket,InetAddress hostDetails)/*,List<Socket> clientsSockets)*/ throws Exception{

	if(input[0].trim().length()==0);
	else if((input.length==1) && (input[0].equalsIgnoreCase("monitor"))){
	GlobalVars.mFlag=1;
	System.out.println("MONITORING SIMPELLA NETWORK");
	System.out.println("Press enter to continue");
	System.out.println("---------------------------");
	String breakPt=br.readLine();
	if(breakPt.equals("")){
	GlobalVars.mFlag=0;
	System.out.println("(enter pressed)");
	System.out.println("Simpella:>");
	}


	}
	else if(input.length==1 && input[0].equalsIgnoreCase("list")){
	System.out.println("List of Files returned by find");
	for(int i=0;i<GlobalVars.queryHitList.size();i++){
	System.out.println(/*GlobalVars.queryHitList.get(i).index*/(i+1)+"\t"+GlobalVars.queryHitList.get(i).fileName);

	}

	}
	else if(input.length==1 && input[0].equalsIgnoreCase("quit")){
	System.exit(1);

	}
	else if(((input.length==1) || (input.length==2)) && input[0].equalsIgnoreCase("clear")){
	try{
	if(input.length==2){
	int temp= Integer.parseInt(input[1]);
	for(int i=0;i<GlobalVars.queryHitList.size();i++){
	if(temp-1==i){
	GlobalVars.queryHitList.remove(i);
	}
	}
	}
	else{
	GlobalVars.queryHitList.clear();
	}
	}catch(NumberFormatException e)
	{
	System.out.println("Please Enter Valid Numbers only");
	}
	}
	else if((input.length==2) && (input[0].equalsIgnoreCase("open"))){
	String servDetails[]= input[1].split(":");
	if(servDetails.length==2){
	if((servDetails[0].equalsIgnoreCase("127.0.0.1")||(servDetails[0].equalsIgnoreCase(hostDetails.getHostAddress().toString())))&&(Integer.parseInt(servDetails[1])==servSocket.getLocalPort())){
	System.out.println("Self connection not allowed");
	}
	else{

	if(checkForDuplicates(GlobalVars.listOfHostsConnected, servDetails[0],Integer.parseInt(servDetails[1])))
	System.out.println("Duplicate Connections not allowed");
	else	
	open(servDetails[0],Integer.parseInt(servDetails[1]));

	}
	}
	else{
	System.out.println("Please Enter Valid Input");
	}
	}
	else if(input.length==1 && input[0].equalsIgnoreCase("update")){
	sendPings();
	}
	else if(input.length==2 && input[0].equalsIgnoreCase("share")){

	if(input[1].equalsIgnoreCase("-i")){
	//	dir = System.getProperty("user.dir");
	if(GlobalVars.filePath!=null){
	System.out.println("Sharing "+GlobalVars.filePath);
	}
	else
	System.out.println("None of the directory is shared.");
	}
	else if(!input[1].startsWith("/")){

	dir = System.getProperty("user.dir");
	dir+=input[1];
	System.out.println("Sharing "+dir);
	GlobalVars.filePath=dir;
	GlobalVars.filesIndex = getFilesList(dir);
	}
	else{
	dir=input[1];
	System.out.println("Sharing "+dir);
	GlobalVars.filePath=dir;
	GlobalVars.filesIndex = getFilesList(dir);

	}

	}
	else if(input.length==1 && input[0].equalsIgnoreCase("scan")){


	//System.out.println("Scanning  "+dir);
	if(GlobalVars.filePath==null){
	System.out.println("Please Share your directory before scanning");

	}
	else{
	System.out.println("Scanning  "+GlobalVars.filePath);
	int filesData[] = new int[2];
	filesData = getFilesLists(GlobalVars.filePath);

	int exp;
	int power;
	double quotient;
	power=(int)Math.log10(filesData[0]);
	exp=power;
	power=(int) Math.pow(10, power);
	quotient=filesData[0]/(double)power;
	System.out.println("Scanned "+filesData[1]+" files and "+quotient+"e+0"+exp+" bytes");
	//System.out.println("Scanned " "files and "+filesData[0]+" bytes");
	GlobalVars.filesIndex = getFilesList(GlobalVars.filePath);
	GlobalVars.fileSize=filesData[0]/1024;
	GlobalVars.fileCount=filesData[1];
	}

	}
	else if(input.length==2 && input[0].equalsIgnoreCase("info")){
	if(input[1].equalsIgnoreCase("c")){
	if(GlobalVars.listOfHostsConnected.size()!=0){
	System.out.println("CONNECTION STATS:");
	System.out.println("-----------------------");
	for(int i=0;i<GlobalVars.listOfHostsConnected.size();i++){
	String actIP="";//actual IP
	if(GlobalVars.listOfHostsConnected.get(i).sock.getInetAddress().getHostAddress().equalsIgnoreCase("127.0.0.1")){
	actIP=GlobalVars.realIP;
	}
	else{
	actIP=GlobalVars.listOfHostsConnected.get(i).sock.getInetAddress().getHostAddress();
	}

	System.out.println("   "+(i+1)+")"+actIP+":"+GlobalVars.listOfHostsConnected.get(i).sock.getPort()+"\t"+"Packs: "+GlobalVars.listOfHostsConnected.get(i).packsSent+":"+GlobalVars.listOfHostsConnected.get(i).packsReceived+"\t"+"Bytes: "+GlobalVars.listOfHostsConnected.get(i).bytesSent+":"+GlobalVars.listOfHostsConnected.get(i).bytesReceived);
	}
	//	int x=GlobalVars.listOfServerConnected.size();
	//	for(int i=0;i<GlobalVars.clientListSockets.size();i++){
	//	System.out.println("   "+(x+i+1)+")"+GlobalVars.clientListSockets.get(i).sock.getInetAddress().getHostAddress()+":"+GlobalVars.clientListSockets.get(i).sock.getPort()+"\t"+"Packs: "+GlobalVars.clientListSockets.get(i).packsSent+":"+GlobalVars.clientListSockets.get(i).packsReceived+"\t"+"Bytes: "+GlobalVars.clientListSockets.get(i).bytesSent+":"+GlobalVars.clientListSockets.get(i).bytesReceived);
	//	}
	}
	else{
	System.out.println("No Connections Available As Of Now");
	}

	}
	else if(input[1].equalsIgnoreCase("d")){

	if(GlobalVars.dList.size()==0){
	System.out.println("No Downloads As Of N");

	}
	else{
	System.out.println("DOWNLOAD STATS:");
	System.out.println("---------------");
	for(int index=0;index<GlobalVars.dList.size();index++){
	if(GlobalVars.dList.isEmpty()){
	//System.out.println("No Files");
	break;
	}
	else{

	System.out.println((index+1)+") "+GlobalVars.dList.get(index).sock.getInetAddress().getHostAddress()+":"+GlobalVars.dList.get(index).sock.getPort()+"\t"+((float)(GlobalVars.dList.get(index).cSize)/GlobalVars.dList.get(index).fSize)*100+"%"+"\t"+(GlobalVars.dList.get(index).cSize)+"/"+GlobalVars.dList.get(index).fSize);
	}

	}

	}
	}
	else if(input[1].equalsIgnoreCase("h")){
	int hosts=0;
	float fileSize=0,files=0;
	String fDenomination="",sizeDenomination="";
	hosts=GlobalVars.pongList.size();
	for(int i=0;i<GlobalVars.pongList.size();i++){
	files+=GlobalVars.pongList.get(i).fileCount;
	fileSize+=GlobalVars.pongList.get(i).fileSize;
	}
	if(files>=1000){
	fDenomination="K";
	files=files/1000;
	if(files>=1000){
	fDenomination="M";
	files=files/1000;
	}
	}

	if(fileSize>=1024){
	sizeDenomination="MB";
	fileSize=fileSize/1024;
	if(fileSize>=1024){
	sizeDenomination="GB";
	fileSize=fileSize/1024;
	}
	}

	System.out.println("HOST STATS:");
	System.out.println("------------");
	System.out.println("\tHosts: "+hosts+"\tFiles: "+files+fDenomination+"\tSize: "+fileSize+sizeDenomination);

	}
	else if(input[1].equalsIgnoreCase("n")){
	float MsgReceived=0,MsgSent=0,BytesRcvd=0,BytesSent=0,Guids=0;
	String MSgReceivedDenomination="",MsgSentDenomination="",BytesRcvdDenomination="",BytesSentDenomination="";

	for(int i=0;i<GlobalVars.listOfHostsConnected.size();i++){
	//System.out.println("   "+(i+1)+")"+GlobalVars.listOfServerConnected.get(i).sock.getInetAddress().getHostAddress()+":"+GlobalVars.listOfServerConnected.get(i).sock.getPort()+"\t"+"Packs: "+GlobalVars.listOfServerConnected.get(i).packsSent+":"+GlobalVars.listOfServerConnected.get(i).packsReceived+"\t"+"Bytes: "+GlobalVars.listOfServerConnected.get(i).bytesSent+":"+GlobalVars.listOfServerConnected.get(i).bytesReceived);
	MsgSent+=GlobalVars.listOfHostsConnected.get(i).packsSent;
	MsgReceived+=GlobalVars.listOfHostsConnected.get(i).packsReceived;
	BytesRcvd+=GlobalVars.listOfHostsConnected.get(i).bytesReceived;
	BytesSent+=GlobalVars.listOfHostsConnected.get(i).bytesSent;

	}
	//int x=GlobalVars.listOfServerConnected.size();
	//	for(int i=0;i<GlobalVars.clientListSockets.size();i++){
	//	//System.out.println("   "+(x+i+1)+")"+GlobalVars.clientListSockets.get(i).sock.getInetAddress().getHostAddress()+":"+GlobalVars.clientListSockets.get(i).sock.getPort()+"\t"+"Packs: "+GlobalVars.clientListSockets.get(i).packsSent+":"+GlobalVars.clientListSockets.get(i).packsReceived+"\t"+"Bytes: "+GlobalVars.clientListSockets.get(i).bytesSent+":"+GlobalVars.clientListSockets.get(i).bytesReceived);
	//	MsgSent+=GlobalVars.clientListSockets.get(i).packsSent;
	//	MsgReceived+=GlobalVars.clientListSockets.get(i).packsReceived;
	//	BytesRcvd+=GlobalVars.clientListSockets.get(i).bytesReceived;
	//	BytesSent+=GlobalVars.clientListSockets.get(i).bytesSent;
	//	}
	Guids=GlobalVars.pingList.size()+GlobalVars.queryList.size();

	if(MsgSent>1000){
	MsgSent=MsgSent/1000;
	MsgSentDenomination="k";

	}
	if(MsgReceived>1000){
	MsgReceived=MsgReceived/1000;
	MSgReceivedDenomination="k";

	}
	if(BytesRcvd>1024){
	BytesRcvd=BytesRcvd/1024;
	BytesRcvdDenomination="K";

	}
	if(BytesSent>1024){
	BytesSent=BytesSent/1024;
	BytesSentDenomination="K";
	}

	System.out.println("NET STATS:");
	System.out.println("----------");
	System.out.println("Msg Received: "+MsgReceived+MSgReceivedDenomination+"\t"+"Msg Sent: "+MsgSent+MsgSentDenomination);
	System.out.println("Unique GUIDs in memory: "+Guids);
	System.out.println("Bytes Rcvd: "+BytesRcvd+BytesRcvdDenomination+"\t"+"Bytes Sent: "+BytesSent+BytesSentDenomination);




	}
	else if(input[1].equalsIgnoreCase("q")){
	System.out.println("QUERY STATS");
	int queriesReceived=0;
	for(int x=0;x<GlobalVars.queryList.size();x++){
	if(GlobalVars.queryList.get(x).sock!=null){
	queriesReceived++;
	}

	}
	//System.out.println("Queries: "+GlobalVars.incomingQueryCounter+"\t"+"Responses Sent: "+GlobalVars.responseCounter);
	System.out.println("Queries: "+queriesReceived+"\t"+"Responses Sent: "+GlobalVars.responseCounter);

	}else if(input[1].equalsIgnoreCase("s")){
	float fSize= GlobalVars.fileSize;
	String fDenomination="KB";
	if(fSize>=1024){
	fSize=fSize/1024;
	fDenomination="MB";
	if(fSize>=1024){
	fSize=fSize/1024;
	fDenomination="GB";
	}
	}

	System.out.println("SHARE STATS:");
	System.out.println("-------------");
	System.out.println("\tNum Shared: "+GlobalVars.fileCount+"\tSize Shared: "+fSize+fDenomination);
	}
	else{
	System.out.println("Please Enter Valid Option For Info Command");
	}
	}
	else if(input.length==2 && input[0].equalsIgnoreCase("download")){
	try{
	//	if(GlobalVars.filePath==null){
	//	GlobalVars.filePath=System.getProperty("user.dir");
	//	//System.out.println("Please share your directory before downloading");
	//
	//	}
	//else{
	download(Integer.parseInt(input[1]));
	}catch(NumberFormatException e){
	System.out.println("Please Enter Valid File Index Numbers only");

	}
	//}

	}
	else if(input.length>=2 && input[0].equalsIgnoreCase("find")){
	GlobalVars.qlimitExceedFlag=0;
	GlobalVars.tempQueryHitList.clear();
	String fileName="";
	for(int index=1;index<input.length;index++){
	fileName+=input[index]+" ";
	}
	System.out.println("Searching Simpella network for '"+fileName.trim()+"'");

	sendQueries(fileName.trim());
	Thread t= new qHitResultThread();
	String breakPt=br.readLine();
	if(breakPt.equals("")){
	GlobalVars.bFlag=1;
	}
	GlobalVars.bFlag=0;
	sleep(200);
	System.out.println("---------------------------");
	System.out.println("The query was '"+fileName.trim()+"'");
	//	System.out.println("");
	sleep(300);

	for(int i=0;i<GlobalVars.tempQueryHitList.size();i++){
	float tempSize;
	String denomination="B";
	tempSize=GlobalVars.tempQueryHitList.get(i).fileSize;
	if(tempSize<1024){
	tempSize=GlobalVars.tempQueryHitList.get(i).fileSize;
	}
	else if(tempSize>1024){
	denomination="K";
	tempSize=tempSize/1024;
	if(tempSize>1024){
	denomination="M";
	tempSize=tempSize/1024;
	if(tempSize>1024){
	denomination="G";
	tempSize=tempSize/1024;
	}
	}

	}

	System.out.println(i+1+") "+GlobalVars.tempQueryHitList.get(i).ip+":"+GlobalVars.tempQueryHitList.get(i).httpPort+"\t"+"Size: "+tempSize+denomination);
	System.out.println("Name: "+GlobalVars.tempQueryHitList.get(i).fileName);
	System.out.println("");

	}


	//	GlobalVars.tempQueryHitList.clear();
	}
	else if(input.length==1 && input[0].equalsIgnoreCase("find")){
	GlobalVars.tempQueryHitList.clear();
	GlobalVars.fEmptyStringFindFlag=1;
	sendQueries("");


	//	System.out.println("The query was '"+fileName.trim()+"'");
	//	System.out.println("");
	sleep(300);
	System.out.println("---------------------------");
	for(int i=0;i<GlobalVars.tempQueryHitList.size();i++){
	float tempSize;
	String denomination="B";
	tempSize=GlobalVars.tempQueryHitList.get(i).fileSize;
	if(tempSize<1024){
	tempSize=GlobalVars.tempQueryHitList.get(i).fileSize;
	}
	else if(tempSize>1024){
	denomination="K";
	tempSize=tempSize/1024;
	if(tempSize>1024){
	denomination="M";
	tempSize=tempSize/1024;
	if(tempSize>1024){
	denomination="G";
	tempSize=tempSize/1024;
	}
	}

	}

	System.out.println(i+1+") "+GlobalVars.tempQueryHitList.get(i).ip+":"+GlobalVars.tempQueryHitList.get(i).httpPort+"\t"+"Size: "+tempSize+denomination);
	System.out.println("Name: "+GlobalVars.tempQueryHitList.get(i).fileName);
	System.out.println("");

	}


	GlobalVars.fEmptyStringFindFlag=0;

	}
	else
	System.out.println("Unknown Command");
	}

}


class clientHttpThread extends Thread{
	Thread t;
	Socket cSock;
	clientHttpThread(Socket cSock){
	this.cSock=cSock;
	start();
	}

	public void run(){
	int flag=0;
	while(true){
	try {
	BufferedReader br= new BufferedReader(new InputStreamReader(cSock.getInputStream()));
	DataOutputStream dStream= new DataOutputStream(cSock.getOutputStream());
	String inf[];
	String req=br.readLine();
	String fSize="";

	int fIndex;
	String fName;
	String tempString="";
	if(req.startsWith("GET")){
	System.out.println("Entered here\n");
	inf= req.split(" ");
	//	for(int i=1;i<inf.length-1;i++){
	//	//	System.out.println(inf[i]);
	//	tempString+=inf[i]+" ";
	//
	//	}
	//	tempString.trim();
	String fInf[]= inf[1].split("/");
	//	String fInf[]=tempString.split("/");

	for(int i=0;i<fInf.length;i++){
	System.out.println(fInf[i]+"  "+fInf[i].length());
	}
	fIndex=Integer.parseInt(fInf[2]);

	fName=fInf[3];

	Set<Entry<Integer, String[]>> set = GlobalVars.filesIndex.entrySet();
	Iterator<Entry<Integer, String[]>> i = set.iterator();
	//Prints key,value pairs
	while(i.hasNext())
	{
	Entry<Integer, String[]> me = i.next();
	//System.out.println(me.getKey()+"  "+me.getValue()[0]);
	int temp=Integer.parseInt(me.getValue()[2]);
	String tempFName= me.getValue()[0];
	fSize=me.getValue()[1];
	if(temp==fIndex/* && tempFName.equalsIgnoreCase(fName)*/){
	flag=1;
	break;

	}

	}
	if(flag==1){
	dStream.writeBytes("HTTP/1.1 200 OK\n");
	dStream.writeBytes("Server: Simpella0.6\r\n");
	dStream.writeBytes("Content-Type: application/binary\r\n");
	dStream.writeBytes("Content-Length: "+fSize+"\r\n");
	dStream.writeBytes("\r\n");

	//write file code

	//	File fileData = new File(GlobalVars.filePath+"\\"+fName);
	//	FileInputStream fis = new FileInputStream(GlobalVars.filePath+"\\"+fName);

	File fileData = new File(GlobalVars.filePath+"/"+fName);
	FileInputStream fis = new FileInputStream(GlobalVars.filePath+"/"+fName);

	OutputStream os = null;
	byte[] mybytearray = new byte[1024];
	fis = new FileInputStream(fileData);
	os = cSock.getOutputStream();

	int count;
	while ((count = fis.read(mybytearray)) >=0) {
	os.write(mybytearray, 0, count);
	os.flush();
	}
	os.flush();
	os.close();
	//cSock.close();
	}
	else{

	dStream.writeBytes("HTTP/1.1 503 File not found. \r\n");
	dStream.writeBytes("\r\n");
	}
	break;
	}
	}
	catch(NullPointerException e){

	}
	catch(java.net.SocketException e){


	}
	catch (IOException e) {
	//e.printStackTrace();
	}

	}
	}
}



class HTTPSThread extends Thread{
	Thread t;
	ServerSocket httpServSock;
	HTTPSThread(){
	super("HTTP Server Thread");
	}

	HTTPSThread(ServerSocket httpServSock){
	this.httpServSock=httpServSock;
	start();
	}

	public void run(){
	while(true){
	try {
	Socket cSock= httpServSock.accept();

	new clientHttpThread(cSock);

	} catch (IOException e) {

	e.printStackTrace();
	}
	}

	}
}





public class Simpella {
	//	public static List<Socket> clientListSockets=new ArrayList<Socket>();


	public static void acceptIncomingRequests(ServerSocket servSocket) throws IOException{
	//tracks no of input connections
	while(true){
	try{
	Socket clientSock=servSocket.accept();
	GlobalVars.inConnCOunter++;

	String ipAddr=clientSock.getInetAddress().getHostAddress();
	String clientRealIP=ipAddr;
	if(ipAddr.equalsIgnoreCase("127.0.0.1")){
	Socket hS= new Socket("8.8.8.8",53);
	InetAddress iA= hS.getLocalAddress();
	clientRealIP=iA.getHostAddress();

	System.out.println("got connection request from "+iA.getHostAddress());
	}
	else
	System.out.println("got connection request from "+clientSock.getInetAddress().getHostAddress());
	/*if(clientSock.isConnected()){
	System.out.println("Connection Successful");
	System.out.println("simpella:>");
	}*/
	BufferedReader inpFromClient= new BufferedReader(new InputStreamReader(clientSock.getInputStream()));

	String requestMessage[]=inpFromClient.readLine().split("\\ ");

	DataOutputStream outToClient= new DataOutputStream(clientSock.getOutputStream());
	outToClient.flush();
	//enters only if client sends  valid connection request message 
	if(requestMessage.length == 2  && requestMessage[0].equals("SIMPELLA") && requestMessage[1].equals("CONNECT/0.6")){

	//sending response code
	String responseString,statusCode;
	if(GlobalVars.inConnCOunter>3){
	statusCode="503";
	responseString="Maximum number of connections reached. Sorry!\r\n";
	//clientSock.close(); 
	}
	else{
	statusCode="200";
	responseString="Accepting the connection\r\n";
	System.out.println("Response to Client: "+responseString);
	GlobalVars.listOfHostsConnected.add(new connectedHostDetails(clientSock,0,0,0,0,0/*,clientRealIP*/));

	}

	String responseMessage="SIMPELLA/0.6 "+statusCode+" "+responseString ;
	outToClient.writeBytes(responseMessage);

	String reqMsg[]=inpFromClient.readLine().split(" ");
	String reqMessage="";
	for(int len=2;len<reqMsg.length;len++){
	reqMessage+=reqMsg[len]+" ";

	}
	System.out.println("Response From Client: "+reqMessage);
	outToClient.flush();
	//outToClient.close();
	if(statusCode.equalsIgnoreCase("200"))
	new HostThread(clientSock);

	}
	}catch(Exception e){


	}
	}
	}

	public static void buildServentID(){
	UUID uuid= UUID.randomUUID();
	String id= uuid.toString().replaceAll("-","").substring(0,16);
	GlobalVars.serventID=id.getBytes();
	System.out.println("ServentId= "+id);

	}



	public static ServerSocket servSocket;
	public static InetAddress hostDetails;
	public static ServerSocket httpServSocket;

	public static void main(String[] args) throws Exception{
	try{
	//	int tcpPort,httpPort;
	if(args.length==0){
	
	GlobalVars.tcpPort=6385;
	GlobalVars.httpPort=5635;
	}
	else{
	GlobalVars.tcpPort=Integer.parseInt(args[0]);
	GlobalVars.httpPort= Integer.parseInt(args[1]);
	if(GlobalVars.tcpPort>60000 || GlobalVars.httpPort>60000 ||GlobalVars.tcpPort<0 || GlobalVars.httpPort<0){
	System.out.println("Please Enter Valid Port Numbers which are greater than 0 and lesser than 60000");
	System.exit(0);
	}
	}
	//getting correct IP by connecting to google DNS server
	Socket hSocket= new Socket("8.8.8.8", 53);
	InetAddress addr=null;
	if(hSocket != null) {
	addr = hSocket.getLocalAddress();
	GlobalVars.realIP=addr.getHostAddress();


	}
	try{
	servSocket= new ServerSocket(GlobalVars.tcpPort);

	}
	catch(java.net.BindException e){
	System.out.println("Binding to "+GlobalVars.tcpPort+" Port is failed because port is already in use or not allowed to bind i.e. permission denied");
	System.exit(0);
	}
	try{
	httpServSocket= new ServerSocket(GlobalVars.httpPort);
	}catch(java.net.BindException e){
	System.out.println("Binding to "+GlobalVars.httpPort+" Port is failed because port is already in use or not allowed to bind i.e. permission denied");
	System.exit(0);
	}
	System.out.println("Local IP: "+GlobalVars.realIP);
	System.out.println("Simpella Net Port: "+GlobalVars.tcpPort);
	System.out.println("Downloading Port: "+GlobalVars.httpPort);
	System.out.println("Simpella Version 0.6 (c) 2012-2013");
	byte temp[]= new byte[4];
	temp= addr.getAddress();
	for(int i=0;i<temp.length;i++){
	GlobalVars.ipAddr[i]=temp[i];	
	}

	hostDetails=addr;
	buildServentID();
	GlobalVars.wFilePath=System.getProperty("user.dir");
	new InputThread(servSocket,hostDetails);
	new HTTPSThread(httpServSocket);
	acceptIncomingRequests(servSocket);
	}

	catch(NumberFormatException e){
	System.out.println("Please Enter Valid Port Numbers");
	}
	catch(ArrayIndexOutOfBoundsException e){
	System.out.println("Please provide both TCP and HTTP Port Numbers");
	}

	}	
}