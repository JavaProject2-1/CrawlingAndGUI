package basicWeb;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.List;

/**
 * 상세 강의 정보를 표시하는 새 창
 */
public class DetailedLectureWindow extends JFrame {
    private JTable detailTable;
    private DefaultTableModel detailTableModel;
    private JLabel statusLabel;

    public DetailedLectureWindow() {
        setTitle("상세 강의 정보");
        setSize(1400, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);

        initComponents();
    }

    private void initComponents() {
        // 상단 상태 라벨
        statusLabel = new JLabel("상세 정보를 불러오는 중...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.NORTH);

        // 상세 정보 테이블 헤더
        String[] detailHeaders = {
            "교과목명", "교과목코드", "담당교수", "강의시간", "강의실", "호실번호", "필수", "설계", "학점"
        };

        // 테이블 모델 생성
        detailTableModel = new DefaultTableModel(detailHeaders, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false; // 모든 셀을 편집 불가능하게 설정
            }
        };

        // 테이블 생성 및 설정
        detailTable = new JTable(detailTableModel);
        detailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailTable.setRowHeight(25);

        // 테이블 렌더러 설정
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 교과목명과 강의시간을 제외한 모든 열을 중앙 정렬
        for (int i = 0; i < detailTable.getColumnCount(); i++) {
            if (i != 0 && i != 3) { // 교과목명(0)과 강의시간(3)은 왼쪽 정렬 유지
                detailTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // 열 너비 설정
        detailTable.getColumnModel().getColumn(0).setPreferredWidth(200); // 교과목명
        detailTable.getColumnModel().getColumn(1).setPreferredWidth(100); // 교과목코드
        detailTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 담당교수
        detailTable.getColumnModel().getColumn(3).setPreferredWidth(250); // 강의시간
        detailTable.getColumnModel().getColumn(4).setPreferredWidth(150); // 강의실
        detailTable.getColumnModel().getColumn(5).setPreferredWidth(100); // 호실번호
        detailTable.getColumnModel().getColumn(6).setPreferredWidth(50);  // 필수
        detailTable.getColumnModel().getColumn(7).setPreferredWidth(50);  // 설계
        detailTable.getColumnModel().getColumn(8).setPreferredWidth(50);  // 학점

        JScrollPane scrollPane = new JScrollPane(detailTable);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel();
        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 상세 정보 데이터 설정
     */
    public void setDetailData(List<Object[]> detailData) {
        SwingUtilities.invokeLater(() -> {
            detailTableModel.setRowCount(0);
            for (Object[] row : detailData) {
                detailTableModel.addRow(row);
            }
            statusLabel.setText("상세 정보 로드 완료: " + detailData.size() + "개 강의");
        });
    }

    /**
     * 상태 메시지 업데이트
     */
    public void updateStatus(String message) {
        SwingUtilities.invokeLater(() -> statusLabel.setText(message));
    }
}
