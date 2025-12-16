<template>
  <div class="chat-container">
    <!-- 顶部标题栏 -->
    <div class="header">
      <div class="header-content">
        <div class="logo">
          <div class="logo-icon">🧠</div>
          <span class="logo-text">AI 脑筋急转弯</span>
        </div>
        <div class="room-badge">
          <span class="room-label">房间号</span>
          <span class="room-number">{{ roomId }}</span>
        </div>
      </div>
    </div>

    <div class="main-content">
      <!-- 左侧历史记录面板 -->
      <div class="sidebar">
        <div class="sidebar-header">
          <span class="sidebar-title">历史对话</span>
          <button class="new-chat-btn" @click="loadCurrentChat">+ 新对话</button>
        </div>
        
        <div class="chat-list">
          <div class="chat-item active" @click="loadCurrentChat">
            <div class="chat-item-icon">💬</div>
            <div class="chat-item-content">
              <div class="chat-item-title">当前会话</div>
              <div class="chat-item-subtitle">房间 {{ roomId }}</div>
            </div>
          </div>
          
          <div 
            class="chat-item" 
            v-for="(chat, index) in savedChats" 
            :key="index"
            @click="loadSavedChat(index)"
          >
            <div class="chat-item-icon">📝</div>
            <div class="chat-item-content">
              <div class="chat-item-title">房间 {{ chat.roomId }}</div>
              <div class="chat-item-subtitle">{{ formatTime(chat.createTime) }}</div>
            </div>
          </div>

          <div v-if="savedChats.length === 0" class="empty-history">
            暂无历史对话
          </div>
        </div>
      </div>

      <!-- 右侧聊天区域 -->
      <div class="chat-area">
        <!-- 消息列表 -->
        <div class="messages-container" ref="messageContainer">
          <div 
            class="message" 
            v-for="(msg, index) in messages" 
            :key="index"
            :class="{ 'ai-message': msg.isAI, 'user-message': !msg.isAI }"
          >
            <template v-if="msg.isAI">
              <div class="avatar ai-avatar">
                <span class="avatar-text">AI</span>
              </div>
              <div class="message-bubble ai-bubble">{{ msg.content }}</div>
            </template>
            <template v-else>
              <div class="message-bubble user-bubble">{{ msg.content }}</div>
              <div class="avatar user-avatar">
                <span class="avatar-text">我</span>
              </div>
            </template>
          </div>
        </div>

        <!-- 底部输入区域 -->
        <div class="input-section">
          <div class="action-buttons">
            <button 
              class="action-btn start-btn" 
              @click="handleStart"
              :disabled="isGameStarted"
            >
              <span class="btn-icon">▶️</span>
              开始游戏
            </button>
            <button 
              class="action-btn end-btn" 
              @click="handleEnd"
              :disabled="isGameEnded"
            >
              <span class="btn-icon">⏹️</span>
              结束游戏
            </button>
          </div>
          
          <div class="input-wrapper">
            <input
              type="text"
              class="message-input"
              placeholder="输入你的问题..."
              v-model="userInput"
              @keyup.enter="handleSend"
              :disabled="isGameEnded"
            />
            <button 
              class="send-btn"
              @click="handleSend"
              :disabled="!isGameStarted || isGameEnded || !userInput.trim()"
            >
              <span class="send-icon">➤</span>
            </button>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick, watch } from 'vue'
import axios from 'axios'

// 创建axios实例
const request = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

// 生成6位数字房间号
const generateRoomId = () => {
  return Math.floor(100000 + Math.random() * 900000).toString()
}

// 状态管理
const roomId = ref(generateRoomId())
const messages = ref([])
const userInput = ref('')
const isGameStarted = ref(false)
const isGameEnded = ref(false)
const savedChats = ref([])
const messageContainer = ref(null)

// 从本地存储加载历史对话
const loadSavedChats = () => {
  const saved = localStorage.getItem('chatHistoryRecords')
  if (saved) {
    savedChats.value = JSON.parse(saved)
  }
}

// 保存当前对话到本地存储
const saveCurrentChat = () => {
  if (messages.value.length === 0) return
  
  const chatRecord = {
    roomId: roomId.value,
    createTime: new Date().toISOString(),
    messages: [...messages.value],
    isEnded: true
  }
  
  // 去重：避免重复保存同一房间
  const existingIndex = savedChats.value.findIndex(
    chat => chat.roomId === roomId.value
  )
  
  if (existingIndex >= 0) {
    savedChats.value[existingIndex] = chatRecord
  } else {
    savedChats.value.unshift(chatRecord) // 最新的放在最前面
  }
  
  // 限制最多保存10条记录
  if (savedChats.value.length > 10) {
    savedChats.value = savedChats.value.slice(0, 10)
  }
  
  // 保存到本地存储
  localStorage.setItem('chatHistoryRecords', JSON.stringify(savedChats.value))
}

// 加载保存的对话记录
const loadSavedChat = (index) => {
  const chat = savedChats.value[index]
  if (chat) {
    messages.value = [...chat.messages]
    roomId.value = chat.roomId
    isGameStarted.value = false
    isGameEnded.value = true
    scrollToBottom()
  }
}

