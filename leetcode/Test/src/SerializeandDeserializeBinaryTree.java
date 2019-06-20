
public class SerializeandDeserializeBinaryTree {

	public static void main(String[] args) {
		Codec code = new Codec();
		TreeNode root = new TreeNode(1);
		TreeNode node1 = new TreeNode(2);
		TreeNode node2 = new TreeNode(3);
		TreeNode node3 = new TreeNode(4);
		TreeNode node4 = new TreeNode(5);
		root.left = node1;
		root.right = node2;
		node1.left = node3;
		node1.right = node4;
		String res = code.serialize(root);
		System.out.println(res);
		TreeNode node = code.deserialize(res);
		System.out.println(node.val);
	}

}

class Codec {

    // Encodes a tree to a single string.
    public String serialize(TreeNode root) {
    	StringBuilder sb = new StringBuilder();
    	inorder(root, sb);
    	sb.deleteCharAt(sb.length() - 1);
    	return sb.toString();
    }
    private void inorder(TreeNode root, StringBuilder sb){
    	if(root == null) {
    		sb.append("N,").toString();
    		return;
    	}
        sb.append(root.val + ",");
        inorder(root.left, sb);
        inorder(root.right, sb);
    }

    // Decodes your encoded data to tree.
    public TreeNode deserialize(String data) {
    	if(!data.contains(",")) return null;
    	String[] datas = data.split(",");
    	len = 0;
    	TreeNode root = buildTree(datas);
    	return root;
    }
    int len = 0;
    private TreeNode buildTree(String[] data){
    	if(len >= data.length) return null;
    	if(data[len].equals("N")) return null;
    	TreeNode root = new TreeNode(Integer.parseInt(data[len]));
    	len++;
    	root.left = buildTree(data);
    	len++;
    	root.right = buildTree(data);
    	return root;
    }
}