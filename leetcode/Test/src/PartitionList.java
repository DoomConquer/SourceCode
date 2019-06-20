
public class PartitionList {

	public ListNode partition(ListNode head, int x) {
		ListNode part1 = null;
		ListNode part2 = null;
		ListNode p = null, q = null;
		while(head != null){
			if(head.val < x){
				if(p == null){
					part1 = head;
					p = part1;
				}else{
					p.next = head;
					p = p.next;
				}
			}else{
				if(q == null){
					part2 = head;
					q = part2;
				}else{
					q.next = head;
					q = q.next;
				}
			}
			head = head.next;
		}
		if(p != null){
			p.next = part2;
		}else{
			part1 = part2;
		}
		if(q != null) q.next = null;
		return part1;
	}
	
	public static void main(String[] args) {
		PartitionList partition = new PartitionList();
		ListNode head = new ListNode(1);
		ListNode node1 = new ListNode(3);
		ListNode node2 = new ListNode(2);
		ListNode node3 = new ListNode(4);
		head.next = node1;
		node1.next = node2;
		node2.next = node3;
		ListNode node = partition.partition(head, 3);
		while(node != null){
			System.out.print(node.val + "  ");
			node = node.next;
		}
	}

}
