package model;

/**
 * @author Kit
 * @version: 2018年9月25日 下午11:19:56
 * 
 */
public class DiskBlock {

	private int no;
	private int index;
	private String type;
	private Object object;
	
	private boolean begin;

	public DiskBlock(int no, int index, String type, Object object) {
		super();
		this.no = no;
		this.index = index;
		this.type = type;
		this.object = object;
		this.begin = false;
	}

	public int getNo() {
		return no;
	}

	public void setNo(int no) {
		this.no = no;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Object getObject() {
		return object;
	}

	public void setObject(Object object) {
		this.object = object;
	}

	public boolean isBegin() {
		return begin;
	}

	public void setBegin(boolean begin) {
		this.begin = begin;
	}

	public void allocBlock(int index, String type, Object object, boolean begin) {
		setIndex(index);
		setType(type);
		setObject(object);
		setBegin(begin);
	}

	public void clearBlock() {
		setIndex(0);
		setType(Utility.EMPTY);
		setObject(null);
		setBegin(false);
	}

	public boolean isFree() {
		return index == 0;
	}

	@Override
	public String toString() {
		Object object = getObject();
		if (object instanceof File) {
			return ((File)object).toString();
		} else {
			return ((Folder)object).toString();
		}
	}
	
}
