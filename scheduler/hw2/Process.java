/**
 * Created by subhankarghosh on 3/11/16.
 */

package hw2;

public class Process implements Comparable<Process> {

  private int arrivalTime;
  private int bound;
  private int CPUTime;
  private int IOTime;
  // ------
  private int CPUBurst;
  private int IOBurst;
  private int totalCPUTime;
  private int totalIOTime;
  private int waitTime;
  private int finishTime;
  private int order;
  private int quantum;
  private int time;
  private int status;
  // 4 = Done
  // 3 = Blocked
  // 2 = Running
  // 1 = Ready
  // 0 = Not Started

  public Process (int a, int b, int c, int io) {
    arrivalTime = a;
    bound = b;
    CPUTime = c;
    IOTime = io;
    totalCPUTime = 0;
    finishTime = 0;
    CPUBurst = 0;
    IOBurst = 0;
    totalCPUTime = 0;
    totalIOTime = 0;
    waitTime = 0;
    status = 0;
    quantum = 0;
    time = 0;
    order = 0;
  }

  @Override
  public int compareTo(Process p)  {
    if (this.arrivalTime > p.arrivalTime)
      return 1;
    else if (this.arrivalTime == p.arrivalTime)
      return 0;
    else
      return -1;
  }

  @Override
  public String toString () {
    return ("(" +arrivalTime + ", " +
    bound + ", " + CPUTime + ", " + IOTime + ")");
  }

  public int getArrivalTime() {
    return arrivalTime;
  }

  public void setArrivalTime(int arrivalTime) {
    this.arrivalTime = arrivalTime;
  }

  public int getBound() {
    return bound;
  }

  public void setBound(int bound) {
    this.bound = bound;
  }

  public int getCPUTime() {
    return CPUTime;
  }

  public void setCPUTime(int CPUTime) {
    this.CPUTime = CPUTime;
  }

  public int getIOTime() {
    return IOTime;
  }

  public void setIOTime(int IOTime) {
    this.IOTime = IOTime;
  }

  public int getTotalCPUTime() {
    return totalCPUTime;
  }

  public void setTotalCPUTime(int totalCPUTime) {
    this.totalCPUTime = totalCPUTime;
  }

  public void incrementTotalCPUTime() {
    this.totalCPUTime++;
  }

  public int getFinishTime() {
    return finishTime;
  }

  public void setFinishTime(int finishTime) {
    this.finishTime = finishTime;
  }

  public int getStatus() {
    return status;
  }

  public void setStatus(int status) {
    this.status = status;
  }

  public int getCPUBurst() {
    return CPUBurst;
  }

  public void setCPUBurst(int CPUBurst) {
    this.CPUBurst = CPUBurst;
  }

  public void decrementCPUBurst() {
    this.CPUBurst--;
  }
  public int getIOBurst() {
    return IOBurst;
  }

  public void setIOBurst(int IOBurst) {
    this.IOBurst = IOBurst;
  }

  public void decrementIOBurst() {
    this.IOBurst--;
  }

  public int getTotalIOTime() {
    return totalIOTime;
  }

  public void setTotalIOTime(int totalIOTime) {
    this.totalIOTime = totalIOTime;
  }

  public void incrementIOTime() {
    this.totalIOTime++;
  }

  public int getWaitTime() {
    return waitTime;
  }

  public void setWaitTime(int waitTime) {
    this.waitTime = waitTime;
  }

  public void incrementWaitTime() {
    this.waitTime++;
  }

  public int getQuantum() {
    return this.quantum;
  }

  public void resetQuantum() {
    this.quantum = 2;
  }

  public void decrementQuantum() {
    this.quantum--;
  }

  public void setTime(int t) {
    this.time = t;
  }

  public int getTime() {
    return this.time;
  }

  public void setOrder(int i) {
    this.order = i;
  }

  public int getOrder() {
    return this.order;
  }
}
