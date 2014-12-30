package com.pccw.nowplayer.utils;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.io.OutputStream;

/**
 * @author AlfredZhong
 */
public class FileUtils {
	
	private FileUtils() {
		// Cannot instantiate.
	}

	/**
	 * NOTE: Don't read big files. It may throw OutOfMemoryError.
	 * @param is
	 * @return
	 * @throws IOException
	 */
    public static byte[] getByteArray(InputStream is) throws IOException {
    	if(is == null)
    		return null;
    	long start = System.currentTimeMillis();
        ByteArrayOutputStream outStream = new ByteArrayOutputStream();        
        byte[] buffer = new byte[1024];        
        int len = -1;
        while( (len = is.read(buffer)) != -1){
        	// System.out.write() can write to console.
        	// Since here write bytes to memory, it may throw OutOfMemoryError if bytes too much.
            outStream.write(buffer, 0, len);        
        }   
        is.close();
        outStream.close();
        System.out.println("getByteArray() costs " + (System.currentTimeMillis() - start) + " ms.");
        return outStream.toByteArray();  
    }
    
    public static byte[] getFileBytes(String filePath) throws Exception {
    	InputStream is = new FileInputStream(new File(filePath));
    	return getByteArray(is);
    }
    
    public static String readFile(String filePath) throws Exception {
    	return readByBytes(new FileInputStream(new File(filePath)));
    }
	
	/**
	 * Read file by bytes.
	 * NOTE: Don't read big files. It may throw OutOfMemoryError.
	 */
	public static String readByBytes(InputStream inStream) throws Exception {
		byte[] bs = getByteArray(inStream);
		if(bs != null)
			return new String(bs);
		return null;
	}
	
    public static String readFile(String filePath, String encoding) throws Exception {
    	return readByLine(new FileInputStream(new File(filePath)), encoding);
    }
	
	/**
	 * Read file by line.
	 * NOTE: Don't read big files. It may throw OutOfMemoryError.
	 */
	public static String readByLine(InputStream is, String encoding) throws Exception {
		long start = System.currentTimeMillis();
		StringBuilder text = new StringBuilder("");
		InputStreamReader reader = new InputStreamReader(is, encoding);
		BufferedReader br = new BufferedReader(reader);
		String str = null;
		while ((str = br.readLine()) != null) {
			if (text.length() > 0)
				text.append("\n");
			text.append(str);
		}
		reader.close();
		br.close();
		System.out.println("readByLine() costs " + (System.currentTimeMillis() - start) + " ms.");
		return text.toString();
	}
    
	public static void copyFile(File sourceFile, File destFile) throws Exception {
		long start = System.currentTimeMillis();
		FileInputStream input = new FileInputStream(sourceFile);
		BufferedInputStream inBuff = new BufferedInputStream(input);
		FileOutputStream output = new FileOutputStream(destFile);
		BufferedOutputStream outBuff = new BufferedOutputStream(output);
		byte[] buffer = new byte[2097152]; // 2M
		int len;
		while ((len = inBuff.read(buffer)) != -1) {
			outBuff.write(buffer, 0, len);
		}
		outBuff.flush();
		inBuff.close();
		outBuff.close();
		output.close();
		input.close();
		System.out.println("copyFile() costs " + (System.currentTimeMillis() - start) + " ms.");
	}
	
	public static void copyDirectory(String srcPath, String destPath) throws Exception {
		File src = new File(srcPath);
		destPath += src.getName();
		File dest = new File(destPath);
		dest.mkdirs();
		srcPath += File.separator;
		destPath = dest.getAbsolutePath() + File.separator;
		File[] file = src.listFiles();
		for (int i = 0; i < file.length; i++) {
			if (file[i].isFile()) {
				copyFile(file[i], new File(destPath, file[i].getName()));
			} else if (file[i].isDirectory()) {
				copyDirectory(srcPath + file[i].getName(), destPath);
			}
		}
	}
	
	public static void Move(File srcFile, String destPath) throws Exception {
		if(srcFile.isFile()) {
			if(srcFile.renameTo(new File(new File(destPath), srcFile.getName()))) {
				return;
			} else {
				copyFile(srcFile, new File(destPath, srcFile.getName()));
				srcFile.delete();
			}	
		} else {
			copyDirectory(srcFile.getPath(), destPath);
			deleteDirectory(srcFile.getPath());
		}
	}
	
    public static void appendFile(String fileName, String content) throws Exception {
        FileWriter writer = new FileWriter(fileName, true);
        writer.write(content);
        writer.close();
    }

	public static boolean deleteFile(String filePath) {
		File file = new File(filePath);
		return file.delete();
	}

	public static boolean deleteDirectory(String filePath) {
		File file = new File(filePath);
		if (!file.exists()) {
			return true;
		}
		if (!file.isDirectory()) {
			return file.delete();
		}
		File[] list = file.listFiles();
		for(File f : list) {
			if(f.isFile()) {
				f.delete();
			} else if(f.isDirectory()) {
				deleteDirectory(f.getAbsolutePath());
			}
		}
		return file.delete();
	}
	
	public static void saveFile(String content, OutputStream outStream) throws Exception {
		outStream.write(content.getBytes());
		outStream.close();
	}
	
	public static void saveFile(byte[] content, OutputStream outStream) throws Exception {
		outStream.write(content);
		outStream.close();
	}
	
	public static void saveFile(byte[] bytes, String filename) throws IOException {
		File file = new File(filename);
		FileOutputStream fout = null;
		fout = new FileOutputStream(file);
		fout.write(bytes);
		fout.close();
	}
	
	public static void saveFile(InputStream is, String filename) throws IOException {
		FileOutputStream fos = new FileOutputStream(new File(filename));
		byte[] buffer = new byte[1024];
		int len = 0;
		while ((len = is.read(buffer)) != -1) {
			fos.write(buffer, 0, len);
		}
		is.close();
		fos.close();
	}
	
	public static InputStream bytesToInputStream(byte[] bytes) {
		return new ByteArrayInputStream(bytes);
	}
	
	public static int getFileLineCount(File file) throws Exception {
        FileReader in = new FileReader(file);
        LineNumberReader reader = new LineNumberReader(in);
        String strLine = reader.readLine();
        int totalLines = 0;
        while (strLine != null) {
            totalLines++;
            strLine = reader.readLine();
        }
        reader.close();
        in.close();
        return totalLines;
	}
	
	/**
	 * Read file specified line content.
	 * <p>NOTE: line starts from 0, can not be negative integer.
	 * @param file
	 * @param line starts from 0.
	 * @return line content or null if line size larger than total line count.
	 * @throws Exception
	 */
	public static String readFileLine(File file, int line) throws Exception {
		if(line < 0)
			throw new IllegalArgumentException("line can not be negative integer.");
        FileReader in = new FileReader(file);
        LineNumberReader reader = new LineNumberReader(in);
        String strLine = reader.readLine();
        int totalLines = 0;
        boolean founded = false;
        while (strLine != null) {
        	if(totalLines == line) {
        		founded = true;
        		break;
        	}
            totalLines++;
            strLine = reader.readLine();
        }
        reader.close();
        in.close();
        if(!founded)
        	strLine = null;
        return strLine;
	}
	
}
