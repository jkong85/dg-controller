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
        try {
            while(index < CarEmulatorApplication.toyota_location.length) {
                Thread.sleep(2000);
                index++;
            }
        } catch (InterruptedException ie) {
        }
        System.out.println("InfoUpload Thread of " +  threadName + " is done.");
    }

    public void start () {
        System.out.println("Data index sync: " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}
