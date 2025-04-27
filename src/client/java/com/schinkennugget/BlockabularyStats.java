package com.schinkennugget;

// import com.google.gson.Gson;
// import com.google.gson.GsonBuilder;
// import com.google.gson.JsonObject;
// import net.fabricmc.loader.api.FabricLoader;
//
// import java.io.IOException;
// import java.io.Reader;
// import java.io.Writer;
// import java.nio.file.Files;
// import java.nio.file.Path;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.loader.api.FabricLoader;

import java.io.*;
import java.nio.file.Path;

public class BlockabularyStats {

    private static final Gson GSON_WRITER = new GsonBuilder().setPrettyPrinting().create();
    private static final Gson GSON_READER = new Gson();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final File STATS_FILE = CONFIG_DIR.resolve("blockabulary_stats.json").toFile();
    private static int totalQuestions;
    private static int rightAnswers;
    private static int wrongCapitalization0;
    private static int wrongCapitalization1;
    private static int wrongCapitalization2;
    private static int wrongAnswers;
    private static int skippedQuestions;


    public static void init() {
        if (!STATS_FILE.exists()) {
            createDefaultStatsFile();
        }
    }

    private static void createDefaultStatsFile() {
        try {
            if (!STATS_FILE.getParentFile().exists()) {
                STATS_FILE.getParentFile().mkdirs();
            }

            String defaultStatsFile = """
                    {
                      "stats": [
                        {
                          "totalQuestions": 0,
                          "rightAnswers": 0,
                          "wrongCapitalization0": 0,
                          "wrongCapitalization1": 0,
                          "wrongCapitalization2": 0,
                          "wrongAnswers": 0,
                          "skippedQuestions": 0
                        }
                      ]
                    }""";

            FileWriter writer = new FileWriter(STATS_FILE);
            writer.write(defaultStatsFile);
            writer.close();
            System.out.println("Default stats file created: " + STATS_FILE.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }

    }


    public static void loadStats () {
        try (FileReader reader = new FileReader(STATS_FILE)) {
            JsonObject jsonObject = GSON_READER.fromJson(reader, JsonObject.class);
            JsonArray statsArray = jsonObject.getAsJsonArray("stats");

            if (statsArray != null && !statsArray.isEmpty()) {
                JsonObject stats = statsArray.get(0).getAsJsonObject();

                totalQuestions = stats.get("totalQuestions").getAsInt();
                rightAnswers = stats.get("rightAnswers").getAsInt();
                wrongCapitalization0 = stats.get("wrongCapitalization0").getAsInt();
                wrongCapitalization1 = stats.get("wrongCapitalization1").getAsInt();
                wrongCapitalization2 = stats.get("wrongCapitalization2").getAsInt();
                wrongAnswers = stats.get("wrongAnswers").getAsInt();
                skippedQuestions = stats.get("skippedQuestions").getAsInt();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static int getTotalQuestions() {
        loadStats();
        return totalQuestions;
    }

    public static int getRightAnswers() {
        loadStats();
        return rightAnswers;
    }

    public static int getWrongCapitalization0() {
        loadStats();
        return wrongCapitalization0;
    }

    public static int getWrongCapitalization1() {
        loadStats();
        return wrongCapitalization1;
    }

    public static int getWrongCapitalization2() {
        loadStats();
        return wrongCapitalization2;
    }

    public static int getWrongAnswers() {
        loadStats();
        return wrongAnswers;
    }

    public static int getSkippedQuestions() {
        loadStats();
        return skippedQuestions;
    }


    public static void setStats (String key, int newValue) {
        try {
            FileReader reader = new FileReader(STATS_FILE);
            JsonObject root = GSON_WRITER.fromJson(reader, JsonObject.class);
            reader.close();

            JsonArray statsArray = root.getAsJsonArray("stats");
            if (statsArray != null && !statsArray.isEmpty()) {
                JsonObject stats = statsArray.get(0).getAsJsonObject();

                if (stats.has(key)) {
                    stats.addProperty(key, newValue);
                    Blockabulary.LOGGER.info("Updated " + key + " to " + newValue);
                } else {
                    Blockabulary.LOGGER.info(key + " does not exist");
                }

                FileWriter writer = new FileWriter(STATS_FILE);
                GSON_WRITER.toJson(root, writer);
                writer.flush();
                writer.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

//    public static void loadStats(){
//        try {
//            FileReader reader = new FileReader(STATS_FILE);
//            Type type = new TypeToken<Map<String, List<Map<String, Integer>>>>() {}.getType();
//            Map<String, List<Map<String, Integer>>> statsData = GSON.fromJson(reader, type);
//            reader.close();
//
//            stats = statsData.get("stats");
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }
//
//    public static int getTotalQuestions() {
//        loadStats();
//
//        if (stats == null || stats.isEmpty()) {
//            return -38808;
//        }
//
//        Map<String, Integer> totalQuestions = stats.get(0);
//
//        VocabularyTrainerStats.totalQuestions = totalQuestions.get("totalQuestions");
//        totalQuestions.clear();
//
//        return VocabularyTrainerStats.totalQuestions;
//    }
//
//    public static int getRightAnswers() {
//        loadStats();
//
//        if (stats == null || stats.isEmpty()) {
//            return -38808;
//        }
//
//        Map<String, Integer> rightAnswers = stats.get(1);
//
//        VocabularyTrainerStats.rightAnswers = rightAnswers.get("rightAnswers");
//        rightAnswers.clear();
//
//        return VocabularyTrainerStats.rightAnswers;
//    }
//
//    public static int getWrongCapitalization0() {
//        loadStats();
//
//        if (stats == null || stats.isEmpty()) {
//            return -38808;
//        }
//
//        Map<String, Integer> wrongCapitalization0 = stats.get(2);
//
//        VocabularyTrainerStats.wrongCapitalization0 = wrongCapitalization0.get("wrongCapitalization0");
//        wrongCapitalization0.clear();
//
//        return VocabularyTrainerStats.wrongCapitalization0;
//    }
//
//    public static int getWrongCapitalization1() {
//        loadStats();
//
//        if (stats == null || stats.isEmpty()) {
//            return -38808;
//        }
//
//        Map<String, Integer> wrongCapitalization1 = stats.get(3);
//
//        VocabularyTrainerStats.wrongCapitalization1 = wrongCapitalization1.get("wrongCapitalization1");
//        wrongCapitalization1.clear();
//
//        return VocabularyTrainerStats.wrongCapitalization1;
//    }
//
//    public static int getWrongCapitalization2() {
//        loadStats();
//
//        if (stats == null || stats.isEmpty()) {
//            return -38808;
//        }
//
//        Map<String, Integer> wrongCapitalization2 = stats.get(4);
//
//        VocabularyTrainerStats.wrongCapitalization2 = wrongCapitalization2.get("wrongCapitalization2");
//        wrongCapitalization2.clear();
//
//        return VocabularyTrainerStats.wrongCapitalization2;
//    }
//
//    public static int getWrongAnswers() {
//        loadStats();
//
//        if (stats == null || stats.isEmpty()) {
//            return -38808;
//        }
//
//        Map<String, Integer> wrongAnswers = stats.get(5);
//
//        VocabularyTrainerStats.wrongAnswers = wrongAnswers.get("wrongAnswers");
//        wrongAnswers.clear();
//
//        return VocabularyTrainerStats.wrongAnswers;
//    }
//
//    public static int getSkippedQuestions() {
//        loadStats();
//
//        if (stats == null || stats.isEmpty()) {
//            return -38808;
//        }
//
//        Map<String, Integer> skippedQuestions = stats.get(5);
//
//        VocabularyTrainerStats.skippedQuestions = skippedQuestions.get("skippedQuestions");
//        skippedQuestions.clear();
//
//        return VocabularyTrainerStats.skippedQuestions;
//    }
}

