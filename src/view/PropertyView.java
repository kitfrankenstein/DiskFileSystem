package view;

import java.util.Map;

import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;
import javafx.scene.control.Toggle;
import javafx.scene.control.ToggleGroup;
import javafx.scene.control.TreeItem;
import javafx.scene.image.Image;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.DiskBlock;
import model.FAT;
import model.File;
import model.Folder;
import model.Path;
import util.FATUtil;

/**
* @author Kit
* @version: 2018年9月29日 上午12:07:57
* 
*/
public class PropertyView {
	
	private DiskBlock block;
	private FAT fat;
	private Label icon;
	private Map<Path, TreeItem<String>> pathMap;
	private String oldName, location;
	private Stage stage;
	private Scene scene;
	private VBox vBox;
	private HBox hBox;
	private GridPane gridPane;
	private TextField nameField;
	private Label typeField, locField, sizeField,
					spaceField, timeField;
	private Button okButton, cancelButton, applyButton;
	private final ToggleGroup toggleGroup = new ToggleGroup();
	private Image ico;
	
	private String name;
	
	public PropertyView(DiskBlock block, FAT fat, Label icon, Map<Path, TreeItem<String>> pathMap) {
		this.block = block;
		this.fat = fat;
		this.icon = icon;
		this.pathMap = pathMap;
		showView();
	}
	
