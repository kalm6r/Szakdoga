package ui;

import dto.CategoryOption;
import model.Category;
import model.Manufacturer;
import model.Product;
import model.Subcategory;
import model.Supply;
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
import javax.swing.ScrollPaneConstants;
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
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

public class ProductManagementPanel extends JPanel {

    private final InventoryService inventoryService = new InventoryService();
    private final Integer currentUserId;
    private final ProductChangeManager changeManager = ProductChangeManager.getInstance();

    private final DefaultListModel<Product> productListModel = new DefaultListModel<>();
    private final JList<Product> productList = new JList<>(productListModel);

    private final JTextField productIdField = new JTextField();
    private final JTextField nameField = new JTextField();
    private final JTextField userIdField = new JTextField();
    private final JTextField imageUrlField = new JTextField();
    private final JComboBox<String> subcategoryCombo = new JComboBox<>();
    private final JComboBox<CategoryOption> categoryCombo = new JComboBox<>();
    private final JButton browseImageButton = ModernUIComponents.createCompactSecondaryButton("Tallózás...");
    
    // Beszerzési adatok
    private final JTextField purchaseDateField = new JTextField();
    private final JTextField purchasePriceField = new JTextField();
    private final JTextField sellPriceField = new JTextField();

    private final JButton addButton;
    private final JButton updateButton;

    private boolean updatingForm = false;
    private int nextProductId = 1;
    
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    public ProductManagementPanel(Integer currentUserId) {
        this.currentUserId = currentUserId;
        setPreferredSize(new Dimension(896, 504));
        setLayout(null);

        productIdField.setEditable(false);
        userIdField.setEditable(false);
        
        // Editable ComboBox-ok beállítása
        categoryCombo.setEditable(true);
        subcategoryCombo.setEditable(true);

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
        ModernUIComponents.applyModernScrollbarStyle(listScroll);
        listPanel.add(listScroll, BorderLayout.CENTER);

        JPanel formWrapper = new JPanel(new BorderLayout());
        formWrapper.setBorder(new LineBorder(Color.BLACK, 1, true));
        formWrapper.setBackground(Color.WHITE);
        formWrapper.setBounds(310, 110, 576, 350);
        add(formWrapper);

        JPanel formPanel = new JPanel(new GridBagLayout());
        formPanel.setBorder(new EmptyBorder(16, 16, 16, 16));
        formPanel.setOpaque(false);
        
        // Scrollolható formPanel
        JScrollPane formScrollPane = new JScrollPane(formPanel);
        formScrollPane.setBorder(null);
        formScrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);
        formScrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
        ModernUIComponents.applyModernScrollbarStyle(formScrollPane);
        formWrapper.add(formScrollPane, BorderLayout.CENTER);

        GridBagConstraints gbcLabel = new GridBagConstraints();
        gbcLabel.gridx = 0;
        gbcLabel.anchor = GridBagConstraints.LINE_START;
        gbcLabel.insets = new Insets(0, 0, 8, 12);  // Csökkentett térköz: 8px helyett 12px

        GridBagConstraints gbcField = new GridBagConstraints();
        gbcField.gridx = 1;
        gbcField.weightx = 1.0;
        gbcField.fill = GridBagConstraints.HORIZONTAL;
        gbcField.insets = new Insets(0, 0, 8, 0);  // Csökkentett térköz: 8px helyett 12px

        addRow(formPanel, gbcLabel, gbcField, 0, "Termék azonosító", productIdField);
        addRow(formPanel, gbcLabel, gbcField, 1, "Megnevezés", nameField);
        addRow(formPanel, gbcLabel, gbcField, 2, "Felhasználó azonosító", userIdField);
        addRow(formPanel, gbcLabel, gbcField, 3, "Kategória", categoryCombo);
        addRow(formPanel, gbcLabel, gbcField, 4, "Alkategória", subcategoryCombo);

        JPanel imageFieldPanel = new JPanel(new BorderLayout(8, 0));
        imageFieldPanel.add(imageUrlField, BorderLayout.CENTER);
        imageFieldPanel.add(browseImageButton, BorderLayout.EAST);
        addRow(formPanel, gbcLabel, gbcField, 5, "Kép", imageFieldPanel);
        
