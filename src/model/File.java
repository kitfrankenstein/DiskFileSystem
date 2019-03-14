package model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Kit
 * @version: 2018年9月25日 下午11:25:54
 * 
 */
public class File {

	private String fileName;//文件名
	private String type;// 类型
	private int diskNum;// 起始盘块号
	private int flag;//读写标记
	private int length;// 占用盘块数
	private String content;// 内容

	private Folder parent;//父文件夹

	private String location; // 位置
	private double size; // 大小
	private String space; // 占用空间
	private Date createTime;//创建时间
	
	private boolean isOpen;//打开标志

	public File(String fileName) {
		this.fileName = fileName;
		this.setOpened(false);
	}

	public File(String fileName, String location, int diskNum, Folder parent) {
		this.fileName = fileName;
		this.type = Utility.FILE;
		this.diskNum = diskNum;
		this.length = 1;
		this.content = "";

		this.location = location;
		this.size = Utility.getSize(content.length());
		this.space = size + "KB";
		this.createTime = new Date();

		this.parent = parent;
		
		this.setOpened(false);
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public int getDiskNum() {
		return diskNum;
	}

	public void setDiskNum(int diskNum) {
		this.diskNum = diskNum;
	}

	public int getFlag() {
		return flag;
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

	public String getLocation() {
		return location;
	}

	public void setLocation(String location) {
		this.location = location;
	}

	public double getSize() {
		return size;
	}

	public void setSize(double KBcount) {
		this.size = KBcount;
		this.setSpace(size + "KB");
	}

	public String getSpace() {
		return space;
	}

	public void setSpace(String space) {
		this.space = space;
	}

	public String getCreateTime() {
		SimpleDateFormat format = new SimpleDateFormat("yyyy年MM月dd日  HH:mm:ss");
		return format.format(createTime);
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Folder getParent() {
		return parent;
	}

	public void setParent(Folder parent) {
		this.parent = parent;
	}

	public boolean hasParent() {
		return (parent == null) ? false : true;
	}
	
	public boolean isOpened() {
		return isOpen;
	}
	
	public void setOpened(boolean isOpen) {
		this.isOpen = isOpen;
	}

	@Override
	public String toString() {
		return fileName;
	}

}
