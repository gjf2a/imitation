package edu.hendrix.imitation.vision;

public enum Dir {
	UP {
		@Override
		public Pos move(Pos p) {
			return new Pos(p.getX(), p.getY() - STEP_SIZE);
		}
	}, DOWN {
		@Override
		public Pos move(Pos p) {
			return new Pos(p.getX(), p.getY() + STEP_SIZE);
		}
	}, LEFT {
		@Override
		public Pos move(Pos p) {
			return new Pos(p.getX() - STEP_SIZE, p.getY());
		}
	}, RIGHT {
		@Override
		public Pos move(Pos p) {
			return new Pos(p.getX() + STEP_SIZE, p.getY());
		}
	};
	
	abstract public Pos move(Pos p);
	public static final int STEP_SIZE = 10;
}
