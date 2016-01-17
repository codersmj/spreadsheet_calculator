import java.util.EmptyStackException;
import java.util.LinkedHashSet;
import java.util.Scanner;
import java.util.Set;
import java.util.Stack;

/**
 * A program to interpret expressions and calculate their values
 * @author Sukalpo Mitra
 *
 */
public class Spreadsheet {

	private Cell[][] cells; //Multidimensional array to store each cell in a spreadsheet
	private int columns;
	private int rows;

	/**
	 * A nested class used as a member variable for the outer class
	 * @author Sukalpo Mitra
	 *
	 */
	private class Cell{
		Double interpretedValue;
		boolean valueInterpreted;
		String cellExpression;
		int rowPosition;
		int colPosition;
		private String cellRefExpr;

		/**\
		 * Constructor
		 * @param cellExpression
		 * @param rowPosition
		 * @param colPosition
		 */
		public Cell(final String cellExpression, final int rowPosition, final int colPosition){
			this.cellExpression = cellExpression;
			this.valueInterpreted = false;
			this.rowPosition = rowPosition;
			this.colPosition = colPosition;
		}

		/**
		 * 
		 * @return
		 */
		public String getCellRefExpr() {
			return cellRefExpr;
		}

		/**
		 * 
		 * @param cellRefExpr
		 */
		public void setCellRefExpr(final String cellRefExpr) {
			this.cellRefExpr = cellRefExpr;
		}
	}

	/**
	 * Main Program Entry Point
	 * @param args
	 */
	public static void main(final String[] args)
	{
		Spreadsheet spreadSheet = new Spreadsheet();
		loadSpreadsheet(spreadSheet);
		for (Cell[] cells:spreadSheet.cells){
			for (Cell cell: cells){
				spreadSheet.calculateCellValue(cell, new LinkedHashSet<Cell>());
			}
		}
		printCalculationOutput(spreadSheet);
	}

	/**
	 * Scan the stdin and populate the members of the classes
	 * @param spreadSheet
	 */
	private static void loadSpreadsheet(final Spreadsheet spreadSheet) {
		Scanner stdIn = new Scanner(System.in);
		try {
			int linesRead = 0, row = 0, col = 0;
			while (stdIn.hasNextLine()) {
				if (linesRead == 0){
					String[] firstLineContent = stdIn.nextLine().split(" ");
					if (firstLineContent.length != 2){
						System.out.println("No. of arguments in the first line of input is not 2.");
						System.exit(1);
					}
					else if (Integer.parseInt(firstLineContent[0]) == 0){
						System.out.println("Column Size cannot be 0.");
						System.exit(1);
					}
					else if (Integer.parseInt(firstLineContent[1]) == 0){
						System.out.println("Row Size cannot be 0.");
						System.exit(1);
					}
					else if (Integer.parseInt(firstLineContent[1]) > 26){
						System.out.println("Row Size cannot be greater than 26.");
						System.exit(1);
					}
					else{
						spreadSheet.columns = Integer.parseInt(firstLineContent[0]);
						spreadSheet.rows = Integer.parseInt(firstLineContent[1]);
						//Initialize the multidimensional array of spreadsheet cells with the given size
						spreadSheet.cells = new Cell[Integer.parseInt(firstLineContent[1])][Integer.parseInt(firstLineContent[0])];
					}
				}
				else{
					if (row == spreadSheet.rows){
						System.out.println("The given number of cell values do not match with the given spreadsheet size!");
						System.exit(1);
					}
					//Read the line and store it as a spreadsheet cell expression
					String expression = stdIn.nextLine();
					if ((expression == null) || (expression.isEmpty())){
						System.out.println("Expression at row " + (row + 1) + " and column " + (col + 1) + " is either empty or null.");
						System.exit(1);
					}
					spreadSheet.cells[row][col] = spreadSheet.new Cell(expression, row, col);
					col++;
					if (col == spreadSheet.columns){
						col = 0;
						row++;
					}
				}
				linesRead++;
			}
			for (int i = 0; i <= spreadSheet.cells.length - 1; i++){
				for (int j = 0; j <= spreadSheet.cells[i].length - 1; j++ ){
					if ((spreadSheet.cells[i][j] == null) || (spreadSheet.cells[i][j].cellExpression.isEmpty())){
						System.out.println("The given number of cell values do not match with the given spreadsheet size!");
						System.exit(1);
					}
				}
			}
		}
		catch(NumberFormatException nex){
			System.out.println("The given row or column argument is either null or not a number	");
			stdIn.close();
			System.exit(1);
		}
		catch(Exception ex){
			System.out.println("Oops!! Something went wrong!!");
			stdIn.close();
			System.exit(1);
		}
		finally{
			stdIn.close();
		}
	}

