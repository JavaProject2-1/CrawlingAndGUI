package basicWeb;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class KnuLectureCrawlerGUI extends JFrame {
    JTextField yearField;
    JTextField semesterField;
    JTextField gradeField;
    JButton searchBtn, saveBtn;
    JTable resultTable;
    DefaultTableModel tableModel;
    JLabel statusLabel;
    JLabel yearLabel, semesterLabel, gradeLabel;

    public KnuLectureCrawlerGUI(int sequence) {
        setTitle("KNU 강의 계획서 크롤러");
        setSize(1200, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        RatingLoader.loadRatings();
        initComponents();
        Language.applyKoreanLabels(this, sequence);
    }

    private void initComponents() {
        JPanel topPanel = new JPanel();

        // 개설연도
        yearLabel = new JLabel();
        topPanel.add(yearLabel);
        yearField = new JTextField("", 6);
        topPanel.add(yearField);

        // 학년
        gradeLabel = new JLabel();
        topPanel.add(gradeLabel);
        gradeField = new JTextField("", 4);
        topPanel.add(gradeField);

        // 학기
        semesterLabel = new JLabel();
        topPanel.add(semesterLabel);
        semesterField = new JTextField("", 8);
        topPanel.add(semesterField);

        // 버튼
        searchBtn = new JButton();
        topPanel.add(searchBtn);

        // 상태
        statusLabel = new JLabel();
        topPanel.add(statusLabel);

        add(topPanel, BorderLayout.NORTH);

        // 테이블
        tableModel = new DefaultTableModel(LanguageChange.getKoreanHeaders(), 0);
        resultTable = new JTable(tableModel);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        // 검색 버튼 이벤트
        searchBtn.addActionListener(e -> new Thread(() -> {
            searchBtn.setEnabled(false);
            tableModel.setRowCount(0);
            statusLabel.setText("검색 중...");
            LectureCrawler.searchLectures(
                    yearField.getText().trim(),
                    semesterField.getText().trim(),
                    gradeField.getText().trim(),
                    tableModel,
                    statusLabel,
                    searchBtn);
        }).start());

        // 셀 클릭 팝업
        resultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = resultTable.rowAtPoint(evt.getPoint());
                int col = resultTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0) {
                    Object value = resultTable.getValueAt(row, col);
                    if (value != null) {
                        JOptionPane.showMessageDialog(KnuLectureCrawlerGUI.this,
                                value.toString(),
                                "셀 내용 보기",
                                JOptionPane.INFORMATION_MESSAGE);
                    }
                }
            }
        });
    }

    public static void main(String[] args) {
        RatingLoader.loadRatings();
        SwingUtilities.invokeLater(() -> new KnuLectureCrawlerGUI(0).setVisible(true));
    }
}
