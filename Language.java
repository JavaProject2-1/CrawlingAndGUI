package basicWeb;

public class Language {
    public static void changeLanguage(KnuLectureCrawlerGUI gui, String lang) {
        gui.setTitle(LanguageChange.getLabel(lang, "title"));
        gui.searchBtn.setText(LanguageChange.getLabel(lang, "search"));
        gui.saveBtn.setText(LanguageChange.getLabel(lang, "save"));
        gui.statusLabel.setText(LanguageChange.getLabel(lang, "status"));

        gui.yearLabel.setText(LanguageChange.getLabel(lang, "year"));
        gui.semesterLabel.setText(LanguageChange.getLabel(lang, "semester"));
        gui.subjectLabel.setText(LanguageChange.getLabel(lang, "subject"));

        for (int i = 0; i < gui.tableModel.getColumnCount(); i++) {
            gui.tableModel.setColumnIdentifiers(LanguageChange.getHeaders(lang));
        }
    }
}
