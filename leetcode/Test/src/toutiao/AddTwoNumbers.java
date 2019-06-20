package toutiao;

public class AddTwoNumbers {

    public ListNode addTwoNumbers(ListNode l1, ListNode l2) {
        int carry = 0;
        ListNode head = new ListNode(0);
        ListNode currNode = head;
        while(l1 != null || l2 != null){
        	int sum = (l1 == null ? 0 : l1.val) + (l2 == null ? 0 : l2.val) + carry;
        	int val = sum % 10;
        	carry = sum / 10;
        	ListNode node = new ListNode(val);
        	currNode.next = node;
        	currNode = currNode.next;
        	if(l1 != null) l1 = l1.next;
        	if(l2 != null) l2 = l2.next;
        }
        if(carry != 0){
        	ListNode node = new ListNode(carry);
        	currNode.next = node;
        }
        return head.next;
    }
    
	public static void main(String[] args) {
		AddTwoNumbers add = new AddTwoNumbers();
		ListNode l1 = new ListNode(7);
		ListNode l2 = new ListNode(9);
		ListNode l3 = new ListNode(4);
		l2.next = l3;
		ListNode res = add.addTwoNumbers(l1, l2);
		while(res != null){
			System.out.print(res.val + "  ");
			res = res.next;
		}
	}

}
