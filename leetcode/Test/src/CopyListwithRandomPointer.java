import java.util.HashMap;
import java.util.Map;

public class CopyListwithRandomPointer {

	public RandomListNode copyRandomList(RandomListNode head) {
		RandomListNode newHead = new RandomListNode(0);
		RandomListNode p = head, q = newHead;
		Map<RandomListNode, RandomListNode> map = new HashMap<>();
		while(p != null){
			RandomListNode node = new RandomListNode(p.label);
			map.put(p, node);
			p = p.next;
			q.next = node;
			q = q.next;
		}
		for(Map.Entry<RandomListNode, RandomListNode> entry : map.entrySet()){
			entry.getValue().random = map.get(entry.getKey().random);
		}
		return newHead.next;
	}
	
	public static void main(String[] args) {
		CopyListwithRandomPointer copy = new CopyListwithRandomPointer();
		RandomListNode head = new RandomListNode(0);
		RandomListNode node1 = new RandomListNode(1);
		RandomListNode node2 = new RandomListNode(2);
		head.next = node1;
		head.random = null;
		node1.next = node2;
		node1.random = head;
		node2.random = node1;
		copy.copyRandomList(head);
	}

}

class RandomListNode {
	int label;
	RandomListNode next, random;
	RandomListNode(int x) { this.label = x; }
}