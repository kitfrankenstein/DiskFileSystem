package model;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Kit
 * @version: 2018年9月25日 下午11:25:54
 * 
 */
public class File {

	private String fileName;
	private String type;// 类型
	private String property;// 属性
	private int diskNum;// 起始盘块号
	private int length;// 占用盘块数
	private String content;// 内容

	private Folder parent;
	private OpenedFile openedFile;

	private String location; // 位置
	private double size; // 大小
	private String space; // 占用空间
	private Date createTime;

	public File(String fileName) {
		this.fileName = fileName;
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

	public String getProperty() {
		return property;
	}

	public void setProperty(String property) {
		this.property = property;
	}

	public int getDiskNum() {
		return diskNum;
	}

	public void setDiskNum(int diskNum) {
		this.diskNum = diskNum;
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

	public OpenedFile getOpenedFile() {
		return openedFile;
	}

	public void setOpenedFile(OpenedFile openedFile) {
		this.openedFile = openedFile;
	}
	
	public boolean isOpened() {
		return openedFile == null ? false : true;
	}

	@Override
	public String toString() {
		return fileName;
	}

}