// 加载当前对话（新建会话）
const loadCurrentChat = () => {
  messages.value = []
  roomId.value = generateRoomId()
  isGameStarted.value = false
  isGameEnded.value = false
  userInput.value = ''
}

// 格式化时间显示
const formatTime = (isoString) => {
  const date = new Date(isoString)
  const now = new Date()
  const diff = now - date
  const hours = Math.floor(diff / (1000 * 60 * 60))
  
  if (hours < 1) {
    return '刚刚'
  } else if (hours < 24) {
    return `${hours}小时前`
  } else if (hours < 48) {
    return '昨天'
  } else {
    return date.toLocaleDateString()
  }
}

// 调用后端发送消息
const sendMessageToBackend = async (prompt) => {
  try {
    const response = await request.post('/chat/doChat', null, {
      params: { 
        roomId: roomId.value,
        prompt: prompt 
      }
    })
    return response.data
  } catch (error) {
    console.error('发送消息失败:', error)
    return '抱歉，服务器连接失败，请稍后再试。'
  }
}

// 处理开始游戏
const handleStart = async () => {
  if (isGameStarted.value) return
  
  // 添加用户"开始"消息
  messages.value.push({ content: '开始', isAI: false })
  scrollToBottom()
  
  // 调用后端获取AI回复
  const aiResponse = await sendMessageToBackend('开始')
  
  // 添加AI回复
  messages.value.push({ content: aiResponse, isAI: true })
  
  // 更新状态
  isGameStarted.value = true
  isGameEnded.value = false
  
  // 检查是否游戏结束
  if (aiResponse.includes('游戏结束') || aiResponse.includes('游戏已结束')) {
    isGameEnded.value = true
    isGameStarted.value = false
    saveCurrentChat()
  }
  
  scrollToBottom()
}

// 处理结束游戏
const handleEnd = async () => {
  if (isGameEnded.value) return
  
  // 添加用户"结束游戏"消息
  messages.value.push({ content: '结束游戏', isAI: false })
  scrollToBottom()
  
  // 调用后端获取AI回复
  const aiResponse = await sendMessageToBackend('结束游戏')
  
  // 添加AI回复
  messages.value.push({ content: aiResponse, isAI: true })
  
  // 更新状态
  isGameEnded.value = true
  isGameStarted.value = false
  
  // 保存到历史记录
  saveCurrentChat()
  
  scrollToBottom()
}

// 处理发送消息
const handleSend = async () => {
  if (!userInput.value.trim() || !isGameStarted.value || isGameEnded.value) return
  
  const input = userInput.value.trim()
  
  // 添加用户消息
  messages.value.push({ content: input, isAI: false })
  
  // 清空输入框
  userInput.value = ''
  scrollToBottom()
  
  // 调用后端获取AI回复
  const aiResponse = await sendMessageToBackend(input)
  
  // 添加AI回复
  messages.value.push({ content: aiResponse, isAI: true })
  scrollToBottom()
  
  // 检查是否游戏结束
  if (aiResponse.includes('游戏结束') || aiResponse.includes('游戏已结束')) {
    isGameEnded.value = true
    isGameStarted.value = false
    saveCurrentChat()
  }
}

// 自动滚动到最新消息
const scrollToBottom = () => {
  nextTick(() => {
    if (messageContainer.value) {
      messageContainer.value.scrollTop = messageContainer.value.scrollHeight
    }
  })
}

// 监听用户输入"开始"
watch(userInput, (newVal) => {
  if (newVal.trim() === '开始' && !isGameStarted.value) {
    handleStart()
    userInput.value = ''
  }
})

// 页面加载时初始化
onMounted(() => {
  loadSavedChats() // 加载本地历史记录
})
</script>

<style scoped>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.chat-container {
  width: 100%;
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC', 'Hiragino Sans GB', sans-serif;
}

/* 顶部标题栏 */
.header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 16px 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  z-index: 10;
}

.header-content {
  max-width: 1400px;
  margin: 0 auto;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.logo {
  display: flex;
  align-items: center;
  gap: 12px;
}

.logo-icon {
  font-size: 32px;
  animation: float 3s ease-in-out infinite;
}

@keyframes float {
  0%, 100% { transform: translateY(0px); }
  50% { transform: translateY(-5px); }
}

.logo-text {
  font-size: 22px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.room-badge {
  display: flex;
  align-items: center;
  gap: 8px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  padding: 8px 16px;
  border-radius: 20px;
  color: white;
  font-weight: 500;
}

.room-label {
  opacity: 0.9;
  font-size: 14px;
}

.room-number {
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
}

/* 主内容区 */
.main-content {
  flex: 1;
  display: flex;
  gap: 20px;
  padding: 20px;
  max-width: 1400px;
  width: 100%;
  margin: 0 auto;
  overflow: hidden;
}

/* 左侧边栏 */
.sidebar {
  width: 280px;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  padding: 20px;
  border-bottom: 1px solid rgba(0, 0, 0, 0.06);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.sidebar-title {
  font-weight: 600;
  font-size: 16px;
  color: #333;
}

.new-chat-btn {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border: none;
  padding: 6px 12px;
  border-radius: 8px;
  font-size: 13px;
  cursor: pointer;
  transition: transform 0.2s;
}

.new-chat-btn:hover {
  transform: scale(1.05);
}

.chat-list {
  flex: 1;
  overflow-y: auto;
  padding: 8px;
}

.chat-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  margin-bottom: 4px;
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.2s;
}

.chat-item:hover {
  background: rgba(102, 126, 234, 0.08);
}

.chat-item.active {
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.15) 0%, rgba(118, 75, 162, 0.15) 100%);
}

