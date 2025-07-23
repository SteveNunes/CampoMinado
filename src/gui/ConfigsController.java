package gui;

import entity.Game;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;

public class ConfigsController {

  @FXML
  private Button buttonStartGame;
  @FXML
  private ComboBox<String> comboBoxFieldSize;
  @FXML
  private ComboBox<String> comboBoxTileSize;
  @FXML
  private ComboBox<Integer> comboBoxTotalBombs;
  
  private int fieldSize;
  
  public void init(Stage stage) {
  	for (int n = 8; n <= 64; n += 8)
  		comboBoxTileSize.getItems().add(n + " px");
  	comboBoxTileSize.getSelectionModel().select(1);
  	comboBoxFieldSize.valueProperty().addListener((o, oldV, newV) -> {
  		String[] split = newV.split(" x ");
  		fieldSize = Integer.parseInt(split[0]);
  		comboBoxTotalBombs.getItems().clear();
    	for (int n = 1; n < fieldSize * fieldSize; n++)
    		comboBoxTotalBombs.getItems().add(n);
    	comboBoxTotalBombs.getSelectionModel().select(comboBoxTotalBombs.getItems().size() / 2);
  	});
  	for (int n = 4; n < 128; n += 4)
  		comboBoxFieldSize.getItems().add(n + " x " + n);
  	comboBoxFieldSize.getSelectionModel().select(1);
  	buttonStartGame.setOnAction(e -> {
  		Game.setFieldSize(fieldSize);
  		int tileSize = Integer.parseInt(comboBoxTileSize.getSelectionModel().getSelectedItem().replace(" px", ""));
  		Game.setTileSize(tileSize);
  		Game.setTotalBombs(comboBoxTotalBombs.getSelectionModel().getSelectedItem());
  		stage.setOnCloseRequest(null);
  		stage.close();
  		Game.start();
  	});
  }
  
}
