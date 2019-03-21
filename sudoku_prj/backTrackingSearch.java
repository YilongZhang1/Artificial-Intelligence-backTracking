package sudoku_pkg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Random;
import java.util.Stack;

public class backTrackingSearch
{
	private ArrayList<int[]> variables = new ArrayList<int[]>();			// keep track of the variables
	private ArrayList<Character[]> domains = new ArrayList<Character[]>();	// record domain of each variable
	private int sudoku_length;		// this sudoku code is parameterized
	private int sudoku_width;
	private Stack<Character[][]> searchTree = new Stack<Character[][]>();	// store the assignment in a stack
	private Stack<ArrayList<int[]>> varStack = new Stack<ArrayList<int[]>>();;				// store the variables as we go down the search tree
	private Stack<ArrayList<Character[]>> domainStack = new Stack<ArrayList<Character[]>>();// store the domains of variables as we go down the search tree
	private int stepCnt = 0; // for display purpose
	// wrapper of the core algorithm
	public boolean backTrackingSearch_(Character[][] assignment, int L, int W)
	{
		sudokuSizeSet(L, W);
		// initialize variables and corresponding domains
		initialization(assignment);
		// before going to the recursion, we push the initial status to stack
		// we need deep copy here. Shallow copy does not work!

		searchTree.push(deepCopyAssignment(assignment));
		varStack.push(deepCopyVariables(variables));
		domainStack.push(deepCopyDomains(domains));
		return Recursive_Backtracking(assignment);		
	}
	
	// the core algorithm of back tracking algorithm
	private boolean Recursive_Backtracking(Character[][] assignment)
	{		
		// the job is done!
		if(isAssignmentComplete(assignment) == true)
			return true;
		// var is the 2-D index of the selected variable
		int[] var = selectUnassignedVariable(assignment);
		
		// find the position of var in the variable list
		int var_index = 0;
		for(int i = 0; i < variables.size(); i++)
			if(var[0] == variables.get(i)[0] && var[1] == variables.get(i)[1])
			{
				var_index = i;
				break;
			}
		// values is the domain of the selected variable
		Character[] values = domains.get(var_index);
		if(stepCnt < 3)
		{
			System.out.println("*                   selected variable is at [" + var[0] + ", " + var[1] + "]           *");
			System.out.println("**************************************************************");
			System.out.println("*                   domain of  selected variable: " + values.length + "          *");
			System.out.println("**************************************************************");
			stepCnt++;
		}		
		for(int i = 0; i < values.length; i++)
			if( isConsistent(var, values[i], assignment) )
			{
				assignment[var[0]][var[1]] = values[i];
				
				// domains is pushed into the stack if forward checking is true
				if (forwardChecking(var, values[i]) == true)
				{
					searchTree.push(deepCopyAssignment(assignment));
					variables.remove(var_index);
					varStack.push(deepCopyVariables(variables));
					boolean result = Recursive_Backtracking(assignment);
					if (result == true)
						return result;	
					searchTree.pop();
					assignment = deepCopyAssignment(searchTree.peek());
					varStack.pop();
					variables = deepCopyVariables(varStack.peek());
					domainStack.pop();
					domains = deepCopyDomains(domainStack.peek());					
				}
			}
		return false;
	}
	
	// set sudoku size
	private void sudokuSizeSet(int L, int W)
	{
		this.sudoku_length = L;
		this.sudoku_width = W;
	}
	
	
	// initialize the variables, and the domain of each variable
	private void initialization(Character[][] assignment)
	{
		for(int i = 0; i < sudoku_length; i++)
			for(int j = 0; j < sudoku_width; j++)
				if(assignment[i][j] == 'x')
				{
					variables.add(new int[]{i,j});	
					domains.add(new Character[]{'1','2','3','4','5','6','7','8','9'});
				}	
	}
	
	// estimate whether the assignment is complete or not
	private boolean isAssignmentComplete(Character[][] assignment)
	{
		for(int i = 0; i < sudoku_length; i++)
			for(int j = 0; j < sudoku_width; j++)
				if(assignment[i][j] == 'x')
					return false;
		return true;
	}
	
