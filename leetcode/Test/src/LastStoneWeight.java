import java.util.Collections;
import java.util.PriorityQueue;

public class LastStoneWeight {

    public int lastStoneWeight(int[] stones) {
        if(stones.length == 0) return 0;
        PriorityQueue<Integer> heap = new PriorityQueue<>(Collections.reverseOrder());
        for(int stone : stones) heap.offer(stone);
        while(heap.size() > 1){
        	heap.offer(heap.poll() - heap.poll());
        }
        return heap.peek();
    }
    
	public static void main(String[] args) {
		LastStoneWeight lastStoneWeight = new LastStoneWeight();
		System.out.println(lastStoneWeight.lastStoneWeight(new int[]{2,7,4,1,8,1}));
		System.out.println(lastStoneWeight.lastStoneWeight(new int[]{2,7,4,1,8}));
		System.out.println(lastStoneWeight.lastStoneWeight(new int[]{8}));
		System.out.println(lastStoneWeight.lastStoneWeight(new int[]{8,100}));
	}

}
