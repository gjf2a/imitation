package edu.hendrix.imitation.ev3;

import lejos.hardware.Button;
import lejos.hardware.Key;

public enum Control {
	UP {
		@Override
		public Key getButton() {
			return Button.UP;
		}
	}, DOWN {
		@Override
		public Key getButton() {
			return Button.DOWN;
		}
	}, LEFT {
		@Override
		public Key getButton() {
			return Button.LEFT;
		}
	}, RIGHT {
		@Override
		public Key getButton() {
			return Button.RIGHT;
		}
	};
	
	public boolean isUp() {
		return getButton().isUp();
	}
	
	public boolean isDown() {
		return getButton().isDown();
	}
	
	abstract public Key getButton();
}
