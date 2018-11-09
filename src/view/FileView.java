package view;

import java.util.Optional;

import javafx.event.Event;
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
import javafx.scene.image.ImageView;
import javafx.stage.Stage;
import javafx.stage.WindowEvent;
import model.DiskBlock;
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
	private FAT fat;
	private DiskBlock block;
	private MainView mainView;
	private String newContent, oldContent;
	private Stage stage;
	private Scene scene;
	private BorderPane borderPane;
	private TextArea contentField;
	private MenuBar menuBar;
	private Menu fileMenu;
	private MenuItem saveItem, closeItem;

	public FileView(File file, FAT fat, DiskBlock block, MainView mainView) {
		this.file = file;
		this.fat = fat;
		this.block = block;
		this.mainView = mainView;
		showView();
	}

	private void showView() {
		//System.out.println(file.getParent());
		contentField = new TextArea();
		contentField.setPrefRowCount(25);
		contentField.setWrapText(true);
		contentField.setText(file.getContent());

		saveItem = new MenuItem("保存");
		saveItem.setGraphic(new ImageView(Utility.saveImg));
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
		closeItem.setGraphic(new ImageView(Utility.closeImg));
		closeItem.setOnAction(ActionEvent -> onClose(ActionEvent));		
		
		fileMenu = new Menu("File", null, saveItem, closeItem);
		menuBar = new MenuBar(fileMenu);
		menuBar.setPadding(new Insets(0));

		borderPane = new BorderPane(contentField, menuBar, null, null, null);

		scene = new Scene(borderPane);
		stage = new Stage();
		stage.setScene(scene);
		stage.setTitle(file.getFileName());
		stage.getIcons().add(new Image(Utility.fileImg));
		stage.setOnCloseRequest(new EventHandler<WindowEvent>() {

			@Override
			public void handle(WindowEvent event) {
				// TODO Auto-generated method stub
				onClose(event);
			}
		});
		stage.show();
	}

	private void onClose(Event event) {
		newContent = contentField.getText();
		oldContent = file.getContent();
		boolean isCancel = false;
		if (newContent == null) {
			newContent = "";
		}
		System.out.println(newContent + " newContent");
		if (!newContent.equals(oldContent)) {
			event.consume();
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
				saveContent(newContent);
			} else if (result.get() == cancelType) {
				isCancel = true;
			}
		}
		if (!isCancel) {
			fat.removeOpenedFile(block);
			mainView.refreshOpenedTable();
			stage.close();
		}
	}
	
	private void saveContent(String newContent) {
		int newLength = newContent.length();
		int blockCount = Utility.blocksCount(newLength);
		file.setLength(blockCount);
		file.setContent(newContent);
		file.setSize(Utility.getSize(newLength));
		if (file.hasParent()) {
			Folder parent = (Folder) file.getParent();
			parent.setSize(Utility.getFolderSize(parent));
			while (parent.hasParent()) {
				parent = (Folder) parent.getParent();
				parent.setSize(Utility.getFolderSize(parent));
			}
		}
		fat.reallocBlocks(blockCount, block);
		mainView.refreshBlockTable();
		mainView.refreshOpenedTable();
	}
	
}
