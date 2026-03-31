<template>
  <div class="herb-container">
    <el-card>
      <template #header>
        <div class="header-wrapper">
          <span class="title">Herb Information</span>

          <div class="title-search">
            <div class="search-bar">
              <el-input
                  v-model="searchKeyword"
                  placeholder="Enter herb name or ID"
                  size="small"
                  clearable
                  @keyup.enter.native="doSearch"
                  style="width: 240px; margin-right: 10px"
              />
              <el-button size="small" type="primary" @click="doSearch">Search</el-button>
              <el-button size="small" @click="resetSearch">Reset</el-button>
            </div>

            <div class="tip-container">
              <ul class="search-tip">
                <li>Herb ID: e.g., HERB003444</li>
                <li>Chinese name: e.g., Ge Gen</li>
              </ul>
            </div>
          </div>
        </div>
      </template>

      <el-table :data="pagedHerbs" style="width: 100%">
        <el-table-column  label="Herb ID" >
          <template #default="scope">
            <el-link type="success" @click="goToHerbDetail(scope.row.id)">
              {{ scope.row.id }}
            </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="name" label="Chinese Name" >
          <template #default="scope">
          <el-link type="success" @click="goToHerbDetail(scope.row.id)">
            {{ scope.row.name }}
          </el-link>
          </template>
        </el-table-column>
        <el-table-column prop="herb_pinyin_name" label="Pinyin Name" />
        <el-table-column prop="herb_en_name" label="English Name" />
      </el-table>

      <div style="text-align: center; margin-top: 20px">
        <el-pagination
            background
            layout="total, sizes, prev, pager, next, jumper"
            :total="filteredHerbs.length"
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
  name: 'Herb',
  data() {
    return {
      herbList: [],
      filteredHerbs: [],
      searchKeyword: '',
      currentPage: 1,
      pageSize: 10
    }
  },
  computed: {
    pagedHerbs() {
      const start = (this.currentPage - 1) * this.pageSize
      return this.filteredHerbs.slice(start, start + this.pageSize)
    }
  },
  created() {
    this.loadHerbs()
  },
  methods: {
  loadHerbs() {
  request.get('/api/herb/all').then(res => {
    if (res.code === '200') {
      const rawData = res.data || [];
      // 判断是否为有效值：非空且不是字符串 "null"/"None"/"undefined"
      const isValid = (val) => val && val !== 'null' && val !== 'None' && val !== 'undefined';
      const validHerbs = rawData.filter(item => isValid(item.id) && isValid(item.name));
      this.herbList = validHerbs;
      this.filteredHerbs = validHerbs;
    } else {
      this.$message.error(res.msg || 'Failed to retrieve herb information');
    }
  });
},
    goToHerbDetail(id) {
      this.$router.push(`/herb/detail/${id}`)
    },
    doSearch() {
      const keyword = this.searchKeyword.trim().toLowerCase()
      this.filteredHerbs = this.herbList.filter(item =>
          item.id?.toLowerCase().includes(keyword) ||
          item.name?.toLowerCase().includes(keyword)
      )
      this.currentPage = 1
    },
    resetSearch() {
      this.searchKeyword = ''
      this.filteredHerbs = this.herbList
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
.herb-container {
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
  content: "🌿";
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