package view;

import java.util.Optional;

import controller.FATManager;
import javafx.event.EventHandler;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.scene.control.ButtonBar.ButtonData;
import javafx.scene.layout.BorderPane;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Menu;
import javafx.scene.control.MenuBar;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TextArea;
import javafx.scene.image.Image;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.FAT;
import model.File;
import model.Folder;
import model.Utility;

/**
 * @author Kit
 * @version: 2018年9月27日 下午7:01:22
 * 
 */
public class FileView {

	private File file;
	private FATManager fatManager;
	private FAT fat;
	private MainView mainView;
	private String newContent, oldContent;
	private Stage stage;
	private Scene scene;
	private BorderPane borderPane;
	private TextArea contentField;
	private MenuBar menuBar;
	private Menu fileMenu;
	private MenuItem saveItem, closeItem;

	public FileView(File file, FATManager fatManager, FAT fat, MainView mainView) {
		this.file = file;
		this.fatManager = fatManager;
		this.fat = fat;
		this.mainView = mainView;
		showView();
	}

	private void showView() {
		System.out.println(file.getParent());
		contentField = new TextArea();
		contentField.setPrefRowCount(25);
		contentField.setWrapText(true);
		contentField.setText(file.getContent());

		saveItem = new MenuItem("保存");
		saveItem.setOnAction(ActionEvent -> {
			newContent = contentField.getText();
			oldContent = file.getContent();
			if (newContent == null) {
				newContent = "";
			}
			if (!newContent.equals(oldContent)) {
				saveContent(newContent);
			}
		});
		closeItem = new MenuItem("关闭");
		closeItem.setOnAction(ActionEvent -> stage.close());
		fileMenu = new Menu("File", null, saveItem, closeItem);
		menuBar = new MenuBar(fileMenu);
		menuBar.setPadding(new Insets(0));

		borderPane = new BorderPane(contentField, menuBar, null, null, null);

		scene = new Scene(borderPane);
		stage = new Stage();
		stage.setScene(scene);
		stage.setTitle(file.getFileName());
		stage.getIcons().add(new Image(Utility.filePath));
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				// TODO Auto-generated method stub
				newContent = contentField.getText();
				oldContent = file.getContent();
				boolean isCancel = false;
				if (newContent == null) {
					newContent = "";
				}
				System.out.println(newContent + " newContent");
				if (!newContent.equals(oldContent)) {
					event.consume();
					// Alert alert = new Alert(AlertType.CONFIRMATION, "sure?");
					// Optional<ButtonType> result = alert.showAndWait();
					Alert alert = new Alert(AlertType.CONFIRMATION);
					alert.setTitle("保存更改");
					alert.setHeaderText(null);
					alert.setContentText("文件内容已更改，是否保存?");
					ButtonType saveType = new ButtonType("保存");
					ButtonType noType = new ButtonType("不保存");
					ButtonType cancelType = new ButtonType("取消", ButtonData.CANCEL_CLOSE);
					alert.getButtonTypes().setAll(saveType, noType, cancelType);
					Optional<ButtonType> result = alert.showAndWait();
					if (result.get() == saveType) {
						// if (newContent == null) {
						// newContent = "";
						// }
						saveContent(newContent);
					} else if (result.get() == cancelType) {
						isCancel = true;
					}
				}
				if (!isCancel) {
					fatManager.removeOpenedFile(fat);
					mainView.refreshOpenedTable();
					stage.close();
				}
			}
		});
		stage.show();
	}

	private void saveContent(String newContent) {
		int newLength = newContent.length();
		int FATcount = Utility.getNumOfFAT(newLength);
		file.setLength(FATcount);
		file.setContent(newContent);
		file.setSize(Utility.getSize(newLength));
		if (file.hasParent()) {
			Folder parent = (Folder) file.getParent();
			parent.setSize(Utility.getFolderSize(parent));
			while (parent.hasParent()) {
				parent = (Folder) parent.getParent();
				parent.setSize(Utility.getFolderSize(parent));
			}
			System.out.println(parent.getSize());
		}
		fatManager.reallocFAT(FATcount, fat);
		mainView.refreshFATTable();
		mainView.refreshOpenedTable();
	}

}