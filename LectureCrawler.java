package basicWeb;

import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.*;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.time.Duration;
import java.util.List;

public class LectureCrawler {

    private static String getCellText(WebElement cell) {
        try {
            WebElement nobr = cell.findElement(By.tagName("nobr"));
            String innerHtml = nobr.getDomProperty("innerHTML");
            String textWithNewlines = innerHtml.replaceAll("(?i)<br[^>]*>", "\n");
            return textWithNewlines.replaceAll("<[^>]+>", "").trim();
        } catch (NoSuchElementException e) {
            return cell.getText().trim();
        }
    }

    public static void searchLectures(String year, String semester, String subject,
                                      DefaultTableModel tableModel, JLabel statusLabel, JButton searchBtn) {

        System.setProperty("webdriver.chrome.driver", "C://KNU_Lecture//2-1//Java_Pro//chromedriver-win64//chromedriver.exe");

        ChromeOptions options = new ChromeOptions();
        options.addArguments("--headless");
        options.addArguments("--disable-gpu");

        WebDriver driver = new ChromeDriver(options);

        try {
            driver.get("https://knuin.knu.ac.kr/public/stddm/lectPlnInqr.knu");

            WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(20));
            JavascriptExecutor js = (JavascriptExecutor) driver;

            WebElement yearInput = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("schEstblYear___input")));
            js.executeScript("arguments[0].value='" + year + "'; arguments[0].dispatchEvent(new Event('change'));", yearInput);

            WebElement semesterSelect = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("select[title='개설학기']")));
            new Select(semesterSelect).selectByVisibleText(semester);

            js.executeScript("document.getElementById('schSbjetCd1').value = '';" );

            WebElement detailSelect = wait.until(ExpectedConditions.elementToBeClickable(By.id("schCode")));
            new Select(detailSelect).selectByVisibleText("교과목명");

            WebElement inputBox = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("schCodeContents")));
            inputBox.clear();
            inputBox.sendKeys(subject);

            WebElement searchBtnElement = wait.until(ExpectedConditions.elementToBeClickable(By.id("btnSearch")));
            searchBtnElement.click();

            Thread.sleep(1500);  // 안정성 확보

            WebElement tbody = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tbody[@id='grid01_body_tbody']")));
            List<WebElement> rows = tbody.findElements(By.tagName("tr"));

            int headerLength = LanguageChange.getHeaders("Korean").length;

            for (WebElement row : rows) {
                List<WebElement> cells = row.findElements(By.tagName("td"));
                if (cells.isEmpty()) continue;

                boolean hasContent = cells.stream().anyMatch(cell -> !getCellText(cell).isEmpty());
                if (!hasContent || cells.size() < headerLength - 1) continue;

                Object[] rowData = new Object[headerLength];
                for (int i = 0; i < headerLength; i++) {
                    if (i < cells.size()) {
                        String text = getCellText(cells.get(i)).replace("\n", " / ").replaceAll(" +", " ").trim();
                        rowData[i] = text;
                    } else {
                        rowData[i] = "";
                    }
                }
                tableModel.addRow(rowData);
            }

            statusLabel.setText("검색 완료: " + tableModel.getRowCount() + "건");

        } catch (Exception e) {
            statusLabel.setText("오류 발생: " + e.getMessage());
        } finally {
            driver.quit();
            searchBtn.setEnabled(true);
        }
    }
}
