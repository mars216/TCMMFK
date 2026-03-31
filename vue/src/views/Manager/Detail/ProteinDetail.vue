<template>
  <div class="herb-detail-container">

    <el-card v-loading="loading" element-loading-text="Loading protein details...">
      <div slot="header" class="card-header">
        <span class="card-title">Protein Details</span>
        <el-button size="small" @click="$router.back()">Back</el-button>
      </div>

      <!-- Basic Information -->
      <div class="section">
        <div class="section-title basic-info">
          <span class="icon">📋</span>
          <span class="text">Basic Information</span>
        </div>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>ID:</strong> {{ detail.id }}</p></el-col>
          <el-col :span="12"><p><strong>Name:</strong> {{ detail.name || '--' }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>TCMSP ID:</strong> {{ detail.tcmspId }}</p></el-col>
          <el-col :span="12"><p><strong>TCMSP Name:</strong> {{ detail.tcmspName || '--' }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>Gene ID:</strong> {{ detail.geneId || '--' }}</p></el-col>
          <el-col :span="12"><p><strong>Gene Name:</strong> {{ detail.geneName || '--' }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>Gene Alias:</strong> {{ (detail.geneAlias || []).join('、') || '--' }}</p></el-col>
          <el-col :span="12"><p><strong>Chromosome Location:</strong> {{ detail.chromosome || '--' }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>DB Xrefs:</strong> {{ (detail.dbXrefs || []).join('、') || '--' }}</p></el-col>
          <el-col :span="12"><p><strong>Map Location:</strong> {{ detail.mapLocation || '--' }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>Description:</strong> {{ detail.description || '--' }}</p></el-col>
          <el-col :span="12"><p><strong>Gene Type:</strong> {{ detail.typeOfGene || '--' }}</p></el-col>
        </el-row>
      </div>

      <!-- Related Ingredients -->
      <div v-if="detail.ingredients?.length" class="section">
        <div class="section-title ingredient">
          <span class="icon">🧪</span>
          <span class="text">Related Ingredients</span>
        </div>
        <el-table :data="pagedIngredients" style="width: 100%">
          <el-table-column label="ID">
            <template slot-scope="scope">
              <el-link :underline="false" type="primary" @click="goToIngredient(scope.row.id)">
                {{ scope.row.id }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="Name" prop="name" />
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="detail.ingredients.length"
              :page-size="pageSizeIngredient"
              :current-page.sync="pageIngredient"
              :page-sizes="[5, 10, 20, 50]"
              @size-change="val => pageSizeIngredient = val"
              @current-change="val => pageIngredient = val"
          />
        </div>
      </div>

      <!-- Related Diseases -->
      <div v-if="detail.diseases?.length" class="section">
        <div class="section-title disease">
          <span class="icon">🧬</span>
          <span class="text">Related Diseases</span>
        </div>
        <el-table :data="pagedDiseases" style="width: 100%">
          <el-table-column label="ID">
            <template slot-scope="scope">
              <el-link :underline="false" type="danger" @click="goToDisease(scope.row.id)">
                {{ scope.row.id }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="Name" prop="name" />
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="detail.diseases.length"
              :page-size="pageSizeDisease"
              :current-page.sync="pageDisease"
              :page-sizes="[5, 10, 20, 50]"
              @size-change="val => pageSizeDisease = val"
              @current-change="val => pageDisease = val"
          />
        </div>
      </div>

      <!-- Related Pathways -->
      <div v-if="detail.pathways?.length" class="section">
        <div class="section-title pathway">
          <span class="icon">🧭</span>
          <span class="text">Related Pathways</span>
        </div>
        <el-table :data="pagedPathways" style="width: 100%">
          <el-table-column label="ID">
            <template slot-scope="scope">
              <el-link :underline="false" type="warning" @click="goToPathway(scope.row.id)">
                {{ scope.row.id }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column label="Name" prop="name" />
        </el-table>
        <div class="pagination-wrapper">
          <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="detail.pathways.length"
              :page-size="pageSizePathway"
              :current-page.sync="pagePathway"
              :page-sizes="[5, 10, 20, 50]"
              @size-change="val => pageSizePathway = val"
              @current-change="val => pagePathway = val"
          />
        </div>
      </div>

    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'ProteinDetail',
  data() {
    return {
      detail: {},
      loading: false,
      pageIngredient: 1,
      pageSizeIngredient: 5,
      pageDisease: 1,
      pageSizeDisease: 5,
      pagePathway: 1,
      pageSizePathway: 5
    }
  },
  computed: {
    pagedIngredients() {
      const start = (this.pageIngredient - 1) * this.pageSizeIngredient
      return (this.detail.ingredients || []).slice(start, start + this.pageSizeIngredient)
    },
    pagedDiseases() {
      const start = (this.pageDisease - 1) * this.pageSizeDisease
      return (this.detail.diseases || []).slice(start, start + this.pageSizeDisease)
    },
    pagedPathways() {
      const start = (this.pagePathway - 1) * this.pageSizePathway
      return (this.detail.pathways || []).slice(start, start + this.pageSizePathway)
    }
  },
  created() {
    const id = this.$route.params.id
    if (!id || id === 'undefined') {
      this.$message.error('No valid protein ID provided')
      return
    }
    this.loadDetail(id)
  },
  methods: {
    loadDetail(id) {
      this.loading = true
      request.get(`/api/protein/detail/${id}`)
          .then(res => {
            if (res.code === '200') {
              this.detail = res.data.proteinDTO || {}
              this.detail.ingredients = res.data.ingredients || []
              this.detail.diseases = res.data.diseases || []
              this.detail.pathways = res.data.pathways || []
            } else {
              this.$message.error(res.msg || 'Failed to fetch protein details')
            }
          })
          .catch(err => {
            console.error('Loading error:', err)
            this.$message.error('Request failed')
          })
          .finally(() => {
            this.loading = false
          })
    },
    goToIngredient(id) {
      if (id) this.$router.push(`/ingredient/detail/${id}`)
    },
    goToDisease(id) {
      if (id) this.$router.push(`/disease/detail/${id}`)
    },
    goToPathway(id) {
      if (id) this.$router.push(`/pathway/detail/${id}`)
    }
  }
}
</script>

<style scoped>
.herb-detail-container {
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
  color: #2d6a4f;
}
.section {
  margin-bottom: 24px;
  padding: 0;
}
.section-title {
  display: flex;
  align-items: center;
  width: 100%;
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