package com.schinkennugget;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.resource.language.I18n;

import java.io.*;
import java.lang.reflect.Type;
import java.nio.file.Path;
import java.util.*;

public class BlockabularyFileManager {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path CONFIG_DIR = FabricLoader.getInstance().getConfigDir();
    private static final File VOCAB_FILE = CONFIG_DIR.resolve("vocab.json").toFile();
    private static final File TXT_FILE = CONFIG_DIR.resolve("vocab.txt").toFile();


    public static List<Map<String, String>> dataFromJson;
    public static List<Map<String, String>> orderedData;
    public static String currentQuestion = null;
    public static String currentAnswer = null;
    //public static boolean noDataLoaded = dataFromJson == null || dataFromJson.isEmpty();

    public static void init() {
        //if (!VOCAB_FILE.exists()) {
        //    createDefaultConfig();
        //}
        loadVocab();
    }

    private static void createDefaultConfig() {
        try {
            if (!VOCAB_FILE.getParentFile().exists()) {
                VOCAB_FILE.getParentFile().mkdirs();
            }

            String defaultJson = """
            {
              "dataFromTxt": [
                {
                  "question": "The config-file for Blockabulary is empty.",
                  "answer": "",
                  "timesAsked": "0",
                  "timesAnsweredCorrectly": "0"
                },
                {
                  "question": "The config-file for Blockabulary is empty.",
                  "answer": "",
                  "timesAsked": "0",
                  "timesAnsweredCorrectly": "0"
                }
              ]
            }""";

            FileWriter writer = new FileWriter(VOCAB_FILE);
            writer.write(defaultJson);
            writer.close();
            Blockabulary.LOGGER.info("Default config file created: " + VOCAB_FILE.getAbsolutePath());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void loadVocab() {
        System.out.println("executing loadVocab");
        try {
            FileReader reader = new FileReader(VOCAB_FILE);
            Type type = new TypeToken<Map<String, List<Map<String, String>>>>() {}.getType();
            Map<String, List<Map<String, String>>> data = GSON.fromJson(reader, type);
            reader.close();
            
            dataFromJson = data.get("data");
        } catch (IOException e) {
            e.printStackTrace();
            Blockabulary.LOGGER.info("vocab.json file was not found");
        }
    }

    public static String loadTxt() {
        if (TXT_FILE.exists()) {
            if (!VOCAB_FILE.exists()) {
                try (BufferedReader reader = new BufferedReader(new FileReader(TXT_FILE))){
                    String line;
                    List<String> dataFromTxt = new ArrayList<>();
                    List<Map<String, String>> dataForJson = new ArrayList<>();
                    String info = "";
                    int numberOfDataLoaded = 0;

                    while ((line = reader.readLine()) != null) {
                        if (!line.trim().isEmpty()) {
                            dataFromTxt.add(line.trim());
                        }
                    }

                    for (int i = 0; i < dataFromTxt.size() - 1; i += 2) {
                        String question = dataFromTxt.get(i);
                        String answer = dataFromTxt.get(i + 1);
                        Map<String, String> entry = new HashMap<>();
                        entry.put("question", question);
                        entry.put("answer", answer);
                        entry.put("timesAsked", "0");
                        entry.put("timesAnsweredCorrectly", "0");
                        dataForJson.add(entry);
                        info = info + "Loaded question " + question + "\nLoaded answer "+answer +"\n\n";
                        numberOfDataLoaded++;
                    }


                    //saving to json file
                    Map<String, List<Map<String, String>>> finalData = new HashMap<>();
                    finalData.put("data", dataForJson);

                    try (FileWriter writer = new FileWriter(VOCAB_FILE)){
                        GSON.toJson(finalData, writer);
                    } catch (IOException e) {
                        e.printStackTrace();
                        return "Failed to write the json file, because it wasn't found";
                    }
                    dataFromJson = dataForJson;
                    return info + "Loaded and saved " + numberOfDataLoaded + " questions and answers";

                } catch (IOException e) {
                    Blockabulary.LOGGER.info("vocab.txt file could not be loaded, because it wasn't found.");
                    e.printStackTrace();
                    return (I18n.translate("message.blockabulary.no_txt_found") + " and you managed to throw an IOException, how tf did you do that");
                }

            } else {
                return I18n.translate("message.blockabulary.cant_overwrite_vocab");
            }
        } else {
            return I18n.translate("message.blockabulary.no_txt_found");
        }


    }

    private static Map<String, String> selected;

    public static List<Map<String, String>> getOrderedData() {
        if (orderedData == null) {
            loadVocab();
            System.out.println("ececuting getOrderedData() (oben)");
            orderedData = new ArrayList<>(dataFromJson);
            Collections.shuffle(orderedData);
        }
        System.out.println("ececuting getOrderedData()");
        System.out.println(orderedData);
        return orderedData;
    }

    public static String getQuestion() {
        System.out.println("executing getQuestion()");
        loadVocab();

        if (dataFromJson == null || dataFromJson.isEmpty()) {
            return "vocab.json is empty or does not exist";
        }


        //Random random = new Random();
        //Map<String, String> selected = dataFromJson.get(random.nextInt(dataFromJson.size()));
        selected = getOrderedData().get(0);

        if (orderedData == null || orderedData.isEmpty()) {
            return "An error occured. The vocab could not be loaded. (Error 43)";
        }
        for (int i = 0; i < orderedData.size(); i++) {
            if (orderedData.get(i) == null) {
                return ("An error occurred. The vocab could not be loaded correctly. (Error 42." + i + ")");
            }
        }

        currentQuestion = selected.get("question");
        currentAnswer = selected.get("answer");

        System.out.println("ende: "+orderedData);

        return currentQuestion;
    }

    public static void reorderData(int newOrder) { //negative: remove, every other digit: where the number gets moved to
        if (newOrder < 0) {
            orderedData.remove(0);
            if (orderedData == null || orderedData.isEmpty()) {
                BlockabularyChatMessages.sendLocalMessage(I18n.translate("message.blockabulary.finished_stack"));
            }
        } else if (newOrder <= orderedData.size()) {
            orderedData.remove(0);
            orderedData.add(newOrder, selected);
        } else {
            orderedData.remove(0);
            orderedData.add(orderedData.size(), selected);
        }
    }

    public static void writeQuestionStats (boolean right) {
        loadVocab();
        if (dataFromJson.contains(selected)) {

            List<Map<String, String>> dataForJson = new ArrayList<>(dataFromJson);
            dataForJson.remove(selected);

            selected.replace("timesAsked", String.valueOf(Integer.valueOf(selected.get("timesAsked") + 1)));
            if (right) {
                selected.replace("timesAnsweredCorrectly", String.valueOf(Integer.valueOf(selected.get("timesAnsweredCorrectly") + 1)));
            }

            dataForJson.add(selected);

            Map<String, List<Map<String, String>>> finalData = new HashMap<>();
            finalData.put("data", dataForJson);

            try (FileWriter writer = new FileWriter(VOCAB_FILE)){
                GSON.toJson(finalData, writer);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
                System.err.println("Failed to write the json file, because it wasn't found");
            }
        }
    }
}
