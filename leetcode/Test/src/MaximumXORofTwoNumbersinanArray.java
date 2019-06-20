
public class MaximumXORofTwoNumbersinanArray {

	class Trie{
		int val;
		Trie left;
		Trie right;
		public Trie(int val){
			this.val = val;
		}
	}
	public int findMaximumXOR(int[] nums) {
		Trie root = buildTrie(nums);
		int max = 0;
		int start = 0;
		while(root != null){
			if(root.left != null && root.right != null) break;
			root = root.left != null ? root.left : root.right;
			start++;
		}
		for(int i = 0; i < nums.length; i++){
			Trie currNode = root;
			int curr = 0;
			for(int k = 31 - start; k >= 0; k--){
				int val = nums[i] & (1 << k);
				if(val == 0){
					if(currNode.right != null) currNode = currNode.right;
					else currNode = currNode.left;
				}else{
					if(currNode.left != null) currNode = currNode.left;
					else currNode = currNode.right;
				}
				curr += (val ^ (currNode.val << k));
			}
			max = Math.max(max, curr);
		}
		return max;
	}
	private Trie buildTrie(int[] nums){
		Trie root = new Trie(0);
		Trie currNode = root;
		for(int num : nums){
			for(int k = 31; k >= 0; k--){
				int val = num & (1 << k);
				if(val == 0){
					if(currNode.left == null)
						currNode.left = new Trie(0);
					currNode = currNode.left;
				}else{
					if(currNode.right == null)
						currNode.right = new Trie(1);
					currNode = currNode.right;
				}
			}
			currNode = root;
		}
		return root;
	}
	
	public static void main(String[] args) {
		MaximumXORofTwoNumbersinanArray max = new MaximumXORofTwoNumbersinanArray();
		System.out.println(max.findMaximumXOR(new int[]{3, 10, 5, 25, 2, 8}));
		System.out.println(max.findMaximumXOR(new int[]{8, 8, 8}));
		System.out.println(max.findMaximumXOR(new int[]{1}));
	}

}
