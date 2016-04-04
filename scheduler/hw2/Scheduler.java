/**
 * Created by subhankarghosh on 3/11/16.
 */

package hw2;

import java.io.*;
import java.util.*;

public class Scheduler {

  private static ArrayList<Process> fcfsProcs = new ArrayList<Process>();
  private static ArrayList<Process> rrobProcs = new ArrayList<Process>();
  private static ArrayList<Process> lcfsProcs = new ArrayList<Process>();
  private static ArrayList<Process> hprnProcs = new ArrayList<Process>();
  private static Scanner rand;

  public static void main(String [] args) {

    // Read file and process input
    File inputFile;
    boolean verbose = false;
    //deal with verbose flag
    if (args.length > 1) {
      inputFile = new File(args[1]);
      verbose = true;
    } else {
      inputFile = new File(args[0]);
    }
    readInput(inputFile, fcfsProcs, rrobProcs, lcfsProcs, hprnProcs);

    try {
      rand = new Scanner(new File("random-numbers.txt"));
    } catch(Exception e) {
      System.exit(0);
    }
    fcfs(fcfsProcs, verbose);
    rand.close();

    try {
      rand = new Scanner(new File("random-numbers.txt"));
    } catch(Exception e) {
      System.exit(0);
    }
    rrob(rrobProcs, verbose);
    rand.close();

    try {
      rand = new Scanner(new File("random-numbers.txt"));
    } catch(Exception e) {
      System.exit(0);
    }
    lcfs(lcfsProcs, verbose);
    rand.close();

    try {
      rand = new Scanner(new File("random-numbers.txt"));
    } catch(Exception e) {
      System.exit(0);
    }
    hprn(hprnProcs, verbose);
    rand.close();

  }

  private static void fcfs(ArrayList<Process> procs, boolean verbose) {
    System.out.println("\nThe original input was: " + procs);
    Collections.sort(procs);
    System.out.println("The (sorted) input is:  " + procs);
    if(verbose)
      System.out.println("\n" +
          "This detailed printout gives the state and remaining " +
          "burst for each process\n" +
          "\n");

    int IOUtil = 0;
    int CPUUtil = 0;
    int cycle = 0;
    int procsToProcess = procs.size();
    Process runningProc = null;
    ArrayList<Process> readyQueue = new ArrayList<Process>();
    ArrayList<Process> blockedQueue = new ArrayList<Process>();
    ArrayList<Process> moveToReady = new ArrayList<Process>();

    while(procsToProcess > 0) {
      if (verbose) {
        System.out.format("Before Cycle %4d:  ", cycle);
        for(Process p : procs) {
          switch (p.getStatus()) {
            case 0:
              System.out.print(" unstarted  0 ");
              break;
            case 1:
              System.out.print("     ready  0 ");
              break;
            case 2:
              System.out.format("   running %2d ", p.getCPUBurst());
              break;
            case 3:
              System.out.format("   blocked %2d ", p.getIOBurst());
              break;
            default:
              System.out.print("terminated  0 ");
              break;
          }
        }
        System.out.println();
      }

      // do_blocked(procs);
      if (blockedQueue.size() > 0) {
        IOUtil++;
        for (Process p : blockedQueue) {
          p.decrementIOBurst();
          p.incrementIOTime();
          if (p.getIOBurst() == 0) {
            p.setStatus(1);
            moveToReady.add(p);
          }
        }
        for (Process p : moveToReady) {
          blockedQueue.remove(p);
        }
        Collections.sort(moveToReady);
        for(int i = 1; i < moveToReady.size(); i++) {
          // lab 2 tie breaking rule
          if((moveToReady.get(i-1).getArrivalTime() ==
              moveToReady.get(i).getArrivalTime()) &&
              (procs.indexOf(moveToReady.get(i-1)) >
              procs.indexOf(moveToReady.get(i)))){
            Collections.swap(moveToReady, i-1, i);
          }
        }
        for (Process p : moveToReady) {
          readyQueue.add(p);
        }
        moveToReady.clear();
      }

      // do_running(procs);
      if (runningProc != null) {
        runningProc.incrementTotalCPUTime();
        runningProc.decrementCPUBurst();
        CPUUtil++;
        if (runningProc != null &&
            runningProc.getTotalCPUTime() < runningProc.getCPUTime() &&
            runningProc.getCPUBurst() == 0) {
          int iBurst = randomOS(runningProc.getIOTime());
          if (iBurst > runningProc.getIOTime())
            runningProc.setIOBurst(runningProc.getIOTime());
          else
            runningProc.setIOBurst(iBurst);
          runningProc.setStatus(3);
          blockedQueue.add(runningProc);
          runningProc = null;
        }
        if (runningProc != null &&
            runningProc.getTotalCPUTime() >= runningProc.getCPUTime()) {
          runningProc.setStatus(4);
          runningProc.setFinishTime(cycle);
          runningProc = null;
          procsToProcess--;
        }
      }

      // do_arriving(procs, cycle);
      for (Process p : procs) {
        if (p.getArrivalTime() == cycle) {
          p.setStatus(1);
          readyQueue.add(p);
        }
      }

      //do_ready(procs, readyQueue);
      if (readyQueue.size() > 0) {
        if (runningProc == null) {
          runningProc = readyQueue.get(0);
          readyQueue.remove(0);
          runningProc.setStatus(2);
          int cBurst = randomOS(runningProc.getBound());
          if (cBurst > runningProc.getCPUTime())
            runningProc.setCPUBurst(runningProc.getCPUTime());
          else
            runningProc.setCPUBurst(cBurst);
        }
        for(Process p: readyQueue)
          p.incrementWaitTime();
      }

      cycle++;
    }

    System.out.println("\nThe scheduling algorithm used was " +
        "First Come First Served\n");
    printResult(procs, cycle, CPUUtil, IOUtil);
  }

