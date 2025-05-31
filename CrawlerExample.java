package basicWeb;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

public class CrawlerExample {
    public static String getCellText(WebElement cell) {
        try {
            List<WebElement> nobrList = cell.findElements(By.tagName("nobr"));
            if (!nobrList.isEmpty()) {
                String innerHtml = nobrList.get(0).getDomProperty("innerHTML");
                String textWithNewlines = innerHtml.replaceAll("(?i)<br[^>]*>", "\n");
                return textWithNewlines.replaceAll("<[^>]+>", "").trim();
            }
            return cell.getText().trim();
        }
        catch (org.openqa.selenium.NoSuchElementException e) {
            return cell.getText().trim();
        }
    }

    private static Subject parseSubject(String year, String semester, String division,
                                        WebElement codeElem, WebElement nameElem, WebElement creditElem) {
        String code = codeElem.getText().trim();
        String html = nameElem.getAttribute("innerHTML");
        String name = html.replaceAll("<[^>]*>", "").split("\\(")[0].trim();
        String credit = creditElem.getText().replaceAll("\\s+", "");

        if (code.isBlank() || code.equals("-") || credit.isBlank() || credit.equals("-")) return null;
        if (name.isBlank() || name.equals("-")) return null;

        boolean isRequired = html.contains("bum01");
        boolean isDesign = html.contains("bum02");

        return new Subject(year, semester, division, code, name, isRequired, isDesign, credit);
    }

    public static List<Subject> crawlCurriculumSubjects(String inputYearFull, String inputYear, String inputSemester) {
        String driverPath = Paths.get("chromedriver.exe").toAbsolutePath().toString();
        System.setProperty("webdriver.chrome.driver", driverPath);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        WebDriver driver = new ChromeDriver(options);

        List<Subject> subjectList = new ArrayList<>();

        try {
            driver.get("https://cse.knu.ac.kr/sub3_2_b.php");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("tbody")));

            String year = "", division = "";

            for (WebElement tbody : driver.findElements(By.tagName("tbody"))) {
                for (WebElement row : tbody.findElements(By.tagName("tr"))) {
                    List<WebElement> ths = row.findElements(By.tagName("th"));
                    List<WebElement> tds = row.findElements(By.tagName("td"));

                    if (!ths.isEmpty()) {
                        if (ths.size() == 2) {
                            year = ths.get(0).getText().trim();
                            division = ths.get(1).getText().trim();
                        } else if (ths.size() == 1) {
                            String txt = ths.get(0).getText().trim();
                            if (txt.matches("\\d")) year = txt;
                            else division = txt;
                        }
                    }

                    if (year.isEmpty()) continue;

                    if (tds.size() >= 3) {
                        Subject s = parseSubject(year, "1학기", division, tds.get(0), tds.get(1), tds.get(2));
                        if (s != null && s.year.equals(inputYear) && s.semester.equals(inputSemester)) {
                            subjectList.add(s);
                        }
                    }

                    if (tds.size() >= 6) {
                        Subject s = parseSubject(year, "2학기", division, tds.get(3), tds.get(4), tds.get(5));
                        if (s != null && s.year.equals(inputYear) && s.semester.equals(inputSemester)) {
                            subjectList.add(s);
                        }
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return subjectList;
    }

    public static List<Object[]> convertSubjectsToRows(List<Subject> subjects) {
        List<Object[]> rowList = new ArrayList<>();
        for (Subject s : subjects) {
            Object[] row = new Object[] {
                false,                    // 선택
                s.name,                   // 교과목명
                s.isRequired ? "O" : "",  // 필수
                s.isDesign ? "O" : "",    // 설계
                s.credit                  // 학점
            };
            rowList.add(row);
        }
        return rowList;
    }
    
    public static List<Object[]> getLectureRowData(String yearFull, String grade, String semester) {
        List<Subject> subjects = crawlCurriculumSubjects(yearFull, grade, semester);
        return convertSubjectsToRows(subjects);
    }
}
