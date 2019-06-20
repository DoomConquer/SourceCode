package toutiao;

public class ValidUtf {

    public boolean validUtf8(int[] data) {
        for(int i = 0; i < data.length; i++){
        	if((data[i] & 0x00000080) == 0) continue;
        	int count = 0;
        	if((data[i] & 0x000000c0) == 0x000000c0) count = 1;
        	if((data[i] & 0x000000e0) == 0x000000e0) count = 2;
        	if((data[i] & 0x000000f8) == 0x000000f0) count = 3;
        	if(count == 0) return false;
        	while(count-- > 0 && ++i < data.length){
        		if((data[i] & 0x00000080) == 0x00000080) continue;
        		return false;
        	}
        	if(count > 0) return false;
        }
        return true;
    }
    
	public static void main(String[] args) {
		ValidUtf validUtf = new ValidUtf();
		System.out.println(validUtf.validUtf8(new int[]{197, 130, 1}));
		System.out.println(validUtf.validUtf8(new int[]{235, 140, 4}));
	}

}
