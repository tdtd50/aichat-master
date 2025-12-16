package com.cxt.AiChat.service.impl;

import com.cxt.AiChat.model.ChatRoom;
import com.cxt.AiChat.service.AiManage;
import com.cxt.AiChat.service.ChatService;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;
import com.volcengine.ark.runtime.model.completion.chat.ChatMessageRole;
import org.springframework.stereotype.Service;

import jakarta.annotation.Resource;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class ChatServiceImpl implements ChatService {

    @Resource
    private AiManage aiManage;

    // 保存每个房间的聊天记录
    private final Map<Long, List<ChatMessage>> chatHistories = new ConcurrentHashMap<>();

    // 系统提示词
    private final String systemPrompt = "你是一个智能助手";

    @Override
    public String doChat(long roomId, String userPrompt) {

        // 获取或创建房间历史
        List<ChatMessage> messages = chatHistories.get(roomId);

        if (messages == null) {
            // 房间不存在,创建新房间
            messages = new ArrayList<>();

            // 添加系统消息
            ChatMessage systemMessage = ChatMessage.builder()
                    .role(ChatMessageRole.SYSTEM)
                    .content(systemPrompt)
                    .build();
            messages.add(systemMessage);

            // 保存到历史记录
            chatHistories.put(roomId, messages);
        }

        // 添加用户消息
        ChatMessage userMessage = ChatMessage.builder()
                .role(ChatMessageRole.USER)
                .content(userPrompt)
                .build();
        messages.add(userMessage);

        // 调用 AI
        String answer = aiManage.doChat(messages);

        // 添加 AI 回复
        ChatMessage answerMessage = ChatMessage.builder()
                .role(ChatMessageRole.ASSISTANT)
                .content(answer)
                .build();
        messages.add(answerMessage);

        // 如果 AI 回复中出现"游戏结束",清除该房间历史
        if (answer.contains("游戏结束")) {
            chatHistories.remove(roomId);
        }

        return answer;
    }

    @Override
    public List<ChatRoom> getChatRoomList() {
        List<ChatRoom> chatRoomList = new ArrayList<>();
        for (Map.Entry<Long, List<ChatMessage>> entry : chatHistories.entrySet()) {
            ChatRoom chatRoom = new ChatRoom();
            chatRoom.setRoomId(entry.getKey());
            List<ChatMessage> messages = entry.getValue();
            chatRoom.setChatMessageList(messages);
            chatRoom.setMessageCount(messages.size());
            chatRoomList.add(chatRoom);
        }
        return chatRoomList;
    }
}