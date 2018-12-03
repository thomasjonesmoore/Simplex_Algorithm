/*
	Thomas Jones-Moore
	
*/	
	
import java.util.Scanner;
import java.io.File;
import java.util.StringTokenizer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.text.*;

// MAKE SURE TO ADD SUPPORT FOR WHEN DATA FILES ARE NOT ALL INTEGERS

public class dataread{

	public static ArrayList<Double> a_everything;
	public static ArrayList<Integer> b_values;
	public static ArrayList<Integer> c_values;	
	public static int row_count;
	public static int col_count;
	public static int is_twophase = 0;
	

	public static void main(String args[]) throws Exception{
	
		File FILEa = new File("A.csv");
		File FILEb = new File("b.csv");
		File FILEc = new File("c.csv");

		a_everything = new ArrayList<Double>();
		b_values = get_vals(FILEb);
		c_values = get_vals(FILEc);

		Scanner sc = new Scanner(FILEa);
		String temp;
		col_count = 0;
		String line = "";
		while(sc.hasNextLine()){
			line = sc.nextLine();
			String[] ln = line.split(",");
			for(String t: ln){
				a_everything.add(Double.parseDouble(t));
				col_count++;
			}
			break;
		}
		
		String temp2;
		row_count = 1;
		String line2 = "";
		while(sc.hasNextLine()){
			row_count++;
			line2 = sc.nextLine();
			String[] ln = line2.split(",");
			for(String t: ln){
				a_everything.add(Double.parseDouble(t));
			}
		}
		
		/* Initializing values from matrix */
		int[][] A_values = new int[row_count][col_count];	
		int c = 0;
		for(int i = 0; i < row_count; i++){
			for(int j = 0; j < col_count; j++){
				if(c < a_everything.size()){
					int tempp = (int)Math.round(a_everything.get(c));
					A_values[i][j] = tempp;
				}
				c++;
			}
		}
		
		/* Initializing table sizes */
		int table_row_size = (col_count + b_values.size() + 2);
		int table_col_size = (b_values.size() + 1);
		double[][] table = new double[table_col_size][table_row_size]; 
		//System.out.println("row_size: " + table_row_size + " col size: " + table_col_size);
		
		initialize_table(table, A_values);
		
		if(do_we_need_two_phase() == 1){
				System.out.println("INFEASIBLE PROBLEM! \nHere I would do the first part of" +  
				" 2 phase to determine if the data set is infeasible. ");
				is_twophase = 1;
				two_phase(table);
		}
		else{
			while(are_we_done_yet(table) != 1){
				simplexiter(table, 0);
			}
			System.out.println("Optimal Solution Found. Current Dictionary Reads");
			print(table);
		}
	}
	
	/* Used to store values in file in an array */
	public static ArrayList<Integer> get_vals(File a) throws Exception{
		ArrayList<Integer> a_values = new ArrayList<Integer>();
		Scanner sc = new Scanner(a);
		int temp;
			
		while(sc.hasNext()){
			temp = sc.nextInt();
			a_values.add(temp);
		}
		return a_values;
	}
	
	/* Fills table with corresponding values with the initial data */
	public static void initialize_table(double[][] table, int[][] A_values){
		for(int i = 0; i < table.length; i++){
			for(int j = 0; j < table[i].length; j++){
				if(j < A_values[0].length){
					if(i < b_values.size()){
						table[i][j] = A_values[i][j];
					}
					else{
						table[i][j] = -1 * c_values.get(j);
					}
				}	
			}
			table[i][col_count + i] = 1;
			if(i < table.length-1){
				table[i][table[i].length-1] = b_values.get(i);
			}
		}
	}
	
	
	/* Used to find pivot row in simplex algorithm */
	public static int find_pivot_col(double[][] table){
		int pivot_col_index = -1;
		int last_row_index = table.length - 1;
		double min = 0.0;
		
		for(int i = 0; i < table[0].length-1; i++){
			if(table[last_row_index][i] < min){
				min = table[last_row_index][i];
				pivot_col_index = i;
			}
		}
		return pivot_col_index;
	}
	
	/*  */
	public static int find_pivot_row(double[][] table, int pivot_col_index){
		int pivot_row_index = -1;
		int last_col_index = table[0].length - 1;
		double min;
		double val;
		if(table[0][pivot_col_index] > 0.0){
			min = table[0][last_col_index] / table[0][pivot_col_index];
			pivot_row_index = 0;
		}
		else{
			min = 99999999.0;
		}
		
		
		for(int i = 1; i < (table.length-1); i++){
			val = table[i][last_col_index] / table[i][pivot_col_index];
			if(val < min && val > 0.0){
				//System.out.println(table[i][last_col_index] + " beep " + table[i][pivot_col_index]);
				min = val;
				pivot_row_index = i;
			}
		}
		
		//System.out.println("pivot row is: " + pivot_row_index);
		
		return pivot_row_index;
	}
	
