package com.dg.kj.caremulator;

public class DataSync implements Runnable{
    private Thread t;
    private String threadName;

    public static Integer index = 0;

    DataSync(String name) {
        threadName = name;
        System.out.println("Data index sync " +  threadName );
    }

    public void run() {
        int cnt = 10;
        // valid range (min, max) : (0, 120)
        int min = 0;
        int max = CarEmulatorApplication.DATA_SIZE;
        for(int i=0; i<cnt; i++){
            System.out.println("The car moves from left to right !");
            while(index < max){
                try { Thread.sleep(2000); } catch (InterruptedException ie) { }
                index++;
            }
            System.out.println("The car moves from right to left !");
            while(index > min ){
                try { Thread.sleep(2000); } catch (InterruptedException ie) { }
                index--;
            }
        }
        System.out.println("Datasync Thread of " +  threadName + " is done.");
    }

    public void start () {
        System.out.println("Data index sync: " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}
