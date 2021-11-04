# JavaChat

> 大一下学期的java期末课程设计-多人聊天工具，分享一下

# 课设要求
>**多人聊天工具**
> 服务器要求1:能够看到所有在线用户(25%)
> 服务器要求2:能够强制用户下线(25%)
> 客户端要求1:能够看到所有在线用户(25%)
> 客户端要求2:能够向某个用户发送消息(25%)

# 相关知识点
**1．服务端能够看到所有在线用户**
服务端继承了JFrame，实现可视化，通过socket实现服务端与客户端的连接，服务端每接收一个连接，把传进来的用户名和对应的socket连接封装成一个User对象，把User对象存进一个ArrayList<User>的用户列表并把User对象通过取用户名方法取得用户名存进一个ArrayList<String>的用户名列表，添加一个JPanel组件，将ArrayList<String>中的内容通过循环显示JPanel中并布局在窗体的右边，在每当有人上线或者下线，刷新JPanel组件。

**2．服务端能够强制用户下线**
创建一个布局在窗体的下方的JPanel，在此JPanel中分别添加JLabel用于显示提示文字，添加JTextField用于获取服务端想要强制用户下线的ID，添加JButton用于绑定强制用户下线的事件监听，事件监听中将获取的JTextField的内容与用户名列表进行逐一匹配，匹配上则创建JSON格式的键值对对象，通过用户列表循环广播告知其他用户，并在用户列表和用户名列表中分别删除该用户信息。

**3．客户端能够看到所有在线用户**
客户端继承了JFrame，实现可视化，添加了一个布局在窗口右边的JPanel，把从服务端接收到的用户名列表中的信息放进去。

**4．客户端要求能够向某个用户发送消息**
客户端私发消息通过在消息后面加入-和目标用户名，传给服务端，服务端截取目标用户名，在用户名列表中判断是否存在此人，有则判断是否是私发，私发则向目标用户发送消息，没有则向全部用户发送消息。
         
**5．运用JDBC实现持久化存储用户信息**
数据库连接池运用了阿里巴巴的durid，定义一个JDBCUtils类，提供静态代码块加载配置文件，初始化连接池对象，通过Spring框架的JDBCTemplate对象进行sql语句的执行，在UserDao中提供了登录和注册方法，登录方法运用queryForObject方法进行登录查询，如果查到返回一个User对象，查不到则返回空，注册方法直接插入新记录，此处建表语句中把用户名设置成了主键，保证了用户名的唯一性，注册失败有警告弹窗提示。
这里加了一个ChatTest类用于绕过数据库账号校验，可以直接进入客户端进行连接。
         
**6．使用JSONObject对象封装数据**
在数据的传输中运用了键值对的形式进行传输，客户端传输给服务端的数据包中，通过判断private键的值来确认是否私发，通过username键告知服务端客户端的用户名，通过msg键传输具体消息，服务端传输给客户端的数据包中，通过判断user_list键的值来确认在线用户及人数
         
**7．使用Maven构建管理项目**
项目中运用到了JDBC相关内容和JSONObject对象，导入了一些依赖jar包，其中仓库和配置文件都是用的idea默认配置。

