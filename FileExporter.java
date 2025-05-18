package basicWeb;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.io.FileWriter;

public class FileExporter {
    public static void exportTableToTxt(JFrame parent, DefaultTableModel model) {
        JFileChooser fileChooser = new JFileChooser();
        fileChooser.setDialogTitle("결과 저장");

        int result = fileChooser.showSaveDialog(parent);
        if (result == JFileChooser.APPROVE_OPTION) {
            try {
                FileWriter writer = new FileWriter(fileChooser.getSelectedFile());

                for (int i = 0; i < model.getColumnCount(); i++) {
                    writer.write(model.getColumnName(i) + (i < model.getColumnCount() - 1 ? "\t" : "\n"));
                }

                for (int row = 0; row < model.getRowCount(); row++) {
                    for (int col = 0; col < model.getColumnCount(); col++) {
                        writer.write((model.getValueAt(row, col) != null ? model.getValueAt(row, col).toString() : "") +
                                (col < model.getColumnCount() - 1 ? "\t" : "\n"));
                    }
                }

                writer.close();
                JOptionPane.showMessageDialog(parent, "파일 저장 완료!");
            } catch (Exception e) {
                JOptionPane.showMessageDialog(parent, "파일 저장 중 오류: " + e.getMessage());
            }
        }
    }
}
