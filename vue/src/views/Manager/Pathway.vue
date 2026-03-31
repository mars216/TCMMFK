<template>
  <div class="pathway-container">
    <el-card>
      <template #header>
        <div class="header-wrapper">
          <span class="title">Pathway Information</span>

          <div class="title-search">
            <div class="search-bar">
              <el-input
                  v-model="searchKeyword"
                  placeholder="Enter ID or Name"
                  size="small"
                  clearable
                  @keyup.enter.native="doSearch"
                  style="width: 300px; margin-right: 10px"
              />
              <el-button size="small" type="primary" @click="doSearch">Search</el-button>
              <el-button size="small" @click="resetSearch">Reset</el-button>
            </div>

            <div class="tip-container">
              <ul class="search-tip">
                <li>Pathway ID or Pathway Name</li>
                <li>Example: PATH000001, Cell surface interactions</li>
              </ul>
            </div>
          </div>
        </div>
      </template>

      <el-table :data="pagedData" style="width: 100%">
        <el-table-column label="Pathway ID" width="160">
          <template #default="scope">
            <el-link type="primary" :underline="false" @click="goToDetail(scope.row.id)">
              {{ scope.row.id }}
            </el-link>
          </template>
        </el-table-column>

        <el-table-column prop="name" label="Name" />
      </el-table>

      <div style="text-align: center; margin-top: 20px">
        <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredList.length"
            :page-size="pageSize"
            :current-page="currentPage"
            :page-sizes="[10, 20, 50, 100]"
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
  name: 'PathwayList',
  data() {
    return {
      pathways: [],
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
    this.loadData()
  },
  methods: {
    loadData() {
      request.get('/api/pathway/all').then(res => {
        if (res.code === '200') {
          this.pathways = res.data || []
          this.filteredList = res.data || []
        } else {
          this.$message.error(res.msg || 'Failed to fetch pathway information')
        }
      })
    },
    goToDetail(id) {
      this.$router.push(`/pathway/detail/${id}`)
    },
    doSearch() {
      const keyword = this.searchKeyword.trim().toLowerCase()
      this.filteredList = this.pathways.filter(item => {
        return (
            item.id?.toLowerCase().includes(keyword) ||
            item.name?.toLowerCase().includes(keyword)
        )
      })
      this.currentPage = 1
    },
    resetSearch() {
      this.searchKeyword = ''
      this.filteredList = this.pathways
      this.currentPage = 1
    },
    handleSizeChange(val) {
      this.pageSize = val
      this.currentPage = 1
    },
    handleCurrentChange(val) {
      this.currentPage = val
    }
  }
}
</script>

<style scoped>
.pathway-container {
  padding: 20px;
}

.title {
  font-size: 24px;
  font-weight: 700;
  color: #2a6f97;
  font-family: "Noto Serif SC", serif;
  padding: 8px 12px;
  background: linear-gradient(135deg, #e0f7fa 0%, #d0f0ff 100%);
  border-radius: 6px;
  margin-bottom: 10px;
  position: relative;
}

.title::before {
  content: "🧭";
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