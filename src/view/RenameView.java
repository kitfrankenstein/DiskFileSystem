package view;

import java.util.Map;

import controller.FATManager;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.control.TreeItem;
import javafx.scene.layout.HBox;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import model.FAT;
import model.File;
import model.Folder;
import model.Path;

/**
 * @author Kit
 * @version: 2018年10月1日 下午3:42:21
 * 
 */
public class RenameView {

	private FAT fat;
	private FATManager fatManager;
	private Label icon;
	private MainView mainView;
	private Map<Path, TreeItem<String>> pathMap;
	private Stage stage;
	private Scene scene;
	private HBox hBox;
	private TextField nameField;
	private Button okButton, cancelButton;
	private String oldName;

	public RenameView(FAT fat, FATManager fatManager, Label icon,
			MainView mainView,
			Map<Path, TreeItem<String>> pathMap) {
		// TODO Auto-generated constructor stub
		this.fat = fat;
		this.fatManager = fatManager;
		this.icon = icon;
		this.mainView = mainView;
		this.pathMap = pathMap;
		showView();
	}

	private void showView() {
		if (fat.getObject() instanceof Folder) {
			oldName = ((Folder) fat.getObject()).getFolderName();
		} else {
			oldName = ((File) fat.getObject()).getFileName();
		}
		nameField = new TextField(oldName);
		okButton = new Button("更改");
		okButton.setOnAction(ActionEvent -> {
			String newName = nameField.getText();
			if (!newName.equals(oldName)) {
				if (fat.getObject() instanceof Folder) {
					Folder thisFolder = (Folder) fat.getObject();
					thisFolder.setFolderName(newName);
					pathMap.get(thisFolder.getPath()).setValue(newName);
					reLoc(thisFolder.getLocation(), thisFolder.getLocation(),
							oldName, newName, thisFolder);
				} else {
					((File) fat.getObject()).setFileName(newName);
				}
				icon.setText(newName);
				mainView.refreshFATTable();
			}
			stage.close();
		});
		cancelButton = new Button("取消");
		cancelButton.setOnAction(ActionEvent -> stage.close());
		hBox = new HBox(nameField, okButton, cancelButton);
		hBox.setSpacing(5);
		hBox.setPadding(new Insets(5));
		hBox.setStyle("-fx-border-color:#f0f8ff;"
				+ "-fx-border-width:3px;"
				+ "-fx-border-radius:3px;"
				+ "-fx-background-color:#f0f8ff");

		scene = new Scene(hBox);
		stage = new Stage();
		stage.setScene(scene);
		stage.initStyle(StageStyle.UNDECORATED);
		stage.show();
	}

	private void reLoc(String oldP, String newP, String oldN,
			String newN, Folder folder) {
		String oldLoc = oldP + "\\" + oldN;
		String newLoc = newP + "\\" + newN;
		Path oldPath = fatManager.getPath(oldLoc);
		fatManager.replacePath(oldPath, newLoc);
		for (Object child : folder.getChildren()) {
			if (child instanceof File) {
				((File) child).setLocation(newLoc);

			} else {
				Folder nextFolder = (Folder) child;
				nextFolder.setLocation(newLoc);
				if (nextFolder.hasChild()) {
					reLoc(oldLoc, newLoc, nextFolder.getFolderName(),
							nextFolder.getFolderName(), nextFolder);
				} else {
					Path nextPath = fatManager.getPath(oldLoc + "\\" +
							nextFolder.getFolderName());
					fatManager.replacePath(nextPath, newLoc + "\\" +
							nextFolder.getFolderName());
				}
			}
		}
	}
}
