import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.Scanner;

public class Main{

    private static Scanner inpGetter = new Scanner(System.in);
    private static NullPattern nullMain;
    private static int[] offsets;

    public static void main(String[] argv){

        System.out.print("\033[H\033[2J");
        System.out.flush();

        init();

        String arch = Utils.getValidInput(inpGetter, "[1]Arm or [2]Arm64 (currently only for arm64) [1,2]: ", new String[]{"1","2"});
        if(arch.equals("1"))
            nullMain = new NullPattern("arm");
        else if(arch.equals("2"))
            nullMain = new NullPattern("arm64");


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
        maxOffsets = Utils.getValidInput(inpGetter,"How many offsets should max be found?: ", 0);


        String writePatternStr = Utils.getValidInput(inpGetter, "Want patterns to be written to out.txt? [1]yes [defualt]no: ", new String[]{"1", "0"});
        boolean writePattern;
        if(writePatternStr.equals("1"))
            writePattern = true;
        else
            writePattern = false;


        String newPath = workingDir + "/out.txt";

        try {
            new FileWriter(newPath, false).close();
        } catch (IOException e) {
            e.printStackTrace();
        }

        for(int i = 0; i < offsets.length; i++) {
            int count = 0;
            boolean underMax = false;
            int latestHigher = -1;
            boolean continueRegardless = false;
            int backForth = 0;
            while(!underMax) {
                NullByte[] pattern = nullMain.getPattern(oldData, offsets[i], count);

                if(pattern == null) {
                    System.out.println("NO OFFSET FOUND FOR: " + "0x" + Integer.toHexString(offsets[i]).toUpperCase());
                    break;
                }

                ArrayList<String> newOffsets = Utils.kmpSearch(pattern, newData);


                if(newOffsets.size() > maxOffsets && !continueRegardless) {
                    count++;
                    if(latestHigher == 0)
                        backForth++;

                    latestHigher = 1;

                }
                else if(newOffsets.isEmpty() && !continueRegardless) {
                    count--;
                    if(latestHigher == 1)
                        backForth++;

                    latestHigher = 0;
                    if(backForth > 3)
                        continueRegardless = true;
                }
                else if(continueRegardless || newOffsets.size() == maxOffsets){
                    underMax = true;
                    if(continueRegardless)
                        System.out.println("Couldnt stay under ceiling here are possible offsets: ");

                    System.out.print("old: " + "0x" + Integer.toHexString(offsets[i]).toUpperCase() + " new: ");

                    for(int j = 0; j < newOffsets.size(); j++)
                        System.out.print(newOffsets.get(j) + " ");
                    System.out.println();

                    try {
                        BufferedWriter writer = new BufferedWriter(new FileWriter(newPath, true));
                        writer.write("Old: " + "0x" + Integer.toHexString(offsets[i]).toUpperCase() + " New: ");
                        for(int j = 0; j < newOffsets.size(); j++)
                            writer.write(newOffsets.get(j) + " ");
                        if(continueRegardless)
                            writer.write(" Couldnt stay under maximum offset ceiling");

                        if(writePattern) {
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