.chat-item-icon {
  font-size: 24px;
  flex-shrink: 0;
}

.chat-item-content {
  flex: 1;
  min-width: 0;
}

.chat-item-title {
  font-weight: 500;
  font-size: 14px;
  color: #333;
  margin-bottom: 4px;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.chat-item-subtitle {
  font-size: 12px;
  color: #999;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.empty-history {
  padding: 40px 16px;
  color: #999;
  text-align: center;
  font-size: 14px;
}

/* 聊天区域 */
.chat-area {
  flex: 1;
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.1);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 消息列表 */
.messages-container {
  flex: 1;
  overflow-y: auto;
  padding: 24px;
  background: linear-gradient(to bottom, #f8f9ff 0%, #ffffff 100%);
}

.message {
  display: flex;
  gap: 12px;
  margin-bottom: 20px;
  align-items: flex-start;
  animation: fadeIn 0.3s ease-out;
}

@keyframes fadeIn {
  from {
    opacity: 0;
    transform: translateY(10px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.ai-message {
  justify-content: flex-start;
}

.user-message {
  justify-content: flex-end;
}

.avatar {
  width: 42px;
  height: 42px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
}

.ai-avatar {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.user-avatar {
  background: linear-gradient(135deg, #f093fb 0%, #f5576c 100%);
  color: white;
}

.message-bubble {
  max-width: 60%;
  padding: 14px 18px;
  border-radius: 18px;
  line-height: 1.6;
  font-size: 15px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
  word-wrap: break-word;
}

.ai-bubble {
  background: white;
  color: #333;
  border-top-left-radius: 4px;
}

.user-bubble {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-top-right-radius: 4px;
}

/* 输入区域 */
.input-section {
  padding: 20px;
  background: white;
  border-top: 1px solid rgba(0, 0, 0, 0.06);
}

.action-buttons {
  display: flex;
  gap: 12px;
  margin-bottom: 16px;
  justify-content: center;
}

.action-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 24px;
  border: none;
  border-radius: 12px;
  font-size: 15px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.2s;
}

.start-btn {
  background: linear-gradient(135deg, #11998e 0%, #38ef7d 100%);
  color: white;
}

.start-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(17, 153, 142, 0.3);
}

.end-btn {
  background: linear-gradient(135deg, #ee0979 0%, #ff6a00 100%);
  color: white;
}

.end-btn:hover:not(:disabled) {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(238, 9, 121, 0.3);
}

.action-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.btn-icon {
  font-size: 16px;
}

.input-wrapper {
  display: flex;
  gap: 12px;
}

.message-input {
  flex: 1;
  padding: 14px 20px;
  border: 2px solid rgba(102, 126, 234, 0.2);
  border-radius: 24px;
  font-size: 15px;
  outline: none;
  transition: all 0.2s;
  background: #f8f9ff;
}

.message-input:focus {
  border-color: #667eea;
  background: white;
  box-shadow: 0 0 0 4px rgba(102, 126, 234, 0.1);
}

.message-input::placeholder {
  color: #aaa;
}

.message-input:disabled {
  background: #f5f5f5;
  cursor: not-allowed;
}

.send-btn {
  width: 50px;
  height: 50px;
  border: none;
  border-radius: 50%;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  font-size: 20px;
  cursor: pointer;
  transition: all 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.send-btn:hover:not(:disabled) {
  transform: scale(1.1);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.send-btn:disabled {
  opacity: 0.5;
  cursor: not-allowed;
}

.send-icon {
  display: inline-block;
  transform: rotate(0deg);
}

/* 滚动条样式 */
.messages-container::-webkit-scrollbar,
.chat-list::-webkit-scrollbar {
  width: 6px;
}

.messages-container::-webkit-scrollbar-track,
.chat-list::-webkit-scrollbar-track {
  background: transparent;
}

.messages-container::-webkit-scrollbar-thumb,
.chat-list::-webkit-scrollbar-thumb {
  background: rgba(102, 126, 234, 0.3);
  border-radius: 3px;
}

.messages-container::-webkit-scrollbar-thumb:hover,
.chat-list::-webkit-scrollbar-thumb:hover {
  background: rgba(102, 126, 234, 0.5);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .sidebar {
    display: none;
  }
  
  .message-bubble {
    max-width: 80%;
  }
  
  .main-content {
    padding: 10px;
  }
}
</style>