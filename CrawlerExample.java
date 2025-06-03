package basicWeb;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import java.io.File;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.*;

/**
 * 강의 상세 정보를 크롤링하는 서비스 클래스
 */
public class CrawlerExample {
    
    /**
     * 셀 내용을 텍스트로 추출
     */
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

    /**
     * HTML 요소에서 교과목 정보 파싱
     */
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

    /**
     * 강의 고유 키 생성 (중복 제거용)
     */
    private static String getCourseKey(List<WebElement> cells) {
        String code = getCellText(cells.get(7));
        String name = getCellText(cells.get(8));
        String professor = getCellText(cells.get(12));
        String time = getCellText(cells.get(13));
        return code + "|" + name + "|" + professor + "|" + time;
    }

    /**
     * 크롬 드라이버 설정 (운영체제별 자동 감지)
     */
    private static void setupChromeDriver() {
        String os = System.getProperty("os.name").toLowerCase();
        String driverPath;

        if (os.contains("win")) {
            driverPath = "chromedriver.exe";
        } else if (os.contains("mac")) {
            driverPath = "chromedriver_mac";
        } else if (os.contains("nux") || os.contains("nix")) {
            driverPath = "chromedriver_linux";
        } else {
            throw new RuntimeException("지원하지 않는 운영체제입니다: " + os);
        }

        String absolutePath = Paths.get(driverPath).toAbsolutePath().toString();
        File driverFile = new File(absolutePath);

        if (!driverFile.exists()) {
            throw new RuntimeException("크롬 드라이버 파일을 찾을 수 없습니다: " + absolutePath);
        }

        if (!os.contains("win")) {
            if (!driverFile.canExecute()) {
                driverFile.setExecutable(true);
            }
        }
    }

