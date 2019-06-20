public class RobotBoundedInCircle {

    public boolean isRobotBounded(String instructions) {
    	if(instructions == null || instructions.length() == 0) return true;
        int x = 0, y = 0;
        int direction = 0;
        int[] directionX = new int[]{0, -1, 0, 1};
        int[] directionY = new int[]{1, 0, -1, 0};
        for(int i = 0; i < 4; i++){
        	for(int j = 0; j < instructions.length(); j++){
        		switch(instructions.charAt(j)){
        		case 'G':
        			x += directionX[direction];
        			y += directionY[direction];
        			break;
        		case 'L':
        			direction = (direction + 1) % 4;
        			break;
        		case 'R':
        			direction = (direction + 3) % 4;
        			break;
        		}
        	}
        	if(x == 0 && y == 0) return true;
        }
        return x == 0 && y == 0 ? true : false;
    }
    
	public static void main(String[] args) {
		RobotBoundedInCircle robotBoundedInCircle = new RobotBoundedInCircle();
		System.out.println(robotBoundedInCircle.isRobotBounded("GGLLGG"));
		System.out.println(robotBoundedInCircle.isRobotBounded("GG"));
		System.out.println(robotBoundedInCircle.isRobotBounded("GL"));
		System.out.println(robotBoundedInCircle.isRobotBounded("LLGRL"));
	}

}
