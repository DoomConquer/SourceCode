public class DivisorGame {

    public boolean divisorGame(int N) {
        boolean[] dp = new boolean[N + 1];
        for(int i = 2; i <= N; i++){
        	for(int j = 1; j <= Math.sqrt(i); j++){
        		if(i % j == 0){
        			dp[i] = dp[i] | !dp[i - j];
        		}
        		if(dp[i]) break;
        	}
        }
        return dp[N];
    }
    
	public static void main(String[] args) {
		DivisorGame divisorGame = new DivisorGame();
		System.out.println(divisorGame.divisorGame(2));
		System.out.println(divisorGame.divisorGame(3));
		System.out.println(divisorGame.divisorGame(4));
		System.out.println(divisorGame.divisorGame(5));
		System.out.println(divisorGame.divisorGame(6));
	}

}
