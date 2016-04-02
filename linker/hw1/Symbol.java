package hw1;

/**
 * Created by subhankarghosh on 2/19/16.
 */

public class Symbol {

  private String symbolName;
  private int symbolAddr;
  private int moduleNumber;
  private String errorMessage;
  private boolean used;

  public Symbol (String name, int addr) {
    this.symbolName = name;
    this.symbolAddr = addr;
    this.errorMessage = "";
    this.used = false;
  }

  public Symbol (String name, int addr, int moduleNumber) {
    this.symbolName = name;
    this.symbolAddr = addr;
    this.errorMessage = "";
    this.used = false;
    this.moduleNumber = moduleNumber;
  }

  public Symbol (String name, int addr, String error) {
    this.symbolName = name;
    this.symbolAddr = addr;
    this.errorMessage = "Error: " + error;
    this.used = false;
  }

  public String getSymbolName () {
    return this.symbolName;
  }

  public void setSymbolName (String s) { this.symbolName = s; }

  public int getSymbolAddr () {
    return this.symbolAddr;
  }

  public void setSymbolAddr (int n) { this.symbolAddr = n; }

  public void addError (String error) {
    this.errorMessage += error;
  }

  public void useSymbol () {
    this.used = true;
  }

  public boolean getUseStatus () {
    return this.used;
  }

  public int getModule () { return this.moduleNumber; }

  public String getErrorMessage () { return this.errorMessage; }

  public String toString () {
    return (this.symbolName + ":  " + this.symbolAddr + "  " +
        this.errorMessage);
  }

  public boolean equals (Symbol s) {
    if (this.getSymbolName().equals(s.symbolName))
      return true;
    else
      return false;
  }
}