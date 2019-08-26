package util;

import java.util.List;

import model.File;
import model.Folder;

/**
* @author Kit
* @version: 2018年9月25日 下午11:32:55
* 
*/
public class FATUtil {
	
	public static final String ICO = "res/ico.png";
	public static final String FOLDER_IMG = "res/folder.png";
	public static final String FILE_IMG = "res/file.png";
	public static final String DISK_IMG = "res/disk.png";
	public static final String TREE_NODE_IMG = "res/node.png";
	public static final String FORWARD_IMG = "res/forward.png";
	public static final String BACK_IMG = "res/back.png";
	public static final String SAVE_IMG = "res/save.png";
	public static final String CLOSE_IMG = "res/close.png";
	
	public static final int END = 255;
	public static final int ERROR = -1;
	public static final int FREE = 0;
	
	public static final String DISK = "磁盘";
	public static final String FOLDER = "文件夹";
	public static final String FILE = "文件";
	public static final String EMPTY = "空";
			
	public static final int FLAGREAD = 0;
	public static final int FLAGWRITE = 1;	
	
	
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
