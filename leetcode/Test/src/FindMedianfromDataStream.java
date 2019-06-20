import java.util.Collections;
import java.util.PriorityQueue;

public class FindMedianfromDataStream {
	public static void main(String[] args) {
		MedianFinder median = new MedianFinder();
		median.addNum(1);
		median.addNum(2);
//		median.addNum(3);
		System.out.println(median.findMedian());
	}

}

class MedianFinder {
	private PriorityQueue<Integer> left = null;
	private PriorityQueue<Integer> right = null;
	private int size;
    public MedianFinder() {
    	left = new PriorityQueue<Integer>(Collections.reverseOrder());
    	right = new PriorityQueue<Integer>();
    	size = 0;
    }
    
    public void addNum(int num) {
        if(size % 2 == 0){
        	left.add(num);
        	right.add(left.poll());
        }else{
        	right.add(num);
        	left.add(right.poll());
        }
        size++;
    }
    
    public double findMedian() {
        return size % 2 == 0 ? (double)(left.peek() + right.peek()) / 2 : right.peek();
    }
}