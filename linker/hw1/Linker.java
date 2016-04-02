package hw1;

/**
 * Created by subhankarghosh on 2/17/16.
 */

import java.io.*;
import java.util.ArrayList;
import java.util.StringTokenizer;

import java.util.*;
import java.io.*;

public class Linker {

  static ArrayList<Symbol> symbolTable = new ArrayList<Symbol>();
  static ArrayList<Module> moduleList = new ArrayList<Module>();
  static ArrayList<String> memoryMap = new ArrayList<String>();

  public static void main (String [] args) {

    BufferedReader br = new BufferedReader(new InputStreamReader(System.in));
    File inputFile;

    // Figure out where to process input from and set up bufferedReader
    // accordingly
    if (args.length != 0) {
      inputFile = new File(args[0]);
      try {
        br = new BufferedReader(new FileReader(inputFile));
      } catch (IOException e) {
        e.printStackTrace();
        System.exit(0);
      }
    }


    // Determine the base address for each module and the absolute address for
    // each external symbol while storing the latter in the symbol table
    passOne(br, symbolTable, moduleList);

    // Using the base addresses and the symbol table computed in pass one,
    // generate output by relocating relative addresses and resolving external
    // references
    passTwo(symbolTable, moduleList, memoryMap);

    System.out.println("Symbol Table");
    printSymbolTable(symbolTable);

    System.out.println("Memory Map");
    printMemoryMap(memoryMap);

    // Check if any symbols are defined but unused and print errors
    detectUnusedSymbols(symbolTable);

  }

