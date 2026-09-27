package com.gamezone.persistence;

import com.gamezone.model.BulkPurchaseDiscount;
import com.gamezone.model.CategoryDiscount;
import com.gamezone.model.PercentageDiscount;
import com.gamezone.model.Promotion;
import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Handles persistence operations for promotions, storing and retrieving
 * them from a CSV file. A discriminator column ("PERCENTAGE", "CATEGORY"
 * or "BULK") is used to tell the three concrete promotion types apart
 * when reloading the file.
 */
public class PromotionDAO {

    private static final String FILE_PATH = "data/promotions.csv";

    /**
     * Persists the full list of promotions, overwriting the file's
     * previous content.
     *
     * @param promotions the complete list of promotions to save
     */
    public void saveAll(List<Promotion> promotions) {
        File dir = new File("data");
        if (!dir.exists()) dir.mkdirs();

        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Promotion p : promotions) {
                if (p instanceof PercentageDiscount) {
                    PercentageDiscount pd = (PercentageDiscount) p;
                    writer.println("PERCENTAGE," + pd.getId() + "," + pd.getName() + ","
                            + pd.getStartDate() + "," + pd.getEndDate() + "," + pd.getPercentage());
                } else if (p instanceof CategoryDiscount) {
                    CategoryDiscount cd = (CategoryDiscount) p;
                    writer.println("CATEGORY," + cd.getId() + "," + cd.getName() + ","
                            + cd.getStartDate() + "," + cd.getEndDate() + ","
                            + cd.getPercentage() + "," + cd.getTargetCategory());
                } else if (p instanceof BulkPurchaseDiscount) {
                    BulkPurchaseDiscount bd = (BulkPurchaseDiscount) p;
                    writer.println("BULK," + bd.getId() + "," + bd.getName() + ","
                            + bd.getStartDate() + "," + bd.getEndDate() + ","
                            + bd.getMinQuantity() + "," + bd.getPercentage());
                }
            }
        } catch (IOException e) {
            System.out.println("Error saving promotions: " + e.getMessage());
        }
    }

    /**
     * Returns all promotions currently stored in the file.
     *
     * @return the list of all persisted promotions, or an empty list
     *         if the file does not exist yet
     */
    public List<Promotion> loadAll() {
        List<Promotion> promotions = new ArrayList<>();
        File file = new File(FILE_PATH);
        if (!file.exists()) return promotions;

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) continue;
                try {
                    Promotion p = parseLine(line);
                    if (p != null) promotions.add(p);
                } catch (Exception e) {
                    System.out.println("Skipping corrupted line: " + line);
                }
            }
        } catch (IOException e) {
            System.out.println("Error reading promotions: " + e.getMessage());
        }
        return promotions;
    }

    private Promotion parseLine(String line) {
        String[] parts = line.split(",", -1);
        String type = parts[0];
        String id = parts[1];
        String name = parts[2];
        LocalDate startDate = LocalDate.parse(parts[3]);
        LocalDate endDate = LocalDate.parse(parts[4]);

        if (type.equals("PERCENTAGE")) {
            double percentage = Double.parseDouble(parts[5]);
            return new PercentageDiscount(id, name, startDate, endDate, percentage);
        } else if (type.equals("CATEGORY")) {
            double percentage = Double.parseDouble(parts[5]);
            String targetCategory = parts[6];
            return new CategoryDiscount(id, name, startDate, endDate, percentage, targetCategory);
        } else if (type.equals("BULK")) {
            int minQuantity = Integer.parseInt(parts[5]);
            double percentage = Double.parseDouble(parts[6]);
            return new BulkPurchaseDiscount(id, name, startDate, endDate, minQuantity, percentage);
        }
        return null;
    }
}