        // Beszerzési adatok
        addRow(formPanel, gbcLabel, gbcField, 6, "Beszerzés dátuma", purchaseDateField);
        addRow(formPanel, gbcLabel, gbcField, 7, "Beszerzési ár (Ft)", purchasePriceField);
        addRow(formPanel, gbcLabel, gbcField, 8, "Eladási ár (Ft)", sellPriceField);
        
        // Kompakt modern stílus alkalmazása (kisebb padding)
        ModernUIComponents.applyCompactTextFieldStyle(productIdField);
        ModernUIComponents.applyCompactTextFieldStyle(nameField);
        ModernUIComponents.applyCompactTextFieldStyle(userIdField);
        ModernUIComponents.applyCompactTextFieldStyle(imageUrlField);
        ModernUIComponents.applyCompactTextFieldStyle(purchaseDateField);
        ModernUIComponents.applyCompactTextFieldStyle(purchasePriceField);
        ModernUIComponents.applyCompactTextFieldStyle(sellPriceField);
        ModernUIComponents.applyModernComboBoxStyle(categoryCombo);
        ModernUIComponents.applyModernComboBoxStyle(subcategoryCombo);

        JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 8, 6));
        buttonPanel.setBackground(new Color(248, 249, 250));
        buttonPanel.setBorder(new javax.swing.border.CompoundBorder(
            new javax.swing.border.MatteBorder(1, 0, 0, 0, new Color(222, 226, 230)),
            new EmptyBorder(4, 8, 4, 8)
        ));
        formWrapper.add(buttonPanel, BorderLayout.SOUTH);

        JButton newButton = ModernUIComponents.createCompactSecondaryButton("Új");
        addButton = ModernUIComponents.createCompactSuccessButton("Hozzáadás");
        updateButton = ModernUIComponents.createCompactPrimaryButton("Mentés");
        JButton deleteButton = ModernUIComponents.createCompactDangerButton("Törlés");

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
                subcategoryCombo.setSelectedItem("");  // Üresre állítjuk az alkategóriát
                loadSubcategoriesForCategory();  // Betöltjük a kategóriához tartozó alkategóriákat
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
    
    /**
     * Betölti a kiválasztott kategóriához tartozó alkategóriákat
     */
    private void loadSubcategoriesForCategory() {
        subcategoryCombo.removeAllItems();
        
        Object selectedObj = categoryCombo.getSelectedItem();
        if (selectedObj == null) {
            return;
        }
        
        // Kategória ID megszerzése (lehet CategoryOption vagy String)
        int categoryId = -1;
        if (selectedObj instanceof CategoryOption) {
            categoryId = ((CategoryOption) selectedObj).getId();
        } else if (selectedObj instanceof String) {
            String categoryName = ((String) selectedObj).trim();
            // Keressük meg név alapján
            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                CategoryOption opt = categoryCombo.getItemAt(i);
                if (opt != null && opt.getName().equalsIgnoreCase(categoryName)) {
                    categoryId = opt.getId();
                    break;
                }
            }
        }
        
        if (categoryId == -1) {
            return;  // Nem találtuk vagy új kategória
        }
        
        // Minden termék lekérése
        List<Product> allProducts = inventoryService.listAllProducts();
        
        // Alkategóriák gyűjtése ehhez a kategóriához
        Set<String> subcategoryNames = new HashSet<>();
        for (Product p : allProducts) {
            if (p.getCategory() != null && 
                p.getCategory().getId() == categoryId &&
                p.getSubcategory() != null) {
                String subcatDisplay = formatSubcategory(p.getSubcategory());
                if (!subcatDisplay.isEmpty()) {
                    subcategoryNames.add(subcatDisplay);
                }
            }
        }
        
        // ABC sorrendben hozzáadjuk
        subcategoryNames.stream()
            .sorted(String.CASE_INSENSITIVE_ORDER)
            .forEach(subcategoryCombo::addItem);
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

            selectCategory(product.getCategory());
            loadSubcategoriesForCategory();  // Betöltjük a kategóriához tartozó alkategóriákat
            
            // Alkategória beállítása
            String subcatDisplay = formatSubcategory(product.getSubcategory());
            subcategoryCombo.setSelectedItem(subcatDisplay);
            
            // Supply adatok betöltése
            Optional<Supply> supplyOpt = inventoryService.findSupplyByProductId(product.getId());
            if (supplyOpt.isPresent()) {
                Supply supply = supplyOpt.get();
                purchaseDateField.setText(supply.getBought() != null 
                    ? supply.getBought().format(DATE_FORMAT) 
                    : "");
                purchasePriceField.setText(String.valueOf(supply.getPurchasePrice()));
                sellPriceField.setText(String.valueOf(supply.getSellPrice()));
            } else {
                purchaseDateField.setText("");
                purchasePriceField.setText("");
                sellPriceField.setText("");
            }

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
        subcategoryCombo.removeAllItems();
        subcategoryCombo.setSelectedItem("");
        
        // Mai dátum automatikus beállítása
        purchaseDateField.setText(LocalDate.now().format(DATE_FORMAT));
        purchasePriceField.setText("");
        sellPriceField.setText("");
        
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
                // Supply adatok mentése
                boolean supplySuccess = saveSupplyData(product.getId());
                
                // Sikeres üzenet összeállítása
                String message = "A termék és beszerzési adatok hozzáadása sikeres volt.";
                
                // Ha új kategória lett létrehozva, jelezzük
                String categoryName = product.getCategory().getName();
                if (isNewlyCreatedCategory(categoryName)) {
                    message += "\n\n✓ Új kategória létrehozva: " + categoryName;
                }
                
                if (supplySuccess) {
                    JOptionPane.showMessageDialog(this,
                            message,
                            "Siker",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "A termék létrejött, de a beszerzési adatok mentése sikertelen volt.",
                            "Részleges siker",
                            JOptionPane.WARNING_MESSAGE);
                }
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
    
    /**
     * Ellenőrzi, hogy az adott kategória újonnan lett-e létrehozva
     */
    private boolean isNewlyCreatedCategory(String categoryName) {
        // Megnézzük, hogy a kategória a lista végén van-e (utoljára hozzáadva)
        int itemCount = categoryCombo.getItemCount();
        if (itemCount > 0) {
            CategoryOption lastItem = categoryCombo.getItemAt(itemCount - 1);
            return lastItem != null && lastItem.getName().equalsIgnoreCase(categoryName);
        }
        return false;
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
                // Supply adatok mentése
                boolean supplySuccess = saveSupplyData(product.getId());
                if (supplySuccess) {
                    JOptionPane.showMessageDialog(this,
                            "A módosítás és beszerzési adatok mentése sikeres volt.",
                            "Siker",
                            JOptionPane.INFORMATION_MESSAGE);
                } else {
                    JOptionPane.showMessageDialog(this,
                            "A termék módosult, de a beszerzési adatok mentése sikertelen volt.",
                            "Részleges siker",
                            JOptionPane.WARNING_MESSAGE);
                }
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

        // Ellenőrizzük, hogy van-e Supply rekord
        boolean hasSupply = inventoryService.findSupplyByProductId(selected.getId()).isPresent();
        
        String warningMessage = "Biztosan törlöd a kiválasztott terméket?\n\n" +
                                selected.getName() + " (ID: " + selected.getId() + ")";
        
        if (hasSupply) {
            warningMessage += "\n\n⚠ A termékhez tartozó készletadatok is törlésre kerülnek!";
        }
        
        warningMessage += "\n\nFIGYELEM: Ez a művelet nem vonható vissza!";

        int confirm = JOptionPane.showConfirmDialog(this,
                warningMessage,
                "Törlés megerősítése",
                JOptionPane.YES_NO_OPTION,
                JOptionPane.WARNING_MESSAGE);
        
        if (confirm != JOptionPane.YES_OPTION) {
            return;
        }

        try {
            // Először töröljük a Supply rekordot (ha van)
            if (hasSupply) {
                boolean supplyDeleted = inventoryService.deleteSupply(selected.getId());
                if (!supplyDeleted) {
                    JOptionPane.showMessageDialog(this,
                            "Nem sikerült törölni a készletadatokat.",
                            "Hiba",
                            JOptionPane.ERROR_MESSAGE);
                    return;
                }
            }
            
            // Aztán töröljük a terméket
            if (inventoryService.deleteProduct(selected.getId())) {
                JOptionPane.showMessageDialog(this,
                        "A termék sikeresen törölve.",
                        "Siker",
                        JOptionPane.INFORMATION_MESSAGE);
                loadProducts(-1);
            } else {
                JOptionPane.showMessageDialog(this,
                        "Nem sikerült törölni a terméket.",
                        "Hiba",
                        JOptionPane.ERROR_MESSAGE);
            }
        } catch (Exception ex) {
            JOptionPane.showMessageDialog(this,
                    "Hiba történt a törlés során:\n" + ex.getMessage(),
                    "Hiba",
                    JOptionPane.ERROR_MESSAGE);
        }
    }

    private Product buildProductFromForm(Product template) {
        String idText = productIdField.getText().trim();
        String name = nameField.getText().trim();
        String userIdText = userIdField.getText().trim();
        String imageUrl = imageUrlField.getText().trim();
        
        // Alkategória a ComboBox-ból (vagy beírt szövegből)
        Object subcatObj = subcategoryCombo.getSelectedItem();
        String subcatText = subcatObj != null ? subcatObj.toString().trim() : "";

        if (idText.isEmpty() || userIdText.isEmpty()) {
            throw new IllegalArgumentException("Add meg a termék és a felhasználó azonosítóját.");
        }
        if (name.isEmpty()) {
            throw new IllegalArgumentException("Add meg a termék nevét.");
        }

        // Kategória kezelése (lehet létező vagy új)
        Object categoryObj = categoryCombo.getSelectedItem();
        CategoryOption categoryOption;
        
        if (categoryObj instanceof CategoryOption) {
            categoryOption = (CategoryOption) categoryObj;
        } else if (categoryObj instanceof String) {
            String categoryName = ((String) categoryObj).trim();
            if (categoryName.isEmpty()) {
                throw new IllegalArgumentException("Add meg a kategóriát.");
            }
            
            // Keressük meg a kategóriát név szerint
            CategoryOption found = null;
            for (int i = 0; i < categoryCombo.getItemCount(); i++) {
                CategoryOption opt = categoryCombo.getItemAt(i);
                if (opt.getName().equalsIgnoreCase(categoryName)) {
                    found = opt;
                    break;
                }
            }
            
            // Ha nem találtuk, létrehozzuk az új kategóriát
            if (found == null) {
                Optional<CategoryOption> created = inventoryService.createCategory(categoryName);
                if (created.isPresent()) {
                    categoryOption = created.get();
                    // Frissítjük a kategória listát
                    final CategoryOption finalCategory = categoryOption;
                    SwingUtilities.invokeLater(() -> {
                        loadCategories();
                        categoryCombo.setSelectedItem(finalCategory);
                    });
                } else {
                    throw new IllegalArgumentException(
                        "Nem sikerült létrehozni a kategóriát: " + categoryName);
                }
            } else {
                categoryOption = found;
            }
        } else {
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
        Category category = new Category(categoryOption.getId(), categoryOption.getName(), subcategory);

        return new Product(id, name, userId, category, imageUrl.isBlank() ? null : imageUrl, subcategory.getId());
    }
    
    private boolean saveSupplyData(int productId) {
        try {
            String dateStr = purchaseDateField.getText().trim();
            String purchasePriceStr = purchasePriceField.getText().trim();
            String sellPriceStr = sellPriceField.getText().trim();
            
            // Ha minden mező üres, nem mentünk Supply-t
            if (dateStr.isEmpty() && purchasePriceStr.isEmpty() && sellPriceStr.isEmpty()) {
                return true;
            }
            
            // Ha valamelyik ki van töltve, akkor mindhárom kötelező
            if (dateStr.isEmpty() || purchasePriceStr.isEmpty() || sellPriceStr.isEmpty()) {
                throw new IllegalArgumentException(
                    "Add meg az összes beszerzési adatot (dátum, beszerzési ár, eladási ár) vagy hagyd őket üresen.");
            }
            
            LocalDate purchaseDate = LocalDate.parse(dateStr, DATE_FORMAT);
            int purchasePrice = Integer.parseInt(purchasePriceStr);
            int sellPrice = Integer.parseInt(sellPriceStr);
            
            if (purchasePrice < 0 || sellPrice < 0) {
                throw new IllegalArgumentException("Az árak nem lehetnek negatívak.");
            }
            
            if (sellPrice < purchasePrice) {
                int confirm = JOptionPane.showConfirmDialog(this,
                    "Az eladási ár alacsonyabb a beszerzési árnál. Biztosan így akarod menteni?",
                    "Megerősítés",
                    JOptionPane.YES_NO_OPTION,
                    JOptionPane.WARNING_MESSAGE);
                if (confirm != JOptionPane.YES_OPTION) {
                    return false;
                }
            }
            
            // Darabszám: alapértelmezett 1
            Supply supply = new Supply(productId, purchaseDate, 1, purchasePrice, sellPrice);
            return inventoryService.upsertSupply(supply);
            
        } catch (DateTimeParseException ex) {
            JOptionPane.showMessageDialog(this,
                "Hibás dátum formátum. Használd: yyyy-MM-dd (pl. 2025-11-11)",
                "Formátum hiba",
                JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (NumberFormatException ex) {
            JOptionPane.showMessageDialog(this,
                "Az áraknak egész számoknak kell lenniük.",
                "Formátum hiba",
                JOptionPane.ERROR_MESSAGE);
            return false;
        } catch (IllegalArgumentException ex) {
            JOptionPane.showMessageDialog(this, ex.getMessage(), "Hiba", JOptionPane.ERROR_MESSAGE);
            return false;
        }
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
        subcategoryCombo.setEnabled(editable);
        purchaseDateField.setEditable(editable);
        purchasePriceField.setEditable(editable);
        sellPriceField.setEditable(editable);
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
            // Ellenőrizzük, hogy létezik-e már az alkategória ezzel a gyártóval
            Optional<Subcategory> exactMatch = inventoryService
                    .findSubcategoryByNameAndManufacturer(namePart, manufacturerPart);
            if (exactMatch.isPresent()) {
                return exactMatch.get();
            }

            // Ha nem létezik, próbáljuk meg létrehozni (a createSubcategory létrehozza a gyártót is, ha kell)
            Optional<Subcategory> created = inventoryService.createSubcategory(namePart, manufacturerPart);
            if (created.isPresent()) {
                return created.get();
            }
            
            // Ha a createSubcategory sem tudta létrehozni, akkor a gyártót kell előbb létrehoznunk
            throw new IllegalArgumentException(
                    "Nem sikerült létrehozni az alkategóriát. Lehet, hogy a gyártó (" + 
                    manufacturerPart + ") nem szerepel az adatbázisban.");
        }

        // Ha nincs gyártó megadva zárójelben
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

        // Alapértelmezett könyvtár: src/resources/images
        File defaultDir = new File("src/resources/images");
        if (defaultDir.exists() && defaultDir.isDirectory()) {
            chooser.setCurrentDirectory(defaultDir);
        } else {
            // Ha nem létezik, próbáljuk meg a jelenlegi útvonalból
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
        }

        int result = chooser.showOpenDialog(this);
        if (result == JFileChooser.APPROVE_OPTION) {
            File selectedFile = chooser.getSelectedFile();
            if (selectedFile != null) {
                String path = selectedFile.getAbsolutePath();
                
                // Átalakítás classpath formátumra, ha a resources mappán belül van
                String classpathPath = convertToClasspathFormat(path);
                imageUrlField.setText(classpathPath);
            }
        }
    }
    
    /**
     * Átalakítja az abszolút elérési utat classpath formátumra
     */
    private String convertToClasspathFormat(String absolutePath) {
        // Normalizálás: backslash → forward slash
        String normalized = absolutePath.replace('\\', '/');
        
        // Keressük meg a "resources" mappát az elérési útban
        int resourcesIndex = normalized.indexOf("/resources/");
        if (resourcesIndex >= 0) {
            // Ha megtaláltuk, vágjuk le az elejét és tegyük classpath: elé
            String relativePath = normalized.substring(resourcesIndex + "/resources".length());
            return "classpath:" + relativePath;
        }
        
        // Alternatív elérési út keresés: src/resources/images
        int srcResourcesIndex = normalized.indexOf("src/resources/");
        if (srcResourcesIndex >= 0) {
            String relativePath = normalized.substring(srcResourcesIndex + "src/resources".length());
            return "classpath:" + relativePath;
        }
        
        // Ha nem találtuk a resources mappát, adjuk vissza az eredeti útvonalat
        // (de javasoljuk, hogy a resources mappába tegyék a képeket)
        return absolutePath;
    }
}