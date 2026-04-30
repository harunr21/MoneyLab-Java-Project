package application;

import java.io.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class FileManager {
    private static final String PROFILE_LIST_FILE = "profiles.txt";

    public static void saveProfileName(String name) {
        try (FileWriter fw = new FileWriter(PROFILE_LIST_FILE, true);
             PrintWriter pw = new PrintWriter(fw)) {
            pw.println(name);
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static List<String> loadProfileList() {
        List<String> profiles = new ArrayList<>();
        File file = new File(PROFILE_LIST_FILE);
        if (!file.exists()) return profiles;
        try (Scanner scanner = new Scanner(file)) {
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (!line.isEmpty()) profiles.add(line);
            }
        } catch (FileNotFoundException e) { e.printStackTrace(); }
        return profiles;
    }

    public static void deleteProfile(String name) {
        List<String> profiles = loadProfileList();
        profiles.remove(name);
        try (PrintWriter pw = new PrintWriter(new FileWriter(PROFILE_LIST_FILE))) {
            for (String p : profiles) pw.println(p);
        } catch (IOException e) { e.printStackTrace(); }
        File profileData = new File(name + ".txt");
        if (profileData.exists()) profileData.delete();
    }

    public static void saveTransaction(String profileName, Transaction tx) {
        Profile p = loadProfile(profileName);
        p.addTransaction(tx);
        rewriteProfileData(p);
    }

    public static void rewriteProfileData(Profile profile) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(profile.getName() + ".txt"))) {
            writer.println("[TRANSACTIONS]");
            for (Transaction t : profile.getTransactions()) {
                writer.println(t.getType() + "|" + t.getDescription() + "|" + t.getAmount() + "|" + t.getStartDate() + "|" + t.getFrequency());
            }
            writer.println("[GOALS]");
            for (Goal g : profile.getGoals()) {
                writer.println(g.getName() + "|" + g.getTargetAmount() + "|" + g.getSavedAmount() + "|" + g.isCompleted());
            }
        } catch (IOException e) { e.printStackTrace(); }
    }

    public static Profile loadProfile(String profileName) {
        Profile profile = new Profile(profileName);
        File file = new File(profileName + ".txt");
        if (!file.exists()) return profile;

        try (Scanner scanner = new Scanner(file)) {
            String currentSection = "";
            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();
                if (line.isEmpty()) continue;
                if (line.equals("[TRANSACTIONS]")) { currentSection = "TRANSACTIONS"; continue; }
                if (line.equals("[GOALS]")) { currentSection = "GOALS"; continue; }

                String[] parts = line.split("\\|");
                if (currentSection.equals("TRANSACTIONS") && parts.length == 5) {
                    String type = parts[0];
                    String desc = parts[1];
                    double amount = Double.parseDouble(parts[2]);
                    LocalDate date = LocalDate.parse(parts[3]);
                    String freq = parts[4];
                    if (type.equals("INCOME")) profile.addTransaction(new Income(desc, amount, date, freq));
                    else profile.addTransaction(new Expense(desc, amount, date, freq));
                } else if (currentSection.equals("GOALS") && parts.length == 4) {
                    Goal g = new Goal(parts[0], Double.parseDouble(parts[1]));
                    g.addFunds(Double.parseDouble(parts[2]));
                    g.setCompleted(Boolean.parseBoolean(parts[3]));
                    profile.addGoal(g);
                }
            }
        } catch (FileNotFoundException e) { e.printStackTrace(); }
        return profile;
    }
}