  public static void passOne (BufferedReader br,
                              ArrayList<Symbol> symbolTable,
                              ArrayList<Module> moduleList) {

    StringTokenizer tk = null;

    // Variables to process input
    String str = null, symbolName, wordType = null;
    int symbolAddr = 0, word = 0, currModuleIndex = -1, currModuleBaseAddr = 0;
    boolean addedBase = false, updatedBaseAddr = false;
    Module currModule = null;
    Symbol temp = null;

    // Indicates how many elements left to process in line
    int numPerLine = 0;

    // Indicates which context of module being processed
    // 0: Definition List
    // 1: Use List
    // 2: Program Text
    int contextCase = 0;

    try {
      // Process input
      while ((tk != null && tk.hasMoreElements()) || (str = br.readLine()) != null) {
        if (tk == null || !tk.hasMoreElements())
          tk = new StringTokenizer(str);

        if (!tk.hasMoreElements()) {
          continue;
        }

        // If all elements in context processed, move to next context and figure
        // out how many elements need to be processed
        if (numPerLine == 0 && tk.hasMoreTokens()) {
          String s = tk.nextToken();
          numPerLine = Integer.parseInt(s);
        }

        // create new module
        if (!addedBase) {
          currModuleIndex++;
          currModule = new Module(currModuleBaseAddr);
          moduleList.add(currModule);
          addedBase = true;
          updatedBaseAddr = false;
        }

        // Process elements; decrement counter; if still within context with
        // elements that need to be processed, break out of loop and move to
        // next line of input
        while (numPerLine != 0 && tk.hasMoreElements()) {
          switch (contextCase) {
            case 0:
              symbolName = tk.nextToken();
              if (!tk.hasMoreElements()) {
                str = br.readLine();
                tk = new StringTokenizer(str);
              }
              symbolAddr = Integer.parseInt(tk.nextToken());
              symbolAddr += currModuleBaseAddr;
              temp = new Symbol(symbolName, symbolAddr, currModuleIndex);
              addSymbol (temp, symbolTable);
              break;

            case 1:
              symbolName = tk.nextToken();
              if (!tk.hasMoreElements()) {
                str = br.readLine();
                tk = new StringTokenizer(str);
              }
              symbolAddr = Integer.parseInt(tk.nextToken());
              currModule.addSymbolToList(new Symbol(symbolName, symbolAddr));
              break;

            case 2:
              if (!updatedBaseAddr) {
                currModuleBaseAddr += numPerLine;
                updatedBaseAddr = true;
              }
              wordType = tk.nextToken();
              if (!tk.hasMoreElements()) {
                str = br.readLine();
                tk = new StringTokenizer(str);
              }
              word = Integer.parseInt(tk.nextToken());
              currModule.addWordToList(new Symbol(wordType, word));
              break;
          }
          numPerLine--;
        }

        // If all elements of line processed, switch to next context. If entire
        // module processed, increment module counter
        if (numPerLine == 0) {
          contextCase = (contextCase + 1) % 3;
          if (contextCase == 0)
            addedBase = false;
        }

      }

      // check that symbols defined in correct module
      Symbol currSymbol = null;
      for (int i  = 0; i < symbolTable.size(); i++) {

        currSymbol = symbolTable.get(i);

        if (currSymbol.getSymbolAddr() >=
            (moduleList.get(currSymbol.getModule()).moduleLength() +
            moduleList.get(currSymbol.getModule()).getBaseAddress())) {

          currSymbol.addError(" Error:  The value of " +
              currSymbol.getSymbolName() + " is outside " +
              "module " + currSymbol.getModule() + "; zero (relative) used. ");

          currSymbol.setSymbolAddr
              (moduleList.get(currSymbol.getModule()).getBaseAddress());
        }

      }

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  public static void passTwo (ArrayList<Symbol> symbolTable,
                              ArrayList<Module> moduleList,
                              ArrayList<String> memoryMap) {
    int modules = moduleList.size();
    int currWordAddr = 0;
    int symbolLoc = 0;
    int nextInList = 0;
    int useListLength = 0;
    Module currModule;
    Symbol currWord;
    Symbol currSymbol;

    for (int i = 0; i < modules; i++) {
      currModule = moduleList.get(i);
      useListLength =  currModule.useSymbolLength();

      if (useListLength > 0) {
        for (int j = 0; j < useListLength; j++) {

          currSymbol = currModule.symbolAt(j);
          nextInList = currSymbol.getSymbolAddr();

          while (nextInList != 777) {
            currWord = currModule.elementAt(nextInList);
            currWordAddr = currWord.getSymbolAddr();
            currWord.useSymbol();

            if (currWord.getSymbolName().charAt(0) != 'E') {
              currWord.addError(" Error: " + currWord.getSymbolName()
                  + " type address on use chain; treated as E type.");
              currWord.setSymbolName("E");
            }

            nextInList = currWordAddr % 1000;
            if (nextInList >= currModule.moduleLength() && nextInList != 777) {
              nextInList = 777;
              currWord.addError(" Error: Pointer in use chain exceeds module" +
                  " size; chain terminated.");
            }

            currWordAddr = (currWordAddr / 1000) * 1000;
            symbolLoc = symbolTableLoc(currSymbol, symbolTable);
            if (symbolLoc == -1) {
              currWord.addError(" Error: " + currSymbol.getSymbolName()
                  + " is not defined; zero used. ");
            } else {
              currWordAddr += symbolLoc;
            }
            currWord.setSymbolAddr(currWordAddr);
          }
        }
      }

      for (int k = 0; k < currModule.moduleLength(); k++) {
        currWord = currModule.elementAt(k);
        switch (currWord.getSymbolName().charAt(0)) {
          case 'E':
            if (!currWord.getUseStatus()) {
              currWord.setSymbolName("I");
              currWord.addError(" Error: E type address not on use chain;" +
                  " treated as I type.");
            }
            memoryMap.add("" + currWord.getSymbolAddr() +
                currWord.getErrorMessage());
            break;

          case 'R':
            memoryMap.add("" +
                (currModule.getBaseAddress() + currWord.getSymbolAddr()));
            break;

          default:
            memoryMap.add("" + currWord.getSymbolAddr() +
                currWord.getErrorMessage());
            break;

        }
      }
    }


  }

  public static void detectUnusedSymbols (ArrayList<Symbol> symbolTable) {
    int length = symbolTable.size();
    Symbol s;
    System.out.println("");
    for (int i = 0; i < length; i++) {
      s = symbolTable.get(i);
      if (!s.getUseStatus())
        System.out.println("Warning: " + s.getSymbolName() +
            " was defined in module " + s.getModule() +
            " but was never used. ");
    }
  }

  public static void addSymbol (Symbol s, ArrayList<Symbol> arr) {
    int arrLength = arr.size();

    for (int i = 0; i < arrLength; i++) {
      if (s.getSymbolName().equals(arr.get(i).getSymbolName())) {
        arr.get(i).addError(" Error: This variable is multiply defined; " +
            "first value used. ");
        return;
      }
    }
    arr.add(s);
  }

  public static int symbolTableLoc (Symbol s, ArrayList<Symbol> symbolTable) {
    for (int i = 0; i < symbolTable.size(); i ++) {
      if (symbolTable.get(i).getSymbolName().equalsIgnoreCase(s.getSymbolName())) {
        symbolTable.get(i).useSymbol();
        return symbolTable.get(i).getSymbolAddr();
      }
    }
    return -1;
  }

  public static void printSymbolTable (ArrayList<Symbol> symtab) {
    Symbol s;
    for (int i = 0; i < symtab.size(); i++) {
      s = symtab.get(i);
      System.out.println(s.getSymbolName() + "=" + s.getSymbolAddr() +
          s.getErrorMessage());
    }
    System.out.println("");
  }

  public static void printMemoryMap (ArrayList<String> memmap) {
    for (int i = 0; i < memmap.size(); i++) {
      System.out.format("%-3d: %s\n", i, memmap.get(i));
    }
  }

}