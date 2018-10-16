package model;
/**
* @author Kit
* @version: 2018年9月25日 下午11:23:50
* 
*/
public class Disk {

	private String diskName;
		
	public Disk(String diskName) {
		super();
		this.diskName = diskName;
	}
	
	public String getDiskName() {
		return diskName;
	}

	public void setDiskName(String diskName) {
		this.diskName = diskName;
	}
	
	@Override
	public String toString() {
		return diskName;
	}
	
}
