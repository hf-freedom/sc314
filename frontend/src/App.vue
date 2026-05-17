<template>
  <el-container class="app-container">
    <el-header class="app-header">
      <div class="header-title">
        <el-icon><Document /></el-icon>
        <span>企业合同到期预警系统</span>
      </div>
      <div class="header-user">
        <el-button type="primary" @click="scanSystem">
          <el-icon><Refresh /></el-icon>
          扫描系统
        </el-button>
      </div>
    </el-header>
    <el-container>
      <el-aside width="220px" class="app-aside">
        <el-menu
          :default-active="activeMenu"
          router
          class="aside-menu"
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
        >
          <el-menu-item index="/">
            <el-icon><DataAnalysis /></el-icon>
            <span>数据概览</span>
          </el-menu-item>
          <el-menu-item index="/contracts">
            <el-icon><Document /></el-icon>
            <span>合同管理</span>
          </el-menu-item>
          <el-menu-item index="/payments">
            <el-icon><Money /></el-icon>
            <span>付款管理</span>
          </el-menu-item>
          <el-menu-item index="/reminders">
            <el-icon><Bell /></el-icon>
            <span>提醒任务</span>
          </el-menu-item>
          <el-menu-item index="/renewals">
            <el-icon><RefreshRight /></el-icon>
            <span>续签评估</span>
          </el-menu-item>
          <el-menu-item index="/suppliers">
            <el-icon><OfficeBuilding /></el-icon>
            <span>供应商管理</span>
          </el-menu-item>
        </el-menu>
      </el-aside>
      <el-main class="app-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import axios from 'axios'

const route = useRoute()
const router = useRouter()
const activeMenu = ref(route.path)

const scanSystem = async () => {
  try {
    await axios.post('/api/system/scan')
    ElMessage.success('系统扫描完成')
    router.go(0)
  } catch (e) {
    ElMessage.error('扫描失败')
  }
}
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body, html, #app {
  width: 100%;
  height: 100%;
}

.app-container {
  height: 100vh;
}

.app-header {
  background-color: #fff;
  border-bottom: 1px solid #e6e6e6;
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 0 20px;
}

.header-title {
  font-size: 20px;
  font-weight: bold;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 10px;
}

.app-aside {
  background-color: #304156;
}

.aside-menu {
  border-right: none;
  height: 100%;
}

.app-main {
  background-color: #f0f2f5;
  padding: 20px;
  overflow-y: auto;
}
</style>
