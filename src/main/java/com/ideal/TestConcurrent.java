package com.ideal;

import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.SchedulerFactory;
import org.quartz.Trigger;
import org.quartz.impl.StdSchedulerFactory;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * @author zhaopei
 * @create 2019-12-11 17:31
 */
public class TestConcurrent {
    public static void main(String[] args) {
        try {
            SchedulerFactory sf=new StdSchedulerFactory();
            sf.getScheduler().start();
            JobDetail jobDetail = new JobDetail("myJobName","myJobGroup" , Class.forName("com.ideal.MyJob"));
            Trigger trigger = new CronTrigger("myJobName","myJobGroup", "0/5 * * * * ? ");
            sf.getScheduler().scheduleJob(jobDetail, trigger);
        } catch (Exception e) {
            e.printStackTrace();
        }
       /* for (int i = 0; i < 30; i++) {
            System.out.println(i);
            Thread t=new Thread(new Runnable() {
                @Override
                public void run() {
                    for (int j = 0; j <2000 ; j++) {
                        insertProcessed();
                    }
                }
            });
            t.start();

        }*/
    }

    private static void insertProcessed() {
        PreparedStatement pst = null;
        MyJob myJob = new MyJob();
        Connection collConn = myJob.getConn();
        try {
            System.out.println("start  sql");
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
                if (!pst.isClosed())
                    pst.close();
                if (collConn != null)
                    collConn.close();
            } catch (SQLException e) {
                System.out.println(e);
            }
        }
    }
}