	// find a variable with MRV and degree heuristic
	// return index of the variable in the variable list
	private int[] selectUnassignedVariable(Character[][] assignment)
	{
		// MRV_indice records the indices of the variables with minimum remaining values
		ArrayList<int[]> MRV_indice = MRV();
		// if there exists a variable with the minimum remaining value
		if(MRV_indice.size() == 1)
			return MRV_indice.get(0);
		// otherwise, we have to turn to heuristic degree tie breaker!
		ArrayList<int[]> degreeHeuristic_indice = degreeHeuristic(assignment, MRV_indice);
		if(degreeHeuristic_indice.size() == 1)
			return degreeHeuristic_indice.get(0);
		// randomly expand those with same degree as well as the same MRVs
		int max = degreeHeuristic_indice.size();
        int min = 0;
        Random random = new Random();
        int s = random.nextInt(max)%(max-min+1) + min;
        return degreeHeuristic_indice.get(s);
	}
	
	// minimum remaining values
	private ArrayList<int[]> MRV()
	{
		// count the length of the domain of each variable
		ArrayList<Integer> remainValuesCnt = new ArrayList<Integer>();
		for(int i = 0; i < domains.size(); i++)
			remainValuesCnt.add(domains.get(i).length);

		// find the shortest length - the domain with fewest values
		int fewestValues = sudoku_length * sudoku_width;
		for(int i = 0; i < remainValuesCnt.size(); i++)
			if(fewestValues > remainValuesCnt.get(i))
				fewestValues = remainValuesCnt.get(i);	

		// find the indices of the variables/domains with the fewest values
		ArrayList<int[]> minimumRemainingValueIdx = new ArrayList<int[]>();
		int counter = 0;
		for(int i = 0; i < remainValuesCnt.size(); i++)
		{
			if(fewestValues == remainValuesCnt.get(i))
				minimumRemainingValueIdx.add(new int[]{variables.get(counter)[0],variables.get(counter)[1]});
			counter++;
		}
		return minimumRemainingValueIdx;
	}
	
	// degree heuristic - used as secondary tie breaker
	private ArrayList<int[]> degreeHeuristic(Character[][] assignment, ArrayList<int[]> MRV_indice)
	{
		// degrees denote the degree of each variable in the array MRV_indice
		ArrayList<int[]> degrees = new ArrayList<int[]>();
		Iterator<int[]> it = MRV_indice.iterator();

		while(it.hasNext())
		{
			int[] currentVarIdx = it.next();
			int x = currentVarIdx[0];
			int y = currentVarIdx[1];
			
			int currentVarDegree = 0;
			// check number of constrains in the same row			
			for(int i = 0; i < sudoku_length; i++)
				if(assignment[i][y] == 'x')
					currentVarDegree++;
			currentVarDegree--; // the current variable should not be considered a degree
			// check number of constrains in the same column			
			for(int j = 0; j < sudoku_width; j++)
				if(assignment[x][j] == 'x')
					currentVarDegree++;
			currentVarDegree--; // the current variable should not be considered a degree
			// check number of constrains in the same 3*3 square
			int start_x = x/3;
			start_x *= 3; 		// in this way we take the floor
			int start_y = y/3;
			start_y *= 3;		// same way
			for(int i = start_x; i < start_x + 3; i++)
				for(int j = start_y; j < start_y +3; j++)
					// those in the same row or column have been checked already
					if(i != x && j != y && assignment[i][j] == 'x')
						currentVarDegree++;
			degrees.add(new int[]{currentVarDegree, x, y}); // degrees is a 1*3 array, with the first element degree, and the last two index of the current variable
		}
		int largest_degree = 0;
		for(int i = 0; i < degrees.size(); i++)
			if (largest_degree < degrees.get(i)[0])
				largest_degree = degrees.get(i)[0];
		
		if(stepCnt < 3)
		{
			System.out.println("*                  degree of selected variable: " + largest_degree + "           *");
			System.out.println("**************************************************************");
		}	
		// returnInfo contains the x and y index of the variables with largest degree
		ArrayList<int[]> returnInfo = new ArrayList<int[]>();
		for(int i = 0; i < degrees.size(); i++)
			if (largest_degree == degrees.get(i)[0])
				returnInfo.add(new int[]{degrees.get(i)[1], degrees.get(i)[2]});
		return returnInfo;
	}

