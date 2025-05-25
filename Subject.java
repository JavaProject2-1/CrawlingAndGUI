package Crawler;

public class Subject {
    public String year, semester, division, code, name, credit;
    public boolean isRequired, isDesign;

    public Subject(String year, String semester, String division, String code, String name, boolean isRequired, boolean isDesign, String credit) {
        this.year = year;
        this.semester = semester;
        this.division = division;
        this.code = code;
        this.name = name;
        this.isRequired = isRequired;
        this.isDesign = isDesign;
        this.credit = credit;
    }
}
