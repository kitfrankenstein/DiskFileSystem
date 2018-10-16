package view;

import model.FAT;
import model.File;
import model.Folder;
import model.OpenedFile;
import model.Path;
import model.Utility;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import controller.FATManager;
import javafx.application.Platform;
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
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.FlowPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

/**
* @author Kit
* @version: 2018年9月25日 下午11:19:30
* 
*/
public class MainView {

	private FATManager fatManager;
	private int index;
	private List<FAT> fatList;
	private String recentPath;
	private Map<Path, TreeItem<String>> pathMap;
	
	private Scene scene;
	private BorderPane borderPane;
	
	private FlowPane flowPane;
	private Label[] icons;
	
	private HBox locBox;
	private Label locLabel;
	private TextField locField;
	private Button gotoButton, backButton;
	private MenuBar menuBar;
	private Menu startMenu;
	private MenuItem exItem;
	private VBox vBox;
	
	private TreeView<String> treeView;
	private TreeItem<String> rootNode, recentNode;
	private ImageView rootImg = new ImageView(Utility.diskPath);
	
	private TableView<FAT> fatTable;
	private TableView<OpenedFile> openedTable;
	private ObservableList<FAT> dataFAT;
	private ObservableList<OpenedFile> dataOpened;
	
	private ContextMenu contextMenu, contextMenu2; 
	private MenuItem createFileItem, createFolderItem, openItem,
						renameItem, delItem, propItem;
	
	public MainView(Stage stage) {
		fatManager = new FATManager();
		pathMap = new HashMap<Path, TreeItem<String>>();
		recentPath = "C:";
		initFrame(stage);
	}
	
	private void initFrame(Stage stage) {
		
		initContextMenu();
		menuItemSetOnAction();
		
		initTopBox();
		initTables();
		initTreeView();
				
		flowPane = new FlowPane();
		flowPane.setPrefSize(400, 100);
		flowPane.setStyle("-fx-background-color: #ffffff;");
		flowPane.addEventHandler(MouseEvent.MOUSE_CLICKED,  (MouseEvent me) -> {
			if (me.getButton() == MouseButton.SECONDARY &&
					!contextMenu2.isShowing()) {
				contextMenu.show(flowPane, me.getScreenX(), me.getScreenY());
			} else {
				contextMenu.hide();
			}
		});		
	    
		borderPane = new BorderPane();
                     
        borderPane.setCenter(flowPane);        
        borderPane.setTop(vBox);
        borderPane.setBottom(openedTable);
        borderPane.setLeft(treeView);
        borderPane.setRight(fatTable);

        borderPane.setStyle("-fx-background-radius: 3px;"
        		+ "-fx-border-color:#95AFA6;"
        		+ "-fx-border-width:3px;"
        		+ "-fx-border-radius:3px;");
        
        scene = new Scene(borderPane);
        scene.setFill(Color.TRANSPARENT);
        stage.setScene(scene);
        stage.getIcons().add(new Image(Utility.diskPath));
        stage.setTitle("模拟磁盘文件系统");
        stage.show();
	}
	
	private void initContextMenu() {
		createFileItem = new MenuItem("新建文件");
		createFolderItem = new MenuItem("新建文件夹");
		
		openItem = new MenuItem("打开");
		delItem = new MenuItem("删除");
		renameItem = new MenuItem("重命名");
		propItem = new MenuItem("属性");
		
		contextMenu = new ContextMenu(createFileItem, createFolderItem);		
		contextMenu2 = new ContextMenu(openItem, delItem,
				renameItem, propItem);
	}
	
	private void menuItemSetOnAction() {
		createFileItem.setOnAction(ActionEvent -> {
			int no = fatManager.createFile(locField.getText());
			if (no == Utility.ERROR) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("磁盘容量已满，无法创建");
				alert.showAndWait();
			} else {				
				flowPane.getChildren().removeAll(flowPane.getChildren());
				addIcon(fatManager.getFATList(locField.getText()), locField.getText());
				refreshFATTable();
			}			
		});
		
