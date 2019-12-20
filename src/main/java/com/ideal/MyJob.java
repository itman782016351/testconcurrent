package com.ideal;

import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

/**
 * @author zhaopei
 * @create 2019-12-11 17:46
 */
public class MyJob implements Job {
    private static Semaphore semaphore = new Semaphore(1);
    private static ExecutorService executorService = Executors.newFixedThreadPool(2);
    CountDownLatch latch;

    @Override
    public void execute(JobExecutionContext context) throws JobExecutionException {
        System.out.println(Thread.currentThread().getName()+"task start....");
        if (semaphore.tryAcquire()) {
            System.out.println("semaphore getted...");
            try {
                int count=0;
                for (int i = 0; i <200 ; i++) {
                    EvtInstTask evtInstTask = new EvtInstTask();
                    executorService.submit(evtInstTask);
                      count++;
                }
                latch=new CountDownLatch(count);
                latch.await();
            } catch (InterruptedException e) {
                System.out.println(e);
            } finally {
                semaphore.release();
            }
        } else {
            System.out.println("semaphore  not getted....");
        }

    }

    private void insertProcessed() {
        PreparedStatement pst = null;
        Connection collConn = null;
        try {
            System.out.println("excute sql");
            collConn = getConn();
            System.out.println("conn info:"+collConn);
            if (collConn != null) {
                String sql = "insert into test1 values(sysdate)";
                pst = collConn.prepareStatement(sql);
                pst.executeUpdate();
                collConn.commit();
            }

        } catch (Exception e) {
            try {
                collConn.rollback();
            } catch (SQLException e1) {
                System.out.println(e1);
            }
            System.out.println(e);
        } finally {
            try {
                if (pst!=null)
                    pst.close();
                if (collConn != null)
                    collConn.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }

    public Connection getConn() {
        try {
//            String url = "jdbc:oracle:thin:@10.145.196.101:1521:cqdb";
//            String user = "coll";
//            String pwd = "coll";
            String url = "jdbc:oracle:thin:@10.145.196.101:1521:iamzwdb";
            String user = "dbusr01";
            String pwd = "dbusr01123";
            String driver = "oracle.jdbc.driver.OracleDriver";
            Class.forName(driver);
            return DriverManager.getConnection(url, user, pwd);
        } catch (Exception e) {
            System.out.println(e);
            return  null;
        }
    }

    class EvtInstTask implements Runnable {

        @Override
        public void run() {
            try {
                insertProcessed();
            } catch (Exception e) {
                System.out.println(e);
            } finally {
                while (latch == null) {
                    try {
                        Thread.sleep(5000);
                    } catch (InterruptedException e) {
                        System.out.println(e);
                    }
                }
                latch.countDown();
            }
        }
    }
}
