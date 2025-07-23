package entity;

import java.util.Objects;

import enums.TileState;

public class Tile {

	private int x;
	private int y;
	private TileState state;
	private int bombsAround;
	private boolean isBomb;

	public Tile(int x, int y) {
		this.x = x;
		this.y = y;
		state = TileState.UNREVEALED;
		bombsAround = 0;
		isBomb = false;
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public TileState getState() {
		return state;
	}

	public void setState(TileState state) {
		this.state = state;
	}

	public boolean isBomb() {
		return isBomb;
	}

	public void setBomb(boolean isBomb) {
		this.isBomb = isBomb;
	}

	public boolean haveBombsAround() {
		return bombsAround > 0;
	}

	public int getBombsAround() {
		return bombsAround;
	}

	public void incBombsAround() {
		bombsAround++;
	}

	public boolean isATileAroundOf(Tile tile) {
		return !this.equals(tile) && Math.abs(tile.getX() - getX()) < 2 && Math.abs(tile.getY() - getY()) < 2;
	}

	@Override
	public int hashCode() {
		return Objects.hash(x, y);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Tile other = (Tile) obj;
		return x == other.x && y == other.y;
	}
	
}
