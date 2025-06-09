package com.example.sklepbezglutenowy;

import android.content.Context;

import com.example.sklepbezglutenowy.models.Product;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ProductFileHelper {
    private static final String FILE_NAME = "products.txt";

    public static void addProduct(Context context, String name, String price, String description) throws IOException {
        List<String[]> products = getProducts(context);
        int newId = getNextId(products);
        String line = newId + "|" + name + "|" + price + "|" + description + "\n";

        FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_APPEND);
        fos.write(line.getBytes());
        fos.close();
    }
    public static List<Product> getProductList(Context context) throws IOException {
        List<String[]> rawProducts = getProducts(context);
        List<Product> productList = new ArrayList<>();

        for (String[] p : rawProducts) {
            if (p.length == 4) {
                try {
                    int id = Integer.parseInt(p[0]);
                    String name = p[1];
                    double price = Double.parseDouble(p[2]);
                    String description = p[3];
                    productList.add(new Product(id, name, price, description));
                } catch (NumberFormatException e) {
                    e.printStackTrace(); // skip invalid entries
                }
            }
        }

        return productList;
    }
    public static List<String[]> getProducts(Context context) throws IOException {
        List<String[]> products = new ArrayList<>();
        File file = new File(context.getFilesDir(), FILE_NAME);

        if (!file.exists()) return products;

        BufferedReader reader = new BufferedReader(new FileReader(file));
        String line;

        while ((line = reader.readLine()) != null) {
            products.add(line.split("\\|"));
        }

        reader.close();
        return products;
    }

    public static void updateProductById(Context context, int id, String name, String price, String description) throws IOException {
        List<String[]> products = getProducts(context);

        for (int i = 0; i < products.size(); i++) {
            if (Integer.parseInt(products.get(i)[0]) == id) {
                products.set(i, new String[]{String.valueOf(id), name, price, description});
                break;
            }
        }

        saveAll(context, products);
    }

    public static void deleteProductById(Context context, int id) throws IOException {
        List<String[]> products = getProducts(context);

        products.removeIf(product -> Integer.parseInt(product[0]) == id);

        saveAll(context, products);
    }

    private static void saveAll(Context context, List<String[]> products) throws IOException {
        FileOutputStream fos = context.openFileOutput(FILE_NAME, Context.MODE_PRIVATE);
        for (String[] product : products) {
            fos.write((String.join("|", product) + "\n").getBytes());
        }
        fos.close();
    }

    private static int getNextId(List<String[]> products) {
        int maxId = 0;
        for (String[] product : products) {
            int id = Integer.parseInt(product[0]);
            if (id > maxId) {
                maxId = id;
            }
        }
        return maxId + 1;
    }
}
