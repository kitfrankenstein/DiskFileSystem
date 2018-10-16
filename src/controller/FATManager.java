package controller;

import java.util.ArrayList;
import java.util.List;

import model.FAT;
import model.File;
import model.Folder;
import model.OpenedFile;
import model.Path;
import model.Utility;

/**
 * @author Kit
 * @version: 2018年9月25日 下午11:39:46
 * 
 */
public class FATManager {

	private FAT[] FATs;
	private List<OpenedFile> openedFiles;
	// private Disk cDisk = new Disk("C");
	private Folder c;
	private Path rootPath = new Path("C:", null);
	private List<Path> paths;

	public FATManager() {
		c = new Folder("C:", "root", 0, null);
		FATs = new FAT[128];
		FATs[0] = new FAT(0, Utility.END, Utility.DISK, c);
		FATs[1] = new FAT(1, Utility.END, Utility.DISK, c);
		for (int i = 2; i < 128; i++) {
			FATs[i] = new FAT(i, Utility.FREE, Utility.EMPTY, null);
		}
		openedFiles = new ArrayList<OpenedFile>();
		paths = new ArrayList<Path>();
		paths.add(rootPath);
		c.setPath(rootPath);
	}

	public void addOpenedFile(FAT fat, int flag) {
		File thisFile = (File) fat.getObject();
		OpenedFile openedFile = new OpenedFile(thisFile, flag);
		openedFiles.add(openedFile);
		thisFile.setOpenedFile(openedFile);
	}

	public void removeOpenedFile(FAT fat) {
		File thisFile = (File) fat.getObject();
		for (int i = 0; i < openedFiles.size(); i++) {
			if (openedFiles.get(i).getFile() == thisFile) {
				openedFiles.remove(i);
				thisFile.setOpenedFile(null);
			}
		}
	}

	public boolean isOpenedFile(FAT fat) {
		return ((File) fat.getObject()).isOpened();
	}

	// 创建文件夹
	public int createFolder(String path) {
		String folderName = null;
		boolean canName = true;
		int index = 1;
		// 得到新建文件夹名字
		do {
			folderName = "新建文件夹";
			canName = true;
			folderName += index;
			for (int i = 2; i < FATs.length; i++) {
				if (!FATs[i].isFree()) {
					if (FATs[i].getType() == Utility.FOLDER) {
						Folder folder = (Folder) FATs[i].getObject();
						if (path.equals(folder.getLocation())) {
							if (folderName.equals(folder.getFolderName())) {
								canName = false;
							}
						}
					}
				}
			}
			index++;
		} while (!canName);
		// 在myFAT中添加文件夹
		int index2 = searchEmptyFromMyFAT();
		if (index2 == Utility.ERROR) {
			return Utility.ERROR;
		} else {
			Folder parent = getFolder(path);
			Folder folder = new Folder(folderName, path, index2, parent);
			if (parent instanceof Folder) {
				parent.addChildren(folder);
			}
			FATs[index2].allocFAT(Utility.END, Utility.FOLDER, folder);
			Path parentP = getPath(path);
			Path thisPath = new Path(path + "\\" + folderName, parentP);
			if (parentP != null) {
				parentP.addChildren(thisPath);
			}
			paths.add(thisPath);
			folder.setPath(thisPath);
		}
		return index2;
	}

	/**
	 * 创建文件
	 * 
	 * @return
	 */
	public int createFile(String path) {
		String fileName = null;
		boolean canName = true;
		int index = 1;
		// 得到新建文件名字
		do {
			fileName = "新建文件";
			canName = true;
			fileName += index;
			for (int i = 2; i < FATs.length; i++) {
				if (!FATs[i].isFree()) {
					if (FATs[i].getType() == Utility.FILE) {
						File file = (File) FATs[i].getObject();
						if (path.equals(file.getLocation())) {
							if (fileName.equals(file.getFileName())) {
								canName = false;
							}
						}
					}
				}
			}
			index++;
		} while (!canName);
		// 在myFAT中添加文件
		int index2 = searchEmptyFromMyFAT();
		if (index2 == Utility.ERROR) {
			return Utility.ERROR;
		} else {
			Folder parent = getFolder(path);
			File file = new File(fileName, path, index2, parent);
			if (parent instanceof Folder) {
				parent.addChildren(file);
			}
			FATs[index2].allocFAT(Utility.END, Utility.FILE, file);
		}
		return index2;
	}

