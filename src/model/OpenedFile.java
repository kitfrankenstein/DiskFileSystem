package model;

/**
 * @author Kit
 * @version: 2018年9月25日 下午11:30:00
 * 
 */
public class OpenedFile {

	private String fileName;
	private int flag;// 0 以读打开 1以写打开
	private int diskNum;
	private String path;
	private int length;
	private File file;

	public OpenedFile(File file, int flag) {
		this.file = file;
		this.fileName = file.getFileName();
		this.flag = flag;
		this.diskNum = file.getDiskNum();
		this.path = file.getLocation();
		this.setLength(file.getLength());
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getFlag() {
		return (flag == 1) ? "读写" : "只读";
	}

	public void setFlag(int flag) {
		this.flag = flag;
	}

	public int getDiskNum() {
		return diskNum;
	}

	public void setDiskNum(int diskNum) {
		this.diskNum = diskNum;
	}

	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public File getFile() {
		return file;
	}

	public void setFile(File file) {
		this.file = file;
	}

}
