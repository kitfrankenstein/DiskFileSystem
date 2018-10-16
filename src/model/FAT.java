package model;

/**
 * @author Kit
 * @version: 2018年9月25日 下午11:19:56
 * 
 */
public class FAT {

	private int no;
	private int index;// 磁盘块
	private String type;// 该磁盘块中存放的是File还是Folder
	private Object object;// File或Folder

	public FAT(int no, int index, String type, Object object) {
		super();
		this.no = no;
		this.index = index;
		this.type = type;
		this.object = object;
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
		// this.noProperty.set(no);
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
		// this.indexProperty.set(index);
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
		// this.typeProperty.set(type);
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public void allocFAT(int index, String type, Object object) {
		setIndex(index);
		setType(type);
		setObject(object);
	}

	public void clearFAT() {
		setIndex(0);
		setType(Utility.EMPTY);
		setObject(null);
	}

	public boolean isFree() {
		return index == 0;
	}

}
