package view;

import java.util.Map;

import controller.FATManager;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import model.FAT;
import model.File;
import model.Folder;
import model.Path;

/**
* @author Kit
* @version: 2018年9月29日 上午12:07:57
* 
*/
public class PropertyView {
	
	private FAT fat;
	private FATManager fatManager;
	private Label icon;
	private MainView mainView;
	private Map<Path, TreeItem<String>> pathMap;
	private String oldName;
	private Stage stage;
	private Scene scene;
	private VBox vBox;
	private HBox hBox;
	private GridPane gridPane;
	private TextField nameField;
	private Label typeField, locField, sizeField,
					spaceField, timeField;
	private Button okButton, cancelButton, applyButton;
	
	public PropertyView(FAT fat, FATManager fatManager, Label icon,
			MainView mainView, Map<Path, TreeItem<String>> pathMap) {
		this.fat = fat;
		this.fatManager = fatManager;
		this.icon = icon;
		this.mainView = mainView;
		this.pathMap = pathMap;
		showView();
	}
	
	private void showView() {
		if (fat.getObject() instanceof Folder) {
			Folder folder = (Folder)fat.getObject();
			nameField = new TextField(folder.getFolderName());
			typeField = new Label(folder.getType());
			locField = new Label(folder.getLocation());
			sizeField = new Label(folder.getSize() + "KB");
			spaceField = new Label(folder.getSpace());
			timeField = new Label(folder.getCreateTime());
			oldName = folder.getFolderName();
		} else {
			File file = (File)fat.getObject();
			nameField = new TextField(file.getFileName());
			typeField = new Label(file.getType());
			locField = new Label(file.getLocation());
			sizeField = new Label(file.getSize() + "KB");
			spaceField = new Label(file.getSpace());
			timeField = new Label(file.getCreateTime());
			oldName = file.getFileName();
		}
		
		nameField.textProperty().addListener(new ChangeListener<String>() {

			@Override
			public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
				// TODO Auto-generated method stub
				if (newValue == null || newValue.equals(oldValue)) {
					applyButton.setDisable(true);
				} else {
					applyButton.setDisable(false);
				}
			}
		});
		
		okButton = new Button("确定");
		cancelButton = new Button("取消");
		applyButton = new Button("应用");
		okButton.setPrefSize(100, 20);
		cancelButton.setPrefSize(100, 20);
		applyButton.setPrefSize(100, 20);
		buttonOnAction();
		
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
		gridPane.add(nameField, 1, 0);
		gridPane.add(typeField, 1, 1);
		gridPane.add(locField, 1, 2);
		gridPane.add(sizeField, 1, 3);
		gridPane.add(spaceField, 1, 4);
		gridPane.add(timeField, 1, 5);
		gridPane.setPadding(new Insets(15, 12, 0, 12));
		gridPane.setVgap(10);
		gridPane.setHgap(10);
				
		vBox = new VBox();
		vBox.getChildren().addAll(gridPane, hBox);
		vBox.setStyle("-fx-background-color: #ffffff;");
		
		scene = new Scene(vBox);
		stage = new Stage();
		stage.setScene(scene);
		stage.show();
				
	}
	
	private void buttonOnAction() {		
		applyButton.setOnAction(ActionEvent -> {
			String newName = nameField.getText();
			if (!oldName.equals(newName)) {
				if (fat.getObject() instanceof Folder) {
					Folder thisFolder = (Folder)fat.getObject();
					thisFolder.setFolderName(newName);
					pathMap.get(thisFolder.getPath()).setValue(newName);
					reLoc(thisFolder.getLocation(), thisFolder.getLocation(),
							oldName, newName, thisFolder);
					oldName = newName;
				} else {
					((File)fat.getObject()).setFileName(newName);
				}
				icon.setText(newName);
				mainView.refreshFATTable();
				applyButton.setDisable(true);
			}			
		});
		cancelButton.setOnAction(ActionEvent -> {
			stage.close();
		});
		okButton.setOnAction(ActionEvent -> {
			String newName = nameField.getText();
			if (!oldName.equals(newName)) {
				if (fat.getObject() instanceof Folder) {
					Folder thisFolder = (Folder)fat.getObject();
					thisFolder.setFolderName(newName);
					pathMap.get(thisFolder.getPath()).setValue(newName);
					reLoc(thisFolder.getLocation(), thisFolder.getLocation(),
							oldName, newName, thisFolder);
				} else {
					((File)fat.getObject()).setFileName(newName);
				}
				icon.setText(newName);
				mainView.refreshFATTable();
			}
			stage.close();
		});
	}
	
	private void reLoc(String oldP, String newP, String oldN, String newN, Folder folder) {
		String oldLoc = oldP + "\\" + oldN;
		String newLoc = newP + "\\" + newN;
		Path oldPath = fatManager.getPath(oldLoc);
		fatManager.replacePath(oldPath, newLoc);
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
					Path nextPath = fatManager.getPath(oldLoc + "\\" +
							nextFolder.getFolderName());
					String newNext = newLoc + "\\" + nextFolder.getFolderName();
					fatManager.replacePath(nextPath, newNext);
				}
			}
		}
	}
	
}
