public class GuessNumberHigherorLower extends GuessGame{

	public int guessNumber(int n) {
        int left = 1, right = n;
        while(left < right){
            int mid = left + (right - left) / 2;
            int res = guess(mid);
            if(res == 0) return mid;
            else if(res == 1) left = mid + 1;
            else right = mid;
        }
        return left;
    }
    
	public static void main(String[] args) {
		GuessNumberHigherorLower GuessNumberHigherorLower = new GuessNumberHigherorLower();
		System.out.println(GuessNumberHigherorLower.guessNumber(10));
	}

}
class GuessGame{ // just for test
	protected int guess(int num){
		if(num == 6) return 0;
		else if(num > 6) return -1;
		else return 1;
	}
}
