import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class MostProfitAssigningWork {

	class Pair{
		int difficulty;
		int profit;
		public Pair(int difficulty, int profit){
			this.difficulty = difficulty;
			this.profit = profit;
		}
	}
	public int maxProfitAssignment(int[] difficulty, int[] profit, int[] worker) {
		List<Pair> list = new ArrayList<>();
		int len = difficulty.length;
		for(int i = 0; i < len; i++) list.add(new Pair(difficulty[i], profit[i]));
		Collections.sort(list, (o1, o2) ->{ return o1.difficulty - o2.difficulty; });
		Arrays.sort(worker);
		int maxProfit = 0;
		int currProfit = 0;
		int j = 0;
		for(int i = 0; i < worker.length; i++){
			for(; j < list.size() && worker[i] >= list.get(j).difficulty; j++)
				currProfit = Math.max(currProfit, list.get(j).profit);
			maxProfit += currProfit;
		}
		return maxProfit;
	}
	
	public static void main(String[] args) {
		MostProfitAssigningWork profits = new MostProfitAssigningWork();
		System.out.println(profits.maxProfitAssignment(new int[]{2,4,6,8,10}, new int[]{10,20,30,40,50}, new int[]{4,5,6,7}));
	}

}
