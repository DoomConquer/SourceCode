
public class IntersectionofTwoLinkedLists {

	public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
		int lenA = 0;
		int lenB = 0;
		ListNode p = headA, q = headB;
		while(p != null){ lenA++; p = p.next; }
		while(q != null){ lenB++; q = q.next; }
		if(lenA < lenB){
			ListNode temp = headA;
			headA = headB;
			headB = temp;
		}
		int minus = Math.abs(lenA - lenB);
		while(minus-- > 0){ headA = headA.next; }
		while(headA != null){
			if(headA.val == headB.val) return headA;
			headA = headA.next;
			headB = headB.next;
		}
		return null;
	}
	
	// 一种更巧的方法，不用知道每个链表的长度，遍历两遍，一遍完后交换链表，两遍总共长度都为lenA + lenB
	public ListNode getIntersectionNode1(ListNode headA, ListNode headB) {
	    if(headA == null || headB == null) return null;
	    ListNode a = headA;
	    ListNode b = headB;
	    while(a != b){
	        a = a == null? headB : a.next;
	        b = b == null? headA : b.next;    
	    }
	    return a;
	}
	
	public static void main(String[] args) {
		IntersectionofTwoLinkedLists intersection = new IntersectionofTwoLinkedLists();
		ListNode head1 = new ListNode(4);
		ListNode l1 = new ListNode(9);
		ListNode l2 = new ListNode(4);
		head1.next = l1;
		l1.next = l2;
		ListNode head2 = new ListNode(5);
		head2.next = head1;
		ListNode res = intersection.getIntersectionNode(head1, head2);
		while(res != null){
			System.out.print(res.val + "  ");
			res = res.next;
		}
	}

}
