package com.code;
import java.io.IOException;
import java.nio.ByteBuffer;
public class byteToInt
{
	public static void main(String []args) throws IOException, Exception
	{
		int port = 34344;
		byte num[] = new byte[4];
		num = ByteBuffer.allocate(4).putInt(port).array();
		int p = (num[2]&0xff)<<8;
		int q = num[3]&0xff;
		int newport = p+q;
		System.out.println(newport);
	}

}