		createFolderItem.setOnAction(ActionEvent -> {
			int no = fatManager.createFolder(locField.getText());
			if (no == Utility.ERROR) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText("磁盘容量已满，无法创建");
				alert.showAndWait();
			} else {
				Folder newFolder = (Folder)fatManager.getFAT(no).getObject();
				Path newPath = newFolder.getPath();
				flowPane.getChildren().removeAll(flowPane.getChildren());
				addIcon(fatManager.getFATList(locField.getText()), locField.getText());
				refreshFATTable();
				addNode(recentNode, newPath);
			}
		});
		
		openItem.setOnAction(ActionEvent -> {
			FAT thisFAT = fatList.get(index);
			if (thisFAT.getObject() instanceof File) {
				if (fatManager.getOpenedFiles().size() < Utility.num) {
					if (fatManager.isOpenedFile(thisFAT)) {
						Alert duplicate = new Alert(AlertType.ERROR, "文件已打开");
						duplicate.showAndWait();
					} else {
						fatManager.addOpenedFile(thisFAT, Utility.flagWrite);
						refreshOpenedTable();
						new FileView((File)thisFAT.getObject(),
								fatManager, thisFAT, MainView.this);						
					}
				} else {
					Alert exceed = new Alert(AlertType.ERROR, "文件打开已到上限");
					exceed.showAndWait();
				}								
			} else {
				Folder thisFolder = (Folder)thisFAT.getObject();
				String newPath = thisFolder.getLocation() +
						"\\" + thisFolder.getFolderName();
				flowPane.getChildren().removeAll(flowPane.getChildren());
				addIcon(fatManager.getFATList(newPath), newPath);
				locField.setText(newPath);
				recentPath = newPath;
				recentNode = pathMap.get(thisFolder.getPath());
			}
		});
		
		delItem.setOnAction(ActionEvent -> {
			FAT thisFAT = fatList.get(index);						
			if (fatManager.isOpenedFile(thisFAT)) {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setHeaderText(null);
				alert.setContentText("文件未关闭");
				alert.showAndWait();
			} else {
				new delView(thisFAT, fatManager, MainView.this);
				flowPane.getChildren().removeAll(flowPane.getChildren());
				addIcon(fatManager.getFATList(locField.getText()), locField.getText());
				refreshFATTable();
			}			
		});
		
		renameItem.setOnAction(ActionEvent -> {
			FAT thisFAT = fatList.get(index);
			new RenameView(thisFAT, fatManager, icons[index], MainView.this,
					pathMap);
		});
		
		propItem.setOnAction(ActionEvent -> {
			FAT thisFAT = fatList.get(index);
			new PropertyView(thisFAT, fatManager, icons[index], MainView.this,
					pathMap);
		});
				
	}
	
	private void initTopBox() {
		locLabel = new Label("当前目录：");	
		locField = new TextField("C:");
		locField.setPrefWidth(400);
		backButton = new Button("后退");
		backButton.setOnAction(ActionEvent -> {
			System.out.println(recentPath);
			Path backPath = fatManager.getPath(recentPath).getParent();
			if (backPath != null) {
				List<FAT> fats = fatManager.getFATList(backPath.getPathName());
				flowPane.getChildren().removeAll(flowPane.getChildren());
				addIcon(fats, backPath.getPathName());
				recentPath = backPath.getPathName();
				recentNode = pathMap.get(backPath);
				locField.setText(recentPath);
			}
		});
		gotoButton = new Button("前往");
		gotoButton.setOnAction(ActionEvent -> {
			String textPath = locField.getText();
			Path gotoPath = fatManager.getPath(textPath);
			if (gotoPath != null) {
				List<FAT> fats = fatManager.getFATList(textPath);
				flowPane.getChildren().removeAll(flowPane.getChildren());
				addIcon(fats, textPath);
				recentPath = textPath;
				recentNode = pathMap.get(gotoPath);
			} else {
				Alert alert = new Alert(AlertType.ERROR);
				alert.setContentText("目录不存在");
				alert.setHeaderText(null);
				alert.show();
			}			
		});
		locBox = new HBox(backButton, locLabel, locField, gotoButton);	
		locBox.setStyle("-fx-background-color: #ffffff;"
				+ "-fx-border-color: #d3d3d3;");
		locBox.setSpacing(10);
		locBox.setPadding(new Insets(5, 5, 5, 5));
		
		exItem = new MenuItem("退出");
		exItem.setOnAction(ActionEvent -> Platform.exit());
		startMenu = new Menu("开始", null, exItem);
		menuBar = new MenuBar(startMenu);
		menuBar.setPadding(new Insets(0));
		vBox = new VBox(locBox, menuBar);
	}
	
	@SuppressWarnings({ "rawtypes", "unchecked" })
	private void initTables() {
		fatTable = new TableView<FAT>();
		openedTable = new TableView<OpenedFile>();
				
		dataFAT = FXCollections.observableArrayList(fatManager.getFATs());
		dataOpened = FXCollections.observableArrayList(fatManager.getOpenedFiles());
	    		
		TableColumn noCol = new TableColumn("磁盘块");
	    noCol.setCellValueFactory(
                new PropertyValueFactory("no"));
	    noCol.setSortable(false);
	    noCol.setMaxWidth(50);
	    noCol.setResizable(false);
	    
	    TableColumn indexCol = new TableColumn("值");
	    indexCol.setCellValueFactory(
                new PropertyValueFactory("index"));
	    indexCol.setSortable(false);
	    indexCol.setMaxWidth(50);
	    indexCol.setResizable(false);
	    
	    TableColumn typeCol = new TableColumn("类型");
	    typeCol.setCellValueFactory(
	    		new PropertyValueFactory("type"));
	    typeCol.setSortable(false);
	    typeCol.setMaxWidth(50);
	    typeCol.setResizable(false);
	    
	    TableColumn objCol = new TableColumn("内容");
	    objCol.setCellValueFactory(
	    		new PropertyValueFactory("object"));
	    objCol.setSortable(false);
	    objCol.setMinWidth(135);
	    objCol.setResizable(false);
	    
	    TableColumn nameCol = new TableColumn("文件名");
	    nameCol.setCellValueFactory(
	    		new PropertyValueFactory("fileName"));
	    nameCol.setSortable(false);
	    
	    TableColumn flagCol = new TableColumn("打开方式");
	    flagCol.setCellValueFactory(
	    		new PropertyValueFactory("flag"));
	    flagCol.setSortable(false);
	    
	    TableColumn diskCol = new TableColumn("起始盘块号");
	    diskCol.setCellValueFactory(
	    		new PropertyValueFactory("diskNum"));
	    diskCol.setSortable(false);
	    
	    TableColumn pathCol = new TableColumn("路径");
	    pathCol.setCellValueFactory(
	    		new PropertyValueFactory("path"));
	    pathCol.setSortable(false);
	    pathCol.setMinWidth(500);
	    
	    TableColumn lengthCol = new TableColumn("文件长度");
	    lengthCol.setCellValueFactory(
	    		new PropertyValueFactory("length"));
	    lengthCol.setSortable(false);
	    
	    fatTable.setItems(dataFAT);        
	    fatTable.getColumns().addAll(noCol, indexCol, typeCol, objCol);
	    fatTable.setEditable(false);
	    fatTable.setPrefWidth(300);
	    
	    openedTable.setItems(dataOpened);
	    openedTable.getColumns().addAll(nameCol, flagCol, diskCol, pathCol, lengthCol);
	    openedTable.setPrefHeight(200);
	}
	
	private void initTreeView() {
		rootImg.setFitWidth(16);
        rootImg.setFitHeight(16);
        rootNode = new TreeItem<>("C:",rootImg);
        rootNode.setExpanded(true);
        
        recentNode = rootNode;
        pathMap.put(fatManager.getPath("C:"), rootNode);
        
        treeView = new TreeView<String>(rootNode);
        treeView.setPrefWidth(200);
        treeView.setCellFactory((TreeView<String> p) -> new TextFieldTreeCellImpl());
	}
	
	private void addIcon(List<FAT> fList, String path) {
		fatList = fList;
		int n = fList.size();
		flowPane.setPrefSize(482, Utility.getHeight(n));
		icons = new Label[n];
		for (int i = 0; i < n; i++) {
			if (fList.get(i).getIndex() == Utility.END){
				if (fList.get(i).getObject() instanceof Folder){
					icons[i] = new Label(((Folder)fList.get(i).getObject()).getFolderName(),
							new ImageView(Utility.folderPath));					
//					icons[i].setLabelFor(folderIcon);
//					System.out.println(icons[i].labelForProperty());
				} else {
					icons[i] = new Label(((File)fList.get(i).getObject()).getFileName(),
							new ImageView(Utility.filePath));
//					icons[i].setLabelFor(fileIcon);
//					System.out.println(icons[i].labelForProperty());
				}
				icons[i].setContentDisplay(ContentDisplay.TOP);
				icons[i].setWrapText(true);
				flowPane.getChildren().add(icons[i]);
				icons[i].setOnMouseEntered(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						// TODO Auto-generated method stub
						((Label)event.getSource()).setStyle("-fx-background-color: #f0f8ff;");
					}
				});
				icons[i].setOnMouseExited(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						// TODO Auto-generated method stub
						((Label)event.getSource()).setStyle("-fx-background-color: #ffffff;");
					}
				});
				icons[i].setOnMouseClicked(new EventHandler<MouseEvent>() {

					@Override
					public void handle(MouseEvent event) {
						// TODO Auto-generated method stub
						Label src = (Label)event.getSource();
						for (int j = 0; j < n; j++) {
							if (src == icons[j]) {
								index = j;
							}
						}
						if (event.getButton() == MouseButton.SECONDARY &&
								event.getClickCount() == 1) {
							contextMenu2.show(src, event.getScreenX(),
									event.getScreenY());
						} else if (event.getButton() == MouseButton.PRIMARY &&
								event.getClickCount() == 2) {							
							FAT thisFAT = fList.get(index);			
							if (thisFAT.getObject() instanceof File) {
								if (fatManager.getOpenedFiles().size() < Utility.num) {
									if (fatManager.isOpenedFile(thisFAT)) {
										Alert duplicate = new Alert(AlertType.ERROR, "文件已打开");
										duplicate.showAndWait();
									} else {
										fatManager.addOpenedFile(thisFAT, Utility.flagWrite);
										refreshOpenedTable();
										new FileView((File)thisFAT.getObject(),
												fatManager, thisFAT, MainView.this);										
									}
								} else {
									Alert exceed = new Alert(AlertType.ERROR, "文件打开已到上限");
									exceed.showAndWait();
								}								
							} else {
								Folder thisFolder = (Folder)thisFAT.getObject();
								String newPath = thisFolder.getLocation() +
										"\\" + thisFolder.getFolderName();
								flowPane.getChildren().removeAll(flowPane.getChildren());
								addIcon(fatManager.getFATList(newPath), newPath);
								locField.setText(newPath);
								recentPath = newPath;
								recentNode = pathMap.get(thisFolder.getPath());
								System.out.println(recentPath);
							}
						} else {
							contextMenu2.hide();
						}
					}
				});
			}
		}
	}
	
	public void addNode(TreeItem<String> parentNode, Path newPath) {
		String pathName = newPath.getPathName();
		String value = pathName.substring(pathName.lastIndexOf('\\') + 1);
		TreeItem<String> newNode = new TreeItem<String>(value);
		newNode.setExpanded(true);
		pathMap.put(newPath, newNode);
		parentNode.getChildren().add(newNode);
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

	public void refreshFATTable() {
		dataFAT = FXCollections.observableArrayList(fatManager.getFATs());			
		fatTable.setItems(dataFAT);
		fatTable.refresh();
	}
	
	public void refreshOpenedTable() {
		dataOpened = FXCollections.observableArrayList(fatManager.getOpenedFiles());
		openedTable.setItems(dataOpened);
		openedTable.refresh();
	}
	    
	public final class TextFieldTreeCellImpl extends TreeCell<String> {

		private TextField textField;

	    public TextFieldTreeCellImpl() {
	        
	        this.setOnMouseClicked(new EventHandler<MouseEvent>() {	
				@Override
				public void handle(MouseEvent event) {
					// TODO Auto-generated method stub
					if (event.getButton() == MouseButton.PRIMARY &&
							event.getClickCount() == 1) {
						String pathName = null;
						for (Map.Entry<Path, TreeItem<String>> entry :
							pathMap.entrySet()) {
	        				if (getTreeItem() == entry.getValue()) {
	        					pathName = entry.getKey().getPathName();
	        					break;
	        				}
	        			}
	    				List<FAT> fats = fatManager.getFATList(pathName);
	    				flowPane.getChildren().removeAll(flowPane.getChildren());
	    				addIcon(fats, pathName);
	    				recentPath = pathName;
	    				recentNode = getTreeItem();
	    				locField.setText(recentPath);
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
