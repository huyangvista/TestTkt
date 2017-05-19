
import vdll.data.msql.MySql;
import vdll.net.socket.Client;
import vdll.net.socket.Conned;
import vdll.net.socket.Server;
import vdll.utils.io.FileOperate;
import vdll.utils.time.DateTime;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.ExecutionException;

/**
 * Created by Hocean on 2017/5/11.  1365676089
 */
public class Hello {
    public static void main(String[] args) {
        startServer();
        while (true) {
            try {
                System.out.print("输入\"e\"退出 请输入要搜索的文件名（票号）：");
                Scanner scanner = new Scanner(System.in);
                String next = scanner.nextLine();
                if (next.equals("e")) {
                    System.out.println("程序退出。");
                    break;
                } else if (next.trim().equals("")) {
                    continue;
                }

                FindTkt f = new FindTkt();
                String sql = f.find(next);
                DateTime dt = DateTime.Now();
                sql = "-- 时间" + dt + "\r\n" + sql;
                System.out.println(sql);
                FileOperate.createFile(getPath() + "生成的语句.txt", sql);


                System.out.println("请输入\"Y\"或数字\"0\"，更新数据库 其他任何字符跳过。");
                if (!f.tag_r) {
                    System.out.println("注意票状态不是退票状态，谨慎操作");
                }
                next = scanner.nextLine();
                if (next.equals("y") || next.equals("Y") || next.equals("0")) {
                    MySql mySql = new MySql();
                    mySql.exeU(f.sql_grossrefund_amount);
                    mySql.exeU(f.sql_refundtickettax);
                    mySql.exeU(f.sql_ticketstatus);
                    mySql.exeU(f.sql_dedu);
                    mySql.exeU(f.sql_realrefundticketfee);
                    mySql.exeU(f.sql_rfnb);
                    mySql.exeU(f.sql_rtime);
                    mySql.close();
                    System.out.println("数据更新完成");
                    System.out.println();
                }
                System.out.println();
            } catch (Exception e) {
                System.err.println("搜索到的文件不是机票的格式。");

            }

        }

    }


    public static String getPath() {
        String filePath = System.getProperty("java.class.path");
        String pathSplit = System.getProperty("path.separator");//windows下是";",linux下是":"
        if (filePath.contains(pathSplit)) {
            filePath = filePath.substring(0, filePath.indexOf(pathSplit));
        } else if (filePath.endsWith(".jar")) {//截取路径中的jar包名,可执行jar包运行的结果里包含".jar"
            //此时的路径是"E:\workspace\Demorun\Demorun_fat.jar"，用"/"分割不行
            //下面的语句输出是-1，应该改为lastIndexOf("\\")或者lastIndexOf(File.separator)
//          System.out.println("getPath2:"+filePath.lastIndexOf("/"));
            filePath = filePath.substring(0, filePath.lastIndexOf(File.separator) + 1);
        }
        return filePath;
    }

