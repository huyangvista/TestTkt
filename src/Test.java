import vdll.net.socket.Client;
import vdll.net.socket.Conned;
import vdll.net.socket.Server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;

/**
 * Created by Hocean on 2017/5/11.   1365676089
 */
public class Test {
    private JButton button1;
    private JPanel panel;
    private JButton button2;
    private JButton button3;
    private JButton button4;
    private JTextField textField1;
    private JTextArea textArea1;
    private JPanel buttons;
    private JButton 搜索Button;
    private JButton 更新数据Button;
    private JScrollPane scrollPane1;
    private JPanel panelText;

    public Server server;
    public Client client;
    public Client clientVives;



    public Test() {
        textArea1.setLineWrap(true);        //激活自动换行功能
        textArea1.setWrapStyleWord(true);            // 激活断行不断字功能</strong>

        panelText.setPreferredSize(new Dimension(300, 500));//关键代码,设置JPanel的大小

        button1.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                server = new Server();
                server.setReceive(new Conned.IReceive() {
                    @Override
                    public void invoke(Object msg) {
                        System.out.println("服务器接受的消息" + msg);
                    }
                });
                server.Conn();
            }
        });
        button2.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                client = new Client();
                client.setReceive(new Conned.IReceive() {
                    @Override
                    public void invoke(Object msg) {
                        //System.out.println("客户收到" + msg);
                        textArea1.append(msg.toString());
                    }
                });
                client.Conn();
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

                textArea1.setText("");
                client.conned.send(textField1.getText());

            }
        });

        搜索Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String ins = textField1.getText();
                if (ins.length() < 5) {
                    JOptionPane.showMessageDialog(null, "为了快速检索，请输入大于5位的文件名或票号!", "系统信息", JOptionPane.WARNING_MESSAGE);
                    return;
                }
                搜索Button.setEnabled(false);
                搜索Button.setText("搜索中...");
                if (client != null )
                {
                    client.Close();
                    client = null;
                }
                Client client = conn();
                textArea1.setText("");
                client.conned.send("find&&" + ins + "&&0");
            }
        });
        更新数据Button.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int option = JOptionPane.showConfirmDialog(null, "请检查票号无误后，点击确定。", "确定更新？", JOptionPane.YES_NO_OPTION,
                        JOptionPane.WARNING_MESSAGE, null);
                switch (option) {
                    case JOptionPane.YES_NO_OPTION:

                        更新数据Button.setEnabled(false);
                        更新数据Button.setText("更新中...");

                        if (clientVives != null)
                        {
                            clientVives.Close();
                            clientVives = null;
                        }
                        Client clientVives = ConnVives();
                        clientVives.conned.send("updateVives&&"+ textArea1.getText().replace("\r\n","@@@")+"&&0");
                        break;
                    case JOptionPane.NO_OPTION:
                        break;
                }

            }
        });

        final boolean[] textboxHasText = {false};//判断输入框是否有文本
        textField1.addFocusListener(new FocusAdapter() {
            @Override
            public void focusGained(FocusEvent e) {
                if (textboxHasText[0] == false)
                {
                    textField1.setText("");
                }
                textField1.setForeground(Color.black);//textBox1.ForeColor = Color.Black;
                super.focusGained(e);
            }

            @Override
            public void focusLost(FocusEvent e) {
                if ("".equals(textField1.getText()))
                {
                    textField1.setText("1365676089");
                    textField1.setForeground(Color.lightGray);                    //textBox1.ForeColor = Color.LightBlue;
                    textboxHasText[0] = false;
                }
                else
                    textboxHasText[0] = true;
                super.focusLost(e);
            }
        });
    }
    public Client conn(){
        if(client == null){
            client = new Client("123.57.83.39", 61234);
            client.setReceive(new Conned.IReceive() {
                @Override
                public void invoke(Object msg) {
                    //System.out.println("客户收到" + msg);
                    textArea1.append(msg.toString());
                    搜索Button.setEnabled(true);
                    搜索Button.setText("搜索");
                }
            });
        }
        if(client != null && client.conned  == null){
            client.Conn();
        }
        return client;
    }

    public Client ConnVives()
    {
        if (clientVives == null)
        {
            clientVives = new Client("123.206.23.166", 61234);
            //clientVives = new Client("127.0.0.1", 61234);
            clientVives.setReceive(new Conned.IReceive() {
                @Override
                public void invoke(Object msg) {
                    //System.out.println("客户收到" + msg);
                    textArea1.append(msg.toString());
                    更新数据Button.setEnabled(true);
                    更新数据Button.setText("更新数据");
                }
            });
        }
        if (clientVives != null && clientVives.conned == null)
        {
            clientVives.Conn();
        }
        return clientVives;
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
                if (text.server != null) text.server.Close();
                if (text.client != null) text.client.Close();
                super.windowClosing(e);
            }
        });
        frame.setSize(1000, 800);
        // frame.setLayout(null);
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here

    }
}
