package model;

import java.util.List;

/**
* @author Kit
* @version: 2018年9月25日 下午11:32:55
* 
*/
public class Utility {
	
	public static String ico = "res/ico.png";
	public static String folderImg = "res/folder.png";
	public static String fileImg = "res/file.png";
	public static String diskImg = "res/disk.png";
	public static String treeNodeImg = "res/node.png";
	public static String forwardImg = "res/forward.png";
	public static String backImg = "res/back.png";
	public static String saveImg = "res/save.png";
	public static String closeImg = "res/close.png";
	
	public static int END = 255;
	public static int ERROR = -1;
	public static int FREE = 0;
	
	public static String DISK = "磁盘";
	public static String FOLDER = "文件夹";
	public static String FILE = "文件";
	public static String EMPTY = "空";
			
	public static int FLAGREAD = 0;
	public static int FLAGWRITE = 1;	
	
	
	public static int blocksCount(int length){
		if (length <= 64){
			return 1;
		} else {
			int n = 0;
			if (length % 64 == 0){
				n = length / 64;
			} else {
				n = length / 64;
				n++;
			}
			return n;
		}
	}
	
	public static double getSize(int length) {
		return Double.parseDouble((String.format("%.2f", length / 1024.0)));
	}
	
//	public static double getFolderSize(Folder folder) {
//		List<Object> children = folder.getChildren();
//		int length = 0;
//		for (Object child : children) {
//			if (child instanceof File) {
//				length += ((File)child).getContent().length();
//			} else {
//				length += getFolderLength((Folder)child);
//			}			
//		}
//		return getSize(length);
//	}
//	
//	public static int getFolderLength(Folder folder) {
//		List<Object> children = folder.getChildren();
//		int totalLength = 0;
//		for (Object child : children) {
//			if (child instanceof File) {
//				totalLength += ((File)child).getContent().length();
//			} else {
//				totalLength += getFolderLength((Folder)child);
//			}			
//		}
//		System.out.println("total" + totalLength);
//		return totalLength;
//	}
	
	public static double getFolderSize(Folder folder) {
		List<Object> children = folder.getChildren();
		double size = 0;
		for (Object child : children) {
			if (child instanceof File) {
				size += ((File)child).getSize();
			} else {
				size += getFolderSize((Folder)child);
			}			
		}
		return Double.parseDouble((String.format("%.2f", size)));
	}
	
}
