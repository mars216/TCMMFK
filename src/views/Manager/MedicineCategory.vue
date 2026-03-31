<template>
  <div class="category-container">
    <el-card>
      <template #header>
        <div class="header-wrapper">
          <span class="title">类目信息</span>

          <div class="title-search">
            <div class="search-bar">
              <el-input
                  v-model="searchKeyword"
                  placeholder="输入类目ID或名称"
                  size="small"
                  clearable
                  @keyup.enter.native="doSearch"
                  style="width: 240px; margin-right: 10px"
              />
              <el-button size="small" type="primary" @click="doSearch">搜索</el-button>
              <el-button size="small" @click="resetSearch">重置</el-button>
            </div>

            <div class="tip-container">
              <ul class="search-tip">
                <li>一级类目 ID 或名称</li>
                <li>二级类目 ID 或名称</li>
                <li>例如：C001 解表药、C00102 辛凉解表药</li>
              </ul>
            </div>
          </div>
        </div>
      </template>

      <el-table :data="pagedData" style="width: 100%">
        <el-table-column label="一级类目 ID">
          <template #default="scope">
            <el-link type="primary" @click="goDetail(scope.row.level1Id)">
              {{ scope.row.level1Id }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column label="一级类目名称">
          <template #default="scope">
            <el-link type="primary" @click="goDetail(scope.row.level1Id)">
              {{ scope.row.level1Name }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column label="二级类目 ID">
          <template #default="scope">
            <el-link type="success" @click="goDetail(scope.row.level2Id)">
              {{ scope.row.level2Id }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column label="二级类目名称">
          <template #default="scope">
            <el-link type="success" @click="goDetail(scope.row.level2Id)">
              {{ scope.row.level2Name }}
            </el-link>
          </template>
        </el-table-column>
      </el-table>

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
    </el-card>
  </div>
</template>

<script>
import request from '@/utils/request'

export default {
  name: 'MedicineCategory',
  data() {
    return {
      categoryList: [],
      filteredList: [],
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10
    }
  },
  computed: {
    pagedData() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredList.slice(start, start + this.pageSize)
    }
  },
  created() {
    this.loadCategory()
  },
  methods: {
    loadCategory() {
      request.get('/api/medicine-category/all').then(res => {
        if (res.code === '200') {
          this.categoryList = res.data
          this.filteredList = res.data
        } else {
          this.$message.error(res.msg || '获取类目信息失败')
        }
      })
    },
    doSearch() {
      const keyword = this.searchKeyword.trim().toLowerCase()
      this.filteredList = this.categoryList.filter(item =>
          item.level1Id?.toLowerCase().includes(keyword) ||
          item.level1Name?.toLowerCase().includes(keyword) ||
          item.level2Id?.toLowerCase().includes(keyword) ||
          item.level2Name?.toLowerCase().includes(keyword)
      )
      this.currentPage = 1
    },
    resetSearch() {
      this.searchKeyword = ''
      this.filteredList = this.categoryList
      this.currentPage = 1
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    handleCurrentChange(val) {
      this.currentPage = val
    },
    goDetail(id) {
      this.$router.push(`/medicineCategory/detail/${id}`)
    }
  }
}
</script>

<style scoped>
.category-container {
  padding: 20px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #2d6a4f;
  font-family: "Noto Serif SC", serif;
  padding: 8px 12px;
  background: linear-gradient(135deg, #f0f7ee 0%, #e6f2e0 100%);
  border-radius: 6px;
  margin-bottom: 10px;
  position: relative;
}

.title::before {
  content: "📂";
  margin-right: 8px;
}

.header-wrapper {
  display: flex;
  flex-direction: column;
}

.title-search {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  gap: 10px;
  margin-top: 10px;
}

.tip-container {
  align-self: flex-start;
  margin-left: 4px;
}

.search-tip {
  font-size: 13px;
  color: #888;
  line-height: 1.6;
  list-style-type: disc;
  padding-left: 20px;
  background: #f9f9f9;
  border-radius: 6px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08);
}
</style>
