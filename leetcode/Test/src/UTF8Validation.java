
public class UTF8Validation {

	public boolean validUtf8(int[] data) {
		for(int i = 0; i < data.length; i++){
			if((data[i] & 0x00000080) == 0) continue;
			int times = 0;
			if((data[i] & 0x000000C0) == 0x000000C0) times = 1;
			if((data[i] & 0x000000E0) == 0x000000E0) times = 2;
			if((data[i] & 0x000000F8) == 0x000000F0) times = 3;
			if(times == 0) return false;
			while(times-- > 0 && ++i < data.length){
				if((data[i] & 0x00000080) == 0x00000080) continue;
				return false;
			}
			if(times > 0) return false;
		}
		return true;
	}
	
	public static void main(String[] args) {
		UTF8Validation utf8 = new UTF8Validation();
		System.out.println(utf8.validUtf8(new int[]{197, 130, 1}));
		System.out.println(utf8.validUtf8(new int[]{235, 140, 4}));
		System.out.println(utf8.validUtf8(new int[]{255}));
		System.out.println(utf8.validUtf8(new int[]{248,130,130,130}));
	}

}
