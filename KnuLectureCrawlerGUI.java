// KnuLectureCrawlerGUI.java
import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.ArrayList;
import java.util.List;

public class KnuLectureCrawlerGUI extends JFrame {
    private JTextField yearField;
    private JComboBox<String> semesterCombo;
    private JComboBox<String> gradeCombo;
    private JButton searchBtn;
    private JButton detailBtn;
    private JTable resultTable;
    private DefaultTableModel tableModel;
    private JLabel statusLabel;
    private JLabel yearLabel, semesterLabel, gradeLabel;
    private List<Subject> currentSubjects;

    public KnuLectureCrawlerGUI(int sequence) {
        setTitle("KNU 강의 계획서 크롤러");
        setSize(1200, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        currentSubjects = new ArrayList<>();

        initComponents();
        LanguageChange.applyKoreanLabels(this, sequence);
    }

    private void initComponents() {
        JPanel topPanel = createTopPanel();
        add(topPanel, BorderLayout.NORTH);

        tableModel = createTableModel();
        resultTable = new JTable(tableModel);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        setupTableRenderers();
        setupSearchButtonAction();
        setupDetailButtonAction();
        setupTableClickEvent();
    }

    private JPanel createTopPanel() {
        JPanel topPanel = new JPanel();

        yearLabel = new JLabel("개설연도:");
        topPanel.add(yearLabel);
        yearField = new JTextField("", 6);
        topPanel.add(yearField);

        gradeLabel = new JLabel("학년:");
        topPanel.add(gradeLabel);
        gradeCombo = new JComboBox<>(new String[]{"1", "2", "3", "4"});
        topPanel.add(gradeCombo);

        semesterLabel = new JLabel("학기:");
        topPanel.add(semesterLabel);
        semesterCombo = new JComboBox<>(new String[]{"1학기", "2학기", "계절학기(하계)", "계절학기(동계)"});
        topPanel.add(semesterCombo);

        searchBtn = new JButton("검색");
        topPanel.add(searchBtn);

        detailBtn = new JButton("선택한 과목 상세 정보");
        detailBtn.setEnabled(false);
        topPanel.add(detailBtn);

        statusLabel = new JLabel("Ready");
        topPanel.add(statusLabel);

        return topPanel;
    }

    private DefaultTableModel createTableModel() {
        return new DefaultTableModel(LanguageChange.getHeaders(0), 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class;
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0;
            }
        };
    }

    private void setupTableRenderers() {
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        SwingUtilities.invokeLater(() -> {
            for (int i = 1; i < resultTable.getColumnCount(); i++) {
                resultTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        });
    }

    private void setupSearchButtonAction() {
        searchBtn.addActionListener(e -> new Thread(() -> {
            searchBtn.setEnabled(false);
            detailBtn.setEnabled(false);
            tableModel.setRowCount(0);
            currentSubjects.clear();
            statusLabel.setText("검색 중...");

            try {
                String yearFull = yearField.getText().trim();
                String grade = gradeCombo.getSelectedItem().toString();
                String semester = semesterCombo.getSelectedItem().toString();

                List<Subject> subjects = CrawlerExample.crawlCurriculumSubjects(yearFull, grade, semester);
                currentSubjects.addAll(subjects);

                List<Object[]> rows = CrawlerExample.convertSubjectsToRows(subjects);
                for (Object[] row : rows) {
                    tableModel.addRow(row);
                }

                statusLabel.setText("검색 완료: 전체 " + rows.size() + "개 과목");
                detailBtn.setEnabled(true);
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("오류 발생: " + ex.getMessage());
            } finally {
                searchBtn.setEnabled(true);
            }
        }).start());
    }

    private void setupDetailButtonAction() {
        detailBtn.addActionListener(e -> new Thread(() -> {
            List<Subject> selectedSubjects = getSelectedSubjects();
            if (selectedSubjects.isEmpty()) {
                JOptionPane.showMessageDialog(this, "상세 정보를 보려는 과목을 선택해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
                return;
            }

            DetailedLectureWindow detailWindow = new DetailedLectureWindow(this);
            SwingUtilities.invokeLater(() -> {
                this.setVisible(false);
                detailWindow.setVisible(true);
            });

            detailBtn.setEnabled(false);
            statusLabel.setText("상세 정보 가져오는 중...");

            try {
                String yearFull = yearField.getText().trim();
                List<DetailedSubject> detailedSubjects = CrawlerExample.crawlSelectedSubjectsDetail(selectedSubjects, yearFull);
                List<Object[]> detailRows = CrawlerExample.convertDetailedSubjectsToRows(detailedSubjects);
                detailWindow.setDetailData(detailRows);
                statusLabel.setText("상세 정보 수집 완료: " + detailedSubjects.size() + "개 강의");
            } catch (Exception ex) {
                ex.printStackTrace();
                detailWindow.updateStatus("오류 발생: " + ex.getMessage());
                statusLabel.setText("상세 정보 수집 중 오류 발생");
            } finally {
                detailBtn.setEnabled(true);
            }
        }).start());
    }

    private List<Subject> getSelectedSubjects() {
        List<Subject> selectedSubjects = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) tableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                if (i < currentSubjects.size()) {
                    selectedSubjects.add(currentSubjects.get(i));
                }
            }
        }
        return selectedSubjects;
    }

    private void setupTableClickEvent() {
        resultTable.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                int row = resultTable.rowAtPoint(evt.getPoint());
                int col = resultTable.columnAtPoint(evt.getPoint());
                if (row >= 0 && col >= 0 && col != 0) {
                    Object value = resultTable.getValueAt(row, col);
                    if (value != null) {
                        TableCellRenderer renderer = resultTable.getCellRenderer(row, col);
                        Component comp = renderer.getTableCellRendererComponent(resultTable, value, false, false, row, col);
                        Font font = comp.getFont();
                        FontRenderContext frc = new FontRenderContext(new AffineTransform(), true, true);
                        int textWidth = (int) font.getStringBounds(value.toString(), frc).getWidth();
                        int columnWidth = resultTable.getColumnModel().getColumn(col).getWidth();
                        if (textWidth > columnWidth) {
                            JOptionPane.showMessageDialog(KnuLectureCrawlerGUI.this,
                                    value.toString(), "셀 내용 보기", JOptionPane.INFORMATION_MESSAGE);
                        }
                    }
                }
            }
        });
    }

    public JTextField getYearField() { return yearField; }
    public JComboBox<String> getSemesterCombo() { return semesterCombo; }
    public JComboBox<String> getGradeCombo() { return gradeCombo; }
    public JButton getSearchBtn() { return searchBtn; }
    public JTable getResultTable() { return resultTable; }
    public DefaultTableModel getTableModel() { return tableModel; }
    public JLabel getStatusLabel() { return statusLabel; }
    public JLabel getYearLabel() { return yearLabel; }
    public JLabel getSemesterLabel() { return semesterLabel; }
    public JLabel getGradeLabel() { return gradeLabel; }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KnuLectureCrawlerGUI(0).setVisible(true));
    }
}
