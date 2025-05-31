package basicWeb;

public class Language {
    public static void applyKoreanLabels(KnuLectureCrawlerGUI gui, int sequence) {
        gui.setTitle("KNU 강의 계획서 크롤러");
        gui.searchBtn.setText("검색");
        gui.statusLabel.setText("대기 중...");
        gui.yearLabel.setText("개설연도:");
        gui.semesterLabel.setText("학기:");
        gui.gradeLabel.setText("학년:");
        gui.tableModel.setColumnIdentifiers(LanguageChange.getHeaders(sequence));
    }
}
