import javax.swing.*;
import java.awt.*;

public class TimetableGUI extends JFrame {
    private JPanel timetablePanel;
    private GridBagConstraints gbc;
    private JButton addButton;

    public TimetableGUI() {
        // 시스템 Look and Feel 설정 ( os에 맞게 디자인)
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

        // 상단 패널: 제목 + 과목 추가 버튼
        JPanel topPanel = new JPanel(new BorderLayout());
        topPanel.setBorder(BorderFactory.createEmptyBorder(10, 20, 10, 20));
        topPanel.setBackground(new Color(245, 245, 245));

        JLabel titleLabel = new JLabel("📅 나의 시간표");
        titleLabel.setFont(new Font("SansSerif", Font.BOLD, 22));
        topPanel.add(titleLabel, BorderLayout.WEST);

        // 과목 추가 버튼 디자인: 둥근 버튼 효과
        addButton = new JButton("➕ 과목 추가") {
            @Override
            protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getBackground());
                g2.fillRoundRect(0, 0, getWidth(), getHeight(), 12, 12); // 둥근 사각형 배경
                super.paintComponent(g);
                g2.dispose();
            }
        };
        addButton.setFocusPainted(false);
        addButton.setBackground(new Color(100, 149, 237)); // 연한 파란색 배경
        addButton.setForeground(Color.WHITE);
        addButton.setFont(new Font("SansSerif", Font.BOLD, 14));
        addButton.setContentAreaFilled(false); // 기본 배경 제거
        addButton.setBorderPainted(false); // 기본 테두리 제거
        addButton.setPreferredSize(new Dimension(120, 40));
        addButton.setCursor(new Cursor(Cursor.HAND_CURSOR));
        addButton.addActionListener(e -> showAddSubjectDialog());
        topPanel.add(addButton, BorderLayout.EAST);

        add(topPanel, BorderLayout.NORTH);

        String[] days = {"월", "화", "수", "목", "금"};
        String[] times = {"9시", "10시", "11시", "12시", "13시", "14시", "15시", "16시", "17시"};

        // 요일 헤더
        gbc.gridx = 0;
        gbc.gridy = 0;
        timetablePanel.add(new JLabel(""), gbc);
        for (int i = 0; i < days.length; i++) {
            gbc.gridx = i + 1;
            gbc.gridy = 0;
            JLabel label = new JLabel(days[i], SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.BOLD, 14));
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
            timetablePanel.add(label, gbc);
        }

        // 시간 및 셀 배치
        for (int i = 0; i < times.length; i++) {
            int baseY = i * 2 + 1;
            gbc.gridx = 0;
            gbc.gridy = baseY;
            gbc.gridheight = 2;
            JLabel label = new JLabel(times[i], SwingConstants.CENTER);
            label.setFont(new Font("SansSerif", Font.PLAIN, 13));
            label.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1, Color.LIGHT_GRAY));
            timetablePanel.add(label, gbc);

            gbc.gridheight = 1;
            for (int j = 0; j < days.length; j++) {
                for (int k = 0; k < 2; k++) {
                    gbc.gridx = j + 1;
                    gbc.gridy = baseY + k;
                    JPanel cell = new JPanel();
                    cell.setBackground(Color.WHITE);
                    cell.setBorder(BorderFactory.createLineBorder(new Color(230, 230, 230)));
                    cell.setPreferredSize(new Dimension(120, 40));
                    timetablePanel.add(cell, gbc);
                }
            }
        }

        // 과목 추가
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

    /**
     * 과목 추가 입력 다이얼로그
     */
    private void showAddSubjectDialog() {
        JTextField nameField = new JTextField();
        JTextField profField = new JTextField();
        JTextField placeField = new JTextField();
        JComboBox<String> dayBox = new JComboBox<>(new String[]{"월", "화", "수", "목", "금"});
        JTextField startHourField = new JTextField();
        JTextField endHourField = new JTextField();

        JPanel panel = new JPanel(new GridLayout(0, 1, 5, 5));
        panel.setPreferredSize(new Dimension(300, 250));
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

    /**
     * 시간표에 과목 블록 추가
     */
    private void addSubject(String name, String prof, String place, int col, int row, int height, Color color) {
        gbc.gridx = col;
        gbc.gridy = row;
        gbc.gridheight = height;

        JButton subject = new JButton("<html><b>" + name + "</b><br>" + prof + "<br>" + place + "</html>");
        subject.setOpaque(true);
        subject.setBackground(color);
        subject.setForeground(Color.BLACK);
        subject.setFont(new Font("SansSerif", Font.PLAIN, 13));
        subject.setBorder(BorderFactory.createLineBorder(Color.GRAY));
        subject.setFocusPainted(false);
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
