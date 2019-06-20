
class Trie {
	
	TrieNode root = null;
	/** Initialize your data structure here. */
    public Trie() {
        root = new TrieNode();
    }
    
    /** Inserts a word into the trie. */
    public void insert(String word) {
    	if(!word.isEmpty()){
			TrieNode p = root;
			for(char ch : word.toCharArray()){
				if(p.child[ch - 'a'] == null){
					TrieNode node = new TrieNode(ch);
					p.child[ch - 'a'] = node;
				}
				p = p.child[ch - 'a'];
			}
			p.isEnd = true;
		}
    }
    
    /** Returns if the word is in the trie. */
    public boolean search(String word) {
    	if(root != null && !word.isEmpty()){
			TrieNode p = root;
			for(char ch : word.toCharArray()){
				if(p.child[ch - 'a'] == null) return false;
				p = p.child[ch - 'a'];
			}
			if(p.isEnd) return true;
		}
		return false;
    }
    
    /** Returns if there is any word in the trie that starts with the given prefix. */
    public boolean startsWith(String prefix) {
        if(root != null && !prefix.isEmpty()){
        	TrieNode p = root;
        	for(char ch : prefix.toCharArray()){
        		if(p.child[ch - 'a'] == null) return false;
        		p = p.child[ch -'a'];
        	}
        	return true;
        }
        return false;
    }

}

class TrieNode{
	char val;
	TrieNode[] child;
	boolean isEnd;
	public TrieNode(){
		child = new TrieNode[26];
		isEnd = false;
	}
	public TrieNode(char val){
		this.val = val;
		child = new TrieNode[26];
		isEnd = false;
	}
}
