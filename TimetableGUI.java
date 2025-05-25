package timeGUI;

import javax.swing.*;
import java.awt.*;

public class TimetableGUI extends JFrame {
    private JPanel timetablePanel;
    private GridBagConstraints gbc;
    private JButton addButton;

    public TimetableGUI() {
        try {
            UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        setTitle("2025 시간표");
        setSize(1000, 1000);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setLayout(new BorderLayout());

        timetablePanel = new JPanel(new GridBagLayout());
        timetablePanel.setBackground(Color.WHITE);
        gbc = new GridBagConstraints();
        gbc.fill = GridBagConstraints.BOTH;
        gbc.weightx = 1;
        gbc.weighty = 1;

        JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        addButton = new JButton("과목 추가");
        addButton.addActionListener(e -> showAddSubjectDialog());
        topPanel.add(addButton);
        add(topPanel, BorderLayout.NORTH);

        String[] days = {"월", "화", "수", "목", "금"};
        String[] times = {"9시", "10시", "11시", "12시", "13시", "14시", "15시", "16시", "17시"};

        gbc.gridx = 0;
        gbc.gridy = 0;
        timetablePanel.add(new JLabel(""), gbc);

        for (int i = 0; i < days.length; i++) {
            gbc.gridx = i + 1;
            gbc.gridy = 0;
            JLabel label = new JLabel(days[i], SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            timetablePanel.add(label, gbc);
        }

        for (int i = 0; i < times.length; i++) {
            int baseY = i * 2 + 1;
            gbc.gridx = 0;
            gbc.gridy = baseY;
            gbc.gridheight = 2;
            JLabel label = new JLabel(times[i], SwingConstants.CENTER);
            label.setBorder(BorderFactory.createLineBorder(Color.GRAY));
            timetablePanel.add(label, gbc);

            gbc.gridheight = 1;
            for (int j = 0; j < days.length; j++) {
                for (int k = 0; k < 2; k++) {
                    gbc.gridx = j + 1;
                    gbc.gridy = baseY + k;
                    JPanel cell = new JPanel();
                    cell.setPreferredSize(new Dimension(120, 40));
                    cell.setBorder(BorderFactory.createLineBorder(Color.LIGHT_GRAY));
                    timetablePanel.add(cell, gbc);
                }
            }
        }

        // 고정된 과목
        addSubject("시스템프로그래밍", "류은정", "IT대학5호관(IT8)", 2, 1, 8, new Color(204, 255, 255));
        addSubject("자바프로그래밍", "정창수", "IT대학5호관(IT8)", 3, 1, 8, new Color(204, 229, 255));
        addSubject("알고리즘실습", "배준현", "IT대학5호관(IT8)", 4, 1, 8, new Color(255, 255, 204));
        addSubject("물리학Ⅰ", "김창독", "IT대학5호관(IT8)", 1, 4, 3, new Color(255, 204, 204));
        addSubject("물리학Ⅰ", "김창독", "IT대학5호관(IT8)", 5, 1, 3, new Color(255, 204, 204));
        addSubject("확률및통계", "김진욱", "IT대학5호관(IT8)", 1, 10, 3, new Color(255, 230, 230));
        addSubject("확률및통계", "김진욱", "IT대학5호관(IT8)", 3, 13, 3, new Color(255, 230, 230));
        addSubject("컴퓨터구조", "김명석", "IT대학5호관(IT8)", 2, 10, 3, new Color(230, 255, 230));
        addSubject("컴퓨터구조", "김명석", "IT대학5호관(IT8)", 4, 13, 3, new Color(230, 255, 230));

        add(timetablePanel, BorderLayout.CENTER);
        setVisible(true);
    }

    private void showAddSubjectDialog() {
        JTextField nameField = new JTextField();
        JTextField profField = new JTextField();
        JTextField placeField = new JTextField();
        JComboBox<String> dayBox = new JComboBox<>(new String[]{"월", "화", "수", "목", "금"});
        JTextField startHourField = new JTextField();
        JTextField endHourField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1));
        panel.add(new JLabel("과목명:"));
        panel.add(nameField);
        panel.add(new JLabel("교수명:"));
        panel.add(profField);
        panel.add(new JLabel("강의실:"));
        panel.add(placeField);
        panel.add(new JLabel("요일:"));
        panel.add(dayBox);
        panel.add(new JLabel("시작 시각 (예: 9):"));
        panel.add(startHourField);
        panel.add(new JLabel("종료 시각 (예: 11):"));
        panel.add(endHourField);

        int result = JOptionPane.showConfirmDialog(this, panel, "과목 추가", JOptionPane.OK_CANCEL_OPTION);
        if (result == JOptionPane.OK_OPTION) {
            try {
                String name = nameField.getText();
                String prof = profField.getText();
                String place = placeField.getText();
                int day = dayBox.getSelectedIndex() + 1;
                int startHour = Integer.parseInt(startHourField.getText());
                int endHour = Integer.parseInt(endHourField.getText());

                int row = (startHour - 9) * 2 + 1;
                int height = (endHour - startHour) * 2;

                Color color = new Color((int)(Math.random()*200)+55, (int)(Math.random()*200)+55, (int)(Math.random()*200)+55);
                addSubject(name, prof, place, day, row, height, color);
                timetablePanel.revalidate();
                timetablePanel.repaint();
            } catch (Exception ex) {
                JOptionPane.showMessageDialog(this, "입력 오류: 숫자 형식 또는 시간 범위를 확인하세요.", "오류", JOptionPane.ERROR_MESSAGE);
            }
        }
    }

    private void addSubject(String name, String prof, String place, int col, int row, int height, Color color) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.gridheight = height;

        JButton subject = new JButton("<html><b>" + name + "</b><br>" + prof + "<br>" + place + "</html>");
        subject.setOpaque(true);
        subject.setContentAreaFilled(true);
        subject.setBorderPainted(true);
        subject.setFocusPainted(false);
        subject.setBackground(color);
        subject.setForeground(Color.BLACK);
        subject.setFont(new Font("Dialog", Font.BOLD, 13));
        subject.setPreferredSize(new Dimension(120, height * 40));

        timetablePanel.add(subject, gbc);
        timetablePanel.setComponentZOrder(subject, 0);

        gbc.gridheight = 1;
    }

    public static void main(String[] args) {
        UIManager.put("Button.foreground", Color.BLACK);
        SwingUtilities.invokeLater(TimetableGUI::new);
    }
}
