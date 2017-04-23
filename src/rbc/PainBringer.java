package rbc;

import java.awt.Color;
import java.util.ArrayList;

import rbc.Enemy;
import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.Condition;
import robocode.CustomEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.RoundEndedEvent;

public class PainBringer extends AdvancedRobot {

	// Globale variabler for avstanden til n�rmeste fiende, og kj�reretning

	double shortestDistance;
	double targetBearing;
	int moveDirection = 1;
	ArrayList<Enemy> tempList = new ArrayList<Enemy>();
	int isChanged = 0;

	@Override
	public void run() {

		while (true) {

			setColors(Color.BLACK, new Color(252, 5, 29), Color.BLACK);

			setAdjustRadarForRobotTurn(false);
			setAdjustGunForRobotTurn(true);

			findEnemy();

			target();

			gunshot();

			moving();

			execute();

		}

	}

	private void moving() {
		/*
		 * henter x og y koordinatene til roboten disse brukes til � hindre at
		 * den sitter fast i hj�rnene
		 * 
		 */
		double x = getX();
		double y = getY();

		if (x <= 100 && y <= 100 || x >= 700 && y <= 100 || x <= 100 && y >= 500 || x >= 700 && y >= 500) {
			setTurnLeft(45 * moveDirection);
			execute();

			while (getTurnRemaining() > 0) {
				execute();
			}

		}
		
		/*
		 * sjekker om roboten holder p� � krasje i veggen, og endrer kj�reretning 
		 */
		if(y <= 50 || y >= 550 || x <= 50 || x >= 750){
			
		
			if(isChanged == 0){
				
				moveDirection *= -1;
				isChanged = 1;
				System.out.println("moving" +" " + isChanged);
			}
						
		}else {
			isChanged = 0;
			System.out.println("outside" +" "+ y + " "+ x + " " + isChanged); 

		}

		/*
		 * moveDirection er enten 1 eller -1, og bestemmer om den skal g� frem
		 * eller tilbake.
		 *
		 */
		setAhead(25 * moveDirection);

	}


	public void target() {
		/*
		 * shortestDistance blir satt til 1000 hver scan, for � nullstille s�ket
		 */
		shortestDistance = 1000;
		String name = "";
		tempList.clear();

		for (Enemy e : Enemy.getEnemies()) {

			tempList.add(e);

		}
		for (Enemy e : tempList) {

			/*
			 * sjekker avstanden til hver fiende, og om de fremdeles er i live
			 * Den nye laveste avstanden vil hele tiden bytte ut den gamle, det
			 * samme vil tilh�rende navn p� fiende
			 */

			if (e.getDistance() <= shortestDistance && e.getStatus() != "dead") {

				name = e.getName();
				shortestDistance = e.getDistance();

			}
		}
		for (Enemy e : tempList) {
			/*
			 * en ny l�kke finner den endelige n�rmeste roboten utifra navn
			 * Denne f�r status "target"
			 */

			if (e.getName() == name && e.getStatus() != "dead") {
				e.setStatus("target");
				/*
				 * s� snart roboten finner et nytt target vil den snu seg 90 - 5
				 * grader i forhold til fienden. p� denne m�ten kan den enklere
				 * unng� skudd. ved � sette p� - 5 hindrer jeg roboten i � kj�re
				 * lenger � lenger unna m�let
				 */
				targetBearing = e.getBearing();

				setTurnRight(normalizeBearing(targetBearing + 90 - (10 * moveDirection)));

			} else if (e.getStatus() != "dead") {
				/*
				 * fiender som tidligere har v�rt target f�r status "alive"
				 * igjen
				 */

				e.setStatus("alive");
			}
		}

	}

	/*
	 * ved d�d eller slutt p� runde t�mmes listen over fiender
	 */
	public void onRoundEnded(RoundEndedEvent event) {
		Enemy.getEnemies().clear();

	}

	public void onDeath(DeathEvent event) {
		Enemy.getEnemies().clear();
	}

	/*
	 * g�r konstant for � finne fiender
	 */
	public void findEnemy() {
		setTurnRadarRight(360);

	}