  private static void rrob(ArrayList<Process> procs, boolean verbose) {
    System.out.println("\nThe original input was: " + procs);
    Collections.sort(procs);
    System.out.println("The (sorted) input is:  " + procs);
    if(verbose)
      System.out.println("\n" +
          "This detailed printout gives the state and remaining " +
          "burst for each process\n" +
          "\n");

    int IOUtil = 0;
    int CPUUtil = 0;
    int cycle = 0;
    int procsToProcess = procs.size();
    Process runningProc = null;
    ArrayList<Process> readyQueue = new ArrayList<Process>();
    ArrayList<Process> blockedQueue = new ArrayList<Process>();
    ArrayList<Process> moveToReady = new ArrayList<Process>();

    while(procsToProcess > 0) {
      if (verbose) {
        System.out.format("Before Cycle %4d:  ", cycle);
        for (Process p : procs) {
          switch (p.getStatus()) {
            case 0:
              System.out.print(" unstarted  0 ");
              break;
            case 1:
              System.out.print("     ready  0 ");
              break;
            case 2:
              System.out.format("   running %2d ", p.getCPUBurst());
              break;
            case 3:
              System.out.format("   blocked %2d ", p.getIOBurst());
              break;
            default:
              System.out.print("terminated  0 ");
              break;
          }
        }
        System.out.println();
      }

      // do_blocked(procs);
      if (blockedQueue.size() > 0) {
        IOUtil++;
        for (Process p : blockedQueue) {
          p.decrementIOBurst();
          p.incrementIOTime();
          if (p.getIOBurst() == 0) {
            p.setStatus(1);
            moveToReady.add(p);
          }
        }
        for (Process p : moveToReady) {
          blockedQueue.remove(p);
        }
      }

      // do_running(procs);
      if (runningProc != null) {
        runningProc.incrementTotalCPUTime();
        runningProc.decrementCPUBurst();
        runningProc.decrementQuantum();
        CPUUtil++;
        if (runningProc != null &&
            runningProc.getTotalCPUTime() < runningProc.getCPUTime() &&
            runningProc.getCPUBurst() == 0) {
          int iBurst = randomOS(runningProc.getIOTime());
          if (iBurst > runningProc.getIOTime())
            runningProc.setIOBurst(runningProc.getIOTime());
          else
            runningProc.setIOBurst(iBurst);
          runningProc.setStatus(3);
          blockedQueue.add(runningProc);
          runningProc = null;
        }
        if (runningProc != null &&
            runningProc.getTotalCPUTime() >= runningProc.getCPUTime()) {
          runningProc.setStatus(4);
          runningProc.setFinishTime(cycle);
          runningProc = null;
          procsToProcess--;
        }
        if (runningProc != null &&
            runningProc.getQuantum() == 0) {
          moveToReady.add(runningProc);
          runningProc = null;
        }
      }

      // do_arriving(procs, cycle);
      for (Process p : procs) {
        if (p.getArrivalTime() == cycle) {
          p.setStatus(1);
          moveToReady.add(p);
        }
      }

      Collections.sort(moveToReady);
      for (int i = 1; i < moveToReady.size(); i++) {
        // lab 2 tie breaking rule
        if ((moveToReady.get(i - 1).getArrivalTime() ==
            moveToReady.get(i).getArrivalTime()) &&
            (procs.indexOf(moveToReady.get(i - 1)) >
                procs.indexOf(moveToReady.get(i)))) {
          Collections.swap(moveToReady, i - 1, i);
        }
      }
      for (Process p : moveToReady) {
        readyQueue.add(p);
      }
      moveToReady.clear();

      //do_ready(procs, readyQueue);
      if (readyQueue.size() > 0) {
        if (runningProc == null) {
          runningProc = readyQueue.get(0);
          readyQueue.remove(runningProc);
          runningProc.setStatus(2);
          if(runningProc.getCPUBurst() == 0) {
            int cBurst = randomOS(runningProc.getBound());
            if (cBurst > runningProc.getCPUTime())
              runningProc.setCPUBurst(runningProc.getCPUTime());
            else
              runningProc.setCPUBurst(cBurst);
          }
          runningProc.resetQuantum();
        }
        for (Process p : readyQueue)
          p.incrementWaitTime();
      }

      cycle++;
    }
    System.out.println("\nThe scheduling algorithm used was " +
        "Round Robin\n");
    printResult(procs, cycle, CPUUtil, IOUtil);
  }

