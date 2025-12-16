import { createRouter, createWebHistory } from 'vue-router'
import FirstPage from '@/views/FirstPage.vue'
import ChatRoom from '@/views/ChatRoom.vue'

const routes = [
  {
    path: '/',
    name: 'FirstPage',
    component: FirstPage
  },
  {
    path: '/chat/:roomId',
    name: 'ChatRoom',
    component: ChatRoom,
    props: true
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router