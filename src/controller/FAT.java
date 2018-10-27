package controller;

import java.util.ArrayList;
import java.util.List;

import model.DiskBlock;
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
public class FAT {

	private DiskBlock[] diskBlocks;
	private List<OpenedFile> openedFiles;
	private Folder c;
	private Path rootPath = new Path("C:", null);
	private List<Path> paths;

	public FAT() {
		c = new Folder("C:", "root", 0, null);
		diskBlocks = new DiskBlock[128];
		diskBlocks[0] = new DiskBlock(0, Utility.END, Utility.DISK, c);
		diskBlocks[0].setBegin(true);
		diskBlocks[1] = new DiskBlock(1, Utility.END, Utility.DISK, c);
		for (int i = 2; i < 128; i++) {
			diskBlocks[i] = new DiskBlock(i, Utility.FREE, Utility.EMPTY, null);
		}
		openedFiles = new ArrayList<OpenedFile>();
		paths = new ArrayList<Path>();
		paths.add(rootPath);
		c.setPath(rootPath);
	}

	public void addOpenedFile(DiskBlock block, int flag) {
		File thisFile = (File) block.getObject();
		OpenedFile openedFile = new OpenedFile(thisFile, flag);
		openedFiles.add(openedFile);
		thisFile.setOpenedFile(openedFile);
	}

	public void removeOpenedFile(DiskBlock block) {
		File thisFile = (File) block.getObject();
		for (int i = 0; i < openedFiles.size(); i++) {
			if (openedFiles.get(i).getFile() == thisFile) {
				openedFiles.remove(i);
				thisFile.setOpenedFile(null);
				break;
			}
		}
	}

	public boolean isOpenedFile(DiskBlock block) {
		if (block.getObject() instanceof Folder) {
			return false;
		}
		return ((File) block.getObject()).isOpened();
	}

