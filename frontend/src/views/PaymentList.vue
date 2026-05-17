<template>
  <div class="payment-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>付款管理</span>
          <div>
            <el-button-group>
              <el-button @click="filterStatus = 'all'" :type="filterStatus === 'all' ? 'primary' : ''">全部</el-button>
              <el-button @click="filterStatus = 'PENDING'" :type="filterStatus === 'PENDING' ? 'primary' : ''">待付款</el-button>
              <el-button @click="filterStatus = 'PAID'" :type="filterStatus === 'PAID' ? 'primary' : ''">已付款</el-button>
              <el-button @click="filterStatus = 'OVERDUE'" :type="filterStatus === 'OVERDUE' ? 'primary' : ''">已逾期</el-button>
            </el-button-group>
          </div>
        </div>
      </template>
      <el-table :data="filteredPayments" border stripe>
        <el-table-column prop="nodeName" label="付款节点" width="150" />
        <el-table-column prop="contractName" label="所属合同" min-width="180">
          <template #default="{ row }">
            <div>
              <div>{{ getContractName(row.contractId) }}</div>
              <el-tag v-if="getContractStatus(row.contractId) === 'CLOSED'" type="info" size="small" style="margin-top: 3px">合同已关闭</el-tag>
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="paymentDate" label="应付款日期" width="130" />
        <el-table-column prop="amount" label="金额(元)" width="130" />
        <el-table-column prop="status" label="状态" width="130">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row)" size="small">
              {{ getStatusText(row) }}
            </el-tag>
            <div v-if="row.status === 'CANCELLED'" style="font-size: 12px; color: #909399; margin-top: 3px">
              （合同已关闭，自动终止）
            </div>
          </template>
        </el-table-column>
        <el-table-column prop="overdueDays" label="逾期天数" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isOverdue && row.status !== 'CANCELLED'" type="danger" size="small">{{ row.overdueDays }}天</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="actualPaymentDate" label="实际付款日期" width="130">
          <template #default="{ row }">
            {{ row.actualPaymentDate || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="success" @click="markAsPaid(row)" :disabled="row.status === 'PAID' || row.status === 'CANCELLED'">标记已付</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const payments = ref([])
const contracts = ref([])
const filterStatus = ref('all')

const filteredPayments = computed(() => {
  if (filterStatus.value === 'all') return payments.value
  if (filterStatus.value === 'OVERDUE') {
    return payments.value.filter(p => p.isOverdue && p.status === 'PENDING')
  }
  return payments.value.filter(p => p.status === filterStatus.value)
})

const getStatusTag = (row) => {
  if (row.isOverdue && row.status === 'PENDING') return 'danger'
  const map = { PENDING: 'warning', PAID: 'success', CANCELLED: 'info' }
  return map[row.status] || 'info'
}

const getStatusText = (row) => {
  if (row.isOverdue && row.status === 'PENDING') return '已逾期'
  const map = { PENDING: '待付款', PAID: '已付款', CANCELLED: '已终止' }
  return map[row.status] || row.status
}

const getContractName = (contractId) => {
  const c = contracts.value.find(c => c.id === contractId)
  return c ? c.name : '-'
}

const getContractStatus = (contractId) => {
  const c = contracts.value.find(c => c.id === contractId)
  return c ? c.status : ''
}

const loadData = async () => {
  try {
    const [payRes, contractRes] = await Promise.all([
      axios.get('/api/payments'),
      axios.get('/api/contracts')
    ])
    payments.value = payRes.data.data
    contracts.value = contractRes.data.data
  } catch (e) {
    ElMessage.error('加载数据失败')
  }
}

const markAsPaid = (row) => {
  ElMessageBox.confirm('确定要标记此付款节点为已付款吗？', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await axios.post(`/api/payments/${row.id}/pay`)
      ElMessage.success('标记成功')
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