  private static void lcfs(ArrayList<Process> procs, boolean verbose) {
    System.out.println("\nThe original input was: " + procs);
    Collections.sort(procs);
    System.out.println("The (sorted) input is:  " + procs);
    if(verbose)
      System.out.println("\n" +
          "This detailed printout gives the state and remaining " +
          "burst for each process\n" +
          "\n");

    int IOUtil = 0;
    int CPUUtil = 0;
    int cycle = 0;
    int procsToProcess = procs.size();
    Process runningProc = null;
    ArrayList<Process> readyQueue = new ArrayList<Process>();
    ArrayList<Process> blockedQueue = new ArrayList<Process>();
    ArrayList<Process> moveToReady = new ArrayList<Process>();

    while(procsToProcess > 0) {
      if (verbose) {
        System.out.format("Before Cycle %4d:  ", cycle);
        for(Process p : procs) {
          switch (p.getStatus()) {
            case 0:
              System.out.print(" unstarted  0 ");
              break;
            case 1:
              System.out.print("     ready  0 ");
              break;
            case 2:
              System.out.format("   running %2d ", p.getCPUBurst());
              break;
            case 3:
              System.out.format("   blocked %2d ", p.getIOBurst());
              break;
            default:
              System.out.print("terminated  0 ");
              break;
          }
        }
        System.out.println();
      }

      // do_blocked(procs);
      if (blockedQueue.size() > 0) {
        IOUtil++;
        for (Process p : blockedQueue) {
          p.decrementIOBurst();
          p.incrementIOTime();
          if (p.getIOBurst() == 0) {
            p.setStatus(1);
            moveToReady.add(p);
          }
        }
        for (Process p : moveToReady) {
          blockedQueue.remove(p);
        }
      }

      // do_running(procs);
      if (runningProc != null) {
        runningProc.incrementTotalCPUTime();
        runningProc.decrementCPUBurst();
        CPUUtil++;
        if (runningProc != null &&
            runningProc.getTotalCPUTime() < runningProc.getCPUTime() &&
            runningProc.getCPUBurst() == 0) {
          int iBurst = randomOS(runningProc.getIOTime());
          if (iBurst > runningProc.getIOTime())
            runningProc.setIOBurst(runningProc.getIOTime());
          else
            runningProc.setIOBurst(iBurst);
          runningProc.setStatus(3);
          blockedQueue.add(runningProc);
          runningProc = null;
        }
        if (runningProc != null &&
            runningProc.getTotalCPUTime() >= runningProc.getCPUTime()) {
          runningProc.setStatus(4);
          runningProc.setFinishTime(cycle);
          runningProc = null;
          procsToProcess--;
        }
      }

      // do_arriving(procs, cycle);
      for (Process p : procs) {
        if (p.getArrivalTime() == cycle) {
          p.setStatus(1);
          moveToReady.add(p);
        }
      }

      Collections.sort(moveToReady);
      for(int i = 1; i < moveToReady.size(); i++) {
        // lab 2 tie breaking rule
        if((moveToReady.get(i-1).getArrivalTime() ==
            moveToReady.get(i).getArrivalTime()) &&
            (procs.indexOf(moveToReady.get(i-1)) >
                procs.indexOf(moveToReady.get(i)))){
          Collections.swap(moveToReady, i-1, i);
        }
      }
      Collections.reverse(moveToReady);
      for (Process p : moveToReady) {
        readyQueue.add(p);
      }
      moveToReady.clear();

      //do_ready(procs, readyQueue);
      if (readyQueue.size() > 0) {
        if (runningProc == null) {
          runningProc = readyQueue.get(readyQueue.size()-1);
          readyQueue.remove(runningProc);
          runningProc.setStatus(2);
          int cBurst = randomOS(runningProc.getBound());
          if (cBurst > runningProc.getCPUTime())
            runningProc.setCPUBurst(runningProc.getCPUTime());
          else
            runningProc.setCPUBurst(cBurst);
        }
        for(Process p: readyQueue)
          p.incrementWaitTime();
      }

      cycle++;
    }

    System.out.println("\nThe scheduling algorithm used was " +
        "Last Come First Served\n");
    printResult(procs, cycle, CPUUtil, IOUtil);
  }

