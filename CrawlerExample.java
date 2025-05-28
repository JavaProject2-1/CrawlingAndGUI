import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.time.Duration;
import java.util.*;

public class CrawlerExample {

    // 중복 제거된 과목명을 저장하는 전역 리스트
    private static List<String> uniqueSubjectNames;

    // 외부에서 저장된 과목명 목록을 가져오는 getter
    public static List<String> getUniqueSubjectNames() {
        return uniqueSubjectNames;
    }

    public static String getCellText(WebElement cell) {
        try {
            List<WebElement> nobrList = cell.findElements(By.tagName("nobr"));
            if (!nobrList.isEmpty()) {
                String innerHtml = nobrList.get(0).getDomProperty("innerHTML");
                String textWithNewlines = innerHtml.replaceAll("(?i)<br[^>]*>", "\n");
                return textWithNewlines.replaceAll("<[^>]+>", "").trim();
            }
            return cell.getText().trim();
        } catch (org.openqa.selenium.NoSuchElementException e) {
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

    private static String getCourseKey(List<WebElement> cells) {
        String code = getCellText(cells.get(7));
        String name = getCellText(cells.get(8));
        String professor = getCellText(cells.get(12));
        String time = getCellText(cells.get(13));
        return code + "|" + name + "|" + professor + "|" + time;
    }

    // DetailedSubject 리스트에서 과목명을 중복 없이 수집해 반환하는 함수
    public static List<String> collectUniqueSubjectNames(List<DetailedSubject> detailedSubjects) {
        Set<String> seenNames = new LinkedHashSet<>();
        for (DetailedSubject ds : detailedSubjects) {
            seenNames.add(ds.name);
        }
        return new ArrayList<>(seenNames);
    }

    public static void main(String[] args) throws InterruptedException {
        System.setProperty("webdriver.chrome.driver", "C:/JAVA_WORKSPACE/chromedriver-win64/chromedriver.exe");
        Scanner scanner = new Scanner(System.in);

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://cse.knu.ac.kr/sub3_2_b.php");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
            wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("tbody")));

            List<Subject> allSubjects = new ArrayList<>();
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
                        Subject sub = parseSubject(year, "1학기", division, tds.get(0), tds.get(1), tds.get(2));
                        if (sub != null) allSubjects.add(sub);
                    }
                    if (tds.size() >= 6) {
                        Subject sub = parseSubject(year, "2학기", division, tds.get(3), tds.get(4), tds.get(5));
                        if (sub != null) allSubjects.add(sub);
                    }
                }
            }

            System.out.println("총 커리큘럼 과목 수: " + allSubjects.size());

            System.out.print("개설년도 입력 (예: 2025): ");
            String inputYearFull = scanner.nextLine().trim();

            System.out.print("학년 입력 (예: 1): ");
            String inputYear = scanner.nextLine().trim();

            System.out.print("학기 입력 (예: 1학기, 2학기, 계절학기(하계), 계절학기(동계)): ");
            String inputSemester = scanner.nextLine().trim();

            List<Subject> filtered = new ArrayList<>();
            for (Subject s : allSubjects) {
                if (s.year.equals(inputYear) && s.semester.equals(inputSemester)) filtered.add(s);
            }

            System.out.println("선택한 " + inputYear + "학년 " + inputSemester + " 과목 수: " + filtered.size());

            if (filtered.isEmpty()) {
                System.out.println("선택한 조건에 해당하는 과목이 없습니다.");
                return;
            }

            driver.get("https://knuin.knu.ac.kr/public/stddm/lectPlnInqr.knu");
            WebDriverWait wait2 = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            List<DetailedSubject> detailedSubjects = new ArrayList<>();
            Set<String> uniqueCourses = new HashSet<>();

            for (int idx = 0; idx < filtered.size(); idx++) {
                Subject s = filtered.get(idx);
                driver.navigate().refresh();
                wait2.until(ExpectedConditions.presenceOfElementLocated(By.id("schEstblYear___input")));

                WebElement yearInput = driver.findElement(By.id("schEstblYear___input"));
                js.executeScript("arguments[0].value=arguments[1];", yearInput, inputYearFull);

                WebElement semesterSelect = wait2.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[title='개설학기']")));
                new Select(semesterSelect).selectByVisibleText(s.semester);

                WebElement detailSelect = wait2.until(ExpectedConditions.elementToBeClickable(By.id("schCode")));
                new Select(detailSelect).selectByVisibleText("교과목코드");

                WebElement inputBox = wait2.until(ExpectedConditions.presenceOfElementLocated(By.id("schCodeContents")));
                inputBox.clear();
                inputBox.sendKeys(s.code);

                WebElement searchBtn = wait2.until(ExpectedConditions.elementToBeClickable(By.id("btnSearch")));
                searchBtn.click();

                wait2.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody[@id='grid01_body_tbody']/tr[1]")));

                Thread.sleep(1000);

                WebElement scrollDiv = driver.findElement(By.id("grid01_scrollY_div"));
                Number scrollHeightNum = (Number) js.executeScript("return arguments[0].scrollHeight;", scrollDiv);
                double scrollHeight = scrollHeightNum.doubleValue();

                Number clientHeightNum = (Number) js.executeScript("return arguments[0].clientHeight;", scrollDiv);
                double clientHeight = clientHeightNum.doubleValue();

                double scrollTop = 0;
                double increment = 150;
                boolean newCourseFound;
                boolean isLastSubject = (idx == filtered.size() - 1);

                do {
                    js.executeScript("arguments[0].scrollTop = arguments[1];", scrollDiv, scrollTop);
                    Thread.sleep(800);

                    List<WebElement> rows = driver.findElements(By.xpath("//tbody[@id='grid01_body_tbody']/tr"));
                    int rowsToProcess = isLastSubject ? rows.size() : Math.max(0, rows.size() - 1);
                    newCourseFound = false;

                    for (int i = 0; i < rowsToProcess; i++) {
                        WebElement row = rows.get(i);
                        List<WebElement> cells = row.findElements(By.tagName("td"));
                        if (cells.size() < 17) continue;

                        String key = getCourseKey(cells);
                        if (!uniqueCourses.contains(key)) {
                            uniqueCourses.add(key);

                            year = getCellText(cells.get(3));
                            String code = getCellText(cells.get(7));
                            String lectureTime = getCellText(cells.get(13));
                            String classroom = getCellText(cells.get(15));
                            String roomNumber = getCellText(cells.get(16));
                            String professor = getCellText(cells.get(12));

                            DetailedSubject detailedSubject = new DetailedSubject(
                                    year, s.semester, s.division,
                                    code, s.name, s.isRequired, s.isDesign, s.credit,
                                    professor, lectureTime, classroom, roomNumber
                            );

                            detailedSubjects.add(detailedSubject);
                            newCourseFound = true;
                        }
                    }

                    if (scrollTop + clientHeight >= scrollHeight) break;
                    scrollTop = Math.min(scrollTop + increment, scrollHeight - clientHeight);
                } while (newCourseFound);

                Thread.sleep(1000);
            }

            System.out.println("\n=== 과목명 목록 ===");
            // 전역 변수에 저장 (외부에서도 접근 가능하게 하기 위해)
            uniqueSubjectNames = collectUniqueSubjectNames(detailedSubjects);
            for (String name : uniqueSubjectNames) {
                System.out.println(name);
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
            scanner.close();
        }
    }
}
