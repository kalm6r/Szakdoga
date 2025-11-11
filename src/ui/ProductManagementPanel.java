package ui;

import dto.CategoryOption;
import model.Category;
import model.Manufacturer;
import model.Product;
import model.Subcategory;
import service.InventoryService;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.filechooser.FileNameExtensionFilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.File;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductManagementPanel extends JPanel {

    private final InventoryService inventoryService = new InventoryService();
    private final Integer currentUserId;

    private final DefaultListModel<Product> productListModel = new DefaultListModel<>();
    private final JList<Product> productList = new JList<>(productListModel);

    private final JTextField productIdField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField userIdField = new JTextField();
    private final JTextField imageUrlField = new JTextField();
    private final JTextField subcategoryField = new JTextField();
    private final JComboBox<CategoryOption> categoryCombo = new JComboBox<>();
    private final JButton browseImageButton = new JButton("Tallózás...");

    private final JButton addButton;
    private final JButton updateButton;

    private boolean updatingForm = false;
    private int nextProductId = 1;

    public ProductManagementPanel(Integer currentUserId) {
        this.currentUserId = currentUserId;
        setPreferredSize(new Dimension(896, 504));
        setLayout(null);

        productIdField.setEditable(false);
        userIdField.setEditable(false);

        JLabel title = new JLabel("Termékek kezelése");
        title.setFont(new Font("Segoe UI", Font.PLAIN, 23));
        title.setBounds(77, 43, 400, 32);
        add(title);

        JPanel listPanel = new JPanel(new BorderLayout());
        listPanel.setBorder(new LineBorder(Color.BLACK, 1, true));
        listPanel.setBackground(Color.WHITE);
        listPanel.setBounds(77, 110, 200, 350);
        add(listPanel);

        productList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        productList.setCellRenderer(new DefaultListCellRenderer() {
            @Override
            public java.awt.Component getListCellRendererComponent(JList<?> list, Object value, int index,
                                                                   boolean isSelected, boolean cellHasFocus) {
                super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);
                if (value instanceof Product product) {
                    setText(product.getId() + " — " + product.getName());
                }
                return this;
            }
        });

        productList.addListSelectionListener(e -> {
            if (!e.getValueIsAdjusting()) {
                Product selected = productList.getSelectedValue();
                populateForm(selected);
            }
        });

        JScrollPane listScroll = new JScrollPane(productList);
        listScroll.setBorder(BorderFactory.createEmptyBorder());
        listPanel.add(listScroll, BorderLayout.CENTER);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBorder(new LineBorder(Color.BLACK, 1, true));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setBounds(310, 110, 576, 350);
        add(formWrapper);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        formPanel.setOpaque(false);
        formWrapper.add(formPanel, BorderLayout.CENTER);

        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.anchor = GridBagConstraints.LINE_START;
        gbcLabel.insets = new Insets(0, 0, 12, 12);

        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.gridx = 1;
        gbcField.weightx = 1.0;
        gbcField.fill = GridBagConstraints.HORIZONTAL;
        gbcField.insets = new Insets(0, 0, 12, 0);

        addRow(formPanel, gbcLabel, gbcField, 0, "Termék azonosító", productIdField);
        addRow(formPanel, gbcLabel, gbcField, 1, "Megnevezés", nameField);
        addRow(formPanel, gbcLabel, gbcField, 2, "Felhasználó azonosító", userIdField);
        addRow(formPanel, gbcLabel, gbcField, 3, "Kategória", categoryCombo);
        addRow(formPanel, gbcLabel, gbcField, 4, "Alkategória", subcategoryField);

        JPanel imageFieldPanel = new JPanel(new BorderLayout(8, 0));
        imageFieldPanel.add(imageUrlField, BorderLayout.CENTER);
        imageFieldPanel.add(browseImageButton, BorderLayout.EAST);
        addRow(formPanel, gbcLabel, gbcField, 5, "Kép", imageFieldPanel);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 12, 12));
        buttonPanel.setOpaque(false);
        formWrapper.add(buttonPanel, BorderLayout.SOUTH);

        JButton newButton = new JButton("Új");
        addButton = new JButton("Hozzáadás");
        updateButton = new JButton("Mentés");
        JButton deleteButton = new JButton("Törlés");

        buttonPanel.add(newButton);
        buttonPanel.add(addButton);
        buttonPanel.add(updateButton);
        buttonPanel.add(deleteButton);

        addButton.setEnabled(currentUserId != null);
        updateButton.setEnabled(false);

        newButton.addActionListener(e -> {
            productList.clearSelection();
            clearForm();
        });

        addButton.addActionListener(e -> addProduct());
        updateButton.addActionListener(e -> updateProduct());
        deleteButton.addActionListener(e -> deleteSelectedProduct());
        browseImageButton.addActionListener(e -> openImageChooser());

        categoryCombo.addActionListener(e -> {
            if (!updatingForm) {
                subcategoryField.setText("");
            }
        });

        loadCategories();
        loadProducts(-1);
    }

    private void addRow(JPanel panel, GridBagConstraints gbcLabel, GridBagConstraints gbcField,
                        int row, String labelText, java.awt.Component field) {
        GridBagConstraints labelConstraints = (GridBagConstraints) gbcLabel.clone();
        labelConstraints.gridy = row;
        panel.add(new JLabel(labelText), labelConstraints);

        GridBagConstraints fieldConstraints = (GridBagConstraints) gbcField.clone();
        fieldConstraints.gridy = row;
        panel.add(field, fieldConstraints);
    }

    private void loadCategories() {
        categoryCombo.removeAllItems();
        List<CategoryOption> categories = inventoryService.listAllCategories();
        Set<Integer> addedCategoryIds = new HashSet<>();
        Set<String> addedCategoryNames = new HashSet<>();
        for (CategoryOption option : categories) {
            if (option == null) {
                continue;
            }

            String name = option.getName();
            if (name == null) {
                continue;
            }

            String normalizedName = name.trim().toLowerCase(Locale.ROOT);
            if (normalizedName.isEmpty()) {
                continue;
            }

            if (addedCategoryIds.contains(option.getId()) || addedCategoryNames.contains(normalizedName)) {
                continue;
            }

            addedCategoryIds.add(option.getId());
            addedCategoryNames.add(normalizedName);
            categoryCombo.addItem(option);
        }
    }

    private void loadProducts(int selectProductId) {
        List<Product> products = inventoryService.listAllProducts();
        updateNextProductId(products);
        productListModel.clear();
        int indexToSelect = -1;
        for (Product product : products) {
            productListModel.addElement(product);
            if (product.getId() == selectProductId) {
                indexToSelect = productListModel.size() - 1;
            }
        }

        if (indexToSelect >= 0) {
            int finalIndexToSelect = indexToSelect;
            SwingUtilities.invokeLater(() -> productList.setSelectedIndex(finalIndexToSelect));
        } else {
            clearForm();
        }
    }

    private void populateForm(Product product) {
        updatingForm = true;
        try {
            if (product == null) {
                clearForm();
                return;
            }

            productIdField.setText(String.valueOf(product.getId()));
            productIdField.setEditable(false);
            nameField.setText(product.getName());
            userIdField.setText(String.valueOf(product.getUserId()));
            imageUrlField.setText(product.getImageUrl() == null ? "" : product.getImageUrl());
            subcategoryField.setText(formatSubcategory(product.getSubcategory()));

            selectCategory(product.getCategory());

            boolean canEdit = canCurrentUserEdit(product);
            setFormEditable(canEdit);
            updateButton.setEnabled(canEdit);
        } finally {
            updatingForm = false;
        }
    }

    private void selectCategory(Category category) {
        if (category == null) {
            categoryCombo.setSelectedIndex(-1);
            return;
        }

        for (int i = 0; i < categoryCombo.getItemCount(); i++) {
            CategoryOption option = categoryCombo.getItemAt(i);
            if (option != null && option.getId() == category.getId()) {
                categoryCombo.setSelectedIndex(i);
                return;
            }
        }
        categoryCombo.setSelectedIndex(-1);
    }

    private void clearForm() {
        if (updatingForm) return;
        productIdField.setText(String.valueOf(nextProductId));
        nameField.setText("");
        userIdField.setText(currentUserId == null ? "" : String.valueOf(currentUserId));
        imageUrlField.setText("");
        categoryCombo.setSelectedIndex(-1);
        subcategoryField.setText("");
        setFormEditable(currentUserId != null);
        updateButton.setEnabled(false);
    }

    private void addProduct() {
        if (currentUserId == null) {
            JOptionPane.showMessageDialog(this,
                    "Csak bejelentkezett felhasználók vehetnek fel terméket.",
                    "Nincs felhasználó",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }
        try {
            Product product = buildProductFromForm(null);
            Optional<Product> existing = inventoryService.findProductById(product.getId());
            if (existing.isPresent()) {
                JOptionPane.showMessageDialog(this,
                        "Ezzel az azonosítóval már létezik termék.",
                        "Hiba",
                        JOptionPane.ERROR_MESSAGE);
                return;
            }

            int inserted = inventoryService.addProduct(product);
            if (inserted == 1) {
                JOptionPane.showMessageDialog(this,
                        "A termék hozzáadása sikeres volt.",
                        "Siker",
                        JOptionPane.INFORMATION_MESSAGE);
                loadProducts(product.getId());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Nem sikerült a termék hozzáadása.",
                        "Hiba",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void updateProduct() {
        Product selected = productList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Válassz ki egy terméket a listából a módosításhoz.",
                    "Nincs kiválasztott elem",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        if (!canCurrentUserEdit(selected)) {
            JOptionPane.showMessageDialog(this,
                    "Csak a termék létrehozója módosíthatja a terméket.",
                    "Nincs jogosultság",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        try {
            Product product = buildProductFromForm(selected);
            if (inventoryService.updateProduct(product)) {
                JOptionPane.showMessageDialog(this,
                        "A módosítás sikeres volt.",
                        "Siker",
                        JOptionPane.INFORMATION_MESSAGE);
                loadProducts(product.getId());
            } else {
                JOptionPane.showMessageDialog(this,
                        "Nem sikerült a módosítás.",
                        "Hiba",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
        }
    }

    private void deleteSelectedProduct() {
        Product selected = productList.getSelectedValue();
        if (selected == null) {
            JOptionPane.showMessageDialog(this,
                    "Válassz ki egy terméket a törléshez.",
                    "Nincs kiválasztott elem",
                    JOptionPane.WARNING_MESSAGE);
            return;
        }

        int confirm = JOptionPane.showConfirmDialog(this,
                "Biztosan törlöd a kiválasztott terméket?",
                "Megerősítés",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        if (inventoryService.deleteProduct(selected.getId())) {
            JOptionPane.showMessageDialog(this,
                    "A törlés sikeres volt.",
                    "Siker",
                    JOptionPane.INFORMATION_MESSAGE);
            loadProducts(-1);
        } else {
            JOptionPane.showMessageDialog(this,
                    "Nem sikerült a törlés.",
                    "Hiba",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Product buildProductFromForm(Product template) {
        String idText = productIdField.getText().trim();
        String name = nameField.getText().trim();
        String userIdText = userIdField.getText().trim();
        String imageUrl = imageUrlField.getText().trim();
        String subcatText = subcategoryField.getText().trim();

        if (idText.isEmpty() || userIdText.isEmpty()) {
            throw new IllegalArgumentException("Add meg a termék és a felhasználó azonosítóját.");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Add meg a termék nevét.");
        }

        CategoryOption option = (CategoryOption) categoryCombo.getSelectedItem();
        if (option == null) {
            throw new IllegalArgumentException("Válassz kategóriát a termékhez.");
        }
        if (subcatText.isEmpty()) {
        	 throw new IllegalArgumentException("Add meg az alkategória nevét.");
        }

        int id = template != null ? template.getId() : Integer.parseInt(idText);
        int userId;
        if (template != null) {
            userId = template.getUserId();
        } else if (currentUserId != null) {
            userId = currentUserId;
        } else {
            userId = Integer.parseInt(userIdText);
        }

        Subcategory subcategory = resolveSubcategoryFromInput(subcatText);
        Category category = new Category(option.getId(), option.getName(), subcategory);

        return new Product(id, name, userId, category, imageUrl.isBlank() ? null : imageUrl, subcategory.getId());
    }

    private void updateNextProductId(List<Product> products) {
        nextProductId = products.stream().mapToInt(Product::getId).max().orElse(0) + 1;
    }

    private boolean canCurrentUserEdit(Product product) {
        return currentUserId != null && product != null && product.getUserId() == currentUserId;
    }

    private void setFormEditable(boolean editable) {
        nameField.setEditable(editable);
        imageUrlField.setEditable(editable);
        categoryCombo.setEnabled(editable);
        browseImageButton.setEnabled(editable);
        subcategoryField.setEditable(editable);

    }

    private String formatSubcategory(Subcategory subcategory) {
        if (subcategory == null) {
            return "";
        }
        String name = subcategory.getName();
        Manufacturer manufacturer = subcategory.getManufacturer();
        String manufacturerName = manufacturer != null ? manufacturer.getName() : null;
        if (manufacturerName != null && !manufacturerName.isBlank()) {
            return (name == null ? "" : name.trim()) + " (" + manufacturerName.trim() + ")";
        }
        return name == null ? "" : name.trim();
    }

    private Subcategory resolveSubcategoryFromInput(String input) {
        String raw = input == null ? "" : input.trim();
        if (raw.isEmpty()) {
            throw new IllegalArgumentException("Add meg az alkategória nevét.");
        }

        String namePart = raw;
        String manufacturerPart = null;

        int openParen = raw.lastIndexOf('(');
        int closeParen = raw.lastIndexOf(')');
        if (openParen >= 0 && closeParen > openParen) {
            manufacturerPart = raw.substring(openParen + 1, closeParen).trim();
            namePart = raw.substring(0, openParen).trim();
        }

        if (namePart.isEmpty()) {
            throw new IllegalArgumentException("Add meg az alkategória nevét.");
        }

        if (manufacturerPart != null && !manufacturerPart.isEmpty()) {
            final String nameForError = namePart;
            final String manufacturerForError = manufacturerPart;
            Optional<Subcategory> exactMatch = inventoryService
                    .findSubcategoryByNameAndManufacturer(namePart, manufacturerPart);
            if (exactMatch.isPresent()) {
                return exactMatch.get();
            }

            return inventoryService.createSubcategory(nameForError, manufacturerForError)
                    .orElseThrow(() -> new IllegalArgumentException(
                            "Nem található \"" + nameForError + "\" alkategória a megadott gyártóval, " +
                                    "és a gyártó (" + manufacturerForError + ") sincs az adatbázisban."));
        }

        List<Subcategory> matches = inventoryService.findSubcategoriesByName(namePart);
        if (matches.isEmpty()) {
            throw new IllegalArgumentException("Nem található ilyen alkategória: " + namePart + ". " +
                    "Add meg a gyártót is zárójelben, pl.: \"" + namePart + " (Gyártó)\" az új alkategória létrehozásához.");
        }
        if (matches.size() == 1) {
            return matches.get(0);
        }

        String suggestions = matches.stream()
                .map(this::formatSubcategory)
                .filter(s -> s != null && !s.isBlank())
                .distinct()
                .sorted()
                .collect(Collectors.joining(", "));

        throw new IllegalArgumentException("Több alkategória is egyezik ezzel a névvel. " +
                "Add meg a gyártót is zárójelben, pl.: \"" + formatSubcategory(matches.get(0)) + "\". " +
                (suggestions.isEmpty() ? "" : "Választható értékek: " + suggestions + "."));
    }

    private void openImageChooser() {
        JFileChooser chooser = new JFileChooser();
        chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
        chooser.setAcceptAllFileFilterUsed(true);
        chooser.setFileFilter(new FileNameExtensionFilter("Képfájlok", "png", "jpg", "jpeg", "gif", "bmp"));

        String currentValue = imageUrlField.getText().trim();
        if (!currentValue.isEmpty()) {
            File currentFile = new File(currentValue);
            if (currentFile.isDirectory()) {
                chooser.setCurrentDirectory(currentFile);
            } else {
                File parent = currentFile.getParentFile();
                if (parent != null && parent.exists()) {
                    chooser.setCurrentDirectory(parent);
                }
            }
        }

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            if (selectedFile != null) {
                imageUrlField.setText(selectedFile.getAbsolutePath());
            }
        }
    }
}