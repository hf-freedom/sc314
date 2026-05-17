<template>
  <div class="renewal-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>续签评估</span>
        </div>
      </template>
      <el-table :data="renewals" border stripe>
        <el-table-column prop="contractName" label="合同名称" min-width="180" />
        <el-table-column prop="expiryDate" label="到期日期" width="120" />
        <el-table-column prop="currentAmount" label="当前金额" width="130" />
        <el-table-column prop="expectedAmount" label="预期金额" width="130" />
        <el-table-column prop="performanceStatus" label="履约情况" width="120" />
        <el-table-column prop="riskAssessment" label="风险评估" width="120" />
        <el-table-column prop="renewalSuggestion" label="续签建议" min-width="150" />
        <el-table-column prop="approver" label="审批人" width="100" />
        <el-table-column prop="approvalStatus" label="审批状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.approvalStatus)" size="small">{{ row.approvalStatus }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="200" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看</el-button>
            <el-button size="small" type="success" @click="approve(row)" :disabled="row.approvalStatus !== 'PENDING'">批准</el-button>
            <el-button size="small" type="danger" @click="reject(row)" :disabled="row.approvalStatus !== 'PENDING'">拒绝</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="detailVisible" title="续签评估详情" width="700px">
      <el-descriptions :column="2" border v-if="currentRenewal">
        <el-descriptions-item label="合同名称">{{ currentRenewal.contractName }}</el-descriptions-item>
        <el-descriptions-item label="到期日期">{{ currentRenewal.expiryDate }}</el-descriptions-item>
        <el-descriptions-item label="当前金额">{{ currentRenewal.currentAmount }}</el-descriptions-item>
        <el-descriptions-item label="预期金额">{{ currentRenewal.expectedAmount }}</el-descriptions-item>
        <el-descriptions-item label="履约情况">{{ currentRenewal.performanceStatus }}</el-descriptions-item>
        <el-descriptions-item label="风险评估">{{ currentRenewal.riskAssessment }}</el-descriptions-item>
        <el-descriptions-item label="审批人">{{ currentRenewal.approver || '-' }}</el-descriptions-item>
        <el-descriptions-item label="审批状态">
          <el-tag :type="getStatusTag(currentRenewal.approvalStatus)">{{ currentRenewal.approvalStatus }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="续签建议" :span="2">{{ currentRenewal.renewalSuggestion }}</el-descriptions-item>
        <el-descriptions-item label="备注" :span="2">{{ currentRenewal.remark || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const renewals = ref([])
const detailVisible = ref(false)
const currentRenewal = ref(null)

const getStatusTag = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const loadData = async () => {
  try {
    const res = await axios.get('/api/renewals')
    renewals.value = res.data.data
  } catch (e) {
    ElMessage.error('加载数据失败')
  }
}

const viewDetail = (row) => {
  currentRenewal.value = row
  detailVisible.value = true
}

const approve = (row) => {
  ElMessageBox.prompt('请输入审批人姓名', '批准续签', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入审批人...',
    type: 'info',
    inputValidator: (value) => {
      if (!value) return '请输入审批人姓名'
      return true
    }
  }).then(async ({ value }) => {
    try {
      await axios.post(`/api/renewals/${row.id}/approve`, { approver: value })
      ElMessage.success('已批准')
      loadData()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const reject = (row) => {
  ElMessageBox.prompt('请输入拒绝原因和审批人', '拒绝续签', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入原因...',
    type: 'warning'
  }).then(async ({ value }) => {
    try {
      await axios.post(`/api/renewals/${row.id}/reject`, { approver: '系统', remark: value })
      ElMessage.success('已拒绝')
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
