package view;

import model.DiskBlock;
import model.FAT;
import model.File;
import model.Folder;
import model.Path;
import util.FATUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

/**
 * @author Kit
 * @version: 2018年9月25日 下午11:19:30
 * 
 */
public class MainView {

	private static final String DURATION_PATH = "disk";
	
	private FAT fat;
	private int index;
	private List<DiskBlock> blockList;
	private String recentPath;
	private Map<Path, TreeItem<String>> pathMap;

	private Scene scene;
	private HBox workBox, mainBox;
	private VBox rightBox, fullBox;

	private FlowPane flowPane;
	private Label[] icons;

	private HBox locBox;
	private Label locLabel;
	private TextField locField;
	private Button gotoButton, backButton;

	private TreeView<String> treeView;
	private TreeItem<String> rootNode, recentNode;

	private TableView<DiskBlock> blockTable;
	private TableView<File> openedTable;
	private ObservableList<DiskBlock> dataBlock;
	private ObservableList<File> dataOpened;

	private ContextMenu contextMenu, contextMenu2;
	private MenuItem createFileItem, createFolderItem, openItem, renameItem, delItem, propItem;

	public MainView(Stage stage) {
		pathMap = new HashMap<Path, TreeItem<String>>();
		try (ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(DURATION_PATH))) {
			fat = (FAT) inputStream.readObject();
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (fat == null) {
			fat = new FAT();
		}
		recentPath = "C:";
		initFrame(stage);
	}

	private void initFrame(Stage stage) {

		flowPane = new FlowPane();
		flowPane.setPrefSize(600, 100);
		flowPane.setStyle("-fx-background-color: #ffffff;" + "-fx-border-color: #ffffff;" + "-fx-border-width:0.5px;");
		flowPane.addEventHandler(MouseEvent.MOUSE_CLICKED, (MouseEvent me) -> {
			if (me.getButton() == MouseButton.SECONDARY && !contextMenu2.isShowing()) {
				contextMenu.show(flowPane, me.getScreenX(), me.getScreenY());
			} else {
				contextMenu.hide();
			}
		});

		initContextMenu();
		menuItemSetOnAction();

		initTopBox();
		initTables();
		initTreeView();
		
		workBox = new HBox(flowPane, blockTable);
		rightBox = new VBox(workBox, openedTable);
		mainBox = new HBox(treeView, rightBox);
		fullBox = new VBox(locBox, mainBox);
		fullBox.setPrefSize(1090, 600);

		scene = new Scene(fullBox);
		stage.setScene(scene);
		stage.setResizable(false);
		stage.getIcons().add(new Image(FATUtil.ICO));
		stage.setTitle("模拟磁盘文件系统");
		stage.show();
		stage.setOnCloseRequest(e -> {
			try (ObjectOutputStream outputStream = new ObjectOutputStream(new FileOutputStream(DURATION_PATH))) {
				System.out.println("writing");
				outputStream.writeObject(fat);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			} catch (IOException e2) {
				e2.printStackTrace();
			}
		});
	}

	private void initContextMenu() {
		createFileItem = new MenuItem("新建文件");
		createFolderItem = new MenuItem("新建文件夹");

		openItem = new MenuItem("打开");
		delItem = new MenuItem("删除");
		renameItem = new MenuItem("重命名");
		propItem = new MenuItem("属性");

		contextMenu = new ContextMenu(createFileItem, createFolderItem);
		contextMenu2 = new ContextMenu(openItem, delItem, renameItem, propItem);
	}

	private void menuItemSetOnAction() {
		createFileItem.setOnAction(ActionEvent -> {
			int no = fat.createFile(recentPath);
			if (no == FATUtil.ERROR) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("磁盘容量已满，无法创建");
				alert.showAndWait();
			} else {
				flowPane.getChildren().removeAll(flowPane.getChildren());
				addIcon(fat.getBlockList(recentPath), recentPath);
			}
		});

