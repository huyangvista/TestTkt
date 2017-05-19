package vdll.net.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Hocean on 2017/5/11.
 */
public class Server {
    private ServerSocket serverSocket;
    private List<Conned> listConned;
    private String ip;
    private int prot;
    private Thread thread;
    private boolean flag = true;
    private Conned.IReceive receive;


    public Server() {
        this("127.0.0.1", 61234);
    }

    public Server(String ip, int prot) {
        this.ip = ip;
        this.prot = prot;
        listConned = new ArrayList<Conned>();
    }

    public ServerSocket Conn() {
        //服务器IP地址  
        try {
            serverSocket = new ServerSocket(prot);
            thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    ListenClientConnect();
                }
            });
            thread.start();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return serverSocket;
    }

    /// <summary>  
    /// 监听客户端连接  
    /// </summary>  
    private void ListenClientConnect() {
        while (flag) {
            try {
                Socket clientSocket = serverSocket.accept();
                Conned conned = new Conned(clientSocket);
                conned.tag = "服务器使用";
                conned.setReceive(getReceive(conned));
                listConned.add(conned);
            } catch (Exception e) {
//                try {
//                    Thread.sleep(1000);
//                } catch (InterruptedException e1) {
//                    e1.printStackTrace();
//                }
                break;
            }

        }
    }

    public void Close() {
        try {
            flag = false;
            for (int i = listConned.size() - 1; i >= 0; i--) {
                listConned.get(i).close();
            }
            if (serverSocket != null) serverSocket.close();
        } catch (Exception e) {

        }

    }


    public List<Conned> getListConned() {
        return listConned;
    }

    public void setListConned(List<Conned> listConned) {
        this.listConned = listConned;
    }

    /**
     *     重写此方法可以为用户独立设置侦听
     */
    public Conned.IReceive getReceive(Conned conned) {
        return receive;
    }

    public void setReceive(Conned.IReceive receive) {
        this.receive = receive;
    }

}
