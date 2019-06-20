
public class GasStation {

	public int canCompleteCircuit(int[] gas, int[] cost) {
		int currGas = 0;
		int total = 0;
		int index = 0;
		for(int i = 0; i < gas.length; i++){
			currGas += gas[i] - cost[i];
			if(currGas < 0){
				index = i + 1;
				total += currGas;
				currGas = 0;
			}
		}
		if(total + currGas >= 0)
			return index;
		return -1;
	}
	
	public static void main(String[] args) {
		GasStation gas = new GasStation();
		System.out.println(gas.canCompleteCircuit(new int[]{5,6}, new int[]{10,1}));
	}

}
