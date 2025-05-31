package basicWeb;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import java.awt.*;
import java.awt.font.FontRenderContext;
import java.awt.geom.AffineTransform;
import java.util.List;

public class KnuLectureCrawlerGUI extends JFrame {
    JTextField yearField;
    JComboBox<String> semesterCombo;
    JComboBox<String> gradeCombo;
    JButton searchBtn;
    JTable resultTable;
    DefaultTableModel tableModel;
    JLabel statusLabel;
    JLabel yearLabel, semesterLabel, gradeLabel;

    public KnuLectureCrawlerGUI(int sequence) {
        setTitle("KNU 강의 계획서 크롤러");
        setSize(1200, 600);
        setDefaultCloseOperation(EXIT_ON_CLOSE);
        setLocationRelativeTo(null);

        initComponents();
        Language.applyKoreanLabels(this, sequence);
    }

    private void initComponents() {
        JPanel topPanel = new JPanel();

        yearLabel = new JLabel("개설년도:");
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

        statusLabel = new JLabel("Ready");
        topPanel.add(statusLabel);

        add(topPanel, BorderLayout.NORTH);

        tableModel = new DefaultTableModel(LanguageChange.getHeaders(0), 0) {
            @Override
            public Class<?> getColumnClass(int columnIndex) {
                if (columnIndex == 0) return Boolean.class; // 체크박스 열
                return super.getColumnClass(columnIndex);
            }

            @Override
            public boolean isCellEditable(int row, int column) {
                return column == 0; // 체크박스 열만 편집 가능
            }
        };

        resultTable = new JTable(tableModel);
        add(new JScrollPane(resultTable), BorderLayout.CENTER);

        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        SwingUtilities.invokeLater(() -> {
            for (int i = 1; i < resultTable.getColumnCount(); i++) {
                resultTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        });

        searchBtn.addActionListener(e -> new Thread(() -> {
            searchBtn.setEnabled(false);
            tableModel.setRowCount(0);
            statusLabel.setText("검색 중...");
            try {
                List<Object[]> rows = CrawlerExample.getLectureRowData(
                        yearField.getText().trim(),
                        gradeCombo.getSelectedItem().toString(),
                        semesterCombo.getSelectedItem().toString()
                );
                for (Object[] row : rows) {
                    tableModel.addRow(row);
                }
                statusLabel.setText("검색 완료: 전체 " + rows.size() + "개 과목");
            } catch (Exception ex) {
                ex.printStackTrace();
                statusLabel.setText("오류 발생: " + ex.getMessage());
            } finally {
                searchBtn.setEnabled(true);
            }
        }).start());

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

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new KnuLectureCrawlerGUI(0).setVisible(true));
    }
}