	// 得到myFAT中第一个为空的磁盘块索引
	public int searchEmptyFromMyFAT() {
		for (int i = 2; i < FATs.length; i++) {
			if (FATs[i].isFree()) {
				return i;
			}
		}
		return Utility.ERROR;
	}

	// 得到磁盘块的使用
	public int getNumOfFAT() {
		int n = 0;
		for (int i = 2; i < FATs.length; i++) {
			if (!FATs[i].isFree()) {
				n++;
			}
		}
		return n;
	}

	// 得到空的磁盘块数量
	public int getSpaceOfFAT() {
		int n = 0;
		for (int i = 2; i < FATs.length; i++) {
			if (FATs[i].isFree()) {
				n++;
			}
		}
		return n;
	}

	// 保存数据时重新分配磁盘
	public boolean reallocFAT(int num, FAT fat) {
		// 从哪片磁盘开始
		File thisFile = (File) fat.getObject();
		int begin = thisFile.getDiskNum();
		int index = FATs[begin].getIndex();
		int oldNum = 1;
		while (index != Utility.END) {
			oldNum++;
			if (FATs[index].getIndex() == Utility.END) {
				begin = index;
			}
			index = FATs[index].getIndex();
		}

		if (num > oldNum) {
			// 增加磁盘块
			int n = num - oldNum;
			if (this.getSpaceOfFAT() < n) {
				// 超过磁盘容量
				return false;
			}
			int space = this.searchEmptyFromMyFAT();
			FATs[begin].setIndex(space);
			for (int i = 1; i <= n; i++) {
				space = this.searchEmptyFromMyFAT();
				if (i == n) {
					FATs[space].allocFAT(Utility.END, Utility.FILE, thisFile);
				} else {
					FATs[space].allocFAT(Utility.END, Utility.FILE, thisFile);// 同一个文件的所有磁盘块拥有相同的对象
					int space2 = this.searchEmptyFromMyFAT();
					FATs[space].setIndex(space2);
				}
			}
		} else if (num < oldNum) {
			// 减少磁盘块
			int end = thisFile.getDiskNum();
			while (num > 1) {
				end = FATs[end].getIndex();
				num--;
			}
			int next = 0;
			for (int i = FATs[end].getIndex(); i != Utility.END; i = next) {
				next = FATs[i].getIndex();
				FATs[i].clearFAT();
			}
			FATs[end].setIndex(Utility.END);
		} else {
			// 不变
		}
		thisFile.getOpenedFile().setLength(num);
		return true;
	}

	public List<Folder> getFolders(String path) {
		List<Folder> list = new ArrayList<Folder>();
		for (int i = 2; i < FATs.length; i++) {
			if (!FATs[i].isFree()) {
				if (FATs[i].getObject() instanceof Folder) {
					if (((Folder) (FATs[i].getObject())).getLocation().equals(path)) {
						list.add((Folder) FATs[i].getObject());
					}
				}
			}
		}
		return list;
	}

	public List<File> getFiles(String path) {
		List<File> list = new ArrayList<File>();
		for (int i = 2; i < FATs.length; i++) {
			if (!FATs[i].isFree()) {
				if (FATs[i].getObject() instanceof File) {
					if (((File) (FATs[i].getObject())).getLocation().equals(path)) {
						list.add((File) FATs[i].getObject());
					}
				}
			}
		}
		return list;
	}

	public List<FAT> getFATList(String path) {
		List<FAT> fList = new ArrayList<FAT>();
		for (int i = 2; i < FATs.length; i++) {
			if (!FATs[i].isFree()) {
				if (FATs[i].getObject() instanceof Folder) {
					if (((Folder) (FATs[i].getObject())).getLocation().equals(path)
							&& FATs[i].getIndex() == Utility.END) {
						fList.add(FATs[i]);
					}
				}
			}
		}
		for (int i = 2; i < FATs.length; i++) {
			if (!FATs[i].isFree()) {
				if (FATs[i].getObject() instanceof File) {
					if (((File) (FATs[i].getObject())).getLocation().equals(path)
							&& FATs[i].getIndex() == Utility.END) {
						fList.add(FATs[i]);
					}
				}
			}
		}
		return fList;
	}

