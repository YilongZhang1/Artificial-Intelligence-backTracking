package sudoku_pkg;

import java.util.Scanner;

public class startHere
{
	public static int sudoku_length = 9;
	public static int sudoku_width = 9;
	
	public static void main(String[] args)
	{
		Scanner scan = new Scanner(System.in);
		// input format control
		System.out.println("**************************************************************");
		System.out.println("*                                                            *");
		System.out.println("*               I can solve your sudoku problem!             *");
		System.out.println("*            Input your sudoku and let's have a try!         *");	
		System.out.println("*              For blank squares input 'x' instead.          *");
		System.out.println("*                  No blankspaces between input.             *");
		System.out.println("*                                                            *");
		System.out.println("**************************************************************");
		System.out.println();
		
		String inputStr;
		Character[][] assignment = new Character[sudoku_length][sudoku_width];
		for(int i = 0; i < sudoku_length; i++)
		{
			System.out.print("line " + (i + 1) + ": ");
			inputStr = scan.next();
			for(int j = 0; j < sudoku_width; j++)
			assignment[i][j] = inputStr.charAt(j);
		}
		System.out.println("**************************************************************");
		System.out.println("*                      Your input sudoku is                  *");
		for(int i = 0; i < sudoku_length; i++)
		{
			System.out.print("*                    ");
			for(int j = 0; j < sudoku_width; j++)
				System.out.print(assignment[i][j] + "  ");
			System.out.println("             *");
		}
		System.out.println("**************************************************************");
		
		long startTime = System.currentTimeMillis();
		backTrackingSearch backTrackInstance = new backTrackingSearch();

		Character[][] result;
		if(backTrackInstance.backTrackingSearch_(assignment, sudoku_length, sudoku_width) == true)
		{
			long endTime = System.currentTimeMillis();
			result = backTrackInstance.getSolution();
			System.out.println("*                      The solved sudoku is                  *");
			for(int i = 0; i < sudoku_length; i++)
			{
				System.out.print("*                    ");
				for(int j = 0; j < sudoku_width; j++)
					System.out.print(result[i][j] + "  ");
				System.out.println("             *");
			}
			System.out.println("**************************************************************");	
			System.out.println("*                    execution time is " + (endTime - startTime) + " ms                  *");
			System.out.println("**************************************************************");
		}
		else
		{
			System.out.println("*              Your input sudoku has no solution!            *");	
			System.out.println("**************************************************************");			
		}

	}
}