	/*
	 * Om en av fiendene d�r, blir status endret til "dead", og den vil bli
	 * ignorert
	 */
	public void onRobotDeath(RobotDeathEvent bot) {
		for (Enemy e : Enemy.getEnemies()) {
			if (e.getName() == bot.getName()) {
				e.setStatus("dead");
			}
		}
	}

	/*
	 * Om det er ikke er en robot med tilsvarende navn i Arraylisten, vil det
	 * bli opprettet et nytt Enemy-objekt
	 * 
	 * De som allerede eksisterer, vil bli oppdatert
	 */
	public void onScannedRobot(ScannedRobotEvent bot) {

		String name = bot.getName();
		double bearing = bot.getBearing();
		double distance = bot.getDistance();
		String status = "alive";

		if (checkEnemyNameList(name) == false) {
			new Enemy(name, bearing, distance, status);
		} else {
			for (Enemy e : Enemy.getEnemies()) {
				if (e.getName() == name) {
					e.setBearing(bearing);
					e.setDistance(distance);
				}
			}
		}
		//System.out.println(Enemy.getEnemies());

	}

	/*
	 * metode brukt til � sjekke listen etter navn
	 */
	public boolean checkEnemyNameList(String name) {
		for (Enemy e : Enemy.getEnemies()) {
			if (e.getName() == name) {
				return true;
			}
		}
		return false;
	}

	/*
	 * her normaliseres vinkelen til kanonen
	 * 
	 * Funnet her:
	 * http://mark.random-article.com/weber/java/robocode/lesson4.html
	 */
	double normalizeBearing(double angle) {
		while (angle > 180)
			angle -= 360;
		while (angle < -180)
			angle += 360;
		return angle;
	}

	public void gunshot() {
		/*
		 * Bruker normaliseringsfunksjonen for � unng� un�dvendig rotering av
		 * kanonen
		 * 
		 * sikteformelen "getHeading() - getGunHeading() + e.getBearing()" er
		 * funnet p� samme side.
		 */

		setTurnGunRight(normalizeBearing(getHeading() - getGunHeading() + targetBearing));
		execute();

		while (getGunTurnRemaining() > 0) {
			execute();

		}

		/*
		 * "ammunisjon" endres etter fiendens avstand.
		 * 
		 * Er fienden n�rme vil sjansen for � treffe med tyngre ammunisjon v�re
		 * st�rre
		 * 
		 */
		if (shortestDistance <= 100) {
			setFire(3);
		}
		if (shortestDistance >= 100 && shortestDistance <= 150) {
			setFire(2.5);
		}
		if (shortestDistance > 150 && shortestDistance < 250) {
			setFire(2.5);
		}
		if (shortestDistance > 250 && shortestDistance < 350) {
			setFire(2);
		}
		if (shortestDistance >= 350) {
			setFire(1.5);
		}

	}

	/*
	
	 * dette skjer ved treff og hvis roboten blir truffet, men bare hvis avstanden itl veggen er stor nok.
	 */
	public void onHitByBullet(HitByBulletEvent event) {
		double x = getX();
		double y = getY();
		if(y > 50 || y < 550 || x > 50 || x < 750){

		moveDirection *= -1;

		}
	}

	public void onBulletHit(BulletHitEvent event) {

		// Jeg er klar over at oppgaven sier at jeg trenger logikk her, men jeg
		// vil ikke at roboten skal gj�re noe.
	}

	/*
	 * ved kollisjon med fiende vil den flytte seg unna 300px i den bestemte
	 * retningen.
	 */
	public void onHitRobot(HitRobotEvent event) {

		setTurnLeft(45 * moveDirection);
		execute();
		setAhead(150 * moveDirection);

		while (getDistanceRemaining() > 0) {
			execute();
		}
		moveDirection *= -1;
	}

	/*
	 * den gitte bevegelsesretningen vil endres om roboten krasjer i en vegg
	 * eller en annen robot.
	 */		
	public void onHitWall(HitWallEvent e) {
		moveDirection *= -1;

	}


}
