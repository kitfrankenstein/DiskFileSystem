package view;

import java.util.Optional;

import controller.FATManager;
import javafx.scene.control.Alert;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Alert.AlertType;

import model.FAT;
import model.File;
import model.Folder;
import model.Path;

/**
* @author Kit
* @version: 2018年10月2日 下午3:02:06
* 
*/
public class delView {

	private FAT fat;
	private FATManager fatManager;
	private MainView mainView;
	private Alert mainAlert, okAlert, errAlert;	
	
	public delView(FAT fat, FATManager fatManager, MainView mainView) {
		this.fat = fat;
		this.fatManager = fatManager;
		this.mainView = mainView;
		showView();
	}
	
	private void showView() {
		String mesg = "";
		if (fat.getObject() instanceof Folder) {
			Folder folder = (Folder)fat.getObject();
			mesg = folder.getFolderName()
					+ "\n类型:" + folder.getType()
					+ "\n大小:" + folder.getSize()
					+ "\n创建时间:" + folder.getCreateTime();
		} else {
			File file = (File)fat.getObject();
			mesg = file.getFileName()
					+ "\n类型:" + file.getType()
					+ "\n大小:" + file.getSize() + "KB"
					+ "\n创建时间:" + file.getCreateTime();
		}
		mainAlert = new Alert(AlertType.CONFIRMATION);
		mainAlert.setHeaderText("确认删除");
		mainAlert.setContentText(mesg);
		okAlert = new Alert(AlertType.INFORMATION);
		errAlert = new Alert(AlertType.ERROR);
		showAlert();
	}
	
	private void showAlert() {
				
		Optional<ButtonType> result = mainAlert.showAndWait();	
		Path thisPath = null;
		if (result.get() == ButtonType.OK) {
			if (fat.getObject() instanceof Folder) {
				thisPath = ((Folder)fat.getObject()).getPath();
			}
			int res = fatManager.delete(fat);
			if (res == 0) {//删除文件夹成功
				mainView.removeNode(mainView.getRecentNode(), thisPath);								
				okAlert.setHeaderText(null);
				okAlert.setContentText("删除文件夹成功");
				okAlert.show();
			} else if (res == 1) {
				okAlert.setHeaderText(null);
				okAlert.setContentText("删除文件成功");
				okAlert.show();
			} else if (res == 2) {//文件夹不为空
				errAlert.setHeaderText(null);
				errAlert.setHeaderText("文件夹不为空");
				errAlert.show();
			} else {//文件未关闭				
				errAlert.setHeaderText(null);
				errAlert.setHeaderText("文件未关闭");
				errAlert.show();
			}
		} else {
			
		}
	}
	
}
