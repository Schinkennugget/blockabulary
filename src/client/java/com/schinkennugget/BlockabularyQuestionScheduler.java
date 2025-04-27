package com.schinkennugget;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.minecraft.client.resource.language.I18n;

import static com.schinkennugget.BlockabularyChatMessages.sendLocalMessage;

public class BlockabularyQuestionScheduler {
    private static int tickCounter = 0;
    private static final int TICKS_PER_SECOND = 20;

    public static int questionCycleTime = 180;

    public static boolean questionsEnabled = true;

    public static boolean isWaitingForAnswer = false;

    public static void init() {
        if(questionsEnabled) {
            ClientTickEvents.END_CLIENT_TICK.register(client -> {
                tickCounter++;
                if(tickCounter >= 400 && isWaitingForAnswer && questionsEnabled) {
                    sendLocalMessage("§b[Blockabulary]§r "+ BlockabularyFileManager.currentQuestion);
                    tickCounter = 0;
                }
                if (tickCounter >= BlockabularyQuestionScheduler.questionCycleTime * TICKS_PER_SECOND && !isWaitingForAnswer) {
                    if(questionsEnabled) {
                        sendLocalMessage("§b[Blockabulary]§r " + BlockabularyFileManager.getRandomQuestion());
                        isWaitingForAnswer = true;

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
