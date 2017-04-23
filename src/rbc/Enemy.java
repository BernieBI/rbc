package rbc;

import java.util.ArrayList;

public class Enemy {

	String name;
	double bearing;
	double distance;
	String status;
	public static ArrayList<Enemy> enemies = new ArrayList<>();

	public Enemy(String name, double bearing, double distance, String status) {
		this.name = name;
		this.bearing = bearing;
		this.distance = distance;
		this.status = status;
		enemies.add(this);
	}

	public static ArrayList<Enemy> getEnemies() {
		return enemies;
	}

	public static void setEnemies(ArrayList<Enemy> enemies) {
		Enemy.enemies = enemies;
	}

	@Override
	public String toString() {
		return  name + " " + status;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public double getBearing() {
		return bearing;
	}

	public void setBearing(double bearing) {
		this.bearing = bearing;
	}



	public double getDistance() {
		return distance;
	}

	public void setDistance(double distance) {
		this.distance = distance;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}


}
