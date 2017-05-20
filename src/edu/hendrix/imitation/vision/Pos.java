package edu.hendrix.imitation.vision;

public class Pos {
	private int x, y;
	
	public Pos(int x, int y) {
		this.x = x;
		this.y = y;
	}
	
	public boolean equals(Object other) {
		if (other instanceof Pos) {
			Pos that = (Pos)other;
			return this.x == that.x && this.y == that.y;
		} else {
			return false;
		}
	}
	
	public Pos add(Pos other) {
		return new Pos(getX() + other.getX(), 
				getY() + other.getY());
	}
	
	public int getX() {return x;}
	public int getY() {return y;}
}
