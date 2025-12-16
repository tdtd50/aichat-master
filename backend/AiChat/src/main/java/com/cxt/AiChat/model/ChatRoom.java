package com.cxt.AiChat.model;

import com.volcengine.ark.runtime.model.completion.chat.ChatMessage;

import java.util.List;

public class ChatRoom {

    private Long roomId;
    private List<ChatMessage> chatMessageList;

    // 可选：消息数量
    private Integer messageCount;

    public Long getRoomId() {
        return roomId;
    }

    public void setRoomId(Long roomId) {
        this.roomId = roomId;
    }

    public List<ChatMessage> getChatMessageList() {
        return chatMessageList;
    }

    public void setChatMessageList(List<ChatMessage> chatMessageList) {
        this.chatMessageList = chatMessageList;
    }

    public Integer getMessageCount() {
        return messageCount;
    }

    public void setMessageCount(Integer messageCount) {
        this.messageCount = messageCount;
    }
}
