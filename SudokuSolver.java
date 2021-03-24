package SudokuSolverLib;

import javax.swing.*;
import java.awt.*;
import java.awt.Color;
import java.awt.event.*;
import javax.swing.border.Border;
import java.util.List;
import java.util.*;

public class SudokuSolver extends SwingWorker<Void, ArrayList<Integer>>{
	public static void main(String[] args){
		try{
			createGUI();
		}catch(Exception e){e.printStackTrace();}
	}
	
	public static void createGUI(){
		JFrame frame = new JFrame("Sudoku Solver");
		frame.getContentPane().setBackground(Color.BLACK);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		addComponentsToPane(frame.getContentPane());
		addFunctions();
		frame.pack();
        frame.setVisible(true);
	}
	
	final static boolean shouldFill = true;
    final static boolean shouldWeightX = true;
    final static boolean RIGHT_TO_LEFT = false;	
	static JButton solve, clear, load;
	static JLabel tf, randomDesc;
	public static JTextField[][] textGrid = new JTextField[9][9];
	public static int[][] intGrid = new int[9][9];
	public static SudokuSolver sw1;
		
	public static void addComponentsToPane(Container pane) {
        if (RIGHT_TO_LEFT) {
            pane.setComponentOrientation(ComponentOrientation.RIGHT_TO_LEFT);
        }
 
		pane.setLayout(new GridBagLayout());
		GridBagConstraints c = new GridBagConstraints();
		if (shouldFill) {
			c.fill = GridBagConstraints.BOTH;
		}
		
		for(int y = 1; y <10; y++){
			for(int x = 0; x < 9; x++){
				JTextField cell = new JTextField();
				textGrid[x][y-1] = cell;
				cell.setHorizontalAlignment(JTextField.CENTER);
				Border border = BorderFactory.createLineBorder(Color.BLACK, 1);
				cell.setBorder(border);
				if((x+1)%3 == 0){ 
					c.insets = new Insets(0,0,0,1);
					if(y%3 == 0){ c.insets = new Insets(0,0,1,1);}
				}else{
					if(y%3 == 0){ c.insets = new Insets(0,0,1,0);
					}else{
						c.insets = new Insets(0,0,0,0);
					}
				}
				
				c.weightx = 1.0;
				c.weighty = 1.0;
				c.ipady = 25;
				c.ipadx = 50;
				c.gridx = x;
				c.gridy = y;
				pane.add(cell, c);
			}
		}
		
		solve = new JButton("Solve");
		c.ipady = 15;      
		c.ipadx = 50;
		c.weightx = 0.5;
		c.gridwidth = 3;
		c.gridx = 0;
		c.gridy = 10;
		pane.add(solve, c);
		
		load = new JButton("Load");
		c.ipady = 15;      
		c.ipadx = 50;
		c.weightx = 0.5;
		c.gridwidth = 3;
		c.gridx = 3;
		c.gridy = 10;
		pane.add(load, c);
	
		clear = new JButton("Clear");
		c.ipady = 15;      
		c.ipadx = 50;
		c.weightx = 0.5;
		c.gridwidth = 3;
		c.gridx = 6;
		c.gridy = 10;
		pane.add(clear, c);
		
		
 	
	}
	
