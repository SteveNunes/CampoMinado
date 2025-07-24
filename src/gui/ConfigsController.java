package gui;

import entity.Game;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.CheckBox;
import javafx.scene.control.ComboBox;
import javafx.stage.Stage;
import util.DesktopUtils;

public class ConfigsController {

  @FXML
  private Button buttonStartGame;
  @FXML
  private ComboBox<String> comboBoxFieldSize;
  @FXML
  private ComboBox<String> comboBoxTileSize;
  @FXML
  private ComboBox<Integer> comboBoxTotalBombs;
  @FXML
  private CheckBox checkBoxOnlySquares;
  
  private int fieldWidth;
  private int fieldHeight;
  private int tileSize;
  
  public void init(Stage stage) {
  	for (int n = 8; n <= 64; n += 8)
  		comboBoxTileSize.getItems().add(n + " px");
  	comboBoxTileSize.valueProperty().addListener((o, oldV, newV) -> {
  		tileSize = Integer.parseInt(newV.replace(" px", ""));
  		refreshCombos();
 		});
  	comboBoxTileSize.getSelectionModel().select(3);
  	comboBoxFieldSize.valueProperty().addListener((o, oldV, newV) -> {
  		if (newV != null) {
	  		String[] split = newV.split(" x ");
	  		fieldWidth = Integer.parseInt(split[0]);
	  		fieldHeight = Integer.parseInt(split[1]);
	  		comboBoxTotalBombs.getItems().clear();
	    	for (int n = fieldWidth < fieldHeight ? fieldWidth : fieldHeight; n < fieldWidth * fieldHeight; n += (n == 1 ? 4 : 5))
	    		comboBoxTotalBombs.getItems().add(n);
	    	comboBoxTotalBombs.getSelectionModel().select(comboBoxTotalBombs.getItems().size() / 10);
  		}
  	});
  	comboBoxFieldSize.getSelectionModel().select(0);
  	checkBoxOnlySquares.selectedProperty().addListener((o, oldV, newV) -> refreshCombos());
  	buttonStartGame.setOnAction(e -> {
  		Game.setFieldSize(fieldWidth, fieldHeight);
  		Game.setTileSize(tileSize);
  		Game.setTotalBombs(comboBoxTotalBombs.getSelectionModel().getSelectedItem());
  		stage.setOnCloseRequest(null);
  		stage.close();
  		Game.start();
  	});
  }

	private void refreshCombos() {
		int maxW = DesktopUtils.getSystemScreenWidth();
		int maxH = DesktopUtils.getSystemScreenHeight();
		comboBoxFieldSize.getItems().clear();
		for (int h = 6; (h * tileSize) < (maxH * 0.94); h += 4)
    	for (int w = h; (w * tileSize) < (maxW * 0.99); w += h) {
    		comboBoxFieldSize.getItems().add(w + " x " + h);
    		if (checkBoxOnlySquares.isSelected())
    			break;
    	}
  	comboBoxFieldSize.getSelectionModel().select(1);
	}
  
}