	private void showView() {		
		RadioButton checkRead = new RadioButton("只读");
		checkRead.setToggleGroup(toggleGroup);
		checkRead.setUserData(FATUtil.FLAGREAD);
		
		RadioButton checkWrite = new RadioButton("读写");
		checkWrite.setToggleGroup(toggleGroup);
		checkWrite.setUserData(FATUtil.FLAGWRITE);
		
		HBox checkBoxGroup = new HBox(checkRead, checkWrite);
		checkBoxGroup.setSpacing(10);
		
		if (block.getObject() instanceof Folder) {
			Folder folder = (Folder)block.getObject();
			nameField = new TextField(folder.getFolderName());
			typeField = new Label(folder.getType());
			locField = new Label(folder.getLocation());
			sizeField = new Label(folder.getSize() + "KB");
			spaceField = new Label(folder.getSpace());
			timeField = new Label(folder.getCreateTime());
			oldName = folder.getFolderName();
			location = folder.getLocation();
			checkRead.setDisable(true);
			checkWrite.setDisable(true);
			ico = new Image(FATUtil.FOLDER_IMG);
		} else {
			File file = (File)block.getObject();
			nameField = new TextField(file.getFileName());
			typeField = new Label(file.getType());
			locField = new Label(file.getLocation());
			sizeField = new Label(file.getSize() + "KB");
			spaceField = new Label(file.getSpace());
			timeField = new Label(file.getCreateTime());
			oldName = file.getFileName();
			location = file.getLocation();
			toggleGroup.selectToggle(file.getFlag() == FATUtil.FLAGREAD ? checkRead : checkWrite);
			ico = new Image(FATUtil.FILE_IMG);
		}
		
		name = nameField.getText();
		
		okButton = new Button("确定");
		okButton.setPrefSize(100, 20);
		okButton.setStyle("-fx-background-color:#d3d3d3;");
		okButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				okButton.setStyle("-fx-background-color: #808080;");
			}
		});
		okButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				okButton.setStyle("-fx-background-color: #d3d3d3;");
			}
		});
		
		cancelButton = new Button("取消");
		cancelButton.setPrefSize(100, 20);
		cancelButton.setStyle("-fx-background-color:#d3d3d3;");
		cancelButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				cancelButton.setStyle("-fx-background-color: #ffffff;");
			}
		});
		cancelButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				cancelButton.setStyle("-fx-background-color: #d3d3d3;");
			}
		});
		
		applyButton = new Button("应用");	
		applyButton.setPrefSize(100, 20);
		applyButton.setStyle("-fx-background-color:#d3d3d3;");
		applyButton.setOnMouseEntered(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				applyButton.setStyle("-fx-background-color: #808080;");
			}
		});
		applyButton.setOnMouseExited(new EventHandler<MouseEvent>() {

			@Override
			public void handle(MouseEvent event) {
				applyButton.setStyle("-fx-background-color: #d3d3d3;");
			}
		});
		
		buttonOnAction();
		
		nameField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				if (newValue.equals("") || newValue.equals(name)) {
					applyButton.setDisable(true);
					okButton.setDisable(true);
				} else {
					applyButton.setDisable(false);
				}
			}
		});
		
		toggleGroup.selectedToggleProperty().addListener(new ChangeListener<Toggle>() {

			@Override
			public void changed(ObservableValue<? extends Toggle> observable, Toggle oldValue, Toggle newValue) {
				applyButton.setDisable(false);
			}
		});
		
		hBox = new HBox(okButton, cancelButton, applyButton);
		hBox.setPadding(new Insets(15, 12, 5, 12));
		hBox.setSpacing(10);
		
		gridPane = new GridPane();		
		gridPane.add(new Label("名称:"), 0, 0);
		gridPane.add(new Label("文件类型:"), 0, 1);
		gridPane.add(new Label("位置:"), 0, 2);
		gridPane.add(new Label("大小:"), 0, 3);
		gridPane.add(new Label("占用空间:"), 0, 4);
		gridPane.add(new Label("建立时间:"), 0, 5);
		gridPane.add(new Label("属性:"), 0, 6);
		gridPane.add(nameField, 1, 0);
		gridPane.add(typeField, 1, 1);
		gridPane.add(locField, 1, 2);
		gridPane.add(sizeField, 1, 3);
		gridPane.add(spaceField, 1, 4);
		gridPane.add(timeField, 1, 5);
		gridPane.add(checkBoxGroup, 1, 6);
		gridPane.setPadding(new Insets(15, 12, 0, 12));
		gridPane.setVgap(10);
		gridPane.setHgap(10);
				
		vBox = new VBox();
		vBox.getChildren().addAll(gridPane, hBox);
		vBox.setStyle("-fx-background-color: #ffffff;");
		
		scene = new Scene(vBox);
		stage = new Stage();
		stage.setScene(scene);
		stage.getIcons().add(ico);
		stage.setAlwaysOnTop(true);
		stage.show();
				
	}
	
	private void buttonOnAction() {		
		applyButton.setOnAction(ActionEvent -> {
			String newName = nameField.getText();
			if (!oldName.equals(newName)) {
				if (fat.hasName(location, newName)) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setHeaderText(null);
					alert.setContentText("此位置已包含同名文件/文件夹");
					alert.show();
				} else {
					if (block.getObject() instanceof Folder) {
						Folder thisFolder = (Folder)block.getObject();
						thisFolder.setFolderName(newName);
						pathMap.get(thisFolder.getPath()).setValue(newName);
						reLoc(location, location, oldName, newName, thisFolder);
					} else {
						((File)block.getObject()).setFileName(newName);
					}
					oldName = newName;
					icon.setText(newName);
				}				
			}
			if (block.getObject() instanceof File) {
				File thisFile = ((File)block.getObject());
				int newFlag = toggleGroup.getSelectedToggle().getUserData().hashCode();
				thisFile.setFlag(newFlag);
			}
			applyButton.setDisable(true);
		});
		cancelButton.setOnAction(ActionEvent -> {
			stage.close();
		});
		okButton.setOnAction(ActionEvent -> {
			String newName = nameField.getText();
			if (!oldName.equals(newName)) {
				if (fat.hasName(location, newName)) {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setHeaderText(null);
					alert.setContentText("此位置已包含同名文件/文件夹");
					alert.showAndWait();
				} else {
					if (block.getObject() instanceof Folder) {
						Folder thisFolder = (Folder)block.getObject();
						thisFolder.setFolderName(newName);
						pathMap.get(thisFolder.getPath()).setValue(newName);
						reLoc(location, location, oldName, newName, thisFolder);
					} else {
						((File)block.getObject()).setFileName(newName);
					}
					icon.setText(newName);
				}
			}
			if (block.getObject() instanceof File) {
				File thisFile = ((File)block.getObject());
				int newFlag = toggleGroup.getSelectedToggle().getUserData().hashCode();
				thisFile.setFlag(newFlag);
			}
			stage.close();
		});
	}
	
	private void reLoc(String oldP, String newP, String oldN, String newN, Folder folder) {
		String oldLoc = oldP + "\\" + oldN;
		String newLoc = newP + "\\" + newN;
		Path oldPath = fat.getPath(oldLoc);
		fat.replacePath(oldPath, newLoc);
		for (Object child : folder.getChildren()) {
			if (child instanceof File) {
				((File) child).setLocation(newLoc);				
			} else {
				Folder nextFolder = (Folder)child;
				nextFolder.setLocation(newLoc);		
				if (nextFolder.hasChild()) {
					reLoc(oldLoc, newLoc, nextFolder.getFolderName(),
							nextFolder.getFolderName(), nextFolder);
				}
				else {
					Path nextPath = fat.getPath(oldLoc + "\\" +
							nextFolder.getFolderName());
					String newNext = newLoc + "\\" + nextFolder.getFolderName();
					fat.replacePath(nextPath, newNext);
				}
			}
		}
	}
	
}