		createFolderItem.setOnAction(ActionEvent -> {
			int no = fat.createFolder(recentPath);
			if (no == FATUtil.ERROR) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("磁盘容量已满，无法创建");
				alert.showAndWait();
			} else {
				Folder newFolder = (Folder) fat.getBlock(no).getObject();
				Path newPath = newFolder.getPath();
				flowPane.getChildren().removeAll(flowPane.getChildren());
				addIcon(fat.getBlockList(recentPath), recentPath);
				addNode(recentNode, newPath);
			}
		});

		openItem.setOnAction(ActionEvent -> onOpen());

		delItem.setOnAction(ActionEvent -> {
			DiskBlock thisBlock = blockList.get(index);
			if (fat.isOpenedFile(thisBlock)) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText(null);
				alert.setContentText("文件未关闭");
				alert.showAndWait();
			} else {
				new delView(thisBlock, fat, MainView.this);
				flowPane.getChildren().removeAll(flowPane.getChildren());
				addIcon(fat.getBlockList(recentPath), recentPath);
			}
		});

		renameItem.setOnAction(ActionEvent -> {
			DiskBlock thisBlock = blockList.get(index);
			new RenameView(thisBlock, fat, icons[index], pathMap);
		});

		propItem.setOnAction(ActionEvent -> {
			DiskBlock thisBlock = blockList.get(index);
			new PropertyView(thisBlock, fat, icons[index], pathMap);
		});

	}

	private void initTopBox() {
		locLabel = new Label("当前目录：");
		locLabel.setStyle("-fx-font-weight: bold;" + "-fx-font-size: 16px");

		locField = new TextField("C:");
		locField.setPrefWidth(400);

		backButton = new Button();
		backButton.setOnAction(ActionEvent -> {
			System.out.println(recentPath);
			Path backPath = fat.getPath(recentPath).getParent();
			if (backPath != null) {
				List<DiskBlock> blocks = fat.getBlockList(backPath.getPathName());
				flowPane.getChildren().removeAll(flowPane.getChildren());
				addIcon(blocks, backPath.getPathName());
				recentPath = backPath.getPathName();
				recentNode = pathMap.get(backPath);
				locField.setText(recentPath);
			}
		});
		backButton.setGraphic(new ImageView(FATUtil.BACK_IMG));
		backButton.setStyle("-fx-background-color: #ffffff;");
		backButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				backButton.setStyle("-fx-background-color: #1e90ff;");
			}
		});
		backButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				backButton.setStyle("-fx-background-color: #ffffff;");
			}
		});

		gotoButton = new Button();
		gotoButton.setOnAction(ActionEvent -> {
			String textPath = locField.getText();
			Path gotoPath = fat.getPath(textPath);
			if (gotoPath != null) {
				List<DiskBlock> blocks = fat.getBlockList(textPath);
				flowPane.getChildren().removeAll(flowPane.getChildren());
				addIcon(blocks, textPath);
				recentPath = textPath;
				recentNode = pathMap.get(gotoPath);
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText("目录不存在");
				alert.setHeaderText(null);
				alert.show();
				locField.setText(recentPath);
			}
		});
		gotoButton.setGraphic(new ImageView(FATUtil.FORWARD_IMG));
		gotoButton.setStyle("-fx-background-color: #ffffff;");
		gotoButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				gotoButton.setStyle("-fx-background-color: #1e90ff;");
			}
		});
		gotoButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				gotoButton.setStyle("-fx-background-color: #ffffff;");
			}
		});

		locBox = new HBox(backButton, locLabel, locField, gotoButton);
		locBox.setStyle("-fx-background-color: #ffffff;" + "-fx-border-color: #d3d3d3;" + "-fx-border-width:0.5px;");
		locBox.setSpacing(10);
		locBox.setPadding(new Insets(5, 5, 5, 5));

	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initTables() {
		blockTable = new TableView<DiskBlock>();
		openedTable = new TableView<File>();

		blockTable
				.setStyle("-fx-background-color: #ffffff;" + "-fx-border-color: #d3d3d3;" + "-fx-border-width:0.5px;");
		openedTable
				.setStyle("-fx-background-color: #ffffff;" + "-fx-border-color: #d3d3d3;" + "-fx-border-width:0.5px;");

		dataBlock = FXCollections.observableArrayList(fat.getDiskBlocks());
		dataOpened = fat.getOpenedFiles();

		TableColumn noCol = new TableColumn("磁盘块");
		noCol.setCellValueFactory(new PropertyValueFactory<DiskBlock, String>("noP"));
		noCol.setSortable(false);
		noCol.setMaxWidth(50);
		noCol.setResizable(false);

		TableColumn indexCol = new TableColumn("值");
		indexCol.setCellValueFactory(new PropertyValueFactory<DiskBlock, String>("indexP"));
		indexCol.setSortable(false);
		indexCol.setMaxWidth(50);
		indexCol.setResizable(false);

		TableColumn typeCol = new TableColumn("类型");
		typeCol.setCellValueFactory(new PropertyValueFactory<DiskBlock, String>("typeP"));
		typeCol.setSortable(false);
		typeCol.setMaxWidth(50);
		typeCol.setResizable(false);

		TableColumn objCol = new TableColumn("内容");
		objCol.setCellValueFactory(new PropertyValueFactory<DiskBlock, String>("objectP"));
		objCol.setSortable(false);
		objCol.setMinWidth(133);
		objCol.setResizable(false);

		TableColumn nameCol = new TableColumn("文件名");
		nameCol.setCellValueFactory(new PropertyValueFactory<File, String>("fileNameP"));
		nameCol.setSortable(false);
		nameCol.setMinWidth(156);
		nameCol.setResizable(false);

		TableColumn flagCol = new TableColumn("打开方式");
		flagCol.setCellValueFactory(new PropertyValueFactory<File, String>("flagP"));
		flagCol.setSortable(false);
		flagCol.setResizable(false);

		TableColumn diskCol = new TableColumn("起始盘块号");
		diskCol.setCellValueFactory(new PropertyValueFactory<File, String>("diskNumP"));
		diskCol.setSortable(false);
		diskCol.setResizable(false);

		TableColumn pathCol = new TableColumn("路径");
		pathCol.setCellValueFactory(new PropertyValueFactory<File, String>("locationP"));
		pathCol.setSortable(false);
		pathCol.setMinWidth(500);
		pathCol.setResizable(false);

		TableColumn lengthCol = new TableColumn("文件长度");
		lengthCol.setCellValueFactory(new PropertyValueFactory<File, String>("lengthP"));
		lengthCol.setSortable(false);
		lengthCol.setResizable(false);

		blockTable.setItems(dataBlock);
		blockTable.getColumns().addAll(noCol, indexCol, typeCol, objCol);
		blockTable.setEditable(false);
		blockTable.setPrefWidth(300);

		openedTable.setItems(dataOpened);
		openedTable.getColumns().addAll(nameCol, flagCol, diskCol, pathCol, lengthCol);
		openedTable.setPrefHeight(200);
	}

	private void initTreeView() {
		rootNode = new TreeItem<>("C:", new ImageView(FATUtil.DISK_IMG));
		rootNode.setExpanded(true);

		recentNode = rootNode;
		pathMap.put(fat.getPath("C:"), rootNode);

		treeView = new TreeView<String>(rootNode);
		treeView.setPrefWidth(200);
		treeView.setCellFactory((TreeView<String> p) -> new TextFieldTreeCellImpl());
		treeView.setStyle("-fx-background-color: #ffffff;" + "-fx-border-color: #d3d3d3;" + "-fx-border-width:0.5px;");
		
		for (Path path : fat.getPaths()) {
			System.out.println(path);
			if (path.hasParent() && path.getParent().getPathName().equals(rootNode.getValue())) {
				initTreeNode(path, rootNode);
			}
		}
		addIcon(fat.getBlockList(recentPath), recentPath);
	}
	
	private void initTreeNode(Path newPath, TreeItem<String> parentNode) {
		TreeItem<String> newNode = addNode(parentNode, newPath);
		if (newPath.hasChild()) {
			for (Path child : newPath.getChildren()) {
				initTreeNode(child, newNode);
			}
		}
	}

	private void addIcon(List<DiskBlock> bList, String path) {
		blockList = bList;
		int n = bList.size();
		icons = new Label[n];
		for (int i = 0; i < n; i++) {
			if (bList.get(i).getObject() instanceof Folder) {
				icons[i] = new Label(((Folder) bList.get(i).getObject()).getFolderName(),
						new ImageView(FATUtil.FOLDER_IMG));
			} else {
				icons[i] = new Label(((File) bList.get(i).getObject()).getFileName(), new ImageView(FATUtil.FILE_IMG));
			}
			icons[i].setContentDisplay(ContentDisplay.TOP);
			icons[i].setWrapText(true);
			flowPane.getChildren().add(icons[i]);
			icons[i].setOnMouseEntered(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					((Label) event.getSource()).setStyle("-fx-background-color: #f0f8ff;");
				}
			});
			icons[i].setOnMouseExited(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					((Label) event.getSource()).setStyle("-fx-background-color: #ffffff;");
				}
			});
			icons[i].setOnMouseClicked(new EventHandler<MouseEvent>() {

				@Override
				public void handle(MouseEvent event) {
					Label src = (Label) event.getSource();
					for (int j = 0; j < n; j++) {
						if (src == icons[j]) {
							index = j;
						}
					}
					if (event.getButton() == MouseButton.SECONDARY && event.getClickCount() == 1) {
						contextMenu2.show(src, event.getScreenX(), event.getScreenY());
					} else if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 2) {
						onOpen();
					} else {
						contextMenu2.hide();
					}
				}
			});
		}
	}

	private TreeItem<String> addNode(TreeItem<String> parentNode, Path newPath) {
		String pathName = newPath.getPathName();
		String value = pathName.substring(pathName.lastIndexOf('\\') + 1);
		TreeItem<String> newNode = new TreeItem<String>(value, new ImageView(FATUtil.TREE_NODE_IMG));
		newNode.setExpanded(true);
		pathMap.put(newPath, newNode);
		parentNode.getChildren().add(newNode);
		return newNode;
	}

	public void removeNode(TreeItem<String> recentNode, Path remPath) {
		recentNode.getChildren().remove(pathMap.get(remPath));
		pathMap.remove(remPath);
	}

	public TreeItem<String> getRecentNode() {
		return recentNode;
	}

	public void setRecentNode(TreeItem<String> recentNode) {
		this.recentNode = recentNode;
	}