	/**
	 * 
	 * @param path
	 * @return
	 */
	public int createFolder(String path) {
		String folderName = null;
		boolean canName = true;
		int index = 1;
		// 得到文件夹名
		do {
			folderName = "文件夹";
			canName = true;
			folderName += index;
			for (int i = 2; i < diskBlocks.length; i++) {
				if (!diskBlocks[i].isFree()) {
					if (diskBlocks[i].getType() == Utility.FOLDER) {
						Folder folder = (Folder) diskBlocks[i].getObject();
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
		int index2 = searchEmptyDiskBlock();
		if (index2 == Utility.ERROR) {
			return Utility.ERROR;
		} else {
			Folder parent = getFolder(path);
			Folder folder = new Folder(folderName, path, index2, parent);
			if (parent instanceof Folder) {
				parent.addChildren(folder);
			}
			diskBlocks[index2].allocBlock(Utility.END, Utility.FOLDER, folder, true);
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
	 * 
	 * @param path
	 * @return
	 */
	public int createFile(String path) {
		String fileName = null;
		boolean canName = true;
		int index = 1;
		// 得到文件名
		do {
			fileName = "文件";
			canName = true;
			fileName += index;
			for (int i = 2; i < diskBlocks.length; i++) {
				if (!diskBlocks[i].isFree()) {
					if (diskBlocks[i].getType() == Utility.FILE) {
						File file = (File) diskBlocks[i].getObject();
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
		int index2 = searchEmptyDiskBlock();
		if (index2 == Utility.ERROR) {
			return Utility.ERROR;
		} else {
			Folder parent = getFolder(path);
			File file = new File(fileName, path, index2, parent);
			if (parent instanceof Folder) {
				parent.addChildren(file);
			}
			diskBlocks[index2].allocBlock(Utility.END, Utility.FILE, file, true);
		}
		return index2;
	}

	// 得到第一个为空的磁盘块
	public int searchEmptyDiskBlock() {
		for (int i = 2; i < diskBlocks.length; i++) {
			if (diskBlocks[i].isFree()) {
				return i;
			}
		}
		return Utility.ERROR;
	}

	public int numOfUsedBlocks() {
		int n = 0;
		for (int i = 2; i < diskBlocks.length; i++) {
			if (!diskBlocks[i].isFree()) {
				n++;
			}
		}
		return n;
	}

	public int numOfFreeBlocks() {
		int n = 0;
		for (int i = 2; i < diskBlocks.length; i++) {
			if (diskBlocks[i].isFree()) {
				n++;
			}
		}
		return n;
	}

	// 保存数据时重新分配磁盘
	public boolean reallocBlocks(int num, DiskBlock block) {
		// 从哪片磁盘开始
		File thisFile = (File) block.getObject();
		int begin = thisFile.getDiskNum();
		int index = diskBlocks[begin].getIndex();
		int oldNum = 1;
		while (index != Utility.END) {
			oldNum++;
			if (diskBlocks[index].getIndex() == Utility.END) {
				begin = index;
			}
			index = diskBlocks[index].getIndex();
		}

		if (num > oldNum) {
			// 增加磁盘块
			int n = num - oldNum;
			if (numOfFreeBlocks() < n) {
				// 超过磁盘容量
				return false;
			}
			int space = searchEmptyDiskBlock();
			diskBlocks[begin].setIndex(space);
			for (int i = 1; i <= n; i++) {
				space = searchEmptyDiskBlock();
				if (i == n) {
					diskBlocks[space].allocBlock(Utility.END, Utility.FILE, thisFile, false);
				} else {
					diskBlocks[space].allocBlock(Utility.END, Utility.FILE, thisFile, false);// 同一个文件的所有磁盘块拥有相同的对象
					int space2 = searchEmptyDiskBlock();
					diskBlocks[space].setIndex(space2);
				}
				System.out.println(thisFile);
			}
		} else if (num < oldNum) {
			// 减少磁盘块
			int end = thisFile.getDiskNum();
			while (num > 1) {
				end = diskBlocks[end].getIndex();
				num--;
			}
			int next = 0;
			for (int i = diskBlocks[end].getIndex(); i != Utility.END; i = next) {
				next = diskBlocks[i].getIndex();
				diskBlocks[i].clearBlock();
			}
			diskBlocks[end].setIndex(Utility.END);
		} else {
			// 不变
		}
		thisFile.getOpenedFile().setLength(num);
		return true;
	}

	public List<Folder> getFolders(String path) {
		List<Folder> list = new ArrayList<Folder>();
		for (int i = 2; i < diskBlocks.length; i++) {
			if (!diskBlocks[i].isFree()) {
				if (diskBlocks[i].getObject() instanceof Folder) {
					if (((Folder) (diskBlocks[i].getObject())).getLocation().equals(path)) {
						list.add((Folder) diskBlocks[i].getObject());
					}
				}
			}
		}
		return list;
	}

	public List<File> getFiles(String path) {
		List<File> list = new ArrayList<File>();
		for (int i = 2; i < diskBlocks.length; i++) {
			if (!diskBlocks[i].isFree()) {
				if (diskBlocks[i].getObject() instanceof File) {
					if (((File) (diskBlocks[i].getObject())).getLocation().equals(path)) {
						list.add((File) diskBlocks[i].getObject());
					}
				}
			}
		}
		return list;
	}

	public List<DiskBlock> getBlockList(String path) {
		List<DiskBlock> bList = new ArrayList<DiskBlock>();
		for (int i = 2; i < diskBlocks.length; i++) {
			if (!diskBlocks[i].isFree()) {
				if (diskBlocks[i].getObject() instanceof Folder) {
					if (((Folder) (diskBlocks[i].getObject())).getLocation().equals(path)
							&& diskBlocks[i].isBegin()) {
						bList.add(diskBlocks[i]);
					}
				}
			}
		}
		for (int i = 2; i < diskBlocks.length; i++) {
			if (!diskBlocks[i].isFree()) {
				if (diskBlocks[i].getObject() instanceof File) {
					if (((File) (diskBlocks[i].getObject())).getLocation().equals(path)
							&& diskBlocks[i].isBegin()) {
						bList.add(diskBlocks[i]);
					}
				}
			}
		}
		return bList;
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
	 * @param block
	 */
	public int delete(DiskBlock block) {
		if (block.getObject() instanceof File) {
			if (isOpenedFile(block)) {
				// 文件正打开着，不能删除
				return 3;
			}
			File thisFile = (File) block.getObject();
			Folder parent = thisFile.getParent();
			if (parent instanceof Folder) {
				parent.removeChildren(thisFile);
				parent.setSize(Utility.getFolderSize(parent));
				while (parent.hasParent()) {
					parent = parent.getParent();
					parent.setSize(Utility.getFolderSize(parent));
				}
			}
			for (int i = 2; i < diskBlocks.length; i++) {
				if (!diskBlocks[i].isFree() && diskBlocks[i].getObject() instanceof File) {
					System.out.println("yes");
					if (((File) diskBlocks[i].getObject()).equals(thisFile)) {// 同一个对象
						System.out.println("yes2");
						diskBlocks[i].clearBlock();
					}
				}
			}
			return 1;
		} else {
			String folderPath = ((Folder) block.getObject()).getLocation() + "\\"
					+ ((Folder) block.getObject()).getFolderName();
			int index = 0;
			for (int i = 2; i < diskBlocks.length; i++) {
				if (!diskBlocks[i].isFree()) {
					Object obj = diskBlocks[i].getObject();
					if (diskBlocks[i].getType() == Utility.FOLDER) {
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
					if (diskBlocks[i].getType() == Utility.FOLDER) {
						if (((Folder) diskBlocks[i].getObject()).equals(block.getObject())) {
							index = i;
						}
					}
				}
			}
			Folder thisFolder = (Folder) block.getObject();
			Folder parent = thisFolder.getParent();
			if (parent instanceof Folder) {
				parent.removeChildren(thisFolder);
				parent.setSize(Utility.getFolderSize(parent));
			}
			paths.remove(getPath(folderPath));
			diskBlocks[index].clearBlock();
			return 0;
		}
	}

	public DiskBlock[] getDiskBlocks() {
		return diskBlocks;
	}

	public void setDiskBlocks(DiskBlock[] diskBlocks) {
		this.diskBlocks = diskBlocks;
	}

	public DiskBlock getBlock(int index) {
		return diskBlocks[index];
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
	
	public boolean hasName(String path, String name) {
		Folder thisFolder = getFolder(path);
		for (Object child : thisFolder.getChildren()) {
			if (child.toString().equals(name)) {
				return true;
			}
		}		
		return false;
	}

}