	// check whether the current value of the selected variable is consistent with the assignment up to now
	private boolean isConsistent(int[] var, Character value, Character[][] assignment)
	{
		// at this time, the value of var is not added into the assignment
		// compare with elements in the same column
		for(int i = 0; i < sudoku_length; i++)
			if(value == assignment[i][var[1]])
				return false;
		// compare with elements in the same row
		for(int j = 0; j < sudoku_width; j++)
			if(value == assignment[var[0]][j])
				return false;			
		int start_x = var[0]/3;
		start_x *= 3;
		int start_y = var[1]/3;
		start_y *= 3;
		for(int i = start_x; i < start_x + 3; i++)
			for(int j = start_y; j < start_y +3; j++)
				if(value == assignment[i][j])
					return false;		
		return true;
	}

	// forward checking the variables 
	private boolean forwardChecking(int[] var, Character value)
	{
		ArrayList<Character[]> tempDomains = new ArrayList<Character[]>();
		Character[] revisedDomain;
		
		int start_x = var[0]/3;
		start_x *= 3;		
		int start_y = var[1]/3;
		start_y *= 3;	

		// traverse all the variables to find those in the same row, or column, or square with the selected variable
		for(int i = 0; i < variables.size(); i++)
		{
			int currentCheck_x = variables.get(i)[0];
			int currentCheck_y = variables.get(i)[1];
			// of course we don't check var itself
			if(currentCheck_x == var[0] && currentCheck_y == var[1])
				continue;
			
			// we focus on the variables that are in the same row, or column, or same square with the selected variable
			if(currentCheck_x == var[0] || currentCheck_y == var[1] || ((currentCheck_x >= start_x && currentCheck_x < start_x + 3) && (currentCheck_y >= start_y && currentCheck_y < start_y + 3)))
			{
				
				Character[] currentDomain = domains.get(i);
				int[] revisionInfo = new int[currentDomain.length]; // 0 denotes need to prune, 1 needs to store
				for(int j = 0; j < currentDomain.length; j++)
				{
					if(currentDomain[j] == value)
						revisionInfo[j] = 0;
					else
						revisionInfo[j] = 1;
				}
				// check sum
				int revisionInfoSum = 0;
				for(int k = 0; k < revisionInfo.length; k++)
					revisionInfoSum += revisionInfo[k];
				if(revisionInfoSum == 0) // some variable's domain is empty
					return false;
				// otherwise, we need to update the domain of this variable
				revisedDomain = new Character[revisionInfoSum];
				int ptr = 0;
				for(int k = 0; k < revisionInfo.length; k++)
					if(revisionInfo[k] == 1)
						revisedDomain[ptr++] = currentDomain[k];
			}
			else
				revisedDomain = domains.get(i);
			tempDomains.add(revisedDomain);
		}
		domains = tempDomains;
		domainStack.push(deepCopyDomains(tempDomains));
		return true;
	}
	
	// return the full assignment if succeed
	public Character[][] getSolution()
	{
		return searchTree.peek();
	}

	// return type of inference
	class inferType
	{
		int[] addr;
		Character value;
		inferType(int[] Index, Character V)
		{
			addr = Index;
			value = V;
		}
	}

	// deep copy assignment
	Character[][] deepCopyAssignment(Character[][] assignment)
	{
		Character[][] cpAssignment = new Character[sudoku_length][sudoku_width];
		for(int i = 0; i < sudoku_length; i++)
			for(int j = 0; j < sudoku_width; j++)
				cpAssignment[i][j] = assignment[i][j];
		return cpAssignment;
	}

	// deep copy variables
	ArrayList<int[]> deepCopyVariables(ArrayList<int[]> variables)
	{
		ArrayList<int[]> cpVariables = new ArrayList<int[]>();
		for(int i = 0; i < variables.size(); i++)
		{
			int[] tempAddr = new int[2];
			tempAddr[0] = variables.get(i)[0];
			tempAddr[1] = variables.get(i)[1];
			cpVariables.add(tempAddr);
		}
		return cpVariables;
	}
	
	// deep copy domains
	ArrayList<Character[]> deepCopyDomains(ArrayList<Character[]> domains)
	{
		ArrayList<Character[]> cpDomains = new ArrayList<Character[]>();
		for(int i = 0; i < domains.size(); i++)
		{
			Character[] tempDomain = new Character[domains.get(i).length];
			for(int j = 0; j < domains.get(i).length; j++)
				tempDomain[j] = domains.get(i)[j];
			cpDomains.add(tempDomain);
		}
		return cpDomains;
	}	
}