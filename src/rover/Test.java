package rover;

import java.util.Arrays;

public class Test {

	public static void main(String[] args) {
		for (int nb_check_each_side = 1; nb_check_each_side <= 5; nb_check_each_side++) {
			int check_precision = 10;
			int check_relative_angles[] = new int[2*nb_check_each_side];
			for (int i = 0; i < check_relative_angles.length; i++) {
				if (i < nb_check_each_side) {
					check_relative_angles[i] = -check_precision;
				} else if (i == nb_check_each_side) {
					check_relative_angles[i] = (nb_check_each_side+1)*check_precision;
				} else {
					check_relative_angles[i] = check_precision;
				}
			}
			System.out.println(Arrays.toString(check_relative_angles));
	
		}
		float  x             = 170													/1000f;
		System.out.println(x);
		System.out.println((int)(2 * 1500/(x*1000)));
	}
}
