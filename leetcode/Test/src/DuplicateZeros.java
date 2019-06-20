import java.util.Arrays;

public class DuplicateZeros {

    public void duplicateZeros(int[] arr) {
        if(arr == null || arr.length == 0) return;
        int pos = 0, len = 0;
        for(int i = 0; i < arr.length; i++){
        	if(arr[i] == 0) len += 2;
        	else len++;
        	pos = i;
        	if(len >= arr.length) break;
        }
        int index = arr.length - 1;
        if(len > arr.length){
        	arr[index--] = 0; pos--;
        }
        for(int i = pos; i >= 0; i--){
        	if(arr[i] == 0){
        		arr[index--] = 0;
        		arr[index--] = 0;
        	}else{
        		arr[index--] = arr[i];
        	}
        }
    }
    
	public static void main(String[] args) {
		DuplicateZeros duplicateZeros = new DuplicateZeros();
		int[] arr = new int[]{1,0,2,3,0,4,5,0};
		duplicateZeros.duplicateZeros(arr);
		System.out.println(Arrays.toString(arr));
		
		arr = new int[]{1,2,3};
		duplicateZeros.duplicateZeros(arr);
		System.out.println(Arrays.toString(arr));
		
		arr = new int[]{1,1,0};
		duplicateZeros.duplicateZeros(arr);
		System.out.println(Arrays.toString(arr));
	}

}
