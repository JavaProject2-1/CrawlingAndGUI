package basicWeb;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class KnuLectureCrawlerGUI extends JFrame {
    JTextField yearField;
    JComboBox<String> semesterCombo;
    JTextField subjectField;
    JButton searchBtn, saveBtn;
    JTable resultTable;
    DefaultTableModel tableModel;
    JLabel statusLabel;
    JComboBox<String> langCombo;
    JLabel yearLabel, semesterLabel, subjectLabel, languageLabel;

    public KnuLectureCrawlerGUI() {
        setTitle("KNU 강의 계획서 크롤러");
        setSize(1200, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
    }

    private void initComponents() {
        JPanel topPanel = new JPanel();

        yearLabel = new JLabel("개설연도:");
        topPanel.add(yearLabel);
        yearField = new JTextField("2025", 6);
        topPanel.add(yearField);

        semesterLabel = new JLabel("개설학기:");
        topPanel.add(semesterLabel);
        semesterCombo = new JComboBox<>(new String[]{"1학기", "2학기", "계절학기(하계)", "계절학기(동계)"});
        semesterCombo.setSelectedItem("계절학기(하계)");
        topPanel.add(semesterCombo);

        subjectLabel = new JLabel("교과목명:");
        topPanel.add(subjectLabel);
        subjectField = new JTextField("공학수학", 10);
        topPanel.add(subjectField);

        searchBtn = new JButton("검색");
        saveBtn = new JButton("저장(.txt)");
        topPanel.add(searchBtn);
        topPanel.add(saveBtn);

        statusLabel = new JLabel("대기 중...");
        topPanel.add(statusLabel);

        languageLabel = new JLabel("언어:");
        topPanel.add(languageLabel);
        langCombo = new JComboBox<>(new String[]{"한국어", "English"});
        topPanel.add(langCombo);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(LanguageChange.getHeaders("Korean"), 0);
        resultTable = new JTable(tableModel);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        searchBtn.addActionListener(e -> new Thread(() -> {
            searchBtn.setEnabled(false);
            tableModel.setRowCount(0);
            statusLabel.setText("검색 중...");
            LectureCrawler.searchLectures(yearField.getText().trim(),
                    (String) semesterCombo.getSelectedItem(),
                    subjectField.getText().trim(),
                    tableModel,
                    statusLabel,
                    searchBtn);
        }).start());

        saveBtn.addActionListener(e -> FileExporter.exportTableToTxt(this, tableModel));

        langCombo.addActionListener(e -> {
            String selectedLang = langCombo.getSelectedItem().toString();
            Language.changeLanguage(this, selectedLang.equals("English") ? "English" : "한국어");
        });
    }

    public void updateLanguage(String title, String searchText, String saveText, String statusText,
                                String yearLabelText, String semesterLabelText, String subjectLabelText,
                                String languageLabelText, String[] headers) {
        setTitle(title);
        searchBtn.setText(searchText);
        saveBtn.setText(saveText);
        statusLabel.setText(statusText);
        yearLabel.setText(yearLabelText);
        semesterLabel.setText(semesterLabelText);
        subjectLabel.setText(subjectLabelText);
        languageLabel.setText(languageLabelText);

        tableModel.setColumnIdentifiers(headers);
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KnuLectureCrawlerGUI().setVisible(true));
    }
}
