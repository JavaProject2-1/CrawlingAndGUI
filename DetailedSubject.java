package basicWeb;

public class DetailedSubject extends Subject {
    public String professor;
    public String lectureTime;
    public String classroom;
    public String roomNumber;

    public DetailedSubject(String year, String semester, String division, String code, String name, boolean isRequired, boolean isDesign, String credit, String professor, String lectureTime, String classroom, String roomNumber) {
        super(year, semester, division, code, name, isRequired, isDesign, credit);
        this.professor = professor;
        this.lectureTime = lectureTime;
        this.classroom = classroom;
        this.roomNumber = roomNumber;
    }

    public String getFormattedInfo() {
        return String.format("%s (학년: %s) - %s, 교수: %s, 강의시간: %s, 강의실: %s, 호실번호: %s",
                code, year, name, professor, lectureTime, classroom, roomNumber);
    }
}
