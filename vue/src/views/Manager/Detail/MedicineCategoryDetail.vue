<template>
  <div class="detail-container">
    <el-card>
      <div slot="header" class="card-header">
        <span class="title">Category Details</span>
        <el-button @click="$router.back()" size="small">Back</el-button>
      </div>

      <div v-if="categoryType === 'level1'">
        <h3>Level 1 Category Information</h3>
        <p><strong>ID:</strong> {{ detail.id }}</p>
        <p><strong>Name:</strong> {{ detail.name }}</p>
        <h4>Level 2 Category List:</h4>
        <el-table :data="detail.children || []" style="width: 100%">
          <el-table-column label="Level 2 Category ID">
            <template slot-scope="scope">
              <el-link type="primary" @click="goToDetail(scope.row.id)">
                {{ scope.row.id }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="Level 2 Category Name" />
        </el-table>
      </div>

      <div v-else-if="categoryType === 'level2'">
        <h3>Level 2 Category Information</h3>
        <p><strong>ID:</strong> {{ detail.id }}</p>
        <p><strong>Name:</strong> {{ detail.name }}</p>
        <h4>Herb List:</h4>
        <el-table :data="pagedHerbs" style="width: 100%">
          <el-table-column label="Herb ID">
            <template slot-scope="scope">
              <el-link type="success" @click="goToHerb(scope.row.id)">
                {{ scope.row.id }}
              </el-link>
            </template>
          </el-table-column>
          <el-table-column prop="name" label="Chinese Name" />
          <el-table-column prop="herb_pinyin_name" label="Pinyin Name" />
          <el-table-column prop="herb_en_name" label="English Name" />
        </el-table>

        <!-- Pagination -->
        <div style="text-align: center; margin-top: 20px">
          <el-pagination
              background
              layout="total, sizes, prev, pager, next, jumper"
              :total="filteredList.length"
              :page-size="pageSize"
              :current-page="currentPage"
              :page-sizes="[5, 10, 20, 50]"
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
          />
        </div>
      </div>
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'MedicineCategoryDetail',
  data() {
    return {
      detail: {},
      categoryType: '', // 'level1' or 'level2'
      currentPage: 1,
      pageSize: 5,
      filteredList: []
    }
  },
  computed: {
    pagedHerbs() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredList.slice(start, start + this.pageSize)
    }
  },
  created() {
    this.loadDetail(this.$route.params.id)
  },
  watch: {
    '$route.params.id'(newId) {
      this.loadDetail(newId)
    }
  },
  methods: {
    loadDetail(id) {
      request.get(`/api/medicine-category/detail/${id}`).then(res => {
        if (res.code === '200') {
          this.detail = res.data
          this.categoryType = res.data.herbs ? 'level2' : 'level1'

          if (this.categoryType === 'level2') {
            this.filteredList = res.data.herbs || []
            this.currentPage = 1
          }
        } else {
          this.$message.error(res.msg || 'Failed to fetch details')
        }
      })
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    handleCurrentChange(val) {
      this.currentPage = val
    },
    goToDetail(id) {
      this.$router.push(`/medicineCategory/detail/${id}`)
    },
    goToHerb(id) {
      this.$router.push(`/herb/detail/${id}`)
    }
  }
}
</script>

<style scoped>
.detail-container {
  padding: 20px;
}
.title {
  font-size: 22px;
  font-weight: bold;
  color: #2d6a4f;
}
.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>