  private static void hprn(ArrayList<Process> procs, boolean verbose) {
    System.out.println("\nThe original input was: " + procs);
    Collections.sort(procs);
    for (int i = 0; i < procs.size(); i++) {
      procs.get(i).setOrder(i);
    }
    System.out.println("The (sorted) input is:  " + procs);
    if(verbose)
      System.out.println("\n" +
          "This detailed printout gives the state and remaining " +
          "burst for each process\n" +
          "\n");

    int IOUtil = 0;
    int CPUUtil = 0;
    int cycle = 0;
    int procsToProcess = procs.size();
    Process runningProc = null;
    ArrayList<Process> readyQueue = new ArrayList<Process>();
    ArrayList<Process> blockedQueue = new ArrayList<Process>();
    ArrayList<Process> moveToReady = new ArrayList<Process>();

    while(procsToProcess > 0) {
      if (verbose) {
        System.out.format("Before Cycle %4d:  ", cycle);
        for (Process p : procs) {
          switch (p.getStatus()) {
            case 0:
              System.out.print(" unstarted  0 ");
              break;
            case 1:
              System.out.print("     ready  0 ");
              break;
            case 2:
              System.out.format("   running %2d ", p.getCPUBurst());
              break;
            case 3:
              System.out.format("   blocked %2d ", p.getIOBurst());
              break;
            default:
              System.out.print("terminated  0 ");
              break;
          }
        }
        System.out.println();
      }
      for(Process p:procs){
        p.setTime(cycle);
      }

      // do_blocked(procs);
      if (blockedQueue.size() > 0) {
        IOUtil++;
        for (Process p : blockedQueue) {
          p.decrementIOBurst();
          p.incrementIOTime();
          if (p.getIOBurst() == 0) {
            p.setStatus(1);
            moveToReady.add(p);
          }
        }
        for (Process p : moveToReady) {
          blockedQueue.remove(p);
        }
      }

      // do_running(procs);
      if (runningProc != null) {
        runningProc.incrementTotalCPUTime();
        runningProc.decrementCPUBurst();
        CPUUtil++;
        if (runningProc != null &&
            runningProc.getTotalCPUTime() < runningProc.getCPUTime() &&
            runningProc.getCPUBurst() == 0) {
          int iBurst = randomOS(runningProc.getIOTime());
          if (iBurst > runningProc.getIOTime())
            runningProc.setIOBurst(runningProc.getIOTime());
          else
            runningProc.setIOBurst(iBurst);
          runningProc.setStatus(3);
          blockedQueue.add(runningProc);
          runningProc = null;
        }
        if (runningProc != null &&
            runningProc.getTotalCPUTime() >= runningProc.getCPUTime()) {
          runningProc.setStatus(4);
          runningProc.setFinishTime(cycle);
          runningProc = null;
          procsToProcess--;
        }
      }

      // do_arriving(procs, cycle);
      for (Process p : procs) {
        if (p.getArrivalTime() == cycle) {
          p.setStatus(1);
          moveToReady.add(p);
        }
      }

      for (Process p : moveToReady) {
        readyQueue.add(p);
      }
      moveToReady.clear();

      Collections.sort(readyQueue, new Comparator<Process>() {
        @Override
        public int compare(Process o1, Process o2) {
          float pr1 = ((float) o1.getTime() - o1.getArrivalTime()) /
              (Math.max(1, o1.getTotalCPUTime()));
          float pr2 = ((float) o2.getTime() - o2.getArrivalTime()) /
              (Math.max(1, o2.getTotalCPUTime()));
          if (-1*Float.compare(pr1, pr2) != 0)
            return -1*Float.compare(pr1, pr2);
          else if (Integer.compare(o1.getArrivalTime(), o2.getArrivalTime())
              != 0)
            return Integer.compare(o1.getArrivalTime(), o2.getArrivalTime());
          else
            return Integer.compare(o1.getOrder(), o2.getOrder());
        }
      });

      //do_ready(procs, readyQueue);
      if (readyQueue.size() > 0) {
        if (runningProc == null) {
          runningProc = readyQueue.get(0);
          readyQueue.remove(runningProc);
          runningProc.setStatus(2);
          int cBurst = randomOS(runningProc.getBound());
          if (cBurst > runningProc.getCPUTime())
            runningProc.setCPUBurst(runningProc.getCPUTime());
          else
            runningProc.setCPUBurst(cBurst);
        }
        for (Process p : readyQueue)
          p.incrementWaitTime();
      }

      cycle++;
    }
    System.out.println("\nThe scheduling algorithm used was " +
        "Highest Penalty Ratio Next\n");
    printResult(procs, cycle, CPUUtil, IOUtil);
  }

