<template>
  <div class="first-page-container">
    <div class="background-decoration">
      <div class="circle circle-1"></div>
      <div class="circle circle-2"></div>
      <div class="circle circle-3"></div>
    </div>
    
    <div class="content">
      <div class="logo-icon">🧠</div>
      <h1 class="title">AI 脑筋急转弯</h1>
      <p class="subtitle">挑战你的思维极限，与AI一起解谜</p>
      <button class="start-button" @click="handleStartGame">
        <span class="button-text">开始游戏</span>
        <span class="button-icon">→</span>
      </button>
    </div>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'

const router = useRouter()

// 生成随机房间号（100000-999999之间）
const generateRoomId = () => {
  return Math.floor(100000 + Math.random() * 900000)
}

const handleStartGame = () => {
  const roomId = generateRoomId()
  router.push(`/chat/${roomId}`)
}
</script>

<style scoped>
.first-page-container {
  position: relative;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100vh;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  overflow: hidden;
}

/* 背景装饰圆圈 */
.background-decoration {
  position: absolute;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.circle {
  position: absolute;
  border-radius: 50%;
  background: rgba(255, 255, 255, 0.1);
  animation: float-around 20s infinite ease-in-out;
}

.circle-1 {
  width: 300px;
  height: 300px;
  top: -100px;
  left: -100px;
  animation-delay: 0s;
}

.circle-2 {
  width: 200px;
  height: 200px;
  bottom: -50px;
  right: -50px;
  animation-delay: 5s;
}

.circle-3 {
  width: 150px;
  height: 150px;
  top: 50%;
  right: 10%;
  animation-delay: 10s;
}

@keyframes float-around {
  0%, 100% {
    transform: translate(0, 0) scale(1);
    opacity: 0.3;
  }
  25% {
    transform: translate(20px, -20px) scale(1.1);
    opacity: 0.5;
  }
  50% {
    transform: translate(-20px, 20px) scale(0.9);
    opacity: 0.4;
  }
  75% {
    transform: translate(15px, 15px) scale(1.05);
    opacity: 0.45;
  }
}

/* 内容区域 */
.content {
  position: relative;
  z-index: 1;
  text-align: center;
  animation: fadeInUp 1s ease-out;
}

@keyframes fadeInUp {
  from {
    opacity: 0;
    transform: translateY(30px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}

.logo-icon {
  font-size: 80px;
  margin-bottom: 20px;
  animation: bounce 2s ease-in-out infinite;
}

@keyframes bounce {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.title {
  font-size: 56px;
  font-weight: 800;
  color: white;
  margin-bottom: 16px;
  text-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  letter-spacing: 2px;
}

.subtitle {
  font-size: 18px;
  color: rgba(255, 255, 255, 0.9);
  margin-bottom: 50px;
  font-weight: 300;
  letter-spacing: 1px;
}

.start-button {
  position: relative;
  padding: 18px 48px;
  font-size: 20px;
  font-weight: 600;
  color: #667eea;
  background: white;
  border: none;
  border-radius: 50px;
  cursor: pointer;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
  transition: all 0.3s ease;
  display: inline-flex;
  align-items: center;
  gap: 12px;
  overflow: hidden;
}

.start-button::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(255, 255, 255, 0.5), transparent);
  transition: left 0.5s ease;
}

.start-button:hover::before {
  left: 100%;
}

.start-button:hover {
  transform: translateY(-4px) scale(1.05);
  box-shadow: 0 12px 32px rgba(0, 0, 0, 0.3);
}

.start-button:active {
  transform: translateY(-2px) scale(1.02);
}

.button-text {
  position: relative;
  z-index: 1;
}

.button-icon {
  position: relative;
  z-index: 1;
  font-size: 24px;
  transition: transform 0.3s ease;
}

.start-button:hover .button-icon {
  transform: translateX(5px);
}

/* 响应式设计 */
@media (max-width: 768px) {
  .title {
    font-size: 40px;
  }
  
  .subtitle {
    font-size: 16px;
    margin-bottom: 40px;
  }
  
  .logo-icon {
    font-size: 60px;
  }
  
  .start-button {
    padding: 16px 40px;
    font-size: 18px;
  }
  
  .circle-1 {
    width: 200px;
    height: 200px;
  }
  
  .circle-2 {
    width: 150px;
    height: 150px;
  }
  
  .circle-3 {
    width: 100px;
    height: 100px;
  }
}
</style>