package basicWeb;

import javax.swing.*;

/**
 * 애플리케이션 진입점
 */
public class KNUGUIApp {
    public static void main(String[] args) {
        // 평점 정보 로드
        // RatingLoader.loadRatings();
        
        // GUI 실행
        SwingUtilities.invokeLater(() -> {
            KnuLectureCrawlerGUI gui = new KnuLectureCrawlerGUI(0);
            gui.setVisible(true);
        });
    }
}