//	public void refreshBlockTable() {
//		dataBlock = FXCollections.observableArrayList(fat.getDiskBlocks());
//		blockTable.setItems(dataBlock);
//		blockTable.refresh();
//	}

//	public void refreshOpenedTable() {
//		dataOpened = FXCollections.observableArrayList(fat.getOpenedFiles());
//		openedTable.setItems(dataOpened);
//		openedTable.refresh();
//	}

	private void onOpen() {
		DiskBlock thisBlock = blockList.get(index);
		for (DiskBlock block : blockList) {
			System.out.println(block);
		}
		if (thisBlock.getObject() instanceof File) {
			if (fat.getOpenedFiles().size() < 5) {
				if (fat.isOpenedFile(thisBlock)) {
					Alert duplicate = new Alert(AlertType.ERROR, "文件已打开");
					duplicate.showAndWait();
				} else {
					fat.addOpenedFile(thisBlock);
					new FileView((File) thisBlock.getObject(), fat, thisBlock);
				}
			} else {
				Alert exceed = new Alert(AlertType.ERROR, "文件打开已到上限");
				exceed.showAndWait();
			}
		} else {
			Folder thisFolder = (Folder) thisBlock.getObject();
			String newPath = thisFolder.getLocation() + "\\" + thisFolder.getFolderName();
			flowPane.getChildren().removeAll(flowPane.getChildren());
			addIcon(fat.getBlockList(newPath), newPath);
			locField.setText(newPath);
			recentPath = newPath;
			recentNode = pathMap.get(thisFolder.getPath());
		}
	}	

	public final class TextFieldTreeCellImpl extends TreeCell<String> {

		private TextField textField;

		public TextFieldTreeCellImpl() {

			this.setOnMouseClicked(new EventHandler<MouseEvent>() {
				@Override
				public void handle(MouseEvent event) {
					if (event.getButton() == MouseButton.PRIMARY && event.getClickCount() == 1) {
						if (getTreeItem() != null) {
							String pathName = null;
							for (Map.Entry<Path, TreeItem<String>> entry : pathMap.entrySet()) {
								if (getTreeItem() == entry.getValue()) {
									pathName = entry.getKey().getPathName();
									break;
								}
							}
							List<DiskBlock> fats = fat.getBlockList(pathName);
							flowPane.getChildren().removeAll(flowPane.getChildren());
							addIcon(fats, pathName);
							recentPath = pathName;
							recentNode = getTreeItem();
							locField.setText(recentPath);
						}
					}
				}
			});
		}

		@Override
		public void startEdit() {
			super.startEdit();

			if (textField == null) {
				createTextField();
			}
			setText(null);
			setGraphic(textField);
			textField.selectAll();
		}

		@Override
		public void cancelEdit() {
			super.cancelEdit();
			setText((String) getItem());
			setGraphic(getTreeItem().getGraphic());
		}

		@Override
		public void updateItem(String item, boolean empty) {
			super.updateItem(item, empty);

			if (empty) {
				setText(null);
				setGraphic(null);
			} else {
				if (isEditing()) {
					if (textField != null) {
						textField.setText(getString());
					}
					setText(null);
					setGraphic(textField);
				} else {
					setText(getString());
					setGraphic(getTreeItem().getGraphic());
				}
			}
		}

		private void createTextField() {
			textField = new TextField(getString());
			textField.setOnKeyReleased((KeyEvent t) -> {
				if (t.getCode() == KeyCode.ENTER) {
					commitEdit(textField.getText());
				} else if (t.getCode() == KeyCode.ESCAPE) {
					cancelEdit();
				}
			});

		}

		private String getString() {
			return getItem() == null ? "" : getItem().toString();
		}

	}

}
