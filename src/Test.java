import vdll.net.socket.Client;
import vdll.net.socket.Conned;
import vdll.net.socket.Server;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

/**
 * Created by Hocean on 2017/5/11.
 */
public class Test {
    private JButton button1;
    private JPanel panel;
    private JButton button2;
    private JButton button3;
    private JButton button4;

    public Server server ;
    public Client client ;

    public Test() {

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server = new Server();
                server.Conn();
                server.setReceive(new Conned.IReceive() {
                    @Override
                    public void invoke(Object msg) {
                        System.out.println("服务器接受的消息" + msg);
                    }
                });
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client = new Client();
                client.Conn();
                client.setReceive(new Conned.IReceive() {
                    @Override
                    public void invoke(Object msg) {
                        System.out.println("客户收到" + msg);
                    }
                });
            }
        });
        button3.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server.Close();
                client.Close();
            }
        });
        button4.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client.conned.send("0:0:0");

            }
        });
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Test");
        final Test text = new Test();
        frame.setContentPane(text.panel);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.pack();
        frame.setVisible(true);
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                text.server.Close();
                text.client.Close();
                super.windowClosing(e);
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
