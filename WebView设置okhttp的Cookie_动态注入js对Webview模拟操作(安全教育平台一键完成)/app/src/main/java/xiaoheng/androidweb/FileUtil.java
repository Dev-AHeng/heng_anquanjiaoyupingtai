package xiaoheng.androidweb;

import java.io.*;

/**
 * 个人工具类
 *
 * 封装于：2019.4.6
 * 作者：小亨
 */
 
public class FileUtil
{
	/**
	 * 创建文件并写入
	 * 
	 * 参数1 文件目录
	 * 参数2 文件名
	 * 参数3 写入内容
	 */
	public static void writeFile(String fileurl, String filename, String intxt)
	{
		File file = new File(fileurl);
		if (!file.isDirectory())
		{
			// 创建目录
			file.mkdir();
		}
		File fileDir = new File(file, filename);
		if (!fileDir.isFile())
		{
			try
			{
				fileDir.createNewFile();
			}
			catch (IOException e)
			{
				e.printStackTrace();
			}
		}
		try
		// FileWriter写入文件时不能指定编码格式，编码格式是系统默认的编码格式
		// Environment.getExternalStorageDirectory().getAbsolutePath() + "/htmlcode/loginhtmlcode.html";
		{
			FileWriter fw = new FileWriter(fileDir);
			// 向文件中写入字符串
			fw.write(intxt); 
			// 刷新
			fw.flush(); 
			// 关闭流
			fw.close(); 
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}


	/**
	 * 读取本地txt
	 * 
	 * 参数1 txt路径
	 */
	public static String readTxt(String fileurl)
	{
        String result=null;
        try
		{
			// Environment.getExternalStorageDirectory().getPath() + filename
            File f=new File(fileurl);
            int length=(int)f.length();
            byte[] buff=new byte[length];
            FileInputStream fin=new FileInputStream(f);
            fin.read(buff);
            fin.close();
            result = new String(buff, "UTF-8");
        }
		catch (Exception e)
		{
            e.printStackTrace();
			System.out.println("没有找到文件");
        }
        return result;
    }

	
}
