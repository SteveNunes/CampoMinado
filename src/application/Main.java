package application;

import java.util.HashSet;
import java.util.Set;

import gameutil.GameUtils;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.MyMath;

public class Main extends Application {

	private static boolean PREVIEW_BOMBS = false;
	private static final int TOTAL_BOMBS = 10;
	private static final int TILE_SIZE = 32;
	private static final int FIELD_W = 10, FIELD_H = 10;
	private static final int W = FIELD_W * TILE_SIZE, H = FIELD_H * TILE_SIZE;

	private Set<String> verifiedTiles = new HashSet<>();
	private Stage stage;
	private Canvas canvas;
	private GraphicsContext gc;
	private Tile[][] field;
	private Image sprites;
	private Image numbers;
	private boolean gameOver;
	private boolean win;
	private boolean close;

	private class Tile {

		int status; // -3 Question, 0 -2 Flag, -1 Unrevealed, 0 Revealed, 1-7 Number Mark
		boolean isBomb;

		public Tile() {
			status = -1;
			isBomb = false;
		}

	}

	@Override
	public void start(Stage stage) throws Exception {
		this.stage = stage;
		field = new Tile[FIELD_H][FIELD_W];
		sprites = new Image("file:sprites.png");
		numbers = new Image("file:numbers.png");
		canvas = new Canvas(W, H);
		gc = canvas.getGraphicsContext2D();
		gc.setImageSmoothing(false);
		VBox vBox = new VBox(canvas);
		Scene scene = new Scene(vBox);
		scene.setOnMouseClicked(e -> {
			if (gameOver) {
				reset();
				return;
			}
			clickedAt((int) e.getX() / TILE_SIZE, (int) e.getY() / TILE_SIZE, e.getButton() == MouseButton.PRIMARY ? 1 : 2);
		});
		stage.setScene(scene);
		stage.setOnCloseRequest(e -> close = true);
		stage.show();
		reset();
		mainLoop();
	}

	private void reset() {
		stage.setTitle("Campo Minado");
		close = false;
		win = false;
		gameOver = false;
		for (int y = 0; y < FIELD_H; y++)
			for (int x = 0; x < FIELD_W; x++)
				field[y][x] = new Tile();
		int bombs = 0;
		do {
			int x = (int)MyMath.getRandom(0, FIELD_W - 1),
					y = (int)MyMath.getRandom(0, FIELD_H - 1);
			Tile tile = field[y][x];
			if ((int)MyMath.getRandom(0, 4) == 0 && !tile.isBomb) {
				tile.isBomb = true;
				bombs++;
			}
		}
		while (bombs < TOTAL_BOMBS);
	}
	
	private int totalOfBombsAround(int x, int y) {
		int bombs = 0;
		for (int y2 = -1; y2 < 2; y2++)
			for (int x2 = -1; x2 < 2; x2++)
				if (x + x2 >= 0 && x + x2 < FIELD_W && y + y2 >= 0 && y + y2 < FIELD_H && (x2 != 0 || y2 != 0) && field[y + y2][x + x2].isBomb)
					bombs++;
		return bombs;
	}

	private void checkAroundTile(int x, int y) {
		if (x < 0 || x >= FIELD_W || y < 0 || y >= FIELD_H)
			return;

		String key = x + "@" + y;
		if (verifiedTiles.contains(key))
			return;

		verifiedTiles.add(key);
		Tile tile = field[y][x];
		if (!gameOver && (tile.isBomb || tile.status >= 0))
			return;

		int bombsAround = totalOfBombsAround(x, y);
		if (tile.status == -1)
			tile.status = bombsAround;

		if (bombsAround == 0 || gameOver) {
			for (int y2 = -1; y2 <= 1; y2++) {
				for (int x2 = -1; x2 <= 1; x2++) {
					if (x2 != 0 || y2 != 0)
						checkAroundTile(x + x2, y + y2);
				}
			}
		}
	}

	private void clickedAt(int x, int y, int button) {
		if (x < 0 || x >= FIELD_W || y < 0 || y >= FIELD_H)
			return;

		Tile tile = field[y][x];
		if (tile.status == 0)
			return;

		if (button == 2) {
			if (tile.status < 0 && --tile.status == -4)
				tile.status = -1;
			return;
		}
		
		verifiedTiles.clear();

		if (tile.isBomb) {
			stage.setTitle("Campo Minado (PERDEU)");
			gameOver = true;
		}

		checkAroundTile(x, y);

		if ((getUnrevealedTiles() - getMarkedAsNumberTiles()) == TOTAL_BOMBS) {
			stage.setTitle("Campo Minado (VENCEU)");
			win = true;
			gameOver = true;
		}
	}

	public static void main(String[] args) {
		launch(args);
	}

	private int getUnrevealedTiles() {
		int total = 0;
		for (int y = 0; y < FIELD_H; y++)
			for (int x = 0; x < FIELD_W; x++) {
				Tile tile = field[y][x];
				if (tile.isBomb || tile.status != 0)
					total++;
			}
		return total;
	}

	private int getMarkedAsNumberTiles() {
		int total = 0;
		for (int y = 0; y < FIELD_H; y++)
			for (int x = 0; x < FIELD_W; x++) {
				Tile tile = field[y][x];
				if (tile.status > 0)
					total++;
			}
		return total;
	}

	private void mainLoop() {
		GameUtils.createAnimationTimer(30, (load, fps) -> close, () -> {
			gc.clearRect(0, 0, W, H);
			for (int y = 0; y < FIELD_H; y++)
				for (int x = 0; x < FIELD_W; x++) {
					Tile tile = field[y][x];
					if (!gameOver && tile.status < 0) {
						gc.drawImage(sprites, 0, 0, 16, 16, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
						if (tile.status < -1)
							gc.drawImage(sprites, 16 + -tile.status * 16, 0, 16, 16, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
					}
					else {
						gc.drawImage(sprites, 16, 0, 16, 16, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
						if (field[y][x].isBomb)
							gc.drawImage(sprites, win ? 48 : 32, 0, 16, 16, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
					}
					if (PREVIEW_BOMBS && field[y][x].isBomb) {
						gc.setGlobalAlpha(0.3);
						gc.drawImage(sprites, 32, 0, 16, 16, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
						gc.setGlobalAlpha(1);
					}
					if (tile.status > 0 && !tile.isBomb)
						gc.drawImage(numbers, tile.status * 64, 0, 64, 64, x * TILE_SIZE, y * TILE_SIZE, TILE_SIZE, TILE_SIZE);
				}
		}).start();
	}

}