# 类图
![请添加图片描述](https://img-blog.csdnimg.cn/767a9827552a4cc092f3ff1cb533b393.jpg?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzUwMjE2Mjcw,size_16,color_FFFFFF,t_70)
# 项目框架
![在这里插入图片描述](https://img-blog.csdnimg.cn/1333df0fc968410faa7080e3f7117591.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzUwMjE2Mjcw,size_16,color_FFFFFF,t_70)
# 核心代码
## 1.maven配置文件pom.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<project xmlns="http://maven.apache.org/POM/4.0.0"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd">
    <modelVersion>4.0.0</modelVersion>

    <groupId>org.example</groupId>
    <artifactId>MyChat</artifactId>
    <version>1.0-SNAPSHOT</version>

    <properties>
        <maven.compiler.source>15</maven.compiler.source>
        <maven.compiler.target>15</maven.compiler.target>
    </properties>

    <dependencies>
        <dependency>
            <groupId>net.sf.json-lib</groupId>
            <artifactId>json-lib</artifactId>
            <version>2.4</version>
            <classifier>jdk15</classifier>
        </dependency>
        <dependency>
            <groupId>com.alibaba</groupId>
            <artifactId>druid</artifactId>
            <version>1.2.3</version>
        </dependency>
        <dependency>
            <groupId>mysql</groupId>
            <artifactId>mysql-connector-java</artifactId>
            <version>5.1.45</version>
        </dependency>
        <dependency>
            <groupId>org.springframework</groupId>
            <artifactId>spring-jdbc</artifactId>
            <version>5.3.6</version>
        </dependency>
    </dependencies>

</project>
```

## 2.服务器端Server.java

```java
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.util.*;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import net.sf.json.JSONObject;


//继承JFrame实现可视化
public class Server extends JFrame{

    //用户列表，用于存放连接上的用户信息
    ArrayList<User> user_list = new ArrayList<>();
    //用户名列表，用于显示已连接上的用户
    ArrayList<String> username_list = new ArrayList<>();

    //消息显示区域
    JTextArea show_area = new JTextArea();
    //用户名显示区域
    JTextArea show_user = new JTextArea(10, 10);

    //socket的数据输出流
    DataOutputStream outputStream = null;
    //socket的数据输入流
    DataInputStream inputStream = null;

    //从主函数里面开启服务端
    public static void main(String[] args) {
        new Server();
    }

    //构造函数
    public Server() {

        //设置流式布局
        setLayout(new BorderLayout());
        //VERTICAL_SCROLLBAR_AS_NEEDED设置垂直滚动条需要时出现
        //HORIZONTAL_SCROLLBAR_NEVER设置水平滚动条不出现
        //创建信息显示区的画布并添加到show_area
        JScrollPane panel = new JScrollPane(show_area,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        //设置信息显示区标题
        panel.setBorder(new TitledBorder("信息显示区"));
        //布局到中央
        add(panel,BorderLayout.CENTER);
        //设置信息显示区为不可编辑
        show_area.setEditable(false);


        //创建用于显示用户的画布
        final JPanel panel_east = new JPanel();
        //添加流式布局
        panel_east.setLayout(new BorderLayout());
        //设置标题
        panel_east.setBorder(new TitledBorder("在线用户"));
        //在用户显示区添加show_uesr
        panel_east.add(new JScrollPane(show_user), BorderLayout.CENTER);
        //设置用户显示区域为不可编辑
        show_user.setEditable(false);
        //将显示用户的画布添加到整体布局的右侧
        add(panel_east, BorderLayout.EAST);

        //创建关于踢下线用户的画布
        final JPanel panel_south = new JPanel();
        //创建标签
        JLabel label = new JLabel("输入要踢下线用户的ID");
        //创建输入框
        JTextField out_area = new JTextField(40);
        //创建踢下线按钮
        JButton out_btn = new JButton("踢下线");
        //依次添加进画布
        panel_south.add(label);
        panel_south.add(out_area);
        panel_south.add(out_btn);
        //将踢下线用户的画布添加到整体布局的下侧
        add(panel_south,BorderLayout.SOUTH);

        //设置踢下线按钮的监听
        out_btn.addActionListener(e -> {
            try {
                //用于存储踢下线用户的名字
                String out_username;
                //从输入框中获取踢下线用户名
                out_username = out_area.getText().trim();
                //用于判断盖用户是否被踢下线
                boolean is_out=false;
                //遍历用户列表依次判断
                for (int i = 0; i < user_list.size(); i++){
                    //比较用户名，相同则踢下线
                    if(user_list.get(i).getUsername().equals(out_username)){
                        //获取被踢下线用户对象
                        User out_user = user_list.get(i);
                        //使用json封装将要传递的数据
                        JSONObject data = new JSONObject();
                        //封装全体用户名，广播至所有用户
                        data.put("user_list", username_list);
                        //广播的信息内容
                        data.put("msg", out_user.getUsername() + "被管理员踢出\n");
                        //服务端消息显示区显示相应信息
                        show_area.append(out_user.getUsername() + "被你踢出\n");
                        //依次遍历用户列表
                        for (User value : user_list) {
                            try {
                                //获取每个用户列表的socket连接
                                outputStream = new DataOutputStream(value.getSocket().getOutputStream());
                                //传递信息
                                outputStream.writeUTF(data.toString());
                            } catch (IOException ex) {
                                ex.printStackTrace();
                            }
                        }
                        //将被踢用户移出用户列表
                        user_list.remove(i);
                        //将被踢用户移出用户名列表
                        username_list.remove(out_user.getUsername());
                        //刷新在线人数
                        show_user.setText("人数有 " + username_list.size() + " 人\n");
                        //刷新在线用户
                        for (String s : username_list) {
                            show_user.append(s + "\n");
                        }
                        //判断踢出成功
                        is_out=true;
                        break;
                    }

                }
                //根据是否踢出成功弹出相应提示
                if(is_out){
                    JOptionPane.showMessageDialog(null,"踢下线成功","提示",
                            JOptionPane.WARNING_MESSAGE);
                }
                if(!is_out){
                    JOptionPane.showMessageDialog(null,"不存在用户","提示",
                            JOptionPane.WARNING_MESSAGE);
                }
                //重置输入框
                out_area.setText("");
            } catch(Exception ex) {
                ex.printStackTrace();
            }
        });

        //设置该窗口名
        setTitle("服务器 ");
        //引入图片
        BufferedImage img;
        try {
            //根据图片名引入图片
            img = ImageIO.read(Server.class.getResource("/a.jpg"));
            //设置其为该窗体logo
            setIconImage(img);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        //设置窗体大小
        setSize(700, 700);
        //设置窗体位置可移动
        setLocationRelativeTo(null);
        //设置窗体关闭方式
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        //设置窗体可见
        setVisible(true);

        //socket连接相关代码
        try {
            //开启socket服务器，绑定端口11111
            ServerSocket serverSocket = new ServerSocket(11111);
            //信息显示区打印服务器启动时间
            show_area.append("服务器启动时间 " + new Date() + "\n");
            //持续接收连接
            while (true) {
                //接收连接
                Socket socket = serverSocket.accept();
                //创建用户对象
                User user = new User();
                //判断是否连接上
                if (socket != null) {
                    //获取输入流
                    inputStream = new DataInputStream(socket.getInputStream());
                    //读取输入流
                    String json = inputStream.readUTF();
                    //创建信息对象
                    JSONObject data = JSONObject.fromObject(json);
                    //信息显示区打印用户上线
                    show_area.append("用户 " + data.getString("username") + " 在" + new Date() + "登陆系统"+"\n");
                    //创建新用户
                    user = new User();
                    //存储socket对象
                    user.setSocket(socket);
                    //获取输入流用户名
                    user.setUsername(data.getString("username"));
                    //添加进用户列表
                    user_list.add(user);
                    //添加进用户名列表
                    username_list.add(data.getString("username"));

                    //刷新在线人数
                    show_user.setText("人数有 " + username_list.size() + " 人\n");
                    //刷新在线用户
                    for (String s : username_list) {
                        show_user.append(s + "\n");
                    }

                }

                //封装信息对象
                JSONObject online = new JSONObject();
                //设置接收信息对象
                online.put("user_list", username_list);
                //设置信息内容
                online.put("msg", user.getUsername() + "上线了");
                //依次遍历，将信息广播给所有在线用户
                for (User value : user_list) {
                    //获取输出流
                    outputStream = new DataOutputStream(value.getSocket().getOutputStream());
                    //给所有用户输出上线信息
                    outputStream.writeUTF(online.toString());
                }

                //开启新线程，持续接收该socket信息
                new Thread(new ServerThread(socket)).start();

            }
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    //线程代码
    class ServerThread implements Runnable {
        //存放全局变量socket
        private final Socket socket;

        //构造函数，初始化socket
        public ServerThread(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                //获取输入流
                DataInputStream inputStream = new DataInputStream(socket.getInputStream());
                //持续接收信息
                while (true) {
                    //获取传递进来的信息
                    String json = inputStream.readUTF();
                    //封装成json格式
                    JSONObject data = JSONObject.fromObject(json);

                    //通过json里面的private判断是否私发
                    boolean is_private = false;
                    //私发处理
                    for (int i = 0; i < user_list.size(); i++) {
                        //找到私发对象
                        if (user_list.get(i).getUsername().equals(data.getString("private"))) {
                            //构建私发信息内容
                            String msg = data.getString("time") +"\n" + data.getString("username")
                                    + " 给你发了一条私密消息，其它用户看不到" + "\n"  + data.getString("msg");
                            //用该方法指定对象发送信息
                            send_msg(i, msg);

                            //将发送成功反馈给原用户
                            for (int j = 0; j < user_list.size(); j++) {
                                //找到发信息用户
                                if(user_list.get(j).getUsername().equals(data.getString("username"))){
                                    //构建反馈信息内容
                                    String msg2 = data.getString("time")+"\n你成功向"+user_list.get(i).getUsername()
                                            +"发送了一条私密消息\n" +data.getString("msg");
                                    //用该方法指定对象发送信息
                                    send_msg(j,msg2);
                                }
                            }
                            //将该操作打印到服务器监视窗
                            show_area.append(data.getString("username") +data.getString("time")+ "私发给"
                                    + data.getString("private") + ":\n" + data.getString("msg") + "\n");
                            //判断是私发
                            is_private = true;
                            break;
                        }
                    }
                    //非私发的情况
                    if (!is_private) {
                        //构建信息内容
                        String msg = data.getString("username") + " " + data.getString("time") + ":\n"
                                + data.getString("msg");
                        //添加到服务器显示
                        show_area.append(msg + "\n");
                        //依次发给所有在线用户
                        for (int i = 0; i < user_list.size(); ) {
                            send_msg(i, msg);
                            i++;
                        }
                    }

                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        //发送信息给指定用户的方法
        public void send_msg(int i, String msg) {
            //构建对象
            JSONObject data = new JSONObject();
            //封装信息
            data.put("user_list", username_list);
            data.put("msg", msg);
            //获取目标对象
            User user = user_list.get(i);
            try {
                //获取输出流
                outputStream = new DataOutputStream(user.getSocket().getOutputStream());
                //写信息
                outputStream.writeUTF(data.toString());
            } catch (IOException e) {
                //如果没有找到，则说明该用户已经下线
                User out_user = user_list.get(i);
                //重复删除操作
                user_list.remove(i);
                username_list.remove(out_user.getUsername());
                //重新构建信息
                JSONObject out = new JSONObject();
                out.put("user_list", username_list);
                out.put("msg", out_user.getUsername() + "下线了\n");
                //将其下线通知广播给所有用户
                for (User value : user_list) {
                    try {
                        outputStream = new DataOutputStream(value.getSocket().getOutputStream());
                        outputStream.writeUTF(out.toString());
                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                }
            }

        }
    }


}
```
~~代码太多了，懒得注释了，大家应该看得懂~~ 
## 3.客户端登录界面Client.java

```java
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;


public class Client extends JFrame{


    public static void main(String[] args) {
        new Client();
    }

    public Client(){
        setTitle("登陆界面");
        BufferedImage img;
        try {
            img = ImageIO.read(Server.class.getResource("/a.jpg"));
            setIconImage(img);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        setLayout(null);
        setSize(500,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(null);
        setResizable(false);

        JLabel username_label = new JLabel("用户名");
        username_label.setBounds(60,100,100,50);
        add(username_label);

        JLabel password_label = new JLabel("密码");
        password_label.setBounds(60,200,100,50);
        add(password_label);

        JTextField username_field = new JTextField();
        username_field.setBounds(110,100,300,50);
        add(username_field);

        JPasswordField password_field = new JPasswordField();
        password_field.setBounds(110,200,300,50);
        add(password_field);

        JButton login = new JButton("登陆");
        login.setBounds(130,300,100,50);
        add(login);
        JButton register = new JButton("注册");
        register.setBounds(280,300,100,50);
        add(register);

        setVisible(true);


        login.addActionListener(e -> {
            String username = username_field.getText();
            String password = String.valueOf(password_field.getPassword());
            if(username.length()!=0 && password.length()!=0){
                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                UserDao dao = new UserDao();
                User u = dao.login(user);
                if(u!=null){
                    setVisible(false);
                    new Chat(username);
                }else{
                    JOptionPane.showMessageDialog(null,"登录失败，账号或密码错误","提示",
                        JOptionPane.WARNING_MESSAGE);
                }
            }else {
                JOptionPane.showMessageDialog(null,"登录失败，账号或密码不能为空","提示",
                        JOptionPane.WARNING_MESSAGE);
            }
        });

        register.addActionListener(e -> {
            setVisible(false);
            new Register();
        });
    }

}

```
## 4.客户端注册界面Register.java

```java
import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.image.BufferedImage;
import java.io.IOException;


public class Register extends JFrame {
    public Register(){
        setTitle("注册界面");
        BufferedImage img;
        try {
            img = ImageIO.read(Server.class.getResource("/a.jpg"));
            this.setIconImage(img);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        setLayout(null);
        setSize(500,500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

        setLayout(null);
        setResizable(false);

        JLabel username_label = new JLabel("用户名");
        username_label.setBounds(60,50,100,50);
        add(username_label);

        JLabel password_label = new JLabel("密码");
        password_label.setBounds(60,150,100,50);
        add(password_label);

        JLabel password_label2 = new JLabel("请再次输入密码");
        password_label2.setBounds(20,250,100,50);
        add(password_label2);

        JTextField username_field = new JTextField();
        username_field.setBounds(110,50,300,50);
        add(username_field);

        JPasswordField password_field = new JPasswordField();
        password_field.setBounds(110,150,300,50);
        add(password_field);

        JPasswordField password_field2 = new JPasswordField();
        password_field2.setBounds(110,250,300,50);
        add(password_field2);


        JButton register_success = new JButton("注册");
        register_success.setBounds(130,350,100,50);
        add(register_success);

        JButton back = new JButton("返回");
        back.setBounds(280,350,100,50);
        add(back);

        setVisible(true);


        register_success.addActionListener(e -> {
            String username = username_field.getText();
            String password = String.valueOf(password_field.getPassword());
            String password2 = String.valueOf(password_field2.getPassword());
            System.out.println(password);
            System.out.println(password2);
            if(username.length()==0 || password.length()==0){
                JOptionPane.showMessageDialog(null,"注册失败，账号或密码不能为空","提示",
                        JOptionPane.WARNING_MESSAGE);
            }else if (!password.equals(password2)) {
                JOptionPane.showMessageDialog(null,"注册失败，前后密码不匹配","提示",
                        JOptionPane.WARNING_MESSAGE);
            }else{
                System.out.println();
                System.out.println(password);
                User user = new User();
                user.setUsername(username);
                user.setPassword(password);
                UserDao dao = new UserDao();
                int flag = dao.register(user);
                if(flag!=0){
                    JOptionPane.showMessageDialog(null,"注册成功，欢迎您登录","提示",
                            JOptionPane.WARNING_MESSAGE);

                }else{
                    //建表语句中设置了user为主键，重复则建表失败
                    JOptionPane.showMessageDialog(null,"注册失败，账号已经存在","提示",
                            JOptionPane.WARNING_MESSAGE);
                }
            }
        });

        back.addActionListener(e ->{
           setVisible(false);
           new Client();
        });


    }

}

```

## 5.客户端聊天界面Chat.java

```java
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.awt.*;
import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.TitledBorder;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;
public class Chat extends JFrame{

    JTextArea show_area = new JTextArea();
    JTextArea show_user = new JTextArea(10, 10);
    DataOutputStream outputStream;
    DataInputStream inputStream;
    String username;
    ArrayList<String> username_list = new ArrayList<>();
    boolean is_stop = false;

    public Chat(final String username) {
        this.username = username;
        
        final JPanel panel_south = new JPanel();
        panel_south.setLayout(new BorderLayout());
        panel_south.setBorder(new TitledBorder("写消息区，若私聊，在内容后添加（-用户名）"));
        JTextField send_area = new JTextField(40);
        panel_south.add(send_area, BorderLayout.CENTER);
        JButton send_btn = new JButton("发送");
        panel_south.add(send_btn,BorderLayout.EAST);
        add(panel_south, BorderLayout.SOUTH);

        send_btn.addActionListener(e -> {
            try {

                if (is_stop) {
                    show_area.append("你已被踢出，不能发送消息\n");
                    JOptionPane.showMessageDialog(null,"你已被踢出，不能发送消息，进程已经关闭","提示",
                            JOptionPane.WARNING_MESSAGE);
                    System.exit(0);
                } else {
                    SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                    String time = sdf.format(new Date());

                    String msg = send_area.getText().trim();

                    if (!msg.equals("")) {
                        String[] msg1 = msg.split("-");
                        JSONObject data = new JSONObject();
                        data.put("username", username);
                        data.put("msg", msg1[0]);
                        data.put("time", time);

                        try {
                            data.put("private", msg1[1]);

                        } catch (ArrayIndexOutOfBoundsException e1) {
                            data.put("private", "");
                        }
                        outputStream.writeUTF(data.toString());
                    }
                }
                send_area.setText("");
            } catch (Exception ex) {
                ex.printStackTrace();
            }
        });

        JScrollPane panel = new JScrollPane(show_area,ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED,
                ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        panel.setBorder(new TitledBorder("信息显示区"));
        add(panel,BorderLayout.CENTER);
        show_area.setEditable(false);
        
        final JPanel panel_east = new JPanel();
        panel_east.setLayout(new BorderLayout());
        panel_east.setBorder(new TitledBorder("在线用户"));
        panel_east.add(new JScrollPane(show_user), BorderLayout.CENTER);
        show_user.setEditable(false);
        add(panel_east, BorderLayout.EAST);

        setTitle("用户  " + username);
        BufferedImage img;
        try {
            img = ImageIO.read(Server.class.getResource("/a.jpg"));
            this.setIconImage(img);
        } catch (IOException exception) {
            exception.printStackTrace();
        }
        setSize(500, 500);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setVisible(true);

        JSONObject data = new JSONObject();
        data.put("username", username);
        data.put("msg", null);

        try {
            Socket socket = new Socket("127.0.0.1", 11111);
            inputStream = new DataInputStream(socket.getInputStream());
            outputStream = new DataOutputStream(socket.getOutputStream());
            outputStream.writeUTF(data.toString());
            new Thread(new Read()).start();
        } catch (IOException e) {
            show_area.append("服务器无响应");
            JOptionPane.showMessageDialog(null,"服务器无响应","提示",
                    JOptionPane.WARNING_MESSAGE);
        }
    }

    public class Read implements Runnable {
        @Override
        public void run() {
            try {
                while (true) {
                    String json = inputStream.readUTF();
                    JSONObject data = JSONObject.fromObject(json);
                    String msg = data.getString("msg");
                    if (msg.contains("踢出") && msg.contains(username)) {
                        is_stop = true;
                        show_area.append(username + ",你已经被踢出群聊\n");
                        JOptionPane.showMessageDialog(null,"你已经被踢出群聊","提示",
                                JOptionPane.WARNING_MESSAGE);
                        System.exit(0);
                    } else {
                        show_area.append(msg + "\n");
                        show_area.selectAll();
                        username_list.clear();
                        JSONArray jsonArray = data.getJSONArray("user_list");
                        for (Object o : jsonArray) {
                            username_list.add(o.toString());
                        }
                        show_user.setText("人数有 " + jsonArray.size() + " 人\n");
                        for (String s : username_list) {
                            show_user.append(s + "\n");
                        }
                    }

                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

}

```
## 6.用户实体User.java

```java
import java.net.Socket;

public class User {
    private String username;
    private String password;
    private Socket socket;

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public Socket getSocket() {
        return socket;
    }

    public void setSocket(Socket socket) {
        this.socket = socket;
    }
}

```
## 7.JDBC工具类

```java
import com.alibaba.druid.pool.DruidDataSourceFactory;

import javax.sql.DataSource;
import java.io.InputStream;
import java.util.Properties;

public class JDBCUtils {
    private static DataSource ds;

    static {
        try {
            Properties pro = new Properties();
            InputStream is = JDBCUtils.class.getClassLoader().getResourceAsStream("druid.properties");
            pro.load(is);
            ds = DruidDataSourceFactory.createDataSource(pro);
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public static DataSource getDataSource(){
        return ds;
    }


}

```
## 8.UserDao.java连接数据库

```java
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.BeanPropertyRowMapper;
import org.springframework.jdbc.core.JdbcTemplate;



public class UserDao {
    private final JdbcTemplate template = new JdbcTemplate(JDBCUtils.getDataSource());


    public User login(User login_user) {
        try {
            //编写sql
            String sql = "select * from user where username = ? and password = ?";

            User user = template.queryForObject(sql,
                    new BeanPropertyRowMapper<User>(User.class),
                    login_user.getUsername(), login_user.getPassword());
            return user;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return null;
        }
    }

    public int register(User register_user) {
        try {
            String sql = "insert into user values (null ,?,?)";
            int count = template.update(sql,register_user.getUsername(),register_user.getPassword());
            return count;
        } catch (DataAccessException e) {
            e.printStackTrace();
            return 0;
        }
    }

}

```
# 运行结果
![请添加图片描述](https://img-blog.csdnimg.cn/6140b7fe3cb24a3da8e1a7a02c6f5fe3.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzUwMjE2Mjcw,size_16,color_FFFFFF,t_70)
![请添加图片描述](https://img-blog.csdnimg.cn/b72b278996c14a3f8f7b945ed07576d1.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzUwMjE2Mjcw,size_16,color_FFFFFF,t_70)![请添加图片描述](https://img-blog.csdnimg.cn/3e09faed72b647d6831ec37b7aeee689.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzUwMjE2Mjcw,size_16,color_FFFFFF,t_70)![请添加图片描述](https://img-blog.csdnimg.cn/37722ca4b52f41ceaab0e691e0dfedf8.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzUwMjE2Mjcw,size_16,color_FFFFFF,t_70)


![请添加图片描述](https://img-blog.csdnimg.cn/109dbbdac3274bf7a0d9fe73ed89e44e.png?x-oss-process=image/watermark,type_ZmFuZ3poZW5naGVpdGk,shadow_10,text_aHR0cHM6Ly9ibG9nLmNzZG4ubmV0L3FxXzUwMjE2Mjcw,size_16,color_FFFFFF,t_70)



