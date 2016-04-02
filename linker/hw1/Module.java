package hw1;

import java.util.*;

public class Module {
  private int baseAddress;
  private ArrayList<Symbol> moduleElements;
  private ArrayList<Symbol> moduleUseSymbols;

  public Module (int baseAddr) {
    this.baseAddress = baseAddr;
    moduleElements = new ArrayList<Symbol>();
    moduleUseSymbols = new ArrayList<Symbol>();
  }

  public void addWordToList (Symbol s) {
    this.moduleElements.add(s);
  }

  public void addSymbolToList (Symbol s) {
    this.moduleUseSymbols.add(s);
  }

  public int moduleLength () {
    return this.moduleElements.size();
  }

  public int useSymbolLength () { return this.moduleUseSymbols.size(); }

  public int getBaseAddress () { return this.baseAddress; }

  public Symbol symbolAt (int i) {
    return this.moduleUseSymbols.get(i);
  }

  public Symbol elementAt (int i) {
    return this.moduleElements.get(i);
  }

}