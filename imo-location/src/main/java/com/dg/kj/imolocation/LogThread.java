package com.dg.kj.imolocation;

public class LogThread implements Runnable{
    private Thread t;
    private String threadName;


    LogThread( String name) {
        threadName = name;
        System.out.println("Creating " +  threadName );
    }

    public void run() {
        System.out.println("Running " + threadName);
        while(true) {
            if(ImoLocationApplication.logQueue.isEmpty()){
                try {
                    Thread.sleep(200);
                } catch (InterruptedException ie) {
                }
            }
            Integer location = ImoLocationApplication.logQueue.poll();
            System.out.println("Upload location log : " + ImoLocationApplication.curServiceName + " : " + location.toString());

            ImoLocationApplication.logUpload(ImoLocationApplication.curServiceName, location.toString());

            try {
                Thread.sleep(200);
            } catch (InterruptedException ie) {
            }
        }
    }

    public void start () {
        System.out.println("Starting " +  threadName );
        if (t == null) {
            t = new Thread (this, threadName);
            t.start ();
        }
    }
}
