package basicWeb;

import java.util.*;

public class LanguageChange {
    private static final Map<String, String[]> headersMap = new HashMap<>();
    private static final Map<String, Map<String, String>> labelMap = new HashMap<>();

    static {
        headersMap.put("Korean", new String[]{
                "No", "개설연도", "개설학기", "학년", "교과구분", "개설대학", "개설학과", "강좌번호", "교과목명", "학점",
                "강의", "실습", "담당교수", "강의시간", "강의시간(실제시간)", "강의실", "호실번호", "수강정원", "수강신청",
                "수강꾸러미신청", "수강꾸러미신청가능여부", "대학원공통교과목여부", "비고"
        });

        headersMap.put("English", new String[]{
                "No", "Year", "Semester", "Grade", "Category", "College", "Department", "Course Code", "Subject Name", "Credits",
                "Lecture", "Lab", "Professor", "Time", "Real Time", "Classroom", "Room No.", "Quota", "Enrolled",
                "Cart", "Cart Available", "Graduate Common", "Note"
        });

        Map<String, String> koLabels = new HashMap<>();
        koLabels.put("title", "KNU 강의 계획서 크롤러");
        koLabels.put("search", "검색");
        koLabels.put("save", "저장");
        koLabels.put("status", "대기 중...");
        koLabels.put("year", "개설연도:");
        koLabels.put("semester", "개설학기:");
        koLabels.put("subject", "교과목명:");

        Map<String, String> enLabels = new HashMap<>();
        enLabels.put("title", "KNU Lecture Plan Crawler");
        enLabels.put("search", "Search");
        enLabels.put("save", "Save");
        enLabels.put("status", "Waiting...");
        enLabels.put("year", "Year:");
        enLabels.put("semester", "Semester:");
        enLabels.put("subject", "Subject:");

        labelMap.put("Korean", koLabels);
        labelMap.put("English", enLabels);
    }

    public static String[] getHeaders(String lang) {
        return headersMap.getOrDefault(lang, headersMap.get("Korean"));
    }

    public static String getLabel(String lang, String key) {
        return labelMap.getOrDefault(lang, labelMap.get("Korean")).getOrDefault(key, key);
    }
}
