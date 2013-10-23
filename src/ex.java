public class ex
{
	public static void main(String []args)
	{
		String dir = System.getProperty("user.dir");
		System.out.println(dir);
//		char dirArr[] = dir.toCharArray();
//		 int len = dirArr.length;
//		 char[] filePathArr = new char[len+2];
//		 filePathArr = dirArr;
//		 filePathArr[len] = '\\';
//		String filePath = filePathArr.toString();
//		System.out.println(filePath);
		String fileName = "sample.txt";
		String fullPath = dir+"\\"+fileName;
		System.out.println(fullPath);
	}
}