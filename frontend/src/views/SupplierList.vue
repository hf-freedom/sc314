<template>
  <div class="supplier-list">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>供应商管理</span>
          <el-button type="primary" @click="showAddDialog">
            <el-icon><Plus /></el-icon>
            新增供应商
          </el-button>
        </div>
      </template>
      <el-table :data="suppliers" border stripe>
        <el-table-column prop="name" label="供应商名称" min-width="180" />
        <el-table-column prop="contactPerson" label="联系人" width="120" />
        <el-table-column prop="phone" label="电话" width="140" />
        <el-table-column prop="email" label="邮箱" width="180" />
        <el-table-column prop="score" label="信用评分" width="120">
          <template #default="{ row }">
            <el-progress :percentage="row.score" :color="getScoreColor(row.score)" :stroke-width="12" />
          </template>
        </el-table-column>
        <el-table-column prop="level" label="等级" width="80">
          <template #default="{ row }">
            <el-tag :type="getLevelTag(row.level)" size="large">{{ row.level }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="overdueCount" label="逾期次数" width="100" />
        <el-table-column prop="totalOverdueAmount" label="累计逾期金额" width="140" />
        <el-table-column prop="remark" label="备注" min-width="150" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button size="small" type="primary" @click="editSupplier(row)">编辑</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="供应商名称" prop="name">
          <el-input v-model="form.name" />
        </el-form-item>
        <el-form-item label="联系人" prop="contactPerson">
          <el-input v-model="form.contactPerson" />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" />
        </el-form-item>
        <el-form-item label="邮箱">
          <el-input v-model="form.email" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="submitForm">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import axios from 'axios'
import { ElMessage } from 'element-plus'

const suppliers = ref([])
const dialogVisible = ref(false)
const isEdit = ref(false)
const editingId = ref(null)
const formRef = ref(null)
const dialogTitle = ref('')

const form = ref({
  name: '',
  contactPerson: '',
  phone: '',
  email: '',
  remark: ''
})

const rules = {
  name: [{ required: true, message: '请输入供应商名称', trigger: 'blur' }],
  contactPerson: [{ required: true, message: '请输入联系人', trigger: 'blur' }],
  phone: [{ required: true, message: '请输入电话', trigger: 'blur' }]
}

const getScoreColor = (score) => {
  if (score >= 90) return '#67C23A'
  if (score >= 75) return '#409EFF'
  if (score >= 60) return '#E6A23C'
  return '#F56C6C'
}

const getLevelTag = (level) => {
  const map = { A: 'success', B: 'primary', C: 'warning', D: 'danger' }
  return map[level] || 'info'
}

const loadData = async () => {
  try {
    const res = await axios.get('/api/suppliers')
    suppliers.value = res.data.data
  } catch (e) {
    ElMessage.error('加载数据失败')
  }
}

const showAddDialog = () => {
  isEdit.value = false
  editingId.value = null
  dialogTitle.value = '新增供应商'
  form.value = {
    name: '',
    contactPerson: '',
    phone: '',
    email: '',
    remark: ''
  }
  dialogVisible.value = true
}

const editSupplier = (row) => {
  isEdit.value = true
  editingId.value = row.id
  dialogTitle.value = '编辑供应商'
  form.value = {
    name: row.name,
    contactPerson: row.contactPerson,
    phone: row.phone,
    email: row.email,
    remark: row.remark
  }
  dialogVisible.value = true
}

const submitForm = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
    if (isEdit.value) {
      await axios.put(`/api/suppliers/${editingId.value}`, form.value)
      ElMessage.success('更新成功')
    } else {
      await axios.post('/api/suppliers', form.value)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadData()
  } catch (e) {
    ElMessage.error(isEdit.value ? '更新失败' : '创建失败')
  }
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
