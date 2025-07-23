package entity;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import enums.TileState;
import gameutil.GameUtils;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.image.Image;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import util.Misc;
import util.MyMath;
import util.SoundsFX;

public class Game {

	private static Set<Tile> verifiedTiles;
	private static List<Tile> tiles;
	private static Map<Tile, Integer> explosion;
	private static Stage stage;
	private static Canvas canvas;
	private static GraphicsContext gc;
	private static Image sprites;
	private static Image numbers;
	private static int tileSize;
	private static int fieldSize;
	private static int totalBombs;
	private static int screenSize;
	private static int placedFlags;
	private static boolean gameOver;
	private static boolean win;
	private static boolean close;

	public static void start() {
		stage = new Stage();
		sprites = new Image("file:appdata/images/sprites.png");
		numbers = new Image("file:appdata/images/numbers.png");
		screenSize = fieldSize * tileSize;
		canvas = new Canvas(screenSize, screenSize);
		gc = canvas.getGraphicsContext2D();
		gc.setImageSmoothing(false);
		VBox vBox = new VBox(canvas);
		Scene scene = new Scene(vBox);
		scene.setOnMouseClicked(e -> {
			if (gameOver) {
				reset();
				return;
			}
			clickedAt((int) e.getX() / tileSize, (int) e.getY() / tileSize, e.getButton() == MouseButton.PRIMARY ? 1 : 2);
		});
		stage.setScene(scene);
		stage.setOnCloseRequest(e -> close = true);
		stage.show();
		reset();
		mainLoop();
	}

	public static void setTileSize(int tileSize) {
		Game.tileSize = tileSize;
	}

	public static void setFieldSize(int fieldSize) {
		Game.fieldSize = fieldSize;
	}

	public static void setTotalBombs(int totalBombs) {
		Game.totalBombs = totalBombs;
	}

	private static void reset() {
		stage.setTitle("Campo Minado");
		verifiedTiles = new HashSet<>();
		tiles = new ArrayList<>();
		explosion = new HashMap<>();
		close = false;
		win = false;
		gameOver = false;
		placedFlags = 0;
		for (int y = 0; y < fieldSize; y++)
			for (int x = 0; x < fieldSize; x++)
				tiles.add(new Tile(x, y));
		int bombs = 0;
		do {
			int	n = (int)MyMath.getRandom(0, tiles.size() - 1);
			Tile tile = tiles.get(n);
			if ((int)MyMath.getRandom(0, 4) == 0 && !tile.isBomb()) {
				tile.setBomb(true);
				iterateAroundTiles(tile, t -> t.incBombsAround());
				bombs++;
			}
		}
		while (bombs < totalBombs);
	}
	

	private static void iterateAroundTiles(Tile tile, Consumer<Tile> consumer) {
		for (Tile t : tiles)
			if (t.isATileAroundOf(tile))
				consumer.accept(t);
	}
	
	private static void checkAroundTiles(Tile tile) {
		if (verifiedTiles.contains(tile) || (!gameOver && tile.getState() == TileState.REVEALED))
			return;

		verifiedTiles.add(tile);

		tile.setState(TileState.REVEALED);
		if (!tile.haveBombsAround() || gameOver)
			iterateAroundTiles(tile, t -> checkAroundTiles(t));
	}

	private static void clickedAt(int x, int y, int button) {
		Tile tile = null;
		for (Tile t : tiles)
			if (t.getX() == x && t.getY() == y) {
				tile = t;
				break;
			}
		if (tile == null || tile.getState() == TileState.REVEALED)
			return;

		if (button == 2) {
			if (tile.getState() == TileState.FLAGGED) {
				--placedFlags;
				tile.setState(TileState.QUESTION);
			}
			else if (tile.getState() == TileState.QUESTION)
				tile.setState(TileState.UNREVEALED);
			else if (placedFlags < totalBombs) {
				++placedFlags;
				tile.setState(TileState.FLAGGED);
			}
			else
				tile.setState(TileState.QUESTION);
			playWav("rclick");
			return;
		}
		
		verifiedTiles.clear();

		if (!gameOver && tile.isBomb()) {
			explode(tile);
			gameOver();
		}
		else
			playWav("click");

		checkAroundTiles(tile);
		
		if (!gameOver && getUnrevealedTiles() == totalBombs)
			gameWon();
	}
	
	private static void playWav(String soundName) {
		SoundsFX.playWav("appdata/sounds/" + soundName + ".wav");
	}

	private static void explode(Tile tile) {
		explosion.put(tile, 0);
		playWav("explosion");
	}

	private static void gameWon() {
		playWav("won");
		stage.setTitle("Campo Minado (VENCEU)");
		win = true;
		gameOver = true;
	}

	private static void gameOver() {
		playWav("gameOver");
		stage.setTitle("Campo Minado (PERDEU)");
		gameOver = true;
	}

	private static int getUnrevealedTiles() {
		return (int)tiles.stream()
				.filter(t -> t.getState() != TileState.REVEALED)
				.count();
	}

	private static void mainLoop() {
		GameUtils.createAnimationTimer(30, (load, fps) -> close, () -> {
			gc.clearRect(0, 0, screenSize, screenSize);
			for (Tile tile : tiles) {
				int x = tile.getX(), y = tile.getY();
				TileState state = tile.getState();
				if (!gameOver && (state == TileState.UNREVEALED || state == TileState.FLAGGED || state == TileState.QUESTION)) {
					gc.drawImage(sprites, 0, 0, 16, 16, x * tileSize, y * tileSize, tileSize, tileSize);
					if (state != TileState.UNREVEALED)
						gc.drawImage(sprites, state == TileState.FLAGGED ? 48 : 64, 0, 16, 16, x * tileSize, y * tileSize, tileSize, tileSize);
				}
				else {
					gc.drawImage(sprites, 16, 0, 16, 16, x * tileSize, y * tileSize, tileSize, tileSize);
					if (tile.isBomb()) {
						if (explosion.containsKey(tile)) {
							int v = explosion.get(tile);
							if (Misc.blink(50) && v < 5) {
								int next = v + 1;
								if (v < 0 && next >= 0)
									v = 5;
								else if (next >= 4)
									v = -4;
								else
									v = next;
								if (next == 3) {
									for (Tile t : tiles)
										if (t.isBomb() && !explosion.containsKey(t)) {
											explode(t);
											break;
										}
								}
								explosion.put(tile, v);
							}
							gc.drawImage(sprites, (int)Math.abs(v) * 16, 16, 16, 16, x * tileSize, y * tileSize, tileSize, tileSize);
						}
						else
							gc.drawImage(sprites, win ? 48 : 32, 0, 16, 16, x * tileSize, y * tileSize, tileSize, tileSize);
					}
				}
				if (state == TileState.REVEALED && !tile.isBomb() && tile.haveBombsAround())
					gc.drawImage(numbers, tile.getBombsAround() * 64, 0, 64, 64, x * tileSize, y * tileSize, tileSize, tileSize);
			}
		}).start();
	}

	public static void close() {
	}

}
