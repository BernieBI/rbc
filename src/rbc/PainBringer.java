package rbc;

import java.awt.Color;
import java.util.ArrayList;

import rbc.Enemy;
import robocode.AdvancedRobot;
import robocode.BulletHitEvent;
import robocode.DeathEvent;
import robocode.HitByBulletEvent;
import robocode.HitRobotEvent;
import robocode.HitWallEvent;
import robocode.RobotDeathEvent;
import robocode.ScannedRobotEvent;
import robocode.RoundEndedEvent;

public class PainBringer extends AdvancedRobot {

	// Globale variabler for avstanden til nærmeste fiende, og kjøreretning

	double shortestDistance;
	double targetBearing;
	private byte moveDirection = 1;
	ArrayList<Enemy> tempList = new ArrayList();

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
		 * henter x og y koordinatene til roboten disse brukes til å hindre at
		 * den sitter fast i hjørnene
		 * 
		 */
		int movedistance;
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
		 * uavhengig om den er i et hjørne eller ikke, skal roboten bevege seg
		 * moveDirection er enten 1 eller -1, og bestemmer om den skal gå frem
		 * eller tilbake.
		 * 
		 * roboten endrer hvor langt den kjører etter hvor langt unna fienden
		 * er, for å bedre unngå skudd.
		 */

		if (shortestDistance > 200) {
			movedistance = 50;
		} else if (shortestDistance > 500) {
			movedistance = 25;
		} else {
			movedistance = 75;

		}
		setAhead(movedistance * moveDirection);
		
	}

	public void target() {
		/*
		 * shortestDistance blir satt til 1000 hver scan, for å nullstille søket
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
			 * samme vil tilhørende navn på fiende
			 */

			if (e.getDistance() <= shortestDistance && e.getStatus() != "dead") {

				name = e.getName();
				shortestDistance = e.getDistance();
				System.out.println(shortestDistance);

			}
		}
		for (Enemy e : tempList) {
			/*
			 * en ny løkke finner den endelige nærmeste roboten utifra navn
			 * Denne får status "target"
			 */

			if (e.getName() == name && e.getStatus() != "dead") {
				e.setStatus("target");
				/*
				 * så snart roboten finner et nytt target vil den snu seg 90
				 * grader i forhold til fienden. på denne måten kan den enklere
				 * unngå skudd.
				 */
				targetBearing = e.getBearing();

				setTurnRight(normalizeBearing(targetBearing + 90));

			} else if (e.getStatus() != "dead") {
				/*
				 * fiender som tidligere har vært target får status "alive"
				 * igjen
				 */

				e.setStatus("alive");
			}
		}

	}

	/*
	 * ved død eller slutt på runde tømmes listen over fiender
	 */
	public void onRoundEnded(RoundEndedEvent event) {
		Enemy.getEnemies().clear();

	}

	public void onDeath(DeathEvent event) {
		Enemy.getEnemies().clear();
	}

	/*
	 * går konstant for å finne fiender
	 */
	public void findEnemy() {
		setTurnRadarRight(360);

	}

	/*
	 * Om en av fiendene dør, blir status endret til "dead", og den vil bli
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
		System.out.println(Enemy.getEnemies());

	}

	/*
	 * metode brukt til å sjekke listen etter navn
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
		 * Bruker normaliseringsfunksjonen for å unngå unødvendig rotering av
		 * kanonen
		 * 
		 * sikteformelen "getHeading() - getGunHeading() + e.getBearing()" er
		 * funnet på samme side.
		 */

		setTurnGunRight(normalizeBearing(getHeading() - getGunHeading() + targetBearing));
		execute();
		
		while(getGunTurnRemaining() > 0){
			execute();

		}
		
	
		/*
		 * "ammunisjon" endres etter fiendens avstand.
		 * 
		 * Er fienden nærme vil sjansen for å treffe med tyngre ammunisjon være
		 * større
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
	 * For å være vanskeligere å treffe, vil roboten flytte seg en tilfeldig
	 * lengde mellom 75px - 150px i den forhåndsbestemte retningen.
	 * 
	 * dette skjer ved treff og hvis roboten blir truffet
	 */
	public void onHitByBullet(HitByBulletEvent event) {

		moveDirection *= -1;

	}

	public void onBulletHit(BulletHitEvent event) {

		//trenger noe her !!!
	}

	/*
	 * ved kollisjon med fiende vil den flytte seg unna 300px i den bestemte
	 * retningen.
	 */
	public void onHitRobot(HitRobotEvent event) {

		setAhead(300 * moveDirection);
		
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