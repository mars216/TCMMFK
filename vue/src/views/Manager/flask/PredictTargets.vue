<template>
  <div style="padding: 20px">
    <h2>🔮 Herb Target Prediction</h2>

    <!-- Step 1: Query herb information -->
    <el-card shadow="hover" style="margin-bottom: 20px">
      <div slot="header">
        <span>1. Query Herb Information</span>
      </div>
      <el-row :gutter="20">
        <el-col :span="6">
          <el-input
            v-model="herbName"
            placeholder="Enter herb name (e.g., 葛根)"
            clearable
            @keyup.enter="handleQuery"
          />
        </el-col>
        <el-col :span="4">
          <el-button type="primary" @click="handleQuery" :loading="queryLoading">Query</el-button>
        </el-col>
      </el-row>

      <!-- Display herb information -->
      <div v-if="herbInfo" style="margin-top: 20px">
        <el-descriptions title="Herb Information" :column="2" border>
          <el-descriptions-item label="Herb hit2_ID">{{ herbInfo.entity_id || 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="Name">{{ herbInfo.name || 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="English Name">{{ herbInfo.alias || 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="Description">{{ herbInfo.family || 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="Medicinal Part">{{ herbInfo.part || 'N/A' }}</el-descriptions-item>
          <el-descriptions-item label="Taste">{{ herbInfo.taste || 'N/A' }}</el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- Step 2: Predict targets (only show if herbInfo exists) -->
    <el-card v-if="herbInfo" shadow="hover">
      <div slot="header">
        <span>2. Predict Targets (customizable number)</span>
      </div>
      <el-row :gutter="20" style="margin-bottom: 20px">
        <el-col :span="6">
          <el-input-number
            v-model="topK"
            :min="1"
            :max="50"
            label="Prediction count"
            controls-position="right"
            style="width: 100%"
          ></el-input-number>
          <span style="color: #999; font-size: 12px; margin-left: 10px;">Max 50</span>
        </el-col>
        <el-col :span="4">
          <el-button type="success" @click="handlePredict" :loading="predictLoading">Start Prediction</el-button>
        </el-col>
      </el-row>

      <!-- Prediction results table -->
      <el-table
        v-if="predictions.length"
        :data="predictions"
        stripe
        border
        style="width: 100%"
      >
        <el-table-column type="index" label="Rank" width="60" />
        <el-table-column prop="entity_id" label="Protein ID" width="120" />
        <el-table-column prop="name" label="Name" width="200" />
        <el-table-column prop="description" label="Description" show-overflow-tooltip />
        <el-table-column prop="uniprot_id" label="UniProt ID" width="120" />
        <el-table-column prop="score" label="Confidence Score" width="120">
          <template slot-scope="scope">
            <span>{{ scope.row.score.toFixed(4) }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-else-if="!predictLoading" description="No prediction results yet" />
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'

const API_BASE = '/flask';

export default {
  data() {
    return {
      herbName: '',
      herbInfo: null,          // Store queried herb information
      topK: 50,                // Default prediction count
      queryLoading: false,
      predictLoading: false,
      predictions: []
    }
  },
  methods: {
    // Query herb information
    async handleQuery() {
      if (!this.herbName.trim()) {
        this.$message.warning('Please enter a herb name')
        return
      }
      this.queryLoading = true
      this.herbInfo = null      // Clear previous info
      this.predictions = []     // Clear previous predictions
      try {
        const res = await axios.get(`${API_BASE}/herb_info`, {
          params: { name: this.herbName }
        })
        if (res.data.code === 200) {
          this.herbInfo = res.data.data
          this.$message.success('Query successful')
        } else {
          this.$message.error(res.data.msg || 'Query failed')
        }
      } catch (err) {
        this.$message.error('Request failed: ' + (err.response?.data?.msg || err.message))
      } finally {
        this.queryLoading = false
      }
    },

    // Start prediction
    async handlePredict() {
      if (!this.herbInfo) {
        this.$message.warning('Please query herb information first')
        return
      }
      this.predictLoading = true
      try {
        const res = await axios.post(`${API_BASE}/predict_targets`, {
          herb_name: this.herbName,   // Use name, backend will find entity ID
          top_k: this.topK
        })
        if (res.data.code === 200) {
          this.predictions = res.data.data.predictions
          this.$message.success('Prediction completed')
        } else {
          this.$message.error(res.data.msg || 'Prediction failed')
        }
      } catch (err) {
        this.$message.error('Request failed: ' + (err.response?.data?.msg || err.message))
      } finally {
        this.predictLoading = false
      }
    }
  }
}
</script>

<style scoped>
.el-card {
  margin-bottom: 20px;
}
.el-input-number {
  width: 100%;
}
</style>