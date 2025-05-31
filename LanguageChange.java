package basicWeb;

public class LanguageChange {
    private static final String[] HEADERS_SUBJECTS_ADD = {
            "No", "개설연도", "개설학기", "학년", "교과구분", "개설대학", "개설학과", "강좌번호", "교과목명", "학점",
            "강의", "실습", "담당교수", "강의시간", "강의시간(실제시간)", "강의실", "호실번호", "수강정원", "수강신청",
            "수강꾸러미신청", "수강꾸러미신청가능여부", "대학원공통교과목여부", "비고", "평점"
    };
    
    private static final String[] HEADERS_TIMETABLE_ADD = {
    		"선택", "교과목명", "필수", "설계", "학점"
    };

    public static String[] getHeaders(int sequence) {
        if (sequence == 0) return HEADERS_TIMETABLE_ADD;
        else return HEADERS_SUBJECTS_ADD;
    }
}
