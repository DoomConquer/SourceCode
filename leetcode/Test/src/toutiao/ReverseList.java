package toutiao;

public class ReverseList {

    public ListNode reverseList(ListNode head) {
    	ListNode res = new ListNode(0);
    	reverse(head, res);
    	return res.next;
    }
    private void reverse(ListNode head, ListNode res){
    	if(head == null) return;
    	ListNode next = head.next;
    	head.next = res.next;
    	res.next = head;
    	reverse(next, res);
    }
    
    public static void main(String[] args) {
    	ReverseList reverseList = new ReverseList();
    	ListNode head = new ListNode(0);
		ListNode l1 = new ListNode(1);
		ListNode l2 = new ListNode(3);
		ListNode l3 = new ListNode(4);
		head.next = l1;
		l1.next = l2;
		l2.next = l3;
		ListNode res = reverseList.reverseList(head);
		while(res != null){
			System.out.print(res.val + "  ");
			res = res.next;
		}
	}
    
}

class ListNode {
  int val;
  ListNode next;
  ListNode(int x) { val = x; }
}