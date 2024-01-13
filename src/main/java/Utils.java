import java.util.ArrayList;
import java.util.Scanner;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;




public class Utils{


    public static String[] colors = {
            "\033[38;5;203m",
            "\033[38;5;204m",
            "\033[38;5;205m",
            "\033[38;5;206m",
            "\033[38;5;207m",
            "\033[38;5;213m",
            "\033[38;5;212m",
            "\033[38;5;211m"
    };

    public static String reset = "\033[0m";

    public static String[] ansiiTxt = {
            "             _ _ _   _           _       _            ",
            "            | | | | | |         | |     | |           ",
            " _ __  _   _| | | | | |_ __   __| | __ _| |_ ___ _ __ ",
            "| '_ \\| | | | | | | | | '_ \\ / _` |/ _` | __/ _ \\ '__|",
            "| | | | |_| | | | |_| | |_) | (_| | (_| | ||  __/ |   ",
            "|_| |_|\\__,_|_|_|\\___/| .__/ \\__,_|\\__,_|\\__\\___|_|   ",
            "                      | |                             ",
            "                      |_|                             "

    };


    //Knuth Morris Pratt pattern searching algorithm

    public static ArrayList<String> kmpSearch(NullByte[] pattern , byte[] data) {

        ArrayList<String> foundAddresses = new ArrayList<String>();

        int m = pattern.length;
        int n = data.length;

        int[] longestPS = computeLPS(pattern, m);

        int i = 0;
        int j = 0;
        while(i < n) {
            if(pattern[j].getValue() == data[i] || pattern[j].isWildcard()) {
                j++;
                i++;
            }
            if(j == m) {
                foundAddresses.add("0x" + Integer.toHexString((i - j)).toUpperCase());
                j = longestPS[j - 1];
            }
            else if(i < n && pattern[j].getValue() != data[i]
                    && !pattern[j].isWildcard()) {
                if(j != 0)
                    j = longestPS[j - 1];
                else
                    i = i + 1;
            }
        }
        return foundAddresses;
    }

    private static int[] computeLPS(NullByte[] pattern, int m) {
        int[] longestPS = new int[m];
        int length = 0;
        int i = 1;

        longestPS[0] = 0;

        while(i < m) {
            if(pattern[i].getValue() == pattern[length].getValue()
                    || pattern[i].isWildcard()) {
                length++;
                longestPS[i] = length;
                i++;
            }
            else {
                if (length != 0) {
                    length = longestPS[length - 1];
                }
                else {
                    longestPS[i] = length;
                    i++;
                }
            }
        }
        return longestPS;
    }



    public static byte[] getData(Path path){

        try{
            byte[] bytes = Files.readAllBytes(path);
            return bytes;
        }
        catch (IOException e){
            e.printStackTrace();
        }

        return null;
    }

    public static boolean hasTwoComma(String opStr){
        int commaCount = 0;
        for (int i = 0; i<opStr.length(); i++){
            if (opStr.charAt(i) == ',')
                commaCount++;
        }
        return commaCount == 2;
    }

    public static void printPattern(NullByte[] pattern){
        System.out.print(getPatternString(pattern));
        System.out.println();
    }

    public static String getPatternString(NullByte[] pattern) {
        String buf = "";
        for(int i = 0; i < pattern.length; i++){
            if(pattern[i].isWildcard())
                buf = buf + "?? ";
            else
                buf = buf + (String.format("%02X", pattern[i].getValue()) + " ");
        }
        return buf;

    }

    public static int getINTMAX(){
        int val = 0;
        val =~ val;
        val >>>= 1;
        return val;
    }

    public static int[] extractOffsets(Path filePath) {

        int count = 0;
        ArrayList<Integer> offsetList = new ArrayList<>();
        try {

            try (BufferedReader br = new BufferedReader(new FileReader(filePath.toString()))) {
                String line;
                while ((line = br.readLine()) != null) {
                    if (!line.trim().startsWith("#")) {
                        count++;
                        offsetList.add(Integer.decode(line.trim()));
                    }
                }
            }


            int[] offsets = new int[count];


            for (int i = 0; i < count; i++) {
                offsets[i] = offsetList.get(i);
            }

            return offsets;

        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("[NullLog] WARNING no offsets found in offsets txt");
            return new int[0];
        }


    }

    public static Path getValidPath(Scanner inpGetter, String prompt) {
        Path filePath;

        do {
            System.out.print(prompt);
            String path = inpGetter.nextLine();
            filePath = Paths.get(path);
            if(!Files.exists(filePath))
                System.out.println("[NullLog] Path doesnt exist");
        } while(!Files.exists(filePath));

        return filePath;
    }

    public static String getValidInput(Scanner inpGetter, String prompt, String[] possibleInputs) {
        boolean gotValid = false;
        String input;
        do {
            System.out.print(prompt);
            input = inpGetter.nextLine();
            for(int i = 0; i < possibleInputs.length; i++) {
                if(possibleInputs[i].equals(input))
                    gotValid = true;
            }
            if(!gotValid)
                System.out.println("[NullLog] Invalid input");
        } while(!gotValid);

        return input;
    }

    public static int getValidInput(Scanner inpGetter, String prompt, int out) {
        boolean gotValid = false;
        do {
            try {
                out = Integer.parseInt(inpGetter.nextLine());
                gotValid = true;
            } catch (NumberFormatException e) {
                System.out.println("[NullLog] Invalid input");
            }
        } while(!gotValid);
        return out;
    }
}
