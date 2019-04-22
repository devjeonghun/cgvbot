package main;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import util.Config;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.lang.invoke.MethodHandles;
import java.util.Base64;
import java.util.Base64.Decoder;

public class MainForm extends JFrame implements ActionListener {
    private static final Logger LOGGER = LoggerFactory.getLogger(MethodHandles.lookup().lookupClass());
    static MainForm instance;
    private Boolean flag = true;
    JTextPane logPane;
    SettingForm settingForm;
    Thread th1;

    public MainForm() {
        //Creating the Frame
        super("CGV Bot");
        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        this.setSize(600, 400);
        try {

            String base64 = "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";
            Decoder decoder = Base64.getDecoder();
            byte[] btDataFile = decoder.decode(base64);
            BufferedImage image = ImageIO.read(new ByteArrayInputStream(btDataFile));
            this.setIconImage(image);
        } catch (Exception e) {

        }


        //Creating the MenuBar and adding components
        JMenuBar mb = new JMenuBar();
        JMenu m1 = new JMenu("CGV");
        JMenu m2 = new JMenu("Help");
        mb.add(m1);
        mb.add(m2);
        JMenuItem m22 = new JMenuItem("Exit");
        m22.addActionListener(this);
        m1.add(m22);

        //Creating the panel at bottom and adding components
        JPanel panel = new JPanel(); // the panel is not visible in output
        JButton setting = new JButton("setting");
        JButton start = new JButton("start");
        JButton stop = new JButton("stop");
        setting.addActionListener(this);
        start.addActionListener(this);
        stop.addActionListener(this);
        panel.add(setting);
        panel.add(start);
        panel.add(stop);

        // Text Area at the Center
        JScrollPane jScrollPane = new JScrollPane();//스크롤팬 생성

        JPanel panel3 = new JPanel();//스크롤팬에 붙일 패널 생성

     /*   Dimension size = new Dimension();//사이즈를 지정하기 위한 객체 생성

        size.setSize(1000, 1000);//객체의 사이즈를 지정

        panel3.setPreferredSize(size);//사이즈 정보를 가지고 있는 객체를 이용해 패널의 사이즈 지정*/
        jScrollPane.setViewportView(panel3);//스크롤 팬 위에 패널을 올린다.

        panel3.setLayout(new BorderLayout(0, 0));
        logPane = new JTextPane();
        panel3.add(logPane);
        JLabel label2 = new JLabel("로그");
        JPanel panel2 = new JPanel();
        panel2.add(label2);

        //Adding Components to the frame.
        this.add(BorderLayout.SOUTH, panel);
        this.add(BorderLayout.NORTH, mb);
        this.add(BorderLayout.CENTER, jScrollPane);
        this.add(BorderLayout.WEST, panel2);
        this.setVisible(true);

        settingForm = new SettingForm(this, "setting");
    }

    public static JTextPane getTextPane() {
        return getInstance().logPane;
    }

    public static MainForm getInstance() {
        if (instance == null) {
            instance = new MainForm();
        }
        return instance;
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (e.getActionCommand()) {
                case "setting":
                    settingForm.setVisible(true);
                    break;
                case "start":
                    flag = true;
                    start();
                    break;
                case "stop":
                    stop();
                    break;
                case "Exit":
                    String command[] = new String[3];
                    command[0] = "cmd.exe";
                    command[1] = "taskkill";
                    command[2] =  "/im chromedriver.exe /F";
                    try {
                        Runtime.getRuntime().exec(command);
                    } catch (IOException er) {
                        er.printStackTrace();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception er) {
            er.printStackTrace();
        }
    }


    public void start() throws Exception {
        th1 = new CgvController(Integer.parseInt(Config.properties.getProperty("VIEW_TYPE")));
        ((CgvController) th1).flag = true;
        th1.start();
    }

    public void stop() {
        ((CgvController) th1).flag = false;
        th1.interrupt();
    }
}
