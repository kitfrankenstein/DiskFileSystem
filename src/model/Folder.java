package model;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author Kit
 * @version: 2018年9月25日 下午11:24:44
 * 
 */
public class Folder {

	private String folderName;
	private String property;
	private int diskNum;
	private String type;

	private String location;
	private double size;
	private String space;
	private Date createTime;

	private Folder parent;
	private List<Object> children;
	private Path path;

	public Folder(String folderName) {
		this.folderName = folderName;
	}

	public Folder(String folderName, String location, int diskNum, Folder parent) {
		this.folderName = folderName;
		this.location = location;
		this.size = 0;
		this.space = size + "KB";
		this.createTime = new Date();

		this.diskNum = diskNum;
		this.type = Utility.FOLDER;

		this.parent = parent;
		this.setChildren(new ArrayList<>());
	}

	public String getFolderName() {
		return folderName;
	}

	public void setFolderName(String folderName) {
		this.folderName = folderName;
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

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
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

	public List<Object> getChildren() {
		return children;
	}

	public void setChildren(List<Object> children) {
		this.children = children;
	}

	public void addChildren(Object child) {
		this.children.add(child);
	}

	public void removeChildren(Object child) {
		this.children.remove(child);
	}

	public boolean hasChild() {
		return children.isEmpty() ? false : true;
	}

	public Path getPath() {
		return path;
	}

	public void setPath(Path path) {
		this.path = path;
	}

	@Override
	public String toString() {
		return folderName;
	}

}
