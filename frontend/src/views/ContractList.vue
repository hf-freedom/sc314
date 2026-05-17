<template>
  <div class="contract-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>合同管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            新增合同
          </el-button>
        </div>
      </template>

      <el-table :data="contracts" border stripe>
        <el-table-column prop="contractNo" label="合同编号" width="150" />
        <el-table-column prop="name" label="合同名称" min-width="180" />
        <el-table-column prop="partyA" label="甲方" width="150" />
        <el-table-column prop="partyB" label="乙方" width="150" />
        <el-table-column prop="effectiveDate" label="生效日期" width="120" />
        <el-table-column prop="expiryDate" label="到期日期" width="120" />
        <el-table-column prop="amount" label="金额(元)" width="130" />
        <el-table-column prop="responsiblePerson" label="负责人" width="100" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getStatusTag(row.status)" size="small">{{ getStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="高金额" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.isHighAmount" type="danger" size="small">是</el-tag>
            <el-tag v-else type="info" size="small">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="viewDetail(row)">查看</el-button>
            <el-button size="small" type="primary" @click="editContract(row)" :disabled="row.status === 'CLOSED'">变更</el-button>
            <el-button size="small" type="danger" @click="closeContract(row)" :disabled="row.status === 'CLOSED'">关闭</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="800px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="合同编号" prop="contractNo">
              <el-input v-model="form.contractNo" :disabled="isEdit" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同名称" prop="name">
              <el-input v-model="form.name" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="甲方" prop="partyA">
              <el-input v-model="form.partyA" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="乙方" prop="partyB">
              <el-input v-model="form.partyB" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="生效日期" prop="effectiveDate">
              <el-date-picker v-model="form.effectiveDate" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="到期日期" prop="expiryDate">
              <el-date-picker v-model="form.expiryDate" type="date" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="金额(元)" prop="amount">
              <el-input-number v-model="form.amount" :min="0" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人" prop="responsiblePerson">
              <el-input v-model="form.responsiblePerson" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="部门">
              <el-input v-model="form.department" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="合同类型">
              <el-select v-model="form.contractType" style="width: 100%">
                <el-option label="采购合同" value="采购" />
                <el-option label="销售合同" value="销售" />
                <el-option label="服务合同" value="服务" />
                <el-option label="租赁合同" value="租赁" />
                <el-option label="其他" value="其他" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="24">
            <el-form-item label="合同内容">
              <el-input v-model="form.content" type="textarea" :rows="3" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-divider>付款节点</el-divider>
        <el-button size="small" @click="addPaymentNode" type="primary" plain>
          <el-icon><Plus /></el-icon>
          添加付款节点
        </el-button>
        <el-table :data="form.paymentNodes" border size="small" style="margin-top: 10px">
          <el-table-column prop="nodeName" label="节点名称" width="150">
            <template #default="{ row }">
              <el-input v-model="row.nodeName" size="small" />
            </template>
          </el-table-column>
          <el-table-column prop="paymentDate" label="付款日期" width="180">
            <template #default="{ row }">
              <el-date-picker v-model="row.paymentDate" type="date" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column prop="amount" label="金额" width="180">
            <template #default="{ row }">
              <el-input-number v-model="row.amount" :min="0" size="small" style="width: 100%" />
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注">
            <template #default="{ row }">
              <el-input v-model="row.remark" size="small" />
            </template>
          </el-table-column>
          <el-table-column label="操作" width="80">
            <template #default="{ $index }">
              <el-button size="small" type="danger" @click="removePaymentNode($index)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-divider v-if="isEdit">变更信息</el-divider>
        <el-row v-if="isEdit" :gutter="20">
          <el-col :span="12">
            <el-form-item label="变更原因">
              <el-input v-model="form.changeReason" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="审批人">
              <el-input v-model="form.approver" />
            </el-form-item>
          </el-col>
        </el-row>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="detailVisible" title="合同详情" width="900px">
      <el-descriptions :column="2" border v-if="currentContract">
        <el-descriptions-item label="合同编号">{{ currentContract.contractNo }}</el-descriptions-item>
        <el-descriptions-item label="合同名称">{{ currentContract.name }}</el-descriptions-item>
        <el-descriptions-item label="甲方">{{ currentContract.partyA }}</el-descriptions-item>
        <el-descriptions-item label="乙方">{{ currentContract.partyB }}</el-descriptions-item>
        <el-descriptions-item label="生效日期">{{ currentContract.effectiveDate }}</el-descriptions-item>
        <el-descriptions-item label="到期日期">{{ currentContract.expiryDate }}</el-descriptions-item>
        <el-descriptions-item label="金额">{{ currentContract.amount }}</el-descriptions-item>
        <el-descriptions-item label="负责人">{{ currentContract.responsiblePerson }}</el-descriptions-item>
        <el-descriptions-item label="部门">{{ currentContract.department }}</el-descriptions-item>
        <el-descriptions-item label="合同类型">{{ currentContract.contractType }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="getStatusTag(currentContract.status)">{{ getStatusText(currentContract.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="当前版本">V{{ currentContract.currentVersion }}</el-descriptions-item>
        <el-descriptions-item label="合同内容" :span="2">{{ currentContract.content }}</el-descriptions-item>
      </el-descriptions>
      <el-divider>付款节点</el-divider>
      <el-table :data="currentContract?.paymentNodes || []" border size="small">
        <el-table-column prop="nodeName" label="节点名称" width="150" />
        <el-table-column prop="paymentDate" label="付款日期" width="120" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="getPaymentStatusTag(row.status)" size="small">{{ row.status }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overdueDays" label="逾期天数" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.isOverdue" type="danger" size="small">{{ row.overdueDays }}天</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" />
      </el-table>
      <el-divider>版本历史</el-divider>
      <el-alert
        title="变更说明"
        type="info"
        :closable="false"
        style="margin-bottom: 15px"
      >
        <template #default>
          <ul style="margin: 0; padding-left: 20px">
            <li>合同变更会自动保留旧版本记录</li>
            <li>新版本需要审批后才能正式生效</li>
            <li>审批通过后，合同信息将更新为新版本内容</li>
          </ul>
        </template>
      </el-alert>
      <el-table :data="currentContract?.versions || []" border size="small">
        <el-table-column prop="version" label="版本" width="80">
          <template #default="{ row }">
            <el-tag v-if="row.version === currentContract?.currentVersion" type="primary" size="small">
              V{{ row.version }} (当前)
            </el-tag>
            <span v-else>V{{ row.version }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="合同名称" width="150" />
        <el-table-column prop="amount" label="金额" width="120" />
        <el-table-column prop="effectiveDate" label="生效日期" width="120" />
        <el-table-column prop="expiryDate" label="到期日期" width="120" />
        <el-table-column prop="responsiblePerson" label="负责人" width="100" />
        <el-table-column prop="changeReason" label="变更原因" min-width="150">
          <template #default="{ row }">
            {{ row.changeReason || '初始版本' }}
          </template>
        </el-table-column>
        <el-table-column prop="approver" label="审批人" width="100">
          <template #default="{ row }">
            {{ row.approver || '-' }}
          </template>
        </el-table-column>
        <el-table-column prop="approvalStatus" label="审批状态" width="120">
          <template #default="{ row }">
            <el-tag :type="getApprovalStatusTag(row.approvalStatus)" size="small">
              {{ getApprovalStatusText(row.approvalStatus) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button
              size="small"
              type="success"
              @click="approveVersion(row)"
              :disabled="row.approvalStatus !== 'PENDING'"
            >
              审批通过
            </el-button>
            <el-button
              size="small"
              type="danger"
              @click="rejectVersion(row)"
              :disabled="row.approvalStatus !== 'PENDING'"
            >
              拒绝
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'

const contracts = ref([])
const dialogVisible = ref(false)
const detailVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const currentContract = ref(null)
const formRef = ref(null)

const dialogTitle = ref('')

const form = ref({
  contractNo: '',
  name: '',
  partyA: '',
  partyB: '',
  effectiveDate: '',
  expiryDate: '',
  amount: 0,
  responsiblePerson: '',
  department: '',
  contractType: '',
  content: '',
  paymentNodes: [],
  changeReason: '',
  approver: ''
})

const rules = {
  contractNo: [{ required: true, message: '请输入合同编号', trigger: 'blur' }],
  name: [{ required: true, message: '请输入合同名称', trigger: 'blur' }],
  partyA: [{ required: true, message: '请输入甲方', trigger: 'blur' }],
  partyB: [{ required: true, message: '请输入乙方', trigger: 'blur' }],
  effectiveDate: [{ required: true, message: '请选择生效日期', trigger: 'change' }],
  expiryDate: [{ required: true, message: '请选择到期日期', trigger: 'change' }],
  amount: [{ required: true, message: '请输入金额', trigger: 'blur' }],
  responsiblePerson: [{ required: true, message: '请输入负责人', trigger: 'blur' }]
}

const getStatusTag = (status) => {
  const map = { ACTIVE: 'success', EXPIRED: 'info', CLOSED: 'danger' }
  return map[status] || 'info'
}

const getStatusText = (status) => {
  const map = { ACTIVE: '进行中', EXPIRED: '已过期', CLOSED: '已关闭' }
  return map[status] || status
}

const getPaymentStatusTag = (status) => {
  const map = { PENDING: 'warning', PAID: 'success', CANCELLED: 'info' }
  return map[status] || 'info'
}

const getApprovalStatusTag = (status) => {
  const map = { PENDING: 'warning', APPROVED: 'success', REJECTED: 'danger' }
  return map[status] || 'info'
}

const getApprovalStatusText = (status) => {
  const map = { PENDING: '待审批', APPROVED: '已通过', REJECTED: '已拒绝' }
  return map[status] || status
}

const loadContracts = async () => {
  try {
    const res = await axios.get('/api/contracts')
    contracts.value = res.data.data
  } catch (e) {
    ElMessage.error('加载合同列表失败')
  }
}

const showAddDialog = () => {
  isEdit.value = false
  editingId.value = null
  dialogTitle.value = '新增合同'
  form.value = {
    contractNo: 'HT' + Date.now(),
    name: '',
    partyA: '',
    partyB: '',
    effectiveDate: '',
    expiryDate: '',
    amount: 0,
    responsiblePerson: '',
    department: '',
    contractType: '',
    content: '',
    paymentNodes: [],
    changeReason: '',
    approver: ''
  }
  dialogVisible.value = true
}

const editContract = (row) => {
  isEdit.value = true
  editingId.value = row.id
  dialogTitle.value = '合同变更（新版本待审批）'
  form.value = {
    contractNo: row.contractNo,
    name: row.name,
    partyA: row.partyA,
    partyB: row.partyB,
    effectiveDate: row.effectiveDate,
    expiryDate: row.expiryDate,
    amount: row.amount,
    responsiblePerson: row.responsiblePerson,
    department: row.department,
    contractType: row.contractType,
    content: row.content,
    paymentNodes: JSON.parse(JSON.stringify(row.paymentNodes || [])),
    changeReason: '',
    approver: ''
  }
  dialogVisible.value = true
}

const viewDetail = async (row) => {
  try {
    const res = await axios.get(`/api/contracts/${row.id}`)
    currentContract.value = res.data.data
    detailVisible.value = true
  } catch (e) {
    ElMessage.error('加载详情失败')
  }
}

const closeContract = (row) => {
  ElMessageBox.confirm('确定要关闭此合同吗？关闭后相关付款和提醒任务将自动终止。', '提示', {
    type: 'warning'
  }).then(async () => {
    try {
      await axios.post(`/api/contracts/${row.id}/close`)
      ElMessage.success('合同已关闭')
      loadContracts()
    } catch (e) {
      ElMessage.error('关闭失败')
    }
  }).catch(() => {})
}

const approveVersion = (versionRow) => {
  ElMessageBox.prompt('请输入审批人姓名', '审批通过版本 V' + versionRow.version, {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入审批人姓名...',
    type: 'success',
    inputValidator: (value) => {
      if (!value || value.trim() === '') return '请输入审批人姓名'
      return true
    }
  }).then(async ({ value }) => {
    try {
      await axios.post(`/api/contracts/versions/${versionRow.id}/approve`, {
        approver: value,
        contractId: currentContract.value.id
      })
      ElMessage.success('版本审批通过')
      viewDetail({ id: currentContract.value.id })
      loadContracts()
    } catch (e) {
      ElMessage.error('审批失败')
    }
  }).catch(() => {})
}

const rejectVersion = (versionRow) => {
  ElMessageBox.prompt('请输入拒绝原因', '拒绝版本 V' + versionRow.version, {
    confirmButtonText: '确定拒绝',
    cancelButtonText: '取消',
    inputPlaceholder: '请输入拒绝原因...',
    type: 'warning',
    inputValidator: (value) => {
      if (!value || value.trim() === '') return '请输入拒绝原因'
      return true
    }
  }).then(async ({ value }) => {
    try {
      await axios.post(`/api/contracts/versions/${versionRow.id}/reject`, {
        approver: '系统管理员',
        remark: value,
        contractId: currentContract.value.id
      })
      ElMessage.success('版本已拒绝')
      viewDetail({ id: currentContract.value.id })
      loadContracts()
    } catch (e) {
      ElMessage.error('操作失败')
    }
  }).catch(() => {})
}

const addPaymentNode = () => {
  form.value.paymentNodes.push({
    nodeName: '',
    paymentDate: '',
    amount: 0,
    remark: ''
  })
}

const removePaymentNode = (index) => {
  form.value.paymentNodes.splice(index, 1)
}

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    let resp
    if (isEdit.value) {
      resp = await axios.put(`/api/contracts/${editingId.value}`, form.value)
      ElMessage.success({
        message: `合同变更成功！\n生效日期: ${resp.data.data.effectiveDate}\n到期日期: ${resp.data.data.expiryDate}\n金额: ${resp.data.data.amount} 元\n负责人: ${resp.data.data.responsiblePerson}`,
        duration: 5000,
        offset: 50
      })
    } else {
      resp = await axios.post('/api/contracts', form.value)
      ElMessage.success({
        message: `合同创建成功！\n合同编号: ${resp.data.data.contractNo}\n生效日期: ${resp.data.data.effectiveDate}\n到期日期: ${resp.data.data.expiryDate}\n金额: ${resp.data.data.amount} 元\n负责人: ${resp.data.data.responsiblePerson}`,
        duration: 5000,
        offset: 50
      })
    }
    dialogVisible.value = false
    loadContracts()
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
}

onMounted(() => {
  loadContracts()
})
</script>

<style scoped>
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