	/**
	 * Use recursion to calculate the cell values.
	 * @param cell
	 * @param cellsToEvaluate
	 * @return
	 */
	private Double calculateCellValue(final Cell cell,final Set<Cell> cellsToEvaluate) {
		//When the cell as referred by a cell reference such as A2 has already been interpreted
		if(cell.valueInterpreted){
			return cell.interpretedValue;
		}
		//When the cell is not interpreted yet and the cell has not been considered yet for interpretation in the current pass
		else if(!cell.valueInterpreted && !cellsToEvaluate.contains(cell))
		{
			cellsToEvaluate.add(cell);
			String[] expressions = cell.cellExpression.split(" ");
			//Use a stack for simple calculator logic
			Stack<Double> operands = new Stack<Double>();
			try{
				for(String expression: expressions) {
					if (expression.equals("+"))
						operands.push(operands.pop() + operands.pop());
					else if (expression.equals("++"))
						operands.push(operands.pop() + 1);
					else if (expression.equals("--"))
						operands.push(operands.pop() - 1);
					else if (expression.equals("*"))
						operands.push(operands.pop() * operands.pop());
					else if (expression.equals("/")){
						double divisor = operands.pop();
						double dividend = operands.pop();
						operands.push( dividend / divisor);
					}
					else if (expression.equals("-"))
					{
						double subtractor = operands.pop();
						double subtractee = operands.pop();
						operands.push( subtractee - subtractor);
					}
					else {
						try{
							//if the current expression is not operator then it can be an operand
							operands.push(Double.parseDouble(expression));
						}
						catch(NumberFormatException nex){
							try{
								//Set the expression to be evaluated. to be used when a cyclic dependency is detected.
								cell.setCellRefExpr(expression);
								// or a reference to another cell
								int row = Character.getNumericValue(expression.charAt(0)) - 10; // integer value of 'A' is 10
								int col = Integer.parseInt(expression.substring(1,expression.length()))-1;
								operands.push(calculateCellValue(cells[row][col],cellsToEvaluate));
							}
							catch(NumberFormatException innerNex){
								System.out.println("Wrong Cell Reference :- " + expression);
								System.exit(1);
							}
							catch(ArrayIndexOutOfBoundsException aex){
								System.out.println("Wrong Cell Reference :- " + expression);
								System.exit(1);
							}
							catch(StringIndexOutOfBoundsException siex){
								System.out.println("Extra space detected :- " + cell.cellExpression);
								System.exit(1);
							}
						}
					}
				}
			}
			catch(EmptyStackException ex){
				System.out.println("Mismatch of operands and operator in expression " + cell.cellExpression + " at row " +
						(cell.rowPosition + 1) + " and column " + (cell.colPosition + 1));
				System.exit(1);
			}
			if (operands.size() > 1){
				System.out.println("Mismatch of operands and operator in expression " + cell.cellExpression + " at row " +
						(cell.rowPosition + 1) + " and column " + (cell.colPosition + 1));
				System.exit(1);
			}
			//Store the interpreted value and mark as interpreted
			cell.interpretedValue = operands.pop();
			cell.valueInterpreted = true;

		}
		// If in the current pass the cell has already been considered then mark it as cyclical as interpreting this cell
		// again will yield the same result
		else {
			Cell cyclicalCell = (Cell)(cellsToEvaluate.toArray()[cellsToEvaluate.size() - 1]);
			System.out.println("Cyclical dependency detected while calculating expression " + cyclicalCell.getCellRefExpr() + " in the cell expresiion "
					+ cyclicalCell.cellExpression + " at row " + (cyclicalCell.rowPosition + 1) + " and column " + (cyclicalCell.colPosition + 1));
			for (Cell loopedCell: cellsToEvaluate){
				if (loopedCell == cell){
					System.out.println("Call to calculate this cell was already made at row " +
							(cell.rowPosition + 1) + " and column " + (cell.colPosition + 1) + " having expression " + cell.cellExpression);
					break;
				}
			}
			System.exit(1);
		}

		return cell.interpretedValue;
	}

	/**
	 * Print the output in the desired format
	 * @param spreadSheet
	 */
	private static void printCalculationOutput(final Spreadsheet spreadSheet){
		System.out.println(spreadSheet.columns+" "+spreadSheet.rows);
		for (Cell[] cells : spreadSheet.cells){
			for (Cell cell : cells) {
				System.out.printf("%.5f%n", cell.interpretedValue);
			}
		}
	}

}

