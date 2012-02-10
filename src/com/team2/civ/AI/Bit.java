public class Bit {
	int value = 0;
	Bit(boolean input){
		if (input){
			value = 1;
		} else {
			value = 0;
		}
	}
	Bit(int input){
		if (input == 0){
			value = 0;
		} else {
			value = 1;
		}
	}		
	public static int val(){
		return value;
	}
	public static boolean truth(){
		if (value == 1){
			return true;
		} else {
			return false;
		}
	}
}
