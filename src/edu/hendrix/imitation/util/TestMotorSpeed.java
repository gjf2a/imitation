package edu.hendrix.imitation.util;

import lejos.hardware.Button;
import lejos.hardware.lcd.LCD;
import lejos.hardware.motor.Motor;

public class TestMotorSpeed {
	public static void main(String[] args) {
		int speed = 0;
		while (Button.ESCAPE.isUp()) {
			if (Button.UP.isDown() || Button.RIGHT.isDown()) {
				speed += 10;
			} else if (Button.DOWN.isDown() || Button.LEFT.isDown()) {
				speed -= 10;
			}
			Motor.A.setSpeed(speed);
			Motor.D.setSpeed(speed);
			LCD.drawString(String.format("%d    ", speed), 3, 3);
			Util.motorAt(Motor.A, speed);
			Util.motorAt(Motor.D, speed);
		}
	}
}