    /**
     * 교과과정 페이지에서 기본 교과목 정보 크롤링
     */
    private static List<Subject> crawlCurriculumSubjects() {
        setupChromeDriver();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        WebDriver driver = new ChromeDriver(options);

        List<Subject> allSubjects = new ArrayList<>();

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
                        Subject sub = parseSubject(year, "1학기", division, tds.get(0), tds.get(1), tds.get(2));
                        if (sub != null) allSubjects.add(sub);
                    }
                    if (tds.size() >= 6) {
                        Subject sub = parseSubject(year, "2학기", division, tds.get(3), tds.get(4), tds.get(5));
                        if (sub != null) allSubjects.add(sub);
                    }
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return allSubjects;
    }

    /**
     * 강의 상세 정보 크롤링
     */
    private static List<DetailedSubject> crawlDetailedLectureInfo(List<Subject> filteredSubjects, String inputYearFull) {
        setupChromeDriver();
        
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--disable-gpu");
        WebDriver driver = new ChromeDriver(options);

        List<DetailedSubject> detailedSubjects = new ArrayList<>();
        Set<String> uniqueCourses = new HashSet<>();

        try {
            driver.get("https://knuin.knu.ac.kr/public/stddm/lectPlnInqr.knu");
            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            for (int idx = 0; idx < filteredSubjects.size(); idx++) {
                Subject s = filteredSubjects.get(idx);
                
                try {
                    driver.navigate().refresh();
                    wait.until(ExpectedConditions.presenceOfElementLocated(By.id("schEstblYear___input")));

                    // 개설년도 입력
                    WebElement yearInput = driver.findElement(By.id("schEstblYear___input"));
                    js.executeScript("arguments[0].value=arguments[1];", yearInput, inputYearFull);

                    // 학기 선택
                    WebElement semesterSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[title='개설학기']")));
                    new Select(semesterSelect).selectByVisibleText(s.getSemester());

                    // 검색 조건을 교과목코드로 설정
                    WebElement detailSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("schCode")));
                    new Select(detailSelect).selectByVisibleText("교과목코드");

                    // 교과목코드 입력
                    WebElement inputBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("schCodeContents")));
                    inputBox.clear();
                    inputBox.sendKeys(s.getCode());

                    // 검색 실행
                    WebElement searchBtn = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnSearch")));
                    searchBtn.click();

                    wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody[@id='grid01_body_tbody']/tr[1]")));
                    Thread.sleep(1000);

                    // 스크롤 가능한 영역에서 데이터 추출
                    WebElement scrollDiv = driver.findElement(By.id("grid01_scrollY_div"));
                    Number scrollHeightNum = (Number) js.executeScript("return arguments[0].scrollHeight;", scrollDiv);
                    double scrollHeight = scrollHeightNum.doubleValue();

                    Number clientHeightNum = (Number) js.executeScript("return arguments[0].clientHeight;", scrollDiv);
                    double clientHeight = clientHeightNum.doubleValue();

                    double scrollTop = 0;
                    double increment = 150;
                    boolean newCourseFound;
                    boolean isLastSubject = (idx == filteredSubjects.size() - 1);

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

                                String year = getCellText(cells.get(3));
                                String code = getCellText(cells.get(7));
                                String lectureTime = getCellText(cells.get(13));
                                String classroom = getCellText(cells.get(15));
                                String roomNumber = getCellText(cells.get(16));
                                String professor = getCellText(cells.get(12));

                                DetailedSubject detailedSubject = new DetailedSubject(
                                        year, s.getSemester(), s.getDivision(),
                                        code, s.getName(), s.isRequired(), s.isDesign(), s.getCredit(),
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
                    
                } catch (Exception e) {
                    System.err.println("과목 " + s.getCode() + " 처리 중 오류 발생: " + e.getMessage());
                    continue;
                }
            }

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            driver.quit();
        }

        return detailedSubjects;
    }

    /**
     * 강의 상세 정보가 담긴 배열을 반환하는 메인 함수
     * @param inputYearFull 개설년도 (예: "2025")
     * @param inputYear 학년 (예: "1")
     * @param inputSemester 학기 (예: "1학기", "2학기", "계절학기(하계)", "계절학기(동계)")
     * @return DetailedSubject 배열
     */
    public static DetailedSubject[] getDetailedLectureInfo(String inputYearFull, String inputYear, String inputSemester) {
        try {
            System.out.println("교과과정 정보를 가져오는 중...");
            
            // 1단계: 기본 교과목 정보 크롤링
            List<Subject> allSubjects = crawlCurriculumSubjects();
            System.out.println("총 커리큘럼 과목 수: " + allSubjects.size());

            // 2단계: 조건에 맞는 과목 필터링
            List<Subject> filteredSubjects = new ArrayList<>();
            for (Subject s : allSubjects) {
                if (s.getYear().equals(inputYear) && s.getSemester().equals(inputSemester)) {
                    filteredSubjects.add(s);
                }
            }

            System.out.println("선택한 " + inputYear + "학년 " + inputSemester + " 과목 수: " + filteredSubjects.size());

            if (filteredSubjects.isEmpty()) {
                System.out.println("선택한 조건에 해당하는 과목이 없습니다.");
                return new DetailedSubject[0];
            }

            // 3단계: 상세 강의 정보 크롤링
            System.out.println("상세 강의 정보를 가져오는 중...");
            List<DetailedSubject> detailedSubjects = crawlDetailedLectureInfo(filteredSubjects, inputYearFull);

            System.out.println("상세 정보 수집 완료: " + detailedSubjects.size() + "개 강의");

            // List를 배열로 변환하여 반환
            return detailedSubjects.toArray(new DetailedSubject[0]);

        } catch (Exception e) {
            System.err.println("강의 정보 수집 중 오류 발생: " + e.getMessage());
            e.printStackTrace();
            return new DetailedSubject[0];
        }
    }

    /**
     * 교과목 리스트를 테이블 행 데이터로 변환 (기본 정보용)
     */
    public static List<Object[]> convertSubjectsToRows(List<Subject> subjects) {
        List<Object[]> rowList = new ArrayList<>();
        for (Subject s : subjects) {
            Object[] row = new Object[] {
                false,                      // 선택
                s.getName(),                // 교과목명
                s.isRequired() ? "O" : "",  // 필수
                s.isDesign() ? "O" : "",    // 설계
                s.getCredit()               // 학점
            };
            rowList.add(row);
        }
        return rowList;
    }

    /**
     * 상세 교과목 리스트를 테이블 행 데이터로 변환 (상세 정보용)
     */
    public static List<Object[]> convertDetailedSubjectsToRows(DetailedSubject[] detailedSubjects) {
        List<Object[]> rowList = new ArrayList<>();
        for (DetailedSubject ds : detailedSubjects) {
            Object[] row = new Object[] {
                false,                          // 선택
                ds.getName(),                   // 교과목명
                ds.getCode(),                   // 교과목코드
                ds.getProfessor(),              // 담당교수
                ds.getLectureTime(),            // 강의시간
                ds.getClassroom(),              // 강의실
                ds.getRoomNumber(),             // 호실번호
                ds.isRequired() ? "O" : "",     // 필수
                ds.isDesign() ? "O" : "",       // 설계
                ds.getCredit()                  // 학점
            };
            rowList.add(row);
        }
        return rowList;
    }
    
    /**
     * 강의 정보를 테이블 행 데이터로 가져오기 (기본 정보)
     */
    public static List<Object[]> getLectureRowData(String yearFull, String grade, String semester) {
        List<Subject> allSubjects = crawlCurriculumSubjects();
        List<Subject> filteredSubjects = new ArrayList<>();
        
        for (Subject s : allSubjects) {
            if (s.getYear().equals(grade) && s.getSemester().equals(semester)) {
                filteredSubjects.add(s);
            }
        }
        
        return convertSubjectsToRows(filteredSubjects);
    }
}
