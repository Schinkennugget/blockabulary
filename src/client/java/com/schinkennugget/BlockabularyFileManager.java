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


    private static List<Map<String, String>> data;
    public static String currentQuestion = null;
    public static String currentAnswer = null;

    public static void init() {
        //if (!VOCAB_FILE.exists()) {
        //    createDefaultConfig();
        //}
        loadConfig();
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

    private static void loadConfig() {
        try {
            FileReader reader = new FileReader(VOCAB_FILE);
            Type type = new TypeToken<Map<String, List<Map<String, String>>>>() {}.getType();
            Map<String, List<Map<String, String>>> data = GSON.fromJson(reader, type);
            reader.close();
            
            BlockabularyFileManager.data = data.get("data");
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
                    return info + "Loaded and saved " + String.valueOf(numberOfDataLoaded) + " questions and answers";

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


    public static String getRandomQuestion() {
        loadConfig();

        if (data == null || data.isEmpty()) {
            return "vocab.json is empty or does not exist";
        }

        Random random = new Random();



        Map<String, String> selected = data.get(random.nextInt(data.size()));

        currentQuestion = selected.get("question");
        currentAnswer = selected.get("answer");

        selected.clear();

        return currentQuestion;
    }
}
