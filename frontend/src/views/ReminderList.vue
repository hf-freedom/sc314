<template>
  <div class="reminder-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>提醒任务</span>
          <div>
            <el-button-group>
              <el-button @click="filterStatus = 'all'" :type="filterStatus === 'all' ? 'primary' : ''">全部</el-button>
              <el-button @click="filterStatus = 'PENDING'" :type="filterStatus === 'PENDING' ? 'primary' : ''">待处理</el-button>
              <el-button @click="filterStatus = 'COMPLETED'" :type="filterStatus === 'COMPLETED' ? 'primary' : ''">已完成</el-button>
              <el-button @click="filterStatus = 'CANCELLED'" :type="filterStatus === 'CANCELLED' ? 'primary' : ''">已取消</el-button>
            </el-button-group>
          </div>
        </div>
      </template>
      <el-table :data="filteredReminders" border stripe>
        <el-table-column prop="type" label="类型" width="100">
          <template #default="{ row }">
            <el-tag :type="getTypeTag(row.type)" size="small">{{ getTypeText(row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="contractName" label="合同名称" min-width="180">
          <template #default="{ row }">
            <div>
              <div>{{ row.contractName }}</div>
              <el-tag v-if="getContractStatus(row.contractId) === 'CLOSED'" type="info" size="small" style="margin-top: 3px">合同已关闭</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" min-width="180" />
        <el-table-column prop="content" label="内容" min-width="250" show-overflow-tooltip />
        <el-table-column prop="remindDate" label="提醒日期" width="120" />
        <el-table-column prop="priority" label="优先级" width="100">
          <template #default="{ row }">
            <el-tag :type="getPriorityTag(row.priority)" size="small">{{ row.priority }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="responsiblePerson" label="负责人" width="100" />
        <el-table-column prop="status" label="状态" width="140">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">
              {{ getStatusText(row.status) }}
            </el-tag>
            <div v-if="row.status === 'CANCELLED'" style="font-size: 12px; color: #909399; margin-top: 3px">
              （合同已关闭，自动终止）
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="warnLevel" label="预警级别" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.warnLevel && row.status !== 'CANCELLED'" type="danger" size="small">{{ row.warnLevel }}级</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="completeReminder(row)" :disabled="row.status !== 'PENDING'">完成</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox, ElInput } from 'element-plus'

const reminders = ref([])
const contracts = ref([])
const filterStatus = ref('all')

const filteredReminders = computed(() => {
  if (filterStatus.value === 'all') return reminders.value
  return reminders.value.filter(r => r.status === filterStatus.value)
})

const getContractStatus = (contractId) => {
  const c = contracts.value.find(c => c.id === contractId)
  return c ? c.status : ''
}

const getTypeTag = (type) => {
  const map = { PAYMENT: 'warning', EXPIRY: 'info', OVERDUE: 'danger' }
  return map[type] || 'info'
}

const getTypeText = (type) => {
  const map = { PAYMENT: '付款', EXPIRY: '到期', OVERDUE: '逾期' }
  return map[type] || type
}

const getPriorityTag = (priority) => {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }
  return map[priority] || 'info'
}

const getStatusTag = (status) => {
  const map = { PENDING: 'warning', COMPLETED: 'success', CANCELLED: 'info' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { PENDING: '待处理', COMPLETED: '已完成', CANCELLED: '已终止' }
  return map[status] || status
}

const loadData = async () => {
  try {
    const [remRes, contractRes] = await Promise.all([
      axios.get('/api/reminders'),
      axios.get('/api/contracts')
    ])
    reminders.value = remRes.data.data
    contracts.value = contractRes.data.data
  } catch (e) {
    ElMessage.error('加载数据失败')
  }
}

const completeReminder = (row) => {
  ElMessageBox.prompt('请输入处理备注（可选）', '完成提醒', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入备注...',
    type: 'info'
  }).then(async ({ value }) => {
    try {
      await axios.post(`/api/reminders/${row.id}/complete`, { remark: value })
      ElMessage.success('已完成')
      loadData()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
