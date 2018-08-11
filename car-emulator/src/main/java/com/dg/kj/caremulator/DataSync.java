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
        for(int i=0; i<CarEmulatorApplication.DATA_SIZE; i++){
            try { Thread.sleep(2000); } catch (InterruptedException ie) { }
            index++;
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
