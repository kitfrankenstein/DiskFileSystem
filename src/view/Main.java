package view;

import javafx.application.Application;
import javafx.stage.Stage;

/**
* @author Kit
* @version: 2018年9月25日 下午11:18:09
* 
*/
public class Main extends Application{

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		launch(args);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		// TODO Auto-generated method stub
		new MainView(primaryStage);
	}

}
