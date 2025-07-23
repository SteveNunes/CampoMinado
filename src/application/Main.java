package application;

import java.io.IOException;

import entity.Game;
import gui.ConfigsController;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;

public class Main extends Application {

	private static boolean closed;

	@Override
	public void start(Stage stage) throws Exception {
		openConfigWindow();
	}
	
	public static void main(String[] args) {
		launch(args);
	}

	public static void openConfigWindow() {
		try {
			Stage stage = new Stage();
			FXMLLoader loader = new FXMLLoader(Game.class.getResource("/gui/ConfigsView.fxml"));
			stage.setScene(new Scene((VBox)loader.load()));
			((ConfigsController)loader.getController()).init(stage);
			stage.setTitle("Campo Minado");
			stage.setResizable(false);
			stage.setOnCloseRequest(e -> Game.close());
			stage.show();
		}
		catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static boolean isClosed() {
		return closed;
	}

}
