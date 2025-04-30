package com.schinkennugget;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.IntegerArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandManager;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.minecraft.client.resource.language.I18n;
import net.minecraft.text.Text;

import static com.schinkennugget.BlockabularyChatMessages.sendLocalMessage;

public class BlockabularyCommands {

    public static int ignoreCapitalization = 1; //0=no, 1=only half a point, 2=yes

    public static void registerCommands() {
        ClientCommandRegistrationCallback.EVENT.register((dispatcher, registryAccess) -> {
            registerVocCommand(dispatcher);
        });
    }




    private static void registerVocCommand(CommandDispatcher dispatcher) {
        dispatcher.register(ClientCommandManager.literal("voc")
            .then(ClientCommandManager.literal("disable")
                .executes(context -> {
                    BlockabularyQuestionScheduler.disableQuestions();

                    return Command.SINGLE_SUCCESS;
                })

            )

            .then(ClientCommandManager.literal("enable")
                    .executes(context -> {
                        BlockabularyQuestionScheduler.enableQuestions();

                        return Command.SINGLE_SUCCESS;
                    })

            )

            .then(ClientCommandManager.literal("a")
                .then(ClientCommandManager.argument("answer", StringArgumentType.string())
                    .executes(context -> {
                            String answer = StringArgumentType.getString(context, "answer");

                            //no question asked
                            if(BlockabularyFileManager.currentQuestion == null || BlockabularyFileManager.currentAnswer == null || !BlockabularyQuestionScheduler.isWaitingForAnswer) {
                                sendLocalMessage(I18n.translate("message.blockabulary.no_question_asked"));

                            //right answer
                            } else if (answer.equals(BlockabularyFileManager.currentAnswer)) {
                                sendLocalMessage("'" + answer + "' " + I18n.translate("message.blockabulary.right_answer"));
                                BlockabularyStats.setStats("rightAnswers", BlockabularyStats.getRightAnswers()+1);
                                BlockabularyStats.setStats("totalQuestions", BlockabularyStats.getTotalQuestions()+1);
                                BlockabularyFileManager.reorderData(-1);
                                BlockabularyFileManager.writeQuestionStats(true);

                            //right answer, but with wrong capitalization
                            } else if(answer.equalsIgnoreCase(BlockabularyFileManager.currentAnswer)){
                                sendLocalMessage("'§6" + answer + "§r'" + " "+I18n.translate("message.blockabulary.wrong_capitalization") + " '§a" + BlockabularyFileManager.currentAnswer + "§r'");
                                BlockabularyStats.setStats("totalQuestions", BlockabularyStats.getTotalQuestions()+1);

                                if(ignoreCapitalization == 0){
                                    BlockabularyStats.setStats("wrongCapitalization0", BlockabularyStats.getWrongCapitalization0()+1);
                                    context.getSource().sendFeedback(Text.translatable("message.blockabulary.wrong_capitalization_0_info"));
                                    BlockabularyFileManager.reorderData(6);
                                    BlockabularyFileManager.writeQuestionStats(false);

                                } else if(ignoreCapitalization == 1){
                                    BlockabularyStats.setStats("wrongCapitalization1", BlockabularyStats.getWrongCapitalization1()+1);
                                    context.getSource().sendFeedback(Text.translatable("message.blockabulary.wrong_capitalization_1_info"));
                                    BlockabularyFileManager.reorderData(12);
                                    BlockabularyFileManager.writeQuestionStats(true);

                                } else if(ignoreCapitalization == 2){
                                    BlockabularyStats.setStats("wrongCapitalization2", BlockabularyStats.getWrongCapitalization2()+1);
                                    context.getSource().sendFeedback(Text.translatable("message.blockabulary.wrong_capitalization_2_info"));
                                    BlockabularyFileManager.reorderData(-1);
                                    BlockabularyFileManager.writeQuestionStats(true);
                                }

                            //wrong answer
                            } else {
                                sendLocalMessage("'§4" + answer + "§r'" + " "+ I18n.translate("message.blockabulary.wrong_answer") + " '§a" + BlockabularyFileManager.currentAnswer + "§r'");
                                BlockabularyStats.setStats("totalQuestions", BlockabularyStats.getTotalQuestions()+1);
                                BlockabularyStats.setStats("wrongAnswers", BlockabularyStats.getWrongAnswers()+1);
                                BlockabularyFileManager.reorderData(6);
                                BlockabularyFileManager.writeQuestionStats(false);
                            }

                            BlockabularyQuestionScheduler.setTickCounter(0);
                            BlockabularyQuestionScheduler.isWaitingForAnswer = false;
                            return Command.SINGLE_SUCCESS;
                        }
                    )
                )
            )

            .then(ClientCommandManager.literal("cycle_time")
                    .then(ClientCommandManager.argument("seconds", IntegerArgumentType.integer())
                            .executes(context -> {
                                        BlockabularyQuestionScheduler.questionCycleTime = IntegerArgumentType.getInteger(context, "seconds");

                                        sendLocalMessage(I18n.translate("message.blockabulary.cycle_time_changed_to") + " " + BlockabularyQuestionScheduler.questionCycleTime + "s");
                                        return Command.SINGLE_SUCCESS;
                                    }
                            )
                    )
            )

                .then(ClientCommandManager.literal("skip")
                        .executes(context -> {
                                    sendLocalMessage(BlockabularyFileManager.currentQuestion + I18n.translate("message.blockabulary.skipped") + BlockabularyFileManager.currentAnswer);
                                    BlockabularyQuestionScheduler.isWaitingForAnswer = false;
                                    BlockabularyQuestionScheduler.setTickCounter(0);
                                    BlockabularyStats.setStats("totalQuestions", BlockabularyStats.getTotalQuestions()+1);
                                    BlockabularyStats.setStats("skippedQuestions", BlockabularyStats.getSkippedQuestions()+1);
                            BlockabularyFileManager.writeQuestionStats(false);
                            BlockabularyFileManager.reorderData(6);
                                    return Command.SINGLE_SUCCESS;
                                }
                        )
                )


            .then(ClientCommandManager.literal("help")
                    .executes(context -> {
                        context.getSource().sendFeedback(Text.translatable("message.blockabulary.info"));
                                return Command.SINGLE_SUCCESS;
                            }
                    )
            )

            .then(ClientCommandManager.literal("stats")
                    .executes(context -> {
                                sendLocalMessage(I18n.translate("message.blockabulary.stats_header") + "\n" +
                                        I18n.translate("message.blockabulary.stats_total_questions_before") +
                                        BlockabularyStats.getTotalQuestions() +
                                        I18n.translate("message.blockabulary.stats_total_questions_after") + "\n" + I18n.translate("message.blockabulary.stats_right_answers_before") +
                                        BlockabularyStats.getRightAnswers() +
                                        I18n.translate("message.blockabulary.stats_right_answers_after") + "\n" +
                                        I18n.translate("message.blockabulary.stats_wrong_capitalization0_before") +
                                        BlockabularyStats.getWrongCapitalization0() +
                                        I18n.translate("message.blockabulary.stats_wrong_capitalization0_after") + "\n" +
                                        I18n.translate("message.blockabulary.stats_wrong_capitalization1_before") +
                                        BlockabularyStats.getWrongCapitalization1() +
                                        I18n.translate("message.blockabulary.stats_wrong_capitalization1_after") + "\n" +
                                        I18n.translate("message.blockabulary.stats_wrong_capitalization2_before") +
                                        BlockabularyStats.getWrongCapitalization2() +
                                        I18n.translate("message.blockabulary.stats_wrong_capitalization2_after") + "\n" +
                                        I18n.translate("message.blockabulary.stats_wrong_answers_before") +
                                        BlockabularyStats.getWrongAnswers() +
                                        I18n.translate("message.blockabulary.stats_wrong_answers_after") + "\n" +
                                        I18n.translate("message.blockabulary.stats_skipped_questions_before") +
                                        BlockabularyStats.getSkippedQuestions() +
                                        I18n.translate("message.blockabulary.stats_skipped_questions_after") + "\n" +
                                        I18n.translate("message.blockabulary.stats_end")
                                );
                                return Command.SINGLE_SUCCESS;
                            }
                    )
            )

            .then(ClientCommandManager.literal("load_txt")
                    .executes(context -> {
                                sendLocalMessage(BlockabularyFileManager.loadTxt());
                                return Command.SINGLE_SUCCESS;
                            }
                    )
            )
        );
    }
}