	/*  */
	public static void update_pivot_rowcolumn(double[][] table, int pivot_row, int pivot_col){
		double scalar = 1 / table[pivot_row][pivot_col];
		//System.out.println("scalar: " + scalar);
		
		for(int i = 0; i < table[0].length; i++){
			table[pivot_row][i] = table[pivot_row][i] * scalar;
			//System.out.print(table[pivot_row][i] + " ");
		}
	}

	/*  */
	public static void zero_out_pivot_columns(double[][] table, int pivot_row, int pivot_col){
		double scalar;
		
		for(int i = 0; i < table.length; i++){
			if(i != pivot_row){
				scalar = table[i][pivot_col];
				for(int j = 0; j < table[i].length; j++){
					table[i][j] = table[i][j] - (scalar * table[pivot_row][j]);
				}
			}
		}
		
	}
	
	/*  */
	public static int are_we_done_yet(double[][] table){
		double min = 0.0;
		int bool = 1;
		int last_row_index = table.length - 1;
		for(int i = 0; i < table[0].length; i++){
			if(table[last_row_index][i] < min){
				bool = 0;
			}	
		}
		return bool;
	}

	public static void print(double[][] table){
		DecimalFormat df = new DecimalFormat("#.###");
		for(int i = 0; i < table.length; i++){
			for(int j = 0; j < table[i].length; j++){
					//System.out.print(df.format(table[i][j]) + " ");
			}
			//System.out.println();
		}
		
		double val = 0.0;
		//int j;
		for(int i = 0; i < table.length; i++){
			int x = -1;
			if(i != table.length-1){
				x = find_x(table[i], table);
				System.out.print("X_" + (x+1) + " = ");
			}
			else{
				System.out.print("Z = ");
			}
			for(int j = 0; j < table[0].length-1; j++){
				val = (-1 * table[i][j]);
				if(j == 0){
					System.out.print(df.format(table[i][table[0].length-1]) + " + ");
					if(val != 0.0 && j != x){
						System.out.print(df.format(val) + "X_" + (j+1) + " + ");
					}	
				}
				else if(j != x && j != table[0].length-1 && val != 0.0 && j != table[0].length-2){
					System.out.print(df.format(val) + "X_" + (j+1) + " + ");
				}
				
			}
			//System.out.print(df.format(val) + "X_" + (j+1));
			System.out.println();
		}
		
		
		System.exit(0);
	}
	
	// if flag is 1, then it is a 2phase problem. if not, then it is normal
	public static void simplexiter(double[][] table, int flag){
		int pivot_col = find_pivot_col(table);
		if(pivot_col < 0){
			System.out.println("INFEASIBLE. Currect Dictionary Reads");
			if(flag == 0)
				print(table);
			else
				twophase_print(table);
		}
		int pivot_row = find_pivot_row(table, pivot_col);
		if(pivot_row < 0 && flag != 1){
			System.out.println("Unbounded Problem Detected. Current Dictionary Reads");
			if(flag == 0)
				print(table);
			else
				twophase_print(table);
		}
		update_pivot_rowcolumn(table, pivot_row, pivot_col);
		zero_out_pivot_columns(table, pivot_row, pivot_col);		
	}

	// PUTS x_0 COLUMN AT THE VERY beginning of matrix
	public static void two_phase(double[][] table){
		/*  1. add the x_0 column full of -1's
			2. do the first caveat step with two phase problems
			3. then continue with simplex, but when there is a tie in leaving
			variables, make x_0 leave. Then we're done(?). 
			4. Make sure Z is updating properly
		*/

		//    1. Putting column of -1.0's in front of everything except for the last row
		//       where it puts a +1.0
		double[][] new_table = new double[table.length][table[0].length + 1];	
		for(int i = 0; i < new_table.length; i++){
			for(int j = 0; j < new_table[0].length; j++){
				if(j != 0){
					new_table[i][j] = table[i][j-1];
				}
				else{
					if(i != new_table.length-1){
						new_table[i][j] = -1.0;
					}
					else{
						new_table[i][j] = 1.0;
					}
				}
			}
		}
		for(int i = 0; i < new_table[0].length; i++){
			if(i == 0){
				new_table[new_table.length-1][i] = 1.0;
			}
			else if(i != new_table[0].length-2){
				new_table[new_table.length-1][i] = 0.0;
			}
		}
		
		// Holding old z row value
		double[] old_z = new double[table[0].length];
		for(int i = 0; i < old_z.length; i++){
			old_z[i] = table[table.length-1][i];
		}
	
		int pivot_col = 0;
		int pivot_row = find_pivot_row(new_table, pivot_col);
		if(pivot_row < 0){
			System.out.println("Unbounded Problem Detected. Current Dictionary Reads");
			twophase_print(new_table);
		}
		update_pivot_rowcolumn(new_table, pivot_row, pivot_col);
		zero_out_pivot_columns(new_table, pivot_row, pivot_col);
		
		// Does phase1 of two_phase. Goes until X_0 leaves.
		while(are_we_done_yet(new_table) != 1  && is_phase_one_done(new_table) != 1){
			simplexiter(new_table, 1);
		}
		//update_z(new_table, old_z);
		
		System.out.println("Phase1 is done. Now I would do the second part of 2phase to find optimal X values. Current dictionary:");
		twophase_print(new_table);
		
		//System.out.println("pivotrow: "+pivot_row);
	
	
		
		
		// Printing new tableau
		for(int i = 0; i < new_table.length; i++){
			for(int j = 0; j < new_table[0].length; j++){
				//System.out.print(new_table[i][j] + " ");
			}
			//System.out.println();
		}
		
	
	}
	