	/**
	 * 获得当前路径指向的文件夹
	 * 
	 * @param path
	 * @return
	 */
	public Folder getFolder(String path) {
		if (path.equals("C:")) {
			return c;
		}
		int split = path.lastIndexOf('\\');
		String location = path.substring(0, split);
		String folderName = path.substring(split + 1);
		List<Folder> folders = getFolders(location);
		for (Folder folder : folders) {
			if (folder.getFolderName().equals(folderName)) {
				return folder;
			}
		}
		return null;
	}

	public Path getPath(String path) {
		for (Path p : paths) {
			if (p.getPathName().equals(path)) {
				return p;
			}
		}
		return null;
	}

	/**
	 * 
	 * @param fat
	 */
	public int delete(FAT fat) {
		if (fat.getType() == Utility.FILE) {
			// ---------------->文件
			// 判断是否文件正在打开，如果打开则不能删除
			for (int i = 0; i < openedFiles.size(); i++) {
				if (openedFiles.get(i).getFile().equals(fat.getObject())) {
					// 文件正打开着，不能删除
					return 3;
				}
			}
			File thisFile = (File) fat.getObject();
			Folder parent = thisFile.getParent();
			if (parent instanceof Folder) {
				parent.removeChildren(thisFile);
				parent.setSize(Utility.getFolderSize(parent));
				while (parent.hasParent()) {
					parent = parent.getParent();
					parent.setSize(Utility.getFolderSize(parent));
				}
			}
			for (int i = 2; i < FATs.length; i++) {
				if (!FATs[i].isFree() && FATs[i].getType() == Utility.FILE) {
					if (((File) FATs[i].getObject()).equals(fat.getObject())) {// 同一个对象，见saveToModifyFATS2方法
						System.out.println(FATs[i].toString());
						System.out.println(i);
						System.out.println(fat.toString() + fat.getObject().toString());
						FATs[i].clearFAT();
						System.out.println("----------------------->删除");
					}
				}
			}
			return 1;
		} else {
			// ---------------->文件夹
			// String path = ((Folder)fat.getObject()).getLocation();
			String folderPath = ((Folder) fat.getObject()).getLocation() + "\\"
					+ ((Folder) fat.getObject()).getFolderName();
			System.out.println("路径：" + folderPath);
			int index = 0;
			for (int i = 2; i < FATs.length; i++) {
				if (!FATs[i].isFree()) {
					Object obj = FATs[i].getObject();
					if (FATs[i].getType() == Utility.FOLDER) {
						if (((Folder) obj).getLocation().equals(folderPath)) {
							// 文件夹不为空，不能删除
							return 2;
						}
					} else {
						if (((File) obj).getLocation().equals(folderPath)) {
							// 文件夹不为空，不能删除
							return 2;
						}
					}
					if (FATs[i].getType() == Utility.FOLDER) {
						if (((Folder) FATs[i].getObject()).equals(fat.getObject())) {
							index = i;
						}
					}
				}
			}
			Folder thisFolder = (Folder) fat.getObject();
			Folder parent = thisFolder.getParent();
			if (parent instanceof Folder) {
				parent.removeChildren(thisFolder);
				parent.setSize(Utility.getFolderSize(parent));
			}
			paths.remove(getPath(folderPath));
			FATs[index].clearFAT();
			return 0;
		}
	}

	public FAT[] getFATs() {
		return FATs;
	}

	public void setFATs(FAT[] FATs) {
		this.FATs = FATs;
	}

	public FAT getFAT(int index) {
		return FATs[index];
	}

	public List<OpenedFile> getOpenedFiles() {
		return openedFiles;
	}

	public void setOpenedFiles(List<OpenedFile> openFiles) {
		this.openedFiles = openFiles;
	}

	public List<Path> getPaths() {
		return paths;
	}

	public void setPaths(List<Path> paths) {
		this.paths = paths;
	}

	public void addPath(Path path) {
		paths.add(path);
	}

	public void removePath(Path path) {
		paths.remove(path);
		if (path.hasParent()) {
			path.getParent().removeChildren(path);
		}
	}

	public void replacePath(Path oldPath, String newName) {
		oldPath.setPathName(newName);
	}

	public boolean hasPath(Path path) {
		for (Path p : paths) {
			if (p.equals(path)) {
				return true;
			}
		}
		return false;
	}

}
