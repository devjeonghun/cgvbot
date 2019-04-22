package main;

import com.google.common.collect.ImmutableMap;
import util.Config;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Properties;

public class SettingForm extends JDialog implements ActionListener {
    private  JLabel fileLabel;
    private JLabel labels[];
    private JTextField fields[];
    private Map<String, String> names = ImmutableMap.<String, String>builder()
            .put("아이디", "CGV_ID")
            .put("비밀번호", "CGV_PW")
            .put("영화제목", "MOVIE_NAME")
            .put("검색일(YYYYMMDD)", "SEARCH_DATE")
            .put("시작(HH:24mm)", "SEARCH_STR_TIME")
            .put("상영종료(HH:24mm)", "SEARCH_END_TIME")
            .put("상영타입(4DX,2D,IMAX)", "MOVIE_TYPE")
            .put("좌석검색타입(1)", "SEAT_SELECT_TYPE")
            .put("좌석수", "SEAT_NUM")
            .put("좌석타입(Prime석,Economy석,Standard석)", "SEAT_TYPE")
            .put("재시도시간", "RETRY_TIME")
            .put("화면표시", "VIEW_TYPE")
            .build();
    private JButton setBtn,setFile,openFile;
    private JPanel panelCenter, panelSouth;
    JFileChooser fileChooser;
    private int size = 0;
    private String authFile = Config.SYSTEM_ROOT + "/config.properties";

    public SettingForm(JFrame frame, String title) {
        super(frame, title);
        labels = new JLabel[names.size()];
        fields = new JTextField[names.size()];
        names.forEach((k, v) -> {
            labels[size] = new JLabel(k);
            fields[size] = new JTextField(Config.properties.getProperty(v));
            size++;
        });

        panelCenter = new JPanel();
        panelCenter.setLayout(new GridLayout(size, 2));
        for (int i = 0; i < size; i++) {
            panelCenter.add(labels[i]);
            panelCenter.add(fields[i]);
        }

        openFile = new JButton("설정 파일 열기");
        setFile = new JButton("설정 파일 생성");
        setBtn = new JButton("설정 완료");
        fileLabel = new JLabel(authFile);
        panelSouth = new JPanel();

        panelSouth.add(setFile);
        panelSouth.add(fileLabel);
        panelSouth.add(openFile);
        panelSouth.add(setBtn);
        add(panelCenter, "Center");
        add(panelSouth, "South");
        setBounds(300, 300, 570, 250);
        setBtn.addActionListener(this);
        setFile.addActionListener(this);
        openFile.addActionListener(this);
    }
    public void updateText(){
        fields[0].setText(Config.properties.getProperty("CGV_ID"));
        fields[1].setText(Config.properties.getProperty("CGV_PW"));
        fields[2].setText(Config.properties.getProperty("MOVIE_NAME"));
        fields[3].setText(Config.properties.getProperty("SEARCH_DATE"));
        fields[4].setText(Config.properties.getProperty("SEARCH_STR_TIME"));
        fields[5].setText(Config.properties.getProperty("SEARCH_END_TIME"));
        fields[6].setText(Config.properties.getProperty("MOVIE_TYPE"));
        fields[7].setText(Config.properties.getProperty("SEAT_SELECT_TYPE"));
        fields[8].setText(Config.properties.getProperty("SEAT_NUM"));
        fields[9].setText(Config.properties.getProperty("SEAT_TYPE"));
        fields[10].setText(Config.properties.getProperty("RETRY_TIME"));
        fields[11].setText(Config.properties.getProperty("VIEW_TYPE"));
    }

    public void saveAuth(String authFile){
        Config.authUpdate("CGV_ID", fields[0].getText(),authFile);
        Config.authUpdate("CGV_PW", fields[1].getText(),authFile);
        Config.authUpdate("MOVIE_NAME", fields[2].getText(),authFile);
        Config.authUpdate("SEARCH_DATE", fields[3].getText(),authFile);
        Config.authUpdate("SEARCH_STR_TIME", fields[4].getText(),authFile);
        Config.authUpdate("SEARCH_END_TIME", fields[5].getText(),authFile);
        Config.authUpdate("MOVIE_TYPE", fields[6].getText(),authFile);
        Config.authUpdate("SEAT_SELECT_TYPE", fields[7].getText(),authFile);
        Config.authUpdate("SEAT_NUM", fields[8].getText(),authFile);
        Config.authUpdate("SEAT_TYPE", fields[9].getText(),authFile);
        Config.authUpdate("RETRY_TIME", fields[10].getText(),authFile);
        Config.authUpdate("VIEW_TYPE", fields[11].getText(),authFile);
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        try {
            switch (e.getActionCommand()) {
                case "설정 완료":
                    saveAuth(authFile);
                    this.setVisible(false);
                    break;
                case "설정 파일 생성":
                    fileChooser = new JFileChooser(Config.SYSTEM_ROOT);
                    if(fileChooser.showSaveDialog(null) == JFileChooser.APPROVE_OPTION){
                        authFile = fileChooser.getSelectedFile().toString();
                        fileLabel.setText(fileChooser.getSelectedFile().toString());
                        saveAuth(fileChooser.getSelectedFile().toString());
                        updateText();
                    }
                    break;
                case "설정 파일 열기":
                    fileChooser = new JFileChooser(Config.SYSTEM_ROOT);
                    if(fileChooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION){
                        authFile = fileChooser.getSelectedFile().toString();
                        fileLabel.setText(fileChooser.getSelectedFile().toString());
                        saveAuth(fileChooser.getSelectedFile().toString());
                        updateText();
                    }
                    break;
                default:
                    break;
            }
        } catch (Exception er) {
            er.printStackTrace();
        }
    }
}
