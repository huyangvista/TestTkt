package vdll.net.socket;


import java.io.*;
import java.net.Socket;

/**
 * Created by Hocean on 2017/5/11.
 */
public class Conned {
    public String tag = "";
    public Socket socket;
    private Thread thread;
    private boolean isConned = false; //连接状态
    private boolean flagReceive = true; //连接状态

    private BufferedReader in;
    private BufferedWriter out;
    private IReceive receive;

    public interface IReceive {
        public void invoke(Object msg);
    }

    public Conned(Socket socket) {
        this.socket = socket;
        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream(),"utf-8"));
            out = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream(),"utf-8"));
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    while (flagReceive) {
                        try {
                            String ins = in.readLine();
                            if(ins != null){
                                if(receive != null){
                                    receive.invoke(ins + "\r\n");
                                }
                                //System.out.println(tag + ins);
                            }else{
                                Thread.sleep(1000);
                            }
                        } catch (Exception e) {
                            try {   //等待处理连接强制丢失
                                close();
                                Thread.sleep(1000);
                            } catch (Exception e1) {

                            }
                        }
                    }
                }
            });
            thread.start();
        } catch (Exception e) {

        }
        isConned = true;
    }

    public void send(Object msg) {
        try {
            out.write(msg.toString());
            out.newLine();
            out.flush();
        } catch (Exception e) {

        }
    }

    /// <summary>
    /// 等待 完善
    /// </summary>
    /// <param name="msg"></param>
    public void anySend(Object msg) {
        send(msg);
    }


    /// <summary>
    /// 释放全部资源后
    /// </summary>
    public void close() {
        try {
            flagReceive = false;
            if (socket != null) socket.close();
            if (in != null) in.close();
            if (out != null) out.close();
            //if(thread != null ) thread.stop();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    /// <summary>
    /// 端口状态
    /// </summary>
    /// <param name="port"></param>
    /// <returns></returns>
    public static boolean portEnable(int port) {
        return false;
    }



    public IReceive getReceive() {
        return receive;
    }

    public void setReceive(IReceive receive) {
        this.receive = receive;
    }
}