	/* Checks to see if there is a negative in the constraints. Returns 1 if there is */
	public static int do_we_need_two_phase(){
		int bool = 0;	
		for(int i = 0; i < b_values.size(); i++){
			if(b_values.get(i) < 0){
				bool = 1;
			}
		}
		return bool;
	}

	/* Returns index of the correct x_value. Add 1 to this value to the the x_# */
	public static int find_x(double[] row, double[][] table){
		int x = -1;
		for(int i = 0; i < row.length-2; i++){
			if(row[i] == 1.0){
				x = i;
				if(test_x(x, table) == 1){
					break;
				}
			}
		}
		return x;
	}

	public static int test_x(int x, double[][] table){
		int bool = 1;
		for(int i = 0; i < table.length; i++){
			if(table[i][x] != 0.0){
				bool = 0;
			}
		}
		return bool;
	}

	public static void twophase_print(double[][] table){
		DecimalFormat df = new DecimalFormat("#.###");
		for(int i = 0; i < table.length; i++){
			for(int j = 0; j < table[i].length; j++){
				
					//System.out.print(df.format(table[i][j]) + " ");
				
				
			}
			//System.out.println();
		}
		
		double val = 0.0;
		//int j;
		for(int i = 0; i < table.length; i++){
			int x = -1;
			if(i != table.length-1){
				x = find_x(table[i], table);
				System.out.print("X_" + (x) + " = ");
			}
			else{
				System.out.print("Z = ");
			}
			for(int j = 0; j < table[0].length-1; j++){
				val = (-1 * table[i][j]);
				if(j == 0){
					System.out.print(df.format(table[i][table[0].length-1]) + " + ");
					if(val != 0.0 && j != x){
						System.out.print(df.format(val) + "X_" + (j) + " + ");
					}	
				}
				else if(j != x && j != table[0].length-1 && val != 0.0 && j != table[0].length-2){
					System.out.print(df.format(val) + "X_" + (j) + " + ");
				}
				
			}
			//System.out.print(df.format(val) + "X_" + (j+1));
			System.out.println();
		}
		System.exit(0);
	}
	
	public static int is_phase_one_done(double[][] table){
		int bool = 0;
		double[] z_ar = new double[table[0].length];
		z_ar[0] = 1.0;
		z_ar[z_ar.length-2] = 1.0;
		
		if(z_ar.equals(table[table.length-1])){
			bool = 1;
			System.out.print("good z: ");
			for(int i = 0; i < z_ar.length; i++){
			System.out.println(z_ar[i] + " ");
		}
		}
		return bool;
	}
	
	public static void update_z(double[][] table, double[] old_z){
		double scalar = 0.0;
		int row = -1;
		double[] temp = new double[old_z.length];
		//twophase_print(table);
		for(int i = 0; i < old_z.length; i++){
			if(old_z[i] != 0.0){
				//System.out.println(Arrays.toString(old_z));
				if(two_phase_test_x(i+1, table) == 1){
					System.out.println(row);
					row = find_row(i+1, table);
				}
				scalar = old_z[i];
				for(int j = 0; j < temp.length; j++){
					temp[j] = temp[j] + (scalar * table[row][j]);
				}
			}
			
		}
	}
	
	public static int two_phase_test_x(int x, double[][] table){
		int bool = 1;
		for(int i = 0; i < table.length-1; i++){
			if(table[i][x] != 0.0){
				bool = 0;
			}
		}
		return bool;
	}
	
	public static int find_row(int x, double[][] table){
		int row = -1;
		for(int i = 0; i < table.length; i++){
			if(table[i][x] == 1.0){
				row = i;
			}
		}
		return row;
	}
	
	/*  */
	public static int two_phase_find_pivot_row(double[][] table, int pivot_col_index){
		int pivot_row_index = -1;
		int last_col_index = table[0].length - 1;
		double min;
		double val;
		if((table[0][last_col_index] / table[0][pivot_col_index]) > 0.0){
			min = table[0][last_col_index] / table[0][pivot_col_index];
			pivot_row_index = 0;
		}
		else{
			min = 99999999.0;
		}
		
		
		for(int i = 1; i < (table.length-1); i++){
			val = table[i][last_col_index] / table[i][pivot_col_index];
			if(val < min && val > 0.0){
				//System.out.println(table[i][last_col_index] + " beep " + table[i][pivot_col_index]);
				min = val;
				pivot_row_index = i;
			}
		}
		
		//System.out.println("pivot row is: " + pivot_row_index);
		
		return pivot_row_index;
	}
}