	public static void addFunctions(){
		solve.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				if (sw1 == null || sw1.isDone() || sw1.isCancelled()) {
					sw1 = new SudokuSolver();
					sw1.execute(); 
				}					
			}  
		});
			
		clear.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){
				if(sw1 != null){		
					sw1.cancel(true);
				}
				cleanup();
			}  
		}); 
		
		load.addActionListener(new ActionListener(){  
			public void actionPerformed(ActionEvent e){  
				load();
			}  
		}); 
	}
	
	@Override
	protected Void doInBackground() throws Exception{ 
		solve(); 
		return null;
	}
	
	@Override
	protected void process(List<ArrayList<Integer>> args){
		for(ArrayList<Integer> arg : args){
			if(arg.get(2)==0){
				textGrid[arg.get(1)][arg.get(0)].setText("");
			}else{
				textGrid[arg.get(1)][arg.get(0)].setText("" + arg.get(2));
			}
			switch(arg.get(3)){
				case 1:
					textGrid[arg.get(1)][arg.get(0)].setBackground(Color.GREEN);
					break;
				case 2:
					textGrid[arg.get(1)][arg.get(0)].setBackground(Color.RED);
					break;
				case 3:
					textGrid[arg.get(1)][arg.get(0)].setBackground(Color.WHITE);
					break;
			}
		}
		
	}
	
	public void myPublish(ArrayList<Integer> args){
		super.publish(args);
	}
	
	@Override
	protected void done(){
		System.out.println("Solved or Cancelled");	
	}
	
	public static void solve(){
		
		for(int i = 0; i < textGrid.length; i++){
			for(int j = 0; j < textGrid[i].length; j++){
				if(textGrid[i][j].getText().equals("")){
					intGrid[i][j] = 0;
				}else{
					intGrid[i][j] = Integer.parseInt(textGrid[i][j].getText());
				}
			}
		}
		System.out.println("Solving");
		solve(intGrid);
	}

	public static boolean solve(int[][] grid){
		int[] ra = unassigned(grid);
        if (ra[0] == -1) {
            return true;
        }
			
		int col = ra[0];
        int row = ra[1];
		
		for (int num = 1; num <= 9; num++) {
			if(sw1.isCancelled()){break;}
			try{
				Thread.sleep(5);
			}catch(Exception e){}
			ArrayList<Integer> working = new ArrayList<Integer>(Arrays.asList(row, col, num, 2));
			sw1.myPublish(working);
			if (isValid(grid, row, col, num)) {
				grid[col][row] = num;
				ArrayList<Integer> calc = new ArrayList<Integer>(Arrays.asList(row, col, num, 1));
				sw1.myPublish(calc);
				boolean check = solve(grid);
				if (check == true) {
                    return true;
                }
				
                grid[col][row] = 0;
				ArrayList<Integer> wrong = new ArrayList<Integer>(Arrays.asList(row, col, num, 2));
				sw1.myPublish(wrong);
			}
        }
        return false;
		
    }
 
    public static int[] unassigned(int[][] arr) {
        int[] ra = new int[2];
        ra[0] = -1;
        ra[1] = -1;
        for (int row = 0; row < arr.length; row++) {
            for (int col = 0; col < arr.length; col++) {
                if (arr[col][row] == 0) {
                    ra[0] = col;
                    ra[1] = row;
                    return ra;
                }
            }
        }
        return ra;
    }
	
	public static boolean checkRow(int[][] grid, int row, int num) {
        for (int i = 0; i < grid.length; i++) {
            if (grid[i][row] == num) {
                return true;
            }
        }
        return false;
    }
 
    public static boolean checkCol(int[][] grid, int col, int num) {
        for (int i = 0; i < grid.length; i++) {
            if (grid[col][i] == num) {
                return true;
            }
        }
        return false;
    }
 
    public static boolean checkBox(int[][] grid, int row1Start, int col1Start, int num) {
        for (int row = 0; row < 3; row++)
            for (int col = 0; col < 3; col++)
                if (grid[col + col1Start][row + row1Start] == num) {
                    return true;
                }
        return false;
 
    }
 
    public static boolean isValid(int[][] grid, int row, int col, int num) {
		return (!checkCol(grid, col, num) && !checkRow(grid, row, num) && !checkBox(grid, row - row % 3, col - col % 3, num));
    }
	
	public static void printGrid(JTextField[][] grid){
		
		for(int i = 0; i < grid.length; i++){
			for(int j = 0; j < grid[i].length; j++){
				if(grid[i][j].getText().equals("")){
					System.out.print("0" + " ");
				}else{
					System.out.print(grid[j][i].getText() + " ");///////////
				}
			}
			System.out.println();
		}
	}
	
	public static void printGrid(int[][] grid){
		
		for(int i = 0; i < grid.length; i++){
			for(int j = 0; j < grid.length; j++){
			System.out.print(grid[j][i] + " ");
			}
		System.out.println();
		}
	}
	
	public static void cleanup(){
		System.out.println("Clearing");
		try{
			Thread.sleep(500);
		}catch(Exception e){e.printStackTrace();}
		for(int i = 0; i < textGrid.length; i++){
			for(int j = 0; j < textGrid[i].length; j++){
				ArrayList<Integer> args = new ArrayList<Integer>(Arrays.asList(i, j, 0, 3));
				if(sw1 != null){
					sw1.myPublish(args);
				} else {
					textGrid[i][j].setText("");
				}
			}
		}
		for(int i = 0; i < intGrid.length; i++){
			for(int j = 0; j < intGrid[i].length; j++){
				intGrid[j][i] = 0;
			}
		}
	}
	
	public static void load(){
		int[][] sampleArr = {{5,3,4,6,7,8,9,0,0},
							{6,0,0,1,9,5,0,0,0},
							{0,9,8,0,0,0,0,6,0},
							{8,0,0,0,6,0,0,0,3},
							{4,0,0,8,0,3,0,0,1},
							{7,0,0,0,2,0,0,0,6},
							{0,6,0,0,0,0,2,8,0},
							{0,0,0,4,1,9,0,0,5},
							{0,0,0,0,8,0,0,7,9}};
		for(int i = 0; i < sampleArr.length; i++){
			for(int j = 0; j < sampleArr.length; j++){
				if(sampleArr[j][i] != 0){
					textGrid[i][j].setText("" + sampleArr[j][i]);
				}
			}
		}
	}
}