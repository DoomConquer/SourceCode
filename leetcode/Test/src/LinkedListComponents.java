import java.util.HashSet;
import java.util.Set;

public class LinkedListComponents {

	public int numComponents(ListNode head, int[] G) {
		Set<Integer> set = new HashSet<>();
		for(int num : G) set.add(num);
		int sum = 0;
		boolean flag = true;
		while(head != null){
			if(set.contains(head.val)){
				if(flag){
					sum++;
					flag = false;
				}
			}else{
				flag = true;
			}
			head = head.next;
		}
		return sum;
	}
	
	public static void main(String[] args) {
		LinkedListComponents linked = new LinkedListComponents();
		ListNode head = new ListNode(1);
		ListNode l2 = new ListNode(2);
		ListNode l3 = new ListNode(3);
		head.next = l2;
		l2.next = l3;
		System.out.println(linked.numComponents(head, new int[]{1,3,2}));
	}

}
