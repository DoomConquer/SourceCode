//  DP(Time limited exception)
public class CheapestFlightsWithinKStops {

	public int findCheapestPrice(int n, int[][] flights, int src, int dst, int K) {
		int[][][] cheapest = new int[n][n][K + 1];
		for(int i = 0; i < n; i++)
			for(int j = 0; j < n; j++)
				for(int k = 0; k <= K; k++)
					cheapest[i][j][k] = Integer.MAX_VALUE;
		for(int i = 0; i < flights.length; i++){
			cheapest[flights[i][0]][flights[i][1]][0] = flights[i][2];
		}
		for(int k = 1; k <= K; k++)
			for(int i = 0; i < n; i++)
				for(int j = 0; j < n; j++){
					cheapest[i][j][k] = cheapest[i][j][k - 1];
					for(int l = 0; l < n; l++)
						if(cheapest[i][l][k - 1] != Integer.MAX_VALUE && cheapest[l][j][0] != Integer.MAX_VALUE){
							if(cheapest[i][j][k] > cheapest[i][l][k - 1] + cheapest[l][j][0])
								cheapest[i][j][k] = cheapest[i][l][k - 1] + cheapest[l][j][0];
						}
				}
		return cheapest[src][dst][K] == Integer.MAX_VALUE ? -1 : cheapest[src][dst][K];
	}
	
	public static void main(String[] args) {
		CheapestFlightsWithinKStops cheapest = new CheapestFlightsWithinKStops();
		System.out.println(cheapest.findCheapestPrice(10, new int[][]{{3,4,4},{2,5,6},{4,7,10},{9,6,5},{7,4,4},{6,2,10},{6,8,6},{7,9,4},{1,5,4},{1,0,4},{9,7,3},{7,0,5},{6,5,8},{1,7,6},{4,0,9},{5,9,1},{8,7,3},{1,2,6},{4,1,5},{5,2,4},{1,9,1},{7,8,10},{0,4,2},{7,2,8}}, 6, 0, 7));
		System.out.println(cheapest.findCheapestPrice(5, new int[][]{{0,1,5},{1,2,5},{0,3,2},{3,1,2},{1,4,1},{4,2,1}}, 0, 2, 1));
		System.out.println(cheapest.findCheapestPrice(8, new int[][]{{3,4,7},{6,2,2},{0,2,7},{0,1,2},{1,7,8},{4,5,2},{0,3,2},{7,0,6},{3,2,7},{1,3,10},{1,5,1},{4,1,6},{4,7,5},{5,7,10}}, 4, 3, 7));
	}

}
