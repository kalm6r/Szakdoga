package ui;

import dao.ReportDao;
import dao.jdbc.JdbcReportDao;
import dto.TopProduct;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;

public class SearchPanel extends JPanel {
    private JPanel contentPane;
    private JTextField txtKeyword;
    private JButton btnSearch;
    private JTable table;
    private DefaultTableModel model;

    private final ReportDao reportDao = new JdbcReportDao();

    public SearchPanel() {
        setLayout(new BorderLayout());
        initComponents();
    }

    private void initComponents() {
        contentPane = new JPanel();
        add(contentPane, BorderLayout.CENTER);

        JLabel lblTitle = new JLabel("Keresés kulcsszó alapján");
        lblTitle.setFont(new Font("Segoe UI", Font.BOLD, 18));
        JLabel lblK = new JLabel("Kulcsszó:");
        txtKeyword = new JTextField();
        btnSearch = new JButton("Keresés");
        btnSearch.addActionListener(e -> refresh());

        model = new DefaultTableModel(new Object[]{"Termék", "Gyártó", "Kategória", "Ár (Ft)"}, 0){
            public boolean isCellEditable(int r,int c){return false;}
        };
        table = new JTable(model);
        JScrollPane sp = new JScrollPane(table);

        GroupLayout gl = new GroupLayout(contentPane);
        contentPane.setLayout(gl);
        gl.setAutoCreateGaps(true);
        gl.setAutoCreateContainerGaps(true);

        gl.setHorizontalGroup(
            gl.createParallelGroup(GroupLayout.Alignment.LEADING)
              .addComponent(lblTitle)
              .addGroup(gl.createSequentialGroup()
                    .addComponent(lblK)
                    .addComponent(txtKeyword, 260, 260, 360)
                    .addComponent(btnSearch, 110, 110, 110))
              .addComponent(sp)
        );
        gl.setVerticalGroup(
            gl.createSequentialGroup()
              .addComponent(lblTitle)
              .addGroup(gl.createParallelGroup(GroupLayout.Alignment.BASELINE)
                    .addComponent(lblK)
                    .addComponent(txtKeyword, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE)
                    .addComponent(btnSearch, GroupLayout.PREFERRED_SIZE, 30, GroupLayout.PREFERRED_SIZE))
              .addComponent(sp)
        );
    }

    private void refresh() {
        model.setRowCount(0);
        String kw = txtKeyword.getText().trim();
        for (TopProduct t : reportDao.searchByKeywordWithPrice(kw)) {
            model.addRow(new Object[]{ t.getProductName(), t.getManufacturerName(), t.getCategoryName(), t.getSellPrice() });
        }
    }
}
