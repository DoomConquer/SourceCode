package toutiao;

public class IntersectionofTwoLinkedLists {

    public ListNode getIntersectionNode(ListNode headA, ListNode headB) {
        int lenA = 0, lenB = 0;
        ListNode nodeA = headA, nodeB = headB;
        while(nodeA != null){ nodeA = nodeA.next; lenA++; }
        while(nodeB != null){ nodeB = nodeB.next; lenB++; }
        if(lenA < lenB){
        	ListNode temp = headB;
        	headB = headA;
        	headA = temp;
        }
        int diff = Math.abs(lenA - lenB);
        nodeA = headA; nodeB = headB;
        while(diff-- > 0) nodeA = nodeA.next;
        while(nodeA != null && nodeA != nodeB){
        	nodeA = nodeA.next; nodeB = nodeB.next;
        }
        return nodeA;
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
