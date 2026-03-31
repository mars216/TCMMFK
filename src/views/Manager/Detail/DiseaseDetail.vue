<template>
  <div class="disease-detail-container">

    <el-card v-loading="loading" element-loading-text="正在加载疾病详情...">
      <template #header>
        <div class="card-header">
          <span class="card-title">疾病详情</span>
          <el-button size="small" @click="$router.back()">返回</el-button>
        </div>
      </template>

      <!-- 基本信息 -->
      <div class="section">
        <div class="section-title basic-info">
          <span class="icon">📋</span>
          <span class="text">基本信息</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>ID：</strong>{{ detail.id || '--' }}</p></el-col>
          <el-col :span="12"><p><strong>HERB名称：</strong>{{ detail.name || '--' }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>TCMSP ID：</strong>{{ detail.tcmspId || '--' }}</p></el-col>
          <el-col :span="12"><p><strong>TCMSP 名称：</strong>{{ detail.tcmspName || '--' }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>DisGeNET ID：</strong>{{ detail.disgenetDiseaseId || '--' }}</p></el-col>
          <el-col :span="12"><p><strong>HERB ID：</strong>{{ detail.herbId || '--' }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>疾病类型：</strong>{{ detail.diseaseType || '--' }}</p></el-col>
          <el-col :span="12"><p><strong>同义词：</strong>{{ (detail.synonyms || []).join('、') || '--' }}</p></el-col>
        </el-row>
      </div>

      <!-- 相关蛋白 -->
      <div v-if="detail.proteins?.length" class="section">
        <div class="section-title protein">
          <span class="icon">🧬</span>
          <span class="text">相关蛋白</span>
        </div>
        <el-table :data="pagedProteins" style="width: 100%">
          <el-table-column label="ID">
            <template slot-scope="scope">
              <el-link :underline="false" type="primary" @click="goToProtein(scope.row.id)">
                {{ scope.row.id }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="名称" prop="name">
            <template slot-scope="scope">
              {{ scope.row.name || '--' }}
            </template>
          </el-table-column>
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="detail.proteins.length"
              :page-size="pageSizeProtein"
              :current-page.sync="pageProtein"
              :page-sizes="[5, 10, 20, 50]"
              @size-change="val => pageSizeProtein = val"
              @current-change="val => pageProtein = val"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'DiseaseDetail',
  data() {
    return {
      detail: {},
      loading: false,
      pageProtein: 1,
      pageSizeProtein: 5
    }
  },
  computed: {
    pagedProteins() {
      const start = (this.pageProtein - 1) * this.pageSizeProtein
      return (this.detail.proteins || []).slice(start, start + this.pageSizeProtein)
    }
  },
  created() {
    const id = this.$route.params.id
    if (!id || id === 'undefined') {
      this.$message.error('未传入有效疾病ID')
      return
    }
    this.loadDetail(id)
  },
  methods: {
    loadDetail(id) {
      this.loading = true
      request.get(`/api/disease/detail/${id}`)
          .then(res => {
            if (res.code === '200') {
              this.detail = res.data.diseaseDTO || {}
              this.detail.proteins = res.data.proteins || []
            } else {
              this.$message.error(res.msg || '获取疾病详情失败')
            }
          })
          .catch(err => {
            console.error('加载出错:', err)
            this.$message.error('请求失败')
          })
          .finally(() => {
            this.loading = false
          })
    },
    goToProtein(id) {
      if (id) this.$router.push(`/protein/detail/${id}`)
    }
  }
}
</script>

<style scoped>
.disease-detail-container {
  padding: 20px;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.card-title {
  font-size: 24px;
  font-weight: bold;
  color: #166d88;
}
.section {
  margin-bottom: 24px;
}
.section-title {
  display: flex;
  align-items: center;
  font-size: 20px;
  font-weight: bold;
  border-radius: 6px;
  padding: 10px 16px;
  margin-bottom: 16px;
}
.section-title .icon {
  font-size: 22px;
  margin-right: 8px;
}
.pagination-wrapper {
  text-align: center;
  margin-top: 20px;
}
</style>
