package nullBit.offsetupdater;


import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

//user
import nullBit.offsetupdater.Utils;
import nullBit.offsetupdater.NullByte;


public class Main{
    
    private static Scanner inpGetter = new Scanner(System.in);
    private static NullPattern nullMain;
    private static int[] offsets;
    
    public static void main(String[] argv) {
        
        System.out.print("\033[H\033[2J");  
        System.out.flush();
        
        init();
        
        
        boolean validInput = false;
        while(!validInput) {
	        System.out.print("[1]Arm or [2]Arm64 (currently only for arm64) [1,2]: ");
	        String arch = inpGetter.nextLine();
	        
	        arch = arch.trim();
	        
	        if(arch.equals("1")) {
	        	nullMain = new NullPattern("arm");
	        	validInput = true;
	        }
	        else if(arch.equals("2")) {
	        	nullMain = new NullPattern("arm64");
	        	validInput = true;
	        }
	        else 
	        	System.out.println("[NullLog] Invalid input");
	    }
        
        	
    	if(!nullMain.testCapstone()) {
        	System.out.println("[NullLog] Capstone not properly working quitting...");
        	System.exit(1);
        }
    	else 
    		System.out.println("[NullLog] Capstone working");
    	
    	
    	String workingDir = System.getProperty("user.dir");
        Path offFilePath = Paths.get(workingDir, "offsets.txt");
        
        	
        	
    	if(!Files.exists(offFilePath)) {
    		System.out.println("[NullLog] no offsets.txt found! ");
    		offFilePath = Utils.getValidPath(inpGetter, "Enter path to txt file with offsets: ");
    	}
    	
    	offsets = Utils.extractOffsets(offFilePath);
    	
    	Path oldLibFile = Utils.getValidPath(inpGetter, "Enter old path: ");
    	Path newLibFile = Utils.getValidPath(inpGetter, "Enter new lib path: ");
    	
    	
    	byte[] oldData = Utils.getData(oldLibFile);
    	byte[] newData = Utils.getData(newLibFile);
    	
    	if(oldData.length >= Utils.getINTMAX() || newData.length >= Utils.getINTMAX())
    		System.out.println("[NullLog] WARNING data bigger than this tool can handle (will be updated soon) might work faulty or not at all!");
    	int maxOffsets = 1;
    	try {
    		System.out.print("How many offsets should max be found?: ");
    		maxOffsets = inpGetter.nextInt();
		inpGetter.nextLine()
    	} catch (InputMismatchException e) {
    		maxOffsets = 1;
    		inpGetter.nextLine();
    	}
    	System.out.print("Want patterns to be written to out.txt? [1]yes [defualt]no: ");
    	String writePatternStr = inpGetter.nextLine();
    	int writePattern;
    	if(!writePatternStr.trim().equals("1"))
    		writePattern = 0;
    	else 
    		writePattern = 1;
    	
    	
    	String newPath = workingDir + "/out.txt";
    	
    	try {
    	    new FileWriter(newPath, false).close(); 
    	} catch (IOException e) {
    	    e.printStackTrace();
    	}
    	
        for(int i = 0; i < offsets.length; i++) {
        	int count = 0;
        	boolean underMax = false;
        	while(!underMax) {
        		NullByte[] pattern = nullMain.getPattern(oldData, offsets[i], count);
        		
        		if(pattern == null) {
        			System.out.println("NO OFFSET FOUND FOR: " + "0x" + Integer.toHexString(offsets[i]).toUpperCase());
        			break;
        		}
        		
        		ArrayList<String> newOffsets = Utils.kmpSearch(pattern, newData);
        		
        			
        		if(newOffsets.size() > maxOffsets) 
        			count++;
        		
        		else if(newOffsets.size() == 0)
        			count--;
        		else {
        			underMax = true;
        			
        			System.out.print("old: " + "0x" + Integer.toHexString(offsets[i]).toUpperCase() + " new: ");
        			
        			for(int j = 0; j < newOffsets.size(); j++)
        				System.out.print(newOffsets.get(j) + " ");
        			System.out.println();
        			
        			try {
        	            
        	            BufferedWriter writer = new BufferedWriter(new FileWriter(newPath, true));
        	            writer.write("Old: " + "0x" + Integer.toHexString(offsets[i]).toUpperCase() + " New: ");
        	            for(int j = 0; j < newOffsets.size(); j++) 
	    	            	writer.write(newOffsets.get(j) + " ");
        	            
        	            
        	            if(writePattern == 1) {
        	            	writer.newLine();
        	            	writer.write("Pattern: " + Utils.getPatternString(pattern));
        	            }
        	            writer.newLine();
        	            writer.write("-------------------------------");
        	            writer.newLine();
        	            writer.close();
        	            System.out.println("[NullLog] Finished successfully wrote offsets into out.txt");

        	        } catch (IOException e) {
        	        	System.out.println("[NullLog] WARNING couldnt write to out.txt");
        	            e.printStackTrace();
        	        }
        		}
        	}
        	
        }
    	
    	
    }
    
    
    private static void init() {
    	for(int i = 0; i < Utils.ansiiTxt.length; i++)
            System.out.println(Utils.colors[i] + Utils.ansiiTxt[i]);
        System.out.println(Utils.reset);
    }
}