    public static void startServer() {

        new Thread(new Runnable() {
            @Override
            public void run() {
                Server server = new Server() {
                    @Override
                    public Conned.IReceive getReceive(final Conned conned) {
                        return new Conned.IReceive() {
                            //定义缓存变量
                            FindTkt f = new FindTkt();

                            @Override
                            public void invoke(Object msg) {
                                try {
                                    System.out.println("服务器接受的消息: " + msg);
                                    String[] con = msg.toString().split("&&");
                                    String act = con[0];
                                    String next = con[1];
                                    String yes = con[2];

                                    if ("find".equals(act)) {
                                        String sql = f.find(next);
                                        DateTime dt = DateTime.Now();
                                        sql = "-- 时间" + dt + "\r\n" + sql;
                                        System.out.println(sql);
                                        conned.send(sql);
                                        //FileOperate.createFile(getPath() + "生成的语句.txt", sql);
                                        System.out.println();
                                    } else if ("update".equals(act)) {
                                        if (!f.tag_r) {
                                            String msgFinish = "-- 票状态不是退票状态";
                                            System.out.println(msgFinish);
                                            conned.send(msgFinish);
                                            return;
                                        }

                                        MySql mySql = new MySql();
                                        mySql.exeU(f.sql_grossrefund_amount);
                                        mySql.exeU(f.sql_refundtickettax);
                                        mySql.exeU(f.sql_ticketstatus);
                                        mySql.exeU(f.sql_dedu);
                                        mySql.exeU(f.sql_realrefundticketfee);
                                        mySql.exeU(f.sql_rfnb);
                                        mySql.exeU(f.sql_rtime);
                                        mySql.close();
                                        String msgFinish = "-- 票号：" + f.TicketNumber + " 的数据更新完成";
                                        System.out.println(msgFinish);
                                        conned.send(msgFinish);
                                        System.out.println();
                                    } else if ("updateVives".equals(act)) {
                                        String[] ss = next.split("@@@");
                                        String numbers = "";
                                        try {

                                            for (int i = 0; i < ss.length; i++) {
                                                String s = ss[i];
                                                int inds = ss[i].indexOf("票状态");
                                                if (inds >= 0) {
                                                    String status = s.substring(inds + 3).replace("-", "").trim();
                                                    if ("R".equals(status) || "r".equals(status)) {

                                                    } else {
                                                        String msgFinish = "-- 票状态不是退票状态";
                                                        System.out.println(msgFinish);
                                                        conned.send(msgFinish);
                                                        System.out.println();
                                                        return;
                                                    }
                                                }
                                            }

                                            MySql mySql = new MySql();
                                            for (int i = 0; i < ss.length; i++) {
                                                String s = ss[i];
                                                int ind = ss[i].indexOf("票号");

                                                if (ind >= 0) {
                                                    String number = s.substring(ind + 2).replace("-", "").trim();
                                                    numbers = number;
                                                    mySql.exeQ("SELECT grossrefund_amount, refundtickettax,ticketstatus,dedu,realrefundticketfee,rfnb,rtime FROM  T_BLUESKY_ORD_TKTDATA " +
                                                            "WHERE  ticketnumber = '" + number + "';");
                                                    List<Map<String, Object>> list = mySql.getParms();
                                                    StringBuilder sb = new StringBuilder();
                                                    for (Map<String, Object> item : list) {
                                                        Iterator<Map.Entry<String, Object>> iterator = item.entrySet().iterator();
                                                        while (iterator.hasNext()) {
                                                            Map.Entry<String, Object> n = iterator.next();
                                                            sb.append(n.getKey() + ":" + n.getValue() + "   ");
                                                        }
                                                        sb.append("\r\n");
                                                    }
                                                    DateTime dt = DateTime.Now();
                                                    FileOperate.createFile(getPath() + "tktback/" + number + " " + dt.format().replace(":", "") + ".txt", sb.toString());
                                                    break;
                                                }
                                            }
                                            mySql.close();
                                        } catch (Exception e) {

                                        }

                                        if(numbers.equals("")){
                                            String msgFinish = "-- 语句错误，请查找后再更新数据。";
                                            System.out.println(msgFinish);
                                            conned.send(msgFinish);
                                            System.out.println();
                                        }else{
                                            MySql mySql = new MySql();
                                            for (int i = 0; i < ss.length; i++) {
                                                mySql.exeU(ss[i]);
                                            }
                                            mySql.close();
                                            String msgFinish = "-- 票号:" + numbers + " 的数据更新完成";
                                            System.out.println(msgFinish);
                                            conned.send(msgFinish);
                                            System.out.println();
                                        }
                                    }
                                } catch (Exception e) {
                                    //格式异常终止连接
                                    conned.close();
                                }
                            }
                        };
                    }
                };
//                server.setReceive(new Conned.IReceive() {
//                    @Override
//                    public void invoke(Object msg) {
//                        try {
//                            System.out.println("服务器接受的消息: " + msg);
//                            String[] con = msg.toString().split("&");
//                            String act = con[0];
//                            String next = con[1];
//                            String yes = con[2];
//
//                            if ("f".equals(act)    ){
//                                FindTkt f = new FindTkt();
//                                String sql = f.find(next);
//                                DateTime dt = DateTime.Now();
//                                sql = "-- 时间" + dt + "\r\n" + sql;
//                                System.out.println(sql);
//                                FileOperate.createFile(getPath() + "生成的语句.txt", sql);
//
//
//                                System.out.println("请输入\"Y\"或数字\"0\"，更新数据库 其他任何字符跳过。");
//                                if (!f.tag_r) {
//                                    System.out.println("注意票状态不是退票状态，谨慎操作");
//                                }
//
//                                if (yes.equals("y") || yes.equals("Y")) {
//                                    MySql mySql = new MySql();
//                                    mySql.exeU(f.sql_grossrefund_amount);
//                                    mySql.exeU(f.sql_refundtickettax);
//                                    mySql.exeU(f.sql_ticketstatus);
//                                    mySql.exeU(f.sql_dedu);
//                                    mySql.exeU(f.sql_realrefundticketfee);
//                                    mySql.exeU(f.sql_rfnb);
//                                    mySql.exeU(f.sql_rtime);
//                                    mySql.close();
//                                    System.out.println("数据更新完成");
//                                    System.out.println();
//                                }
//                                System.out.println();
//                            }
//                        }catch (Exception e){
//
//                        }
//
//                    }
//                });
                server.Conn();
            }
        }).start();
    }


}
