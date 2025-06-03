package basicWeb;

import javax.swing.*;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * 상세 강의 정보를 표시하는 새 창
 */
public class DetailedLectureWindow extends JFrame {
    private JTable detailTable;
    private DefaultTableModel detailTableModel;
    private JLabel statusLabel;
    private List<DetailedSubject> detailedSubjects;
    private TimetableGUI timetableGUI; // 기존 시간표 참조

    public DetailedLectureWindow() {
        setTitle("상세 강의 정보");
        setSize(1400, 700);
        setLocationRelativeTo(null);
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        
        detailedSubjects = new ArrayList<>();
        initComponents();
    }

    public DetailedLectureWindow(TimetableGUI timetableGUI) {
        this();
        this.timetableGUI = timetableGUI;
    }

    private void initComponents() {
        // 상단 상태 라벨
        statusLabel = new JLabel("상세 정보를 불러오는 중...");
        statusLabel.setHorizontalAlignment(SwingConstants.CENTER);
        statusLabel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
        add(statusLabel, BorderLayout.NORTH);

        // 상세 정보 테이블 헤더 (체크박스 열 추가)
        String[] detailHeaders = {
            "선택", "교과목명", "교과목코드", "담당교수", "강의시간", "강의실", "호실번호", "필수", "설계", "학점"
        };

        // 테이블 모델 생성
        detailTableModel = new DefaultTableModel(detailHeaders, 0) {
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

        // 테이블 생성 및 설정
        detailTable = new JTable(detailTableModel);
        detailTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        detailTable.setRowHeight(25);

        // 테이블 렌더러 설정
        DefaultTableCellRenderer centerRenderer = new DefaultTableCellRenderer();
        centerRenderer.setHorizontalAlignment(SwingConstants.CENTER);
        
        // 교과목명과 강의시간을 제외한 모든 열을 중앙 정렬
        for (int i = 1; i < detailTable.getColumnCount(); i++) {
            if (i != 1 && i != 4) { // 교과목명(1)과 강의시간(4)은 왼쪽 정렬 유지
                detailTable.getColumnModel().getColumn(i).setCellRenderer(centerRenderer);
            }
        }

        // 열 너비 설정
        detailTable.getColumnModel().getColumn(0).setPreferredWidth(50);  // 선택
        detailTable.getColumnModel().getColumn(1).setPreferredWidth(200); // 교과목명
        detailTable.getColumnModel().getColumn(2).setPreferredWidth(100); // 교과목코드
        detailTable.getColumnModel().getColumn(3).setPreferredWidth(100); // 담당교수
        detailTable.getColumnModel().getColumn(4).setPreferredWidth(250); // 강의시간
        detailTable.getColumnModel().getColumn(5).setPreferredWidth(150); // 강의실
        detailTable.getColumnModel().getColumn(6).setPreferredWidth(100); // 호실번호
        detailTable.getColumnModel().getColumn(7).setPreferredWidth(50);  // 필수
        detailTable.getColumnModel().getColumn(8).setPreferredWidth(50);  // 설계
        detailTable.getColumnModel().getColumn(9).setPreferredWidth(50);  // 학점

        JScrollPane scrollPane = new JScrollPane(detailTable);
        add(scrollPane, BorderLayout.CENTER);

        // 하단 버튼 패널
        JPanel buttonPanel = new JPanel();
        
        JButton confirmBtn = new JButton("시간표에 추가");
        confirmBtn.addActionListener(e -> addSelectedToTimetable());
        buttonPanel.add(confirmBtn);
        
        JButton closeBtn = new JButton("닫기");
        closeBtn.addActionListener(e -> dispose());
        buttonPanel.add(closeBtn);
        
        add(buttonPanel, BorderLayout.SOUTH);
    }

    /**
     * 선택된 강의들을 시간표에 추가
     */
    private void addSelectedToTimetable() {
        List<DetailedSubject> selectedSubjects = getSelectedSubjects();
        
        if (selectedSubjects.isEmpty()) {
            JOptionPane.showMessageDialog(this, "시간표에 추가할 강의를 선택해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
            return;
        }

        // 시간 겹침 검사
        if (hasTimeConflict(selectedSubjects)) {
            JOptionPane.showMessageDialog(this, "선택한 강의들 중 시간이 겹치는 강의가 있습니다.\n다시 선택해주세요.", "시간 겹침 경고", JOptionPane.WARNING_MESSAGE);
            return;
        }

        // 기존 시간표가 없으면 새로 생성
        if (timetableGUI == null) {
            timetableGUI = new TimetableGUI();
        }

        // 강의 추가
        int addedCount = 0;
        for (DetailedSubject subject : selectedSubjects) {
            if (addSubjectToTimetable(subject)) {
                addedCount++;
            }
        }
        
        if (addedCount > 0) {
            JOptionPane.showMessageDialog(this, addedCount + "개의 강의가 시간표에 추가되었습니다.", "완료", JOptionPane.INFORMATION_MESSAGE);
        } else {
            JOptionPane.showMessageDialog(this, "추가할 수 있는 강의가 없습니다.\n강의 시간 정보를 확인해주세요.", "알림", JOptionPane.INFORMATION_MESSAGE);
        }
    }

    /**
     * 선택된 강의들 반환
     */
    private List<DetailedSubject> getSelectedSubjects() {
        List<DetailedSubject> selectedSubjects = new ArrayList<>();
        
        for (int i = 0; i < detailTableModel.getRowCount(); i++) {
            Boolean isSelected = (Boolean) detailTableModel.getValueAt(i, 0);
            if (isSelected != null && isSelected) {
                if (i < detailedSubjects.size()) {
                    selectedSubjects.add(detailedSubjects.get(i));
                }
            }
        }
        
        return selectedSubjects;
    }

    /**
     * 시간 겹침 검사
     */
    private boolean hasTimeConflict(List<DetailedSubject> subjects) {
        for (int i = 0; i < subjects.size(); i++) {
            for (int j = i + 1; j < subjects.size(); j++) {
                if (isTimeConflict(subjects.get(i), subjects.get(j))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 두 강의의 시간 겹침 여부 확인
     */
    private boolean isTimeConflict(DetailedSubject subject1, DetailedSubject subject2) {
        List<TimeSlot> slots1 = parseTimeSlots(subject1.getLectureTime());
        List<TimeSlot> slots2 = parseTimeSlots(subject2.getLectureTime());
        
        for (TimeSlot slot1 : slots1) {
            for (TimeSlot slot2 : slots2) {
                if (slot1.isConflictWith(slot2)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * 강의시간 문자열을 TimeSlot 리스트로 파싱
     */
    private List<TimeSlot> parseTimeSlots(String lectureTime) {
        List<TimeSlot> timeSlots = new ArrayList<>();
        
        if (lectureTime == null || lectureTime.trim().isEmpty()) {
            return timeSlots;
        }
        
        System.out.println("파싱할 강의시간: " + lectureTime); // 디버깅용
        
        // 강의시간 형태: "월 09:00~12:00, 수 13:00~15:00" 등
        String[] timeParts = lectureTime.split(",");
        
        for (String timePart : timeParts) {
            timePart = timePart.trim();
            if (timePart.isEmpty()) continue;
            
            try {
                // "월 09:00~12:00" 형태 파싱
                String[] dayAndTime = timePart.split("\\s+");
                if (dayAndTime.length >= 2) {
                    String day = dayAndTime[0].trim();
                    String timeRange = dayAndTime[1].trim();
                    
                    if (timeRange.contains("~")) {
                        String[] times = timeRange.split("~");
                        if (times.length == 2) {
                            String startTime = times[0].trim();
                            String endTime = times[1].trim();
                            
                            timeSlots.add(new TimeSlot(day, startTime, endTime));
                            System.out.println("파싱된 시간슬롯: " + day + " " + startTime + "~" + endTime); // 디버깅용
                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("시간 파싱 오류: " + timePart + " - " + e.getMessage());
            }
        }
        
        return timeSlots;
    }

    /**
     * 강의를 시간표에 추가
     */
    private boolean addSubjectToTimetable(DetailedSubject subject) {
        List<TimeSlot> timeSlots = parseTimeSlots(subject.getLectureTime());
        
        if (timeSlots.isEmpty()) {
            System.out.println("시간 정보가 없는 과목: " + subject.getName());
            return false;
        }
        
        boolean added = false;
        for (TimeSlot slot : timeSlots) {
            int dayIndex = getDayIndex(slot.getDay());
            if (dayIndex == -1) {
                System.out.println("지원하지 않는 요일: " + slot.getDay());
                continue;
            }
            
            int startHour = getHourFromTime(slot.getStartTime());
            int endHour = getHourFromTime(slot.getEndTime());
            
            if (startHour == -1 || endHour == -1) {
                System.out.println("시간 파싱 실패: " + slot.getStartTime() + "~" + slot.getEndTime());
                continue;
            }
            
            // 시간표 범위 체크 (9시~18시)
            if (startHour < 9 || endHour > 18) {
                System.out.println("시간표 범위 벗어남: " + startHour + "~" + endHour);
                continue;
            }
            
            int row = (startHour - 9) * 2 + 1;
            int height = (endHour - startHour) * 2;
            
            Color color = new Color((int)(Math.random()*200)+55, (int)(Math.random()*200)+55, (int)(Math.random()*200)+55);
            timetableGUI.addSubjectToTable(subject.getName(), subject.getProfessor(), subject.getClassroom(), dayIndex + 1, row, height, color);
            added = true;
            
            System.out.println("시간표에 추가됨: " + subject.getName() + " - " + slot.getDay() + " " + startHour + "~" + endHour);
        }
        
        return added;
    }

    /**
     * 요일을 인덱스로 변환
     */
    private int getDayIndex(String day) {
        switch (day) {
            case "월": return 0;
            case "화": return 1;
            case "수": return 2;
            case "목": return 3;
            case "금": return 4;
            default: return -1;
        }
    }

    /**
     * 시간 문자열에서 시간 추출 (예: "09:00" -> 9)
     */
    private int getHourFromTime(String time) {
        try {
            if (time.contains(":")) {
                return Integer.parseInt(time.split(":")[0]);
            }
            return Integer.parseInt(time);
        } catch (NumberFormatException e) {
            System.err.println("시간 파싱 오류: " + time);
            return -1;
        }
    }

    /**
     * 상세 정보 데이터 설정
     */
    public void setDetailData(List<Object[]> detailData) {
        SwingUtilities.invokeLater(() -> {
            detailTableModel.setRowCount(0);
            detailedSubjects.clear();
            
            for (Object[] row : detailData) {
                // 체크박스를 맨 앞에 추가
                Object[] newRow = new Object[row.length + 1];
                newRow[0] = false; // 체크박스 초기값
                System.arraycopy(row, 0, newRow, 1, row.length);
                detailTableModel.addRow(newRow);
                
                // DetailedSubject 객체 생성 (실제 데이터에서)
                if (row.length >= 9) {
                    DetailedSubject subject = new DetailedSubject(
                        "", "", "", // year, semester, division
                        (String) row[1], // code
                        (String) row[0], // name
                        row[6].toString().equals("O"), // isRequired
                        row[7].toString().equals("O"), // isDesign
                        (String) row[8], // credit
                        (String) row[2], // professor
                        (String) row[3], // lectureTime
                        (String) row[4], // classroom
                        (String) row[5]  // roomNumber
                    );
                    detailedSubjects.add(subject);
                }
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

    /**
     * 시간 슬롯 클래스
     */
    private static class TimeSlot {
        private String day;
        private String startTime;
        private String endTime;

        public TimeSlot(String day, String startTime, String endTime) {
            this.day = day;
            this.startTime = startTime;
            this.endTime = endTime;
        }

        public String getDay() { return day; }
        public String getStartTime() { return startTime; }
        public String getEndTime() { return endTime; }

        public boolean isConflictWith(TimeSlot other) {
            if (!this.day.equals(other.day)) {
                return false; // 다른 요일이면 겹치지 않음
            }

            try {
                int thisStart = getMinutesFromTime(this.startTime);
                int thisEnd = getMinutesFromTime(this.endTime);
                int otherStart = getMinutesFromTime(other.startTime);
                int otherEnd = getMinutesFromTime(other.endTime);

                // 시간 겹침 검사
                return !(thisEnd <= otherStart || otherEnd <= thisStart);
            } catch (Exception e) {
                return false;
            }
        }

        private int getMinutesFromTime(String time) {
            String[] parts = time.split(":");
            int hour = Integer.parseInt(parts[0]);
            int minute = parts.length > 1 ? Integer.parseInt(parts[1]) : 0;
            return hour * 60 + minute;
        }
    }
}
