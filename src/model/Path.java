package model;
/**
* @author Kit
* @version: 2018年10月13日 下午12:27:29
* 
*/

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class Path implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String pathName;
	private Path parent;
	private List<Path> children;

	public Path(String name, Path parent) {
		this.setPathName(name);
		this.setParent(parent);
		this.children = new ArrayList<Path>();
	}

	public String getPathName() {
		return pathName;
	}

	public void setPathName(String pathName) {
		this.pathName = pathName;
	}

	public Path getParent() {
		return parent;
	}

	public void setParent(Path parent) {
		this.parent = parent;
	}

	public boolean hasParent() {
		return (parent == null) ? false : true;
	}

	public List<Path> getChildren() {
		return children;
	}

	public void setChildren(List<Path> children) {
		this.children = children;
	}

	public void addChildren(Path child) {
		this.children.add(child);
	}

	public void removeChildren(Path child) {
		this.children.remove(child);
	}

	public boolean hasChild() {
		return children.isEmpty() ? false : true;
	}

	@Override
	public String toString() {
		return "Path [pathName=" + pathName + "]";
	}

}
