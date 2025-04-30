package com.schinkennugget;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.resource.language.I18n;

import static com.schinkennugget.BlockabularyChatMessages.sendLocalMessage;

public class BlockabularyQuestionScheduler {
    private static final int TICKS_PER_SECOND = 20;
    public static int questionCycleTime = 180;
    private static int tickCounter = 0;
    public static boolean questionsEnabled = true;
    public static boolean isWaitingForAnswer = false;


    public static void init() {
        System.out.println("executing init");
        if(questionsEnabled) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                tickCounter++;

                if (BlockabularyFileManager.dataFromJson.isEmpty() || (BlockabularyFileManager.dataFromJson == null)) {
                    BlockabularyFileManager.loadVocab();
                }
                // ask again if the question wasn't answered for 20 sec
                if(tickCounter >= 400 && isWaitingForAnswer && questionsEnabled && !BlockabularyFileManager.dataFromJson.isEmpty() && !(BlockabularyFileManager.dataFromJson == null)) {
                    if (BlockabularyFileManager.orderedData == null || BlockabularyFileManager.orderedData.isEmpty() || BlockabularyFileManager.orderedData.contains(null)) {
                        sendLocalMessage("An error occurred. The vocab could not be loaded correctly. (Error 44)");
                        tickCounter = 0;
                    } else if (BlockabularyFileManager.getQuestion() == null || BlockabularyFileManager.currentAnswer == null) {
                        sendLocalMessage("An error occurred. The vocab could not be loaded correctly. (Error 45)");
                        System.err.println("An error occurred. The vocab could not be loaded correctly. (Error 45)");
                        tickCounter = 0;
                    } else {
                        sendLocalMessage("§b[Blockabulary]§r " + BlockabularyFileManager.getQuestion());
                        tickCounter = 0;
                        System.out.println("init 1");
                    }
                } else if (tickCounter >= 1000 && isWaitingForAnswer && questionsEnabled) {
                    sendLocalMessage("§b[Blockabulary]§r "+ I18n.translate("message.blockabulary.no_data_loaded"));
                    tickCounter = 0;
                    System.out.println("init 2");
                }

                // ask question
                if (tickCounter >= questionCycleTime * TICKS_PER_SECOND && !isWaitingForAnswer) {
                    if(questionsEnabled && !BlockabularyFileManager.dataFromJson.isEmpty() && !(BlockabularyFileManager.dataFromJson == null)) {
                        sendLocalMessage("§b[Blockabulary]§r " + BlockabularyFileManager.getQuestion());
                        isWaitingForAnswer = true;
                        setTickCounter(0);
                        System.out.println("init 3");
                    } else if (questionsEnabled) {
                        sendLocalMessage("§b[Blockabulary]§r "+ I18n.translate("message.blockabulary.no_data_loaded"));
                        isWaitingForAnswer = false;
                        System.out.println("dataFromJson"+BlockabularyFileManager.dataFromJson);
                        System.out.println("orderedData"+BlockabularyFileManager.orderedData);
                        System.out.println("init 4");
                    }
                    setTickCounter(0);
                }
            });
        }
    }


    public static void enableQuestions() {
        questionsEnabled = true;
        sendLocalMessage(I18n.translate("message.blockabulary.enabled"));

    }

    public static void disableQuestions() {
        questionsEnabled = false;
        sendLocalMessage(I18n.translate("message.blockabulary.disabled"));

    }

    public static void setTickCounter(int setTickCounter){
        tickCounter = setTickCounter;
    }
}
