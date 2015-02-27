// Ricardo Turcios
// CS 311
// Dr.Sang
// Project 2: Dynamic FSA
// Due: February 24, 2015

// INSTRUCTIONS TO COMPILE AND RUN PROPERLY:
//          MUST HAVE Proj2_Input1.txt and Proj2_Input2.txt in working directory
//          Printing data content(arrays) neat in netbeans, but not in cmd.
//          I decided to leave it like this for the sake of printing less pages.


package dynamicfsa;

import java.io.FileReader;
import java.util.Scanner;

public class DynamicFSA {
    // Instantiating data structure
    static int  [] swtch  = new int[54];
    static char [] symbol = new char [200];
    static int  [] next   = new int  [200];
    // nextAvailable is pointer to nextAvailable poistion on parallel array
    static int nextAvailable = 0;
    // boolean for printing processed words
    static boolean print;
    
    public static void main(String[] args) throws Exception{
        // Initialize switch to -1 value 
        for (int i=0; i<swtch.length; i++)
            swtch[i] = -1;
        // set print to false and process initial file with reserved words.
        print = false;
        processFile("Proj2_Input1.txt",'*');
        // set print to true and process second file 
        print = true;
        processFile("Proj2_Input2.txt",'@');
        // print arrays
        printArrays();
    }
    // Process file method 
    public static void processFile(String file,char delim) throws Exception{
        // Instantiate fileReader and scanner 
        FileReader f = new FileReader(file);
        Scanner scan = new Scanner(f);
        // outer loop will loop through line by line of the file
        while (scan.hasNext()){
            Scanner scanLine = new Scanner(scan.nextLine());
            // Nested loop will loop string by string, seperated by spaces.
            while (scanLine.hasNext()){
                // process the current string
                processString(scanLine.next(),delim);
            }
            // if print is enabled, print line after line processed
            if (print) System.out.println();
        }
        // Close file
        f.close();
    }
    
    // Process string method
    public static void processString(String s, char delim){
        // evaulating first character
        char first = s.charAt(0);
        int sLoc = getSwitchPos(first);
        // If not a valid first char (not in switch array)
        if (sLoc == -1){
            // If size of string is greater than 1, skip first character.
            if (s.length() > 1)
                processString(s.substring(1),delim);
            // If not, ignore the string, and return.
        }
        // If first time encountering the valid first characeter
        else if (swtch[sLoc] == -1){
            // set the switch value for char to nextAvailable position
            swtch[sLoc] = nextAvailable;
            // If identifier legnth is 1, add delimeter to nextAvailable pos
            if (s.length() == 1){
                if (nextAvailable == symbol.length) incrementDataSize();
                symbol[nextAvailable++] = delim;
                // Print if enabled 
                if (print) System.out.print(s + "?" + " ");
            }
            // If length of current identifier > 1, call create method 
            else
                create(s,1,delim);
        }
        // Else, switch value defined for first character of identifier
        else { 
            // set pointer to content of corresponding switch array
            int ptr = swtch[sLoc];
            // current will hold current char being processed
            char current;
            // Loop through character by character starting at 2nd char (pos 1)
            for (int p=1; p<s.length(); p++){
                current = s.charAt(p);
                // Check if symbol follows identifier rules
                if (!validSymbol(current)){
                    // if not a valid identifier symbol
                    // Check for closure of identifier up to invalid symbol
                    boolean foundMatch = false;
                    int prevPtr;
                    // do while to check for closure
                    do{
                        // prevPtr used for rolling back pointer
                        prevPtr = ptr;
                        // if closure found
                        if (symbol[ptr] == '*' || symbol[ptr] == '@'){
                            // print identifier, process remaining portion of string
                            foundMatch = true;
                            if (print) System.out.print(s.substring(0, p) + "" + symbol[ptr] + " ");
                            processString(s.substring(p),delim);
                            return;
                        }
                        // inc. pointer to next value, loop until next undefined
                        ptr = next[ptr];
                    } while (ptr != 0);
                    // If no match found, add closure
                    if (!foundMatch){
                        // Check for data increment if necessary
                        if (nextAvailable == symbol.length) incrementDataSize();
                        next[prevPtr] = nextAvailable; 
                        symbol[nextAvailable++] = delim;
                        if (print) System.out.print(s.substring(0, p) + "? ");
                        processString(s.substring(p), delim);
                        return;
                    }
                }
                // else matched current symbol, so increment pointer
                else if (current == symbol[ptr]){
                    ptr++;        
                }
                // else (symbol is valid, but no match) and next field is defined
                else if (next[ptr] != 0){
                    //roll back p to re-process symbol 
                    p--;
                    // move pointer to next position
                    ptr = next[ptr];
                }
                // else no match and next is not defined
                else{
                    // define next field at pointer location
                    next[ptr] = nextAvailable;
                    // crete remaining portion of word
                    create(s,p,delim);
                    return;
                }
            }
            // If exited from loop, identifier symbols all consumed,
            // so check for closure, and add closure if needed.
            boolean exit = false;
            boolean found = false;
            while (!exit){
                // if closure found
                if (symbol[ptr] == '*' || symbol[ptr] == '@'){
                    exit = true; found = true;
                }
                // while next is defined, move pointer to next
                else if (next[ptr] != 0){
                    ptr = next[ptr];
                }
                // next undefined, and no closure found exit
                else {
                    exit = true;
                }
            }
            if (found){
                // If closure found, print
                if (print) System.out.print(s + "" + symbol[ptr] + " ");
            }
            else {
                // no closure found, so add closure
                if (nextAvailable == symbol.length) incrementDataSize();
                next[ptr] = nextAvailable; 
                symbol[nextAvailable++] = delim;
                System.out.print(s + "? ");
            }
        }
    }
    
