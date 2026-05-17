<template>
  <div class="dashboard">
    <el-row :gutter="20">
      <el-col :span="6" v-for="item in statCards" :key="item.title">
        <el-card class="stat-card" :body-style="{ padding: '20px' }">
          <div class="stat-content">
            <div class="stat-icon" :style="{ backgroundColor: item.color }">
            <el-icon :size="30">{{ item.icon }}</el-icon>
            </div>
            <div class="stat-info">
            <div class="stat-title">{{ item.title }}</div>
            <div class="stat-value">{{ item.value }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span class="card-header">即将到期合同</span>
          </template>
          <el-table :data="expiringContracts" border size="small">
            <el-table-column prop="name" label="合同名称" min-width="150" />
            <el-table-column prop="contractNo" label="合同编号" width="150" />
            <el-table-column prop="partyB" label="乙方" width="150" />
            <el-table-column prop="expiryDate" label="到期日期" width="120" />
            <el-table-column prop="amount" label="金额" width="120" />
            <el-table-column prop="responsiblePerson" label="负责人" width="100" />
            <el-table-column label="状态" width="100">
              <template #default="{ row }">
                <el-tag v-if="row.isHighAmount" type="danger" size="small">高金额</el-tag>
                <el-tag v-else type="info" size="small">普通</el-tag>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span class="card-header">待处理提醒</span>
          </template>
          <el-table :data="pendingReminders" border size="small">
            <el-table-column prop="type" label="类型" width="100">
              <template #default="{ row }">
                <el-tag :type="getReminderTypeTag(row.type)" size="small">{{ getReminderTypeText(row.type) }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="contractName" label="合同名称" min-width="150" />
            <el-table-column prop="title" label="标题" min-width="150" />
            <el-table-column prop="remindDate" label="提醒日期" width="120" />
            <el-table-column prop="priority" label="优先级" width="100">
              <template #default="{ row }">
                <el-tag :type="getPriorityTag(row.priority)" size="small">{{ row.priority }}</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="responsiblePerson" label="负责人" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="20" style="margin-top: 20px">
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span class="card-header">逾期付款</span>
          </template>
          <el-table :data="overduePayments" border size="small">
            <el-table-column prop="nodeName" label="付款节点" min-width="120" />
            <el-table-column prop="paymentDate" label="应付款日期" width="120" />
            <el-table-column prop="amount" label="金额" width="120" />
            <el-table-column prop="overdueDays" label="逾期天数" width="100">
              <template #default="{ row }">
                <el-tag type="danger" size="small">{{ row.overdueDays }}天</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="status" label="状态" width="100" />
          </el-table>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card class="chart-card">
          <template #header>
            <span class="card-header">供应商评级分布</span>
          </template>
          <el-table :data="suppliers" border size="small">
            <el-table-column prop="name" label="供应商名称" min-width="150" />
            <el-table-column prop="score" label="评分" width="100" />
            <el-table-column prop="level" label="等级" width="100">
              <template #default="{ row }">
                <el-tag :type="getLevelTag(row.level)" size="small">{{ row.level }}级</el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="overdueCount" label="逾期次数" width="100" />
            <el-table-column prop="contactPerson" label="联系人" width="100" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { Document, Money, Bell, OfficeBuilding } from '@element-plus/icons-vue'

const statistics = ref({})
const expiringContracts = ref([])
const pendingReminders = ref([])
const overduePayments = ref([])
const suppliers = ref([])

const statCards = ref([])

const loadData = async () => {
  try {
    const [statsRes, contractsRes, remindersRes, paymentsRes, suppliersRes] = await Promise.all([
      axios.get('/api/system/statistics'),
      axios.get('/api/contracts/expiring/90'),
      axios.get('/api/reminders/pending'),
      axios.get('/api/payments/overdue'),
      axios.get('/api/suppliers')
    ])
    statistics.value = statsRes.data.data
    expiringContracts.value = contractsRes.data.data.slice(0, 5)
    pendingReminders.value = remindersRes.data.data.slice(0, 5)
    overduePayments.value = paymentsRes.data.data.slice(0, 5)
    suppliers.value = suppliersRes.data.data.slice(0, 5)

    statCards.value = [
      { title: '合同总数', value: statistics.value.totalContracts, color: '#409EFF', icon: 'Document' },
      { title: '进行中合同', value: statistics.value.activeContracts, color: '#67C23A', icon: 'Document' },
      { title: '待付款', value: statistics.value.pendingPayments, color: '#E6A23C', icon: 'Money' },
      { title: '待处理提醒', value: statistics.value.pendingReminders, color: '#F56C6C', icon: 'Bell' },
      { title: '逾期付款', value: statistics.value.overduePayments, color: '#F56C6C', icon: 'Money' },
      { title: '供应商总数', value: statistics.value.totalSuppliers, color: '#909399', icon: 'OfficeBuilding' },
      { title: '待续签评估', value: statistics.value.pendingRenewals, color: '#409EFF', icon: 'RefreshRight' },
      { title: '高金额合同', value: statistics.value.highAmountContracts, color: '#F56C6C', icon: 'Document' }
    ]
  } catch (e) {
    console.error(e)
  }
}

const getReminderTypeTag = (type) => {
  const map = { PAYMENT: 'warning', EXPIRY: 'info', OVERDUE: 'danger' }
  return map[type] || 'info'
}

const getReminderTypeText = (type) => {
  const map = { PAYMENT: '付款', EXPIRY: '到期', OVERDUE: '逾期' }
  return map[type] || type
}

const getPriorityTag = (priority) => {
  const map = { HIGH: 'danger', MEDIUM: 'warning', LOW: 'info' }
  return map[priority] || 'info'
}

const getLevelTag = (level) => {
  const map = { A: 'success', B: 'primary', C: 'warning', D: 'danger' }
  return map[level] || 'info'
}

onMounted(() => {
  loadData()
})
</script>

<style scoped>
.stat-card {
  margin-bottom: 20px;
}

.stat-content {
  display: flex;
  align-items: center;
  gap: 15px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 10px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #fff;
}

.stat-info {
  flex: 1;
}

.stat-title {
  font-size: 14px;
  color: #909399;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
}

.chart-card {
  height: 100%;
}

.card-header {
  font-weight: bold;
  font-size: 16px;
}
</style>
