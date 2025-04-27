package com.schinkennugget;

import net.minecraft.client.MinecraftClient;
import net.minecraft.text.Text;

public class BlockabularyChatMessages {
    public static void sendLocalMessage(String message) {
        MinecraftClient client = MinecraftClient.getInstance();
        if (client.player != null) {
            client.inGameHud.getChatHud().addMessage(Text.literal(message));
        }
    }
}