    // Method for created/adding a identifier to data structure
    public static void create(String s,int ptr, char delim){
        // start adding identifier from specified string pointer location
        for (int i=ptr; i<s.length(); i++){
            char sym = s.charAt(i);
            // check for current symbol validity
            if (validSymbol(sym)){
                // if valid, add to symbol array, and increment nextAvailable
                if (nextAvailable == symbol.length) incrementDataSize();
                symbol[nextAvailable++] = sym;
            }
            // invalid symbole
            else{
                // else add closure, and process remaining portion of string
                if (nextAvailable == symbol.length) incrementDataSize();
                symbol[nextAvailable++] = delim;
                if (print) System.out.print(s.substring(0, i) + "? ");
                processString(s.substring(i),delim);
                return;
            }
        }
        // If exited loop, all symbols properly read, so add closure
        if (nextAvailable == symbol.length) incrementDataSize();
        symbol[nextAvailable++] = delim;
        if (print) System.out.print(s + "? ");
    }
        
    // Checks for validity of symbols
    public static boolean validSymbol(char s){
        if (s<'A' || s>'Z')
            if (s<'a' || s > 'z')
                if (s<'0' || s>'9')
                    if (s != '_')
                        if (s != '$')
                            return false;
        return true;
    }
    
    // gets value of the cooresponding 1st symbol of a string being read in switch
    public static int getSwitchPos(char c){
        // return  character value minus 'A' ASCII value if upper case
        if (c>='A' && c<='Z')
            return c-'A';
        // Else if lower case, return char - 'a' ASCII value + 26.
        else if (c>='a' && c<= 'z')
            return c-'a'+26;
        // if underscore
        else if (c == '_')
            return 52;
        // if $ sign
        else if (c == '$')
            return 53;
        // Not a valid starting character
        return -1;
    }
    
    // Doubles parallel arrays (symbol and next)
    public static void incrementDataSize(){
        char[] tempS = new char[nextAvailable*2];
        int[] tempN = new int[nextAvailable*2];
        // Copy over existing info
        for (int i=0; i<nextAvailable; i++){
            tempS[i] = symbol[i];
            tempN[i] = next[i];
        }
        // replace
        symbol = tempS;
        next = tempN;
    }
    
    // Prints data structure into readable format
    public static void printArrays(){
        // Print A-Z
        System.out.printf("%8s", "");
        for (int i=0; i<26; i++){
           char current = (char) ('A' + i);
           System.out.printf("%5s",current); 
        }
        System.out.print("\nswitch: ");
        for (int i=0; i<26; i++){
            System.out.printf("%5d", swtch[i]);
        }
        System.out.println();
        
        // Print a-z,
        System.out.printf("\n%8s", "");
        for (int i=0; i<26; i++){
           char current = (char) ('a' + i);
           System.out.printf("%5s",current); 
        }
        System.out.print("\nswitch: ");
        for (int i=0; i<26; i++){
            System.out.printf("%5d", swtch[i+26]);
        }
        System.out.println();
        
       // Printing _ and $
        System.out.printf("\n%13s","_"); 
        System.out.printf("%5s","$"); 
        System.out.print("\nswitch: ");
        System.out.printf("%5d", swtch[52]);
        System.out.printf("%5d\n", swtch[53]);
        
 
        // Loop through all values in symbol and next
        int i=0;
        while (i<nextAvailable){
            // if i is a factor of 26 (Printing 26 per row), and not 0
            if (i>0 && i%26 == 0){
                // offset for printint corresponging symbol and next lines
                int offset = (i-26);
                // print symbol header:
                System.out.print("\nsymbol: ");
                // Loop through symbol array and print
                for (int j=0; j<26; j++){
                    System.out.printf("%5s", symbol[j+offset]);
                }
                // Do the same for next on new line
                System.out.println();
                System.out.print("next:   ");
                for (int j=0; j<26; j++){
                    // determine if next field is valid, print if so.
                    if (next[j+offset] != 0) 
                        System.out.printf("%5d", next[j+offset]);
                    else 
                        System.out.printf("%5s", "");
                }
                System.out.println();
            }
            // add padding for new line
            if (i%26 == 0)
                System.out.printf("\n%8s", "");
            // print i
            System.out.printf("%5d", i++);
        }
        // print reamining left over symbol values not printed
        System.out.print("\nsymbol: ");
        for (int j=i-(i%26); j<nextAvailable; j++){
            System.out.printf("%5s", symbol[j]);
        }
        // do the same for next values
        System.out.println();
        System.out.print("next:   ");
        for (int j=i-(i%26); j<nextAvailable; j++){
            if (next[j] != 0)
                System.out.printf("%5d", next[j]);
            else 
                System.out.printf("%5s", "");
        }
        System.out.println();
        
    }
}