  private static int randomOS(int n) {
    int num = (1 + rand.nextInt()%n);
    return num;
  }

  private static void printResult(ArrayList<Process> procs,
                                  int cycle,
                                  int CPUUtil,
                                  int IOUtil) {
    int totalTurnaroundTime = 0;
    int totalWaitingTime = 0;
    for(int i = 0; i < procs.size(); i++) {
      Process p = procs.get(i);
      totalTurnaroundTime += (p.getFinishTime()-p.getArrivalTime());
      totalWaitingTime += p.getWaitTime();
      System.out.println("Process " + i +":");
      System.out.println("        (A, B, C, IO) = " + p);
      System.out.println("        Finishing Time : " + p.getFinishTime());
      System.out.println("        Turnaround Time : "
          + (p.getFinishTime()-p.getArrivalTime()));
      System.out.println("        I/O Time : " + p.getTotalIOTime());
      System.out.println("        Waiting Time : " + p.getWaitTime()
          + "\n");
    }
    System.out.println("Summary Data:");
    System.out.println("        Finishing time: " + (cycle-1));
    System.out.format("        CPU Utilization: %.6f\n", ((float)CPUUtil/(cycle-1)));
    System.out.format("        I/O Utilization: %.6f\n", ((float) IOUtil/(cycle-1)));
    System.out.format("        Throughput: %.6f processes per hundred cycles\n",
        (((float)100*procs.size())/(cycle-1)));
    System.out.format("        Average turnaround time: %.6f\n",
        ((float)totalTurnaroundTime/procs.size()));
    System.out.format("        Average wait time: %.6f\n",
        ((float)totalWaitingTime/procs.size()));
  }

  private static void readInput(File inputFile,
                                ArrayList<Process> procs1,
                                ArrayList<Process> procs2,
                                ArrayList<Process> procs3,
                                ArrayList<Process> procs4) {
    try {
      Scanner s = new Scanner(inputFile);
      int numProcs = s.nextInt();
      int a, b, c, io;
      while(numProcs > 0) {
        a = s.nextInt();
        b = s.nextInt();
        c = s.nextInt();
        io = s.nextInt();
        procs1.add(new Process(a, b, c, io));
        procs2.add(new Process(a, b, c, io));
        procs3.add(new Process(a, b, c, io));
        procs4.add(new Process(a, b, c, io));
        numProcs--;
      }

    } catch (IOException e) {
      e.printStackTrace();
      System.exit(0);
    }
  }

}
