package com.cxt.AiChat;

import com.cxt.AiChat.service.AiManage;
import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionRequest;
import com.volcengine.ark.runtime.model.bot.completion.chat.BotChatCompletionResult;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import com.volcengine.ark.runtime.service.ArkService;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class AiChatTest {

    @Resource
    private ArkService service;  // 注入配置类创建的 Bean

    @Resource
    private AiManage aiManage;

    @Test
    public void doTest() {
        // 测试方法1: 使用 systemPrompt 和 userPrompt
        System.out.println("\n----- 测试 doChat(systemPrompt, userPrompt) -----");
        String systemPrompt = "你是java编程专家";
        String userPrompt = "给我一个java示例程序";
        String answer = aiManage.doChat(systemPrompt, userPrompt);
        System.out.println(answer);

        // 测试方法2: 使用 List<ChatMessage>
        System.out.println("\n----- 测试 doChat(List<ChatMessage>) -----");
        final List<ChatMessage> messages = new ArrayList<>();
        final ChatMessage systemMessage = ChatMessage.builder()
                .role(ChatMessageRole.SYSTEM)
                .content("你是java编程专家")
                .build();
        final ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content("解释一下什么是Spring Bean")
                .build();
        messages.add(systemMessage);
        messages.add(userMessage);

        String answer2 = aiManage.doChat(messages);
        System.out.println(answer2);

//        System.out.println("\n----- standard request -----");
//        final List<ChatMessage> messages = new ArrayList<>();
//        final ChatMessage systemMessage = ChatMessage.builder()
//                .role(ChatMessageRole.SYSTEM)
//                .content("你是java编程专家")
//                .build();
//        final ChatMessage userMessage = ChatMessage.builder()
//                .role(ChatMessageRole.USER)
//                .content("给我一个java示例程序")
//                .build();
//        messages.add(systemMessage);
//        messages.add(userMessage);
//
//        BotChatCompletionRequest chatCompletionRequest = BotChatCompletionRequest.builder()
//                .botId("bot-20251129133556-r6jng")
//                .messages(messages)
//                .build();
//
//        BotChatCompletionResult chatCompletionResult = service.createBotChatCompletion(chatCompletionRequest);
//        chatCompletionResult.getChoices().forEach(choice ->
//                System.out.println(choice.getMessage().getContent())
//        );
//
//        // the references example
//        if (chatCompletionResult.getReferences() != null) {
//            chatCompletionResult.getReferences().forEach(ref ->
//                    System.out.println(ref.getUrl())
//            );
//        }

//        System.out.println("\n----- streaming request -----");
//        final List<ChatMessage> streamMessages = new ArrayList<>();
//        final ChatMessage streamSystemMessage = ChatMessage.builder()
//                .role(ChatMessageRole.SYSTEM)
//                .content("你是java编程专家")
//                .build();
//        final ChatMessage streamUserMessage = ChatMessage.builder()
//                .role(ChatMessageRole.USER)
//                .content("解释一下什么是Spring Bean")
//                .build();
//        streamMessages.add(streamSystemMessage);
//        streamMessages.add(streamUserMessage);
//
//        BotChatCompletionRequest streamChatCompletionRequest = BotChatCompletionRequest.builder()
//                .botId("bot-20251129133556-r6jng")
//                .messages(streamMessages)
//                .build();
//
//        service.streamBotChatCompletion(streamChatCompletionRequest)
//                .doOnError(Throwable::printStackTrace)
//                .blockingForEach(choice -> {
//                    if (choice.getReferences() != null && !choice.getReferences().isEmpty()) {
//                        choice.getReferences().forEach(ref -> System.out.println(ref.getUrl()));
//                    }
//                    if (!choice.getChoices().isEmpty()) {
//                        System.out.print(choice.getChoices().get(0).getMessage().getContent());
//                    }
//                });

        // shutdown service after all requests is finished
        service.shutdownExecutor();
    }
}