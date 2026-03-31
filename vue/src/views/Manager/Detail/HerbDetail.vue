<template>
  <div class="herb-detail-container">

    <el-card v-loading="loading" element-loading-text="Loading herb details...">
      <div slot="header" class="card-header">
        <span class="card-title">Herb Details</span>
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
          <el-col :span="12"><p><strong>Name:</strong> {{ detail.name }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>Pinyin:</strong> {{ detail.herbPinyinName }}</p></el-col>
          <el-col :span="12"><p><strong>English Name:</strong> {{ detail.herbEnName }}</p></el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12"><p><strong>Source:</strong> {{ detail.source }}</p></el-col>
          <el-col :span="12"><p><strong>Nature & Flavor:</strong> {{ detail.property }}, {{ (detail.taste || []).join('、') }}</p></el-col>
        </el-row>
        <p><strong>Channel Tropism:</strong> {{ (detail.channelTropism || []).join('、') }}</p>
        <p><strong>Function:</strong> {{ detail.function }}</p>
        <p><strong>Indications:</strong> {{ detail.indications }}</p>
        <p><strong>Modern Research:</strong> {{ detail.modernResearch }}</p>
        <p><strong>Usage:</strong> {{ detail.usage }}</p>
        <p><strong>Introduction:</strong> {{ detail.introduction }}</p>
      </div>

      <!-- Related Ingredients -->
      <div v-if="pagedIngredients.length" class="section ">
        <div class="section-title ingredient">
          <span class="icon">🧪</span>
          <span class="text">Related Ingredients</span>
        </div>
        <el-table :data="pagedIngredients" style="width: 100%">
          <el-table-column label="ID">
            <template slot-scope="scope">
              <router-link :to="`/ingredient/detail/${scope.row.id}`" class="link">
                {{ scope.row.id }}
              </router-link>
            </template>
          </el-table-column>
          <el-table-column label="Ingredient Name">
            <template slot-scope="scope">
              <router-link :to="`/ingredient/detail/${scope.row.id}`" class="link">
                {{ scope.row.name }}
              </router-link>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrapper">
          <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="detail.ingredients.length"
              :page-size="pageSize1"
              :current-page.sync="page1"
              :page-sizes="[5, 10, 20, 50]"
              @size-change="val => pageSize1 = val"
              @current-change="val => page1 = val"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'

function paginate(list, page, size) {
  const start = (page - 1) * size
  return list.slice(start, start + size)
}

export default {
  name: 'HerbDetail',
  data() {
    return {
      detail: {},
      loading: false,
      page1: 1,
      pageSize1: 5
    }
  },
  computed: {
    pagedIngredients() {
      return paginate(this.detail.ingredients || [], this.page1, this.pageSize1)
    }
  },
  created() {
    const id = this.$route.params.id
    if (!id || id === 'undefined') {
      this.$message.error('No valid herb ID provided')
      return
    }
    this.loadHerbDetail(id)
  },
  methods: {
    loadHerbDetail(id) {
      this.loading = true
      request.get(`/api/herb/detail/${id}`)
          .then(res => {
            if (res.code === '200') {
              this.detail = res.data
            } else {
              console.error("Request failed, response:", res)
              this.$message.error(res.msg || 'Failed to retrieve herb details')
            }
          })
          .catch(error => {
            console.error("Request error:", error)
            this.$message.error('Request for herb details failed')
          })
          .finally(() => {
            this.loading = false
          })
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

/* Section block styles */
.section {
  margin-bottom: 24px;
  padding: 20px;
  border-radius: 8px;
}


.section-title .icon {
  font-size: 22px;
  margin-right: 8px;
}

/* Different color schemes */
.section-title.green {
  background: #e0f2e9;
  color: #2d6a4f;
}

.section-title.yellow {
  background: #fff3cc;
  color: #a15b00;
}

/* Table pagination centered */
.pagination-wrapper {
  text-align: center;
  margin-top: 20px;
}

/* Link styles */
.link {
  color: #409EFF;
  text-decoration: none;
}
.link:hover {
  text-decoration: underline;
}
</style>