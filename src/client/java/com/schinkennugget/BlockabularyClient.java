package com.schinkennugget;

import net.fabricmc.api.ClientModInitializer;

public class BlockabularyClient implements ClientModInitializer {
	@Override
	public void onInitializeClient() {
		BlockabularyCommands.registerCommands();

		BlockabularyQuestionScheduler.init();

		BlockabularyFileManager.init();

		BlockabularyStats.init();
	}
}