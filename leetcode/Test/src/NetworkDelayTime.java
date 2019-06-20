public class NetworkDelayTime {

	public int networkDelayTime(int[][] times, int N, int K) {
		int[][] time = new int[N + 1][N + 1];
		int[] dist = new int[N + 1];
		boolean[] s = new boolean[N + 1];
		for(int i = 0; i <= N; i++)
			for(int j = 0; j <= N; j++)
				time[i][j] = -1;
		for(int i = 0; i < times.length; i++)
			time[times[i][0]][times[i][1]] = times[i][2];
		for(int i = 1; i <= N; i++)
			if(time[K][i] != -1)
				dist[i] = time[K][i];
			else
				dist[i] = Integer.MAX_VALUE;
		s[K] = true;
		for(int i = 1; i < N; i++){
			int min = 0;
			int minVal = Integer.MAX_VALUE;
			for(int j = 1; j <= N; j++){
				if(!s[j] && minVal > dist[j] && dist[j] != Integer.MAX_VALUE){
					min = j;
					minVal = dist[j];
				}
			}
			if(min == 0) break;
			s[min] = true;
			for(int j = 1; j <= N; j++){
				if(time[min][j] != -1 && dist[min] != Integer.MAX_VALUE && dist[j] > dist[min] + time[min][j]){
					dist[j] = dist[min] + time[min][j];
				}
			}
		}
		int delay = 0;
		for(int i = 1; i <= N; i++)
			if(i != K){
				if(dist[i] == Integer.MAX_VALUE) return -1;
				if(delay < dist[i]) delay = dist[i];
			}
		return delay;
	}
	
	public static void main(String[] args) {
		NetworkDelayTime delay = new NetworkDelayTime();
		System.out.println(delay.networkDelayTime(new int[][]{{2,1,1},{2,3,1},{3,4,1}}, 4, 2));
		System.out.println(delay.networkDelayTime(new int[][]{{3,5,78},{2,1,1},{1,3,0},{4,3,59},{5,3,85},{5,2,22},{2,4,23},{1,4,43},{4,5,75},{5,1,15},{1,5,91},{4,1,16},{3,2,98},{3,4,22},{5,4,31},{1,2,0},{2,5,4},{4,2,51},{3,1,36},{2,3,59}}, 5, 5));
	}

}
